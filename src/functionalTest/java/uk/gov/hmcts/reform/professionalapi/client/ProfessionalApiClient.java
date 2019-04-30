package uk.gov.hmcts.reform.professionalapi.client;

import static java.util.Arrays.asList;
import static org.springframework.http.HttpStatus.CREATED;
import static uk.gov.hmcts.reform.professionalapi.infrastructure.controllers.request.PbaAccountCreationRequest.aPbaPaymentAccount;
import static uk.gov.hmcts.reform.professionalapi.infrastructure.controllers.request.UserCreationRequest.aUserCreationRequest;
import static uk.gov.hmcts.reform.professionalapi.utils.OrganisationFixtures.someMinimalOrganisationRequest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import uk.gov.hmcts.reform.professionalapi.infrastructure.controllers.request.OrganisationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.infrastructure.controllers.request.PbaAccountCreationRequest;

public class ProfessionalApiClient {

    private static final ObjectMapper mapper = new ObjectMapper();

    private static final String SERVICE_HEADER = "ServiceAuthorization";

    private final String professionalApiUrl;
    private final String s2sToken;

    public ProfessionalApiClient(
                                 String professionalApiUrl,
                                 String s2sToken) {
        this.professionalApiUrl = professionalApiUrl;
        this.s2sToken = s2sToken;
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> createOrganisation(String organisationName,
                                                  String[] pbas) {

        List<PbaAccountCreationRequest> pbaAccounts = asList(
                                                             aPbaPaymentAccount()
                                                                 .pbaNumber(pbas[0])
                                                                 .build(),
                                                             aPbaPaymentAccount()
                                                                 .pbaNumber(pbas[1])
                                                                 .build());

        PbaAccountCreationRequest superUserPaymentAccount = aPbaPaymentAccount()
            .pbaNumber(pbas[1])
            .build();

        OrganisationCreationRequest organisationCreationRequest = someMinimalOrganisationRequest()
            .name(organisationName)
            .pbaAccounts(pbaAccounts)
            .superUser(aUserCreationRequest()
                .firstName("some-fname")
                .lastName("some-lname")
                .email("someone@somewhere.com")
                .pbaAccount(superUserPaymentAccount)
                .build())
            .build();

        Response response = withAuthenticatedRequest()
            .body(organisationCreationRequest)
            .post("/organisations")
            .andReturn();

        response.then()
            .assertThat()
            .statusCode(CREATED.value());

        return response.body().as(Map.class);
    }

    private RequestSpecification withAuthenticatedRequest() {

        return RestAssured.given()
            .relaxedHTTPSValidation()
            .baseUri(professionalApiUrl)
            .header(SERVICE_HEADER, "Bearer " + s2sToken);
    }

    @SuppressWarnings("unused")
    private JsonNode parseJson(String jsonString) throws IOException {
        return mapper.readTree(jsonString);
    }
}
