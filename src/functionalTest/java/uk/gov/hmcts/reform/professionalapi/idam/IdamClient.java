package uk.gov.hmcts.reform.professionalapi.idam;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import io.restassured.RestAssured;
import io.restassured.response.Response;

import java.util.ArrayList;
import java.util.Base64;
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
public class IdamClient {

    private final TestConfigProperties testConfig;

    public static final String BASIC = "Basic ";

    private final String password = "Hmcts1234";

    private Gson gson = new Gson();

    public IdamClient(TestConfigProperties testConfig) {
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

    public String getInternalBearerToken() {
        String userEmail = createUser("prd-admin");
        return getBearerToken(userEmail);
    }


    public String getExternalBearerToken(String role, String firstName, String lastName, String email) {
        String userEmail = createUser(role, email, firstName, lastName);
        return getBearerToken(userEmail);
    }

    public String getBearerToken(String userEmail) {

        String codeAuthorization = Base64.getEncoder().encodeToString((userEmail + ":" + password).getBytes());

        Map<String, String> authorizeParams = new HashMap<>();
        authorizeParams.put("client_id", testConfig.getClientId());
        authorizeParams.put("redirect_uri", testConfig.getOauthRedirectUrl());
        authorizeParams.put("response_type", "code");
        authorizeParams.put("scope", "openid roles profile create-user manage-user");

        Response authorizeResponse = RestAssured
                .given()
                .relaxedHTTPSValidation()
                .baseUri(testConfig.getIdamApiUrl())
                .header(CONTENT_TYPE, APPLICATION_FORM_URLENCODED_VALUE)
                .header("Authorization", BASIC + codeAuthorization)
                .params(authorizeParams)
                .post("/oauth2/authorize")
                .andReturn();



        assertThat(authorizeResponse.getStatusCode()).isEqualTo(200);

        AuthorizationResponse authorizationCode = gson.fromJson(authorizeResponse.getBody().asString(), AuthorizationResponse.class);

        String authCode = authorizationCode.getCode();

        Map<String, String> tokenParams = new HashMap<>();
        tokenParams.put("client_id", testConfig.getClientId());
        tokenParams.put("code", authCode);
        tokenParams.put("grant_type", "authorization_code");
        tokenParams.put("redirect_uri", testConfig.getOauthRedirectUrl());

        Response bearerTokenResponse = RestAssured
                .given()
                .relaxedHTTPSValidation()
                .baseUri(testConfig.getIdamApiUrl())
                .header(CONTENT_TYPE, APPLICATION_FORM_URLENCODED_VALUE)
                .header("Authorization", BASIC + testConfig.getTokenAuthorization())
                .params(tokenParams)
                .post("/oauth2/token")
                .andReturn();

        assertThat(bearerTokenResponse.getStatusCode()).isEqualTo(200);

        BearerTokenResponse accessTokenResponse = gson.fromJson(bearerTokenResponse.getBody().asString(), BearerTokenResponse.class);

        //log.info("ACCESS TOKEN RESPONSE:::: " + accessTokenResponse.getAccessToken());
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
