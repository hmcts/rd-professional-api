package uk.gov.hmcts.reform.professionalapi.dataload.util;

import org.apache.camel.CamelContext;

public interface IRouteExecutor {

    String execute(CamelContext camelContext, String schedulerName, String route);
}
