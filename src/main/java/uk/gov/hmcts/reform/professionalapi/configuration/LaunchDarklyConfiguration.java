package uk.gov.hmcts.reform.professionalapi.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import uk.gov.hmcts.reform.professionalapi.util.FeatureConditionEvaluation;

@Configuration
public class LaunchDarklyConfiguration implements WebMvcConfigurer {

    @Autowired
    private FeatureConditionEvaluation featureConditionEvaluation;

    /**
     * Add here entry in registry and api endpoint path pattern like below.
     * <pre>
     * {@code registry.addInterceptor(featureConditionEvaluation)
     *     addPathPatterns("/refdata/external/v1/organisations/status/**");
     * }
     * </pre>
     * @param registry registry
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(featureConditionEvaluation)
            .addPathPatterns("/refdata/external/v1/organisations/mfa");
        registry.addInterceptor(featureConditionEvaluation)
                .addPathPatterns("/refdata/external/v1/organisations/pba");
        registry.addInterceptor(featureConditionEvaluation)
            .addPathPatterns("/refdata/internal/v1/organisations/{orgId}/mfa");
        registry.addInterceptor(featureConditionEvaluation)
            .addPathPatterns("/refdata/internal/v1/organisations/pba/{status}");
        registry.addInterceptor(featureConditionEvaluation)
                .addPathPatterns("/refdata/internal/v1/organisations/{orgId}/pba/status");
        registry.addInterceptor(featureConditionEvaluation)
                .addPathPatterns("/refdata/external/v1/organisations/addresses");
        registry.addInterceptor(featureConditionEvaluation)
            .addPathPatterns("/refdata/internal/v2/organisations");
        registry.addInterceptor(featureConditionEvaluation)
            .addPathPatterns("/refdata/internal/v2/organisations/pbas");
        registry.addInterceptor(featureConditionEvaluation)
            .addPathPatterns("/refdata/internal/v2/organisations/{orgId}");
        registry.addInterceptor(featureConditionEvaluation)
            .addPathPatterns("/refdata/external/v2/organisations/pbas");
        registry.addInterceptor(featureConditionEvaluation)
            .addPathPatterns("/refdata/external/v2/organisations");
        registry.addInterceptor(featureConditionEvaluation)
                .addPathPatterns("/refdata/internal/v1/bulkCustomer");


    }
}
