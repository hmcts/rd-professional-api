package uk.gov.hmcts.reform.professionalapi.dataload.camel;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.launchdarkly.sdk.server.LDClient;
import net.thucydides.core.annotations.WithTag;
import net.thucydides.core.annotations.WithTags;
import org.apache.camel.ProducerTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import uk.gov.hmcts.reform.lib.util.serenity5.SerenityTest;
import uk.gov.hmcts.reform.professionalapi.configuration.LaunchDarklyConfiguration;
import uk.gov.hmcts.reform.professionalapi.configuration.SecurityConfiguration;
import uk.gov.hmcts.reform.professionalapi.controller.constants.IdamStatus;
import uk.gov.hmcts.reform.professionalapi.controller.feign.UserProfileFeignClient;
import uk.gov.hmcts.reform.professionalapi.dataload.processor.ExceptionProcessor;
import uk.gov.hmcts.reform.professionalapi.dataload.scheduler.ProfessionalApiJobScheduler;
import uk.gov.hmcts.reform.professionalapi.dataload.service.AuditServiceImpl;
import uk.gov.hmcts.reform.professionalapi.dataload.support.IntegrationTestSupport;
import uk.gov.hmcts.reform.professionalapi.dataload.util.PrdDataExecutor;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;
import uk.gov.hmcts.reform.professionalapi.repository.OrganisationRepository;
import uk.gov.hmcts.reform.professionalapi.repository.ProfessionalUserRepository;
import uk.gov.hmcts.reform.professionalapi.service.impl.FeatureToggleServiceImpl;
import uk.gov.hmcts.reform.professionalapi.service.impl.OrganisationServiceImpl;
import uk.gov.hmcts.reform.professionalapi.service.impl.PaymentAccountServiceImpl;
import uk.gov.hmcts.reform.professionalapi.service.impl.ProfessionalUserServiceImpl;
import uk.gov.hmcts.reform.professionalapi.util.AuthorizationEnabledIntegrationTest;
import uk.gov.hmcts.reform.professionalapi.util.ProfessionalReferenceDataClient;
import uk.gov.hmcts.reform.professionalapi.util.SpringBootIntegrationTest;
import uk.gov.hmcts.reform.professionalapi.util.WireMockExtension;

@SerenityTest
@WithTags({@WithTag("testType:Integration")})
@TestPropertySource(properties = {"S2S_URL=http://127.0.0.1:8990", "IDAM_URL:http://127.0.0.1:5000",
        "USER_PROFILE_URL:http://127.0.0.1:8091"})
@DirtiesContext
public abstract class AuthorizationDataloadEnabledIntegrationTest extends SpringBootIntegrationTest implements
    IntegrationTestSupport {

    @Autowired
    ProfessionalApiJobScheduler professionalApiJobScheduler;

    @Autowired
    protected OrganisationRepository organisationRepository;

    @Autowired
    protected ProfessionalUserRepository professionalUserRepository;

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

    @SpyBean
    protected JwtDecoder jwtDecoder;

    @RegisterExtension
    public static WireMockExtension
        userProfileService = new WireMockExtension(8091, new AuthorizationEnabledIntegrationTest.ExternalTransformer());

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

    protected ProfessionalReferenceBulkCustomerClient professionalReferenceBulkCustomerClient;

    @Value("${oidc.issuer}")
    private String issuer;

    @Value("${oidc.expiration}")
    private long expiration;

    public static final String APPLICATION_JSON = "application/json";

    @BeforeEach
    public void setUpClient() {
        professionalReferenceBulkCustomerClient = new ProfessionalReferenceBulkCustomerClient(port, issuer, expiration);
        when(featureToggleService.isFlagEnabled(anyString(), anyString())).thenReturn(true);
    }

    public void userProfileCreateUserWireMock(HttpStatus status)  {
        String body = null;
        int returnHttpStaus = status.value();
        if (status.is2xxSuccessful()) {
            body = "{"
                + "  \"idamId\":\"" + UUID.randomUUID().toString() + "\","
                + "  \"idamRegistrationResponse\":\"201\""
                + "}";
            returnHttpStaus = 201;
        }

        userProfileService.stubFor(post(urlEqualTo("/v1/userprofile"))
            .willReturn(aResponse()
                .withHeader("Content-Type", APPLICATION_JSON)
                .withBody(body)
                .withStatus(returnHttpStaus)
            ));

        String usersBody = "{"
            + "  \"userProfiles\": ["
            + "  {"
            + "  \"userIdentifier\":\"%s" + "\","
            + "  \"firstName\": \"testFn\","
            + "  \"lastName\": \"R\","
            + "  \"email\": \"dummy@email.com\","
            + "  \"idamStatus\": \"" + IdamStatus.ACTIVE + "\","
            + "  \"roles\": ["
            + "  \"pui-organisation-manager\""
            + "  ],"
            + "  \"idamStatusCode\": \"0\","
            + "  \"idamMessage\": \"\""
            + "  },"
            + "  {"
            + "  \"userIdentifier\":\" %s" + "\","
            + "  \"firstName\": \"testFn\","
            + "  \"lastName\": \"L\","
            + "  \"email\": \"dummy@email.com\","
            + "  \"idamStatus\": \"" + IdamStatus.ACTIVE + "\","
            + "  \"roles\": ["
            + "  \"pui-case-manager\""
            + "  ],"
            + "  \"idamStatusCode\": \"0\","
            + "  \"idamMessage\": \"\""
            + "  },"
            + " {"
            + "  \"userIdentifier\":\"%s" + "\","
            + "  \"firstName\": \"testFn\","
            + "  \"lastName\": \"O\","
            + "  \"email\": \"dummy@email.com\","
            + "  \"idamStatus\": \"DELETED\","
            + "  \"roles\": [],"
            + "  \"idamStatusCode\": \"404\","
            + "  \"idamMessage\": \"16 Resource not found\""
            + "  } "
            + " ]"
            + "}";

        userProfileService.stubFor(
            post(urlPathMatching("/v1/userprofile/users.*"))
                .willReturn(
                    aResponse()
                        .withHeader("Content-Type", APPLICATION_JSON)
                        .withBody(usersBody)
                        .withTransformers("transformer-multi-user-response")
                        .withStatus(200)
                )
        );

        String usersBodyWithoutRoles = " {"
            + "  \"userProfiles\": ["
            + "  {"
            + "  \"userIdentifier\":\"%s" + "\","
            + "  \"firstName\": \"testFN\","
            + "  \"lastName\": \"R\","
            + "  \"email\": \"dummy@email.com\","
            + "  \"idamStatus\": \"" + IdamStatus.ACTIVE + "\","
            + "  \"roles\": [],"
            + "  \"idamStatusCode\": \"0\","
            + "  \"idamMessage\": \"\""
            + "  },"
            + "  {"
            + "  \"userIdentifier\":\"%s" + "\","
            + "  \"firstName\": \"testFn\","
            + "  \"lastName\": \"L\","
            + "  \"email\": \"dummy@email.com\","
            + "  \"idamStatus\": \"" + IdamStatus.ACTIVE + "\","
            + "  \"roles\": [],"
            + "  \"idamStatusCode\": \"0\","
            + "  \"idamMessage\": \"\""
            + "  },"
            + " {"
            + "  \"userIdentifier\":\"%s" + "\","
            + "  \"firstName\": \"testFn\","
            + "  \"lastName\": \"O\","
            + "  \"email\": \"dummy@email.com\","
            + "  \"idamStatus\": \"" + IdamStatus.PENDING + "\","
            + "  \"roles\": [],"
            + "  \"idamStatusCode\": \"0\","
            + "  \"idamMessage\": \"\""
            + "  } "
            + " ]"
            + "}";

        userProfileService.stubFor(
            post(urlPathMatching("/v1/userprofile/users.*"))
                .withQueryParam("rolesRequired", equalTo("false"))
                .willReturn(
                    aResponse()
                        .withHeader("Content-Type", APPLICATION_JSON)
                        .withBody(usersBodyWithoutRoles)
                        .withTransformers("transformer-multi-user-response")
                        .withStatus(200)
                )
        );
    }

    public synchronized void mockJwtToken(String role) {
        professionalReferenceBulkCustomerClient.clearTokens();
        String bearerToken = professionalReferenceBulkCustomerClient.getAndReturnBearerTokenBulk(null, role);
        String[] bearerTokenArray = bearerToken.split(" ");
        when(jwtDecoder.decode(anyString())).thenReturn(getJwt(role, bearerTokenArray[1]));
    }

    public String retrieveSuperUserIdFromOrganisationId(String orgId) {
        Organisation organisation = organisationRepository.findByOrganisationIdentifier(orgId);
        List<ProfessionalUser> users = professionalUserRepository.findByOrganisation(organisation);
        return users.get(0).getId().toString();
    }

    public Jwt getJwt(String role, String bearerToken) {
        return Jwt.withTokenValue(bearerToken)
            .claim("exp", Instant.ofEpochSecond(1985763216))
            .claim("iat", Instant.ofEpochSecond(1985734416))
            .claim("token_type", "Bearer")
            .claim("tokenName", "access_token")
            .claim("expires_in", 28800)
            .header("kid", "b/O6OvVv1+y+WgrH5Ui9WTioLt0=")
            .header("typ", "RS256")
            .header("alg", "RS256")
            .build();
    }

}
