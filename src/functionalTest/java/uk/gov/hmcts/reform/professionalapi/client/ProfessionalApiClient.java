package uk.gov.hmcts.reform.professionalapi.client;

import static java.util.Arrays.asList;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;
import static uk.gov.hmcts.reform.professionalapi.controller.request.ContactInformationCreationRequest.aContactInformationCreationRequest;
import static uk.gov.hmcts.reform.professionalapi.controller.request.PbaAccountCreationRequest.aPbaPaymentAccount;
import static uk.gov.hmcts.reform.professionalapi.controller.request.UserCreationRequest.aUserCreationRequest;
import static uk.gov.hmcts.reform.professionalapi.utils.OrganisationFixtures.someMinimalOrganisationRequest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.PbaAccountCreationRequest;

@Slf4j
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

    public String getWelcomePage() {
        return withUnauthenticatedRequest()
                .get("/")
                .then()
                .statusCode(OK.value())
                .and()
                .extract()
                .body()
                .asString();
    }

    public String getHealthPage() {
        return withUnauthenticatedRequest()
                .get("/health")
                .then()
                .statusCode(OK.value())
                .and()
                .extract()
                .body()
                .asString();
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> createOrganisation(String organisationName,
                                                  String[] pbas) {

        List<PbaAccountCreationRequest> pbaAccounts = asList(aPbaPaymentAccount()
                                                                     .pbaNumber(pbas[0])
                                                                     .build(),
                                                             aPbaPaymentAccount()
                                                                     .pbaNumber(pbas[1])
                                                                     .build());

        OrganisationCreationRequest organisationCreationRequest = someMinimalOrganisationRequest()
                        .name(organisationName)
                        .pbaAccounts(pbaAccounts)
                        .superUser(aUserCreationRequest()
                                .firstName("some-fname")
                                .lastName("some-lname")
                                .email("someone@somewhere.com")
                                .build())
                        .contactInformation(Arrays.asList(aContactInformationCreationRequest()
                                .addressLine1("addressLine1").build()))
                        .build();

        Response response = withAuthenticatedRequest()
                .body(organisationCreationRequest)
                .post("/organisations")
                .andReturn();

        if (response.statusCode() != CREATED.value()) {
            log.info("Create organisation response: " + response.asString());
        }

        response.then()
                .assertThat()
                .statusCode(CREATED.value());

        return response.body().as(Map.class);
    }

    private RequestSpecification withUnauthenticatedRequest() {
        return RestAssured.given()
                .relaxedHTTPSValidation()
                .baseUri(professionalApiUrl)
                .header("Content-Type", "application/json")
                .header("Accepts", "application/json");
    }

    private RequestSpecification withAuthenticatedRequest() {
        return withUnauthenticatedRequest()
                .header(SERVICE_HEADER, "Bearer " + s2sToken);
    }

    @SuppressWarnings("unused")
    private JsonNode parseJson(String jsonString) throws IOException {
        return mapper.readTree(jsonString);
    }
}
