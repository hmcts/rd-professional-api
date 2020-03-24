package uk.gov.hmcts.reform.professionalapi.authchecker.servicetokenparser;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import uk.gov.hmcts.reform.professionalapi.authchecker.core.exception.ServiceTokenInvalidException;
import uk.gov.hmcts.reform.professionalapi.authchecker.core.exception.ServiceTokenParsingException;

public class HttpComponentsBasedServiceTokenParser implements ServiceTokenParser {

    private final HttpClient httpClient;
    private final String baseUrl;

    public HttpComponentsBasedServiceTokenParser(HttpClient httpClient, String baseUrl) {
        this.httpClient = httpClient;
        this.baseUrl = baseUrl;
    }


    public String parse(String jwt) {
        try {
            String bearerJwt = jwt.startsWith("Bearer ") ? jwt : "Bearer " + jwt;
            HttpGet request = new HttpGet(baseUrl + "/details");
            request.addHeader("Authorization", bearerJwt);

            return httpClient.execute(request, httpResponse -> {
                checkStatusIs2xx(httpResponse);
                return EntityUtils.toString(httpResponse.getEntity());
            });
        } catch (IOException e) {
            throw new ServiceTokenParsingException(e);
        }
    }

    private void checkStatusIs2xx(HttpResponse httpResponse) throws IOException {
        int status = httpResponse.getStatusLine().getStatusCode();

        if (status == 401) {
            throw new ServiceTokenInvalidException();
        }

        if (status < 200 || status >= 300) {
            throw new ClientProtocolException("Unexpected response status: " + status);
        }
    }
}

