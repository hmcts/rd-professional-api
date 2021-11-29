package uk.gov.hmcts.reform.professionalapi.idam;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.professionalapi.AuthorizationFunctionalTest.EMAIL;
import static uk.gov.hmcts.reform.professionalapi.AuthorizationFunctionalTest.CREDS;
import static uk.gov.hmcts.reform.professionalapi.AuthorizationFunctionalTest.generateRandomEmail;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import com.mifmif.common.regex.Generex;
import io.restassured.response.Response;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.serenitybdd.rest.SerenityRest;
import uk.gov.hmcts.reform.professionalapi.config.TestConfigProperties;


@Slf4j
public class IdamOpenIdClient {

    private TestConfigProperties testConfig;

    private Gson gson = new Gson();

    private static String internalOpenIdTokenPrdAdmin;

    private static String sidamPassword;

    public IdamOpenIdClient(TestConfigProperties testConfig) {
        this.testConfig = testConfig;
    }

    public Map<String, String> createUser(List<String> userRoles) {
        return createUser(userRoles, generateRandomEmail(), "First", "Last");
    }

    public Map<String, String> createUser(List<String> userRoles, String userEmail, String firstName, String lastName) {
        //Generating a random user
        String userGroup = "";
        String password = generateSidamPassword();

        String id = UUID.randomUUID().toString();
        List<Role> roles = new ArrayList<>();
        userRoles.forEach(userRole -> {
            Role role = new Role(userRole);
            roles.add(role);
        });

        Group group = new Group(userGroup);

        User user = new User(userEmail, firstName, id, lastName, password, roles, group);

        String serializedUser = gson.toJson(user);
        //required logs for debug
        //log.info("serializedUser: " + serializedUser);

        Response createdUserResponse = null;

        for (int i = 0; i < 5; i++) {
            log.info("SIDAM createUser retry attempt : " + i + 1);
            createdUserResponse = SerenityRest
                    .given()
                    .relaxedHTTPSValidation()
                    .baseUri(testConfig.getIdamApiUrl())
                    .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                    .body(serializedUser)
                    .post("/testing-support/accounts")
                    .andReturn();
            if (createdUserResponse.getStatusCode() == 504) {
                log.info("SIDAM createUser retry response for attempt " + i + 1 + " 504");
            } else {
                break;
            }
        }

        log.info("SIDAM createUser response: " + createdUserResponse.getStatusCode());

        assertThat(createdUserResponse.getStatusCode()).isEqualTo(201);

        Map<String, String> userCreds = new HashMap<>();
        userCreds.put(EMAIL, userEmail);
        userCreds.put(CREDS, password);
        return userCreds;
    }

    public int createSuperUserWithRetry(List<String> userRoles, String userEmail, String firstName,
                                        String lastName, String password) {
        //Generating a random user
        String userGroup = "";

        String id = UUID.randomUUID().toString();
        List<Role> roles = new ArrayList<>();
        userRoles.forEach(userRole -> {
            Role role = new Role(userRole);
            roles.add(role);
        });

        Group group = new Group(userGroup);

        User user = new User(userEmail, firstName, id, lastName, password, roles, group);

        String serializedUser = gson.toJson(user);
        log.info("serializedUser: " + serializedUser);
        Response createdUserResponse = SerenityRest
                .given()
                .relaxedHTTPSValidation()
                .baseUri(testConfig.getIdamApiUrl())
                .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .body(serializedUser)
                .post("/testing-support/accounts")
                .andReturn();

        log.info("SIDAM createUser with retry response: " + createdUserResponse.getStatusCode());

        return createdUserResponse.getStatusCode();
    }

    public String getInternalOpenIdToken() {
        if (internalOpenIdTokenPrdAdmin == null) {
            List<String> adminRole = new ArrayList<String>();
            adminRole.add("prd-admin");
            Map<String, String> userCreds = createUser(adminRole);
            internalOpenIdTokenPrdAdmin = getOpenIdToken(userCreds.get(EMAIL), userCreds.get(CREDS));
        }
        return internalOpenIdTokenPrdAdmin;
    }

    /*
     This is customized method to generate the token based on passed role
     */
    public String getOpenIdTokenWithGivenRole(String role) {
        List<String> roles = new ArrayList<String>();
        roles.add(role);
        Map<String, String> userCreds = createUser(roles);
        return getOpenIdToken(userCreds.get(EMAIL), userCreds.get(CREDS));
    }

    public String getExternalOpenIdToken(String role, String firstName, String lastName, String email) {
        List<String> roles = new ArrayList<String>();
        roles.add(role);
        Map<String, String> userCreds = createUser(roles, email, firstName, lastName);
        return getOpenIdToken(userCreds.get(EMAIL), userCreds.get(CREDS));
    }

    public String getExternalOpenIdTokenWithRetry(List<String> roles, String firstName, String lastName, String email) {
        String password = generateSidamPassword();
        int statusCode = createSuperUserWithRetry(roles, email, firstName, lastName, password);

        if (statusCode != 201) {
            return String.valueOf(statusCode);
        }

        Map<String, String> userCreds = new HashMap<>();
        userCreds.put(EMAIL, email);
        userCreds.put(CREDS, password);
        return getOpenIdToken(userCreds.get(EMAIL), userCreds.get(CREDS));
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

        Response openIdTokenResponse = SerenityRest
                .given()
                .relaxedHTTPSValidation()
                .baseUri(testConfig.getIdamApiUrl())
                .header(CONTENT_TYPE, APPLICATION_FORM_URLENCODED_VALUE)
                .params(tokenParams)
                .post("/o/token")
                .andReturn();

        log.info("Generate OpenId Token response: " + openIdTokenResponse.getStatusCode());

        assertThat(openIdTokenResponse.getStatusCode()).isEqualTo(200);

        IdamOpenIdClient.BearerTokenResponse accessTokenResponse = gson.fromJson(openIdTokenResponse.getBody()
                .asString(), IdamOpenIdClient.BearerTokenResponse.class);
        return accessTokenResponse.getAccessToken();

    }

    @AllArgsConstructor
    static class User {
        private String email;
        private String forename;
        private String id;
        private String surname;
        private String password;
        private List<Role> roles;
        private Group group;
    }

    @AllArgsConstructor
    static class Role {
        private String code;
    }

    @AllArgsConstructor
    static class Group {
        private String code;
    }

    @Getter
    @AllArgsConstructor
    static class AuthorizationResponse {
        private String code;
    }

    @Getter
    @AllArgsConstructor
    static class BearerTokenResponse {
        @SerializedName("access_token")
        private String accessToken;
    }

    public static String generateSidamPassword() {
        if (isBlank(sidamPassword)) {
            sidamPassword = new Generex("([A-Z])([a-z]{4})([0-9]{4})").random();
        }
        return sidamPassword;
    }


}
