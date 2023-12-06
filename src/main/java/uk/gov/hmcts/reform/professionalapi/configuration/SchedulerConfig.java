package uk.gov.hmcts.reform.professionalapi.configuration;


import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.jdbctemplate.JdbcTemplateLockProvider;
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
@ConditionalOnProperty(value = "prd.scheduler.enabled", matchIfMissing = true, havingValue = "true")
@EnableSchedulerLock(defaultLockAtMostFor = "${prd.scheduler.defaultLockAtMostFor}")
public class SchedulerConfig {

    @Bean
    public LockProvider getLockProvider(@Autowired JdbcTemplate jdbcTemplate) {
        return new JdbcTemplateLockProvider(jdbcTemplate, "lock_details_provider");
    }
}
