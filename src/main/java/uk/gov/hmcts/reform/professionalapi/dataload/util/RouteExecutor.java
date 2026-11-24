package uk.gov.hmcts.reform.professionalapi.dataload.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

import static uk.gov.hmcts.reform.professionalapi.dataload.util.MappingConstants.SUCCESS;

@Slf4j
@Component
public abstract class RouteExecutor implements IRouteExecutor {

    @Autowired
    protected CamelContext camelContext;

    @Autowired
    protected DataLoadUtil dataLoadUtil;

    @Autowired
    protected ProducerTemplate producerTemplate;

    @Override
    public String execute(CamelContext camelContext, String schedulerName, String route) {
        Map<String, String> globalOptions = camelContext.getGlobalOptions();
        globalOptions.remove(MappingConstants.IS_EXCEPTION_HANDLED);
        globalOptions.remove(MappingConstants.SCHEDULER_STATUS);
        dataLoadUtil.setGlobalConstant(camelContext, schedulerName);
        producerTemplate.start();
        producerTemplate.sendBody(route, "starting " + schedulerName);
        return SUCCESS;
    }

    @Override
    public String stop(CamelContext camelContext, String schedulerName, String route) throws Exception {
        Map<String, String> globalOptions = camelContext.getGlobalOptions();
        globalOptions.remove(MappingConstants.IS_EXCEPTION_HANDLED);
        globalOptions.remove(MappingConstants.SCHEDULER_STATUS);
        globalOptions.remove(MappingConstants.SCHEDULER_START_TIME);
        camelContext.removeRoute(route);
        dataLoadUtil.removeGlobalConstant(camelContext);
        producerTemplate.stop();
        return SUCCESS;
    }
}
