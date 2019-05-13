package uk.gov.hmcts.reform.professionalapi.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import com.google.common.collect.ImmutableMap;
import com.warrenstrange.googleauth.GoogleAuthenticator;

import io.restassured.RestAssured;
import io.restassured.response.Response;

import java.util.Map;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class S2sClient {

    private final String              s2sUrl;
    private final String              microserviceName;
    private final String              microserviceKey;
    private final GoogleAuthenticator authenticator = new GoogleAuthenticator();

    public S2sClient(String s2sUrl, String microserviceName, String microserviceKey) {
        this.s2sUrl = s2sUrl;
        this.microserviceName = microserviceName;
        this.microserviceKey = microserviceKey;
    }

    /**
     * Sign in to s2s.
     *
     * @return s2s JWT token.
     */
    public String signIntoS2S() {
        Map<String, Object> params = ImmutableMap.of("microservice",
                                                     this.microserviceName,
                                                     "oneTimePassword",
                                                     authenticator.getTotpPassword(this.microserviceKey));

        Response response = RestAssured
                .given()
                .relaxedHTTPSValidation()
                .baseUri(this.s2sUrl)
                .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .body(params)
                .post("/lease")
                .andReturn();

        assertThat(response.getStatusCode()).isEqualTo(200);

        String jwtToken = response.getBody().asString();
        log.debug("Got JWT from S2S service: ", jwtToken);
        return jwtToken;
    }
}
