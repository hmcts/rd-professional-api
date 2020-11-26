package uk.gov.hmcts.reform.professionalapi.controller;

import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import com.google.common.collect.ImmutableMap;
import com.warrenstrange.googleauth.GoogleAuthenticator;

import io.restassured.RestAssured;
import io.restassured.response.Response;

import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.professionalapi.controller.advice.ExternalApiException;

@Slf4j
@Component
public class S2sClient {

    @Value("${idam.s2s-auth.totp_secret}")
    protected String s2sSecret;

    @Value("${idam.s2s-auth.microservice}")
    protected String s2sMicroServiceName;

    @Value("${idam.s2s-auth.url}")
    protected String s2sUrl;

    private final GoogleAuthenticator authenticator = new GoogleAuthenticator();

    public String signIntoS2S() {
        Map<String, Object> params = ImmutableMap.of(
                "microservice", s2sMicroServiceName,
                "oneTimePassword", authenticator.getTotpPassword(s2sSecret));

        Response response = RestAssured
                .given()
                .relaxedHTTPSValidation()
                .baseUri(s2sUrl)
                .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .body(params)
                .post("/lease")
                .andReturn();

        if (response.statusCode() != 200) {
            throw new ExternalApiException(HttpStatus.FORBIDDEN, "S2S Token call failed");
        }

        return response.getBody().asString();
    }
}

