package uk.gov.hmcts.reform.professionalapi.util.serenity5.extension.page;

import net.serenitybdd.core.environment.WebDriverConfiguredEnvironment;
import net.thucydides.core.annotations.ClearCookiesPolicy;
import net.thucydides.core.annotations.ManagedWebDriverAnnotatedField;
import net.thucydides.core.steps.PageObjectDependencyInjector;
import net.thucydides.core.webdriver.ThucydidesWebDriverSupport;
import net.thucydides.core.webdriver.WebdriverProxyFactory;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.function.Consumer;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;
import static net.thucydides.core.webdriver.ThucydidesWebDriverSupport.getPages;
import static net.thucydides.core.webdriver.ThucydidesWebDriverSupport.initializeFieldsIn;

public class SerenityPageExtension implements BeforeEachCallback {

    @Override
    public void beforeEach(final ExtensionContext extensionContext) throws Exception {
        final TestConfiguration testConfiguration = TestConfiguration.forClass(extensionContext.getRequiredTestClass())
                .withSystemConfiguration(WebDriverConfiguredEnvironment.getDriverConfiguration());
        if (testConfiguration.isAWebTest()) {
            applyTestClassOrTestMethodSpecificWebDriverConfiguration(extensionContext);
            initializeFieldsIn(extensionContext.getRequiredTestInstance());
            injectPageObjectIntoTest(extensionContext.getRequiredTestInstance());

            prepareBrowserForTest(extensionContext);
        }
    }

    private void applyTestClassOrTestMethodSpecificWebDriverConfiguration(final ExtensionContext extensionContext) {
        ThucydidesWebDriverSupport.clearDefaultDriver();

        final Optional<ExplicitWebDriverConfiguration> explicitWebDriverConfiguration =
                explicitWebDriverConfiguration(extensionContext);
        explicitWebDriverConfiguration.ifPresent(it -> {
            final String value = it.getTestSpecificDriver();
            final Consumer<String> consumer = ThucydidesWebDriverSupport::useDefaultDriver;
            notEmpty(value).ifPresent(consumer);
            notEmpty(it.getDriverOptions()).ifPresent(ThucydidesWebDriverSupport::useDriverOptions);
            workaroundForOtherwiseIgnoredWebDriverOptions();
        });
    }

    private Optional<String> notEmpty(final String value) {
        return ofNullable(value).filter(StringUtils::isNotEmpty);
    }

    private void workaroundForOtherwiseIgnoredWebDriverOptions() {
        if (!ThucydidesWebDriverSupport.getDefaultDriverType().isPresent()
                && ThucydidesWebDriverSupport.getDefaultDriverOptions().isPresent()) {
            ThucydidesWebDriverSupport.useDefaultDriver(WebDriverConfiguredEnvironment.getDriverConfiguration()
                    .getDriverType().name());
        }
    }

    private Optional<ExplicitWebDriverConfiguration> explicitWebDriverConfiguration(
            final ExtensionContext extensionContext) {
        final Method testMethod = extensionContext.getRequiredTestMethod();
        final Class<?> requiredTestClass = extensionContext.getRequiredTestClass();
        if (hasExplicitWebDriverConfigurationOnTestMethod(testMethod)) {
            final String testSpecificDriver = TestMethodAnnotations.forTest(testMethod).specifiedDriver();
            final String driverOptions = TestMethodAnnotations.forTest(testMethod).driverOptions();
            return explicitWebDriverConfiguration(testSpecificDriver, driverOptions);
        } else if (hasExplicitWebDriverConfigurationOnTestClass(requiredTestClass)) {
            final ManagedWebDriverAnnotatedField firstAnnotatedField = ManagedWebDriverAnnotatedField
                    .findFirstAnnotatedField(requiredTestClass);
            return explicitWebDriverConfiguration(firstAnnotatedField.getDriver(), firstAnnotatedField.getOptions());
        }

        return empty();
    }

    @NotNull
    private Optional<ExplicitWebDriverConfiguration> explicitWebDriverConfiguration(final String testSpecificDriver,
                                                                                    final String driverOptions) {
        return of(new ExplicitWebDriverConfiguration(testSpecificDriver, driverOptions));
    }

    private void prepareBrowserForTest(final ExtensionContext extensionContext) {
        PatchedManagedWebDriverAnnotatedField.findAnnotatedFields(extensionContext.getRequiredTestClass()).stream()
                .filter(it -> ClearCookiesPolicy.BeforeEachTest.equals(it.getClearCookiesPolicy()))
                .map(it -> it.getValue(extensionContext.getRequiredTestInstance()))
                .forEach(WebdriverProxyFactory::clearBrowserSession);
    }

    private boolean hasExplicitWebDriverConfigurationOnTestClass(final Class<?> requiredTestClass) {
        return ManagedWebDriverAnnotatedField.hasManagedWebdriverField(requiredTestClass);
    }

    private boolean hasExplicitWebDriverConfigurationOnTestMethod(final Method testMethod) {
        return TestMethodAnnotations.forTest(testMethod).isDriverSpecified();
    }

    private void injectPageObjectIntoTest(final Object testClass) {
        new PageObjectDependencyInjector(getPages()).injectDependenciesInto(testClass);
    }

    public static class ExplicitWebDriverConfiguration {

        private final String testSpecificDriver;
        private final String driverOptions;

        public ExplicitWebDriverConfiguration(final String testSpecificDriver, final String driverOptions) {
            this.testSpecificDriver = testSpecificDriver;
            this.driverOptions = driverOptions;
        }

        public String getTestSpecificDriver() {
            return testSpecificDriver;
        }

        public String getDriverOptions() {
            return driverOptions;
        }

    }

}
