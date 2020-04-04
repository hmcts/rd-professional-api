package uk.gov.hmcts.reform.professionalapi.configuration;

import java.util.Collections;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationProvider;
import uk.gov.hmcts.reform.professionalapi.authchecker.core.authorizer.ServiceRequestAuthorizer;
import uk.gov.hmcts.reform.professionalapi.authchecker.core.resolver.CachingSubjectResolver;
import uk.gov.hmcts.reform.professionalapi.authchecker.core.resolver.Service;
import uk.gov.hmcts.reform.professionalapi.authchecker.core.resolver.ServiceResolver;
import uk.gov.hmcts.reform.professionalapi.authchecker.servicetoken.ServiceTokenParser;
import uk.gov.hmcts.reform.professionalapi.authchecker.core.resolver.SubjectResolver;


@Lazy
@Configuration
@ConditionalOnProperty(prefix = "idam.s2s-auth", name = "url")
public class AuthCheckerConfiguration {

    @Bean
    @ConditionalOnMissingBean(name = "serviceResolver")
    public SubjectResolver<Service> serviceResolver(ServiceTokenParser serviceTokenParser, AuthCheckerServiceCacheConfiguration properties) {
        return new CachingSubjectResolver<>(new ServiceResolver(serviceTokenParser), properties.getService().getTtlInSeconds(), properties.getService().getMaximumSize());
    }

    @Bean
    @ConditionalOnMissingBean(name = "serviceRequestAuthorizer")
    @ConditionalOnProperty(name = "idam.s2s-authorised.services")
    public ServiceRequestAuthorizer serviceRequestAuthorizer(SubjectResolver<Service> serviceResolver, @Value("${idam.s2s-authorised.services}") Set<String> authorizedServicesExtractor) {
        return new ServiceRequestAuthorizer(serviceResolver, authorizedServicesExtractor);
    }

    @Bean
    @ConditionalOnMissingBean(name = "preAuthenticatedAuthenticationProvider")
    public PreAuthenticatedAuthenticationProvider preAuthenticatedAuthenticationProvider() {
        PreAuthenticatedAuthenticationProvider authenticationProvider = new PreAuthenticatedAuthenticationProvider();
        authenticationProvider.setPreAuthenticatedUserDetailsService(new AuthCheckerUserDetailsService());

        return authenticationProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(PreAuthenticatedAuthenticationProvider preAuthenticatedAuthenticationProvider) {
        return new ProviderManager(Collections.singletonList(preAuthenticatedAuthenticationProvider));
    }
}
