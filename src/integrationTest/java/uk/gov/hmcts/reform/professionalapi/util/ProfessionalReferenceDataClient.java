package uk.gov.hmcts.reform.professionalapi.util;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static uk.gov.hmcts.reform.professionalapi.util.JwtTokenUtil.generateToken;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.professionalapi.controller.advice.ErrorResponse;
import uk.gov.hmcts.reform.professionalapi.controller.request.NewUserCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.PbaEditRequest;
import uk.gov.hmcts.reform.professionalapi.controller.response.OrganisationMinimalInfoResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.OrganisationResponse;
import uk.gov.hmcts.reform.professionalapi.domain.UserProfileUpdatedData;

@Slf4j
@PropertySource(value = "/integrationTest/resources/application.yml")
public class ProfessionalReferenceDataClient {

    private static final String APP_EXT_BASE_PATH = "/refdata/external/v1/organisations";
    private static final String APP_INT_BASE_PATH = "/refdata/internal/v1/organisations";
    private static final String JWT_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI"
            + "6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";
    private final Integer prdApiPort;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate = new RestTemplate();
    private String baseUrl;
    private String baseIntUrl;
    private String issuer;
    private long expiration;

    public ProfessionalReferenceDataClient(int port, String issuer, Long tokenExpirationInterval) {
        this.prdApiPort = port;
        this.baseUrl = "http://localhost:" + prdApiPort + APP_EXT_BASE_PATH;
        this.baseIntUrl = "http://localhost:" + prdApiPort + APP_INT_BASE_PATH;
        this.issuer = issuer;
        this.expiration = tokenExpirationInterval;
    }

    public Map<String, Object> createOrganisation(OrganisationCreationRequest request) {
        return postRequest(baseUrl, request, null, null);
    }


    public Map<String, Object> findPaymentAccountsByEmail(String email, String role) {
        return getRequest("/refdata/internal/v1/organisations" + "/pbas?email={email}", role, email);
    }

    public Map<String, Object> findPaymentAccountsByEmailFromHeader(String email, String role) {
        return getRequestToGetEmailFromHeader("/refdata/internal/v1/organisations" + "/pbas?email={email}",
                role, "", email);
    }

    public Map<String, Object> findPaymentAccountsByEmailFromHeaderForExternalUsers(String email, String role,
                                                                                    String userId) {
        return getRequestToGetEmailFromHeader("/refdata/external/v1/organisations" + "/pbas?email={email}",
                role, userId, email);
    }

    public Map<String, Object> retrieveSingleOrganisation(String id, String role) {
        return getRequest(APP_INT_BASE_PATH + "?id={id}", role, id);
    }

    public Map<String, Object> retrieveExternalOrganisation(String id, String role) {
        return getRequestForExternal(APP_EXT_BASE_PATH, role, id);
    }

    public Map<String, Object> retrieveAllOrganisations(String role) {
        return getRequest(APP_INT_BASE_PATH + "/", role);
    }

    public Object retrieveOrganisationsWithMinimalInfo(String id, String role, String orgStatus,
                                                       Boolean address, Class expectedClass)
            throws JsonProcessingException {
        ResponseEntity<Object> responseEntity = getRequestForExternalWithGivenResponseType(
                APP_EXT_BASE_PATH + "/status/" + orgStatus + "?address=" + address, role, id, expectedClass);
        HttpStatus status = responseEntity.getStatusCode();
        if (status.is2xxSuccessful()) {
            return Arrays.asList((OrganisationMinimalInfoResponse[]) objectMapper.convertValue(
                    responseEntity.getBody(), expectedClass));
        } else {
            Map<String, Object> errorResponseMap = new HashMap<>();
            errorResponseMap.put("response_body",  objectMapper.readValue(
                    responseEntity.getBody().toString(), ErrorResponse.class));
            errorResponseMap.put("http_status", status);
            return errorResponseMap;
        }
    }

    public Map<String,Object> retrieveAllOrganisationDetailsByStatusTest(String status, String role) {
        return getRequest(APP_INT_BASE_PATH + "?status={status}", role, status);
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
    private Map<String, Object> getRequestToGetEmailFromHeader(String uriPath, String role,String userId,
                                                               Object... params) {

        ResponseEntity<Map> responseEntity;
        HttpHeaders httpHeaders = null;
        try {
            if (StringUtils.isEmpty(userId)) {
                httpHeaders = getMultipleAuthHeaders(role);
            } else {
                httpHeaders = getMultipleAuthHeaders(role, userId);
            }
            httpHeaders.add("User-Email", params[0].toString());
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
            String uriPath,String role, String userId, Object... params) {

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

    private HttpHeaders getMultipleAuthHeaders(String role, String userId) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(APPLICATION_JSON);

        headers.add("ServiceAuthorization", JWT_TOKEN);

        String bearerToken = "Bearer ".concat(getBearerToken(Objects.isNull(userId) ? UUID.randomUUID().toString()
                : userId, role));
        headers.add("Authorization", bearerToken);

        return headers;
    }

    private HttpHeaders getMultipleAuthHeaders(String role) {

        return getMultipleAuthHeaders(role, null);
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

        Map response = objectMapper
                .convertValue(
                        responseEntity.getBody(),
                        Map.class);

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

    public Map<String, Object> editPaymentsAccountsByOrgId(PbaEditRequest pbaEditRequest, String orgId,
                                                           String hmctsAdmin) {
        ResponseEntity<Map> responseEntity = null;
        String urlPath = "http://localhost:" + prdApiPort + APP_INT_BASE_PATH + "/" + orgId + "/pbas";

        try {
            HttpEntity<PbaEditRequest> requestEntity = new HttpEntity<>(pbaEditRequest,
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
}
