package uk.gov.hmcts.reform.professionalapi.dataload.camel;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.professionalapi.dataload.config.AzureBlobConfig;
import uk.gov.hmcts.reform.professionalapi.dataload.config.BlobStorageCredentials;
import uk.gov.hmcts.reform.professionalapi.dataload.config.DataloadConfig;
import uk.gov.hmcts.reform.professionalapi.repository.OrganisationRepository;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.sql.DataSource;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.professionalapi.dataload.support.IntegrationTestSupport.setSourcePath;
import static uk.gov.hmcts.reform.professionalapi.dataload.util.MappingConstants.SUCCESS;

@ContextConfiguration(classes = {DataloadConfig.class,BlobStorageCredentials.class,AzureBlobConfig.class},
    initializers = ConfigDataApplicationContextInitializer.class)
@TestPropertySource(properties = {"spring.config.location=classpath:application.yml"})
class PrdBatchApplicationIntegrationTest extends AuthorizationDataloadEnabledIntegrationTest {


    @Autowired
    protected OrganisationRepository organisationRepository;

    @Autowired
    DataSource dataSource;

    @Value("${select-bulk_customer}")
    protected String bulkCustomerSql;

    @Value("${delete-bulk_customer}")
    protected String bulkCustomerSqlDelete;

    @Value("${select-dataload-schedular-audit}")
    String selectDataLoadSchedulerAudit;

    public static final String FILE_STATUS = "status";

    @BeforeAll
    public static void beforeAll() throws Exception {
        String[] fileWithInvalidJsr = {"classpath:sourceFiles/bulk_customer_ids.csv"};
        System.setProperty("parent.file.name", fileWithInvalidJsr[0]);
        setSourcePath(fileWithInvalidJsr[0],
            "parent.file.path");
        System.setProperty("archival.file.names",  fileWithInvalidJsr[0]);
    }


    @Test
    @RefreshScope
    void testTasklet() throws Exception {
        ReflectionTestUtils.setField(professionalApiJobScheduler, "isSchedulerEnabled", true);
        ReflectionTestUtils.setField(professionalApiJobScheduler, "isSchedulerEnabled", true);
        ReflectionTestUtils.setField(professionalApiJobScheduler, "azureKey", "azurekey");
        ReflectionTestUtils.setField(professionalApiJobScheduler, "s2sSecret", "s2sSecret");

        professionalApiJobScheduler.loadProfessioanlDataJob();

        assertEquals(8, jdbcTemplate.queryForList(bulkCustomerSql).size());
        List<Map<String, Object>> dataLoadSchedulerAudit = jdbcTemplate
            .queryForList(selectDataLoadSchedulerAudit);
        assertEquals(SUCCESS, dataLoadSchedulerAudit.get(0).get(FILE_STATUS));
    }

    static void validateExceptionDbRecordCount(JdbcTemplate jdbcTemplate,
                                               String queryName, int expectedCount,
                                               boolean isPartialSuccessValidation, String... params) {
        List<Map<String, Object>> exceptionList;
        if (isNotEmpty(params)) {
            exceptionList = jdbcTemplate.queryForList(queryName, (Object[]) params);
        } else {
            exceptionList = jdbcTemplate.queryForList(queryName);
        }

        exceptionList.forEach(exception -> {
            assertTrue(isNotEmpty(exception.get("scheduler_name")));
            assertTrue(isNotEmpty(exception.get("scheduler_start_time")));
            assertTrue(isNotEmpty(exception.get("error_description")));
            assertTrue(isNotEmpty(exception.get("updated_timestamp")));
            if (isPartialSuccessValidation) {
                assertTrue(isNotEmpty(exception.get("table_name")));
                if (isNotEmpty(params)) {
                    assertTrue(isNotEmpty(exception.get("key")));
                } else {
                    assertNotNull((exception.get("key")));
                }
                assertTrue(isNotEmpty(exception.get("field_in_error")));
            }
        });
        assertEquals(expectedCount, exceptionList.size());
    }

    @AfterEach
    public void cleanupTestData() {
        jdbcTemplate.execute(bulkCustomerSqlDelete);
        organisationRepository.deleteById(UUID.fromString("046b6c7f-0b8a-43b9-b35d-6489e6daee91"));
    }

}
