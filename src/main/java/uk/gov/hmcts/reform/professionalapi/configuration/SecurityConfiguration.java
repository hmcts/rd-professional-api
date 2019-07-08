package uk.gov.hmcts.reform.professionalapi.configuration;

import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

import java.util.List;

import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

import uk.gov.hmcts.reform.auth.checker.core.RequestAuthorizer;
import uk.gov.hmcts.reform.auth.checker.core.service.Service;
import uk.gov.hmcts.reform.auth.checker.core.user.User;
import uk.gov.hmcts.reform.auth.checker.spring.serviceanduser.AuthCheckerServiceAndUserFilter;
import uk.gov.hmcts.reform.auth.checker.spring.serviceonly.AuthCheckerServiceOnlyFilter;

@EnableWebSecurity
@Slf4j
public class SecurityConfiguration  {

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

        List<String> anonymousPaths;

        private AuthCheckerServiceAndUserFilter authCheckerServiceAndUserFilter;

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


        public RestAllApiSecurityConfigurationAdapter(RequestAuthorizer<User> userRequestAuthorizer,

                                                       RequestAuthorizer<Service> serviceRequestAuthorizer,

                                                       AuthenticationManager authenticationManager) {

            authCheckerServiceAndUserFilter = new AuthCheckerServiceAndUserFilter(serviceRequestAuthorizer, userRequestAuthorizer);

            authCheckerServiceAndUserFilter.setAuthenticationManager(authenticationManager);

        }

        @Override
        protected void configure(HttpSecurity http) throws Exception {

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
                    /*.authorizeRequests()
                    .anyRequest()
                    .authenticated()
                    .and()
                    .addFilter(authCheckerServiceAndUserFilter)*/;
        }


    }

}
