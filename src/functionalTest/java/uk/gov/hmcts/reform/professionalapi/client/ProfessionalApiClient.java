package uk.gov.hmcts.reform.professionalapi.client;

import static org.apache.commons.lang.RandomStringUtils.randomAlphabetic;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.professionalapi.controller.request.ContactInformationCreationRequest.aContactInformationCreationRequest;
import static uk.gov.hmcts.reform.professionalapi.controller.request.DxAddressCreationRequest.dxAddressCreationRequest;
import static uk.gov.hmcts.reform.professionalapi.controller.request.NewUserCreationRequest.aNewUserCreationRequest;
import static uk.gov.hmcts.reform.professionalapi.controller.request.UserCreationRequest.aUserCreationRequest;
import static uk.gov.hmcts.reform.professionalapi.helper.OrganisationFixtures.someMinimalOrganisationRequest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;
import net.serenitybdd.rest.SerenityRest;
import org.apache.commons.lang.StringUtils;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.professionalapi.controller.request.ContactInformationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.DxAddressCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.NewUserCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.PbaEditRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.UserCreationRequest;
import uk.gov.hmcts.reform.professionalapi.domain.Jurisdiction;
import uk.gov.hmcts.reform.professionalapi.domain.UserProfileUpdatedData;

import uk.gov.hmcts.reform.professionalapi.idam.IdamClient;
import uk.gov.hmcts.reform.professionalapi.idam.IdamOpenIdClient;

@Slf4j
public class ProfessionalApiClient {

    private static final ObjectMapper mapper = new ObjectMapper();

    private static final String SERVICE_HEADER = "ServiceAuthorization";
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String RANDOM_EMAIL = "RANDOM_EMAIL";

    private final String professionalApiUrl;
    private final String s2sToken;


    protected IdamOpenIdClient idamOpenIdClient;
    protected IdamClient idamClient;

    public ProfessionalApiClient(
            String professionalApiUrl,
            String s2sToken, IdamOpenIdClient idamOpenIdClient, IdamClient idamClient) {
        this.professionalApiUrl = professionalApiUrl;
        this.s2sToken = s2sToken;
        this.idamOpenIdClient = idamOpenIdClient;
        this.idamClient = idamClient;
    }

    public IdamOpenIdClient getidamOpenIdClient() {
        return idamOpenIdClient;
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

    public static OrganisationCreationRequest.OrganisationCreationRequestBuilder createOrganisationRequest() {
        Set<String> paymentAccounts = new HashSet<>();
        paymentAccounts.add("PBA" + randomAlphabetic(7));
        paymentAccounts.add("PBA" + randomAlphabetic(7));
        paymentAccounts.add("PBA" + randomAlphabetic(7));

        List<DxAddressCreationRequest> dx1 = new LinkedList<>();
        dx1.add(dxAddressCreationRequest()
                .dxNumber("DX 1234567890")
                .dxExchange("dxExchange").build());
        dx1.add(dxAddressCreationRequest()
                .dxNumber("DX 123456777")
                .dxExchange("dxExchange").build());
        dx1.add(dxAddressCreationRequest()
                .dxNumber("DX 123456788")
                .dxExchange("dxExchange").build());
        List<DxAddressCreationRequest> dx2 = new LinkedList<>();
        dx2.add(dxAddressCreationRequest()
                .dxNumber("DX 123452222")
                .dxExchange("dxExchange").build());
        dx2.add(dxAddressCreationRequest()
                .dxNumber("DX 123456333")
                .dxExchange("dxExchange").build());
        List<ContactInformationCreationRequest> contactInfoList = new LinkedList<>();
        contactInfoList.add(aContactInformationCreationRequest()
                .addressLine1("addressLine1")
                .addressLine2("addressLine2")
                .addressLine3("addressLine3")
                .country("some-country")
                .county("some-county")
                .townCity("some-town-city")
                .postCode("some-post-code")
                .dxAddress(dx1)
                .build());
        contactInfoList.add(aContactInformationCreationRequest()
                .addressLine1("addLine1")
                .addressLine2("addLine2")
                .addressLine3("addLine3")
                .country("some-country")
                .county("some-county")
                .townCity("some-town-city")
                .postCode("some-post-code")
                .dxAddress(dx2)
                .build());

        return someMinimalOrganisationRequest()
                .name(randomAlphabetic(10))
                .status("PENDING")
                .sraId(randomAlphabetic(10) + "sra-id-number1")
                .sraRegulated("false")
                .companyUrl(randomAlphabetic(10) + "company-url")
                .companyNumber(randomAlphabetic(5) + "com")
                .paymentAccount(paymentAccounts)
                .superUser(aUserCreationRequest()
                        .firstName("some-fname")
                        .lastName("some-lname")
                        .email(randomAlphabetic(10) + "@somewhere.com".toLowerCase())
                        .jurisdictions(createJurisdictions())
                        .build())
                .contactInformation(contactInfoList);
    }

    public static List<Jurisdiction> createJurisdictions() {

        List<Jurisdiction> jurisdictions = new ArrayList<Jurisdiction>();
        Jurisdiction jurisdiction1 = new Jurisdiction();
        jurisdiction1.setId("PROBATE");
        Jurisdiction jurisdiction2 = new Jurisdiction();
        jurisdiction2.setId("SSCS");
        jurisdictions.add(jurisdiction1);
        jurisdictions.add(jurisdiction2);
        return jurisdictions;
    }

    public Map<String, Object> createOrganisation() {
        return createOrganisation(createOrganisationRequest().build());
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> createOrganisation(OrganisationCreationRequest organisationCreationRequest) {
        Response response = getS2sTokenHeaders()
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

    public Map<String, Object> createOrganisationWithoutS2SToken(OrganisationCreationRequest
                                                                         organisationCreationRequest) {
        Response response = withUnauthenticatedRequest()
                .body(organisationCreationRequest)
                .post("/refdata/external/v1/organisations")
                .andReturn();

        response.then()
                .assertThat()
                .statusCode(UNAUTHORIZED.value());

        return response.body().as(Map.class);
    }

    public Map<String, Object> createOrganisationWithUnknownJurisdictionId() {
        OrganisationCreationRequest organisationCreationRequest = createOrganisationRequest().build();
        organisationCreationRequest.getSuperUser().setJurisdictions(createUnknownJurisdiction());
        return createOrganisation(organisationCreationRequest);
    }

    public List<Jurisdiction> createUnknownJurisdiction() {
        List<Jurisdiction> jurisdictionIds = new ArrayList<>();
        Jurisdiction jurisdiction = new Jurisdiction();
        jurisdiction.setId("UNKNOWN");
        jurisdictionIds.add(jurisdiction);
        return jurisdictionIds;
    }

    public Map<String, Object> createOrganisationWithNoJurisdictionId() {
        OrganisationCreationRequest organisationCreationRequest = createOrganisationRequest().build();
        organisationCreationRequest.getSuperUser().setJurisdictions(new ArrayList<>());
        return createOrganisation(organisationCreationRequest);
    }

    public void
        receiveBadResponseForCreateOrganisationWithInvalidDxAddressFields(OrganisationCreationRequest
                                                                                  organisationCreationRequest) {
        Response response = getS2sTokenHeaders()
                .body(organisationCreationRequest)
                .post("/refdata/external/v1/organisations")
                .andReturn();

        log.info("Create organisation response: " + response.asString());

        response.then()
                .assertThat()
                .statusCode(BAD_REQUEST.value());
    }

    public NewUserCreationRequest createNewUserRequest() {
        List<String> userRoles = new ArrayList<>();
        userRoles.add("pui-user-manager");

        NewUserCreationRequest userCreationRequest = aNewUserCreationRequest()
                .firstName("someName")
                .lastName("someLastName")
                .email(randomAlphabetic(10) + "@hotmail.com".toLowerCase())
                .roles(userRoles)
                .jurisdictions(createJurisdictions())
                .build();

        return userCreationRequest;
    }

    public NewUserCreationRequest createNewUserRequest(String email) {
        List<String> userRoles = new ArrayList<>();
        userRoles.add("caseworker");

        NewUserCreationRequest userCreationRequest = aNewUserCreationRequest()
                .firstName("someFirstName")
                .lastName("someLastName")
                .email(email)
                .roles(userRoles)
                .jurisdictions(createJurisdictions())
                .build();

        return userCreationRequest;
    }

    public NewUserCreationRequest createReInviteUserRequest(String email) {
        List<String> userRoles = new ArrayList<>();
        userRoles.add("pui-user-manager");

        NewUserCreationRequest userCreationRequest = aNewUserCreationRequest()
                .firstName("someName")
                .lastName("someLastName")
                .email(email.equalsIgnoreCase(RANDOM_EMAIL) ? randomAlphabetic(10)
                        + "@hotmail.com".toLowerCase() : email)
                .roles(userRoles)
                .jurisdictions(createJurisdictions())
                .resendInvite(true)
                .build();

        return userCreationRequest;
    }

    public Map<String, Object> addNewUserToAnOrganisation(String orgId, String role,
                                                          NewUserCreationRequest newUserCreationRequest,
                                                          HttpStatus expectedStatus) {
        Response response = getMultipleAuthHeadersInternal()
                .body(newUserCreationRequest)
                .post("/refdata/internal/v1/organisations/" + orgId + "/users/")
                .andReturn();
        response.then()
                .assertThat()
                .statusCode(expectedStatus.value());

        return response.body().as(Map.class);
    }

    public Map<String, Object> addNewUserToAnOrganisationExternal(NewUserCreationRequest newUserCreationRequest,
                                                                  RequestSpecification requestSpecification,
                                                                  HttpStatus expectedStatus) {

        Response response = requestSpecification
                .body(newUserCreationRequest)
                .post("/refdata/external/v1/organisations/users/")
                .andReturn();
        response.then()
                .assertThat()
                .statusCode(expectedStatus.value());

        return response.body().as(Map.class);
    }


    @SuppressWarnings("unchecked")
    public Map<String, Object> searchForUserByEmailAddress(String email, String role) {
        Response response = getMultipleAuthHeadersInternal()
                .param("email", email)
                .get("/refdata/internal/v1/organisations/user")
                .andReturn();
        log.info("Search For User By Email Response: " + response.asString());
        response.then()
                .assertThat()
                .statusCode(OK.value());

        return response.body().as(Map.class);
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> retrieveOrganisationDetails(String id, String role, HttpStatus status) {
        Response response = getMultipleAuthHeadersInternal()
                .body("")
                .get("/refdata/internal/v1/organisations?id=" + id)
                .andReturn();

        if (response.statusCode() != OK.value()) {
            log.info("Retrieve organisation response: " + response.asString());
        }

        response.then()
                .assertThat()
                .statusCode(status.value());

        if (HttpStatus.OK == status) {
            return response.as(Map.class);
        } else {
            return new HashMap<>();
        }

    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> retrieveAllOrganisations(String role) {
        Response response = getMultipleAuthHeadersInternal()
                .body("")
                .get("/refdata/internal/v1/organisations")
                .andReturn();


        response.then()
                .assertThat()
                .statusCode(OK.value());

        return response.body().as(Map.class);
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> retrievePaymentAccountsByEmail(String email, String role) {
        Response response = getMultipleAuthHeadersInternal()
                .body("")
                .get("/refdata/internal/v1/organisations/pbas?email=" + email)
                .andReturn();

        log.info("Retrieve organisation response: " + response.asString());

        response.then()
                .assertThat()
                .statusCode(OK.value());

        return response.body().as(Map.class);
    }

    @SuppressWarnings("unchecked")
    public void retrieveBadRequestForPendingOrganisationWithPbaEmail(String email, String role) {

        Response response = getMultipleAuthHeadersInternal()
                .body("")
                .get("/refdata/internal/v1/organisations/pbas?email=" + email)
                .andReturn();

        log.info("Retrieve organisation response: " + response.asString());

        response.then()
                .assertThat()
                .statusCode(NOT_FOUND.value());
    }


    @SuppressWarnings("unchecked")
    public Map<String, Object> searchUsersByOrganisation(String organisationId, String role, String showDeleted,
                                                         HttpStatus status, String returnRoles) {

        // Use the role supplied to generate tokens appropritely
        RequestSpecification requestSpecification;
        if (!StringUtils.isBlank(role)) {
            requestSpecification = getMultipleAuthHeadersWithGivenRole(role);
        } else {
            requestSpecification = getMultipleAuthHeadersInternal();
        }
        Response response = requestSpecification
                .get("/refdata/internal/v1/organisations/" + organisationId + "/users?showDeleted="
                        + showDeleted + "&returnRoles=" + returnRoles)
                .andReturn();
        response.then()
                .assertThat()
                .statusCode(status.value());
        return response.body().as(Map.class);
    }

    public Map<String, Object> searchUsersByOrganisationWithPagination(String organisationId, String role,
                                                                       String showDeleted, HttpStatus status,
                                                                       String pageNumber, String size) {

        Response response = getMultipleAuthHeadersInternal()
                .get("/refdata/internal/v1/organisations/" + organisationId + "/users?showDeleted="
                        + showDeleted + "&page=" + pageNumber + "&size=" + size)
                .andReturn();
        response.then()
                .assertThat()
                .statusCode(status.value());
        assertThat(response.headers().hasHeaderWithName("Paginationinfo")).isTrue();
        if (HttpStatus.OK == status) {
            return response.as(Map.class);
        } else {
            return new HashMap<>();
        }
    }

    public Map<String, Object>
        searchAllActiveUsersByOrganisationExternalWithPagination(HttpStatus status,
                                                                 RequestSpecification requestSpecification,
                                                                 String userStatus, String pageNumber, String size) {

        Response response = requestSpecification
                .get("/refdata/external/v1/organisations/users?page=" + pageNumber + "&size=" + size)
                .andReturn();

        assertThat(response.headers().hasHeaderWithName("Paginationinfo")).isTrue();

        response.then()
                .assertThat()
                .statusCode(status.value());

        return response.body().as(Map.class);
    }

    public Map<String, Object> searchOrganisationUsersByStatusInternal(String organisationId, String role,
                                                                       HttpStatus status) {

        Response response = getMultipleAuthHeadersInternal()
                .get("/refdata/internal/v1/organisations/" + organisationId + "/users")
                .andReturn();
        response.then()
                .assertThat()
                .statusCode(status.value());
        return response.body().as(Map.class);
    }

    public Map<String, Object> searchOrganisationUsersByStatusExternal(HttpStatus status,
                                                                       RequestSpecification requestSpecification,
                                                                       String userStatus) {

        Response response = requestSpecification
                .get("/refdata/external/v1/organisations/users?status=" + userStatus)
                .andReturn();

        response.then()
                .assertThat()
                .statusCode(status.value());
        if (HttpStatus.OK == status) {
            return response.as(Map.class);
        } else {
            return new HashMap<>();
        }

    }

    public Map<String, Object> retrieveOrganisationByOrgIdExternal(HttpStatus status,
                                                                   RequestSpecification requestSpecification) {

        Response response = requestSpecification
                .get("/refdata/external/v1/organisations")
                .andReturn();

        response.then()
                .assertThat()
                .statusCode(status.value());
        return response.body().as(Map.class);
    }

    public Map<String, Object>
        searchOrganisationUsersByReturnRolesParamExternal(HttpStatus status,
                                                          RequestSpecification requestSpecification,
                                                          String returnRoles) {

        Response response = requestSpecification
                .get("/refdata/external/v1/organisations/users?returnRoles=" + returnRoles)
                .andReturn();

        response.then()
                .assertThat()
                .statusCode(status.value());
        if (HttpStatus.OK == status) {
            return response.as(Map.class);
        } else {
            return new HashMap<>();
        }

    }


    public void updateOrganisation(String organisationIdentifier, String role) {

        OrganisationCreationRequest organisationCreationRequest = createOrganisationRequest().status("ACTIVE").build();

        updateOrganisation(organisationCreationRequest, role, organisationIdentifier);
    }

    public void updateOrganisation(String organisationIdentifier, String role, HttpStatus expectedStatus) {

        OrganisationCreationRequest organisationCreationRequest = createOrganisationRequest().status("ACTIVE").build();

        updateOrganisation(organisationCreationRequest, role, organisationIdentifier, expectedStatus);
    }

    //with OPENID implementation
    public void updateOrganisation(OrganisationCreationRequest organisationCreationRequest, String role,
                                   String organisationIdentifier) {

        updateOrganisation(organisationCreationRequest, role, organisationIdentifier, OK);
    }


    public void updateOrganisation(OrganisationCreationRequest organisationCreationRequest, String role,
                                   String organisationIdentifier, HttpStatus expectedStatus) {

        Response response = getMultipleAuthHeadersInternal()
                .body(organisationCreationRequest)
                .put("/refdata/internal/v1/organisations/" + organisationIdentifier)
                .andReturn();

        log.info("Update organisation response: " + response.getStatusCode());

        response.then()
                .assertThat()
                .statusCode(expectedStatus.value());
    }

    public void updateOrganisationWithoutBearerToken(String role, String organisationIdentifier,
                                                     HttpStatus expectedStatus) {
        OrganisationCreationRequest organisationCreationRequest = createOrganisationRequest().status("ACTIVE").build();
        Response response = getMultipleAuthHeadersWithEmptyBearerToken("")
                .body(organisationCreationRequest)
                .put("/refdata/internal/v1/organisations/" + organisationIdentifier)
                .andReturn();

        log.info("Update organisation response: " + response.getStatusCode());

        response.then()
                .assertThat()
                .statusCode(expectedStatus.value());
    }

    public void updateUser(UserCreationRequest userCreationRequest, String role, String userIdentifier) {

        Response response = getMultipleAuthHeadersInternal()
                .body(userCreationRequest)
                .put("/refdata/internal/v1/users/" + userIdentifier)
                .andReturn();

        log.info("Update user response: " + response.getStatusCode());

        response.then()
                .assertThat()
                .statusCode(OK.value());
    }

    //with Bearer token
    public void updateOrganisationWithOldBearerToken(String organisationIdentifier) {

        OrganisationCreationRequest organisationCreationRequest = createOrganisationRequest().status("ACTIVE").build();

        Response response = getMultipleAuthHeadersInternalWithOldBearerToken()
                .body(organisationCreationRequest)
                .put("/refdata/internal/v1/organisations/" + organisationIdentifier)
                .andReturn();

        log.info("Update organisation response: " + response.getStatusCode());

        response.then()
                .assertThat()
                .statusCode(OK.value());
    }

    public Map<String, Object> retrieveOrganisationDetailsByStatus(String status, String role) {

        Response response = getMultipleAuthHeadersInternal()
                .body("")
                .get("/refdata/internal/v1/organisations?status=" + status)
                .andReturn();
        log.debug("Retrieve organisation response by status: " + response.getStatusCode());
        response.then()
                .assertThat()
                .statusCode(OK.value());

        return response.body().as(Map.class);
    }

    public void retrieveOrganisationDetailsByUnknownStatus(String status, String role) {

        Response response = getMultipleAuthHeadersInternal()
                .body("")
                .get("/refdata/internal/v1/organisations?status=" + status)
                .andReturn();

        log.debug("Retrieve organisation response for unknown status: " + response.asString());

        response.then()
                .assertThat()
                .statusCode(BAD_REQUEST.value());
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> retrieveLegacyPbaNumbersByUserEmail(String email) {
        Response response = withUnauthenticatedRequest()
                .body("")
                .get("/search/pba/" + email)
                .andReturn();

        log.info("Retrieve organisation response: " + response.asString());

        response.then()
                .assertThat()
                .statusCode(OK.value());

        return response.body().as(Map.class);
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> modifyUserToExistingUserForPrdAdmin(HttpStatus status,
                                                                   UserProfileUpdatedData userProfileUpdatedData,
                                                                   String organisationId, String userId) {

        Response response = getMultipleAuthHeadersInternal()
                .body(userProfileUpdatedData)
                .put("/refdata/internal/v1/organisations/" + organisationId + "/users/" + userId)
                .andReturn();

        response.then()
                .assertThat()
                .statusCode(status.value());

        return response.body().as(Map.class);

    }

    public Map<String, Object> editPbaAccountsByOrgId(PbaEditRequest pbaEditRequest, String orgId, String hmctsAdmin) {

        Response response = getMultipleAuthHeadersInternal()
                .body(pbaEditRequest)
                .put("/refdata/internal/v1/organisations/" + orgId + "/pbas")
                .andReturn();

        log.info("Retrieve edit pba response: " + response.asString());

        response.then()
                .assertThat()
                .statusCode(OK.value());

        return response.body().as(Map.class);
    }

    public Map<String, Object> modifyUserToExistingUserForExternal(HttpStatus status,
                                                                   UserProfileUpdatedData userProfileUpdatedData,
                                                                   RequestSpecification requestSpecification,
                                                                   String userId) {

        Response response = requestSpecification
                .body(userProfileUpdatedData)
                .put("/refdata/external/v1/organisations/users/" + userId + "?origin=EXUI")
                .andReturn();

        response.then()
                .assertThat()
                .statusCode(status.value());

        return response.body().as(Map.class);

    }

    public Map<String, Object> findUserStatusByEmail(HttpStatus status, RequestSpecification requestSpecification,
                                                     String email) {

        Response response = requestSpecification
                .get("/refdata/external/v1/organisations/users/accountId?email=" + email)
                .andReturn();

        response.then()
                .assertThat()
                .statusCode(status.value());
        return response.body().as(Map.class);
    }

    public Map<String, Object> retrievePbaAccountsForAnOrganisationExternal(HttpStatus status,
            RequestSpecification requestSpecification) {

        Response response = requestSpecification
                .get("/refdata/external/v1/organisations")
                .andReturn();

        response.then()
                .assertThat()
                .statusCode(status.value());
        return response.body().as(Map.class);
    }

    public void deleteOrganisation(String organisationId, String role, HttpStatus status) {
        Response response = getMultipleAuthHeadersInternal()
                .delete("/refdata/internal/v1/organisations/" + organisationId)
                .andReturn();

        if (response.statusCode() != NO_CONTENT.value()) {
            log.info("Delete organisation response: " + response.asString());
        }
        response.then()
                .assertThat()
                .statusCode(status.value());
    }

    public void deleteOrganisationByExternalUser(String organisationId, HttpStatus status) {
        Response response = getMultipleAuthHeadersInternal()
                .body("")
                .delete("/refdata/external/v1/organisations/" + organisationId)
                .andReturn();

        if (response.statusCode() != NO_CONTENT.value()) {
            log.info("Delete organisation response: " + response.asString());
        }

        response.then()
                .assertThat()
                .statusCode(status.value());


    }

    public void deleteOrganisationByExternalUsersBearerToken(String organisationId,
                                   RequestSpecification requestSpecification, HttpStatus status) {
        Response response = requestSpecification
                .body("")
                .delete("/refdata/internal/v1/organisations/" + organisationId)
                .andReturn();

        if (response.statusCode() != NO_CONTENT.value()) {
            log.info("Delete organisation response: " + response.asString());
        }

        response.then()
                .assertThat()
                .statusCode(status.value());
    }

    private RequestSpecification withUnauthenticatedRequest() {
        return SerenityRest.given()
                .relaxedHTTPSValidation()
                .baseUri(professionalApiUrl)
                .header("Content-Type", APPLICATION_JSON_VALUE)
                .header("Accepts", APPLICATION_JSON_VALUE);
    }

    private RequestSpecification getS2sTokenHeaders() {
        return withUnauthenticatedRequest()
                .header(SERVICE_HEADER, "Bearer " + s2sToken);
    }

    private RequestSpecification getMultipleAuthHeadersInternal() {
        return getMultipleAuthHeaders(idamOpenIdClient.getInternalOpenIdToken());
    }

    private RequestSpecification getMultipleAuthHeadersWithGivenRole(String role) {
        return getMultipleAuthHeaders(idamOpenIdClient.getOpenIdTokenWithGivenRole(role));
    }

    private RequestSpecification getMultipleAuthHeadersInternalWithOldBearerToken() {
        return getMultipleAuthHeaders(idamClient.getInternalBearerToken());
    }

    public RequestSpecification getMultipleAuthHeadersExternal(String role, String firstName, String lastName,
                                                               String email) {
        String bearerTokenForSuperUser = idamOpenIdClient.getExternalOpenIdToken(role, firstName, lastName, email);
        return getMultipleAuthHeaders(bearerTokenForSuperUser);
    }

    public RequestSpecification getMultipleAuthHeaders(String userToken) {
        return SerenityRest.with()
                .relaxedHTTPSValidation()
                .baseUri(professionalApiUrl)
                .header("Content-Type", APPLICATION_JSON_VALUE)
                .header("Accepts", APPLICATION_JSON_VALUE)
                .header(SERVICE_HEADER, "Bearer " + s2sToken)
                .header(AUTHORIZATION_HEADER, "Bearer " + userToken);
    }

    public RequestSpecification getMultipleAuthHeadersWithEmptyBearerToken(String userToken) {
        return SerenityRest.with()
                .relaxedHTTPSValidation()
                .baseUri(professionalApiUrl)
                .header("Content-Type", APPLICATION_JSON_VALUE)
                .header("Accepts", APPLICATION_JSON_VALUE)
                .header(SERVICE_HEADER, "Bearer " + s2sToken)
                .header(AUTHORIZATION_HEADER, "Bearer ");
    }

    @SuppressWarnings("unused")
    private JsonNode parseJson(String jsonString) throws IOException {
        return mapper.readTree(jsonString);
    }

    public static <T> T getNestedValue(Map map, String... keys) {
        Object value = map;

        for (String key : keys) {
            value = ((Map) value).get(key);
        }

        return (T) value;
    }
}