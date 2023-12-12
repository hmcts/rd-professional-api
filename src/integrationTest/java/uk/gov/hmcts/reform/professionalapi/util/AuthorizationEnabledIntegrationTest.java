package uk.gov.hmcts.reform.professionalapi.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.extension.ResponseTransformer;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.Response;
import net.thucydides.core.annotations.WithTag;
import net.thucydides.core.annotations.WithTags;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import uk.gov.hmcts.reform.lib.util.serenity5.SerenityTest;
import uk.gov.hmcts.reform.professionalapi.controller.advice.ErrorResponse;
import uk.gov.hmcts.reform.professionalapi.controller.constants.IdamStatus;
import uk.gov.hmcts.reform.professionalapi.controller.request.NewUserCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.domain.AccessType;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;
import uk.gov.hmcts.reform.professionalapi.domain.UserProfileUpdatedData;
import uk.gov.hmcts.reform.professionalapi.repository.ContactInformationRepository;
import uk.gov.hmcts.reform.professionalapi.repository.DxAddressRepository;
import uk.gov.hmcts.reform.professionalapi.repository.OrgAttributeRepository;
import uk.gov.hmcts.reform.professionalapi.repository.OrganisationRepository;
import uk.gov.hmcts.reform.professionalapi.repository.PaymentAccountRepository;
import uk.gov.hmcts.reform.professionalapi.repository.ProfessionalUserRepository;
import uk.gov.hmcts.reform.professionalapi.repository.UserAccountMapRepository;
import uk.gov.hmcts.reform.professionalapi.repository.UserAttributeRepository;
import uk.gov.hmcts.reform.professionalapi.service.impl.FeatureToggleServiceImpl;
import uk.gov.hmcts.reform.professionalapi.service.impl.ProfessionalUserServiceImpl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static java.lang.String.format;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.professionalapi.controller.request.NewUserCreationRequest.aNewUserCreationRequest;
import static uk.gov.hmcts.reform.professionalapi.helper.OrganisationFixtures.organisationRequestWithAllFields;
import static uk.gov.hmcts.reform.professionalapi.helper.OrganisationFixtures.organisationRequestWithAllFieldsAreUpdated;
import static uk.gov.hmcts.reform.professionalapi.helper.OrganisationFixtures.someMinimalOrganisationRequest;
import static uk.gov.hmcts.reform.professionalapi.util.JwtTokenUtil.decodeJwtToken;
import static uk.gov.hmcts.reform.professionalapi.util.JwtTokenUtil.getUserIdAndRoleFromToken;
import static uk.gov.hmcts.reform.professionalapi.util.KeyGenUtil.getDynamicJwksResponse;

@Configuration
@SerenityTest
@WithTags({@WithTag("testType:Integration")})
@TestPropertySource(properties = {"S2S_URL=http://127.0.0.1:8990", "IDAM_URL:http://127.0.0.1:5000",
        "USER_PROFILE_URL:http://127.0.0.1:8091"})
@DirtiesContext
public abstract class AuthorizationEnabledIntegrationTest extends SpringBootIntegrationTest {

    @Autowired
    protected OrganisationRepository organisationRepository;

    @Autowired
    protected ProfessionalUserRepository professionalUserRepository;

    @Autowired
    protected PaymentAccountRepository paymentAccountRepository;

    @Autowired
    protected ContactInformationRepository contactInformationRepository;

    @Autowired
    protected DxAddressRepository dxAddressRepository;

    @Autowired
    protected UserAccountMapRepository userAccountMapRepository;

    @Autowired
    protected OrgAttributeRepository orgAttributeRepository;

    @Autowired
    protected UserAttributeRepository userAttributeRepository;

    protected ProfessionalReferenceDataClient professionalReferenceDataClient;

    @Autowired
    public ProfessionalUserServiceImpl professionalUserServiceImpl;

    @RegisterExtension
    public static WireMockExtension s2sService = new WireMockExtension(8990);

    @RegisterExtension
    public static WireMockExtension userProfileService = new WireMockExtension(8091, new ExternalTransformer());

    @RegisterExtension
    public static WireMockExtension sidamService = new WireMockExtension(5000, new ExternalTransformer());

    @RegisterExtension
    public static WireMockExtension mockHttpServerForOidc = new WireMockExtension(7000);

    @Value("${prd.security.roles.hmcts-admin}")
    protected String hmctsAdmin;

    @Value("${prd.security.roles.pui-user-manager}")
    protected String puiUserManager;

    @Value("${prd.security.roles.pui-organisation-manager}")
    protected String puiOrgManager;

    @Value("${prd.security.roles.pui-finance-manager}")
    protected String puiFinanceManager;

    @Value("${prd.security.roles.pui-case-manager}")
    protected String puiCaseManager;

    @Value("${prd.security.roles.pui-caa}")
    protected String puiCaa;

    @Value("${prd.security.roles.caseworker-caa}")
    protected String caseworkerCaa;

    @Value("${prd.security.roles.prd-aac-system}")
    protected String systemUser;

    @Value("${resendInterval}")
    protected String resendInterval;

    @Value("${syncInterval}")
    protected String syncInterval;

    @Value("${oidc.issuer}")
    private String issuer;

    @Value("${oidc.expiration}")
    private long expiration;

    protected static final String ACTIVE = "ACTIVE";
    protected static final String STATUS_MUST_BE_ACTIVE_ERROR_MESSAGE =
            "User status must be Active to perform this operation";
    protected static final String ACCESS_IS_DENIED_ERROR_MESSAGE = "Access is denied";
    protected static final String USER_IDENTIFIER = "userIdentifier";
    protected static final String ORG_IDENTIFIER = "organisationIdentifier";
    protected static final String LAST_UPDATED = "lastUpdated";
    protected static final String ACCESS_TYPES = "accessTypes";
    public static final String APPLICATION_JSON = "application/json";

    @MockBean
    protected FeatureToggleServiceImpl featureToggleService;

    @MockBean
    public static JwtDecoder jwtDecoder;

    @BeforeEach
    public void setUpClient() {
        professionalReferenceDataClient = new ProfessionalReferenceDataClient(port, issuer, expiration);
        when(featureToggleService.isFlagEnabled(anyString(), anyString())).thenReturn(true);
    }

    @BeforeEach
    public void setupIdamStubs() throws Exception {

        LinkedHashMap<String,Object> data = new LinkedHashMap<>();
        data.put("id","%s");
        data.put("uid","%s");
        data.put("forename","Super");
        data.put("surname","User");
        data.put("email","dummy@email.com");
        data.put("roles",List.of("%s"));

        s2sService.stubFor(get(urlEqualTo("/details"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", APPLICATION_JSON)
                        .withBody("rd_professional_api")));

        s2sService.stubFor(post(urlEqualTo("/lease"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", APPLICATION_JSON)
                        .withBody("eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJyZF9wcm9mZXNzaW9uYWxfYXBpIiwiZXhwIjoxNTY0NzU2MzY4fQ"
                                + ".UnRfwq_yGo6tVWEoBldCkD1zFoiMSqqm1rTHqq4f_PuTEHIJj2IHeARw3wOnJG2c3MpjM71ZTFa0RNE4D2"
                                + "AUgA")));

        sidamService.stubFor(get(urlPathMatching("/o/userinfo"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", APPLICATION_JSON)
                        .withBody(WireMockUtil.getObjectMapper().writeValueAsString(data))
                        .withTransformers("external_user-token-response")));

        mockHttpServerForOidc.stubFor(get(urlPathMatching("/jwks"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", APPLICATION_JSON)
                        .withBody(getDynamicJwksResponse())));
    }

    @BeforeEach
    public void userProfileGetUserWireMock() throws Exception {

        HashMap<Object,Object> data = new HashMap<>();
        data.put("userIdentifier", UUID.randomUUID().toString());
        data.put("firstName","testFn");
        data.put("lastName","testLn");
        data.put("email","dummy@email.com");
        data.put("idamStatus",IdamStatus.ACTIVE);

        userProfileService.stubFor(get(urlPathMatching("/v1/userprofile.*"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", APPLICATION_JSON)
                        .withStatus(200)
                        .withBody(WireMockUtil.getObjectMapper().writeValueAsString(data))));
    }

    public void userProfileGetPendingUserWireMock() {

        HashMap<Object,Object> data = new HashMap<>();
        data.put("userIdentifier", UUID.randomUUID().toString());
        data.put("firstName","testFn");
        data.put("lastName","dummy");
        data.put("email","dummy@email.com");
        data.put("idamStatus",IdamStatus.PENDING);
        try {
            userProfileService.stubFor(get(urlPathMatching("/v1/userprofile.*"))
                .willReturn(aResponse()
                    .withHeader("Content-Type", APPLICATION_JSON)
                    .withStatus(200)
                    .withBody(WireMockUtil.getObjectMapper().writeValueAsString(data))));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    public void userProfilePostPendingUserWireMock(boolean resend) {

        HashMap<Object,Object> data = new HashMap<>();
        data.put("idamId", UUID.randomUUID().toString());
        data.put("idamRegistrationResponse",201);
        try {
            userProfileService.stubFor(post(urlPathMatching("/v1/userprofile"))
                .withRequestBody(equalToJson("{ \"resendInvite\": " + resend + "}", true,
                    true))
                .willReturn(aResponse()
                    .withHeader("Content-Type", "application/json")
                    .withStatus(201)
                    .withBody(WireMockUtil.getObjectMapper().writeValueAsString(data))));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    @AfterEach
    public void cleanupTestData() {
        dxAddressRepository.deleteAll();
        contactInformationRepository.deleteAll();
        userAttributeRepository.deleteAll();
        userAccountMapRepository.deleteAll();
        professionalUserRepository.deleteAll();
        paymentAccountRepository.deleteAll();
        orgAttributeRepository.deleteAll();
        organisationRepository.deleteAll();
        JwtDecoderMockBuilder.resetJwtDecoder();
    }

    protected String settingUpOrganisation(String role) {
        userProfileCreateUserWireMock(HttpStatus.CREATED);
        String organisationIdentifier = createOrganisationRequest();
        return updateOrgAndInviteUser(organisationIdentifier, role);
    }

    protected Pair<String, String> settingUpMinimalFieldOrganisation(String role) {
        userProfileCreateUserWireMock(HttpStatus.CREATED);
        OrganisationCreationRequest organisationCreationRequest = someMinimalOrganisationRequest().build();
        String userEmail = organisationCreationRequest.getSuperUser().getEmail();
        String userId = updateOrgAndInviteUser(
                createAndActivateOrganisationWithGivenRequest(organisationCreationRequest), role);
        return Pair.of(userEmail, userId);
    }

    protected String updateOrgAndInviteUser(String organisationIdentifier, String role) {
        updateOrganisation(organisationIdentifier, hmctsAdmin, "ACTIVE");

        List<String> userRoles = new ArrayList<>();
        userRoles.add(role);

        String userId = retrieveSuperUserIdFromOrganisationId(organisationIdentifier);

        userProfileCreateUserWireMock(HttpStatus.CREATED);

        Map<String, Object> newUserResponse =
                professionalReferenceDataClient.addUserToOrganisationWithUserId(organisationIdentifier,
                        inviteUserCreationRequest(randomAlphabetic(5) + "@email.com", userRoles),
                        hmctsAdmin, userId);
        return (String) newUserResponse.get(USER_IDENTIFIER);
    }


    public String createOrganisationRequest() {
        OrganisationCreationRequest organisationCreationRequest = organisationRequestWithAllFields().build();
        java.util.Map<String, Object> responseForOrganisationCreation = professionalReferenceDataClient
                .createOrganisation(organisationCreationRequest);
        return (String) responseForOrganisationCreation.get(ORG_IDENTIFIER);
    }

    public String createOrganisationWithMinimumFieldRequest() {
        OrganisationCreationRequest organisationCreationRequest = someMinimalOrganisationRequest().build();
        java.util.Map<String, Object> responseForOrganisationCreation = professionalReferenceDataClient
                .createOrganisation(organisationCreationRequest);
        return (String) responseForOrganisationCreation.get(ORG_IDENTIFIER);
    }

    public String createOrganisationRequestWithRequest(OrganisationCreationRequest organisationCreationRequest) {
        java.util.Map<String, Object> responseForOrganisationCreation = professionalReferenceDataClient
                .createOrganisation(organisationCreationRequest);
        return (String) responseForOrganisationCreation.get(ORG_IDENTIFIER);
    }

    public String createOrganisationWithGivenRequest(OrganisationCreationRequest organisationCreationRequest) {
        java.util.Map<String, Object> responseForOrganisationCreation = professionalReferenceDataClient
                .createOrganisation(organisationCreationRequest);
        return (String) responseForOrganisationCreation.get(ORG_IDENTIFIER);
    }

    public void updateOrganisation(String organisationIdentifier, String role, String status) {
        userProfileCreateUserWireMock(HttpStatus.CREATED);
        OrganisationCreationRequest organisationUpdateRequest = organisationRequestWithAllFieldsAreUpdated()
                .status(status).build();
        professionalReferenceDataClient.updateOrganisation(organisationUpdateRequest, role, organisationIdentifier);
    }

    public void updateOrganisation(String organisationIdentifier, String role, String status,
                                   OrganisationCreationRequest organisationUpdateRequest) {
        userProfileCreateUserWireMock(HttpStatus.CREATED);
        organisationUpdateRequest.setStatus(status);
        professionalReferenceDataClient.updateOrganisation(organisationUpdateRequest, role, organisationIdentifier);
    }

    public String createAndActivateOrganisation() {
        String orgIdentifier = createOrganisationRequest();
        updateOrganisation(orgIdentifier, hmctsAdmin, ACTIVE);
        return orgIdentifier;
    }

    public String createAndActivateOrganisationWithGivenRequest(OrganisationCreationRequest organisationCreationRst) {
        String orgIdentifier = createOrganisationWithGivenRequest(organisationCreationRst);
        updateOrganisationWithGivenRequest(organisationCreationRst, orgIdentifier, hmctsAdmin, ACTIVE);
        return orgIdentifier;
    }

    public NewUserCreationRequest inviteUserCreationRequest(String userEmail, List<String> userRoles) {

        String lastName = "someLastName";
        String firstName = "1Aaron";
        NewUserCreationRequest userCreationRequest = aNewUserCreationRequest()
                .firstName(firstName)
                .lastName(lastName)
                .email(userEmail)
                .roles(userRoles)
                .build();

        return userCreationRequest;

    }

    public NewUserCreationRequest reInviteUserCreationRequest(String userEmail, List<String> userRoles) {

        NewUserCreationRequest userCreationRequest = aNewUserCreationRequest()
                .firstName("firstName")
                .lastName("lastName")
                .email(userEmail)
                .roles(userRoles)
                .resendInvite(true)
                .build();

        return userCreationRequest;

    }

    public String retrieveSuperUserIdFromOrganisationId(String orgId) {
        Organisation organisation = organisationRepository.findByOrganisationIdentifier(orgId);
        List<ProfessionalUser> users = professionalUserRepository.findByOrganisation(organisation);
        return users.get(0).getId().toString();
    }

    public String retrieveSuperUserIdentifierFromOrganisationId(String orgId) {
        Organisation organisation = organisationRepository.findByOrganisationIdentifier(orgId);
        List<ProfessionalUser> users = professionalUserRepository.findByOrganisation(organisation);
        return users.get(0).getUserIdentifier();
    }

    public String retrieveOrganisationIdFromSuperUserId(String userId) {
        return professionalUserRepository.findByUserIdentifier(userId).getOrganisation().getOrganisationIdentifier();
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

    public void userProfileCreateUserWireMockWithExtraRoles() {

        String body = null;
        body = "{"
                + "  \"idamId\":\"" + UUID.randomUUID().toString() + "\","
                + "  \"idamRegistrationResponse\":\"201\""
                + "}";

        userProfileService.stubFor(post(urlEqualTo("/v1/userprofile"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", APPLICATION_JSON)
                        .withBody(body)
                        .withStatus(201)
                ));

        String usersBody = "{"
                + "  \"userProfiles\": ["
                + "  {"
                + "  \"userIdentifier\":\"%s" + "\","
                + "  \"firstName\": \"testFn\","
                + "  \"lastName\": \"O\","
                + "  \"email\": \"dummy@email.com\","
                + "  \"idamStatus\": \"" + IdamStatus.ACTIVE + "\","
                + "  \"roles\": ["
                + "  \"pui-organisation-manager\","
                + "  \"caseworker\""
                + "  ],"
                + "  \"idamStatusCode\": \"0\","
                + "  \"idamMessage\": \"\""
                + "  }"
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
    }

    public void updateUserProfileRolesMock(HttpStatus status)  {
        String body = null;
        int returnHttpStatus = 200;
        ErrorResponse errorResponse;
        try {
            if (status.is2xxSuccessful()) {
                body = "{"
                    + "  \"statusUpdateResponse\": {"
                    + "  \"idamStatusCode\": \"200\","
                    + "  \"idamMessage\": \"Success\""
                    + "  } "
                    + "}";
                returnHttpStatus = 200;
            } else if (status.value() == 400) {
                errorResponse = ErrorResponse
                    .builder()
                    .errorMessage("400")
                    .errorDescription("BAD REQUEST")
                    .timeStamp("23:10")
                    .build();
                body = WireMockUtil.getObjectMapper().writeValueAsString(errorResponse);
                returnHttpStatus = 400;
            } else if (status.value() == 404) {
                errorResponse = ErrorResponse
                    .builder()
                    .errorMessage("404")
                    .errorDescription("No User found with the given ID")
                    .timeStamp("23:10")
                    .build();
                body = WireMockUtil.getObjectMapper().writeValueAsString(errorResponse);
                returnHttpStatus = 404;
            } else if (status.value() == 412) {
                errorResponse = ErrorResponse
                    .builder()
                    .errorMessage("412")
                    .errorDescription("One or more of the Roles provided is already assigned to the User")
                    .timeStamp("23:10")
                    .build();
                body = WireMockUtil.getObjectMapper().writeValueAsString(errorResponse);
                returnHttpStatus = 412;
            } else if (status.is5xxServerError()) {

                body = "{"
                    + "  \"roleAdditionResponse\": {"
                    + "  \"idamStatusCode\": \"500\","
                    + "  \"idamMessage\": \"Internal Server Error\""
                    + "  } ,"
                    + "  \"roleDeletionResponse\": ["
                    + "{"
                    + "  \"idamStatusCode\": \"500\","
                    + "  \"idamMessage\": \"Internal Server Error\""
                    + "  } "
                    + "  ]"
                    + "}";
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        userProfileService.stubFor(
                put(urlPathMatching("/v1/userprofile/.*"))
                        .willReturn(aResponse()
                                .withHeader("Content-Type", APPLICATION_JSON)
                                .withBody(body)
                                .withStatus(returnHttpStatus)
                        )
        );


    }

    public void reinviteUserMock(HttpStatus status) {
        String body = null;
        ErrorResponse errorResponse;
        try {
            if (status.is2xxSuccessful()) {
                HashMap<String, String> data = new HashMap<>();
                data.put("idamId", UUID.randomUUID().toString());
                data.put("idamRegistrationResponse", "201");
                body = WireMockUtil.getObjectMapper().writeValueAsString(data);
            } else if (status == HttpStatus.BAD_REQUEST) {
                errorResponse = ErrorResponse.builder()
                    .errorMessage("3 : There is a problem with your request. Please check and try again")
                    .errorDescription("User is not in PENDING state")
                    .timeStamp("23:10")
                    .build();
                body = WireMockUtil.getObjectMapper().writeValueAsString(errorResponse);
            } else if (status == HttpStatus.NOT_FOUND) {
                errorResponse = ErrorResponse.builder()
                    .errorMessage("4 : Resource not found")
                    .errorDescription("could not find user profile")
                    .timeStamp("23:10")
                    .build();
                body = WireMockUtil.getObjectMapper().writeValueAsString(errorResponse);
            } else if (status == HttpStatus.TOO_MANY_REQUESTS) {
                errorResponse = ErrorResponse.builder()
                    .errorMessage("10 : The request was last made less than 1 hour ago. Please try after some time")
                    .errorDescription(String.format("The request was last made less than %s minutes ago."
                        + " Please try after some time", resendInterval))
                    .timeStamp("23:10")
                    .build();
                body = WireMockUtil.getObjectMapper().writeValueAsString(errorResponse);
            } else if (status == HttpStatus.CONFLICT) {
                errorResponse = ErrorResponse.builder()
                    .errorMessage("7 : Resend invite failed as user is already active. Wait for one hour "
                        + "for the system to refresh.")
                    .errorDescription(String.format("Resend invite failed as user is already active. "
                        + "Wait for %s minutes for the system to refresh.", syncInterval))
                    .timeStamp("23:10")
                    .build();
                body = WireMockUtil.getObjectMapper().writeValueAsString(errorResponse);
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        userProfileService.stubFor(post(urlEqualTo("/v1/userprofile"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", APPLICATION_JSON)
                        .withBody(body)
                        .withStatus(status.value())
                ));
    }

    public void updateUserProfileMock(HttpStatus status) {
        String body = null;
        int returnHttpStatus = status.value();
        if (status.is2xxSuccessful()) {
            body = "{"
                    + "  \"statusUpdateResponse\": {"
                    + "  \"idamStatusCode\": \"200\","
                    + "  \"idamMessage\": \"Success\""
                    + "  } "
                    + "}";
            returnHttpStatus = 200;
        } else if (status.is4xxClientError()) {
            try {
                ErrorResponse errorResponse = ErrorResponse
                    .builder()
                    .errorMessage("400")
                    .errorDescription("BAD REQUEST")
                    .timeStamp("23:10")
                    .build();
                returnHttpStatus = 400;
                body = WireMockUtil.getObjectMapper().writeValueAsString(errorResponse);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }

        userProfileService.stubFor(
                put(urlPathMatching("/v1/userprofile/.*"))
                        .willReturn(aResponse()
                                .withHeader("Content-Type", APPLICATION_JSON)
                                .withBody(body)
                                .withStatus(returnHttpStatus)
                        )
        );


    }

    public void deleteUserProfileMock(HttpStatus status) {
        String body = null;
        int returnHttpStatus = status.value();
        HashMap<String,String> data = new HashMap<>();
        try {
            if (status.is2xxSuccessful()) {
                data.put("statusCode", "204");
                data.put("message", "User Profile Deleted Successfully");
                body = WireMockUtil.getObjectMapper().writeValueAsString(data);
                returnHttpStatus = 204;
            } else if (status == HttpStatus.BAD_REQUEST) {
                data.put("statusCode", "400");
                data.put("message", "User Profile Delete Request has some problem");
                body = WireMockUtil.getObjectMapper().writeValueAsString(data);
            } else if (status == HttpStatus.NOT_FOUND) {
                data.put("statusCode", "404");
                data.put("message", "User Profile Not Found To Delete");
                body = WireMockUtil.getObjectMapper().writeValueAsString(data);
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        userProfileService.stubFor(
                delete(urlEqualTo("/v1/userprofile"))
                        .willReturn(aResponse()
                                .withHeader("Content-Type", APPLICATION_JSON)
                                .withBody(body)
                                .withStatus(returnHttpStatus)
                        )
        );

    }

    public void getUserProfileByEmailWireMock(HttpStatus status) {
        String body = null;
        int returnHttpStaus = status.value();
        if (status.is2xxSuccessful()) {
            body = "{"
                    + "  \"idamStatus\": \"PENDING\""
                    + "}";
            returnHttpStaus = 200;
        } else {
            body = "{"
                    + "  \"idamStatus\": \" \""
                    + "}";
            returnHttpStaus = 500;
        }

        userProfileService.stubFor(get(urlPathMatching("/v1/userprofile"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", APPLICATION_JSON)
                        .withBody(body)
                        .withStatus(returnHttpStaus)
                ));

    }

    public static class MultipleUsersResponseTransformer extends ResponseTransformer {
        @Override
        public Response transform(Request request, Response response, FileSource files, Parameters parameters) {
            String requestBodyAsString = request.getBodyAsString();
            ObjectMapper mapper = new ObjectMapper();
            Optional<HashMap<String, List<String>>> ids = Optional.empty();
            try {
                ids = Optional.ofNullable(mapper.readValue(request.getBodyAsString(), HashMap.class));
            } catch (IOException e) {
                //Do Nothing
            }
            String formatResponse = response.getBodyAsString();
            int replaceParams = formatResponse.split("%s").length - 1;
            List<String> userIds = new ArrayList<>();

            if (ids.isPresent()) {
                userIds = ids.get().get("userIds");
            }

            if (replaceParams > 0) {
                if (replaceParams > userIds.size()) {
                    while (replaceParams != userIds.size()) {
                        userIds.add(UUID.randomUUID().toString());
                    }
                }

                if (replaceParams < userIds.size()) {
                    while (replaceParams != userIds.size()) {
                        userIds.remove(userIds.size() - 1);
                    }
                }
            }

            formatResponse = String.format(formatResponse, userIds.toArray());

            return Response.Builder.like(response)
                    .but().body(formatResponse)
                    .build();
        }

        @Override
        public String getName() {
            return "transformer-multi-user-response";
        }

        public boolean applyGlobally() {
            return false;
        }
    }


    public static class ExternalTransformer extends ResponseTransformer {
        @Override
        public Response transform(Request request, Response response, FileSource files, Parameters parameters) {

            String formatResponse = response.getBodyAsString();

            String token = request.getHeader("Authorization");
            String tokenBody = decodeJwtToken(token.split(" ")[1]);
            LinkedList tokenInfo = getUserIdAndRoleFromToken(tokenBody);
            formatResponse = format(formatResponse, tokenInfo.get(1), tokenInfo.get(1), tokenInfo.get(0));

            return Response.Builder.like(response)
                    .but().body(formatResponse)
                    .build();
        }

        @Override
        public String getName() {
            return "external_user-token-response";
        }

        public boolean applyGlobally() {
            return false;
        }
    }

    public void updateOrganisationWithGivenRequest(OrganisationCreationRequest organisationUpdateRequest,
                                                   String organisationIdentifier, String role, String status) {
        userProfileCreateUserWireMock(HttpStatus.CREATED);
        organisationUpdateRequest.setStatus(status);
        professionalReferenceDataClient.updateOrganisation(organisationUpdateRequest, role, organisationIdentifier);
    }

    public UserProfileUpdatedData createModifyUserConfiguredAccessData(String email, int numAccessTypes) {

        UserProfileUpdatedData userProfileUpdatedData = new UserProfileUpdatedData();
        Set<AccessType> accessTypes = new HashSet<>();
        for (int i = 0; i < numAccessTypes; i++) {
            AccessType accessType = new AccessType("Jurisdiction" + i,
                    "Organisation" + i,
                    "AccessType" + i, true);
            accessTypes.add(accessType);
        }

        userProfileUpdatedData.setEmail(email);
        userProfileUpdatedData.setAccessTypes(accessTypes);
        userProfileUpdatedData.setIdamStatus(IdamStatus.ACTIVE.name());
        userProfileUpdatedData.setRolesAdd(new HashSet<>());
        userProfileUpdatedData.setRolesDelete(new HashSet<>());
        return userProfileUpdatedData;
    }
}
