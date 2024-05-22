package uk.gov.hmcts.reform.professionalapi.dataload.camel;

import com.launchdarkly.sdk.server.LDClient;
import net.thucydides.core.annotations.WithTag;
import net.thucydides.core.annotations.WithTags;
import org.apache.camel.ProducerTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import uk.gov.hmcts.reform.lib.util.serenity5.SerenityTest;
import uk.gov.hmcts.reform.professionalapi.configuration.LaunchDarklyConfiguration;
import uk.gov.hmcts.reform.professionalapi.configuration.SecurityConfiguration;
import uk.gov.hmcts.reform.professionalapi.controller.feign.UserProfileFeignClient;
import uk.gov.hmcts.reform.professionalapi.dataload.processor.ExceptionProcessor;
import uk.gov.hmcts.reform.professionalapi.dataload.scheduler.ProfessionalApiJobScheduler;
import uk.gov.hmcts.reform.professionalapi.dataload.service.AuditServiceImpl;
import uk.gov.hmcts.reform.professionalapi.dataload.support.IntegrationTestSupport;
import uk.gov.hmcts.reform.professionalapi.dataload.util.PrdDataExecutor;
import uk.gov.hmcts.reform.professionalapi.service.impl.FeatureToggleServiceImpl;
import uk.gov.hmcts.reform.professionalapi.service.impl.OrganisationServiceImpl;
import uk.gov.hmcts.reform.professionalapi.service.impl.PaymentAccountServiceImpl;
import uk.gov.hmcts.reform.professionalapi.service.impl.ProfessionalUserServiceImpl;
import uk.gov.hmcts.reform.professionalapi.util.SpringBootIntegrationTest;

@SerenityTest
@WithTags({@WithTag("testType:Integration")})
@TestPropertySource(properties = {"S2S_URL=http://127.0.0.1:8990", "IDAM_URL:http://127.0.0.1:5000",
        "USER_PROFILE_URL:http://127.0.0.1:8091"})
@DirtiesContext
@SuppressWarnings("checkstyle:Indentation")
public abstract class AuthorizationDataloadEnabledIntegrationTest extends SpringBootIntegrationTest implements
    IntegrationTestSupport {

    @Autowired
    ProfessionalApiJobScheduler professionalApiJobScheduler;

    @MockBean
    protected FeatureToggleServiceImpl featureToggleService;

    @MockBean
    protected OrganisationServiceImpl organisationServiceImpls;

    @MockBean
    protected ProfessionalUserServiceImpl professionalUserServiceimpl;

    @MockBean
    protected PaymentAccountServiceImpl paymentAccountServiceImpl;

    @MockBean
    protected UserProfileFeignClient userProfileFeignClient;

    @MockBean
    protected SecurityConfiguration securityConfiguration;


    @MockBean
    LaunchDarklyConfiguration launchDarklyConfiguration;

    @MockBean
    LDClient ldClient;

    @Value("${prdEnumRoleType}")
    protected String prdEnumRoleType;

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    @Autowired
    protected ExceptionProcessor exceptionProcessor;

    @Autowired
    protected AuditServiceImpl auditServiceimpl;

    @Autowired
    PrdDataExecutor commonDataExecutor;

    @Autowired
    protected ProducerTemplate producerTemplate;


}
