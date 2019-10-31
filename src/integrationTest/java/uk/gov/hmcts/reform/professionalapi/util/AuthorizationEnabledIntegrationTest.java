package uk.gov.hmcts.reform.professionalapi.util;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static uk.gov.hmcts.reform.professionalapi.utils.OrganisationFixtures.organisationRequestWithAllFields;
import static uk.gov.hmcts.reform.professionalapi.utils.OrganisationFixtures.organisationRequestWithAllFieldsAreUpdated;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
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
import java.util.Optional;
import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.TestPropertySource;
import uk.gov.hmcts.reform.professionalapi.controller.feign.UserProfileFeignClient;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.response.IdamStatus;
import uk.gov.hmcts.reform.professionalapi.persistence.ContactInformationRepository;
import uk.gov.hmcts.reform.professionalapi.persistence.DxAddressRepository;
import uk.gov.hmcts.reform.professionalapi.persistence.OrganisationRepository;
import uk.gov.hmcts.reform.professionalapi.persistence.PaymentAccountRepository;
import uk.gov.hmcts.reform.professionalapi.persistence.ProfessionalUserRepository;
import uk.gov.hmcts.reform.professionalapi.persistence.UserAccountMapRepository;
import uk.gov.hmcts.reform.professionalapi.persistence.UserAttributeRepository;

@Configuration
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

    protected ProfessionalReferenceDataClient professionalReferenceDataClient;

    @Autowired
    protected UserProfileFeignClient userProfileFeignClient;

    @Rule
    public WireMockRule s2sService = new WireMockRule(8990);

    @Rule
    public WireMockRule sidamService = new WireMockRule(WireMockConfiguration.options().port(5000)
            .extensions(new ExternalTransformer()));

    @Rule
    public WireMockRule userProfileService = new WireMockRule(WireMockConfiguration.options().port(8091)
            .extensions(new MultipleUsersResponseTransformer()));

    @Rule
    public WireMockRule ccdService = new WireMockRule(8092);


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

    protected static final String ACTIVE = "ACTIVE";

    @Before
    public void setUpClient() {
        professionalReferenceDataClient = new ProfessionalReferenceDataClient(port);
    }

    @Before
    public void setupIdamStubs() throws Exception {

        s2sService.stubFor(get(urlEqualTo("/details"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("it")));

        s2sService.stubFor(post(urlEqualTo("/lease"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJyZF9wcm9mZXNzaW9uYWxfYXBpIiwiZXhwIjoxNTY0NzU2MzY4fQ.UnRfwq_yGo6tVWEoBldCkD1zFoiMSqqm1rTHqq4f_PuTEHIJj2IHeARw3wOnJG2c3MpjM71ZTFa0RNE4D2AUgA")));

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

    }

    @Before
    public void userProfileGetUserWireMock() {

        ccdService.stubFor(post(urlEqualTo("/user-profile/users"))
                .willReturn(aResponse()
                        .withStatus(201)
                ));

        userProfileService.stubFor(WireMock.get(urlPathMatching("/v1/userprofile.*"))
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
        int returnHttpStatus = status.value();
        if (status.is2xxSuccessful()) {
            body = "{"
                    + "  \"statusCode\":\"200\","
                    + "  \"statusMessage\":\"success\""
                    + "}";
            returnHttpStatus = 200;
        } else if (status.is4xxClientError()) {
            body = "{"
                    + "  \"statusCode\":\"400\","
                    + "  \"statusMessage\":\"Bad Request\""
                    + "}";
            returnHttpStatus = 400;
        } else if (status.is5xxServerError()) {

            body = "{"
                    + "  \"addRolesResponse\": {"
                    + "  \"idamStatusCode\": \"500\","
                    + "  \"idamMessage\": \"Internal Server Error\""
                    + "  } ,"
                    + "  \"deleteRolesResponse\": ["
                    +   "{"
                    + "  \"idamStatusCode\": \"500\","
                    + "  \"idamMessage\": \"Internal Server Error\""
                    + "  } "
                    + "  ]"
                    + "}";
            returnHttpStatus = 500;

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

    public void updateUserProfileAddRolesMock(HttpStatus status) {
        String body = null;
        int returnHttpStatus = status.value();
        if (status.is2xxSuccessful()) {
            body = "{"
                    + "  \"addRolesResponse\": {"
                    + "  \"idamStatusCode\": \"200\","
                    + "  \"idamMessage\": \"Success\""
                    + "  } "
                    + "}";
            returnHttpStatus = 200;
        } else if (status.is4xxClientError()) {
            body = "{"
                    + "  \"statusCode\":\"400\","
                    + "  \"statusMessage\":\"Bad Request\""
                    + "}";
            returnHttpStatus = 400;
        } else if (status.is5xxServerError()) {

            body = "{"
                    + "  \"addRolesResponse\": {"
                    + "  \"idamStatusCode\": \"500\","
                    + "  \"idamMessage\": \"Internal Server Error\""
                    + "  } "
                    + "}";
            returnHttpStatus = 500;

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

