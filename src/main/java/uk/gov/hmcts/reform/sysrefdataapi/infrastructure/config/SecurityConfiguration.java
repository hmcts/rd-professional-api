package uk.gov.hmcts.reform.sysrefdataapi.infrastructure.config;

import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import uk.gov.hmcts.reform.auth.checker.core.RequestAuthorizer;
import uk.gov.hmcts.reform.auth.checker.core.service.Service;
import uk.gov.hmcts.reform.auth.checker.spring.serviceonly.AuthCheckerServiceOnlyFilter;

@Configuration
@ConfigurationProperties(prefix = "security")
@EnableWebSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    private final List<String> anonymousPaths = new ArrayList<>();
    private final RequestAuthorizer<Service> serviceRequestAuthorizer;
    private final AuthenticationManager authenticationManager;

    public SecurityConfiguration(
        RequestAuthorizer<Service> serviceRequestAuthorizer,
        AuthenticationManager authenticationManager
    ) {
        this.serviceRequestAuthorizer = serviceRequestAuthorizer;
        this.authenticationManager = authenticationManager;
    }

    public List<String> getAnonymousPaths() {
        return anonymousPaths;
    }

    @Override
    public void configure(WebSecurity web) {
        web.ignoring().antMatchers(
            anonymousPaths
                .stream()
                .toArray(String[]::new)
        );
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        AuthCheckerServiceOnlyFilter authCheckerServiceOnlyFilter =
            new AuthCheckerServiceOnlyFilter(serviceRequestAuthorizer);

        authCheckerServiceOnlyFilter.setAuthenticationManager(authenticationManager);

        http
            .addFilter(authCheckerServiceOnlyFilter)
            .sessionManagement().sessionCreationPolicy(STATELESS)
            .and()
            .csrf().disable()
            .formLogin().disable()
            .logout().disable()
            .authorizeRequests().anyRequest().authenticated()
        ;
    }
}
