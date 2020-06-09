package uk.gov.hmcts.reform.professionalapi;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import com.google.common.collect.ImmutableMap;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import java.util.Map;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ActiveProfiles;

@RunWith(SpringIntegrationSerenityRunner.class)
@ActiveProfiles("functional")
public class AuthorisedS2sServicesTest extends AuthorizationFunctionalTest {

    private final GoogleAuthenticator authenticator = new GoogleAuthenticator();

    @Test
    public void authorisedService_rd_professional_api_returns_200() {
        Response response = signIntoS2S(s2sUrl, "rd_professional_api", s2sSecret);
        assertThat(response.getStatusCode()).isEqualTo(200);
    }

    @Test
    public void unauthorisedService_returns_401() {
        Response response = signIntoS2S(s2sUrl, "unauthorised_service", s2sSecret);
        assertThat(response.getStatusCode()).isEqualTo(401);
    }

    private Response signIntoS2S(String s2sUrl, String microserviceName, String microserviceKey) {
        Map<String, Object> params = ImmutableMap.of("microservice", microserviceName, "oneTimePassword", authenticator.getTotpPassword(microserviceKey));

        return RestAssured.given().proxy("proxyout.reform.hmcts.net", 8080).relaxedHTTPSValidation().baseUri(this.s2sUrl).header(CONTENT_TYPE, APPLICATION_JSON_VALUE).body(params).post("/lease").andReturn();
    }
}
