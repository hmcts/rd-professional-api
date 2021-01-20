package uk.gov.hmcts.reform.professionalapi.repository;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@TestConfiguration
@EnableJpaRepositories
@EnableJpaAuditing
public class TestConfig {
}
