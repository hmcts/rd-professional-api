package uk.gov.hmcts.reform.professionalapi.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import uk.gov.hmcts.reform.professionalapi.configuration.resolver.OrganisationIdArgumentResolver;
import uk.gov.hmcts.reform.professionalapi.configuration.resolver.UserIdArgumentResolver;

import java.util.List;

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

    @Override
    public void addArgumentResolvers(
            List<HandlerMethodArgumentResolver> argumentResolvers) {
        argumentResolvers.add(getUserIdArgumentResolver());
        argumentResolvers.add(getOrganisationIdArgumentResolver());
    }
}
