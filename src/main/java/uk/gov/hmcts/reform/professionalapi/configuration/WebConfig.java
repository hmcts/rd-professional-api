package uk.gov.hmcts.reform.professionalapi.configuration;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import uk.gov.hmcts.reform.professionalapi.configuration.resolver.OrganisationIdArgumentResolver;
import uk.gov.hmcts.reform.professionalapi.configuration.resolver.UserIdArgumentResolver;

@Configuration
public class WebConfig  implements WebMvcConfigurer {

    @Bean
    public UserIdArgumentResolver getUserIdArgumentResolver() {
        return new UserIdArgumentResolver();
    }

    @Bean
    public OrganisationIdArgumentResolver getOrganisationIdArgumentResolver() {
        return new OrganisationIdArgumentResolver();
    }

    @Autowired
    FeatureConditionEvaluation featureConditionEvaluation;

    @Override
    public void addArgumentResolvers(
            List<HandlerMethodArgumentResolver> argumentResolvers) {
        argumentResolvers.add(getUserIdArgumentResolver());
        argumentResolvers.add(getOrganisationIdArgumentResolver());
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(featureConditionEvaluation);
    }

}
