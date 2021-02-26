package uk.gov.hmcts.reform.professionalapi.configuration;

import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

import java.util.List;
import javax.inject.Inject;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
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
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationFilter;
import uk.gov.hmcts.reform.authorisation.filters.ServiceAuthFilter;
import uk.gov.hmcts.reform.professionalapi.oidc.JwtGrantedAuthoritiesConverter;

@Configuration
@ConfigurationProperties(prefix = "security")
@EnableWebSecurity
@Slf4j
@SuppressWarnings("unchecked")
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {


    @Value("${spring.security.oauth2.client.provider.oidc.issuer-uri}")
    private String issuerUri;

    @Value("${oidc.issuer}")
    private String issuerOverride;

    private  ServiceAuthFilter serviceAuthFilter;
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
                    .antMatchers(anonymousPaths.toArray(new String[0]))
                    .antMatchers("/refdata/external/v1/organisations/mfa");
    }

    @Inject
    public SecurityConfiguration(final JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter,
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
           .antMatchers("/error").permitAll()
           .antMatchers(HttpMethod.POST, "/refdata/external/v1/organisations").permitAll()
           .antMatchers(HttpMethod.POST, "/refdata/internal/v1/organisations").permitAll()
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
        // FIXME : enable `withIssuer` once idam migration is done
        //OAuth2TokenValidator<Jwt> validator = new DelegatingOAuth2TokenValidator<>(withTimestamp, withIssuer);
        OAuth2TokenValidator<Jwt> validator = new DelegatingOAuth2TokenValidator<>(withTimestamp);
        jwtDecoder.setJwtValidator(validator);
        return jwtDecoder;
    }
}


