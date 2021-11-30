package uk.gov.hmcts.reform.professionalapi.util.serenity5.guice;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class JUnitInjectors {

    private JUnitInjectors() {
    }

    private static Injector injector;

    public static Injector getInjector() {
        if (injector == null) {
            injector = Guice.createInjector(new JUnit5Module());
        }
        return injector;
    }
}
