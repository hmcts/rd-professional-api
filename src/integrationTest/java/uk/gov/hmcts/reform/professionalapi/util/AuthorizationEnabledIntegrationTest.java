package uk.gov.hmcts.reform.professionalapi.util;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static java.lang.String.format;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.professionalapi.controller.request.NewUserCreationRequest.aNewUserCreationRequest;
import static uk.gov.hmcts.reform.professionalapi.helper.OrganisationFixtures.createJurisdictions;
import static uk.gov.hmcts.reform.professionalapi.helper.OrganisationFixtures.organisationRequestWithAllFields;
import static uk.gov.hmcts.reform.professionalapi.helper.OrganisationFixtures.organisationRequestWithAllFieldsAreUpdated;
import static uk.gov.hmcts.reform.professionalapi.util.JwtTokenUtil.decodeJwtToken;
import static uk.gov.hmcts.reform.professionalapi.util.JwtTokenUtil.getUserIdAndRoleFromToken;
import static uk.gov.hmcts.reform.professionalapi.util.KeyGenUtil.getDynamicJwksResponse;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.extension.ResponseTransformer;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.Response;
import com.github.tomakehurst.wiremock.junit.WireMockRule;


import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import net.thucydides.core.annotations.WithTag;
import net.thucydides.core.annotations.WithTags;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import uk.gov.hmcts.reform.professionalapi.controller.constants.IdamStatus;
import uk.gov.hmcts.reform.professionalapi.controller.request.NewUserCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;
import uk.gov.hmcts.reform.professionalapi.repository.ContactInformationRepository;
import uk.gov.hmcts.reform.professionalapi.repository.DxAddressRepository;
import uk.gov.hmcts.reform.professionalapi.repository.OrganisationRepository;
import uk.gov.hmcts.reform.professionalapi.repository.PaymentAccountRepository;
import uk.gov.hmcts.reform.professionalapi.repository.ProfessionalUserRepository;
import uk.gov.hmcts.reform.professionalapi.repository.UserAccountMapRepository;
import uk.gov.hmcts.reform.professionalapi.repository.UserAttributeRepository;
import uk.gov.hmcts.reform.professionalapi.service.impl.FeatureToggleServiceImpl;
import uk.gov.hmcts.reform.professionalapi.service.impl.ProfessionalUserServiceImpl;

@Configuration
@RunWith(SpringIntegrationSerenityRunner.class)
@WithTags({@WithTag("testType:Integration")})
@TestPropertySource(properties = {"S2S_URL=http://127.0.0.1:8990", "IDAM_URL:http://127.0.0.1:5000",
        "USER_PROFILE_URL:http://127.0.0.1:8091", "CCD_URL:http://127.0.0.1:8092"})
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
    protected UserAttributeRepository userAttributeRepository;

    protected ProfessionalReferenceDataClient professionalReferenceDataClient;

    @Autowired
    public ProfessionalUserServiceImpl professionalUserServiceImpl;

    @ClassRule
    public static WireMockRule s2sService = new WireMockRule(wireMockConfig().port(8990));


    @ClassRule
    public static WireMockRule userProfileService = new WireMockRule(wireMockConfig().port(8091)
            .extensions(new MultipleUsersResponseTransformer()));

    @ClassRule
    public static WireMockRule ccdService = new WireMockRule(wireMockConfig().port(8092));

    @ClassRule
    public static WireMockRule sidamService = new WireMockRule(wireMockConfig().port(5000)
            .extensions(ExternalTransformer.class));

    @ClassRule
    public static WireMockRule mockHttpServerForOidc = new WireMockRule(wireMockConfig().port(7000));

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

    protected static final String FORBIDDEN_LD = "Forbidden with Launch Darkly";

    @MockBean
    protected FeatureToggleServiceImpl featureToggleService;

    @Before
    public void setUpClient() {
        professionalReferenceDataClient = new ProfessionalReferenceDataClient(port, issuer, expiration);
        when(featureToggleService.isFlagEnabled(anyString(), anyString())).thenReturn(true);
    }

    @Before
    public void setupIdamStubs() throws Exception {

        s2sService.stubFor(get(urlEqualTo("/details"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("rd_professional_api")));

        s2sService.stubFor(post(urlEqualTo("/lease"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJyZF9wcm9mZXNzaW9uYWxfYXBpIiwiZXhwIjoxNTY0NzU2MzY4fQ"
                                + ".UnRfwq_yGo6tVWEoBldCkD1zFoiMSqqm1rTHqq4f_PuTEHIJj2IHeARw3wOnJG2c3MpjM71ZTFa0RNE4D2"
                                + "AUgA")));

        sidamService.stubFor(get(urlPathMatching("/o/userinfo"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{"
                                +  "  \"id\": \"%s\","
                                +  "  \"uid\": \"%s\","
                                +  "  \"forename\": \"Super\","
                                +  "  \"surname\": \"User\","
                                +  "  \"email\": \"super.user@hmcts.net\","
                                +  "  \"accountStatus\": \"active\","
                                +  "  \"roles\": ["
                                +  "  \"%s\""
                                +  "  ]"
                                +  "}")
                        .withTransformers("external_user-token-response")));

        mockHttpServerForOidc.stubFor(get(urlPathMatching("/jwks"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(getDynamicJwksResponse())));
    }

    @Before
    public void userProfileGetUserWireMock() {

        ccdService.stubFor(post(urlEqualTo("/user-profile/users"))
                .willReturn(aResponse()
                        .withStatus(201)
                ));

        userProfileService.stubFor(get(urlPathMatching("/v1/userprofile.*"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200)
                        .withBody("{"
                                + "  \"userIdentifier\":\"" + UUID.randomUUID().toString() + "\","
                                + "  \"firstName\": \"prashanth\","
                                + "  \"lastName\": \"rao\","
                                + "  \"email\": \"super.user@hmcts.net\","
                                + "  \"idamStatus\": \"" + IdamStatus.ACTIVE + "\""
                                + "}")));
    }

    public void ccdUserProfileErrorWireMock(HttpStatus httpStatus) {

        ccdService.stubFor(post(urlEqualTo("/user-profile/users"))
                .willReturn(aResponse()
                        .withStatus(httpStatus.value())
                ));
    }

    @After
    public void cleanupTestData() {
        dxAddressRepository.deleteAll();
        contactInformationRepository.deleteAll();
        userAttributeRepository.deleteAll();
        userAccountMapRepository.deleteAll();
        professionalUserRepository.deleteAll();
        paymentAccountRepository.deleteAll();
        organisationRepository.deleteAll();
    }

    protected String settingUpOrganisation(String role) {
        userProfileCreateUserWireMock(HttpStatus.CREATED);
        String organisationIdentifier = createOrganisationRequest();
        updateOrganisation(organisationIdentifier, hmctsAdmin, "ACTIVE");

        List<String> userRoles = new ArrayList<>();
        userRoles.add(role);

        String userIdentifier = retrieveSuperUserIdFromOrganisationId(organisationIdentifier);

        userProfileCreateUserWireMock(HttpStatus.CREATED);

        Map<String, Object> newUserResponse =
                professionalReferenceDataClient.addUserToOrganisationWithUserId(organisationIdentifier,
                        inviteUserCreationRequest(randomAlphabetic(5) + "@email.com", userRoles),
                        hmctsAdmin, userIdentifier);



        return (String) newUserResponse.get("userIdentifier");
    }

    public String createOrganisationRequest() {
        OrganisationCreationRequest organisationCreationRequest = organisationRequestWithAllFields().build();
        java.util.Map<String, Object> responseForOrganisationCreation = professionalReferenceDataClient
                .createOrganisation(organisationCreationRequest);
        return (String) responseForOrganisationCreation.get("organisationIdentifier");
    }

    public String createOrganisationRequestWithRequest(OrganisationCreationRequest organisationCreationRequest) {
        java.util.Map<String, Object> responseForOrganisationCreation = professionalReferenceDataClient
                .createOrganisation(organisationCreationRequest);
        return (String) responseForOrganisationCreation.get("organisationIdentifier");
    }

    public String createOrganisationWithGivenRequest(OrganisationCreationRequest organisationCreationRequest) {
        java.util.Map<String, Object> responseForOrganisationCreation = professionalReferenceDataClient
                .createOrganisation(organisationCreationRequest);
        return (String) responseForOrganisationCreation.get("organisationIdentifier");
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
                .jurisdictions(createJurisdictions())
                .build();

        return userCreationRequest;

    }

    public NewUserCreationRequest reInviteUserCreationRequest(String userEmail, List<String> userRoles) {

        NewUserCreationRequest userCreationRequest = aNewUserCreationRequest()
                .firstName("firstName")
                .lastName("lastName")
                .email(userEmail)
                .roles(userRoles)
                .jurisdictions(createJurisdictions())
                .resendInvite(true)
                .build();

        return userCreationRequest;

    }

    public String retrieveSuperUserIdFromOrganisationId(String orgId) {
        Organisation organisation = organisationRepository.findByOrganisationIdentifier(orgId);
        List<ProfessionalUser> users = professionalUserRepository.findByOrganisation(organisation);
        return users.get(0).getId().toString();
    }

    public void userProfileCreateUserWireMock(HttpStatus status) {
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
                        .withHeader("Content-Type", "application/json")
                        .withBody(body)
                        .withStatus(returnHttpStaus)
                ));

        String usersBody = "{"
                + "  \"userProfiles\": ["
                + "  {"
                + "  \"userIdentifier\":\"%s" + "\","
                + "  \"firstName\": \"prashanth\","
                + "  \"lastName\": \"rao\","
                + "  \"email\": \"super.user@hmcts.net\","
                + "  \"idamStatus\": \"" + IdamStatus.ACTIVE + "\","
                + "  \"roles\": ["
                + "  \"pui-organisation-manager\""
                + "  ],"
                + "  \"idamStatusCode\": \"0\","
                + "  \"idamMessage\": \"\""
                + "  },"
                + "  {"
                + "  \"userIdentifier\":\" %s" + "\","
                + "  \"firstName\": \"Shreedhar\","
                + "  \"lastName\": \"Lomte\","
                + "  \"email\": \"super.user@hmcts.net\","
                + "  \"idamStatus\": \"" + IdamStatus.ACTIVE + "\","
                + "  \"roles\": ["
                + "  \"pui-case-manager\""
                + "  ],"
                + "  \"idamStatusCode\": \"0\","
                + "  \"idamMessage\": \"\""
                + "  },"
                + " {"
                + "  \"userIdentifier\":\"%s"  + "\","
                + "  \"firstName\": \"adil\","
                + "  \"lastName\": \"oozeerally\","
                + "  \"email\": \"adil.ooze@hmcts.net\","
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
                                        .withHeader("Content-Type", "application/json")
                                        .withBody(usersBody)
                                        .withTransformers("transformer-multi-user-response")
                                        .withStatus(200)
                        )
        );

        String usersBodyWithoutRoles = " {"
                + "  \"userProfiles\": ["
                + "  {"
                + "  \"userIdentifier\":\"%s" + "\","
                + "  \"firstName\": \"prashanth\","
                + "  \"lastName\": \"rao\","
                + "  \"email\": \"super.user@hmcts.net\","
                + "  \"idamStatus\": \"" + IdamStatus.ACTIVE + "\","
                + "  \"roles\": [],"
                + "  \"idamStatusCode\": \"0\","
                + "  \"idamMessage\": \"\""
                + "  },"
                + "  {"
                + "  \"userIdentifier\":\"%s" + "\","
                + "  \"firstName\": \"Shreedhar\","
                + "  \"lastName\": \"Lomte\","
                + "  \"email\": \"super.user@hmcts.net\","
                + "  \"idamStatus\": \"" + IdamStatus.ACTIVE + "\","
                + "  \"roles\": [],"
                + "  \"idamStatusCode\": \"0\","
                + "  \"idamMessage\": \"\""
                + "  },"
                + " {"
                + "  \"userIdentifier\":\"%s"  + "\","
                + "  \"firstName\": \"adil\","
                + "  \"lastName\": \"oozeerally\","
                + "  \"email\": \"adil.ooze@hmcts.net\","
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
                                        .withHeader("Content-Type", "application/json")
                                        .withBody(usersBodyWithoutRoles)
                                        .withTransformers("transformer-multi-user-response")
                                        .withStatus(200)
                        )
        );
    }

    public void updateUserProfileRolesMock(HttpStatus status) {
        String body = null;
        int returnHttpStatus = 200;
        if (status.is2xxSuccessful()) {
            body = "{"
                    + "  \"statusUpdateResponse\": {"
                    + "  \"idamStatusCode\": \"200\","
                    + "  \"idamMessage\": \"Success\""
                    + "  } "
                    + "}";
            returnHttpStatus = 200;
        } else if (status.is4xxClientError()) {
            body = "{"
                    + "  \"errorMessage\": \"400\","
                    + "  \"errorDescription\": \"BAD REQUEST\","
                    + "  \"timeStamp\": \"23:10\""
                    + "}";
            returnHttpStatus = 400;
        } else if (status.is5xxServerError()) {

            body = "{"
                    + "  \"roleAdditionResponse\": {"
                    + "  \"idamStatusCode\": \"500\","
                    + "  \"idamMessage\": \"Internal Server Error\""
                    + "  } ,"
                    + "  \"roleDeletionResponse\": ["
                    +   "{"
                    + "  \"idamStatusCode\": \"500\","
                    + "  \"idamMessage\": \"Internal Server Error\""
                    + "  } "
                    + "  ]"
                    + "}";
        }

        userProfileService.stubFor(
                put(urlPathMatching("/v1/userprofile/.*"))
                        .willReturn(aResponse()
                                .withHeader("Content-Type", "application/json")
                                .withBody(body)
                                .withStatus(returnHttpStatus)
                        )
        );


    }

    public void reinviteUserMock(HttpStatus status) {
        String body = null;
        if (status.is2xxSuccessful()) {
            body = "{"
                    + "  \"idamId\":\"" + UUID.randomUUID().toString() + "\","
                    + "  \"idamRegistrationResponse\":\"201\""
                    + "}";
        } else if (status == HttpStatus.BAD_REQUEST) {
            body = "{"
                    + "  \"errorMessage\": \"3 : There is a problem with your request. Please check and try again\","
                    + "  \"errorDescription\": \"User is not in PENDING state\","
                    + "  \"timeStamp\": \"23:10\""
                    + "}";
        } else if (status == HttpStatus.NOT_FOUND) {
            body = "{"
                    + "  \"errorMessage\": \"4 : Resource not found\","
                    + "  \"errorDescription\": \"could not find user profile\","
                    + "  \"timeStamp\": \"23:10\""
                    + "}";
        } else if (status == HttpStatus.TOO_MANY_REQUESTS) {
            body = "{"
                    + "  \"errorMessage\": \"10 : The request was last made less than 1 hour ago. Please try after"
                    + " some time\","
                    + "  \"errorDescription\": \"" + String.format("The request was last made less than %s minutes ago."
                    + " Please try after some time", resendInterval) + "\","
                    + "  \"timeStamp\": \"23:10\""
                    + "}";
        } else if (status == HttpStatus.CONFLICT) {
            body = "{"
                    + "  \"errorMessage\": \"7 : Resend invite failed as user is already active. Wait for one hour "
                    + "for the system to refresh.\","
                    + "  \"errorDescription\": \"" + String.format("Resend invite failed as user is already active. "
                    + "Wait for %s minutes for the system to refresh.", syncInterval) + "\","
                    + "  \"timeStamp\": \"23:10\""
                    + "}";
        }

        userProfileService.stubFor(post(urlEqualTo("/v1/userprofile"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
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
            body = "{"
                    + "  \"errorMessage\": \"400\","
                    + "  \"errorDescription\": \"BAD REQUEST\","
                    + "  \"timeStamp\": \"23:10\""
                    + "}";
            returnHttpStatus = 400;
        }

        userProfileService.stubFor(
                put(urlPathMatching("/v1/userprofile/.*"))
                        .willReturn(aResponse()
                                .withHeader("Content-Type", "application/json")
                                .withBody(body)
                                .withStatus(returnHttpStatus)
                        )
        );


    }

    public void deleteUserProfileMock(HttpStatus status) {
        String body = null;
        int returnHttpStatus = status.value();
        if (status.is2xxSuccessful()) {
            body = "{"

                    + "  \"statusCode\": \"204\","
                    + "  \"message\": \"User Profile Deleted Successfully\""

                    + "}";
            returnHttpStatus = 204;
        }  else if (status == HttpStatus.BAD_REQUEST) {
            body = "{"

                    + "  \"statusCode\": \"400\","
                    + "  \"message\": \"User Profile Delete Request has some problem\""

                + "}";
        } else if (status == HttpStatus.NOT_FOUND) {
            body = "{"

                    + "  \"statusCode\": \"404\","
                    + "  \"message\": \"User Profile Not Found To Delete\""

                + "}";
        }

        userProfileService.stubFor(
                delete(urlEqualTo("/v1/userprofile"))
                        .willReturn(aResponse()
                                .withHeader("Content-Type", "application/json")
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
                        .withHeader("Content-Type", "application/json")
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
                ids =  Optional.ofNullable(mapper.readValue(request.getBodyAsString(), HashMap.class));
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
}

