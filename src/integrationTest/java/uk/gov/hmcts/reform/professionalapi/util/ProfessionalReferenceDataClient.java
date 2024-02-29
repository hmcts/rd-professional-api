package uk.gov.hmcts.reform.professionalapi.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.professionalapi.controller.advice.ErrorResponse;
import uk.gov.hmcts.reform.professionalapi.controller.request.BulkCustomerRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.ContactInformationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.DeleteMultipleAddressRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.MfaUpdateRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.NewUserCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationByProfileIdsRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationOtherOrgsCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.PbaRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.UpdatePbaRequest;
import uk.gov.hmcts.reform.professionalapi.controller.response.OrganisationMinimalInfoResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.OrganisationResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.OrganisationsWithPbaStatusResponse;
import uk.gov.hmcts.reform.professionalapi.domain.UserProfileUpdatedData;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static java.util.Objects.isNull;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static uk.gov.hmcts.reform.professionalapi.util.JwtTokenUtil.generateToken;

@Slf4j
@PropertySource(value = "/integrationTest/resources/application.yml")
@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
public class ProfessionalReferenceDataClient {

    private static final String APP_EXT_BASE_PATH = "/refdata/external/v1/organisations";

    private static final String APP_INT_BULK = "/refdata/internal/v1/bulkCustomer";

    private static final String APP_EXT_V2_BASE_PATH = "/refdata/external/v2/organisations";
    private static final String APP_INT_V2_BASE_PATH = "/refdata/internal/v2/organisations";
    private static final String APP_INT_BASE_PATH = "/refdata/internal/v1/organisations";
    private static final String JWT_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI"
            + "6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";
    private final Integer prdApiPort;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate = new RestTemplate();
    private String baseUrl;
    private String baseIntUrl;

    private String baseV2Url;


    private String baseBulkIntUrl;

    private String baseV2IntUrl;

    private String issuer;
    private long expiration;

    public Map<String, String> bearerTokenMap = new HashMap<>();


    public ProfessionalReferenceDataClient(int port, String issuer, Long tokenExpirationInterval) {
        this.prdApiPort = port;
        this.baseUrl = "http://localhost:" + prdApiPort + APP_EXT_BASE_PATH;
        this.baseIntUrl = "http://localhost:" + prdApiPort + APP_INT_BASE_PATH;
        this.baseV2Url = "http://localhost:" + prdApiPort + APP_EXT_V2_BASE_PATH;
        this.baseV2IntUrl = "http://localhost:" + prdApiPort + APP_INT_V2_BASE_PATH;
        this.baseBulkIntUrl = "http://localhost:" + prdApiPort + APP_INT_BULK;
        this.issuer = issuer;
        this.expiration = tokenExpirationInterval;
    }

    public Map<String, Object> createOrganisation(OrganisationCreationRequest request) {
        return postRequest(baseUrl, request, null, null);
    }

    public Map<String, Object> createOrganisationV2(OrganisationOtherOrgsCreationRequest request) {
        return postRequest(baseV2Url, request, null, null);
    }

    public Map<String, Object> findPaymentAccountsByEmail(String email, String role) {
        return getRequestToGetEmailFromHeader("/refdata/internal/v1/organisations" + "/pbas", role, email);
    }

    public Map<String, Object> findPaymentAccountsByEmailFromHeader(String email, String role) {
        return getRequestToGetEmailFromHeader("/refdata/internal/v1/organisations" + "/pbas",
                role, "", email);
    }

    public Map<String, Object> findPaymentAccountsByEmailFromHeaderForExternalUsers(String email, String role,
                                                                                    String userId) {
        return getRequestToGetEmailFromHeader("/refdata/external/v1/organisations" + "/pbas",
                role, userId, email);
    }

    public Map<String, Object> findPaymentAccountsForV2ByEmailFromHeader(String email, String role) {
        return getRequestToGetEmailFromHeader("/refdata/internal/v2/organisations" + "/pbas",
                role, "", email);
    }

    public Map<String, Object> findPaymentAccountsForV2ByEmailFromHeaderForExternalUsers(String email, String role,
                                                                                         String userId) {
        return getRequestToGetEmailFromHeader("/refdata/external/v2/organisations" + "/pbas",
                role, userId, email);
    }

    public Map<String, Object> findMFAByUserID(String professionalUserID) {
        return getRequestWithoutAuthHeaders(APP_EXT_BASE_PATH + "/mfa?user_id={userIdentifier}", professionalUserID);
    }

    public Map<String, Object> retrieveSingleOrganisation(String id, String role) {
        return getRequest(APP_INT_BASE_PATH + "?id={id}", role, id);
    }

    public Map<String, Object> retrieveBulkOrganisation(BulkCustomerRequest request, String role) {
        return postRequest(baseBulkIntUrl, request, role, null);
    }



    public Map<String, Object> retrieveSingleOrganisationForV2Api(String id, String role) {
        return getRequest(APP_INT_V2_BASE_PATH + "?id={id}", role, id);
    }

    public Map<String, Object> retrieveAllOrganisationsWithPagination(String page, String size, String role) {
        return getRequest(APP_INT_BASE_PATH + "?page={page}&size={size}", role, page, size);
    }

    public Map<String, Object> retrieveAllOrganisationsWithPaginationSince(String page, String size, String role,
                                                                           String since) {
        return getRequest(APP_INT_BASE_PATH + "?page={page}&size={size}&since={since}", role, page, size, since);
    }

    public Map<String, Object> retrieveAllOrganisationsWithPaginationForV2Api(String page, String size, String role) {
        return getRequest(APP_INT_V2_BASE_PATH + "?page={page}&size={size}", role, page, size);
    }

    public Map<String, Object> retrieveExternalOrganisation(String id, String role) {
        return getRequestForExternal(APP_EXT_BASE_PATH, role, id);
    }

    public Map<String, Object> retrieveExternalOrganisationForV2Api(String id, String role) {
        return getRequestForExternal(APP_EXT_BASE_PATH, role, id);
    }

    public Map<String, Object> retrieveExternalOrganisationWithPendingPbas(String id, String pbaStatus, String role) {
        return getRequestForExternal(APP_EXT_BASE_PATH + "?pbaStatus=" + pbaStatus, role, id);
    }

    public Map<String, Object> retrieveExternalOrganisationWithPendingPbasForV2Api(String id, String pbaStatus,
                                                                                   String role) {
        return getRequestForExternal(APP_EXT_V2_BASE_PATH + "?pbaStatus=" + pbaStatus, role, id);
    }

    public Map<String, Object> retrieveAllOrganisations(String role) {
        return getRequest(APP_INT_BASE_PATH + "/", role);
    }

    public Map<String, Object> retrieveAllOrganisationsSince(String role, String since) {
        return getRequest(APP_INT_BASE_PATH + "?since={since}", role, since);
    }

    public Map<String, Object> createOrganisationIntV2(OrganisationOtherOrgsCreationRequest request) {
        return postRequest(baseV2IntUrl, request, null, null);
    }

    public Map<String, Object> retrieveAllOrganisationsForV2Api(String role) {
        return getRequest(APP_INT_V2_BASE_PATH + "/", role);
    }

    public Object retrieveOrganisationsWithMinimalInfo(String id, String role, String orgStatus,
                                                       Boolean address, Class expectedClass)
            throws JsonProcessingException {
        ResponseEntity<Object> responseEntity = getRequestForExternalWithGivenResponseType(
                APP_EXT_BASE_PATH + "/status/" + orgStatus + "?address=" + address, role, id, expectedClass);
        HttpStatus status = responseEntity.getStatusCode();
        objectMapper.registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        if (status.is2xxSuccessful()) {
            return Arrays.asList((OrganisationMinimalInfoResponse[]) objectMapper.convertValue(
                    responseEntity.getBody(), expectedClass));
        } else {
            Map<String, Object> errorResponseMap = new HashMap<>();
            errorResponseMap.put("response_body", objectMapper.readValue(
                    responseEntity.getBody().toString(), ErrorResponse.class));
            errorResponseMap.put("http_status", status);
            return errorResponseMap;
        }
    }

    public Object retrieveOrganisationsWithMinimalInfoForV2Api(String id, String role, String orgStatus,
                                                       Boolean address, Class expectedClass)
            throws JsonProcessingException {
        ResponseEntity<Object> responseEntity = getRequestForExternalWithGivenResponseType(
                APP_EXT_V2_BASE_PATH + "/status/" + orgStatus + "?address=" + address, role, id, expectedClass);
        HttpStatus status = responseEntity.getStatusCode();
        objectMapper.registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        if (status.is2xxSuccessful()) {
            return Arrays.asList((OrganisationMinimalInfoResponse[]) objectMapper.convertValue(
                    responseEntity.getBody(), expectedClass));
        } else {
            Map<String, Object> errorResponseMap = new HashMap<>();
            errorResponseMap.put("response_body", objectMapper.readValue(
                    responseEntity.getBody().toString(), ErrorResponse.class));
            errorResponseMap.put("http_status", status);
            return errorResponseMap;
        }
    }

    public Map<String, Object> retrieveAllOrganisationDetailsByStatusTest(String status, String role) {
        return getRequest(APP_INT_BASE_PATH + "?status={status}", role, status);
    }

    public Map<String, Object> retrieveAllOrganisationDetailsByStatusSinceTest(String status, String role,
                                                                               String since) {
        return getRequest(APP_INT_BASE_PATH + "?status={status}&&since={since}", role, status, since);
    }

    public Map<String, Object> retrieveAllOrganisationDetailsByStatusForV2ApiTest(String status, String role) {
        return getRequest(APP_INT_V2_BASE_PATH + "?status={status}", role, status);
    }

    public Map<String, Object> addUserToOrganisationWithUserId(String orgId,
                                                               NewUserCreationRequest newUserCreationRequest,
                                                               String role, String userId) {
        return postRequest(baseIntUrl + "/" + orgId + "/users/", newUserCreationRequest, role, userId);
    }

    public Map<String, Object> addUserToOrganisation(String orgId, NewUserCreationRequest newUserCreationRequest,
                                                     String role) {
        return postRequest(baseIntUrl + "/" + orgId + "/users/", newUserCreationRequest, role, null);
    }

    public Map<String, Object> findUsersByOrganisation(String organisationIdentifier, String showDeleted, String role) {
        return getRequest(APP_INT_BASE_PATH + "/" + organisationIdentifier + "/users?showDeleted={showDeleted}",
                role, showDeleted);
    }

    // Override the method to support return roles param
    public Map<String, Object> findUsersByOrganisation(String organisationIdentifier, String showDeleted, String role,
                                                       String returnRoles) {
        return getRequest(APP_INT_BASE_PATH + "/" + organisationIdentifier
                + "/users?showDeleted={showDeleted}&returnRoles={returnRoles}", role, showDeleted, returnRoles);
    }

    public Map<String, Object> findUsersByOrganisationAndUserIdentifier(String organisationIdentifier, String role,
                                                                        String userIdentifier) {
        return getRequest(APP_INT_BASE_PATH + "/" + organisationIdentifier
                + "/users?userIdentifier={userIdentifier}", role, userIdentifier);
    }

    public Map<String, Object> findUsersByOrganisationWithoutAuthHeaders(
            String organisationIdentifier, String showDeleted, String returnRoles) {
        return getRequestWithoutAuthHeaders(APP_INT_BASE_PATH + "/" + organisationIdentifier
                + "/users?showDeleted={showDeleted}&returnRoles={returnRoles}", showDeleted, returnRoles);
    }

    public Map<String, Object> findUsersByOrganisationWithPaginationInformation(String organisationIdentifier,
                                                                                String showDeleted, String role) {
        return getRequest(APP_INT_BASE_PATH + "/" + organisationIdentifier
                + "/users?showDeleted={showDeleted}&page=1&size=3", role, showDeleted);
    }

    public Map<String, Object> findAllUsersForOrganisationByStatus(String showDeleted, String status, String role,
                                                                   String id) {
        return getRequestForExternal(APP_EXT_BASE_PATH + "/users?showDeleted={showDeleted}&status={status}",
                role, id, showDeleted, status);
    }

    public Map<String, Object> findUsersByOrganisationWithReturnRoles(String returnRoles, String role, String id) {
        return getRequestForExternal(APP_EXT_BASE_PATH + "/users?returnRoles={returnRoles}", role, id,
                returnRoles);
    }

    public Map<String, Object> findRefreshUsersWithSince(String since, Integer pageSize) {
        return getRequestWithoutBearerToken(APP_INT_BASE_PATH + "/users?since={since}&pageSize={pageSize}", since,
                pageSize);
    }

    public Map<String, Object> findRefreshUsersWithUserIdentifier(String userId) {
        return getRequestWithoutBearerToken(APP_INT_BASE_PATH + "/users?userId={userId}",
                userId);
    }

    public Map<String, Object> retrieveOrganisationsByProfileIds(OrganisationByProfileIdsRequest request, Integer
            pageSize, UUID searchAfter) {
        StringBuilder sb = new StringBuilder(baseIntUrl)
                .append("/getOrganisationsByProfile?");
        if (pageSize != null) {
            sb.append("pageSize=").append(pageSize);
        }
        if (searchAfter != null) {
            sb.append("&searchAfter=").append(searchAfter);
        }
        String uriPath = sb.toString();
        return postRequest(uriPath, request, null, null);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private <T> Map<String, Object> postRequest(String uriPath, T requestBody, String role, String userId) {

        HttpEntity<T> request = null;

        if (null == role) {
            request = new HttpEntity<>(requestBody, getS2sTokenHeaders());

        } else {

            request = new HttpEntity<>(requestBody, getMultipleAuthHeaders(role, userId));
        }

        ResponseEntity<Map> responseEntity;

        try {

            responseEntity = restTemplate.postForEntity(
                    uriPath,
                    request,
                    Map.class);

        } catch (RestClientResponseException ex) {
            HashMap<String, Object> statusAndBody = new HashMap<>(2);
            statusAndBody.put("http_status", String.valueOf(ex.getRawStatusCode()));
            statusAndBody.put("response_body", ex.getResponseBodyAsString());
            return statusAndBody;
        }

        return getResponse(responseEntity);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private Map<String, Object> getRequest(String uriPath, String role, Object... params) {

        ResponseEntity<Map> responseEntity;

        try {

            HttpEntity<?> request = new HttpEntity<>(getMultipleAuthHeaders(role));
            responseEntity = restTemplate
                    .exchange("http://localhost:" + prdApiPort + uriPath,
                            HttpMethod.GET,
                            request,
                            Map.class,
                            params);
        } catch (HttpStatusCodeException ex) {
            HashMap<String, Object> statusAndBody = new HashMap<>(2);
            statusAndBody.put("http_status", String.valueOf(ex.getRawStatusCode()));
            statusAndBody.put("response_body", ex.getResponseBodyAsString());
            return statusAndBody;
        }

        return getResponse(responseEntity);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private Map<String, Object> getRequestWithoutBearerToken(String uriPath, Object... params) {

        ResponseEntity<Map> responseEntity;

        try {
            HttpEntity<?> request = new HttpEntity<>(getS2sTokenHeaders());

            responseEntity = restTemplate
                    .exchange("http://localhost:" + prdApiPort + uriPath,
                            HttpMethod.GET,
                            request,
                            Map.class,
                            params);
        } catch (HttpStatusCodeException ex) {
            HashMap<String, Object> statusAndBody = new HashMap<>(2);
            statusAndBody.put("http_status", String.valueOf(ex.getRawStatusCode()));
            statusAndBody.put("response_body", ex.getResponseBodyAsString());
            return statusAndBody;
        }

        return getResponse(responseEntity);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private Map<String, Object> getRequestWithoutAuthHeaders(String uriPath, Object... params) {

        ResponseEntity<Map> responseEntity;

        try {

            HttpEntity<?> request = new HttpEntity<>(new HttpHeaders());
            responseEntity = restTemplate
                    .exchange("http://localhost:" + prdApiPort + uriPath,
                            HttpMethod.GET,
                            request,
                            Map.class,
                            params);
        } catch (HttpStatusCodeException ex) {
            HashMap<String, Object> statusAndBody = new HashMap<>(2);
            statusAndBody.put("http_status", String.valueOf(ex.getRawStatusCode()));
            statusAndBody.put("response_body", ex.getResponseBodyAsString());
            return statusAndBody;
        }

        return getResponse(responseEntity);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private Map<String, Object> getRequestToGetEmailFromHeader(String uriPath, String role, String userId,
                                                               Object... params) {

        ResponseEntity<Map> responseEntity;
        HttpHeaders httpHeaders = null;
        try {
            if (isEmpty(userId)) {
                httpHeaders = getMultipleAuthHeaders(role);
            } else {
                httpHeaders = getMultipleAuthHeaders(role, userId);
            }
            httpHeaders.add("UserEmail", params[0].toString());
            HttpEntity<?> request = new HttpEntity<>(httpHeaders);
            responseEntity = restTemplate
                    .exchange("http://localhost:" + prdApiPort + uriPath,
                            HttpMethod.GET,
                            request,
                            Map.class,
                            params);
        } catch (HttpStatusCodeException ex) {
            HashMap<String, Object> statusAndBody = new HashMap<>(2);
            statusAndBody.put("http_status", String.valueOf(ex.getRawStatusCode()));
            statusAndBody.put("response_body", ex.getResponseBodyAsString());
            return statusAndBody;
        }

        return getResponse(responseEntity);
    }

    private Map<String, Object> getRequestForExternal(String uriPath, String role, String userId, Object... params) {

        ResponseEntity<Map> responseEntity;

        try {
            HttpEntity<?> request = new HttpEntity<>(getMultipleAuthHeaders(role, userId));
            responseEntity = restTemplate
                    .exchange("http://localhost:" + prdApiPort + uriPath,
                            HttpMethod.GET,
                            request,
                            Map.class,
                            params);
        } catch (HttpStatusCodeException ex) {
            HashMap<String, Object> statusAndBody = new HashMap<>(2);
            statusAndBody.put("http_status", String.valueOf(ex.getRawStatusCode()));
            statusAndBody.put("response_body", ex.getResponseBodyAsString());
            return statusAndBody;
        }

        return getResponse(responseEntity);
    }

    @SuppressWarnings("unchecked")
    private ResponseEntity<Object> getRequestForExternalWithGivenResponseType(
            String uriPath, String role, String userId, Class clasz, Object... params) {

        ResponseEntity<Object> responseEntity;
        try {
            HttpEntity<?> request = new HttpEntity<>(getMultipleAuthHeaders(role, userId));
            responseEntity = restTemplate
                    .exchange("http://localhost:" + prdApiPort + uriPath,
                            HttpMethod.GET,
                            request,
                            clasz,
                            params);
        } catch (HttpStatusCodeException ex) {
            return ResponseEntity.status(ex.getRawStatusCode()).body(ex.getResponseBodyAsString());
        }
        return responseEntity;
    }

    private Map<String, Object> getRequestForExternalRoles(
            String uriPath, String role, String userId, Object... params) {

        ResponseEntity<Map> responseEntity;

        try {
            HttpEntity<?> request = new HttpEntity<>(getMultipleAuthHeaders(role, userId));
            responseEntity = restTemplate
                    .exchange("http://localhost:" + prdApiPort + uriPath,
                            HttpMethod.GET,
                            request,
                            Map.class,
                            params);
        } catch (HttpStatusCodeException ex) {
            HashMap<String, Object> statusAndBody = new HashMap<>(2);
            statusAndBody.put("http_status", String.valueOf(ex.getRawStatusCode()));
            statusAndBody.put("response_body", ex.getResponseBodyAsString());
            return statusAndBody;
        }

        return getResponse(responseEntity);
    }


    public Map<String, Object> updateOrganisation(
            OrganisationCreationRequest organisationCreationRequest, String role, String organisationIdentifier) {

        ResponseEntity<OrganisationResponse> responseEntity = null;
        String urlPath = "http://localhost:" + prdApiPort + APP_INT_BASE_PATH + "/" + organisationIdentifier;
        try {
            HttpEntity<OrganisationCreationRequest> requestEntity = new HttpEntity<>(organisationCreationRequest,
                    getMultipleAuthHeaders(role));
            responseEntity = restTemplate.exchange(urlPath, HttpMethod.PUT, requestEntity, OrganisationResponse.class);
        } catch (RestClientResponseException ex) {
            HashMap<String, Object> statusAndBody = new HashMap<>(2);
            statusAndBody.put("http_status", String.valueOf(ex.getRawStatusCode()));
            statusAndBody.put("response_body", ex.getResponseBodyAsString());
            return statusAndBody;
        }

        Map<String, Object> organisationResponse = new HashMap<>();
        organisationResponse.put("http_status", responseEntity.getStatusCodeValue());
        return organisationResponse;
    }

    public Map<String, Object> updateOrganisationForV2Api(
            OrganisationCreationRequest organisationCreationRequest, String role, String organisationIdentifier) {

        ResponseEntity<OrganisationResponse> responseEntity = null;
        String urlPath = "http://localhost:" + prdApiPort + APP_INT_V2_BASE_PATH + "/" + organisationIdentifier;
        try {
            HttpEntity<OrganisationCreationRequest> requestEntity = new HttpEntity<>(organisationCreationRequest,
                    getMultipleAuthHeaders(role));
            responseEntity = restTemplate.exchange(urlPath, HttpMethod.PUT, requestEntity, OrganisationResponse.class);
        } catch (RestClientResponseException ex) {
            HashMap<String, Object> statusAndBody = new HashMap<>(2);
            statusAndBody.put("http_status", String.valueOf(ex.getRawStatusCode()));
            statusAndBody.put("response_body", ex.getResponseBodyAsString());
            return statusAndBody;
        }

        Map<String, Object> organisationResponse = new HashMap<>();
        organisationResponse.put("http_status", responseEntity.getStatusCodeValue());
        return organisationResponse;
    }

    public String getAndReturnBearerToken(String userId, String role) {
        String bearerToken;
        if (bearerTokenMap.get(role) == null && userId != null) {
            bearerToken = "Bearer ".concat(getBearerToken(Objects.isNull(userId) ? UUID.randomUUID().toString()
                    : userId, role));
            bearerTokenMap.put(role, bearerToken);
        } else if (bearerTokenMap.get(role + userId) == null) {
            bearerToken = "Bearer ".concat(getBearerToken(userId, role));
            bearerTokenMap.put(role + userId, bearerToken);
            return bearerToken;
        }
        if (userId == null) {
            return bearerTokenMap.get(role + userId);
        }
        return bearerTokenMap.get(role);
    }


    private HttpHeaders getMultipleAuthHeaders(String role, String userId) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(APPLICATION_JSON);

        headers.add("ServiceAuthorization", JWT_TOKEN);

        String bearerToken = getAndReturnBearerToken(userId, role);
        mockJwtToken(role, userId, bearerToken);
        headers.add("Authorization", bearerToken);

        return headers;
    }

    private HttpHeaders getMultipleAuthHeaders(String role) {

        return getMultipleAuthHeaders(role, null);
    }

    private HttpHeaders getInvalidAuthHeaders(String role) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(APPLICATION_JSON);

        headers.add("ServiceAuthorization", "Invalid token");

        String bearerToken = "Bearer ".concat("invalid token");
        headers.add("Authorization", bearerToken);

        return headers;
    }

    private final String getBearerToken(String userId, String role) {

        return generateToken(issuer, expiration, userId, role);

    }

    private HttpHeaders getS2sTokenHeaders() {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(APPLICATION_JSON);
        headers.add("ServiceAuthorization", JWT_TOKEN);
        return headers;
    }

    private Map getResponse(ResponseEntity<Map> responseEntity) {

        Map response = new HashMap<>();
        if (responseEntity.hasBody()) {
            response = objectMapper
                    .convertValue(
                            responseEntity.getBody(), Map.class);
        }

        response.put("http_status", responseEntity.getStatusCode().toString());
        response.put("headers", responseEntity.getHeaders().toString());

        return response;
    }


    public Map<String, Object> modifyUserRolesOfOrganisation(UserProfileUpdatedData userProfileUpdatedData,
                                                             String orgId, String userIdentifier, String hmctsAdmin) {
        ResponseEntity<Map> responseEntity = null;
        String urlPath = "http://localhost:" + prdApiPort + APP_INT_BASE_PATH + "/" + orgId + "/users/"
                + userIdentifier;

        try {
            HttpEntity<UserProfileUpdatedData> requestEntity = new HttpEntity<>(userProfileUpdatedData,
                    getMultipleAuthHeaders(hmctsAdmin));
            responseEntity = restTemplate.exchange(urlPath, HttpMethod.PUT, requestEntity, Map.class);
        } catch (RestClientResponseException ex) {
            HashMap<String, Object> statusAndBody = new HashMap<>(2);
            statusAndBody.put("http_status", String.valueOf(ex.getRawStatusCode()));
            statusAndBody.put("response_body", ex.getResponseBodyAsString());
            return statusAndBody;
        }


        return getResponse(responseEntity);
    }

    public Map<String, Object> modifyUserRolesOfOrganisationExternal(UserProfileUpdatedData userProfileUpdatedData,
                                                                     String userIdentifier, String externalRole) {
        ResponseEntity<Map> responseEntity = null;
        String urlPath = "http://localhost:" + prdApiPort + APP_EXT_BASE_PATH + "/users/" + userIdentifier;

        try {
            HttpEntity<UserProfileUpdatedData> requestEntity = new HttpEntity<>(userProfileUpdatedData,
                    getMultipleAuthHeaders(externalRole, userIdentifier));
            responseEntity = restTemplate.exchange(urlPath, HttpMethod.PUT, requestEntity, Map.class);
        } catch (RestClientResponseException ex) {
            HashMap<String, Object> statusAndBody = new HashMap<>(2);
            statusAndBody.put("http_status", String.valueOf(ex.getRawStatusCode()));
            statusAndBody.put("response_body", ex.getResponseBodyAsString());
            return statusAndBody;
        }


        return getResponse(responseEntity);
    }

    public Map<String, Object> modifyUserConfiguredAcessOfOrganisation(UserProfileUpdatedData userProfileUpdatedData,
                                                             String orgId, String userIdentifier, String role) {
        ResponseEntity<Map> responseEntity = null;
        String urlPath = "http://localhost:" + prdApiPort + APP_EXT_BASE_PATH + "/users/"
                + userIdentifier;

        try {
            HttpEntity<UserProfileUpdatedData> requestEntity = new HttpEntity<>(userProfileUpdatedData,
                    getMultipleAuthHeaders(role));
            responseEntity = restTemplate.exchange(urlPath, HttpMethod.PUT, requestEntity, Map.class);
        } catch (RestClientResponseException ex) {
            HashMap<String, Object> statusAndBody = new HashMap<>(2);
            statusAndBody.put("http_status", String.valueOf(ex.getRawStatusCode()));
            statusAndBody.put("response_body", ex.getResponseBodyAsString());
            return statusAndBody;
        }


        return getResponse(responseEntity);
    }

    public Map<String, Object> editPaymentsAccountsByOrgId(PbaRequest pbaEditRequest, String orgId,
                                                           String hmctsAdmin, String requestBody) {
        ResponseEntity<Map> responseEntity = null;
        String urlPath = "http://localhost:" + prdApiPort + APP_INT_BASE_PATH + "/" + orgId + "/pbas";

        try {
            HttpEntity<?> requestEntity = new HttpEntity<>(isNull(pbaEditRequest) ? requestBody : pbaEditRequest,
                    getMultipleAuthHeaders(hmctsAdmin));
            responseEntity = restTemplate.exchange(urlPath, HttpMethod.PUT, requestEntity, Map.class);

        } catch (RestClientResponseException ex) {
            HashMap<String, Object> statusAndBody = new HashMap<>();
            statusAndBody.put("http_status", String.valueOf(ex.getRawStatusCode()));
            statusAndBody.put("response_body", ex.getResponseBodyAsString());
            return statusAndBody;
        }


        return getResponse(responseEntity);
    }

    public Map<String, Object> updatePaymentsAccountsByOrgId(UpdatePbaRequest updatePbaRequest, String orgId,
                                                             String hmctsAdmin, String requestBody) {
        ResponseEntity<Map> responseEntity = null;
        String urlPath = "http://localhost:" + prdApiPort + APP_INT_BASE_PATH + "/" + orgId + "/pba/status";

        try {
            HttpEntity<?> requestEntity = new HttpEntity<>(isNull(updatePbaRequest) ? requestBody : updatePbaRequest,
                    getMultipleAuthHeaders(hmctsAdmin));
            responseEntity = restTemplate.exchange(urlPath, HttpMethod.PUT, requestEntity, Map.class);

        } catch (RestClientResponseException ex) {
            HashMap<String, Object> statusAndBody = new HashMap<>();
            statusAndBody.put("http_status", String.valueOf(ex.getRawStatusCode()));
            statusAndBody.put("response_body", ex.getResponseBodyAsString());
            return statusAndBody;
        }

        return getResponse(responseEntity);
    }

    private Jwt createJwt(String token, JWT parsedJwt) {
        Jwt jwt = null;
        try {
            Map<String, Object> headers = new LinkedHashMap<>(parsedJwt.getHeader().toJSONObject());
            Map<String, Object> claims = new HashMap<>();
            for (String key : parsedJwt.getJWTClaimsSet().getClaims().keySet()) {
                Object value = parsedJwt.getJWTClaimsSet().getClaims().get(key);
                if (key.equals("exp") || key.equals("iat")) {
                    value = ((Date) value).toInstant();
                }
                claims.put(key, value);
            }
            jwt = Jwt.withTokenValue(token)
                    .headers(h -> h.putAll(headers))
                    .claims(c -> c.putAll(claims))
                    .build();
        } catch (Exception ex) {
            System.out.println(ex);
        }
        return jwt;
    }

    public Jwt decode(String token) {
        JWT jwt = null;
        try {
            jwt = JWTParser.parse(token);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        return createJwt(token, jwt);
    }

    public synchronized void mockJwtToken(String role, String userId, String bearerToken) {
        String[] bearerTokenArray = bearerToken.split(" ");
        when(JwtDecoderMockBuilder.getJwtDecoder().decode(anyString())).thenReturn(decode(bearerTokenArray[1]));
    }


    public Map<String, Object> deletePaymentsAccountsByOrgId(PbaRequest pbaDeleteRequest, String supportedRole,
                                                             String userId) {
        ResponseEntity<Map> responseEntity = null;
        String urlPath = "http://localhost:" + prdApiPort + APP_EXT_BASE_PATH + "/pba";

        try {
            HttpEntity<PbaRequest> requestEntity = new HttpEntity<>(pbaDeleteRequest,
                    getMultipleAuthHeaders(supportedRole, userId));
            responseEntity = restTemplate.exchange(urlPath, HttpMethod.DELETE, requestEntity, Map.class);

        } catch (RestClientResponseException ex) {
            HashMap<String, Object> statusAndBody = new HashMap<>();
            statusAndBody.put("http_status", String.valueOf(ex.getRawStatusCode()));
            statusAndBody.put("response_body", ex.getResponseBodyAsString());
            return statusAndBody;
        }

        return getResponse(responseEntity);
    }

    public Map<String, Object> updateOrgMfaStatus(MfaUpdateRequest mfaUpdateRequest, String orgId,
                                                  String hmctsAdmin) {
        ResponseEntity<Map> responseEntity = null;
        String urlPath = "http://localhost:" + prdApiPort + APP_INT_BASE_PATH + "/" + orgId + "/mfa";

        try {
            HttpEntity<?> requestEntity = new HttpEntity<>(
                    isNull(mfaUpdateRequest) ? "{\"mfa\":\"error\"}" : mfaUpdateRequest,
                    getMultipleAuthHeaders(hmctsAdmin));
            responseEntity = restTemplate.exchange(urlPath, HttpMethod.PUT, requestEntity, Map.class);

        } catch (RestClientResponseException ex) {
            HashMap<String, Object> statusAndBody = new HashMap<>();
            statusAndBody.put("http_status", String.valueOf(ex.getRawStatusCode()));
            statusAndBody.put("response_body", ex.getResponseBodyAsString());
            return statusAndBody;
        }

        return getResponse(responseEntity);
    }

    public Map<String, Object> updateOrgMfaStatusUnauthorised(MfaUpdateRequest mfaUpdateRequest, String orgId,
                                                              String hmctsAdmin) {
        ResponseEntity<Map> responseEntity = null;
        String urlPath = "http://localhost:" + prdApiPort + APP_INT_BASE_PATH + "/" + orgId + "/mfa";

        try {
            HttpEntity<MfaUpdateRequest> requestEntity = new HttpEntity<>(mfaUpdateRequest,
                    getInvalidAuthHeaders(hmctsAdmin));
            responseEntity = restTemplate.exchange(urlPath, HttpMethod.PUT, requestEntity, Map.class);

        } catch (RestClientResponseException ex) {
            HashMap<String, Object> statusAndBody = new HashMap<>();
            statusAndBody.put("http_status", String.valueOf(ex.getRawStatusCode()));
            statusAndBody.put("response_body", ex.getResponseBodyAsString());
            return statusAndBody;
        }

        return getResponse(responseEntity);
    }

    public Map<String, Object> findUserStatusByEmail(String email, String role) {
        ResponseEntity<Map> responseEntity = null;
        String urlPath = "http://localhost:" + prdApiPort + APP_EXT_BASE_PATH + "/" + "users/accountId?email=" + email;

        try {
            HttpEntity<?> requestEntity = new HttpEntity<>(getMultipleAuthHeaders(role));
            responseEntity = restTemplate.exchange(urlPath, HttpMethod.GET, requestEntity, Map.class);

        } catch (RestClientResponseException ex) {
            HashMap<String, Object> statusAndBody = new HashMap<>();
            statusAndBody.put("http_status", String.valueOf(ex.getRawStatusCode()));
            statusAndBody.put("response_body", ex.getResponseBodyAsString());
            return statusAndBody;
        }

        return getResponse(responseEntity);
    }

    public Map<String, Object> deleteOrganisation(
            String role, String organisationIdentifier) {

        ResponseEntity<Map> responseEntity = null;
        String urlPath = "http://localhost:" + prdApiPort + APP_INT_BASE_PATH + "/" + organisationIdentifier;
        try {
            HttpEntity<?> requestEntity = new HttpEntity<>(getMultipleAuthHeaders(role));
            responseEntity = restTemplate.exchange(urlPath, HttpMethod.DELETE, requestEntity, Map.class);
        } catch (RestClientResponseException ex) {
            HashMap<String, Object> statusAndBody = new HashMap<>(2);
            statusAndBody.put("http_status", String.valueOf(ex.getRawStatusCode()));
            statusAndBody.put("response_body", ex.getResponseBodyAsString());
            return statusAndBody;
        }

        Map<String, Object> deleteOrganisationResponse = new HashMap<>();
        deleteOrganisationResponse.put("http_status", responseEntity.getStatusCodeValue());
        return deleteOrganisationResponse;
    }

    public Map<String, Object> deleteOrganisationExternal(
            String role, String organisationIdentifier) {

        ResponseEntity<Map> responseEntity = null;
        String urlPath = "http://localhost:" + prdApiPort + APP_EXT_BASE_PATH + "/" + organisationIdentifier;
        try {
            HttpEntity<?> requestEntity = new HttpEntity<>(getMultipleAuthHeaders(role));
            responseEntity = restTemplate.exchange(urlPath, HttpMethod.DELETE, requestEntity, Map.class);
        } catch (RestClientResponseException ex) {
            HashMap<String, Object> statusAndBody = new HashMap<>(2);
            statusAndBody.put("http_status", String.valueOf(ex.getRawStatusCode()));
            statusAndBody.put("response_body", ex.getResponseBodyAsString());
            return statusAndBody;
        }

        Map<String, Object> deleteOrganisationResponse = new HashMap<>();
        deleteOrganisationResponse.put("http_status", responseEntity.getStatusCodeValue());
        return deleteOrganisationResponse;
    }

    public Object findOrganisationsByPbaStatus(String pbaStatus, String role, boolean isUnauthorised)
            throws JsonProcessingException {

        ResponseEntity<Object> responseEntity = null;
        String urlPath = "http://localhost:" + prdApiPort + APP_INT_BASE_PATH + "/pba/" + pbaStatus;

        responseEntity = getRequestForInternalWithGivenResponseType(urlPath, role,
                OrganisationsWithPbaStatusResponse[].class, isUnauthorised);

        HttpStatus status = responseEntity.getStatusCode();
        objectMapper.registerModule(new JavaTimeModule()).disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        if (status.is2xxSuccessful()) {
            return Arrays.asList(objectMapper.convertValue(
                    responseEntity.getBody(), OrganisationsWithPbaStatusResponse[].class));
        } else {
            return getErrorResponseMap(responseEntity, status);
        }
    }

    private Map<String, Object> getErrorResponseMap(ResponseEntity<Object> responseEntity, HttpStatus status)
            throws JsonProcessingException {
        Map<String, Object> errorResponseMap = new HashMap<>();
        String body = (String) responseEntity.getBody();
        if (org.apache.commons.lang.StringUtils.isNotEmpty(body)) {
            errorResponseMap.put("response_body", objectMapper.readValue(
                    body, ErrorResponse.class));
        } else {
            errorResponseMap.put("response_body", null);
        }
        errorResponseMap.put("http_status", status);

        return errorResponseMap;
    }

    private ResponseEntity<Object> getRequestForInternalWithGivenResponseType(
            String uriPath, String role, Class clasz, Boolean isUnauthorised) {

        ResponseEntity<Object> responseEntity;
        try {
            HttpEntity<?> request = new HttpEntity<>(
                    isUnauthorised ? getInvalidAuthHeaders(role) : (getMultipleAuthHeaders(role)));
            responseEntity = restTemplate.exchange(uriPath, HttpMethod.GET, request, clasz);
        } catch (HttpStatusCodeException ex) {
            return ResponseEntity.status(ex.getRawStatusCode()).body(ex.getResponseBodyAsString());
        }
        return responseEntity;
    }

    public Map<String, Object> addPaymentsAccountsByOrgId(PbaRequest pbaRequest, String supportedRole,
                                                          String userId) {
        ResponseEntity<Map> responseEntity = null;
        String urlPath = "http://localhost:" + prdApiPort + APP_EXT_BASE_PATH + "/pba";

        try {
            HttpEntity<PbaRequest> requestEntity = new HttpEntity<>(pbaRequest,
                    getMultipleAuthHeaders(supportedRole, userId));
            responseEntity = restTemplate.exchange(urlPath, HttpMethod.POST, requestEntity, Map.class);

        } catch (RestClientResponseException ex) {
            HashMap<String, Object> statusAndBody = new HashMap<>();
            statusAndBody.put("http_status", String.valueOf(ex.getRawStatusCode()));
            statusAndBody.put("response_body", ex.getResponseBodyAsString());
            return statusAndBody;
        }

        return getResponse(responseEntity);
    }

    public Map<String, Object> addContactInformationsToOrganisation(
            List<ContactInformationCreationRequest> contactInformationCreationRequests,
            String supportedRole, String userId) {

        StringBuilder addContactsInfoURL = new StringBuilder(baseUrl);
        addContactsInfoURL.append("/").append("addresses");

        return postRequest(addContactsInfoURL.toString(), contactInformationCreationRequests, supportedRole, userId);
    }

    public Map<String, Object> deleteContactInformationAddressOfOrganisation(
            List<DeleteMultipleAddressRequest> deleteRequest, String supportedRole, String userId) {
        ResponseEntity<Map> responseEntity = null;
        var urlPath = "http://localhost:" + prdApiPort + APP_EXT_BASE_PATH + "/addresses";

        try {
            HttpEntity<List<DeleteMultipleAddressRequest>> requestEntity = new HttpEntity<>(deleteRequest,
                    getMultipleAuthHeaders(supportedRole, userId));
            responseEntity = restTemplate.exchange(urlPath, HttpMethod.DELETE, requestEntity, Map.class);

        } catch (RestClientResponseException ex) {
            var statusAndBody = new HashMap<String, Object>();
            statusAndBody.put("http_status", String.valueOf(ex.getRawStatusCode()));
            statusAndBody.put("response_body", ex.getResponseBodyAsString());
            return statusAndBody;
        }

        return getResponse(responseEntity);
    }

    public Map<String, Object> findOrganisationsByUserId(String userId, String role) {
        ResponseEntity<Map> responseEntity;

        try {
            String urlPath = "http://localhost:" + prdApiPort + APP_INT_BASE_PATH + "/orgDetails/" + userId;

            HttpEntity<?> request = new HttpEntity<>(getMultipleAuthHeaders(role, userId));
            responseEntity = restTemplate
                    .exchange(urlPath,
                            HttpMethod.GET,
                            request,
                            Map.class);
        } catch (HttpStatusCodeException ex) {
            HashMap<String, Object> statusAndBody = new HashMap<>(2);
            statusAndBody.put("http_status", String.valueOf(ex.getRawStatusCode()));
            statusAndBody.put("response_body", ex.getResponseBodyAsString());
            return statusAndBody;
        }
        return getResponse(responseEntity);
    }


    public Map<String, Object> updateOrgNameSraIdStatus(
        OrganisationCreationRequest organisationCreationRequest, String role, String organisationIdentifier) {

        ResponseEntity<Map> responseEntity = null;
        String urlPath = "http://localhost:" + prdApiPort + APP_INT_BASE_PATH + "/nameSra/" + organisationIdentifier;
        try {
            HttpEntity<OrganisationCreationRequest> requestEntity = new HttpEntity<>(organisationCreationRequest,
                getMultipleAuthHeaders(role));
            responseEntity = restTemplate.exchange(urlPath, HttpMethod.PUT, requestEntity, Map.class);
        } catch (RestClientResponseException ex) {
            HashMap<String, Object> statusAndBody = new HashMap<>(2);
            statusAndBody.put("http_status", String.valueOf(ex.getRawStatusCode()));
            statusAndBody.put("response_body", ex.getResponseBodyAsString());
            return statusAndBody;
        }

        Map<String, Object> organisationResponse = new HashMap<>();
        organisationResponse.put("http_status", responseEntity.getStatusCodeValue());
        return organisationResponse;
    }
}
