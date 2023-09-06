package uk.gov.hmcts.reform.professionalapi.dataload.processor;

import static uk.gov.hmcts.reform.professionalapi.dataload.util.DataLoadUtil.getFileDetails;
import static uk.gov.hmcts.reform.professionalapi.dataload.util.DataLoadUtil.registerFileStatusBean;
import static uk.gov.hmcts.reform.professionalapi.dataload.util.MappingConstants.ROUTE_DETAILS;
import static uk.gov.hmcts.reform.professionalapi.dataload.util.MappingConstants.SCHEDULER_STATUS;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.professionalapi.dataload.route.beans.FileStatus;
import uk.gov.hmcts.reform.professionalapi.dataload.route.beans.RouteProperties;

@Component
public class FileResponseProcessor implements Processor {


    @Autowired
    ApplicationContext applicationContext;


    @Override
    public void process(Exchange exchange) {
        final String status = exchange.getContext().getGlobalOption(SCHEDULER_STATUS);
        RouteProperties routeProperties = (RouteProperties) exchange.getIn().getHeader(ROUTE_DETAILS);
        String fileName = routeProperties.getFileName();
        FileStatus fileStatus = getFileDetails(exchange.getContext(), fileName);
        fileStatus.setExecutionStatus(status);
        registerFileStatusBean(applicationContext, fileName, fileStatus, exchange.getContext());
    }
}
