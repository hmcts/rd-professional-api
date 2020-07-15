package uk.gov.hmcts.reform.professionalapi;

import static org.apache.commons.lang.RandomStringUtils.randomAlphabetic;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.professionalapi.controller.request.NewUserCreationRequest.aNewUserCreationRequest;
import static uk.gov.hmcts.reform.professionalapi.controller.request.UserCreationRequest.aUserCreationRequest;
import static uk.gov.hmcts.reform.professionalapi.helper.OrganisationFixtures.createJurisdictions;
import static uk.gov.hmcts.reform.professionalapi.helper.OrganisationFixtures.someMinimalOrganisationRequest;

import com.microsoft.applicationinsights.boot.dependencies.apachecommons.lang3.RandomStringUtils;
import io.restassured.RestAssured;
import io.restassured.parsing.Parser;
import io.restassured.specification.RequestSpecification;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import uk.gov.hmcts.reform.professionalapi.client.ProfessionalApiClient;
import uk.gov.hmcts.reform.professionalapi.client.S2sClient;
import uk.gov.hmcts.reform.professionalapi.config.Oauth2;
import uk.gov.hmcts.reform.professionalapi.config.TestConfigProperties;
import uk.gov.hmcts.reform.professionalapi.controller.request.NewUserCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.UserCreationRequest;
import uk.gov.hmcts.reform.professionalapi.idam.IdamClient;
import uk.gov.hmcts.reform.professionalapi.idam.IdamOpenIdClient;

@RunWith(SpringIntegrationSerenityRunner.class)
@ContextConfiguration(classes = {TestConfigProperties.class, Oauth2.class})
@ComponentScan("uk.gov.hmcts.reform.professionalapi")
@TestPropertySource("classpath:application-functional.yaml")
@Slf4j
public abstract class AuthorizationFunctionalTest {

    @Value("${s2s-url}")
    protected String s2sUrl;

    @Value("${s2s-name}")
    protected String s2sName;

    @Value("${s2s-secret}")
    protected String s2sSecret;

    @Value("${targetInstance}")
    protected String professionalApiUrl;

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

    protected ProfessionalApiClient professionalApiClient;

    protected RequestSpecification bearerToken;

    protected IdamOpenIdClient idamOpenIdClient;

    @Autowired
    protected TestConfigProperties configProperties;

    protected static final String ACCESS_IS_DENIED_ERROR_MESSAGE = "Access is denied";

    protected static Map<String, Long> logFunctionalTime = new HashMap<>();

    private static String callerName;

    @Rule
    public TestName testName = new TestName();

    public static TestName testNameStatic;


    @Before
    public void setUp() {
        logFunctionalTime.put(testName.getMethodName(), System.currentTimeMillis());
        callerName = testName.getMethodName();
        setTestName();
        RestAssured.useRelaxedHTTPSValidation();
        RestAssured.defaultParser = Parser.JSON;

        log.info("Configured S2S secret: " + s2sSecret.substring(0, 2) + "************" + s2sSecret.substring(14));
        log.info("Configured S2S microservice: " + s2sName);
        log.info("Configured S2S URL: " + s2sUrl);

        idamOpenIdClient = new IdamOpenIdClient(configProperties);
        IdamClient idamClient = new IdamClient(configProperties);

        /*SerenityRest.proxy("proxyout.reform.hmcts.net", 8080);
        RestAssured.proxy("proxyout.reform.hmcts.net", 8080);*/

        String s2sToken = new S2sClient(s2sUrl, s2sName, s2sSecret).signIntoS2S();

        professionalApiClient = new ProfessionalApiClient(
            professionalApiUrl,
            s2sToken, idamOpenIdClient, idamClient);
    }

    @After
    public void tearDown() {
        final long startTime = logFunctionalTime.get(testName.getMethodName());
        long endTime = (System.currentTimeMillis() - startTime / 1000) % 60;
        logFunctionalTime.put(testName.getMethodName(), endTime);
        log.info("::method:: {} execution time {} ", testName.getMethodName(), endTime);
    }

    protected String createAndUpdateOrganisationToActive(String role) {
        final long startTime = System.currentTimeMillis();
        Map<String, Object> response = professionalApiClient.createOrganisation();
        logFunctionalTime.put(testName.getMethodName(), System.currentTimeMillis());
        log.info("::executing createAndUpdateOrganisationToActive(role) method called by :: {} execution time {} ",
            testName.getMethodName(), (System.currentTimeMillis() - startTime / 1000) % 60);
        return activateOrganisation(response, role);
    }

    protected String createAndUpdateOrganisationToActive(String role, OrganisationCreationRequest organisationCreationRequest) {
        final long startTime = System.currentTimeMillis();
        Map<String, Object> response = professionalApiClient.createOrganisation(organisationCreationRequest);
        log.info("::executing createAndUpdateOrganisationToActive(role,organisationCreationRequest) method called by ::"
            + "{} execution time {}", testName.getMethodName(), (System.currentTimeMillis() - startTime / 1000) % 60);
        return activateOrganisation(response, role);
    }

    protected String createAndctivateOrganisationWithGivenRequest(OrganisationCreationRequest organisationCreationRequest, String role) {
        final long startTime = System.currentTimeMillis();
        Map<String, Object> organisationCreationResponse = professionalApiClient.createOrganisation(organisationCreationRequest);
        String organisationIdentifier = (String) organisationCreationResponse.get("organisationIdentifier");
        assertThat(organisationIdentifier).isNotEmpty();
        professionalApiClient.updateOrganisation(organisationCreationRequest, role, organisationIdentifier);
        log.info("::executing createAndctivateOrganisationWithGivenRequest method called by :: {} execution time {} ",
            testName.getMethodName(), (System.currentTimeMillis() - startTime / 1000) % 60);
        return organisationIdentifier;
    }

    protected String activateOrganisation(Map<String, Object> organisationCreationResponse, String role) {
        final long startTime = System.currentTimeMillis();
        String organisationIdentifier = (String) organisationCreationResponse.get("organisationIdentifier");
        assertThat(organisationIdentifier).isNotEmpty();
        professionalApiClient.updateOrganisation(organisationIdentifier, role);
        log.info("::executing activateOrganisation method called by :: {} execution time {} ",
            testName.getMethodName(), (System.currentTimeMillis() - startTime / 1000) % 60);
        return organisationIdentifier;
    }

    public RequestSpecification generateBearerTokenFor(String role) {
        final long startTime = System.currentTimeMillis();
        Map<String, Object> response = professionalApiClient.createOrganisation();
        String orgIdentifierResponse = (String) response.get("organisationIdentifier");
        professionalApiClient.updateOrganisation(orgIdentifierResponse, hmctsAdmin);


        List<String> userRoles = new ArrayList<>();
        userRoles.add("pui-user-manager");
        String userEmail = randomAlphabetic(5).toLowerCase() + "@hotmail.com";
        String lastName = "someLastName";
        String firstName = "someName";

        bearerToken = professionalApiClient.getMultipleAuthHeadersExternal(role, firstName, lastName, userEmail);


        NewUserCreationRequest userCreationRequest = aNewUserCreationRequest()
            .firstName(firstName)
            .lastName(lastName)
            .email(userEmail)
            .roles(userRoles)
            .jurisdictions(createJurisdictions())
            .build();
        Map<String, Object> newUserResponse = professionalApiClient.addNewUserToAnOrganisation(orgIdentifierResponse, hmctsAdmin, userCreationRequest, HttpStatus.CREATED);

        log.info("::executing generateBearerTokenFor method called by :: {} execution time {} ",
            testName.getMethodName(), (System.currentTimeMillis() - startTime / 1000) % 60);
        return bearerToken;
    }

    public RequestSpecification generateBearerTokenForExternalUserRolesSpecified(List<String> userRoles) {
        final long startTime = System.currentTimeMillis();
        Map<String, Object> response = professionalApiClient.createOrganisation();
        String orgIdentifierResponse = (String) response.get("organisationIdentifier");
        professionalApiClient.updateOrganisation(orgIdentifierResponse, hmctsAdmin);

        String userEmail = randomAlphabetic(5).toLowerCase() + "@hotmail.com";
        String lastName = "someLastName";
        String firstName = "someName";

        bearerToken = professionalApiClient.getMultipleAuthHeadersExternal(puiUserManager, firstName, lastName, userEmail);


        NewUserCreationRequest userCreationRequest = aNewUserCreationRequest()
            .firstName(firstName)
            .lastName(lastName)
            .email(userEmail)
            .roles(userRoles)
            .jurisdictions(createJurisdictions())
            .build();
        Map<String, Object> newUserResponse = professionalApiClient.addNewUserToAnOrganisation(orgIdentifierResponse, hmctsAdmin, userCreationRequest, HttpStatus.CREATED);

        log.info("::executing generateBearerTokenForExternalUserRolesSpecified method called by :: {} execution time {} ",
            testName.getMethodName(), (System.currentTimeMillis() - startTime / 1000) % 60);
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
        String userEmail = randomAlphabetic(5).toLowerCase() + "@hotmail.com";
        String lastName = "someLastName";
        String firstName = "someFirstName";
        NewUserCreationRequest userCreationRequest = aNewUserCreationRequest()
            .firstName(firstName)
            .lastName(lastName)
            .email(userEmail)
            .roles(userRoles)
            .jurisdictions(createJurisdictions())
            .build();
        return userCreationRequest;
    }

    public RequestSpecification generateSuperUserBearerToken() {
        final long startTime = System.currentTimeMillis();
        String firstName = "some-fname";
        String lastName = "some-lname";
        String email = RandomStringUtils.randomAlphabetic(10) + "@usersearch.test".toLowerCase();
        UserCreationRequest superUser = aUserCreationRequest()
            .firstName(firstName)
            .lastName(lastName)
            .email(email)
            .jurisdictions(createJurisdictions())
            .build();

        bearerToken = professionalApiClient.getMultipleAuthHeadersExternal(puiUserManager, firstName, lastName, email);
        OrganisationCreationRequest request = someMinimalOrganisationRequest()
            .superUser(superUser)
            .build();

        Map<String, Object> response = professionalApiClient.createOrganisation(request);
        String orgIdentifier = (String) response.get("organisationIdentifier");
        request.setStatus("ACTIVE");
        professionalApiClient.updateOrganisation(request, hmctsAdmin, orgIdentifier);
        log.info("::executing generateSuperUserBearerToken method called by :: {} execution time {} ",
            testName.getMethodName(), (System.currentTimeMillis() - startTime / 1000) % 60);
        return bearerToken;
    }

    @AfterClass
    public static void log() {
        log.info("::::exiting from functional suite:::");
        logFunctionalTime.forEach((method, time) -> {
            log.info("::method:: {} execution time {} in seconds ", method, time);
        });
    }

    public static String getCallerName() {
        return callerName;
    }

    private void setTestName() {
        testNameStatic = testName;
    }
}
