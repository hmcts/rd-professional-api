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
import org.junit.Before;
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
@TestPropertySource("classpath:application-functional.yml")
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

    protected ProfessionalApiClient professionalApiClient;

    protected RequestSpecification bearerToken;

    protected IdamOpenIdClient idamOpenIdClient;

    @Autowired
    protected TestConfigProperties configProperties;

    protected static final String ACCESS_IS_DENIED_ERROR_MESSAGE = "Access is denied";

    @Before
    public void setUp() {
        RestAssured.useRelaxedHTTPSValidation();
        RestAssured.defaultParser = Parser.JSON;

        log.info("Configured S2S secret: " + s2sSecret.substring(0, 2) + "************" + s2sSecret.substring(14));
        log.info("Configured S2S microservice: " + s2sName);
        log.info("Configured S2S URL: " + s2sUrl);

        idamOpenIdClient = new IdamOpenIdClient(configProperties);
        IdamClient idamClient = new IdamClient(configProperties);

        /* SerenityRest.proxy("proxyout.reform.hmcts.net", 8080);
        RestAssured.proxy("proxyout.reform.hmcts.net", 8080);*/

        String s2sToken = new S2sClient(s2sUrl, s2sName, s2sSecret).signIntoS2S();

        professionalApiClient = new ProfessionalApiClient(
                professionalApiUrl,
                s2sToken, idamOpenIdClient, idamClient);
    }

    @After
    public void tearDown() {
    }

    protected String createAndUpdateOrganisationToActive(String role) {

        Map<String, Object> response = professionalApiClient.createOrganisation();
        return activateOrganisation(response, role);
    }

    protected String createAndUpdateOrganisationToActive(String role, OrganisationCreationRequest organisationCreationRequest) {

        Map<String, Object> response = professionalApiClient.createOrganisation(organisationCreationRequest);
        return activateOrganisation(response, role);
    }

    protected String createAndctivateOrganisationWithGivenRequest(OrganisationCreationRequest organisationCreationRequest, String role) {
        Map<String, Object> organisationCreationResponse = professionalApiClient.createOrganisation(organisationCreationRequest);
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


        return bearerToken;
    }

    public RequestSpecification generateBearerTokenForExternalUserRolesSpecified(List<String> userRoles) {
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
        return bearerToken;
    }

    public UserCreationRequest createSuperUser(String email, String firstName, String lastName) {
        UserCreationRequest superUser = aUserCreationRequest()
                .firstName(firstName)
                .lastName(lastName)
                .email(email)
                .jurisdictions(createJurisdictions())
                .build();
        return superUser;
    }

}
