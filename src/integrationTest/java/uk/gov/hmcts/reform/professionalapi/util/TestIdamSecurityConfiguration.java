/*
package uk.gov.hmcts.reform.professionalapi.util;

import org.junit.Ignore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.web.context.ContextCleanupListener;


@Configuration
public class TestIdamSecurityConfiguration extends ContextCleanupListener {


    @Bean
    // Overriding as OAuth2ClientRegistrationRepositoryConfiguration loading before wire-mock mappings for /o/.well-known/openid-configuration
    public ClientRegistrationRepository clientRegistrationRepository() {
        //setUpClient();
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

        */
/* stubFor(get(urlPathMatching("/o/.well-known/openid-configuration"))
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
                                + "  \"issuer\": \"http://127.0.0.1:5000/o\","
                                + "  \"jwks_uri\": \"http://127.0.0.1:5000/jwks\" "
                                + "}]}}")));
        *//*


    }

}



*/
