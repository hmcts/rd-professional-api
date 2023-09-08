package uk.gov.hmcts.reform.professionalapi.dataload.route;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import static java.util.Objects.nonNull;
import static org.apache.commons.lang.WordUtils.uncapitalize;
import static org.apache.logging.log4j.util.Strings.isBlank;
import static uk.gov.hmcts.reform.professionalapi.dataload.util.MappingConstants.DIRECT_ROUTE;
import static uk.gov.hmcts.reform.professionalapi.dataload.util.MappingConstants.IS_FILE_STALE;
import static uk.gov.hmcts.reform.professionalapi.dataload.util.MappingConstants.IS_PARENT_FAILED;
import static uk.gov.hmcts.reform.professionalapi.dataload.util.MappingConstants.MAPPING_METHOD;
import static uk.gov.hmcts.reform.professionalapi.dataload.util.MappingConstants.SQL_DELIMITER;
import static uk.gov.hmcts.reform.professionalapi.dataload.util.MappingConstants.TRUNCATE_ROUTE_PREFIX;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Expression;
import org.apache.camel.FailedToCreateRouteException;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.BindyType;
import org.apache.camel.model.language.SimpleExpression;
import org.apache.camel.spring.spi.SpringTransactionPolicy;
import org.apache.commons.lang.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.professionalapi.dataload.processor.CommonCsvFieldProcessor;
import uk.gov.hmcts.reform.professionalapi.dataload.processor.ExceptionProcessor;
import uk.gov.hmcts.reform.professionalapi.dataload.processor.FileReadProcessor;
import uk.gov.hmcts.reform.professionalapi.dataload.processor.FileResponseProcessor;
import uk.gov.hmcts.reform.professionalapi.dataload.processor.HeaderValidationProcessor;
import uk.gov.hmcts.reform.professionalapi.dataload.processor.ParentStateCheckProcessor;
import uk.gov.hmcts.reform.professionalapi.dataload.route.beans.RouteProperties;
import uk.gov.hmcts.reform.professionalapi.dataload.util.MappingConstants;

@Component
public class DataLoadRoute {

    @Autowired
    FileReadProcessor fileReadProcessor;

    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    Environment environment;

    @Autowired
    ExceptionProcessor exceptionProcessor;

    @Autowired
    CamelContext camelContext;

    @Autowired
    HeaderValidationProcessor headerValidationProcessor;

    @Autowired
    FileResponseProcessor fileResponseProcessor;

    @Autowired
    DataSource dataSource;

    @Autowired
    ParentStateCheckProcessor parentStateCheckProcessor;

    @Autowired
    CommonCsvFieldProcessor commonCsvFieldProcessor;

    @Transactional
    public void startRoute(String startRoute, List<String> routesToExecute) throws FailedToCreateRouteException {

        List<RouteProperties> routePropertiesList = getRouteProperties(routesToExecute);

        try {
            camelContext.addRoutes(
                new RouteBuilder() {
                    @Override
                    public void configure() throws Exception {

                        onException(Exception.class)
                            .handled(true)
                            .process(exceptionProcessor)
                            .end()
                            .markRollbackOnly()
                            .end();


                        String[] multiCastRoute = createDirectRoutesForMulticast(routesToExecute);

                        //Started direct route with multi-cast all the configured routes with
                        //Transaction propagation required eg.application-jrd-router.yaml(rd-judicial-data-load)
                        from(startRoute)
                            .multicast()
                            .to(multiCastRoute).end();

                        for (RouteProperties route : routePropertiesList) {

                            Expression exp = new SimpleExpression(route.getBlobPath());
                            List<String> sqls = new ArrayList<>();
                            int loopCount = getLoopCount(route, sqls);

                            Optional<String> updateSqlOptional = route.getUpdateSql();

                            from(DIRECT_ROUTE + route.getRouteName()).id(DIRECT_ROUTE + route.getRouteName())
                                //.transacted("PROPAGATION_REQUIRED")
                                .process(headerValidationProcessor)
                                .split(body()).unmarshal().bindy(BindyType.Csv, applicationContext.getBean(route.getBinder()).getClass())
                                .process(commonCsvFieldProcessor)
                                .process((Processor) applicationContext.getBean(route.getProcessor()))
                                .loop(loopCount)
                                //delete & Insert process
                                .split().body()
                                .streaming()
                                .bean(applicationContext.getBean(route.getMapper()), MAPPING_METHOD)
                                .process(exchange -> setHeader(exchange, "sqlToExecute", sqls))
                                .toD("${header.sqlToExecute}")
                                .end()
                                .process(fileResponseProcessor)
                                .setHeader("isUpdateSQL", updateSqlOptional::isEmpty)
                                .choice()
                                .when(header("isUpdateSQL").isEqualTo(false))
                                .setHeader("updateSQLs", updateSqlOptional::get)
                                .recipientList(header("updateSQLs"), SQL_DELIMITER)
                                .end()
                                .end()
                                .end(); //end route

                            //Route reads file, truncates and then call main file route via below line
                            //to(DIRECT_ROUTE + route.getRouteName())
                            //with Spring Propagation new for each file
                            from(DIRECT_ROUTE + TRUNCATE_ROUTE_PREFIX + route.getRouteName())
                              //  .transacted("PROPAGATION_REQUIRED")
                                .setHeader(MappingConstants.ROUTE_DETAILS, () -> route)
                                //checks parent failure status & set header
                                .process(parentStateCheckProcessor)
                                .choice()
                                .when(header(IS_PARENT_FAILED).isEqualTo(false))
                                .setProperty(MappingConstants.BLOBPATH, exp)
                                .process(fileReadProcessor)
                                .choice()
                                .when(header(IS_FILE_STALE).isEqualTo(false))
                                .to(route.getTruncateSql())
                                .to(DIRECT_ROUTE + route.getRouteName())
                                .endChoice()
                                .endChoice()
                                .end();
                        }
                    }
                });
        } catch (Exception ex) {
            throw new FailedToCreateRouteException(" Data Load - failed to start for route ", startRoute,
                startRoute, ex);
        }
    }

    private void setHeader(Exchange exchange, String headerName, List<String> sqls) {
        Integer index = (Integer) exchange.getProperty(Exchange.LOOP_INDEX);
        exchange.getIn().setHeader(headerName, sqls.get(index));
    }

    private String[] createDirectRoutesForMulticast(List<String> routeList) {
        int index = 0;
        String[] directRouteNameList = new String[routeList.size()];
        for (String child : routeList) {
            directRouteNameList[index] = (DIRECT_ROUTE).concat(TRUNCATE_ROUTE_PREFIX).concat(child);
            index++;
        }
        return directRouteNameList;
    }

    /**
     * Sets Route Properties.
     *
     * @param routes routes
     * @return List RouteProperties.
     */
    private List<RouteProperties> getRouteProperties(List<String> routes) {
        List<RouteProperties> routePropertiesList = new LinkedList<>();
        int index = 0;
        for (String routeName : routes) {
            RouteProperties properties = new RouteProperties();
            properties.setStartRoute(environment.getProperty(MappingConstants.START_ROUTE));
            properties.setRouteName(environment.getProperty(
                MappingConstants.ROUTE + "." + routeName + "." + MappingConstants.ID));
            properties.setSql(environment.getProperty(
                MappingConstants.ROUTE + "." + routeName + "." + MappingConstants.INSERT_SQL));
            properties.setUpdateSql(Optional.ofNullable(environment.getProperty(
                MappingConstants.ROUTE + "." + routeName + "." + MappingConstants.UPDATE_SQL)));
            properties.setTruncateSql(environment.getProperty(
                MappingConstants.ROUTE + "." + routeName + "." + MappingConstants.TRUNCATE_SQL)
                == null ? "log:test" : environment.getProperty(
                MappingConstants.ROUTE + "." + routeName + "." + MappingConstants.TRUNCATE_SQL));
            properties.setBlobPath(environment.getProperty(
                MappingConstants.ROUTE + "." + routeName + "." + MappingConstants.BLOBPATH));
            properties.setMapper(uncapitalize(environment.getProperty(
                MappingConstants.ROUTE + "." + routeName + "." + MappingConstants.MAPPER)));
            properties.setBinder(uncapitalize(environment.getProperty(MappingConstants.ROUTE + "." + routeName + "."
                + MappingConstants.CSVBINDER)));
            properties.setProcessor(uncapitalize(environment.getProperty(MappingConstants.ROUTE + "." + routeName + "."
                + MappingConstants.PROCESSOR)));
            properties.setFileName(environment.getProperty(
                MappingConstants.ROUTE + "." + routeName + "." + MappingConstants.FILE_NAME));
            properties.setTableName(environment.getProperty(
                MappingConstants.ROUTE + "." + routeName + "." + MappingConstants.TABLE_NAME));
            properties.setCsvHeadersExpected(environment.getProperty(
                MappingConstants.ROUTE + "." + routeName + "." + MappingConstants.CSV_HEADERS_EXPECTED));
            String isHeaderValidationEnabled = environment.getProperty(
                MappingConstants.ROUTE + "." + routeName + "." + MappingConstants.IS_HEADER_VALIDATION_ENABLED);
            if (isBlank(isHeaderValidationEnabled)) {
                isHeaderValidationEnabled = Boolean.FALSE.toString();
            }
            properties.setIsHeaderValidationEnabled(isHeaderValidationEnabled);
            routePropertiesList.add(index, properties);
            properties.setDeleteSql(environment.getProperty(MappingConstants.ROUTE + "."
                + routeName + "." + MappingConstants.DELETE_SQL));
            String parentFailureEnabled = environment.getProperty(MappingConstants.ROUTE + "."
                + routeName + "." + MappingConstants.PARENT_FAILURE_ENABLED);
            if (nonNull(parentFailureEnabled)) {
                properties.setParentFailureEnabled(BooleanUtils.toBoolean(parentFailureEnabled));
            }
            properties.setParentFileName(environment.getProperty(MappingConstants.ROUTE + "."
                + routeName + "." + MappingConstants.PARENT_NAME));
            index++;
        }
        return routePropertiesList;
    }

    private int getLoopCount(RouteProperties route, List<String> sqls) {
        if ((nonNull(route.getDeleteSql()))) {
            sqls.add(route.getDeleteSql());
        }
        sqls.add(route.getSql());
        return sqls.size();
    }
}
