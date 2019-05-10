package uk.gov.hmcts.reform.professionalapi.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.*;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.response.OrganisationResponse;

import java.util.HashMap;
import java.util.Map;

public class ProfessionalReferenceDataClient {

    private static final String APP_BASE_PATH = "/organisations";
    private static final String JWT_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";

    private final int prdApiPort;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate = new RestTemplate();

    public ProfessionalReferenceDataClient(int prdApiPort) {
        this.prdApiPort = prdApiPort;
    }

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

        } catch (RestClientResponseException ex) {
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

    public Map<String, Object> updateOrganisation(
            OrganisationCreationRequest organisationCreationRequest, String organisationIdentifier ) {

        ResponseEntity<OrganisationResponse> responseEntity = null;
        String urlPath = "http://localhost:" + prdApiPort + APP_BASE_PATH+"/"+organisationIdentifier;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
        headers.add("ServiceAuthorization", JWT_TOKEN);

        try {
            System.out.println("Into Client :  urlPath :" + urlPath );
            HttpEntity<OrganisationCreationRequest> requestEntity = new HttpEntity<OrganisationCreationRequest>(organisationCreationRequest,headers);
            responseEntity = restTemplate.exchange(urlPath, HttpMethod.PUT, requestEntity, OrganisationResponse.class);
        }
        catch (RestClientResponseException ex) {
            HashMap<String, Object> statusAndBody = new HashMap<>(2);
            statusAndBody.put("http_status", String.valueOf(ex.getRawStatusCode()));
            statusAndBody.put("response_body", ex.getResponseBodyAsString());
            return statusAndBody;
        }

        Map organisationResponse = new HashMap<String, Object>();
        organisationResponse.put("http_status", responseEntity.getStatusCodeValue());
        return organisationResponse;
    }

}
