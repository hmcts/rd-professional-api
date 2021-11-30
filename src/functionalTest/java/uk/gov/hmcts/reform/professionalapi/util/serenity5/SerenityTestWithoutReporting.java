package uk.gov.hmcts.reform.professionalapi.util.serenity5;

import org.junit.jupiter.api.extension.ExtendWith;
import uk.gov.hmcts.reform.professionalapi.util.serenity5.extension.SerenityExtension;
import uk.gov.hmcts.reform.professionalapi.util.serenity5.extension.SerenityJUnitLifecycleAdapterExtension;
import uk.gov.hmcts.reform.professionalapi.util.serenity5.extension.SerenityManualExtension;
import uk.gov.hmcts.reform.professionalapi.util.serenity5.extension.SerenityStepExtension;
import uk.gov.hmcts.reform.professionalapi.util.serenity5.extension.page.SerenityPageExtension;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@ExtendWith({
        SerenityExtension.class,
        SerenityJUnitLifecycleAdapterExtension.class,
        SerenityManualExtension.class,
        SerenityPageExtension.class,
        SerenityStepExtension.class})
public @interface SerenityTestWithoutReporting {
}



