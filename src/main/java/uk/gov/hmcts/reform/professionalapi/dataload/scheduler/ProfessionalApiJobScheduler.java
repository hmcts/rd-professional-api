package uk.gov.hmcts.reform.professionalapi.dataload.scheduler;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.Route;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.professionalapi.dataload.route.DataLoadRoute;
import uk.gov.hmcts.reform.professionalapi.dataload.util.PrdDataExecutor;
import uk.gov.hmcts.reform.professionalapi.repository.PrdDataloadSchedulerJobRepository;
import uk.gov.hmcts.reform.professionalapi.repository.PrdSchedularAuditRepository;
import uk.gov.hmcts.reform.professionalapi.util.PrdDataLoadSchedulerAudit;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static java.time.LocalDateTime.now;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static uk.gov.hmcts.reform.professionalapi.dataload.util.MappingConstants.IS_READY_TO_AUDIT;
import static uk.gov.hmcts.reform.professionalapi.dataload.util.MappingConstants.SCHEDULER_START_TIME;

@Component
@Slf4j
@NoArgsConstructor
@SuppressWarnings("all")
public class ProfessionalApiJobScheduler {

    @Value("${prd.scheduler.enabled:true}")
    private boolean isSchedulerEnabled;

    @Value("${professional-user-details-start-route}")
    private String startRoute;

    @Value("${professional-user-details-routes-to-execute}")
    List<String> routesToExecute;

    @Autowired
    CamelContext camelContext;

    @Autowired
    PrdDataloadSchedulerJobRepository prdDataloadSchedulerJob;

    @Autowired
    PrdSchedularAuditRepository prdSchedularAuditRepository;

    @Autowired
    PrdDataLoadSchedulerAudit prdDataLoadSchedulerAudit;

    @Autowired
    DataLoadRoute dataLoadRoute;

    @Autowired
    PrdDataExecutor commonDataExecutor;

    @Autowired
    protected ProducerTemplate producerTemplate;

    @Value("${logging-component-name}")
    String logComponentName;

    @Value("${batchjob-name}")
    String jobName;

    int i=0;

    @Scheduled(cron = "${prd.scheduler.cronExpression}")
    @SchedulerLock(name = "lockedTask", lockAtMostFor = "${prd.scheduler.lockAtMostFor}",
        lockAtLeastFor = "${prd.scheduler.lockAtLeastFor}")
    public void loadProfessioanlDataJob()throws Exception  {

        log.info("PRD load started......."+isSchedulerEnabled);
        Boolean doAudit=Boolean.TRUE;
        if (isSchedulerEnabled) {
            LocalDateTime jobStartTime = now();

            LocalDateTime latestEntry = prdSchedularAuditRepository.findLatestSchedularEndTime();

            System.out.println("Value of Latestnetyr: "+latestEntry);

            if(Optional.ofNullable(latestEntry).isPresent()) {

                LocalDate LastRunDate = latestEntry.toLocalDate();
                LocalDate currentDate = jobStartTime.toLocalDate();

                if (currentDate.equals(LastRunDate)) {
                    log.info("PRD load failed since job has already ran for the day");
                    return;
                }

            }

            log.info("ProfessionalApiJobScheduler.loadPrdData Job execution in progress");
            if (i==0)
            {
                loadPrdData_fresh(Boolean.TRUE);
            }
            else
            {
                loadPrdData(Boolean.TRUE);
            }
            log.info("ProfessionalApiJobScheduler.loadPrdData Job execution completed successful");

        }
    }

    private void loadPrdData(Boolean aTrue) throws Exception {

        aTrue = (isEmpty(aTrue)) ? Boolean.FALSE : aTrue;
        camelContext.start();
        camelContext.getGlobalOptions().put(SCHEDULER_START_TIME, String.valueOf(new Date().getTime()));
        camelContext.getGlobalOptions().put(IS_READY_TO_AUDIT, aTrue.toString());
        var status = commonDataExecutor.execute(camelContext, "PRD Route", startRoute);

    }

    private void loadPrdData_fresh(Boolean doAudit) throws Exception {
        log.info("Started to load the data");
        doAudit = (isEmpty(doAudit)) ? Boolean.FALSE : doAudit;
        camelContext.getGlobalOptions().put(SCHEDULER_START_TIME, String.valueOf(new Date().getTime()));
        camelContext.getGlobalOptions().put(IS_READY_TO_AUDIT, doAudit.toString());
        /*commonDataExecutor=applicationContext.getBean(PrdDataExecutor.class);*/
        String status="";
        camelContext.start();
        dataLoadRoute.startRoute(startRoute, routesToExecute);
        List<Route> routes = camelContext.getRoutes();
        routes.stream().forEach(route -> {
            route.getId();
            route.getEndpoint().getEndpointUri();

        });
        status = commonDataExecutor.execute(camelContext, "PRD Route", startRoute);


        log.info("{}:: Route Task completes with status::{}", logComponentName, status);
        //TODO camel context need to be closed properly
        /*dataLoadRoute.stopRoute(startRoute, routesToExecute);*/

        /*var stopStatus=commonDataExecutor.stop(camelContext, "PRD Route", startRoute);*/

        i=1;
    }


}

