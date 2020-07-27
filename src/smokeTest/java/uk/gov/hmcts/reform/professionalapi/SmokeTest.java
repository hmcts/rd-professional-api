package uk.gov.hmcts.reform.professionalapi;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(SpringIntegrationSerenityRunner.class)
public class SmokeTest {

    // use this when testing locally - replace the below content with this line
    private final String targetInstance =
        StringUtils.defaultIfBlank(
            System.getenv("TEST_URL"),
            "http://localhost:8090"
        );

    @Test
    public void test_should_prove_app_is_running_and_healthy() {
        // local test
        /*SerenityRest.proxy("proxyout.reform.hmcts.net", 8080);
        RestAssured.proxy("proxyout.reform.hmcts.net", 8080);*/

        RestAssured.baseURI = targetInstance;
        RestAssured.useRelaxedHTTPSValidation();

        Response response = RestAssured
                .given()
                .relaxedHTTPSValidation()
                .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .get("/")
                .andReturn();
        if (null != response && response.statusCode() == 200) {
            assertThat(response.body().asString())
                    .contains("Welcome to the System Reference Data API");

        } else {

            Assert.fail();
        }
    }
}
