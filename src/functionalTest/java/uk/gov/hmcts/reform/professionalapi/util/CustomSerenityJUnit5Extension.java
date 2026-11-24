package uk.gov.hmcts.reform.professionalapi.util;

import com.launchdarkly.sdk.server.LDClient;
import net.serenitybdd.junit5.SerenityJUnit5Extension;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.professionalapi.service.impl.FeatureToggleServiceImpl;

import java.lang.reflect.AnnotatedElement;
import java.util.Optional;

import static java.lang.System.getenv;
import static org.apache.commons.lang3.BooleanUtils.isNotTrue;
import static org.junit.platform.commons.util.AnnotationUtils.findAnnotation;

public class CustomSerenityJUnit5Extension extends SerenityJUnit5Extension implements ExecutionCondition {

    private static LDClient ldClient;

    private static FeatureToggleServiceImpl featureToggleService;

    private static boolean isInitialized = false;

    private static String flagName;

    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
        if (isNotTrue(isInitialized)) {
            initialize();
        }
        final Optional<AnnotatedElement> optElement = context.getElement();
        Optional<ToggleEnable> toggleEnable = findAnnotation(optElement, ToggleEnable.class);

        if (toggleEnable.isEmpty()) {
            return enabled();
        }

        ToggleEnable toggle = toggleEnable.get();

        flagName = featureToggleService.getLaunchDarklyMap().get(toggle.mapKey());
        final boolean isFlagEnabled = featureToggleService.isFlagEnabled("rd_professional_api", flagName);

        ConditionEvaluationResult evaluationResult = disabled();
        if (isFlagEnabled && toggle.withFeature()) {
            evaluationResult = enabled();
        }
        return evaluationResult;
    }

    @NotNull
    public static String getToggledOffMessage() {
        return flagName.concat(" ")
                .concat(FeatureConditionEvaluation.FORBIDDEN_EXCEPTION_LD);
    }

    private ConditionEvaluationResult disabled() {
        return ConditionEvaluationResult.disabled(getToggledOffMessage());
    }

    private ConditionEvaluationResult enabled() {
        return ConditionEvaluationResult.enabled("Feature toggled ON");
    }


    private static void initialize() {
        LDClient ldClient = new LDClient(getenv("LD_SDK_KEY"));
        featureToggleService = new FeatureToggleServiceImpl(ldClient, "rd");
        String executionEnvironment = getenv("execution_environment");
        ReflectionTestUtils.setField(featureToggleService, "environment", executionEnvironment);
        isInitialized = true;
    }
}