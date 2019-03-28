package uk.gov.hmcts.reform.professionalapi;

import static java.util.Arrays.asList;
import static org.apache.commons.lang.RandomStringUtils.randomAlphabetic;
import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static uk.gov.hmcts.reform.professionalapi.infrastructure.controllers.request.PbaAccountCreationRequest.aPbaPaymentAccount;
import static uk.gov.hmcts.reform.professionalapi.infrastructure.controllers.request.UserCreationRequest.aUserCreationRequest;
import static uk.gov.hmcts.reform.professionalapi.utils.OrganisationFixtures.someMinimalOrganisationRequest;

import io.restassured.RestAssured;
import java.util.List;
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
import uk.gov.hmcts.reform.professionalapi.infrastructure.controllers.request.PbaAccountCreationRequest;
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

        String organisationName = randomAlphabetic(10);
        String pbaNumber1 = randomAlphabetic(10);
        String pbaNumber2 = randomAlphabetic(10);

        List<PbaAccountCreationRequest> pbaAccounts = asList(
                aPbaPaymentAccount()
                        .pbaNumber(pbaNumber1)
                        .build(),
                aPbaPaymentAccount()
                        .pbaNumber(pbaNumber2)
                        .build());

        OrganisationCreationRequest organisationCreationRequest =
                someMinimalOrganisationRequest()
                        .name(organisationName)
                        .pbaAccounts(pbaAccounts)
                        .superUser(aUserCreationRequest()
                                .firstName("some-fname")
                                .lastName("some-lname")
                                .email("someone@somewhere.com")
                                .build())
                        .build();

        Map<String, Object> repsonse =
                SerenityRest
                        .given()
                        .contentType(APPLICATION_JSON_UTF8_VALUE)
                        .header(authorizationHeadersProvider.getServiceAuthorization())
                        .body(organisationCreationRequest)
                        .when()
                        .post("/organisations")
                        .then()
                        .statusCode(CREATED.value())
                        .extract()
                        .body().as(Map.class);

        assertThat(repsonse.get("name")).isEqualTo(organisationName);
        assertThat(userIdsFrom(repsonse).size()).isEqualTo(1);
        assertThat(paymentAccountsFrom(repsonse).size()).isEqualTo(2);
    }

    private List<String> userIdsFrom(Map<String, Object> response) {
        return (List<String>) response.get("userIds");
    }

    private List<String> paymentAccountsFrom(Map<String, Object> response) {
        return (List<String>) response.get("pbaAccounts");
    }

}