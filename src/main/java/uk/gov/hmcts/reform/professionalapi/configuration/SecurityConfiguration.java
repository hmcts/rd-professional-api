package uk.gov.hmcts.reform.professionalapi.configuration;

import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

import java.util.List;

import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

import uk.gov.hmcts.reform.auth.checker.core.RequestAuthorizer;
import uk.gov.hmcts.reform.auth.checker.core.service.Service;
import uk.gov.hmcts.reform.auth.checker.core.user.User;
import uk.gov.hmcts.reform.auth.checker.spring.serviceanduser.AuthCheckerServiceAndUserFilter;

@Configuration
@ConfigurationProperties(prefix = "security")
@EnableWebSecurity
@Slf4j
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    List<String>    anonymousPaths;

    private  RequestAuthorizer<Service> serviceRequestAuthorizer;

    private  RequestAuthorizer<User> userRequestAuthorizer;

    private AuthenticationManager      authenticationManager;

    private AuthCheckerServiceAndUserFilter authCheckerServiceAndUserFilter;

    public SecurityConfiguration(
            RequestAuthorizer<Service> serviceRequestAuthorizer,
            RequestAuthorizer<User> userRequestAuthorizer,
            AuthenticationManager authenticationManager) {
        this.serviceRequestAuthorizer = serviceRequestAuthorizer;
        this.userRequestAuthorizer = userRequestAuthorizer;
        this.authenticationManager = authenticationManager;
    }

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

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        log.info("Inside SecurityConfiguration::");
        AuthCheckerServiceAndUserFilter authCheckerServiceAndUserFilter = new AuthCheckerServiceAndUserFilter(serviceRequestAuthorizer, userRequestAuthorizer);
        authCheckerServiceAndUserFilter.setAuthenticationManager(authenticationManager);


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
                .and()
                .addFilter(authCheckerServiceAndUserFilter);
    }
}
