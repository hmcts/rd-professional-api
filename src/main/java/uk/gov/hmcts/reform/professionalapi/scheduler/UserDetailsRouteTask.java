package uk.gov.hmcts.reform.professionalapi.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.CamelContext;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.data.ingestion.camel.route.DataLoadRoute;

import javax.annotation.PostConstruct;
import java.util.List;

@Component
@Slf4j
@SuppressWarnings("unchecked")
public class UserDetailsRouteTask implements Tasklet {

    private static final String ORCHESTRATED_ROUTE = "parent-route";


    private  static final String JUDICIAL_REF_DATA_ORCHESTRATION = "judicial-ref-data-orchestration";

    @Value("${professional-user-details-start-route}")
    String startRoute;

    @Value("${professional-user-details-routes-to-execute}")
    List<String> routesToExecute;

    @Autowired
    CommonDataExecutor commonDataExecutor;

    @Autowired
    CamelContext camelContext;

//    @Autowired
//    DataLoadRoute dataLoadRoute;

    @Value("${logging-component-name}")
    String logComponentName;

    @PostConstruct
    public void init() {
        camelContext.getGlobalOptions().put(ORCHESTRATED_ROUTE, JUDICIAL_REF_DATA_ORCHESTRATION);
//        dataLoadRoute.startRoute(startRoute, routesToExecute);
    }


    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        String status =  commonDataExecutor.execute(camelContext, JUDICIAL_REF_DATA_ORCHESTRATION, startRoute);

        log.info("{}:: LeafRouteTask completes with {}::", logComponentName, status);
        return RepeatStatus.FINISHED;
    }
}
