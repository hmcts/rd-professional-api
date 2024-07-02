package uk.gov.hmcts.reform.professionalapi.util;

import com.launchdarkly.sdk.server.LDClient;
import jakarta.validation.constraints.NotNull;
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

public class FeatureToggleConditionExtension implements ExecutionCondition {

    private static FeatureToggleServiceImpl featureToggleService;

    private static boolean isInitialized = false;

    private static String flagName;

    @Override
    @SuppressWarnings("checkstyle:Indentation")
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {

        if (isNotTrue(isInitialized)) {
            initialize();
        }

        final Optional<AnnotatedElement> element = context.getElement();

        return element.map(annotatedElement -> {
                    Optional<ToggleEnable> toggleEnable = findAnnotation(element, ToggleEnable.class);

                    return toggleEnable.map(toggle -> {
                        featureToggleService.mapServiceToFlag();
                        flagName = featureToggleService.getLaunchDarklyMap().get(toggle.mapKey());

                        final boolean isFlagEnabled =
                                featureToggleService
                                        .isFlagEnabled("rd_professional_api", flagName);

                        ConditionEvaluationResult evaluationResult = null;

                        if (isFlagEnabled) {
                            if (isNotTrue(toggle.withFeature())) {
                                evaluationResult = disabled();
                            } else {
                                evaluationResult = enabled();
                            }
                        } else {
                            if (isNotTrue(toggle.withFeature()) || toggle.withFeature()) {
                                evaluationResult = disabled();
                            }
                        }
                        return evaluationResult;
                    }).orElse(enabled());
                }
        ).orElse(enabled());
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

    @SuppressWarnings("checkstyle:CommentsIndentation")
    private static void initialize() {
        LDClient ldClient = new LDClient(getenv("LD_SDK_KEY"));
        featureToggleService = new FeatureToggleServiceImpl(ldClient, "rd");
        String executionEnvironment = getenv("execution_environment");
        ReflectionTestUtils.setField(featureToggleService, "environment", executionEnvironment);
        isInitialized = true;
    }
}