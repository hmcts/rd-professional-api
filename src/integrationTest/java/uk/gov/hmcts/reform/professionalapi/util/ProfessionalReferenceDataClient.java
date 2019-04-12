package uk.gov.hmcts.reform.professionalapi.util;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import uk.gov.hmcts.reform.professionalapi.infrastructure.controllers.request.OrganisationCreationRequest;

public class ProfessionalReferenceDataClient {

    private static final String APP_BASE_PATH = "/organisations";
    private static final String JWT_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";

    private final int prdApiPort;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate = new RestTemplate();

    public ProfessionalReferenceDataClient(int prdApiPort) {
        this.prdApiPort = prdApiPort;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public Map<String, Object> createOrganisation(
            OrganisationCreationRequest organisationCreationRequest) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
        headers.add("ServiceAuthorization", JWT_TOKEN);

        HttpEntity<OrganisationCreationRequest> request =
                new HttpEntity<>(organisationCreationRequest, headers);

        ResponseEntity<Map> responseEntity;

        try {

            responseEntity = restTemplate.postForEntity(
                    "http://localhost:" + prdApiPort + APP_BASE_PATH,
                    request,
                    Map.class);

        } catch (HttpClientErrorException ex) {
            HashMap<String, Object> statusAndBody = new HashMap<>(2);
            statusAndBody.put("http_status", String.valueOf(ex.getRawStatusCode()));
            statusAndBody.put("response_body", ex.getResponseBodyAsString());
            return statusAndBody;
        }

        Map organisationResponse = objectMapper.convertValue(
                responseEntity.getBody(),
                Map.class);

        organisationResponse.put("http_status", responseEntity.getStatusCode().toString());

        return organisationResponse;
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public Map<String, Object> findUserByEmail(String email) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
        headers.add("ServiceAuthorization", JWT_TOKEN);

        ResponseEntity<Map> responseEntity;

        try {
            HttpEntity<?> request = new HttpEntity<>(headers);
            responseEntity = restTemplate
                    .exchange("http://localhost:" + prdApiPort + "/search/user/" + email,
                              HttpMethod.GET,
                              request,
                              Map.class);
        } catch (HttpClientErrorException ex) {
            HashMap<String, Object> statusAndBody = new HashMap<>(2);
            statusAndBody.put("http_status", String.valueOf(ex.getRawStatusCode()));
            statusAndBody.put("response_body", ex.getResponseBodyAsString());
            return statusAndBody;
        }

        Map organisationResponse = objectMapper
                .convertValue(
                        responseEntity.getBody(),
                        Map.class);

        organisationResponse.put("http_status", responseEntity.getStatusCode().toString());

        return organisationResponse;
    }

}
