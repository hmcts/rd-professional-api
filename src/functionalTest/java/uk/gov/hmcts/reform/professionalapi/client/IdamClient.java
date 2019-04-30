package uk.gov.hmcts.reform.professionalapi.client;

import static org.hamcrest.Matchers.isOneOf;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import com.google.common.collect.ImmutableMap;

import io.restassured.RestAssured;
import io.restassured.response.Response;

import java.util.Base64;
import java.util.Map;
import java.util.Optional;

public class IdamClient {

    private final String idamUrl;
    private final String email;
    private final String password;
    private final String clientId;
    private final String clientSecret;
    private final String redirectUri;

    public IdamClient(
                      String idamUrl,
                      String email,
                      String password,
                      String clientId,
                      String clientSecret,
                      String redirectUri) {
        this.idamUrl = idamUrl;
        this.email = email;
        this.password = password;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.redirectUri = redirectUri;
    }

    /**
     * Registers a new user with Idam's testing support.
     */
    public void registerUser() {
        RestAssured
            .given()
            .relaxedHTTPSValidation()
            .baseUri(this.idamUrl)
            .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
            .body(buildUserRegistrationRequestBody())
            .post(idamUrl + "/testing-support/accounts")
            .then()
            .statusCode(NO_CONTENT.value());
    }

    /**
     * Deletes the user with Idam's testing support.
     */
    public void deleteUser() {
        RestAssured
            .given()
            .relaxedHTTPSValidation()
            .baseUri(this.idamUrl)
            .delete("/testing-support/accounts/{email}", email)
            .then()
            .statusCode(isOneOf(NO_CONTENT.value(), NOT_FOUND.value()));
    }

    public Optional<String> getAuthorisationCode(boolean failIfUnauthorised) {
        Response response = sendAuthorisationRequest();

        if (response.getStatusCode() == OK.value()) {
            return Optional.of(extractAuthorisationCodeFromIdamResponse(response));
        } else if (response.getStatusCode() == UNAUTHORIZED.value() && !failIfUnauthorised) {
            return Optional.empty();
        } else {
            throw new AssertionError(String.format(
                                                   "Unexpected Idam response (%s) when trying to log user in. Response body: %s",
                                                   response.getStatusCode(),
                                                   response.getBody().print()));
        }
    }

    public String getIdamToken(String code) {
        return RestAssured
            .given()
            .relaxedHTTPSValidation()
            .baseUri(this.idamUrl)
            .header(CONTENT_TYPE, APPLICATION_FORM_URLENCODED_VALUE)
            .queryParams(
                         ImmutableMap.of(
                                         "grant_type", "authorization_code",
                                         "client_id", clientId,
                                         "client_secret", clientSecret,
                                         "redirect_uri", redirectUri,
                                         "code", code))
            .post("/oauth2/token")
            .then()
            .statusCode(200)
            .extract()
            .body()
            .jsonPath()
            .get("access_token");
    }

    private Response sendAuthorisationRequest() {
        return RestAssured
            .given()
            .relaxedHTTPSValidation()
            .baseUri(this.idamUrl)
            .header("Authorization", "Basic " + buildIdamSignInToken())
            .queryParams(
                         ImmutableMap.of(
                                         "response_type", "code",
                                         "client_id", clientId,
                                         "client_secret", clientSecret,
                                         "redirect_uri", redirectUri))
            .post("/oauth2/authorize")
            .thenReturn();
    }

    private String buildIdamSignInToken() {
        String unencodedToken = String.format("%s:%s", email, password);

        return Base64.getEncoder().encodeToString(unencodedToken.getBytes());
    }

    private Map<String, String> buildUserRegistrationRequestBody() {
        return ImmutableMap.of(
                               "email", this.email,
                               "forename", "Platform Engineering",
                               "surname", "Smoke tests",
                               "password", this.password);
    }

    private String extractAuthorisationCodeFromIdamResponse(Response response) {
        return response
            .then()
            .statusCode(200)
            .extract()
            .body()
            .jsonPath()
            .get("code");
    }
}
