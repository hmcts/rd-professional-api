/*
package uk.gov.hmcts.reform.professionalapi.dataload.support;

import javax.sql.DataSource;
import java.util.Date;
import java.util.List;

import static uk.gov.hmcts.reform.professionalapi.dataload.util.MappingConstants.IS_READY_TO_AUDIT;
import static uk.gov.hmcts.reform.professionalapi.dataload.util.MappingConstants.SCHEDULER_START_TIME;

import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.test.context.TestContextManager;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.professionalapi.dataload.processor.ExceptionProcessor;
import uk.gov.hmcts.reform.professionalapi.dataload.route.DataLoadRoute;
import uk.gov.hmcts.reform.professionalapi.dataload.service.AuditServiceImpl;
import uk.gov.hmcts.reform.professionalapi.dataload.util.DataLoadUtil;


@ExtendWith(SpringExtension.class)
public abstract class PrdDataloadIntegrationSupport {

    public static final String FILE_STATUS = "status";

    protected boolean notDeletionFlag = false;

    @Autowired
    protected CamelContext camelContext;

    @Autowired
    @Qualifier("springJdbcTemplate")
    protected JdbcTemplate jdbcTemplate;

    @Autowired
    protected DataLoadRoute parentRoute;

    @Value("${start-route}")
    protected String startRoute;

    @Autowired
    protected ProducerTemplate producerTemplate;




    @Value("${select-dataload-schedular-audit}")
    protected String selectDataLoadSchedulerAudit;

    @Value("${scheduler-insert-sql}")
    protected String schedulerInsertJrdSql;

    @Value("${select-dataload-scheduler-audit-failure}")
    protected String schedulerInsertJrdSqlFailure;

    @Value("${select-dataload-scheduler-audit-partial-success}")
    protected String schedulerInsertJrdSqlPartialSuccess;

    @Value("${select-dataload-scheduler-audit-success}")
    protected String schedulerInsertJrdSqlSuccess;

    @Value("${audit-enable}")
    protected Boolean auditEnable;

    @Autowired
    protected DataLoadUtil dataLoadUtil;

    @Autowired
    protected ExceptionProcessor exceptionProcessor;


    @Autowired
    protected AuditServiceImpl judicialAuditServiceImpl;




    @Value("${exception-select-query}")
    protected String exceptionQuery;

    @Value("${truncate-audit}")
    protected String truncateAudit;


    @Value("${archival-file-names}")
    protected List<String> archivalFileNames;

    protected TestContextManager testContextManager;

    @Autowired
    DataSource dataSource;

    @BeforeEach
    public void setUpStringContext() throws Exception {

        */
/*executeScripts("testData/truncate-all.sql");*//*

        camelContext.getGlobalOptions().put(IS_READY_TO_AUDIT, Boolean.TRUE.toString());
        dataLoadUtil.setGlobalConstant(camelContext, "PRD Route");

        camelContext.getGlobalOptions()
            .put(SCHEDULER_START_TIME, String.valueOf(new Date(System.currentTimeMillis()).getTime()));
    }


    public void executeScripts(String path) {
        var a = new FileSystemResource(getClass().getClassLoader().getResource(path)
            .getPath());
        var resourceDatabasePopulator = new ResourceDatabasePopulator();
        resourceDatabasePopulator.addScripts(a);
        resourceDatabasePopulator.execute(dataSource);
    }
}

*/
