package uk.gov.hmcts.reform.professionalapi.util;

import com.launchdarkly.sdk.server.LDClient;

import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.professionalapi.service.impl.FeatureToggleServiceImpl;

import static net.logstash.logback.encoder.org.apache.commons.lang3.BooleanUtils.negate;

public class CustomSerenityRunner extends SpringIntegrationSerenityRunner {

    @Autowired
    LDClient ldClient;

    protected FeatureToggleServiceImpl featureToggleService = new FeatureToggleServiceImpl(ldClient, "rd");

    public CustomSerenityRunner(Class<?> klass) throws InitializationError {
        super(klass);
    }

    @Override
    protected boolean isIgnored(FrameworkMethod child) {
        ToggleEnable o1 = child.getAnnotation(ToggleEnable.class);
        if (o1 != null) {
            featureToggleService.mapServiceToFlag();
            String flag = featureToggleService.getLaunchDarklyMap()
                .get(o1.mapKey());

            boolean isEnabledLD = featureToggleService.isFlagEnabled("rd_professional_api", flag);

            if (isEnabledLD) {
                if (negate(o1.withFeature())) {
                    return true;
                }
            } else {
                if (o1.withFeature()) {
                    return true;
                }
            }
        }
        return super.isIgnored(child);
    }
}
