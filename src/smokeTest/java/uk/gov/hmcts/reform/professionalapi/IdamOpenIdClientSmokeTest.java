package uk.gov.hmcts.reform.professionalapi;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import io.restassured.RestAssured;
import io.restassured.response.Response;

import java.util.*;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;

@Slf4j
public class IdamOpenIdClientSmokeTest {

    private final TestConfigPropertiesSmokeTest testConfig;

    public static final String BASIC = "Basic ";

    private final String password = "Hmcts1234";

    private Gson gson = new Gson();

    public IdamOpenIdClientSmokeTest(TestConfigPropertiesSmokeTest testConfig) {
        this.testConfig = testConfig;
    }

    public String createUser(String userRole) {
        return createUser(userRole, nextUserEmail(), "First", "Last");
    }

    public String createUser(String userRole, String userEmail, String firstName, String lastName) {
        //Generating a random user
        String userGroup = "";
        String password = "Hmcts1234";

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

        assertThat(createdUserResponse.getStatusCode()).isEqualTo(201);

        return userEmail;
    }

    public String getInternalOpenIdToken() {
        String userEmail = createUser("prd-admin");
        return getOpenIdToken(userEmail);
    }


    public String getExternalOpenIdToken(String role, String firstName, String lastName, String email) {
        String userEmail = createUser(role, email, firstName, lastName);
        return getOpenIdToken(userEmail);
    }

    public String getOpenIdToken(String userEmail) {

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

        assertThat(openIdTokenResponse.getStatusCode()).isEqualTo(200);

        IdamOpenIdClientSmokeTest.BearerTokenResponse accessTokenResponse = gson.fromJson(openIdTokenResponse.getBody().asString(), IdamOpenIdClientSmokeTest.BearerTokenResponse.class);
        return accessTokenResponse.getAccessToken();

    }


    private String nextUserEmail() {
        return String.format(testConfig.getGeneratedUserEmailPattern(), RandomStringUtils.randomAlphanumeric(10));
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
}
