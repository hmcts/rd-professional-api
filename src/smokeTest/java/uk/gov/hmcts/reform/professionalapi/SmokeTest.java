package uk.gov.hmcts.reform.professionalapi;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import net.serenitybdd.rest.SerenityRest;
import org.apache.commons.lang.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@Slf4j
class SmokeTest {

    // use this when testing locally - replace the below content with this line
    private final String targetInstance =
            StringUtils.defaultIfBlank(
                    System.getenv("TEST_URL"),
                    "http://localhost:8090"
            );

    @Test
    void test_should_prove_app_is_running_and_healthy() {
        // local test
        /*SerenityRest.proxy("proxyout.reform.hmcts.net", 8080);
        RestAssured.proxy("proxyout.reform.hmcts.net", 8080);*/

        SerenityRest.useRelaxedHTTPSValidation();

        Response response = SerenityRest
                .given().log().all()
                .baseUri(targetInstance)
                .relaxedHTTPSValidation()
                .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .get("/")
                .andReturn();
        log.info("Response::" + response);
        if (null != response && response.statusCode() == 200) {
            assertThat(response.body().asString())
                    .contains("Welcome to the System Reference Data API");

        } else {

            Assertions.fail();
        }
    }
}
