package uk.gov.hmcts.reform.professionalapi;

import static org.apache.commons.lang.RandomStringUtils.randomAlphabetic;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.professionalapi.controller.request.NewUserCreationRequest.aNewUserCreationRequest;

import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import net.serenitybdd.rest.SerenityRest;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import uk.gov.hmcts.reform.professionalapi.client.ProfessionalApiClient;
import uk.gov.hmcts.reform.professionalapi.client.S2sClient;
import uk.gov.hmcts.reform.professionalapi.config.Oauth2;
import uk.gov.hmcts.reform.professionalapi.config.TestConfigProperties;
import uk.gov.hmcts.reform.professionalapi.controller.request.NewUserCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.idam.IdamClient;
import uk.gov.hmcts.reform.professionalapi.utils.OrganisationFixtures;

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

    protected RequestSpecification bearerTokenForPuiUserManager;
    protected RequestSpecification bearerTokenForNonPuiUserManager;

    @Autowired
    protected TestConfigProperties configProperties;

    @Before
    public void setUp() {
        RestAssured.useRelaxedHTTPSValidation();

        log.info("Configured S2S secret: " + s2sSecret.substring(0, 2) + "************" + s2sSecret.substring(14));
        log.info("Configured S2S microservice: " + s2sName);
        log.info("Configured S2S URL: " + s2sUrl);

        IdamClient idamClient = new IdamClient(configProperties);

        log.info("idamClient: " + idamClient);
        SerenityRest.proxy("proxyout.reform.hmcts.net", 8080);
        RestAssured.proxy("proxyout.reform.hmcts.net", 8080);

        String s2sToken = new S2sClient(s2sUrl, s2sName, s2sSecret).signIntoS2S();

        professionalApiClient = new ProfessionalApiClient(
                professionalApiUrl,
                s2sToken, idamClient);
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

    protected String activateOrganisation(Map<String, Object> organisationCreationResponse, String role) {
        String organisationIdentifier = (String) organisationCreationResponse.get("organisationIdentifier");
        assertThat(organisationIdentifier).isNotEmpty();
        professionalApiClient.updateOrganisation(organisationIdentifier,role);
        return organisationIdentifier;
    }

    public RequestSpecification generateBearerTokenForPuiManager() {
        Map<String, Object> response = professionalApiClient.createOrganisation();
        log.info("RESPONSE:::::" + response);
        String orgIdentifierResponse = (String) response.get("organisationIdentifier");
        professionalApiClient.updateOrganisation(orgIdentifierResponse, hmctsAdmin);

        log.info("RESPONSE AFTER UPDATE");


        List<String> userRoles = new ArrayList<>();
        userRoles.add("pui-user-manager");
        String userEmail = randomAlphabetic(5).toLowerCase() + "@hotmail.com";
        String lastName = "someLastName";
        String firstName = "someName";

        bearerTokenForPuiUserManager = professionalApiClient.getMultipleAuthHeadersExternal(puiUserManager, firstName, lastName, userEmail);

        log.info("Bearer token generated for non pui user manager:::: " + bearerTokenForPuiUserManager);

        NewUserCreationRequest userCreationRequest = aNewUserCreationRequest()
                .firstName(firstName)
                .lastName(lastName)
                .email(userEmail)
                .roles(userRoles)
                .jurisdictions(OrganisationFixtures.createJurisdictions())
                .build();
        Map<String, Object> newUserResponse = professionalApiClient.addNewUserToAnOrganisation(orgIdentifierResponse, hmctsAdmin, userCreationRequest);

        log.info("NEW USER RESPONSE::::::::::" + newUserResponse);


        return bearerTokenForPuiUserManager;
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
}
