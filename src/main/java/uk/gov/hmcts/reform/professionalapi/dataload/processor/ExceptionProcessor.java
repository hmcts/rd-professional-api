package uk.gov.hmcts.reform.professionalapi.dataload.processor;

import java.util.Map;

import static org.apache.camel.Exchange.EXCEPTION_CAUGHT;
import static org.apache.commons.lang3.exception.ExceptionUtils.getStackTrace;
import static uk.gov.hmcts.reform.professionalapi.dataload.util.DataLoadUtil.getFileDetails;
import static uk.gov.hmcts.reform.professionalapi.dataload.util.DataLoadUtil.registerFileStatusBean;
import static uk.gov.hmcts.reform.professionalapi.dataload.util.MappingConstants.ERROR_MESSAGE;
import static uk.gov.hmcts.reform.professionalapi.dataload.util.MappingConstants.FILE_NAME;
import static uk.gov.hmcts.reform.professionalapi.dataload.util.MappingConstants.ROUTE_DETAILS;
import static uk.gov.hmcts.reform.professionalapi.dataload.util.MappingConstants.FAILURE;
import static uk.gov.hmcts.reform.professionalapi.dataload.util.MappingConstants.TABLE_NAME;

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.professionalapi.dataload.route.beans.FileStatus;
import uk.gov.hmcts.reform.professionalapi.dataload.route.beans.RouteProperties;
import uk.gov.hmcts.reform.professionalapi.dataload.service.IAuditService;

@Component
@Slf4j
public class ExceptionProcessor implements Processor {

    @Autowired
    CamelContext camelContext;

    @Value("${logging-component-name:data_ingestion}")
    private String logComponentName;

    @Autowired
    FileResponseProcessor fileResponseProcessor;

    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    IAuditService auditService;

    @Override
    public void process(Exchange exchange) throws Exception {
        final Map<String, String> globalOptions = exchange.getContext().getGlobalOptions();

        Exception exception = (Exception) exchange.getProperty(EXCEPTION_CAUGHT);
        log.error("{}:: exception in route for data processing:: {}", logComponentName, getStackTrace(exception));
        RouteProperties routeProperties = (RouteProperties) exchange.getIn().getHeader(ROUTE_DETAILS);
        FileStatus fileStatus = getFileDetails(exchange.getContext(), routeProperties.getFileName());
        fileStatus.setAuditStatus(FAILURE);
        registerFileStatusBean(applicationContext, routeProperties.getFileName(), fileStatus, camelContext);

        globalOptions.put(ERROR_MESSAGE, exception.getMessage());
        globalOptions.put(FILE_NAME, routeProperties.getFileName());
        globalOptions.put(TABLE_NAME, routeProperties.getTableName());

        auditService.auditException(camelContext, exception.getMessage());
        fileResponseProcessor.process(exchange);
    }
}
