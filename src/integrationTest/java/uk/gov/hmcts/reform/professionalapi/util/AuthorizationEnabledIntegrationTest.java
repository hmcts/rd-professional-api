package uk.gov.hmcts.reform.professionalapi.util;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static uk.gov.hmcts.reform.professionalapi.controller.request.NewUserCreationRequest.aNewUserCreationRequest;
import static uk.gov.hmcts.reform.professionalapi.helper.OrganisationFixtures.createJurisdictions;
import static uk.gov.hmcts.reform.professionalapi.helper.OrganisationFixtures.organisationRequestWithAllFields;
import static uk.gov.hmcts.reform.professionalapi.helper.OrganisationFixtures.organisationRequestWithAllFieldsAreUpdated;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.extension.ResponseTransformer;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.Response;
import com.github.tomakehurst.wiremock.junit.WireMockRule;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.TestPropertySource;
import uk.gov.hmcts.reform.professionalapi.controller.constants.IdamStatus;
import uk.gov.hmcts.reform.professionalapi.controller.request.NewUserCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.repository.ContactInformationRepository;
import uk.gov.hmcts.reform.professionalapi.repository.DxAddressRepository;
import uk.gov.hmcts.reform.professionalapi.repository.OrganisationRepository;
import uk.gov.hmcts.reform.professionalapi.repository.PaymentAccountRepository;
import uk.gov.hmcts.reform.professionalapi.repository.ProfessionalUserRepository;
import uk.gov.hmcts.reform.professionalapi.repository.UserAccountMapRepository;
import uk.gov.hmcts.reform.professionalapi.repository.UserAttributeRepository;

@Configuration
@AutoConfigureWireMock(port = 5000)
@TestPropertySource(properties = {"S2S_URL=http://127.0.0.1:8990","IDAM_URL:http://127.0.0.1:5000", "USER_PROFILE_URL:http://127.0.0.1:8091", "CCD_URL:http://127.0.0.1:8092"})
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

    protected ProfessionalReferenceDataClient professionalReferenceDataClient = new ProfessionalReferenceDataClient(port);

    @ClassRule
    public static WireMockRule s2sService = new WireMockRule(8990);



    //@ClassRule
    public static WireMockRule sidamService = new WireMockRule(WireMockConfiguration.options().port(5000).withRootDirectory("src/test/resources/mappings")
            .extensions(new ExternalTransformer()));

    @ClassRule
    public static WireMockRule userProfileService = new WireMockRule(WireMockConfiguration.options().port(8091)
            .extensions(new MultipleUsersResponseTransformer()));

    @ClassRule
    public static WireMockRule ccdService = new WireMockRule(8092);


    @Value("${exui.role.hmcts-admin}")
    protected String hmctsAdmin;

    @Value("${exui.role.pui-user-manager}")
    protected String puiUserManager;

    @Value("${exui.role.pui-organisation-manager}")
    protected String puiOrgManager;

    @Value("${exui.role.pui-finance-manager}")
    protected String puiFinanceManager;

    @Value("${exui.role.pui-case-manager}")
    protected String puiCaseManager;

    @Value("${resendInterval}")
    protected String resendInterval;

    @Value("${syncInterval}")
    protected String syncInterval;

    protected static final String ACTIVE = "ACTIVE";

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
                        .withBody("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c")));


        /*sidamService.stubFor(get(urlPathMatching("/o/.well-known/openid-configuration"))
                .willReturn(aResponse()
                .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("eyJhbGciOiJIUzI1NiIsInR5cCI6Ik")));


         "{"
                            + " \"request_parameter_supported=true \","
                            + " \"claims_parameter_supported=false\","
                            + " \"scopes_supported= \"["
                            + " \"acr\","
                            + " \"openid\","
                            + " \"profile\","
                            + " \"roles\","
                            + " \"authorities\","
                            + " \"email\""
                            +  "],"
                            +  " \"issuer= https" + " \":"
                            +  " \"id_token_encryption_enc_values_supported= \"["
                            +  " \"A256GCM\","
                            +  " \"A192GCM\","
                            +  " \"A128GCM\","
                            +  " \"A128CBC-HS256\","
                            +  " \"A192CBC-HS384\","
                            +  " \"A256CBC-HS512\","
                            +  " ],"
                            +  " \"acr_values_supported= \"["

                            + "],"
                            +   " \"authorization_endpoint=https" + " \":"
                            +  " \"request_object_encryption_enc_values_supported= \"["
                            +  " \"A256GCM\","
                            +  " \"A192GCM\","
                            +  " \"A128GCM\","
                            +  " \"A128CBC-HS256\","
                            +  " \"A192CBC-HS384\","
                            +  " \"A256CBC-HS512\","
                            + " ],"

                            + " \"rcs_request_encryption_alg_values_supported= \"["
                            + " \"RSA-OAEP\","
                            + " \"RSA-OAEP-256\","
                            + " \"A128KW\","
                            + " \"RSA1_5\","
                            + " \"dir\""
                            + " ],"
                            + " \"claims_supported= \"["

                            + " ],"
                            + " \"rcs_request_signing_alg_values_supported= \"["
                            + " \" PS384\","
                            + " \" RS384\","
                            + " \" HS256\","
                            + " \" HS512\","
                            + " \" PS384\","
                            + " \" ES384\" "
                            +  " ],"
                            + " \"token_endpoint_auth_methods_supported= \"["
                            + " \"client_secret_post\","
                            + " \"private_key_jwt\","
                            + " \"self_signed_tls_client_auth\","
                            + " \"tls_client_auth\","
                            + " \"none\","
                            + " \"client_secret_basic\""
                            + " ],"
                            + " \"token_endpoint=https\":,"
                            + " \"response_types_supported=\"["
                            + " \"code token id_token\","
                            + " \"code\","
                            + " \"code id_token\","
                            + " \"device_code\","
                            + " \"id_token\","
                            + " \"code token\","
                            + " \"token\","
                            + " \"token id_token\""
                            + " ],"
                            + " \"request_uri_parameter_supported= true\","
                            + " \"rcs_response_encryption_enc_values_supported= \"["
                            + " \" A256GCM\","
                            + " \" A192GCM\","
                            + " \" A128GCM\","
                            + " \" A128CBC-HS256\","
                            + " \" A192CBC-HS384\","
                            + " \" A256CBC-HS512\" "
                            + "],"
                            + " \"end_session_endpoint=https\":,"
                            + " \"rcs_request_encryption_enc_values_supported= \"["
                            + " \" A256GCM\","
                            + " \" A192GCM\","
                            + " \" A128GCM\","
                            + " \" A128CBC-HS256\","
                            + " \" A192CBC-HS384\","
                            + " \" A256CBC-HS512\" "
                            + "],"
                            + " \"version=3.0\","
                            + " \"rcs_response_encryption_alg_values_supported= \"["
                            + " \"RSA-OAEP \","
                            + " \"A128KW\","
                            + " \"RSA-OAEP-256\","
                            + " \" A256KW\","
                            + " \"dir\","
                            + " \" A192KW\" "
                            + "],"
                            + " \"userinfo_endpoint=https\":,"
                            + " \"id_token_encryption_alg_values_supported= \"["
                            + " \"RSA-OAEP\","
                            + " \"RSA-OAEP-256\","
                            + " \"A128KW\","
                            + " \"RSA-OAEP-256\","
                            + " \" A256KW\","
                            + " \"dir\","
                            + " \" A192KW\" "
                            + "], "
                            + " \"jwks_uri=https \":,"
                            + " \"subject_types_supported= \"["
                            + " \"public\" "
                            + "],"
                            + " \"id_token_signing_alg_values_supported= \"["
                            + " \" ES384\","
                            + " \" HS256\","
                            + " \" HS512\","
                            + " \" ES256\","
                            + " \" HS384\","
                            + " \" ES512\" "
                            + "], "
                            + " \" ES384\","
                            + " \" HS256\","
                            + " \" HS512\","
                            + " \" ES256\","
                            + " \" HS384\","
                            + " \" ES512\" "
                            + " \"request_object_encryption_alg_values_supported= \"["
                            + " \"RSA-OAEP\","
                            + " \"RSA-OAEP-256\","
                            + " \"A128KW\","
                            + " \"RSA-OAEP-256\","
                            + " \" A256KW\","
                            + " \"dir\","
                            + " \" A192KW\" "
                            + "], "
                            + " \"rcs_response_signing_alg_values_supported= \"["
                            + " \" PS384\","
                            + " \" ES384\","
                            + " \" HS512\","
                            + " \" HS256\","
                            + " \" HS384\","
                            + " \" RS256\" "
                            + " \" HS384\","
                            + " \" PS256\","
                            + " \" PS512\","
                            + "] "
                            +  "}")));*/


        sidamService.stubFor(get(urlEqualTo("/details"))
                .withHeader("Authorization", containing("pui-finance-manager"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{"
                                +  "  \"id\": \"%s\","
                                +  "  \"forename\": \"Super\","
                                +  "  \"surname\": \"User\","
                                +  "  \"email\": \"super.user@hmcts.net\","
                                +  "  \"accountStatus\": \"active\","
                                +  "  \"roles\": ["
                                +  "  \"pui-finance-manager\""
                                +  "  ]"
                                +  "}")
                        .withTransformers("external_user-token-response")));

        sidamService.stubFor(get(urlEqualTo("/details"))
                .withHeader("Authorization", containing("pui-case-manager"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{"
                                +  "  \"id\": \"%s\","
                                +  "  \"forename\": \"Super\","
                                +  "  \"surname\": \"User\","
                                +  "  \"email\": \"super.user@hmcts.net\","
                                +  "  \"accountStatus\": \"active\","
                                +  "  \"roles\": ["
                                +  "  \"pui-case-manager\""
                                +  "  ]"
                                +  "}")
                        .withTransformers("external_user-token-response")));

        sidamService.stubFor(get(urlEqualTo("/details")).withHeader("Authorization", containing("prd-admin"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{"
                                +  "  \"id\": \"%s\","
                                +  "  \"forename\": \"Super\","
                                +  "  \"surname\": \"User\","
                                +  "  \"email\": \"super.user@hmcts.net\","
                                +  "  \"accountStatus\": \"active\","
                                +  "  \"roles\": ["
                                +  "  \"prd-admin\""
                                +  "  ]"
                                +  "}")
                        .withTransformers("external_user-token-response")));




        sidamService.stubFor(get(urlEqualTo("/details"))
                .withHeader("Authorization", containing("pui-organisation-manager"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{"
                                +  "  \"id\": \"%s\","
                                +  "  \"forename\": \"Super\","
                                +  "  \"surname\": \"User\","
                                +  "  \"email\": \"super.user@hmcts.net\","
                                +  "  \"accountStatus\": \"active\","
                                +  "  \"roles\": ["
                                +  "  \"pui-organisation-manager\""
                                +  "  ]"
                                +  "}")
                        .withTransformers("external_user-token-response")));

        sidamService.stubFor(get(urlEqualTo("/details"))
                .withHeader("Authorization", containing("pui-user-manager"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{"
                                +  "  \"id\": \"%s\","
                                +  "  \"forename\": \"Super\","
                                +  "  \"surname\": \"User\","
                                +  "  \"email\": \"super.user@hmcts.net\","
                                +  "  \"accountStatus\": \"active\","
                                +  "  \"roles\": ["
                                +  "  \"pui-user-manager\""
                                +  "  ]"
                                +  "}")
                        .withTransformers("external_user-token-response")));


        sidamService.stubFor(get(urlEqualTo("/o/userinfo"))
                .withHeader("Authorization", containing("pui-organisation-manager"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{"
                                +  "  \"uid\": \"%s\","
                                +  "  \"name\": \"Super\","
                                +  "  \"family_name\": \"User\","
                                +  "  \"given_name\": \"User\","
                                +  "  \"sub\": \"super.user@hmcts.net\","
                                +  "  \"accountStatus\": \"active\","
                                +  "  \"roles\": ["
                                +  "  \"pui-organisation-manager\""
                                +  "  ]"
                                +  "}")
                        .withTransformers("external_user-token-response")));

        sidamService.stubFor(get(urlEqualTo("/o/userinfo"))
                .withHeader("Authorization", containing("pui-user-manager"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{"
                                +  "  \"uid\": \"%s\","
                                +  "  \"name\": \"Super\","
                                +  "  \"family_name\": \"User\","
                                +  "  \"given_name\": \"User\","
                                +  "  \"sub\": \"super.user@hmcts.net\","
                                +  "  \"accountStatus\": \"active\","
                                +  "  \"roles\": ["
                                +  "  \"pui-user-manager\""
                                +  "  ]"
                                +  "}")
                        .withTransformers("external_user-token-response")));

        sidamService.stubFor(get(urlEqualTo("/o/userinfo"))
                .withHeader("Authorization", containing("pui-case-manager"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{"
                                +  "  \"uid\": \"%s\","
                                +  "  \"name\": \"Super\","
                                +  "  \"family_name\": \"User\","
                                +  "  \"given_name\": \"User\","
                                +  "  \"sub\": \"super.user@hmcts.net\","
                                +  "  \"accountStatus\": \"active\","
                                +  "  \"roles\": ["
                                +  "  \"pui-user-manager\","
                                +  "  \"pui-case-manager\""
                                +  "  ]"
                                +  "}")
                        .withTransformers("external_user-token-response")));

        sidamService.stubFor(get(urlEqualTo("/o/userinfo"))
                .withHeader("Authorization", containing("pui-finance-manager"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{"
                                +  "  \"uid\": \"%s\","
                                +  "  \"name\": \"Super\","
                                +  "  \"family_name\": \"User\","
                                +  "  \"given_name\": \"User\","
                                +  "  \"sub\": \"super.user@hmcts.net\","
                                +  "  \"accountStatus\": \"active\","
                                +  "  \"roles\": ["
                                +  "  \"pui-user-manager\","
                                +  "  \"pui-finance-manager\""
                                +  "  ]"
                                +  "}")
                        .withTransformers("external_user-token-response")));

        sidamService.stubFor(get(urlEqualTo("/o/userinfo"))
                .withHeader("Authorization", containing("pui-organisation-manager"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{"
                                +  "  \"uid\": \"%s\","
                                +  "  \"name\": \"Super\","
                                +  "  \"family_name\": \"User\","
                                +  "  \"given_name\": \"User\","
                                +  "  \"sub\": \"super.user@hmcts.net\","
                                +  "  \"accountStatus\": \"active\","
                                +  "  \"roles\": ["
                                +  "  \"pui-user-manager\","
                                +  "  \"pui-organisation-manager\""
                                +  "  ]"
                                +  "}")
                        .withTransformers("external_user-token-response")));

        sidamService.stubFor(get(urlEqualTo("/o/userinfo"))
                .withHeader("Authorization", containing("prd-admin"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{"
                                +  "  \"uid\": \"%s\","
                                +  "  \"name\": \"Super\","
                                +  "  \"family_name\": \"User\","
                                +  "  \"given_name\": \"User\","
                                +  "  \"sub\": \"super.user@hmcts.net\","
                                +  "  \"accountStatus\": \"active\","
                                +  "  \"roles\": ["
                                +  "  \"prd-admin\""
                                +  "  ]"
                                +  "}")
                        .withTransformers("external_user-token-response")));







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
        NewUserCreationRequest userCreationRequest = aNewUserCreationRequest()
                .firstName("someName")
                .lastName("someLastName")
                .email(randomAlphabetic(5) + "@email.com")
                .roles(userRoles)
                .jurisdictions(createJurisdictions())
                .build();

        userProfileCreateUserWireMock(HttpStatus.CREATED);
        Map<String, Object> newUserResponse =
                professionalReferenceDataClient.addUserToOrganisation(organisationIdentifier, userCreationRequest, hmctsAdmin);

        return (String) newUserResponse.get("userIdentifier");
    }

    public String createOrganisationRequest() {
        OrganisationCreationRequest organisationCreationRequest = organisationRequestWithAllFields().build();
        java.util.Map<String, Object> responseForOrganisationCreation = professionalReferenceDataClient.createOrganisation(organisationCreationRequest);
        return (String) responseForOrganisationCreation.get("organisationIdentifier");
    }

    public void updateOrganisation(String organisationIdentifier, String role, String status) {
        userProfileCreateUserWireMock(HttpStatus.CREATED);
        OrganisationCreationRequest organisationUpdateRequest = organisationRequestWithAllFieldsAreUpdated().status(status).build();
        professionalReferenceDataClient.updateOrganisation(organisationUpdateRequest, role, organisationIdentifier);
    }

    public String createAndActivateOrganisation() {
        String orgIdentifier = createOrganisationRequest();
        updateOrganisation(orgIdentifier, hmctsAdmin, ACTIVE);
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
                    + "  \"errorMessage\": \"10 : The request was last made less than 1 hour ago. Please try after some time\","
                    + "  \"errorDescription\": \"" + String.format("The request was last made less than %s minutes ago. Please try after some time", resendInterval) + "\","
                    + "  \"timeStamp\": \"23:10\""
                    + "}";
        } else if (status == HttpStatus.CONFLICT) {
            body = "{"
                    + "  \"errorMessage\": \"7 : Resend invite failed as user is already active. Wait for one hour for the system to refresh.\","
                    + "  \"errorDescription\": \"" + String.format("Resend invite failed as user is already active. Wait for %s minutes for the system to refresh.", syncInterval) + "\","
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
            String userId = token.split(" ")[1];

            formatResponse = String.format(formatResponse, userId);

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
}

