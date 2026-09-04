package uk.gov.hmcts.reform.professionalapi.configuration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.authorisation.filters.ServiceAuthFilter;
import uk.gov.hmcts.reform.professionalapi.configuration.security.IdamSecurityProperties;
import uk.gov.hmcts.reform.professionalapi.oidc.JwtGrantedAuthoritiesConverter;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SecurityConfigurationTest {

    private static final String VALID_ISSUER = "https://valid.example.com";
    private static final String INVALID_ISSUER = "https://invalid.example.com";

    @Mock
    private JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter;

    @Mock
    private RestAuthenticationEntryPoint restAuthenticationEntryPoint;

    @Mock
    private SecurityEndpointFilter securityEndpointFilter;

    @Mock
    private ServiceAuthFilter authFilter;

    @Mock
    private IdamSecurityProperties securityProperties;

    private SecurityConfiguration config;

    @BeforeEach
    void setUp() {
        config = new SecurityConfiguration(jwtGrantedAuthoritiesConverter, authFilter,
                restAuthenticationEntryPoint, securityEndpointFilter);
        ReflectionTestUtils.setField(config, "issuerUri", VALID_ISSUER);
    }

    @Test
    void validIssuer() {
        OAuth2TokenValidator<Jwt> validator = config.allowedIssuersValidator(List.of(VALID_ISSUER));
        Jwt jwt = Jwt.withTokenValue("token")
            .header("alg", "none")
            .claim("iss", VALID_ISSUER)
            .build();

        assertFalse(validator.validate(jwt).hasErrors());
    }

    @Test
    void invalidIssuer() {
        OAuth2TokenValidator<Jwt> validator = config.allowedIssuersValidator(List.of(VALID_ISSUER));
        Jwt jwt = Jwt.withTokenValue("token")
            .header("alg", "none")
            .claim("iss", INVALID_ISSUER)
            .build();

        assertTrue(validator.validate(jwt).hasErrors());
    }

    @Test
    void missingIssuer() {
        OAuth2TokenValidator<Jwt> validator = config.allowedIssuersValidator(List.of(VALID_ISSUER));
        Jwt jwt = Jwt.withTokenValue("token")
            .header("alg", "none")
            .claim("sub", "user")
            .build();

        assertTrue(validator.validate(jwt).hasErrors());
    }

    @Test
    void decoderCreated() {
        System.setProperty("idam.security.issuerValidation", Boolean.TRUE.toString());
        when(securityProperties.getAllowedIssuers()).thenReturn(List.of(VALID_ISSUER));
        NimbusJwtDecoder mockDecoder = mock(NimbusJwtDecoder.class);

        try (MockedStatic<JwtDecoders> mocked = mockStatic(JwtDecoders.class)) {
            mocked.when(() -> JwtDecoders.fromOidcIssuerLocation(VALID_ISSUER))
                .thenReturn(mockDecoder);

            JwtDecoder result = config.jwtDecoder(securityProperties);

            assertNotNull(result);
            verify(mockDecoder).setJwtValidator(any());
        }
    }

    @Test
    void oneOfMultipleAllowedIssuersAccepted() {
        OAuth2TokenValidator<Jwt> validator = config.allowedIssuersValidator(
            List.of(VALID_ISSUER, "https://other-valid.example.com")
        );
        Jwt jwt = Jwt.withTokenValue("token")
            .header("alg", "none")
            .claim("iss", "https://other-valid.example.com")
            .build();

        assertFalse(validator.validate(jwt).hasErrors());
    }

    @Test
    void emptyAllowedIssuersRejectsEverything() {
        OAuth2TokenValidator<Jwt> validator = config.allowedIssuersValidator(List.of());
        Jwt jwt = Jwt.withTokenValue("token")
            .header("alg", "none")
            .claim("iss", VALID_ISSUER)
            .build();

        assertTrue(validator.validate(jwt).hasErrors());
    }

    @Test
    void webSecurityCustomized() {
        List<String> anonymousPaths = List.of(
                "/swagger-ui.html",
                "/swagger-ui/**",
                "/swagger-resources/**",
                "/v3/**",
                "/health",
                "/health/liveness",
                "/health/readiness",
                "/status/health",
                "/loggers/**",
                "/");

        config.setAnonymousPaths(anonymousPaths);
        WebSecurityCustomizer customizer = config.webSecurityCustomizer();
        WebSecurity webSecurity = mock(WebSecurity.class, RETURNS_DEEP_STUBS);

        customizer.customize(webSecurity);

        verify(webSecurity.ignoring()).requestMatchers(anonymousPaths.toArray(String[]::new));
    }
}