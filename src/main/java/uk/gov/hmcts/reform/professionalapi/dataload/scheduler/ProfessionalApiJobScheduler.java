package uk.gov.hmcts.reform.professionalapi.dataload.scheduler;


import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.apache.camel.CamelContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.professionalapi.dataload.route.DataLoadRoute;
import uk.gov.hmcts.reform.professionalapi.dataload.util.PrdDataExecutor;
import uk.gov.hmcts.reform.professionalapi.domain.PrdDataloadSchedulerJob;
import uk.gov.hmcts.reform.professionalapi.repository.PrdDataloadSchedulerJobRepository;
import uk.gov.hmcts.reform.professionalapi.util.PrdDataLoadSchedulerAudit;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

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
    PrdDataLoadSchedulerAudit prdDataLoadSchedulerAudit;

    @Autowired
    DataLoadRoute dataLoadRoute;

    @Autowired
    PrdDataExecutor commonDataExecutor;
    /*@Autowired
    DataIngestionLibraryRunner dataIngestionLibraryRunner;*/

    @Value("${logging-component-name}")
    String logComponentName;

    @Value("${batchjob-name}")
    String jobName;


    @Scheduled(cron = "${prd.scheduler.cronExpression}")
    @SchedulerLock(name = "lockedTask", lockAtMostFor = "${prd.scheduler.lockAtMostFor}",
            lockAtLeastFor = "${prd.scheduler.lockAtLeastFor}")
    public void loadProfessioanlDataJob()throws Exception  {

        log.info("PRD load started....."+isSchedulerEnabled);
        if (isSchedulerEnabled) {
            LocalDateTime jobStartTime = now();

//            PrdDataloadSchedulerJob latestEntry = prdDataloadSchedulerJob.findFirstByOrderByIdDesc();

//            System.out.println("Value of Latestnetyr: "+latestEntry);

//            if(Optional.ofNullable(latestEntry).isPresent()) {

//                LocalDate startDate = Optional.ofNullable(latestEntry.getJobStartTime()).isPresent() ? latestEntry
//                        .getJobStartTime().toLocalDate() : null;
//                LocalDate endDate = Optional.ofNullable(latestEntry.getJobEndTime()).isPresent() ? latestEntry
//                        .getJobEndTime().toLocalDate() : null;
//                LocalDate currentDate = jobStartTime.toLocalDate();
//
//                if (currentDate.equals(startDate) || currentDate.equals(endDate)) {
//                    log.info("PRD load failed since job has already ran for the day");
//                }

                PrdDataloadSchedulerJob audit = new PrdDataloadSchedulerJob();
                audit.setJobStartTime(jobStartTime);
                audit.setPublishingStatus("INPROGRESS");


                prdDataLoadSchedulerAudit.auditSchedulerJobStatus(audit);


                log.info("ProfessionalApiJobScheduler.loadPrdData Job execution in progress");
                loadPrdData(true);
                log.info("ProfessionalApiJobScheduler.loadPrdData Job execution completed successful");
//            }
        }
    }

    private void loadPrdData(Boolean doAudit) throws Exception {
        log.info("Started to load the data");
        doAudit = (isEmpty(doAudit)) ? Boolean.FALSE : doAudit;
        camelContext.getGlobalOptions().put(SCHEDULER_START_TIME, String.valueOf(new Date().getTime()));
        camelContext.getGlobalOptions().put(IS_READY_TO_AUDIT, doAudit.toString());
        dataLoadRoute.startRoute(startRoute, routesToExecute);
        var status = commonDataExecutor.execute(camelContext, "CommonData Route", startRoute);
        log.info("{}:: Route Task completes with status::{}", logComponentName, status);
        //TODO camel context need to be closed properly
        camelContext.stop();
    }


}
