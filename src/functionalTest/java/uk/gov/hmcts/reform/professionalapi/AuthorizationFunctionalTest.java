package uk.gov.hmcts.reform.professionalapi;

import static java.util.Arrays.asList;
import static org.apache.commons.lang.RandomStringUtils.randomAlphanumeric;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.professionalapi.controller.request.NewUserCreationRequest.aNewUserCreationRequest;
import static uk.gov.hmcts.reform.professionalapi.controller.request.UserCreationRequest.aUserCreationRequest;

import io.restassured.RestAssured;
import io.restassured.parsing.Parser;
import io.restassured.specification.RequestSpecification;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.support.AbstractTestExecutionListener;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import uk.gov.hmcts.reform.professionalapi.client.ProfessionalApiClient;
import uk.gov.hmcts.reform.professionalapi.client.S2sClient;
import uk.gov.hmcts.reform.professionalapi.config.Oauth2;
import uk.gov.hmcts.reform.professionalapi.config.TestConfigProperties;
import uk.gov.hmcts.reform.professionalapi.controller.advice.ExternalApiException;
import uk.gov.hmcts.reform.professionalapi.controller.constants.IdamStatus;
import uk.gov.hmcts.reform.professionalapi.controller.request.NewUserCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.UserCreationRequest;
import uk.gov.hmcts.reform.professionalapi.domain.RoleName;
import uk.gov.hmcts.reform.professionalapi.domain.UserProfileUpdatedData;
import uk.gov.hmcts.reform.professionalapi.idam.IdamOpenIdClient;

@ContextConfiguration(classes = {TestConfigProperties.class, Oauth2.class})
@ComponentScan("uk.gov.hmcts.reform.professionalapi")
@TestPropertySource("classpath:application-functional.yaml")
@Slf4j
@TestExecutionListeners(listeners = {
    AuthorizationFunctionalTest.class,
    DependencyInjectionTestExecutionListener.class})
public class AuthorizationFunctionalTest extends AbstractTestExecutionListener {

    @Value("${s2s-url}")
    protected String s2sUrl;

    @Value("${s2s-name}")
    protected String s2sName;

    @Value("${s2s-secret}")
    protected String s2sSecret;

    @Value("${targetInstance}")
    protected String professionalApiUrl;

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

    protected static ProfessionalApiClient professionalApiClient;

    protected RequestSpecification bearerToken;

    protected static IdamOpenIdClient idamOpenIdClient;

    @Autowired
    protected TestConfigProperties configProperties;

    protected static final String ACCESS_IS_DENIED_ERROR_MESSAGE = "Access is denied";
    protected static String  s2sToken;
    public static final String EMAIL = "EMAIL";
    public static final String CREDS = "CREDS";
    public static final String EMAIL_TEMPLATE = "freg-test-user-%s@prdfunctestuser.com";
    public static String email;
    public static String activeOrgId;
    public static String activeOrgIdForBearerTokens;
    public static String puiUserManagerBearerToken;
    public static String puiCaseManagerBearerToken;
    public static String puiOrgManagerBearerToken;
    public static String puiFinanceManagerBearerToken;
    public static String courtAdminBearerToken;
    public static NewUserCreationRequest bearerTokenUser;


    @Override
    public void beforeTestClass(TestContext testContext) {
        testContext.getApplicationContext()
            .getAutowireCapableBeanFactory()
            .autowireBean(this);

        RestAssured.useRelaxedHTTPSValidation();
        RestAssured.defaultParser = Parser.JSON;

        log.info("Configured S2S secret: " + s2sSecret.substring(0, 2) + "************" + s2sSecret.substring(14));
        log.info("Configured S2S microservice: " + s2sName);
        log.info("Configured S2S URL: " + s2sUrl);

        /*SerenityRest.proxy("proxyout.reform.hmcts.net", 8080);
        RestAssured.proxy("proxyout.reform.hmcts.net", 8080);*/

        if (null == s2sToken) {
            s2sToken = new S2sClient(s2sUrl, s2sName, s2sSecret).signIntoS2S();
        }

        if (null == idamOpenIdClient) {
            idamOpenIdClient = new IdamOpenIdClient(configProperties);
        }
        professionalApiClient = new ProfessionalApiClient(
            professionalApiUrl,
            s2sToken, idamOpenIdClient);

    }

    protected String createAndUpdateOrganisationToActive(String role) {

        Map<String, Object> response = professionalApiClient.createOrganisation();
        return activateOrganisation(response, role);
    }

    protected String createAndUpdateOrganisationToActive(String role,
                                                         OrganisationCreationRequest organisationCreationRequest) {

        Map<String, Object> response = professionalApiClient.createOrganisation(organisationCreationRequest);
        return activateOrganisation(response, role);
    }

    protected String createAndctivateOrganisationWithGivenRequest(
            OrganisationCreationRequest organisationCreationRequest, String role) {
        Map<String, Object> organisationCreationResponse = professionalApiClient
                .createOrganisation(organisationCreationRequest);
        String organisationIdentifier = (String) organisationCreationResponse.get("organisationIdentifier");
        assertThat(organisationIdentifier).isNotEmpty();
        professionalApiClient.updateOrganisation(organisationCreationRequest, role, organisationIdentifier);
        return organisationIdentifier;
    }

    protected String activateOrganisation(Map<String, Object> organisationCreationResponse, String role) {
        String organisationIdentifier = (String) organisationCreationResponse.get("organisationIdentifier");
        assertThat(organisationIdentifier).isNotEmpty();
        professionalApiClient.updateOrganisation(organisationIdentifier,role);
        return organisationIdentifier;
    }

    public RequestSpecification generateBearerTokenFor(String role) {
        List<String> userRoles = new ArrayList<>();
        userRoles.add("pui-user-manager");
        String userEmail = generateRandomEmail();
        String lastName = "someLastName";
        String firstName = "someName";

        bearerToken = professionalApiClient.getMultipleAuthHeadersExternal(role, firstName, lastName, userEmail);

        NewUserCreationRequest userCreationRequest = aNewUserCreationRequest()
                .firstName(firstName)
                .lastName(lastName)
                .email(userEmail)
                .roles(userRoles)
                .build();

        professionalApiClient.addNewUserToAnOrganisation(activeOrgIdForBearerTokens,
                hmctsAdmin, userCreationRequest, HttpStatus.CREATED);

        return bearerToken;
    }

    public RequestSpecification generateBearerTokenForEmailHeader(String role) {
        List<String> userRoles = new ArrayList<>();
        userRoles.add("pui-user-manager");
        String userEmail = generateRandomEmail();
        String lastName = "someLastName";
        String firstName = "someName";

        bearerToken = professionalApiClient.getEmailFromAuthHeadersExternal(role, firstName, lastName, userEmail);

        NewUserCreationRequest userCreationRequest = aNewUserCreationRequest()
                .firstName(firstName)
                .lastName(lastName)
                .email(userEmail)
                .roles(userRoles)
                .build();

        professionalApiClient.addNewUserToAnOrganisation(activeOrgIdForBearerTokens,
                hmctsAdmin, userCreationRequest, HttpStatus.CREATED);

        return bearerToken;
    }

    public RequestSpecification generateBearerTokenForExternalUserRolesSpecified(List<String> userRoles) {
        String userEmail = generateRandomEmail();
        String lastName = "someLastName";
        String firstName = "someName";

        bearerToken = professionalApiClient.getMultipleAuthHeadersExternal(puiUserManager, firstName, lastName,
                userEmail);

        NewUserCreationRequest userCreationRequest = aNewUserCreationRequest()
                .firstName(firstName)
                .lastName(lastName)
                .email(userEmail)
                .roles(userRoles)
                .build();

        professionalApiClient.addNewUserToAnOrganisation(activeOrgIdForBearerTokens,
                hmctsAdmin, userCreationRequest, HttpStatus.CREATED);

        return bearerToken;
    }

    public RequestSpecification generateBearerTokenForExternalUserRolesSpecified(List<String> userRoles, String email) {
        String userEmail = email;
        String lastName = "someLastName";
        String firstName = "someName";

        bearerToken = professionalApiClient.getEmailFromAuthHeadersExternal(puiUserManager, firstName, lastName,
                userEmail);

        NewUserCreationRequest userCreationRequest = aNewUserCreationRequest()
                .firstName(firstName)
                .lastName(lastName)
                .email(userEmail)
                .roles(userRoles)
                .build();

        professionalApiClient.addNewUserToAnOrganisation(activeOrgIdForBearerTokens,
                        hmctsAdmin, userCreationRequest, HttpStatus.CREATED);

        return bearerToken;
    }

    protected void validateUsers(Map<String, Object> searchResponse, Boolean rolesRequired) {
        assertThat(searchResponse.get("idamStatus")).isNotNull();
        assertThat(searchResponse.get("users")).asList().isNotEmpty();

        List<HashMap> professionalUsersResponses = (List<HashMap>) searchResponse.get("users");
        HashMap professionalUsersResponse = professionalUsersResponses.get(0);

        assertThat(professionalUsersResponse.get("userIdentifier")).isNotNull();
        assertThat(professionalUsersResponse.get("firstName")).isNotNull();
        assertThat(professionalUsersResponse.get("lastName")).isNotNull();
        assertThat(professionalUsersResponse.get("email")).isNotNull();
        if (rolesRequired) {
            assertThat(professionalUsersResponse.get("roles")).isNotNull();
        } else {
            assertThat(professionalUsersResponse.get("roles")).isNull();
        }
    }

    protected NewUserCreationRequest createUserRequest(List<String> userRoles) {

        String userEmail = generateRandomEmail();
        String lastName = "someLastName";
        String firstName = "someFirstName";
        NewUserCreationRequest userCreationRequest = aNewUserCreationRequest()
                .firstName(firstName)
                .lastName(lastName)
                .email(userEmail)
                .roles(userRoles)
                .build();
        return userCreationRequest;
    }


    public String getExternalSuperUserTokenWithRetry(String email, String firstName, String lastName) {
        int counter = 0;

        String idamResponse = idamOpenIdClient
                .getExternalOpenIdTokenWithRetry(superUserRoles(), firstName, lastName, email);

        while (idamResponse.equalsIgnoreCase("504") && counter < 4) {
            log.info("Retry Super User Token attempt :" + counter + "/3");
            email = generateRandomEmail().toLowerCase();
            idamResponse = idamOpenIdClient
                    .getExternalOpenIdTokenWithRetry(superUserRoles(), firstName, lastName, email);
            counter++;
        }

        if (idamResponse.equalsIgnoreCase("504")) {
            throw new ExternalApiException(HttpStatus.GATEWAY_TIMEOUT,
                    "Received more than 3 timeouts from IDAM while Generating Token");
        } else {
            return email;
        }
    }

    public UserCreationRequest createSuperUser(String email, String firstName, String lastName) {
        UserCreationRequest superUser = aUserCreationRequest()
                .firstName(firstName)
                .lastName(lastName)
                .email(email)
                .build();
        return superUser;
    }

    public UserProfileUpdatedData getUserStatusUpdateRequest(IdamStatus status) {
        UserProfileUpdatedData data = new UserProfileUpdatedData();
        data.setFirstName("UpdatedFirstName");
        data.setLastName("UpdatedLastName");
        data.setIdamStatus(status.name());
        return data;
    }

    public Map getActiveUser(List<Map> professionalUsersResponses) {

        Map activeUserMap = null;

        for (Map userMap : professionalUsersResponses) {
            if (userMap.get("idamStatus").equals(IdamStatus.ACTIVE.name())) {
                activeUserMap = userMap;
            }
        }
        return activeUserMap;
    }

    public Map getUserById(List<Map<String,Object>> professionalUsersResponses, String userId) {

        Map activeUserMap = null;

        for (Map userMap : professionalUsersResponses) {
            if (userMap.get("userIdentifier").equals(userId)) {
                activeUserMap = userMap;
            }
        }
        return activeUserMap;
    }

    public static String generateRandomEmail() {
        return String.format(EMAIL_TEMPLATE, randomAlphanumeric(10));
    }

    public List<String> addRoles(String role) {
        List<String> roles = new ArrayList<String>();
        roles.add(role);
        return roles;
    }

    public List<String> superUserRoles() {
        List<String> superUserRoles = new ArrayList<String>();
        superUserRoles.add("pui-case-manager");
        superUserRoles.add("pui-user-manager");
        superUserRoles.add("pui-organisation-manager");
        superUserRoles.add("pui-finance-manager");
        //superUserRoles.add("caseworker-divorce-financialremedy");
        //superUserRoles.add("caseworker-divorce-financialremedy-solicitor");
        //superUserRoles.add("caseworker-divorce-solicitor");
        //superUserRoles.add("caseworker-divorce");
        //superUserRoles.add("caseworker");
        //superUserRoles.add("caseworker-probate");
        //superUserRoles.add("caseworker-probate-solicitor");
        //superUserRoles.add("caseworker-publiclaw");
        //superUserRoles.add("caseworker-publiclaw-solicitor");
        //superUserRoles.add("caseworker-ia-legalrep-solicitor");
        //superUserRoles.add("caseworker-ia");
        return superUserRoles;
    }

    public String generateBearerToken(String bearer, String role) {
        if (null == bearer) {
            bearerTokenUser = createUserRequest(asList(role));

            bearer = professionalApiClient.getBearerTokenExternal(role, bearerTokenUser.getFirstName(),
                    bearerTokenUser.getLastName(), bearerTokenUser.getEmail());

            professionalApiClient.addNewUserToAnOrganisation(activeOrgIdForBearerTokens,
                    hmctsAdmin, bearerTokenUser, HttpStatus.CREATED);
            email = bearerTokenUser.getEmail();
        }
        return bearer;
    }

    @SuppressWarnings("unchecked")
    public String searchUserStatus(String orgIdentifier, String userId) {

        Map<String, Object> searchResponse = professionalApiClient
                .searchOrganisationUsersByStatusInternal(orgIdentifier, hmctsAdmin, HttpStatus.OK);
        List<Map> professionalUsersResponses = (List<Map>) searchResponse.get("users");

        return professionalUsersResponses.stream()
                .filter(user -> ((String) user.get("userIdentifier")).equalsIgnoreCase(userId))
                .map(user -> (String) user.get("idamStatus"))
                .collect(Collectors.toList()).get(0);
    }

    public void responseValidate(Map<String, Object> orgResponse) {

        orgResponse.forEach((k,v) -> {

            if ("organisationIdentifier".equals(k) && "http_status".equals(k)
                    && "name".equals(k) &&  "status".equals(k)
                    && "superUser".equals(k) && "paymentAccount".equals(k)) {

                Assertions.assertThat(v.toString()).isNotEmpty();
                Assertions.assertThat(v.toString().contains("Ok"));
                Assertions.assertThat(v.toString().contains("some-org-name"));
                Assertions.assertThat(v.toString().equals("ACTIVE"));
                Assertions.assertThat(v.toString()).isNotEmpty();
            }

        });

    }

    public UserProfileUpdatedData deleteRoleRequest(String role) {
        UserProfileUpdatedData userProfileUpdatedData = new UserProfileUpdatedData();
        RoleName roleName = new RoleName(role);
        Set<RoleName> roles = new HashSet<>();
        roles.add(roleName);
        userProfileUpdatedData.setRolesDelete(roles);
        return userProfileUpdatedData;
    }

    public UserProfileUpdatedData addRoleRequest(String role) {
        UserProfileUpdatedData userProfileUpdatedData = new UserProfileUpdatedData();
        RoleName roleName = new RoleName(role);
        Set<RoleName> roles = new HashSet<>();
        roles.add(roleName);
        userProfileUpdatedData.setRolesAdd(roles);
        return userProfileUpdatedData;
    }

    public void validatePbaResponse(Map<String, Object> response) {
        List<String> pbaList = (List)((Map)response.get("organisationEntityResponse")).get("paymentAccount");
        assertThat(pbaList).hasSize(3);
    }

    public void validateSingleOrgResponse(Map<String, Object> response, String status) {

        Assertions.assertThat(response.size()).isGreaterThanOrEqualTo(1);
        assertThat(response.get("organisationIdentifier")).isNotNull();
        assertThat(response.get("name")).isNotNull();
        assertThat(response.get("status")).isEqualTo(status);
        assertThat(response.get("sraId")).isNotNull();
        assertThat(response.get("sraRegulated")).isNotNull();
        assertThat(response.get("companyNumber")).isNotNull();
        assertThat(response.get("companyUrl")).isNotNull();
        assertThat(response.get("superUser")).isNotNull();
        assertThat(response.get("paymentAccount")).isNotNull();
        assertThat(response.get("contactInformation")).isNotNull();

    }

    public void validateRetrievedUsers(Map<String, Object> searchResponse, String expectedStatus,
                                       Boolean rolesReturned) {
        assertThat(searchResponse.get("users")).asList().isNotEmpty();
        assertThat(searchResponse.get("organisationIdentifier")).isNotNull();
        List<HashMap> professionalUsersResponses = (List<HashMap>) searchResponse.get("users");

        professionalUsersResponses.forEach(user -> {
            assertThat(user.get("idamStatus")).isNotNull();
            assertThat(user.get("userIdentifier")).isNotNull();
            assertThat(user.get("firstName")).isNotNull();
            assertThat(user.get("lastName")).isNotNull();
            assertThat(user.get("email")).isNotNull();
            if (!expectedStatus.equals("any")) {
                assertThat(user.get("idamStatus").equals(expectedStatus));
            }
            if (rolesReturned) {
                if (user.get("idamStatus").equals(IdamStatus.ACTIVE.toString())) {
                    assertThat(user.get("roles")).isNotNull();
                } else {
                    assertThat(user.get("roles")).isNull();
                }
            }
        });
    }

}
