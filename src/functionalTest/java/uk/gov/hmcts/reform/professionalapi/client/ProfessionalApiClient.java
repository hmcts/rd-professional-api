package uk.gov.hmcts.reform.professionalapi.client;

import static java.util.Arrays.asList;
import static org.apache.commons.lang.RandomStringUtils.randomAlphabetic;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static uk.gov.hmcts.reform.professionalapi.controller.request.ContactInformationCreationRequest.aContactInformationCreationRequest;
import static uk.gov.hmcts.reform.professionalapi.controller.request.DxAddressCreationRequest.dxAddressCreationRequest;
import static uk.gov.hmcts.reform.professionalapi.controller.request.PbaAccountCreationRequest.aPbaPaymentAccount;
import static uk.gov.hmcts.reform.professionalapi.controller.request.UserCreationRequest.aUserCreationRequest;
import static uk.gov.hmcts.reform.professionalapi.utils.OrganisationFixtures.someMinimalOrganisationRequest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import net.serenitybdd.rest.SerenityRest;
import org.springframework.http.HttpStatus;

import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.PbaAccountCreationRequest;
import uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus;

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

    private  OrganisationCreationRequest.OrganisationCreationRequestBuilder createOrganisationRequest() {
        List<PbaAccountCreationRequest> pbaAccounts = asList(aPbaPaymentAccount()
                .pbaNumber(randomAlphabetic(10))
                .build(),
            aPbaPaymentAccount()
                .pbaNumber(randomAlphabetic(10))
                .build());

        return someMinimalOrganisationRequest()
            .name(randomAlphabetic(10))
            .status(OrganisationStatus.PENDING)
            .sraId(randomAlphabetic(10) + "sra-id-number1")
            .sraRegulated(Boolean.FALSE)
            .companyUrl(randomAlphabetic(10) + "company-url")
            .companyNumber(randomAlphabetic(5) + "com")
            .pbaAccounts(pbaAccounts)
            .superUser(aUserCreationRequest()
                .firstName("some-fname")
                .lastName("some-lname")
                .email(randomAlphabetic(10) + "@somewhere.com")
                .build())
            .contactInformation(Arrays.asList(aContactInformationCreationRequest()
                .addressLine1("addressLine1")
                .addressLine2("addressLine2")
                .addressLine3("addressLine3")
                .country("some-country")
                .county("some-county")
                .townCity("some-town-city")
                .postCode("some-post-code")
                .dxAddress(Arrays.asList(dxAddressCreationRequest()
                    .dxNumber("DX 1234567890")
                    .dxExchange("dxExchange").build()))
                .build()));
    }

    public Map<String, Object> createOrganisation() {
        return createOrganisation(createOrganisationRequest().build());
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> createOrganisation(OrganisationCreationRequest organisationCreationRequest) {
        Response response = withAuthenticatedRequest()
                .body(organisationCreationRequest)
                .post("v1/organisations")
                .andReturn();

        if (response.statusCode() != CREATED.value()) {
            log.info("Create organisation response: " + response.asString());
        }

        response.then()
                .assertThat()
                .statusCode(CREATED.value());

        return response.body().as(Map.class);
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> searchForUserByEmailAddress(String email) {
        Response response = withAuthenticatedRequest()
                .param("email", email)
                .get("/v1/organisations/users/")
                .andReturn();
        response.then()
                .assertThat()
                .statusCode(OK.value());

        return response.body().as(Map.class);
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> retrieveOrganisationDetails(String id) {
        Response response = withAuthenticatedRequest()
                .body("")
                .get("v1/organisations?id=" + id)
                .andReturn();

        if (response.statusCode() != OK.value()) {
            log.info("Retrieve organisation response: " + response.asString());
        }

        response.then()
                .assertThat()
                .statusCode(OK.value());

        return response.body().as(Map.class);

    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> retrieveAllOrganisations() {
        Response response = withAuthenticatedRequest()
                .body("")
                .get("v1/organisations")
                .andReturn();

        log.info("Retrieve organisation response: " + response.asString());

        response.then()
                .assertThat()
                .statusCode(OK.value());

        return response.body().as(Map.class);
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> retrievePaymentAccountsByEmail(String email) {
        Response response = withAuthenticatedRequest()
                .body("")
                .get("v1/organisations/pbas?email=" + email)
                .andReturn();

        if (response.statusCode() != OK.value()) {
            log.info("Retrieve organisation response: " + response.asString());
        }

        response.then()
                .assertThat()
                .statusCode(OK.value());

        return response.body().as(Map.class);
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> searchUsersByOrganisation(String organisationId, String showDeleted, HttpStatus status) {
        Response response = withAuthenticatedRequest()
                .get("/v1/organisations/" + organisationId + "/users?showDeleted=" + showDeleted)
                .andReturn();
        response.then()
                    .assertThat()
                    .statusCode(status.value());
        if (HttpStatus.OK == status) {
            return response.body().as(Map.class);
        } else {
            return new HashMap<String, Object>();
        }
    }

    public void updateOrganisation(String organisationIdentifier) {

        OrganisationCreationRequest organisationCreationRequest = createOrganisationRequest().status(OrganisationStatus.ACTIVE).build();

        Response response = withAuthenticatedRequest()
            .body(organisationCreationRequest)
            .put("v1/organisations/" + organisationIdentifier)
            .andReturn();

        log.info("Update organisation response: " + response.getStatusCode());

        response.then()
            .assertThat()
            .statusCode(OK.value());
    }

    public Map<String, Object> retrieveOrganisationDetailsByStatus(String status) {

        Response response = withAuthenticatedRequest()
                .body("")
                .get("v1/organisations?status=" + status)
                .andReturn();
        log.debug("Retrieve organisation response by status: " + response.getStatusCode());
        response.then()
                .assertThat()
                .statusCode(OK.value());

        return response.body().as(Map.class);
    }

    public void retrieveOrganisationDetailsByUnknownStatus(String status) {

        Response response = withAuthenticatedRequest()
                .body("")
                .get("v1/organisations?status=" + status)
                .andReturn();

        log.debug("Retrieve organisation response for unknown status: " + response.asString());

        response.then()
                .assertThat()
                .statusCode(BAD_REQUEST.value());
    }

    private RequestSpecification withUnauthenticatedRequest() {
        return SerenityRest.given()
                .relaxedHTTPSValidation()
                .baseUri(professionalApiUrl)
                .header("Content-Type", APPLICATION_JSON_UTF8_VALUE)
                .header("Accepts", APPLICATION_JSON_UTF8_VALUE);
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
