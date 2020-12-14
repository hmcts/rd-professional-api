package uk.gov.hmcts.reform.professionalapi.sample;

import static org.assertj.core.api.Assertions.assertThat;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.DslPart;
import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import com.google.common.collect.Maps;
import groovy.util.logging.Slf4j;
import java.util.Map;
import java.util.TreeMap;

import net.serenitybdd.rest.SerenityRest;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@Slf4j
@ExtendWith(PactConsumerTestExt.class)
@ExtendWith(SpringExtension.class)
public class IdamConsumerTest {

    private static final String IDAM_USERINFO_URL = "/o/userinfo";

    @Pact(provider = "Idam_api", consumer = "rd_professional_api")
    public RequestResponsePact executeGetUserInfoDetailsAndGet200(PactDslWithProvider builder) {

        Map<String, String> requestHeaders = Maps.newHashMap();
        requestHeaders.put(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        Map<String, Object> params = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

        params.put("redirect_uri", "http://www.dummy-pact-service.com/callback");
        params.put("client_id", "pact");
        params.put("client_secret", "pactsecret");
        params.put("scope", "openid profile roles");
        params.put("username", "prdadmin@email.net");
        params.put("password", "Password123");

        Map<String, String> responseheaders = Maps.newHashMap();
        responseheaders.put("Content-Type", "application/json");

        return builder
                .given("I have obtained an access_token as a user",params)
                .uponReceiving("Provider returns user info to RD-PROFESSIONAL-API")
                .path(IDAM_USERINFO_URL)
                .headers("Authorization","Bearer eyJ0eXAiOiJKV1QiLCJraWQiOiJiL082T3ZWdeRre")
                .method(HttpMethod.GET.toString())
                .willRespondWith()
                .status(HttpStatus.OK.value())
                .headers(responseheaders)
                .body(createUserInfoResponse())
                .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "executeGetUserInfoDetailsAndGet200")
    public void should_get_user_info_details_with_access_token(MockServer mockServer) throws JSONException {

        Map<String, String> headers = Maps.newHashMap();
        headers.put(HttpHeaders.AUTHORIZATION, "Bearer eyJ0eXAiOiJKV1QiLCJraWQiOiJiL082T3ZWdeRre");
        headers.put(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        String detailsResponseBody =
                SerenityRest
                        .given()
                        .headers(headers)
                        .when()
                        .get(mockServer.getUrl() + IDAM_USERINFO_URL)
                        .then()
                        .statusCode(200)
                        .and()
                        .extract()
                        .body()
                        .asString();

        JSONObject response = new JSONObject(detailsResponseBody);

        assertThat(detailsResponseBody).isNotNull();
        assertThat(response).hasNoNullFieldsOrProperties();
        assertThat(response.getString("uid")).isNotBlank();
        assertThat(response.getString("given_name")).isNotBlank();
        assertThat(response.getString("family_name")).isNotBlank();
        JSONArray rolesArr = response.getJSONArray("roles");
        assertThat(rolesArr).isNotNull();
        assertThat(rolesArr.length()).isNotZero();
        assertThat(rolesArr.get(0).toString()).isNotBlank();

    }

    private DslPart createUserInfoResponse() {

        return new PactDslJsonBody()
                .stringType("uid", "1111-2222-3333-4567")
                .stringType("given_name", "puiCaseManager")
                .stringType("family_name", "Jar")
                .array("roles")
                .stringType("prd-admin")
                .stringType("IDAM_ADMIN_USER")
                .closeArray();

    }

}