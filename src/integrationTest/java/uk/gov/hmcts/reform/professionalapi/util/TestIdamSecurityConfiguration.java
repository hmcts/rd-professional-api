package uk.gov.hmcts.reform.professionalapi.util;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static java.lang.String.format;
import static uk.gov.hmcts.reform.professionalapi.util.JwtTokenUtil.decodeJwtToken;
import static uk.gov.hmcts.reform.professionalapi.util.JwtTokenUtil.getUserIdAndRoleFromToken;
import static uk.gov.hmcts.reform.professionalapi.util.KeyGenUtil.getDynamicJwksResponse;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.extension.ResponseTransformer;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.Response;
import com.nimbusds.jose.JOSEException;
import java.util.LinkedList;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.context.ContextCleanupListener;


@Configuration
@TestPropertySource(properties = {"IDAM_URL:http://127.0.0.1:5000", "OPEN_ID_API_BASE_URI:http://0.0.0.0:6000/o"})
public class TestIdamSecurityConfiguration extends ContextCleanupListener {

    public static WireMockServer mockHttpServerForOidc = new WireMockServer(wireMockConfig().port(6000));

    public static WireMockServer mockHttpServerForSidam = new WireMockServer(wireMockConfig().port(5000).extensions(ExternalTransformer.class));


    @Bean
    public ClientRegistrationRepository clientRegistrationRepository() throws JsonProcessingException, JOSEException {
        setUpMockServiceForOidc();
        return new InMemoryClientRegistrationRepository(clientRegistration());
    }

    private ClientRegistration clientRegistration() {
        return ClientRegistration.withRegistrationId("oidc")
                .redirectUriTemplate("{baseUrl}/{action}/oauth2/code/{registrationId}")
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .scope("read:user")
                .authorizationUri("http://0.0.0.0:6000/o/authorize")
                .tokenUri("http://0.0.0.0:6000/o/access_token")
                .userInfoUri("http://0.0.0.0:6000/o/userinfo")
                .userNameAttributeName("id")
                .clientName("Client Name")
                .clientId("client-id")
                .clientSecret("client-secret")
                .build();
    }


    public void setUpMockServiceForOidc() throws JsonProcessingException, JOSEException {

        mockHttpServerForOidc.stubFor(get(urlPathMatching("/o/.well-known/openid-configuration"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(
                                "  {"
                                + "  \"issuer\": \"http://0.0.0.0:6000/o\","
                                + "  \"jwks_uri\": \"http://0.0.0.0:6000/jwks\" "
                                + "}")));


        mockHttpServerForOidc.stubFor(get(urlPathMatching("/jwks"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(getDynamicJwksResponse())));


        mockHttpServerForSidam.stubFor(get(urlPathMatching("/o/userinfo"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{"
                                +  "  \"id\": \"%s\","
                                +  "  \"uid\": \"%s\","
                                +  "  \"forename\": \"Super\","
                                +  "  \"surname\": \"User\","
                                +  "  \"email\": \"super.user@hmcts.net\","
                                +  "  \"accountStatus\": \"active\","
                                +  "  \"roles\": ["
                                +  "  \"%s\""
                                +  "  ]"
                                +  "}")
                        .withTransformers("external_user-token-response")));

        startWireMockService(mockHttpServerForOidc);
        startWireMockService(mockHttpServerForSidam);

    }

    public static class ExternalTransformer extends ResponseTransformer {
        @Override
        public Response transform(Request request, Response response, FileSource files, Parameters parameters) {

            String formatResponse = response.getBodyAsString();

            String token = request.getHeader("Authorization");
            String tokenBody = decodeJwtToken(token.split(" ")[1]);
            LinkedList tokenInfo = getUserIdAndRoleFromToken(tokenBody);
            formatResponse = format(formatResponse, tokenInfo.get(1), tokenInfo.get(1), tokenInfo.get(0));

            return Response.Builder.like(response)
                    .but().body(formatResponse)
                    .build();
        }

        @Override
        public String getName() {
            return "external_user-token-response";
        }

        public boolean applyGlobally() {
            return false;
        }
    }


    public void startWireMockService(WireMockServer service) {
        if (!service.isRunning()) {
            service.start();
        }
    }

}




