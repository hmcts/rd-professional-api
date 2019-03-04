package uk.gov.hmcts.reform.sysrefdataapi;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;

import io.restassured.RestAssured;
import java.util.Arrays;
import java.util.List;
import net.serenitybdd.rest.SerenityRest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("functional")
public class EndpointSecurityTest {

    @Value("${targetInstance}") private String targetInstance;

    private final List<String> restEndpoints =
        Arrays.asList(
            "/sysrefdata/countries/1"
        );

    @Before
    public void setUp() {
        RestAssured.baseURI = targetInstance;
        RestAssured.useRelaxedHTTPSValidation();
    }

    @Test
    public void should_allow_unauthenticated_requests_to_welcome_message_and_return_200_response_code() {

        String response =
            SerenityRest
                .when()
                .get("/")
                .then()
                .statusCode(HttpStatus.OK.value())
                .and()
                .extract().body().asString();

        assertThat(response)
            .contains("Welcome");
    }

    @Test
    public void should_allow_unauthenticated_requests_to_health_check_and_return_200_response_code() {

        String response =
            SerenityRest
                .when()
                .get("/health")
                .then()
                .statusCode(HttpStatus.OK.value())
                .and()
                .extract().body().asString();

        assertThat(response)
            .contains("UP");
    }

    @Test
    public void should_not_allow_unauthenticated_requests_and_return_403_response_code() {

        restEndpoints.forEach(endpoint ->

            SerenityRest
                .given()
                .contentType(APPLICATION_JSON_UTF8_VALUE)
                .when()
                .get(endpoint)
                .then()
                .statusCode(HttpStatus.FORBIDDEN.value())
        );
    }

    @Test
    public void should_not_allow_requests_without_valid_service_authorisation_and_return_403_response_code() {

        String invalidServiceToken = "invalid";

        restEndpoints.forEach(endpoint ->

            SerenityRest
                .given()
                .contentType(APPLICATION_JSON_UTF8_VALUE)
                .header("ServiceAuthorization", invalidServiceToken)
                .when()
                .get(endpoint)
                .andReturn()
                .then()
                .statusCode(HttpStatus.FORBIDDEN.value())
        );

    }
}
