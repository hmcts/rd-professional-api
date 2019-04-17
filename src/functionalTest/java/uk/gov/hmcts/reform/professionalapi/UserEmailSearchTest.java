package uk.gov.hmcts.reform.professionalapi;

import static org.apache.commons.lang.RandomStringUtils.randomAlphabetic;
import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static uk.gov.hmcts.reform.professionalapi.infrastructure.controllers.request.UserCreationRequest.aUserCreationRequest;
import static uk.gov.hmcts.reform.professionalapi.utils.OrganisationFixtures.someMinimalOrganisationRequest;

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
import uk.gov.hmcts.reform.professionalapi.util.AuthorizationHeadersProvider;

@RunWith(SpringIntegrationSerenityRunner.class)
@SpringBootTest
@ActiveProfiles("functional")
public class UserEmailSearchTest {

    @Value("${targetInstance}")
    private String targetInstance;

    @Autowired
    private AuthorizationHeadersProvider authorizationHeadersProvider;

    @Before
    public void setUp() {
        RestAssured.baseURI = targetInstance;
        RestAssured.useRelaxedHTTPSValidation();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void can_find_a_user_by_their_email_address() {

        String organisationName = randomAlphabetic(10);

        OrganisationCreationRequest organisationCreationRequest = someMinimalOrganisationRequest()
                .name(organisationName)
                .superUser(aUserCreationRequest()
                        .firstName("some-fname")
                        .lastName("some-lname")
                        .email("someone@somewhereelse.com")
                        .build())
                .build();

        SerenityRest
                .given()
                .contentType(APPLICATION_JSON_UTF8_VALUE)
                .header(authorizationHeadersProvider.getServiceAuthorization())
                .body(organisationCreationRequest)
                .when()
                .post("/organisations")
                .then()
                .statusCode(CREATED.value());

        Map<String, Object> searchResponse = SerenityRest
                .given()
                .contentType(APPLICATION_JSON_UTF8_VALUE)
                .header(authorizationHeadersProvider.getServiceAuthorization())
                .when()
                .get("/search/user/someone@somewhereelse.com")
                .then()
                .statusCode(OK.value())
                .extract()
                .body()
                .as(Map.class);

        assertThat(searchResponse.get("firstName")).isEqualTo("some-fname");
    }

}