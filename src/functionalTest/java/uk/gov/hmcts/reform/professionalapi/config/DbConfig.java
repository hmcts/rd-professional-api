package uk.gov.hmcts.reform.professionalapi.config;

import static java.lang.System.getenv;

import com.zaxxer.hikari.HikariDataSource;
import javax.annotation.PreDestroy;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.test.context.TestPropertySource;
import java.sql.Connection;

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

    private HikariDataSource dataSource;

    @Bean
    public DataSource dataSource() {

        log.info("Func DB host: {}", postgresHost);
        log.info("Func DB port: {}", postgresPort);
        log.info("Func DB name: {}", postgresDbName);
        log.info("Func DB user name: {}", postgresUserName);
        String url = String.format("jdbc:postgresql://%s:%s/%s", postgresHost, postgresPort, postgresDbName);
        log.info("Func DB url: {}", url);

        try {
            DataSourceBuilder dataSourceBuilder = DataSourceBuilder.create();
            dataSourceBuilder.driverClassName("org.postgresql.Driver");
            dataSourceBuilder.url(url);
            dataSourceBuilder.username(postgresUserName);
            dataSourceBuilder.password(postgresPassword);
            dataSource = (HikariDataSource) dataSourceBuilder.build();
        } catch (Exception e) {
            log.error("Unable to connect to database from functional test case : {}", e.getMessage());
        }
        return dataSource;
    }

    @PreDestroy
    public void createDbConnection() {
        Connection connection;
        String isNightlyBuild = getenv("isNightlyBuild");
        String testUrl = getenv("TEST_URL");
        log.info("isNightlyBuild: {}", isNightlyBuild);
        log.info("testUrl: {}", testUrl);
        if (Boolean.TRUE.toString().equalsIgnoreCase(isNightlyBuild) && testUrl.contains("aat")) {
            log.info("Delete test data script execution started");
            try {
                connection = dataSource.getConnection();
                connection.setAutoCommit(false);
                ScriptUtils.executeSqlScript(connection,
                        new EncodedResource(new ClassPathResource("delete-functional-test-data.sql")),
                        false, true,
                        ScriptUtils.DEFAULT_COMMENT_PREFIX,
                        ScriptUtils.DEFAULT_STATEMENT_SEPARATOR,
                        ScriptUtils.DEFAULT_BLOCK_COMMENT_START_DELIMITER,
                        ScriptUtils.DEFAULT_BLOCK_COMMENT_END_DELIMITER);
                log.info("Delete test data script execution completed");
                if (!connection.isClosed()) {
                    connection.close();
                }
            } catch (Exception exe) {
                log.error("Delete test data script execution failed: {}", exe.getMessage());
            }
        } else {
            log.info("Not executing delete test data script");
        }
    }
}
