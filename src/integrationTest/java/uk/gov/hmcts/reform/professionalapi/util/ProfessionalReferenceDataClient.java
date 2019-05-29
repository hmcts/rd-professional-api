package uk.gov.hmcts.reform.professionalapi.util;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import uk.gov.hmcts.reform.professionalapi.controller.request.NewUserCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.response.OrganisationResponse;

public class ProfessionalReferenceDataClient {

    private static final String APP_BASE_PATH = "v1/organisations";
    private static final String JWT_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";

    private final int prdApiPort;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate = new RestTemplate();
    private String baseUrl;

    public ProfessionalReferenceDataClient(int prdApiPort) {
        this.prdApiPort = prdApiPort;
        this.baseUrl = "http://localhost:" + prdApiPort + APP_BASE_PATH;
    }

    public Map<String, Object> createOrganisation(OrganisationCreationRequest request) {
        return postRequest(baseUrl, request);
    }

    public Map<String, Object> findUserByEmail(String email) {
        return getRequest("/v1/organisations/users?email={email}", email);
    }

    public Map<String, Object> findPaymentAccountsByEmail(String email) {
        return getRequest("/v1/organisations/pbas?email={email}", email);
    }

    public Map<String, Object> findLegacyPbaAccountsByUserEmail(String email) {
        return getRequest("/search/pba/{email}", email);
    }

    public Map<String,Object> retrieveSingleOrganisation(String id) {
        return getRequest(APP_BASE_PATH + "?id={id}", id);
    }

    public Map<String,Object> retrieveAllOrganisations() {
        return getRequest(APP_BASE_PATH);
    }

    public Map<String,Object> retrieveAllOrganisationDetailsByStatusTest(String status) {
        return getRequest("/v1/organisations?status={status}", status);
    }

    public Map<String, Object> addUserToOrganisation(String orgId, NewUserCreationRequest userCreationRequest ) {
        return postRequest( baseUrl + "/" + orgId + "/users/", userCreationRequest);
    }

    public Map<String, Object> findUsersByOrganisation(String organisationIdentifier, String showDeleted) {
        return getRequest("/" + APP_BASE_PATH + "/" + organisationIdentifier + "/users?showDeleted={showDeleted}", showDeleted);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private <T> Map<String, Object> postRequest(String uriPath, T requestBody, Object... params) {

        HttpEntity<T> request =
                new HttpEntity<>(requestBody, getHeaders());

        ResponseEntity<Map> responseEntity;

        try {

            responseEntity = restTemplate.postForEntity(
                    baseUrl + uriPath,
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

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private Map<String, Object> getRequest(String uriPath, Object... params) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
        headers.add("ServiceAuthorization", JWT_TOKEN);

        ResponseEntity<Map> responseEntity;

        try {
            HttpEntity<?> request = new HttpEntity<>(getHeaders());
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
        OrganisationCreationRequest organisationCreationRequest, String organisationIdentifier) {

        ResponseEntity<OrganisationResponse> responseEntity = null;
        String urlPath = "http://localhost:" + prdApiPort + APP_BASE_PATH + "/" + organisationIdentifier;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
        headers.add("ServiceAuthorization", JWT_TOKEN);

        try {
            HttpEntity<OrganisationCreationRequest> requestEntity = new HttpEntity<>(organisationCreationRequest,headers);
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

    private HttpHeaders getHeaders() {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
        headers.add("ServiceAuthorization", JWT_TOKEN);

        return headers;
    }

    private Map getResponse(ResponseEntity<Map> responseEntity) {

        Map response = objectMapper
                .convertValue(
                        responseEntity.getBody(),
                        Map.class);

        response.put("http_status", responseEntity.getStatusCode().toString());

        return response;
    }
}
