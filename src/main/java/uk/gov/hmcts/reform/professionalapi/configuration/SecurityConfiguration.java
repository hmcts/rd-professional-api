package uk.gov.hmcts.reform.professionalapi.configuration;

import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

import java.util.List;

import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationFilter;
import uk.gov.hmcts.reform.authorisation.filters.ServiceAuthFilter;


@EnableWebSecurity
@Slf4j
public class SecurityConfiguration {

    @Configuration
    @Order(1)
    public static class PostApiSecurityConfigurationAdapter extends WebSecurityConfigurerAdapter {

        private  ServiceAuthFilter serviceAuthFilter;

        public PostApiSecurityConfigurationAdapter(ServiceAuthFilter serviceAuthFilter)
        {
            this.serviceAuthFilter = serviceAuthFilter;
        }

    @Override
    public void configure(WebSecurity web) {
            web.ignoring().antMatchers("/swagger-ui.html",
                    "/webjars/springfox-swagger-ui/**",
                    "/swagger-resources/**",
                    "/v2/**",
                    "/health",
                    "/health/liveness",
                    "/status/health",
                    "/loggers/**",
                    "/");
        }

    @Override
    protected void configure(final HttpSecurity http) throws Exception {

            http.requestMatchers()
                    .antMatchers(HttpMethod.POST, "/refdata/external/v1/organisations")
                    .antMatchers(HttpMethod.POST, "/refdata/internal/v1/organisations")
                    .and()
                    .addFilterBefore(serviceAuthFilter, BearerTokenAuthenticationFilter.class)
                    .csrf().disable()
                    .authorizeRequests()
                    .anyRequest().authenticated();

        }

    }

   /* @ConfigurationProperties(prefix = "security")
    @Configuration
    @Order(2)
    public static class RestAllApiSecurityConfigurationAdapter extends WebSecurityConfigurerAdapter {

        List<String> anonymousPaths;

       // private AuthCheckerServiceAndUserFilter authCheckerServiceAndUserFilter;

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


       *//* public RestAllApiSecurityConfigurationAdapter(RequestAuthorizer<User> userRequestAuthorizer,

                                                       RequestAuthorizer<Service> serviceRequestAuthorizer,

                                                       AuthenticationManager authenticationManager) {

            authCheckerServiceAndUserFilter = new AuthCheckerServiceAndUserFilter(serviceRequestAuthorizer, userRequestAuthorizer);

            authCheckerServiceAndUserFilter.setAuthenticationManager(authenticationManager);

        }*//*

        @Override
        protected void configure(HttpSecurity http) throws AccessDeniedException,Exception {

            http.authorizeRequests()
                    .antMatchers("/actuator/**","/search/**")
                    .permitAll()
                    .and()
                    .sessionManagement()
                    .sessionCreationPolicy(STATELESS)
                    .and()
                    .csrf()
                    .disable()
                    .formLogin()
                    .disable()
                    .logout()
                    .disable()
                    .authorizeRequests()
                    .anyRequest()
                    .authenticated()
                    .and();
                  //  .addFilter(authCheckerServiceAndUserFilter);
        }*/


    //}

}
