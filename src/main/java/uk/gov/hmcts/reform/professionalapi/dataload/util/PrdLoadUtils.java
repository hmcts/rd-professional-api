package uk.gov.hmcts.reform.professionalapi.dataload.util;

import org.apache.camel.Exchange;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;
import uk.gov.hmcts.reform.professionalapi.dataload.route.beans.RouteProperties;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.professionalapi.dataload.util.DataLoadUtil.getFileDetails;
import static uk.gov.hmcts.reform.professionalapi.dataload.util.DataLoadUtil.registerFileStatusBean;
import static uk.gov.hmcts.reform.professionalapi.dataload.util.MappingConstants.DATE_TIME_FORMAT;
import static uk.gov.hmcts.reform.professionalapi.dataload.util.MappingConstants.ROUTE_DETAILS;

public class PrdLoadUtils {

    private PrdLoadUtils() {
    }

    public static <T> List<T> filterDomainObjects(List<T> domainObjects, Predicate<T> predicate) {
        return domainObjects.stream()
            .filter(predicate).collect(Collectors.toList());
    }

    public static void setFileStatus(Exchange exchange, ApplicationContext applicationContext, String auditStatus) {
        var routeProperties = (RouteProperties) exchange.getIn().getHeader(ROUTE_DETAILS);
        var fileStatus = getFileDetails(exchange.getContext(), routeProperties.getFileName());
        fileStatus.setAuditStatus(auditStatus);
        registerFileStatusBean(applicationContext, routeProperties.getFileName(), fileStatus,
            exchange.getContext()
        );
    }

    public static Timestamp getDateTimeStamp(String dateTime) {
        if (!StringUtils.isBlank(dateTime)) {
            LocalDateTime ldt = LocalDateTime.parse(dateTime,
                DateTimeFormatter.ofPattern(DATE_TIME_FORMAT));
            return Timestamp.valueOf(ldt);
        }
        return null;
    }
}
