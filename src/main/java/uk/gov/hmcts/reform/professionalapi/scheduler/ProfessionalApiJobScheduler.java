package uk.gov.hmcts.reform.professionalapi.scheduler;


import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.data.ingestion.DataIngestionLibraryRunner;
import uk.gov.hmcts.reform.professionalapi.domain.PrdDataloadSchedulerJob;
import uk.gov.hmcts.reform.professionalapi.repository.PrdDataloadSchedulerJobRepository;
import uk.gov.hmcts.reform.professionalapi.util.PrdDataLoadSchedulerAudit;

import java.time.LocalDateTime;

import static java.time.LocalDateTime.now;

@Component
@Slf4j
@NoArgsConstructor
@SuppressWarnings("all")
public class ProfessionalApiJobScheduler {

    @Value("${prd.scheduler.enabled:true}")
    private boolean isSchedulerEnabled;

    @Autowired
    PrdDataloadSchedulerJobRepository prdDataloadSchedulerJob;

    @Autowired
    PrdDataLoadSchedulerAudit prdDataLoadSchedulerAudit;

    @Autowired
    DataIngestionLibraryRunner dataIngestionLibraryRunner;

    @Value("${batchjob-name}")
    String jobName;

    @Autowired
    Job job;

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
                loadPrdData();
                log.info("ProfessionalApiJobScheduler.loadPrdData Job execution completed successful");
//            }
        }
    }

    private void loadPrdData() throws Exception {

        log.info("Started to load the data");

        JobParameters params = new JobParametersBuilder()
                .addString(jobName, String.valueOf(System.currentTimeMillis()))
                .toJobParameters();
        dataIngestionLibraryRunner.run(job, params);
    }


}
