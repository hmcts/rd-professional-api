package uk.gov.hmcts.reform.professionalapi.dataload.util;

import org.apache.camel.Exchange;
import org.springframework.context.ApplicationContext;
import uk.gov.hmcts.reform.professionalapi.dataload.route.beans.RouteProperties;

import static uk.gov.hmcts.reform.professionalapi.dataload.util.DataLoadUtil.getFileDetails;
import static uk.gov.hmcts.reform.professionalapi.dataload.util.DataLoadUtil.registerFileStatusBean;
import static uk.gov.hmcts.reform.professionalapi.dataload.util.MappingConstants.ROUTE_DETAILS;

public class PrdLoadUtils {

    private PrdLoadUtils() {
    }

    public static void setFileStatus(Exchange exchange, ApplicationContext applicationContext, String auditStatus) {
        var routeProperties = (RouteProperties) exchange.getIn().getHeader(ROUTE_DETAILS);
        var fileStatus = getFileDetails(exchange.getContext(), routeProperties.getFileName());
        fileStatus.setAuditStatus(auditStatus);
        registerFileStatusBean(applicationContext, routeProperties.getFileName(), fileStatus,
            exchange.getContext()
        );
    }
}
