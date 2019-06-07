package uk.gov.hmcts.reform.professionalapi.client;

import static org.apache.commons.lang.RandomStringUtils.randomAlphabetic;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static uk.gov.hmcts.reform.professionalapi.controller.request.ContactInformationCreationRequest.aContactInformationCreationRequest;
import static uk.gov.hmcts.reform.professionalapi.controller.request.DxAddressCreationRequest.dxAddressCreationRequest;
import static uk.gov.hmcts.reform.professionalapi.controller.request.NewUserCreationRequest.aNewUserCreationRequest;
import static uk.gov.hmcts.reform.professionalapi.controller.request.UserCreationRequest.aUserCreationRequest;
import static uk.gov.hmcts.reform.professionalapi.utils.OrganisationFixtures.someMinimalOrganisationRequest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import net.serenitybdd.rest.SerenityRest;
import org.springframework.http.HttpStatus;

import uk.gov.hmcts.reform.professionalapi.controller.request.NewUserCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest;
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
        List<String> paymentAccounts = new ArrayList<>();
        paymentAccounts.add(randomAlphabetic(8));

        return someMinimalOrganisationRequest()
            .name(randomAlphabetic(10))
            .status(OrganisationStatus.PENDING)
            .sraId(randomAlphabetic(10) + "sra-id-number1")
            .sraRegulated(Boolean.FALSE)
            .companyUrl(randomAlphabetic(10) + "company-url")
            .companyNumber(randomAlphabetic(5) + "com")
            .paymentAccount(paymentAccounts)
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
                .post("/refdata/external/v1/organisations")
                .andReturn();

        if (response.statusCode() != CREATED.value()) {
            log.info("Create organisation response: " + response.asString());
        }

        response.then()
                .assertThat()
                .statusCode(CREATED.value());

        return response.body().as(Map.class);
    }

    public  NewUserCreationRequest createNewUserCreationRequest() {
        List<String> userRoles = new ArrayList<>();
        userRoles.add("pui-user-manager");

        NewUserCreationRequest userCreationRequest = aNewUserCreationRequest()
                .firstName("someName")
                .lastName("someLastName")
                .email(randomAlphabetic(10) + "@hotmail.com")
                .status("PENDING")
                .roles(userRoles)
                .build();

        return userCreationRequest;
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> addNewUserToAnOrganisation(String orgId, NewUserCreationRequest newUserCreationRequest) {
        Response response = withAuthenticatedRequest()
                .body(newUserCreationRequest)
                .post("/refdata/external/v1/organisations/" + orgId + "/users/")
                .andReturn();
        response.then()
                .assertThat()
                .statusCode(CREATED.value());

        return response.body().as(Map.class);
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> searchForUserByEmailAddress(String email) {
        Response response = withAuthenticatedRequest()
                .param("email", email)
                .get("/refdata/external/v1/organisations/users/")
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
                .get("refdata/external/v1/organisations?id=" + id)
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
                .get("refdata/external/v1/organisations")
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
                .get("refdata/external/v1/organisations/pbas?email=" + email)
                .andReturn();

        log.info("Retrieve organisation response: " + response.asString());

        response.then()
                .assertThat()
                .statusCode(OK.value());

        return response.body().as(Map.class);
    }

    @SuppressWarnings("unchecked")
    public void retrieveBadRequestForPendingOrganisationWithPbaEmail(String email) {

        Response response = withAuthenticatedRequest()
                .body("")
                .get("refdata/external/v1/organisations/pbas?email=" + email)
                .andReturn();

        log.info("Retrieve organisation response: " + response.asString());

        response.then()
                .assertThat()
                .statusCode(NOT_FOUND.value());
    }


    @SuppressWarnings("unchecked")
    public Map<String, Object> searchUsersByOrganisation(String organisationId, String showDeleted, HttpStatus status) {
        Response response = withAuthenticatedRequest()
                .get("/refdata/external/v1/organisations/" + organisationId + "/users?showDeleted=" + showDeleted)
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
            .put("refdata/external/v1/organisations/" + organisationIdentifier)
            .andReturn();

        log.info("Update organisation response: " + response.getStatusCode());

        response.then()
            .assertThat()
            .statusCode(OK.value());
    }

    public Map<String, Object> retrieveOrganisationDetailsByStatus(String status) {

        Response response = withAuthenticatedRequest()
                .body("")
                .get("refdata/external/v1/organisations?status=" + status)
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
                .get("refdata/external/v1/organisations?status=" + status)
                .andReturn();

        log.debug("Retrieve organisation response for unknown status: " + response.asString());

        response.then()
                .assertThat()
                .statusCode(BAD_REQUEST.value());
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> retrieveLegacyPbaNumbersByUserEmail(String email) {
        Response response = withAuthenticatedRequest()
                .body("")
                .get("/search/pba/" + email)
                .andReturn();

        log.info("Retrieve organisation response: " + response.asString());

        response.then()
                .assertThat()
                .statusCode(OK.value());

        return response.body().as(Map.class);
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
                .header(SERVICE_HEADER, "Bearer " + s2sToken)
                .header("Authorization", "Bearer " + "eyJ0eXAiOiJKV1QiLCJ6aXAiOiJOT05FIiwia2lkIjoiYi9PNk92VnYxK3krV2dySDVVaTlXVGlvTHQwPSIsImFsZyI6IlJTMjU2In0.eyJzdWIiOiJzdXBlci51c2VyQGhtY3RzLm5ldCIsImF1dGhfbGV2ZWwiOjAsImF1ZGl0VHJhY2tpbmdJZCI6IjZiYTdkYTk4LTRjMGYtNDVmNy04ZjFmLWU2N2NlYjllOGI1OCIsImlzcyI6Imh0dHA6Ly9mci1hbTo4MDgwL29wZW5hbS9vYXV0aDIvaG1jdHMiLCJ0b2tlbk5hbWUiOiJhY2Nlc3NfdG9rZW4iLCJ0b2tlbl90eXBlIjoiQmVhcmVyIiwiYXV0aEdyYW50SWQiOiI0NjAzYjVhYS00Y2ZhLTRhNDQtYWQzZC02ZWI0OTI2YjgxNzYiLCJhdWQiOiJteV9yZWZlcmVuY2VfZGF0YV9jbGllbnRfaWQiLCJuYmYiOjE1NTk4OTgxNzMsImdyYW50X3R5cGUiOiJhdXRob3JpemF0aW9uX2NvZGUiLCJzY29wZSI6WyJhY3IiLCJvcGVuaWQiLCJwcm9maWxlIiwicm9sZXMiLCJjcmVhdGUtdXNlciIsImF1dGhvcml0aWVzIl0sImF1dGhfdGltZSI6MTU1OTg5ODEzNTAwMCwicmVhbG0iOiIvaG1jdHMiLCJleHAiOjE1NTk5MjY5NzMsImlhdCI6MTU1OTg5ODE3MywiZXhwaXJlc19pbiI6Mjg4MDAsImp0aSI6IjgxN2ExNjE0LTVjNzAtNGY4YS05OTI3LWVlYjFlYzJmYWU4NiJ9.RLJyLEKldHeVhQEfSXHhfOpsD_b8dEBff7h0P4nZVLVNzVkNoiPdXYJwBTSUrXl4pyYJXEhdBwkInGp3OfWQKhHcp73_uE6ZXD0eIDZRvCn1Nvi9FZRyRMFQWl1l3Dkn2LxLMq8COh1w4lFfd08aj-VdXZa5xFqQefBeiG_xXBxWkJ-nZcW3tTXU0gUzarGY0xMsFTtyRRilpcup0XwVYhs79xytfbq0WklaMJ-DBTD0gux97KiWBrM8t6_5PUfMDBiMvxKfRNtwGD8gN8Vct9JUgVTj9DAIwg0KPPm1rEETRPszYI2wWvD2lpH2AwUtLBlRDANIkN9SdfiHSETvoQ");
    }

    @SuppressWarnings("unused")
    private JsonNode parseJson(String jsonString) throws IOException {
        return mapper.readTree(jsonString);
    }
}
