package uk.gov.hmcts.reform.professionalapi.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestRedirectFilter;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.jwt.JwtIssuerValidator;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import uk.gov.hmcts.reform.authorisation.filters.ServiceAuthFilter;
import uk.gov.hmcts.reform.professionalapi.oidc.JwtGrantedAuthoritiesConverter;

import java.util.List;
import javax.inject.Inject;

import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

@Configuration
@ConfigurationProperties(prefix = "security")
@EnableWebSecurity
@Slf4j
@SuppressWarnings("unchecked")
public class SecurityConfiguration {


    @Value("${spring.security.oauth2.client.provider.oidc.issuer-uri}")
    private String issuerUri;

    @Value("${oidc.issuer}")
    private String issuerOverride;

    @Order(1)
    private  ServiceAuthFilter serviceAuthFilter;
    @Order(2)
    private final SecurityEndpointFilter securityEndpointFilter;
    List<String> anonymousPaths;

    private final JwtAuthenticationConverter jwtAuthenticationConverter;

    private RestAuthenticationEntryPoint restAuthenticationEntryPoint;

    public List<String> getAnonymousPaths() {
        return anonymousPaths;
    }

    public void setAnonymousPaths(List<String> anonymousPaths) {
        this.anonymousPaths = anonymousPaths;
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return web -> web.ignoring()
                    .antMatchers(anonymousPaths.toArray(new String[0]))
                    .antMatchers("/refdata/external/v1/organisations/mfa");
    }

    @Inject
    public SecurityConfiguration(final JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter,
                                 final ServiceAuthFilter serviceAuthFilter,
                                 RestAuthenticationEntryPoint restAuthenticationEntryPoint,
                                 SecurityEndpointFilter securityEndpointFilter) {

        this.serviceAuthFilter = serviceAuthFilter;
        this.restAuthenticationEntryPoint = restAuthenticationEntryPoint;
        jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter);
        this.securityEndpointFilter = securityEndpointFilter;

    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
           .addFilterBefore(serviceAuthFilter, BearerTokenAuthenticationFilter.class)
           .addFilterAfter(securityEndpointFilter, OAuth2AuthorizationRequestRedirectFilter.class)

                .sessionManagement().sessionCreationPolicy(STATELESS).and()
           .csrf().disable()
           .formLogin().disable()
           .logout().disable()
           .authorizeRequests()
           .antMatchers("/error").permitAll()
           .antMatchers(HttpMethod.POST, "/refdata/external/v1/organisations").permitAll()
           .antMatchers(HttpMethod.POST, "/refdata/internal/v1/organisations").permitAll()
            .antMatchers(HttpMethod.GET, "/refdata/internal/v1/organisations/users").permitAll()
                .antMatchers(HttpMethod.POST, "/refdata/external/v2/organisations").permitAll()
                .antMatchers(HttpMethod.POST, "/refdata/internal/v1/organisations/getOrganisationsByProfile")
                .permitAll()
                .antMatchers(HttpMethod.POST, "/refdata/internal/v2/organisations").permitAll()
                .antMatchers(HttpMethod.POST, "/refdata/internal/v2/organisations/users").permitAll()
           .anyRequest()
           .authenticated()
           .and()
           .oauth2ResourceServer().authenticationEntryPoint(restAuthenticationEntryPoint)
           .jwt()
           .jwtAuthenticationConverter(jwtAuthenticationConverter)
           .and()
           .and()
            .oauth2Client();
        return http.build();
    }

    @Bean
    JwtDecoder jwtDecoder() {

        NimbusJwtDecoder jwtDecoder = (NimbusJwtDecoder) JwtDecoders.fromOidcIssuerLocation(issuerUri);

        // We are using issuerOverride instead of issuerUri as SIDAM has the wrong issuer at the moment
        OAuth2TokenValidator<Jwt> withTimestamp = new JwtTimestampValidator();
        OAuth2TokenValidator<Jwt> withIssuer = new JwtIssuerValidator(issuerOverride);
        // FIXME : enable `withIssuer` once idam migration is done
        //OAuth2TokenValidator<Jwt> validator = new DelegatingOAuth2TokenValidator<>(withTimestamp, withIssuer);
        OAuth2TokenValidator<Jwt> validator = new DelegatingOAuth2TokenValidator<>(withTimestamp);
        jwtDecoder.setJwtValidator(validator);
        return jwtDecoder;
    }
}


