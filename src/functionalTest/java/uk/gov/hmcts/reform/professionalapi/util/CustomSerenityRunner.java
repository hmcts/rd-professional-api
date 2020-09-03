package uk.gov.hmcts.reform.professionalapi.util;

import com.launchdarkly.sdk.server.LDClient;

import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestContextManager;
import org.springframework.test.context.TestPropertySource;
import uk.gov.hmcts.reform.professionalapi.config.Oauth2;
import uk.gov.hmcts.reform.professionalapi.config.TestConfigProperties;
import uk.gov.hmcts.reform.professionalapi.service.impl.FeatureToggleServiceImpl;

import static java.lang.System.getenv;
import static net.logstash.logback.encoder.org.apache.commons.lang3.BooleanUtils.negate;

@ContextConfiguration(classes = {TestConfigProperties.class, Oauth2.class})
@ComponentScan("uk.gov.hmcts.reform.professionalapi")
@TestPropertySource("classpath:application-functional.yaml")
public class CustomSerenityRunner extends SpringIntegrationSerenityRunner {

    LDClient ldClient;

    protected TestContextManager testContextManager;

    private FeatureToggleServiceImpl featureToggleService;

    public CustomSerenityRunner(Class<?> klass) throws InitializationError {
        super(klass);
        this.testContextManager =  new TestContextManager(klass);;
    }

    @Override
    protected boolean isIgnored(FrameworkMethod child) {
        ldClient =  new LDClient(getenv("LD_SDK_KEY"));
        featureToggleService = new FeatureToggleServiceImpl(ldClient, "rd");
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
