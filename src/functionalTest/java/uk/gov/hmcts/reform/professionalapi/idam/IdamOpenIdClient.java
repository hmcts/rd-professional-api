package uk.gov.hmcts.reform.professionalapi.idam;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.professionalapi.AuthorizationFunctionalTest.EMAIL;
import static uk.gov.hmcts.reform.professionalapi.AuthorizationFunctionalTest.PASSWORD;
import static uk.gov.hmcts.reform.professionalapi.AuthorizationFunctionalTest.generateRandomEmail;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import io.restassured.RestAssured;
import io.restassured.response.Response;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import uk.gov.hmcts.reform.professionalapi.config.TestConfigProperties;


@Slf4j
public class IdamOpenIdClient {

    private static TestConfigProperties testConfig = null;

    private Gson gson = new Gson();

    private static String internalOpenIdTokenPrdAdmin;

    public IdamOpenIdClient(TestConfigProperties testConfig) {
        this.testConfig = testConfig;
    }

    public Map<String,String> createUser(String userRole) {
        return createUser(userRole, generateRandomEmail(), "First", "Last");
    }

    public Map<String,String> createUser(String userRole, String userEmail, String firstName, String lastName) {
        //Generating a random user
        String userGroup = "";
        String password = generateSidamPassword();

        String id = UUID.randomUUID().toString();

        Role role = new Role(userRole);

        List<Role> roles = new ArrayList<>();
        roles.add(role);

        Group group = new Group(userGroup);

        User user = new User(userEmail, firstName, id, lastName, password, roles, group);

        String serializedUser = gson.toJson(user);

        Response createdUserResponse = RestAssured
            .given()
            .relaxedHTTPSValidation()
            .baseUri(testConfig.getIdamApiUrl())
            .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
            .body(serializedUser)
            .post("/testing-support/accounts")
            .andReturn();


        log.info("openIdTokenResponse createUser response: " + createdUserResponse.getStatusCode());

        assertThat(createdUserResponse.getStatusCode()).isEqualTo(201);

        Map<String,String> userCreds = new HashMap<>();
        userCreds.put("email", userEmail);
        userCreds.put("password", password);
        return userCreds;
    }

    public String getInternalOpenIdToken() {
        if (internalOpenIdTokenPrdAdmin == null) {
            Map<String,String> userCreds = createUser("prd-admin");
            internalOpenIdTokenPrdAdmin = getOpenIdToken(userCreds.get(EMAIL), userCreds.get(PASSWORD));
        }
        return internalOpenIdTokenPrdAdmin;
    }

    /*
     This is customized method to generate the token based on passed role
     */
    public String getOpenIdTokenWithGivenRole(String role) {
        Map<String,String> userCreds = createUser(role);
        return getOpenIdToken(userCreds.get(EMAIL), userCreds.get(PASSWORD));
    }

    public String getExternalOpenIdToken(String role, String firstName, String lastName, String email) {
        Map<String,String> userCreds = createUser(role, email, firstName, lastName);
        return getOpenIdToken(userCreds.get(EMAIL), userCreds.get(PASSWORD));
    }

    public String getOpenIdToken(String userEmail, String password) {

        Map<String, String> tokenParams = new HashMap<>();
        tokenParams.put("grant_type", "password");
        tokenParams.put("username", userEmail);
        tokenParams.put("password", password);
        tokenParams.put("client_id", testConfig.getClientId());
        tokenParams.put("client_secret", testConfig.getClientSecret());
        tokenParams.put("redirect_uri", testConfig.getOauthRedirectUrl());
        tokenParams.put("scope", "openid profile roles manage-user create-user search-user");

        Response openIdTokenResponse = RestAssured
            .given()
            .relaxedHTTPSValidation()
            .baseUri(testConfig.getIdamApiUrl())
            .header(CONTENT_TYPE, APPLICATION_FORM_URLENCODED_VALUE)
            .params(tokenParams)
            .post("/o/token")
            .andReturn();

        log.info("getOpenIdToken response: " + openIdTokenResponse.getStatusCode());

        assertThat(openIdTokenResponse.getStatusCode()).isEqualTo(200);

        IdamOpenIdClient.BearerTokenResponse accessTokenResponse = gson.fromJson(openIdTokenResponse.getBody()
            .asString(), IdamOpenIdClient.BearerTokenResponse.class);
        return accessTokenResponse.getAccessToken();

    }

    @AllArgsConstructor
    class User {
        private String email;
        private String forename;
        private String id;
        private String surname;
        private String password;
        private List<Role> roles;
        private Group group;
    }

    @AllArgsConstructor
    class Role {
        private String code;
    }

    @AllArgsConstructor
    class Group {
        private String code;
    }

    @Getter
    @AllArgsConstructor
    class AuthorizationResponse {
        private String code;
    }

    @Getter
    @AllArgsConstructor
    class BearerTokenResponse {
        @SerializedName("access_token")
        private String accessToken;
    }


    public static String generateSidamPassword() {
        String regex = "^(?=.{10,})(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9]).*$";
        String password = RandomStringUtils.randomAlphanumeric(10);
        if (!password.matches(regex)) {
            password = generateSidamPassword();
        }
        return password;
    }
}
