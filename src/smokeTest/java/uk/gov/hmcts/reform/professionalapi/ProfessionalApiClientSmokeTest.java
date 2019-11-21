package uk.gov.hmcts.reform.professionalapi;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import lombok.extern.slf4j.Slf4j;
import net.serenitybdd.rest.SerenityRest;

@Slf4j
public class ProfessionalApiClientSmokeTest {

    private static final ObjectMapper mapper = new ObjectMapper();

    private static final String SERVICE_HEADER = "ServiceAuthorization";
    private static final String AUTHORIZATION_HEADER = "Authorization";

    private final String professionalApiUrl;
    private final String s2sToken;

    protected IdamOpenIdClientSmokeTest idamOpenIdClient;


    public ProfessionalApiClientSmokeTest(
            String professionalApiUrl,
            String s2sToken, IdamOpenIdClientSmokeTest idamOpenIdClient) {
        this.professionalApiUrl = professionalApiUrl;
        this.s2sToken = s2sToken;
        this.idamOpenIdClient = idamOpenIdClient;

    }

    private RequestSpecification getMultipleAuthHeadersInternal() {
        return getMultipleAuthHeaders(idamOpenIdClient.getInternalOpenIdToken());
    }

    @SuppressWarnings("unchecked")
    public void  retrieveOrganisationDetails(String id, String role) {
        Response response = getMultipleAuthHeadersInternal()
                .body("")
                .get("/refdata/internal/v1/organisations?id=" + id)
                .andReturn();

        if (response.statusCode() != INTERNAL_SERVER_ERROR.value()) {
            log.info("Retrieve organisation response: " + response.asString());
        }

    }


    public RequestSpecification getMultipleAuthHeaders(String userToken) {
        return SerenityRest.with()
                .relaxedHTTPSValidation()
                .baseUri(professionalApiUrl)
                .header("Content-Type", APPLICATION_JSON_UTF8_VALUE)
                .header("Accepts", APPLICATION_JSON_UTF8_VALUE)
                .header(SERVICE_HEADER, "Bearer " + s2sToken)
                .header(AUTHORIZATION_HEADER, "Bearer " + userToken);
    }

}