package uk.gov.hmcts.reform.professionalapi.util;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.client.InMemoryOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.AuthenticatedPrincipalOAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import uk.gov.hmcts.reform.authorisation.filters.ServiceAuthFilter;


@TestConfiguration
public class TestIdamSecurityConfiguration  {


    private final ClientRegistration clientRegistration;

    public TestIdamSecurityConfiguration() {
        this.clientRegistration = clientRegistration().build();
    }

    @MockBean
    ServiceAuthFilter serviceAuthFilter;

    @MockBean
    private JwtDecoder jwtDecoder;

    @Bean
    ClientRegistrationRepository clientRegistrationRepository() {
        return new InMemoryClientRegistrationRepository(clientRegistration);
    }

    private ClientRegistration.Builder clientRegistration() {

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("end_session_endpoint", "https://jhipster.org/logout");

        return ClientRegistration.withRegistrationId("oidc")
                    .redirectUriTemplate("{baseUrl}/{action}/oauth2/code/{registrationId}")
                    .clientAuthenticationMethod(ClientAuthenticationMethod.BASIC)
                    .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                    .scope("read:user")
                    .authorizationUri("https://jhipster.org/login/oauth/authorize")
                    .tokenUri("https://jhipster.org/login/oauth/access_token")
                    .jwkSetUri("https://jhipster.org/oauth/jwk")
                    .userInfoUri("https://api.jhipster.org/user")
                    .providerConfigurationMetadata(metadata)
                    .userNameAttributeName("id")
                    .clientName("Client Name")
                    .clientId("client-id")
                    .clientSecret("client-secret");
    }

    @Bean
    public OAuth2AuthorizedClientService authorizedClientService(ClientRegistrationRepository clientRegistrationRepository) {
        return new InMemoryOAuth2AuthorizedClientService(clientRegistrationRepository);
    }

    @Bean
    public OAuth2AuthorizedClientRepository authorizedClientRepository(OAuth2AuthorizedClientService authorizedClientService) {
        return new AuthenticatedPrincipalOAuth2AuthorizedClientRepository(authorizedClientService);
    }
}



