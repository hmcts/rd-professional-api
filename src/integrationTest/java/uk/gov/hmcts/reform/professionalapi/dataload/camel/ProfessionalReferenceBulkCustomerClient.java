package uk.gov.hmcts.reform.professionalapi.dataload.camel;

import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.professionalapi.util.JwtTokenUtil.generateToken;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.TextCodec;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.professionalapi.controller.advice.ErrorResponse;
import uk.gov.hmcts.reform.professionalapi.controller.request.BulkCustomerRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.MfaUpdateRequest;
import uk.gov.hmcts.reform.professionalapi.util.JwtDecoderMockBuilder;

@Slf4j
@PropertySource(value = "/integrationTest/resources/application.yml")
@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
public class ProfessionalReferenceBulkCustomerClient {

    private static final String APP_EXT_BASE_PATH = "/refdata/external/v1/organisations";

    private static final String APP_EXT_V2_BASE_PATH = "/refdata/external/v2/organisations";
    private static final String APP_INT_V2_BASE_PATH = "/refdata/internal/v2/organisations";
    private static final String APP_INT_BASE_PATH = "/refdata/internal/v1/organisations";
    private static final String APP_INT_BULK_PATH = "/refdata/internal/v1/bulkCustomer";
    private final Integer prdApiPort;
    static String bearerToken;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate = new RestTemplate();
    private String baseUrl;
    private String baseIntUrl;
    private static String JWT_TOKENP = null;

    private String bulkUri;

    private String baseV2Url;

    private String issuer;
    private long expiration;

    public Map<String, String> bearerTokenMap = new HashMap<>();


    public ProfessionalReferenceBulkCustomerClient(int port, String issuer, Long tokenExpirationInterval) {
        this.prdApiPort = port;
        this.baseUrl = "http://localhost:" + prdApiPort + APP_EXT_BASE_PATH;
        this.baseIntUrl = "http://localhost:" + prdApiPort + APP_INT_BASE_PATH;
        this.bulkUri= "http://localhost:" + prdApiPort + APP_INT_BULK_PATH;
        this.baseV2Url = "http://localhost:" + prdApiPort + APP_EXT_V2_BASE_PATH;
        this.issuer = issuer;
        this.expiration = tokenExpirationInterval;
    }

    public String setAndReturnJwtToken() {
        if (StringUtils.isBlank(JWT_TOKENP)) {
            JWT_TOKENP = generateS2SToken("rd_professional_api");
        }
        return JWT_TOKENP;
    }

    public static String generateS2SToken(String serviceName) {
        return Jwts.builder()
            .setSubject(serviceName)
            .setIssuedAt(new Date())
            .signWith(SignatureAlgorithm.HS256, TextCodec.BASE64.encode("AA"))
            .compact();
    }


    public Map<String, Object> fetchBulkCustomerDetails( BulkCustomerRequest bulkCustomerRequest,
                                                     String role,String userId) {
        return postRequestForBulk(bulkUri, bulkCustomerRequest, role, userId);
    }

    private Map<String, Object> postRequestForBulk(String uriPath,
                                                       BulkCustomerRequest bulkCustomerRequest, String role, String userId) {

        ResponseEntity<Object> responseEntity;
        var request =
            new HttpEntity<Object>(bulkCustomerRequest,
                getMultipleAuthHeaders(APPLICATION_JSON_VALUE,role, null));

        try {
            responseEntity = restTemplate.exchange(uriPath, HttpMethod.POST, request, Object.class
            );

        } catch (RestClientResponseException ex) {
            var statusAndBody = new HashMap<String, Object>(2);
            statusAndBody.put("http_status", String.valueOf(ex.getRawStatusCode()));
            statusAndBody.put("response_body", ex.getResponseBodyAsString());
            return statusAndBody;
        }

        return getResponse(responseEntity);
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

    public String getAndReturnBearerTokenBulk(String userId, String role) {
        setAndReturnJwtToken();
        if (StringUtils.isBlank(bearerToken)) {
            bearerToken = "Bearer ".concat(getBearerToken(Objects.isNull(userId) ? UUID.randomUUID().toString()
                : userId, role));
        }
        return bearerToken;
    }

    @NotNull
    private HttpHeaders getMultipleAuthHeaders(String value, String role, String userId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.valueOf(value));
        if (StringUtils.isBlank(JWT_TOKENP)) {

            JWT_TOKENP = generateS2SToken("rd_professional_api");
        }
        getAndReturnBearerTokenBulk(userId, role);

        headers.add("ServiceAuthorization", JWT_TOKENP);

        headers.add("Authorization", bearerToken);
        return headers;
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
        headers.add("ServiceAuthorization", JWT_TOKENP);
        return headers;
    }

    private Map getResponse(ResponseEntity<Object> responseEntity) {

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

    public void clearTokens() {
        JWT_TOKENP = null;
        bearerToken = null;
    }
}
