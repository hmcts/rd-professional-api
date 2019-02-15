package uk.gov.hmcts.reform.sysrefdataapi;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import net.serenitybdd.rest.SerenityRest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.sysrefdataapi.domain.entities.Country;
import uk.gov.hmcts.reform.sysrefdataapi.util.AuthorizationHeadersProvider;

@RunWith(SpringIntegrationSerenityRunner.class)
@SpringBootTest
@ActiveProfiles("functional")
public class RetrieveSysRefDataResource {

    @Value("${targetInstance}") private String targetInstance;

    @Autowired private Environment environment;
    @Autowired private AuthorizationHeadersProvider authorizationHeadersProvider;
    @Autowired private ObjectMapper objectMapper;

    @Before
    public void setUp() {
        RestAssured.baseURI = targetInstance;
        RestAssured.useRelaxedHTTPSValidation();
    }

    @Test
    public void should_get_country_resource() {

        Country expectation = new Country("1", "South Africa");

        Country result =
            SerenityRest
                .given()
                .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                .when()
                .get("/sysrefdata/countries/1")
                .then()
                .statusCode(HttpStatus.OK.value())
                .and()
                .extract()
                .body().as(Country.class);

        assertThat(result).isEqualToComparingFieldByField(expectation);

    }

}