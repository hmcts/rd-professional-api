package uk.gov.hmcts.reform.professionalapi;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;

import io.restassured.RestAssured;
import java.util.Map;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import net.serenitybdd.rest.SerenityRest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.professionalapi.infrastructure.controllers.request.OrganisationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.infrastructure.controllers.request.UserCreationRequest;
import uk.gov.hmcts.reform.professionalapi.util.AuthorizationHeadersProvider;

@RunWith(SpringIntegrationSerenityRunner.class)
@SpringBootTest
@ActiveProfiles("functional")
public class OrganisationCreationsTest {

    @Value("${targetInstance}")
    private String targetInstance;

    @Autowired
    private AuthorizationHeadersProvider authorizationHeadersProvider;

    @Before
    public void setUp() {
        RestAssured.baseURI = targetInstance;
        RestAssured.useRelaxedHTTPSValidation();
    }

    @Test
    public void can_create_an_organisation() {

        OrganisationCreationRequest organisationCreationRequest = new OrganisationCreationRequest(
                "some-org-name",
                new UserCreationRequest(
                        "fnmae",
                        "lname",
                        "email-address"
                )
        );

        Map<String, Object> repsonse =
                SerenityRest
                        .given()
                        .contentType(APPLICATION_JSON_UTF8_VALUE)
                        .header(authorizationHeadersProvider.getServiceAuthorization())
                        .body(organisationCreationRequest)
                        .when()
                        .post("/organisations")
                        .then()
                        .statusCode(OK.value())
                        .extract()
                        .body().as(Map.class);

        assertThat(repsonse.get("name")).isEqualTo("some-org-name");
    }

}