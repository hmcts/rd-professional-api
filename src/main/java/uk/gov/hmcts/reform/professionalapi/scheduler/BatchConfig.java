package uk.gov.hmcts.reform.professionalapi.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableBatchProcessing
@Slf4j
public class BatchConfig {

    @Autowired
    StepBuilderFactory steps;

    @Autowired
    UserDetailsRouteTask userDetailsRouteTask;

    @Autowired
    JobResultListener jobResultListener;

    @Autowired
    JobBuilderFactory jobBuilderFactory;

    @Value("${professional-user-details-route-task}")
    String professionalUserDetailsTask;


    @Value("${batchjob-name}")
    String jobName;

    @Bean
    public Step stepCommonDataCategoriesRoute() {
        return steps.get(professionalUserDetailsTask)
                .tasklet(userDetailsRouteTask)
                .build();
    }


    @Bean
    public Job runRoutesJob() {
        return jobBuilderFactory.get(jobName)
                .start(stepCommonDataCategoriesRoute())
                .listener(jobResultListener)
                .build();
    }

}
