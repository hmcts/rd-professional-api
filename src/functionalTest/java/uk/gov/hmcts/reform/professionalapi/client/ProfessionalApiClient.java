package uk.gov.hmcts.reform.professionalapi.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.response.Response;
import io.restassured.response.ResponseBody;
import io.restassured.specification.RequestSpecification;
import lombok.extern.slf4j.Slf4j;
import net.serenitybdd.rest.SerenityRest;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.professionalapi.controller.advice.ErrorResponse;
import uk.gov.hmcts.reform.professionalapi.controller.request.ContactInformationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.DeleteMultipleAddressRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.DxAddressCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.MfaUpdateRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.NewUserCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrgAttributeRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationOtherOrgsCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.PbaRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.UpdatePbaRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.UserCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.response.OrganisationMinimalInfoResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.OrganisationsWithPbaStatusResponse;
import uk.gov.hmcts.reform.professionalapi.domain.PbaStatus;
import uk.gov.hmcts.reform.professionalapi.domain.UserProfileUpdatedData;
import uk.gov.hmcts.reform.professionalapi.idam.IdamOpenIdClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.apache.commons.lang.RandomStringUtils.randomAlphabetic;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.professionalapi.AuthorizationFunctionalTest.generateRandomEmail;
import static uk.gov.hmcts.reform.professionalapi.controller.request.ContactInformationCreationRequest.aContactInformationCreationRequest;
import static uk.gov.hmcts.reform.professionalapi.controller.request.DxAddressCreationRequest.dxAddressCreationRequest;
import static uk.gov.hmcts.reform.professionalapi.controller.request.NewUserCreationRequest.aNewUserCreationRequest;
import static uk.gov.hmcts.reform.professionalapi.controller.request.UserCreationRequest.aUserCreationRequest;
import static uk.gov.hmcts.reform.professionalapi.helper.OrganisationFixtures.someMinimalOrganisationRequest;

@Slf4j
@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
public class ProfessionalApiClient {

    @Value("${loggingComponentName}")
    private String loggingComponentName;

    private static final ObjectMapper mapper = new ObjectMapper();

    private static final String SERVICE_HEADER = "ServiceAuthorization";
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String USER_EMAIL_HEADER = "UserEmail";
    private static final String RANDOM_EMAIL = "RANDOM_EMAIL";

    private final String professionalApiUrl;
    private final String s2sToken;


    protected static IdamOpenIdClient idamOpenIdClient;

    public ProfessionalApiClient(
        String professionalApiUrl,
        String s2sToken, IdamOpenIdClient idamOpenIdClient) {
        this.professionalApiUrl = professionalApiUrl;
        this.s2sToken = s2sToken;
        ProfessionalApiClient.idamOpenIdClient = idamOpenIdClient;
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
            .response()
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
            .response()
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
            .uprn("uprn1")
            .dxAddress(dx1)
            .build());
        contactInfoList.add(aContactInformationCreationRequest()
            .addressLine1("addLine1")
            .addressLine2("addLine2")
            .addressLine3("addLine3")
            .uprn("uprn")
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
                .email(generateRandomEmail().toLowerCase())
                .build())
            .contactInformation(contactInfoList);
    }

    public static OrganisationOtherOrgsCreationRequest createOrganisationRequestForV2() {
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
                .uprn("uprn1")
                .dxAddress(dx1)
                .build());
        contactInfoList.add(aContactInformationCreationRequest()
                .addressLine1("addLine1")
                .addressLine2("addLine2")
                .addressLine3("addLine3")
                .uprn("uprn")
                .country("some-country")
                .county("some-county")
                .townCity("some-town-city")
                .postCode("some-post-code")
                .dxAddress(dx2)
                .build());

        List<OrgAttributeRequest> orgAttributeRequests = new ArrayList<>();

        OrgAttributeRequest orgAttributeRequest = new OrgAttributeRequest();

        orgAttributeRequest.setKey("testKey");
        orgAttributeRequest.setValue("testValue");

        orgAttributeRequests.add(orgAttributeRequest);
        String superUserEmail = generateRandomEmail();

        OrganisationOtherOrgsCreationRequest organisationOtherOrgsCreationRequest =
                new OrganisationOtherOrgsCreationRequest(randomAlphabetic(10),
                        "PENDING",
                        "test",
                        randomAlphabetic(10) + "sra-id-number1",
                        "false",
                        randomAlphabetic(5) + "com",
                        randomAlphabetic(10) + "company-url",
                        aUserCreationRequest()
                                .firstName("some-fname")
                                .lastName("some-lname")
                                .email(superUserEmail.toLowerCase())
                                .build(),
                        paymentAccounts,
                        contactInfoList,
                        "Doctor",
                        orgAttributeRequests);

        return  organisationOtherOrgsCreationRequest;
    }

    public static List<ContactInformationCreationRequest> createContactInformationCreationRequests() {
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
                .uprn("uprn1")
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
                .uprn("uprn2")
                .addressLine1("addLine1")
                .addressLine2("addLine2")
                .addressLine3("addLine3")
                .country("some-country")
                .county("some-county")
                .townCity("some-town-city")
                .postCode("some-post-code")
                .dxAddress(dx2)
                .build());

        return contactInfoList;
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
            log.info("{}:: Create organisation response: {}", loggingComponentName, response.asString());
        }

        response.then()
            .assertThat()
            .statusCode(CREATED.value());

        return response.body().as(Map.class);
    }

    public Map<String, Object> createOrganisationV2() {
        return createOrganisationV2(createOrganisationRequestForV2());
    }

    public Map<String, Object> createOrganisationV2(OrganisationOtherOrgsCreationRequest organisationCreationRequest) {
        Response response = getS2sTokenHeaders()
                .body(organisationCreationRequest)
                .post("/refdata/external/v2/organisations")
                .andReturn();

        if (response.statusCode() != CREATED.value()) {
            log.info("{}:: Create organisation response: {}", loggingComponentName, response.asString());
        }

        response.then()
                .assertThat()
                .statusCode(CREATED.value());

        return response.body().as(Map.class);
    }



    @SuppressWarnings("unchecked")
    public Map<String, Object> addContactInformationsToOrganisation(

            List<ContactInformationCreationRequest>
                    createContactInformationCreationRequests,
            String pomBearerToken,
            HttpStatus httpStatus) {
        Response response = getMultipleAuthHeaders(pomBearerToken)
                .body(createContactInformationCreationRequests)
                .post("/refdata/external/v1/organisations/addresses")
                .andReturn();

        if (response.statusCode() != CREATED.value()) {
            log.info("{}:: Add contact informations to organisation response: {}",
                    loggingComponentName, response.asString());
        }
        Map<String, Object> hmResponse = new HashMap<>();

        if (FORBIDDEN.value() == httpStatus.value()) {

            hmResponse.put("statusCode", FORBIDDEN.value());

        } else if (CREATED.value() == httpStatus.value()) {
            response.then()
                    .assertThat()
                    .statusCode(CREATED.value());
            hmResponse.put("statusCode", CREATED.value());
        }


        return hmResponse;
    }

    public Response createOrganisationWithoutS2SToken(OrganisationCreationRequest
                                                                     organisationCreationRequest) {
        return withUnauthenticatedRequest()
            .body(organisationCreationRequest)
            .post("/refdata/external/v1/organisations")
            .andReturn();
    }

    public Response createOrganisationWithoutS2STokenV2(OrganisationOtherOrgsCreationRequest
                                                              organisationCreationRequest) {
        return withUnauthenticatedRequest()
                .body(organisationCreationRequest)
                .post("/refdata/external/v2/organisations")
                .andReturn();
    }

    public NewUserCreationRequest createNewUserRequest() {
        List<String> userRoles = new ArrayList<>();
        userRoles.add("pui-user-manager");

        NewUserCreationRequest userCreationRequest = aNewUserCreationRequest()
            .firstName("someName")
            .lastName("someLastName")
            .email(generateRandomEmail())
            .roles(userRoles)
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
            .build();

        return userCreationRequest;
    }

    public NewUserCreationRequest createReInviteUserRequest(String email) {
        List<String> userRoles = new ArrayList<>();
        userRoles.add("pui-user-manager");

        NewUserCreationRequest userCreationRequest = aNewUserCreationRequest()
            .firstName("someName")
            .lastName("someLastName")
            .email(email.equalsIgnoreCase(RANDOM_EMAIL) ? generateRandomEmail() : email)
            .roles(userRoles)
            .resendInvite(true)
            .build();

        return userCreationRequest;
    }

    public static <T> T getNestedValue(Map map, String... keys) {
        Object value = map;

        for (String key : keys) {
            value = ((Map) value).get(key);
        }

        return (T) value;
    }

    public Map<String, Object> addNewUserToAnOrganisation(
        String orgId, String role, NewUserCreationRequest newUserCreationRequest, HttpStatus expectedStatus) {
        Response response = getMultipleAuthHeadersInternal()
            .body(newUserCreationRequest)
            .post("/refdata/internal/v1/organisations/" + orgId + "/users/")
            .andReturn();
        response.then()
            .assertThat()
            .statusCode(expectedStatus.value());
        log.info("{}:: Add new user (Internal) response: {}", loggingComponentName, response.statusCode());
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

        log.info("{}:: Add new user (Internal) response: {}", loggingComponentName, response.statusCode());
        return response.body().as(Map.class);
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> retrieveOrganisationDetails(String id, String role, HttpStatus status) {
        Response response = getMultipleAuthHeadersInternal()
            .body("")
            .get("/refdata/internal/v1/organisations?id=" + id)
            .andReturn();

        if (response.statusCode() != OK.value()) {
            log.info("{}:: Retrieve organisation response: {}", loggingComponentName, response.statusCode());
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

    public Map<String, Object> retrieveOrganisationDetailsForV2(String id, String role, HttpStatus status) {
        Response response = getMultipleAuthHeadersInternal()
                .body("")
                .get("/refdata/internal/v2/organisations?id=" + id)
                .andReturn();

        if (response.statusCode() != OK.value()) {
            log.info("{}:: Retrieve organisation response: {}", loggingComponentName, response.statusCode());
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

        log.info("{}:: Retrieve all orgs (Internal) response: {}", loggingComponentName, response.statusCode());
        return response.body().as(Map.class);
    }

    public Map<String, Object> retrieveAllOrganisationsV2(String role) {
        Response response = getMultipleAuthHeadersInternal()
                .body("")
                .get("/refdata/internal/v2/organisations")
                .andReturn();


        response.then()
                .assertThat()
                .statusCode(OK.value());

        log.info("{}:: Retrieve all orgs (Internal) response: {}", loggingComponentName, response.statusCode());
        return response.body().as(Map.class);
    }

    public Map<String, Object> retrieveAllOrganisationsWithPagination(String role, String page, String size) {
        Response response = getMultipleAuthHeadersInternal()
            .body("")
            .get("/refdata/internal/v1/organisations?" + "page=" + page + "&size=" + size)
            .andReturn();


        response.then()
            .assertThat()
            .statusCode(OK.value());

        log.info("{}:: Retrieve all orgs with pagination (Internal) response: {}",
            loggingComponentName, response.statusCode());
        return response.body().as(Map.class);
    }

    public Map<String, Object> retrieveAllOrganisationsWithPaginationV2(String role, String page, String size) {
        Response response = getMultipleAuthHeadersInternal()
                .body("")
                .get("/refdata/internal/v2/organisations?" + "page=" + page + "&size=" + size)
                .andReturn();


        response.then()
                .assertThat()
                .statusCode(OK.value());

        log.info("{}:: Retrieve all orgs with pagination (Internal) response: {}",
                loggingComponentName, response.statusCode());
        return response.body().as(Map.class);
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> retrievePaymentAccountsByEmail(String email, String role) {
        Response response = getUserEmailAsHeaderWithExisting(idamOpenIdClient
            .getcwdAdminOpenIdToken("prd-admin"), email)
            .body("")
            .get("/refdata/internal/v1/organisations/pbas")
            .andReturn();

        log.info("{}:: Retrieve organisation (Internal) response: {}", loggingComponentName, response.statusCode());

        response.then()
            .assertThat()
            .statusCode(OK.value());

        return response.body().as(Map.class);
    }

    public Map<String, Object> retrievePaymentAccountsByEmailV2(String email, String role) {
        Response response = getUserEmailAsHeaderWithExisting(idamOpenIdClient
                .getcwdAdminOpenIdToken("prd-admin"), email)
                .body("")
                .get("/refdata/internal/v2/organisations/pbas")
                .andReturn();

        log.info("{}:: Retrieve organisation (Internal) response: {}", loggingComponentName, response.statusCode());

        response.then()
                .assertThat()
                .statusCode(OK.value());

        return response.body().as(Map.class);
    }

    @SuppressWarnings("unchecked")
    public void retrievePaymentAccountsWithoutEmailForInternal() {
        Response response = getMultipleAuthHeadersInternal()
                .get("/refdata/internal/v1/organisations/pbas")
                .andReturn();

        log.info("{}:: Retrieve organisation (Internal) response: {}", loggingComponentName, response.statusCode());

        response.then()
                .assertThat()
                .statusCode(BAD_REQUEST.value())
                .body("errorDescription", equalTo("No User Email provided via header"));

    }

    public void retrievePaymentAccountsWithoutEmailForInternalV2() {
        Response response = getMultipleAuthHeadersInternal()
                .get("/refdata/internal/v2/organisations/pbas")
                .andReturn();

        log.info("{}:: Retrieve organisation (Internal) response: {}", loggingComponentName, response.statusCode());



        response.then()
                .assertThat()
                .statusCode(BAD_REQUEST.value())
                .body("errorDescription", equalTo("No User Email provided via header"));
    }

    @SuppressWarnings("unchecked")
    public void retrievePaymentAccountsWithoutEmailForExternal(RequestSpecification requestSpecification) {
        Response response = requestSpecification
                .get("refdata/external/v1/organisations/pbas")
                .andReturn();

        log.info("{}:: Retrieve organisation (External) response: {}", loggingComponentName, response.statusCode());

        response.then()
                .assertThat()
                .statusCode(BAD_REQUEST.value())
                .body("errorDescription", equalTo("No User Email provided via header"));
    }

    @SuppressWarnings("unchecked")
    public void retrievePaymentAccountsWithoutEmailForExternalV2(RequestSpecification requestSpecification) {
        Response response = requestSpecification
                .get("refdata/external/v2/organisations/pbas")
                .andReturn();

        log.info("{}:: Retrieve organisation (External) response: {}", loggingComponentName, response.statusCode());

        response.then()
                .assertThat()
                .statusCode(BAD_REQUEST.value())
                .body("errorDescription", equalTo("No User Email provided via header"));
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> retrievePaymentAccountsByEmailFromHeader(String email, String role) {
        Response response = getUserEmailAsHeaderWithExisting(idamOpenIdClient
            .getcwdAdminOpenIdToken("prd-admin"), email)
                .body("")
                .get("/refdata/internal/v1/organisations/pbas")
                .andReturn();

        log.info("{}:: Retrieve pba by email (Internal) response: {}", loggingComponentName, response.statusCode());

        response.then()
                .assertThat()
                .statusCode(OK.value());

        return response.body().as(Map.class);
    }

    public Map<String, Object> retrievePaymentAccountsByEmailFromHeaderV2(String email, String role) {
        Response response = getUserEmailAsHeaderWithExisting(idamOpenIdClient
                .getcwdAdminOpenIdToken("prd-admin"), email)
                .body("")
                .get("/refdata/internal/v2/organisations/pbas")
                .andReturn();

        log.info("{}:: Retrieve pba by email (Internal) response: {}", loggingComponentName, response.statusCode());

        response.then()
                .assertThat()
                .statusCode(OK.value());

        return response.body().as(Map.class);
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> retrievePaymentAccountsByEmailForExternal(HttpStatus status,
                                                                         RequestSpecification requestSpecification,
                                                                         String email) {
        Response response = requestSpecification
                .body("")
                .get("refdata/external/v1/organisations/pbas?email=" + email)
                .andReturn();

        log.info("{}:: Retrieve organisation (External) response: {}", loggingComponentName, response.statusCode());

        response.then()
                .assertThat()
                .statusCode(status.value());

        return response.body().as(Map.class);
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> searchUsersByOrganisation(String organisationId, String role, String showDeleted,
                                                         HttpStatus status, String returnRoles) {

        // Use the role supplied to generate tokens appropritely
        RequestSpecification requestSpecification;
        if (!StringUtils.isBlank(role) && !"prd-admin".equals(role)) {
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
        log.info("{}:: find users response: {}", loggingComponentName, response.statusCode());
        return response.body().as(Map.class);
    }

    public Map<String, Object> searchUsersByOrganisation(RequestSpecification requestSpecification,
                                                         String organisationId, String showDeleted,
                                                         HttpStatus status, String returnRoles) {

        Response response = requestSpecification
                .get("/refdata/internal/v1/organisations/" + organisationId + "/users?showDeleted="
                        + showDeleted + "&returnRoles=" + returnRoles)
                .andReturn();
        response.then()
                .assertThat()
                .statusCode(status.value());
        log.info("{}:: find users response: {}", loggingComponentName, response.statusCode());
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
        log.info("{}:: find users response: {}", loggingComponentName, response.statusCode());
        if (HttpStatus.OK == status) {
            return response.as(Map.class);
        } else {
            return new HashMap<>();
        }
    }


    public Map<String, Object> searchAllActiveUsersByOrganisationExternalWithPagination(HttpStatus status,
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

        log.info("{}:: find users by status: {}", loggingComponentName, response.statusCode());

        response.then()
            .assertThat()
            .statusCode(status.value());
        if (HttpStatus.UNAUTHORIZED.equals(status)) {
            response.getHeader("UnAuthorized-Token-Error")
                    .contains("Authentication Exception");
        }
        if (HttpStatus.OK == status) {
            return response.as(Map.class);
        } else {
            return new HashMap<>();
        }

    }


    public Map<String, Object> searchOrganisationUsersBySearchStringInternal(String organisationId, String role,
                                                                             String showDeleted, HttpStatus status,
                                                                             String searchString) {

        Response response = getMultipleAuthHeadersInternal()
                .get("/refdata/internal/v1/organisations/" + organisationId + "/users?searchString="
                        + searchString)
                .andReturn();

        response.then()
                .assertThat()
                .statusCode(status.value());
        return response.body().as(Map.class);

    }

    public Map<String, Object> searchOrganisationUsersBySearchStringExternal(HttpStatus status,
                                                                       RequestSpecification requestSpecification,
                                                                       String searchString) {

        Response response = requestSpecification
            .get("/refdata/external/v1/organisations/users?searchString=" + searchString)
            .andReturn();

        log.info("{}:: find users by status: {}", loggingComponentName, response.statusCode());

        response.then()
            .assertThat()
            .statusCode(status.value());
        if (HttpStatus.UNAUTHORIZED.equals(status)) {
            response.getHeader("UnAuthorized-Token-Error")
                .contains("Authentication Exception");
        }
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
        log.info("{}:: find org by orgId (External): {}", loggingComponentName, response.statusCode());
        response.then()
            .assertThat()
            .statusCode(status.value());
        return response.body().as(Map.class);
    }

    public Map<String, Object> retrieveOrganisationByOrgIdExternalV2(HttpStatus status,
                                                                   RequestSpecification requestSpecification) {

        Response response = requestSpecification
                .get("/refdata/external/v2/organisations")
                .andReturn();
        log.info("{}:: find org by orgId (External): {}", loggingComponentName, response.statusCode());
        response.then()
                .assertThat()
                .statusCode(status.value());
        return response.body().as(Map.class);
    }

    public Map<String, Object> retrieveOrganisationByOrgIdWithPbaStatusExternal(HttpStatus status, String pbaStatus,
                                                                   RequestSpecification requestSpecification) {

        Response response = requestSpecification
            .get("/refdata/external/v1/organisations?pbaStatus=" + pbaStatus)
            .andReturn();
        log.info("{}:: find org by orgId (External): {}", loggingComponentName, response.statusCode());
        response.then()
            .assertThat()
            .statusCode(status.value());
        return response.body().as(Map.class);
    }


    public Map<String, Object> searchOrganisationUsersByReturnRolesParamExternal(HttpStatus status,
                                                      RequestSpecification requestSpecification,
                                                      String returnRoles) {

        Response response = requestSpecification
            .get("/refdata/external/v1/organisations/users?returnRoles=" + returnRoles)
            .andReturn();

        log.info("{}:: find org by orgId (External): {}", loggingComponentName, response.statusCode());
        response.then()
            .assertThat()
            .statusCode(status.value());
        if (HttpStatus.OK == status) {
            return response.as(Map.class);
        } else {
            return new HashMap<>();
        }

    }

    public void updateOrganisationV2(String organisationIdentifier, String role) {

        OrganisationOtherOrgsCreationRequest organisationCreationRequest = createOrganisationRequestForV2();
        organisationCreationRequest.setStatus("ACTIVE");

        updateOrganisationV2(organisationCreationRequest, role, organisationIdentifier);
    }

    public void updateOrganisationV2(OrganisationOtherOrgsCreationRequest organisationCreationRequest, String role,
                                     String organisationIdentifier) {

        updateOrganisationV2(organisationCreationRequest, role, organisationIdentifier, OK);
    }

    public void updateOrganisationV2(OrganisationOtherOrgsCreationRequest organisationCreationRequest, String role,
                                     String organisationIdentifier, HttpStatus expectedStatus) {

        Response response = getMultipleAuthHeadersInternal()
                .body(organisationCreationRequest)
                .put("/refdata/internal/v2/organisations/" + organisationIdentifier)
                .andReturn();

        log.info("{}:: Update organisation response: {}", loggingComponentName, response.getStatusCode());

        response.then()
                .assertThat()
                .statusCode(expectedStatus.value());
    }

    public void updateOrganisation(String organisationIdentifier, String role) {

        OrganisationCreationRequest organisationCreationRequest = createOrganisationRequest().status("ACTIVE").build();

        updateOrganisation(organisationCreationRequest, role, organisationIdentifier);
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

        log.info("{}:: Update organisation response: {}", loggingComponentName, response.getStatusCode());

        response.then()
            .assertThat()
            .statusCode(expectedStatus.value());
    }

    public void updateOrganisationToReview(String organisationIdentifier, String statusMessage, String role) {

        OrganisationCreationRequest organisationCreationRequest = createOrganisationRequest()
                .status("REVIEW")
                .statusMessage(statusMessage)
                .build();

        updateOrganisation(organisationCreationRequest, role, organisationIdentifier);
    }

    public void updateOrganisationToReviewV2(String organisationIdentifier, String statusMessage, String role) {

        OrganisationOtherOrgsCreationRequest organisationCreationRequest = createOrganisationRequestForV2();
        organisationCreationRequest.setStatus("REVIEW");

        updateOrganisationV2(organisationCreationRequest, role, organisationIdentifier);
    }

    public void updateOrganisationToBlocked(String organisationIdentifier, String statusMessage, String role) {

        OrganisationCreationRequest organisationCreationRequest = createOrganisationRequest()
                .status("BLOCKED")
                .statusMessage(statusMessage)
                .build();

        updateOrganisation(organisationCreationRequest, role, organisationIdentifier);
    }

    public void updateOrganisationWithoutBearerToken(String role, String organisationIdentifier,
                                                     HttpStatus expectedStatus) {
        OrganisationCreationRequest organisationCreationRequest = createOrganisationRequest().status("ACTIVE").build();
        Response response = getMultipleAuthHeadersWithEmptyBearerToken("")
            .body(organisationCreationRequest)
            .put("/refdata/internal/v1/organisations/" + organisationIdentifier)
            .andReturn();

        log.info("{}:: Update organisation response: {}", loggingComponentName, response.getStatusCode());

        response.then()
            .assertThat()
            .statusCode(expectedStatus.value());
    }

    public void updateUser(UserCreationRequest userCreationRequest, String role, String userIdentifier) {

        Response response = getMultipleAuthHeadersInternal()
            .body(userCreationRequest)
            .put("/refdata/internal/v1/users/" + userIdentifier)
            .andReturn();

        log.info("{}:: Update user response: {}", loggingComponentName, response.getStatusCode());

        response.then()
            .assertThat()
            .statusCode(OK.value());
    }

    public Map<String, Object> retrieveOrganisationDetailsByStatus(String status, String role) {

        Response response = getMultipleAuthHeadersInternal()
            .body("")
            .get("/refdata/internal/v1/organisations?status=" + status)
            .andReturn();
        log.debug("{}:: Retrieve organisation response by status: {}", loggingComponentName, response.getStatusCode());
        response.then()
            .assertThat()
            .statusCode(OK.value());

        return response.body().as(Map.class);
    }

    public Map<String, Object> retrieveOrganisationDetailsByStatusV2(String status, String role) {

        Response response = getMultipleAuthHeadersInternal()
                .body("")
                .get("/refdata/internal/v2/organisations?status=" + status)
                .andReturn();
        log.debug("{}:: Retrieve organisation response by status: {}", loggingComponentName, response.getStatusCode());
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

        log.debug("{}:: Retrieve organisation response for unknown status: {}",
                loggingComponentName, response.asString());

        response.then()
            .assertThat()
            .statusCode(BAD_REQUEST.value());
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

    public Map<String, Object> editPbaAccountsByOrgId(PbaRequest pbaEditRequest, String orgId, String hmctsAdmin) {

        Response response = getMultipleAuthHeadersInternal()
            .body(pbaEditRequest)
            .put("/refdata/internal/v1/organisations/" + orgId + "/pbas")
            .andReturn();

        log.info("{}:: Retrieve edit pba response: {}", loggingComponentName, response.asString());

        response.then()
            .assertThat()
            .statusCode(OK.value());

        return response.body().as(Map.class);
    }

    public Map<String, Object> updatePbas(
            UpdatePbaRequest updatePbaRequest, String orgId, String hmctsAdmin, HttpStatus status) {

        Response response = getMultipleAuthHeadersInternal()
                .body(updatePbaRequest)
                .put("/refdata/internal/v1/organisations/" + orgId + "/pba/status")
                .andReturn();

        log.info("{}:: Update pba response: {}", loggingComponentName, response.asString());

        response.then()
                .assertThat()
                .statusCode(status.value());

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

    public Response deleteOrganisation(String organisationId, String role, HttpStatus status) {
        Response response = getMultipleAuthHeadersInternal()
            .delete("/refdata/internal/v1/organisations/" + organisationId)
            .andReturn();

        if (response.statusCode() != NO_CONTENT.value()) {
            log.info("{}:: Delete organisation response: {}", loggingComponentName, response.asString());
        }
        response.then()
            .assertThat()
            .statusCode(status.value());
        return response;
    }

    public void deleteOrganisationByExternalUser(String organisationId, HttpStatus status) {
        Response response = getMultipleAuthHeadersInternal()
            .body("")
            .delete("/refdata/external/v1/organisations/" + organisationId)
            .andReturn();

        if (response.statusCode() != NO_CONTENT.value()) {
            log.info("{}:: Delete organisation response: {}", loggingComponentName, response.asString());
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
        return getMultipleAuthHeaders(idamOpenIdClient.getcwdAdminOpenIdToken("prd-admin"));
    }

    public RequestSpecification getMultipleAuthHeadersWithGivenRole(String role) {
        return getMultipleAuthHeaders(idamOpenIdClient.getCwdSystemUserOpenIdToken(role));
    }

    public RequestSpecification getMultipleAuthHeadersExternal(String role, String firstName, String lastName,
                                                               String email) {
        String bearerTokenForSuperUser = idamOpenIdClient.getExternalOpenIdToken(role, firstName, lastName, email);
        return getMultipleAuthHeaders(bearerTokenForSuperUser);
    }

    public String getBearerTokenExternal(String role, String firstName, String lastName,
                                                               String email) {
        return idamOpenIdClient.getExternalOpenIdToken(role, firstName, lastName, email);
    }

    public RequestSpecification getEmailFromAuthHeadersExternal(String role, String firstName, String lastName,
                                                               String email) {
        String bearerTokenForSuperUser = idamOpenIdClient.getExternalOpenIdToken(role, firstName, lastName, email);
        return getUserEmailAsHeaderWithExisting(bearerTokenForSuperUser, email);
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

    public RequestSpecification getUserEmailAsHeaderWithExisting(String userToken, String email) {
        return SerenityRest.with()
                .relaxedHTTPSValidation()
                .baseUri(professionalApiUrl)
                .header("Content-Type", APPLICATION_JSON_VALUE)
                .header("Accepts", APPLICATION_JSON_VALUE)
                .header(SERVICE_HEADER, "Bearer " + s2sToken)
                .header(AUTHORIZATION_HEADER, "Bearer " + userToken)
                .header(USER_EMAIL_HEADER, email);
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

    public Object retrieveAllActiveOrganisationsWithMinimalInfo(RequestSpecification requestSpecification,
                                                                HttpStatus expectedStatus, String status,
                                                                boolean address) {
        Response response = requestSpecification
            .get("/refdata/external/v1/organisations/status/" + status + "?address=" + address)
            .andReturn();

        response.then()
            .assertThat()
            .statusCode(expectedStatus.value());
        if (expectedStatus.is2xxSuccessful()) {
            return Arrays.asList(response.getBody().as(OrganisationMinimalInfoResponse[].class));
        } else {
            return response.getBody().as(ErrorResponse.class);
        }
    }

    public Map<String, Object> findMFAByUserId(HttpStatus expectedStatus, String userId) {

        Response response = withUnauthenticatedRequest()
                .get("/refdata/external/v1/organisations/mfa?user_id=" + userId)
                .andReturn();

        response.then()
                .assertThat()
                .statusCode(expectedStatus.value());

        log.info("{}:: find mfa response: {}", loggingComponentName, response.statusCode());
        return response.body().as(Map.class);
    }

    public void updateOrgMfaStatus(MfaUpdateRequest mfaUpdateRequest,
                                   String organisationId, String role, HttpStatus expectedStatus) {

        Response response = getMultipleAuthHeadersInternal()
                .body(mfaUpdateRequest)
                .put("/refdata/internal/v1/organisations/" + organisationId + "/mfa")
                .andReturn();

        response.then()
                .assertThat()
                .statusCode(expectedStatus.value());

        log.info("{}:: Update organisation mfa status response: {}", loggingComponentName, response.getStatusCode());
    }

    public void deletePaymentAccountsOfOrganisation(PbaRequest deletePbaRequest,
                                                    RequestSpecification requestSpecification,
                                                    HttpStatus expectedStatus) {
        Response response = requestSpecification
                .body(deletePbaRequest)
                .delete("/refdata/external/v1/organisations/pba")
                .andReturn();

        response.then()
                .assertThat()
                .statusCode(expectedStatus.value());

        log.info("{}:: Delete PBA of organisation status response: {}",
                loggingComponentName, response.getStatusCode());
    }

    public Object findOrganisationsByPbaStatus(HttpStatus expectedStatus, PbaStatus pbaStatus) {

        Response response = getMultipleAuthHeadersInternal()
                .get("/refdata/internal/v1/organisations/pba/" + pbaStatus.toString())
                .andReturn();

        response.then()
                .assertThat()
                .statusCode(expectedStatus.value());

        if (expectedStatus.is2xxSuccessful()) {
            return Arrays.asList(response.getBody().as(OrganisationsWithPbaStatusResponse[].class));
        } else {
            return response.getBody().as(ErrorResponse.class);
        }
    }

    public ResponseBody addPaymentAccountsOfOrganisation(PbaRequest pbaRequest,
                                                         RequestSpecification requestSpecification,
                                                         HttpStatus expectedStatus) {
        Response response = requestSpecification
                .body(pbaRequest)
                .post("/refdata/external/v1/organisations/pba")
                .andReturn();

        response.then()
                .assertThat()
                .statusCode(expectedStatus.value());

        log.info("{}:: Add PBA of organisation status response: {}",
                loggingComponentName, response.getStatusCode());

        return response.body();
    }

    public void deleteMultipleAddressesOfOrganisation(List<DeleteMultipleAddressRequest> deleteRequest,
                                                    RequestSpecification requestSpecification,
                                                    HttpStatus expectedStatus) {
        Response response = requestSpecification
                .body(deleteRequest)
                .delete("/refdata/external/v1/organisations/addresses")
                .andReturn();

        response.then()
                .assertThat()
                .statusCode(expectedStatus.value());

        log.info("{}:: Delete Multiple Addresses of an organisation status response: {}",
                loggingComponentName, response.getStatusCode());
    }
}
