package uk.gov.hmcts.reform.professionalapi.config;

import com.zaxxer.hikari.HikariDataSource;
import javax.sql.DataSource;
import io.restassured.RestAssured;
import lombok.extern.slf4j.Slf4j;
import net.serenitybdd.rest.SerenityRest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.TestPropertySource;

@Configuration
@TestPropertySource("classpath:application-functional.yaml")
@Slf4j
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

    @Bean
    public DataSource dataSource() {

        //following logs can be deleted once testing is completed
        log.info("Func DB host: " +  postgresHost);
        log.info("Func DB name: " +  postgresDbName);
        log.info("Func DB user name: " +  postgresUserName);
        log.info("Func DB password: " +  postgresPassword);
        String url = String.format("jdbc:postgresql://%s:%s/%s", postgresHost, postgresPort, postgresDbName);
        log.info("Func DB url :: " + url);
        //

        SerenityRest.proxy("proxyout.reform.hmcts.net", 8080);
        RestAssured.proxy("proxyout.reform.hmcts.net", 8080);

        HikariDataSource dataSource = null;
        try {
            DataSourceBuilder dataSourceBuilder = DataSourceBuilder.create();
            dataSourceBuilder.driverClassName("org.postgresql.Driver");
            dataSourceBuilder.url(url);
            dataSourceBuilder.username(postgresUserName);
            dataSourceBuilder.password(postgresPassword);
            dataSource = (HikariDataSource) dataSourceBuilder.build();
        } catch (Exception e) {
            log.error("Unable to connect to database from functional test case", e.getMessage());
        }
        return dataSource;
    }

}
