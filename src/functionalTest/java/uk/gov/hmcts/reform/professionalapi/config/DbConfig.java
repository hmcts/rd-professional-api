package uk.gov.hmcts.reform.professionalapi.config;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.TestPropertySource;

@Configuration
@TestPropertySource("classpath:application-functional.yaml")
@Slf4j
@Getter
public class DbConfig {

    @Value("${postgres.host_name}")
    public String postgresHost;

    @Value("${postgres.name}")
    public String postgresDbName;

    @Value("${postgres.user}")
    public String postgresUserName;

    @Value("${postgres.password}")
    public String postgresPassword;

    @Value("${postgres.port}")
    public String postgresPort;
}
