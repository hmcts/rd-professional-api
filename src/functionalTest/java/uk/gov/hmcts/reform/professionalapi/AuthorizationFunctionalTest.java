package uk.gov.hmcts.reform.professionalapi;

import static java.lang.System.getenv;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang.RandomStringUtils.randomAlphanumeric;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.professionalapi.controller.request.NewUserCreationRequest.aNewUserCreationRequest;
import static uk.gov.hmcts.reform.professionalapi.controller.request.UserCreationRequest.aUserCreationRequest;
import static uk.gov.hmcts.reform.professionalapi.helper.OrganisationFixtures.someMinimalOrganisationRequest;

import io.restassured.RestAssured;
import io.restassured.parsing.Parser;
import io.restassured.specification.RequestSpecification;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
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
import uk.gov.hmcts.reform.professionalapi.config.DbConfig;
import uk.gov.hmcts.reform.professionalapi.config.Oauth2;
import uk.gov.hmcts.reform.professionalapi.config.TestConfigProperties;
import uk.gov.hmcts.reform.professionalapi.controller.constants.IdamStatus;
import uk.gov.hmcts.reform.professionalapi.controller.request.NewUserCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.UserCreationRequest;
import uk.gov.hmcts.reform.professionalapi.domain.UserProfileUpdatedData;
import uk.gov.hmcts.reform.professionalapi.idam.IdamOpenIdClient;
import javax.sql.DataSource;

@ContextConfiguration(classes = {TestConfigProperties.class, Oauth2.class, DbConfig.class})
@ComponentScan("uk.gov.hmcts.reform.professionalapi")
@TestPropertySource("classpath:application-functional.yaml")
@Slf4j()
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

    protected static ProfessionalApiClient professionalApiClient;

    protected RequestSpecification bearerToken;

    protected IdamOpenIdClient idamOpenIdClient;

    @Autowired
    protected TestConfigProperties configProperties;

    @Autowired
    private DataSource dataSource;
    @Autowired
    private DbConfig dbConfig;


    protected static final String ACCESS_IS_DENIED_ERROR_MESSAGE = "Access is denied";

    protected static  String  s2sToken;

    public static final String EMAIL = "EMAIL";

    public static final String CREDS = "CREDS";

    public static final String EMAIL_TEMPLATE = "freg-test-user-%s@prdfunctestuser.com";

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

        if (s2sToken == null) {

            s2sToken = new S2sClient(s2sUrl, s2sName, s2sSecret).signIntoS2S();
        }

        idamOpenIdClient = new IdamOpenIdClient(configProperties);
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
        Map<String, Object> response = professionalApiClient.createOrganisation();
        String orgIdentifierResponse = (String) response.get("organisationIdentifier");
        professionalApiClient.updateOrganisation(orgIdentifierResponse, hmctsAdmin);


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
        Map<String, Object> newUserResponse = professionalApiClient.addNewUserToAnOrganisation(orgIdentifierResponse,
                hmctsAdmin, userCreationRequest, HttpStatus.CREATED);


        return bearerToken;
    }

    public RequestSpecification generateBearerTokenForExternalUserRolesSpecified(List<String> userRoles) {
        Map<String, Object> response = professionalApiClient.createOrganisation();
        String orgIdentifierResponse = (String) response.get("organisationIdentifier");
        professionalApiClient.updateOrganisation(orgIdentifierResponse, hmctsAdmin);

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
        Map<String, Object> newUserResponse = professionalApiClient.addNewUserToAnOrganisation(orgIdentifierResponse,
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

    public RequestSpecification generateSuperUserBearerToken() {
        String firstName = "some-fname";
        String lastName = "some-lname";
        String email = generateRandomEmail();
        UserCreationRequest superUser = aUserCreationRequest()
                .firstName(firstName)
                .lastName(lastName)
                .email(email)
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

    public static String generateRandomEmail() {
        return String.format(EMAIL_TEMPLATE, randomAlphanumeric(10));
    }

    @Override
    public void afterTestClass(TestContext testContext) {
        deleteTestCaseData();
    }

    private void deleteTestCaseData() {
        Connection connection;
        Statement stmt;
        String isNightlyBuild = getenv("isNightlyBuild");
        String testUrl = getenv("TEST_URL");
        log.info("isNightlyBuild: {}", isNightlyBuild);
        log.info("testUrl: {}", testUrl);
        logDbDetails();
        if ("true".equalsIgnoreCase("true")) {
            log.info("Delete test data script execution started");
            try {
                connection = dataSource.getConnection();
                connection.setAutoCommit(false);
                stmt = connection.createStatement();

                String sql = "SELECT count(*) AS orgcount from organisation";
                ResultSet rs = stmt.executeQuery(sql);
                while (rs.next()) {
                    int count  = rs.getInt("orgcount");
                    log.info("Query result: {}", count);
                }
                /*ScriptUtils.executeSqlScript(connection,
                        new EncodedResource(new ClassPathResource("delete-functional-test-data.sql")),
                        false, true,
                        ScriptUtils.DEFAULT_COMMENT_PREFIX,
                        ScriptUtils.DEFAULT_STATEMENT_SEPARATOR,
                        ScriptUtils.DEFAULT_BLOCK_COMMENT_START_DELIMITER,
                        ScriptUtils.DEFAULT_BLOCK_COMMENT_END_DELIMITER);*/
                log.info("Delete test data script execution completed");
            } catch (Exception exe) {
                log.error("Delete test data script execution failed: {}", exe.getMessage());
            }
        } else {
            log.info("Not executing delete test data script");
        }
    }

    private void logDbDetails() {
        if (nonNull(dbConfig)) {
            log.info("Func DB host: {}", dbConfig.postgresHost);
            log.info("Func DB port: {}", dbConfig.postgresPort);
            log.info("Func DB name: {}", dbConfig.postgresDbName);
            log.info("Func DB user name: {}", dbConfig.postgresUserName);
            String url = String.format("jdbc:postgresql://%s:%s/%s", dbConfig.postgresHost,
                    dbConfig.postgresPort, dbConfig.postgresDbName);
            log.info("Func DB url: {}", url);
        } else {
            log.info("No DB info retrieved");
        }

    }
}
