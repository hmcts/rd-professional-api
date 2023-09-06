package uk.gov.hmcts.reform.professionalapi.dataload.processor;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static org.apache.commons.lang.BooleanUtils.isNotTrue;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static uk.gov.hmcts.reform.professionalapi.dataload.util.DataLoadUtil.isStringArraysEqual;

import com.opencsv.CSVReader;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.BOMInputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.professionalapi.dataload.exception.RouteFailedException;
import uk.gov.hmcts.reform.professionalapi.dataload.route.beans.RouteProperties;
import uk.gov.hmcts.reform.professionalapi.dataload.util.MappingConstants;

/**
 * Validate headers in CSV file (limited Only more no headers currently).
 *
 * @since 2020-10-27
 */
@Component
public class HeaderValidationProcessor implements Processor {

    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    CamelContext camelContext;


    @Override
    public void process(Exchange exchange) throws Exception {

        RouteProperties routeProperties = (RouteProperties) exchange.getIn().getHeader(MappingConstants.ROUTE_DETAILS);
        InputStream csvInputStream = exchange.getIn().getBody(InputStream.class);

        BOMInputStream bomInputStream = new BOMInputStream(csvInputStream);

        String csv = IOUtils.toString(bomInputStream, "UTF-8");

        CSVReader reader = new CSVReader(new StringReader(csv));
        String[] actualCsvHeaders = reader.readNext();
        String isHeaderValidationEnabled = routeProperties.getIsHeaderValidationEnabled();
        String expectedCsvHeaders = routeProperties.getCsvHeadersExpected();

        Predicate<String> checkIfHeaderValidationEnabled = (isEnabled) -> isEnabled != null
                && isEnabled.equalsIgnoreCase(Boolean.TRUE.toString());
        boolean isEnabled = checkIfHeaderValidationEnabled.test(isHeaderValidationEnabled);

        if (isEnabled && isNotBlank(expectedCsvHeaders)) {
            String[] expectedHeaders = expectedCsvHeaders.split(MappingConstants.COMA);
            if (isNotTrue(isStringArraysEqual(expectedHeaders, actualCsvHeaders))) {
                throwRouteFailedException(exchange, routeProperties);
            }
        }

        Field[] allFields = applicationContext.getBean(routeProperties.getBinder())
            .getClass().getDeclaredFields();
        List<Field> csvFields = new ArrayList<>();

        for (Field field : allFields) {
            if (!java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
                csvFields.add(field);
            }
        }

        //Auditing in database if headers are missing
        if ((isBlank(expectedCsvHeaders) || !isEnabled) && actualCsvHeaders.length > csvFields.size()) {
            throwRouteFailedException(exchange, routeProperties);
        }

        InputStream inputStream = new ByteArrayInputStream(csv.getBytes(Charset.forName("UTF-8")));

        exchange.getMessage().setBody(inputStream);
    }

    private void throwRouteFailedException(Exchange exchange, RouteProperties routeProperties) {
        exchange.getIn().setHeader(MappingConstants.HEADER_EXCEPTION, MappingConstants.HEADER_EXCEPTION);
        camelContext.getGlobalOptions().put(MappingConstants.FILE_NAME, routeProperties.getFileName());
        throw new RouteFailedException("There is a mismatch in the headers of the csv file :: "
                + routeProperties.getFileName());
    }
}
