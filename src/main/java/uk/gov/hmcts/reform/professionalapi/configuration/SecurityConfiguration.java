package uk.gov.hmcts.reform.professionalapi.configuration;
import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

import javax.inject.Inject;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.jwt.JwtIssuerValidator;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationFilter;
import uk.gov.hmcts.reform.authorisation.filters.ServiceAuthFilter;
import uk.gov.hmcts.reform.professionalapi.authchecker.core.resolver.RequestAuthorizer;
import uk.gov.hmcts.reform.professionalapi.authchecker.core.service.Service;
import uk.gov.hmcts.reform.professionalapi.authchecker.core.service.AuthCheckerServiceOnlyFilter;


@ConfigurationProperties(prefix = "security")
@EnableWebSecurity
@Slf4j
public class SecurityConfiguration {

    @Configuration
    @Order(1)
    public static class PostApiSecurityConfigurationAdapter extends WebSecurityConfigurerAdapter {

        private AuthCheckerServiceOnlyFilter authCheckerServiceOnlyFilter;

        public PostApiSecurityConfigurationAdapter(RequestAuthorizer<Service> serviceRequestAuthorizer,

                                                   AuthenticationManager authenticationManager) {

            authCheckerServiceOnlyFilter = new AuthCheckerServiceOnlyFilter(serviceRequestAuthorizer);

            authCheckerServiceOnlyFilter.setAuthenticationManager(authenticationManager);


        }

        protected void configure(HttpSecurity http) throws Exception {

            http.requestMatchers()
                    .antMatchers(HttpMethod.POST, "/refdata/external/v1/organisations")
                    .antMatchers(HttpMethod.POST, "/refdata/internal/v1/organisations")
                    .and()
                    .addFilter(authCheckerServiceOnlyFilter)
                    .csrf().disable()
                    .authorizeRequests()
                    .anyRequest().authenticated();
        }
    }

    @ConfigurationProperties(prefix = "security")
    @Configuration
    @Order(2)
    public static class RestAllApiSecurityConfigurationAdapter extends WebSecurityConfigurerAdapter {


        @Value("${spring.security.oauth2.client.provider.oidc.issuer-uri}")
        private String issuerUri;

        @Value("${oidc.issuer}")
        private String issuerOverride;

        private final ServiceAuthFilter serviceAuthFilter;
        List<String> anonymousPaths;

        private JwtAuthenticationConverter jwtAuthenticationConverter;

        public List<String> getAnonymousPaths() {
            return anonymousPaths;
        }

        public void setAnonymousPaths(List<String> anonymousPaths) {
            this.anonymousPaths = anonymousPaths;
        }

        @Override
        public void configure(WebSecurity web) {
            web.ignoring()
                    .antMatchers(anonymousPaths.toArray(new String[0]));
        }

         @Inject
         public RestAllApiSecurityConfigurationAdapter(final JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter,
                                                       final ServiceAuthFilter serviceAuthFilter
                                                      ) {

             this.serviceAuthFilter = serviceAuthFilter;
             jwtAuthenticationConverter = new JwtAuthenticationConverter();
             jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter);

        }

        @Override
        protected void configure(final HttpSecurity http) throws Exception {
            http
                    .addFilterBefore(serviceAuthFilter, BearerTokenAuthenticationFilter.class)
                    .sessionManagement().sessionCreationPolicy(STATELESS).and()
                    .csrf().disable()
                    .formLogin().disable()
                    .logout().disable()
                    .authorizeRequests()
                    // To preserve v1EndpointsPathParamSecurityFilter 403 response
                    .antMatchers("/error").permitAll()
                    .anyRequest()
                    .authenticated()
                    .and()
                    .oauth2ResourceServer()
                    .jwt()
                    .jwtAuthenticationConverter(jwtAuthenticationConverter)
                    .and()
                    .and()
                    .oauth2Client();

        }

        @Bean
        JwtDecoder jwtDecoder() {
            NimbusJwtDecoder jwtDecoder = (NimbusJwtDecoder) JwtDecoders.fromOidcIssuerLocation(issuerUri);

            // We are using issuerOverride instead of issuerUri as SIDAM has the wrong issuer at the moment
            OAuth2TokenValidator<Jwt> withTimestamp = new JwtTimestampValidator();
            OAuth2TokenValidator<Jwt> withIssuer = new JwtIssuerValidator(issuerOverride);
            OAuth2TokenValidator<Jwt> validator = new DelegatingOAuth2TokenValidator<>(withTimestamp, withIssuer);

            jwtDecoder.setJwtValidator(validator);
            return jwtDecoder;
        }


    }

}
