package uk.gov.hmcts.reform.professionalapi.util;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;

import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit.WireMockRule;

import org.junit.ClassRule;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.web.context.ContextCleanupListener;



@Configuration
public class TestIdamSecurityConfiguration extends ContextCleanupListener {

    @Value("classpath:_idam_default_details.json")
    protected Resource resourceJwksFile;

    protected String jwksResponse = "";

    @ClassRule
    public static WireMockRule sidamService = new WireMockRule(WireMockConfiguration.options().port(5000));


    @Bean
    // Overriding as OAuth2ClientRegistrationRepositoryConfiguration loading before wire-mock mappings for /o/.well-known/openid-configuration
    public ClientRegistrationRepository clientRegistrationRepository() {
        setUpClient();
        return new InMemoryClientRegistrationRepository(clientRegistration());
    }

    private ClientRegistration clientRegistration() {
        return ClientRegistration.withRegistrationId("oidc")
                .redirectUriTemplate("{baseUrl}/{action}/oauth2/code/{registrationId}")
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .scope("read:user")
                .authorizationUri("http://127.0.0.1:5000/o/authorize")
                .tokenUri("http://127.0.0.1:5000/o/access_token")
                .userInfoUri("http://127.0.0.1:5000/o/userinfo")
                .userNameAttributeName("id")
                .clientName("Client Name")
                .clientId("client-id")
                .clientSecret("client-secret")
                .build();
    }


    public void setUpClient() {

        sidamService.stubFor(get(urlPathMatching("/o/.well-known/openid-configuration"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{"
                                + " \"response\":{"
                                + "  \"status\":200"
                                + "  \"headers\":{"
                                + "  \"issuer\": \"application/json"
                                + "},"
                                + "  \"jsonBody\": ["
                                + "  {"
                                + "  \"issuer\": \"http://localhost:5000/o\","
                                + "  \"jwks_uri\": \"http://localhost:5000/jwks\" "
                                + "}]}}")));


    }

}



