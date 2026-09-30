package uk.gov.hmcts.reform.professionalapi.wiremock;

import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ContextClosedEvent;
import uk.gov.hmcts.reform.professionalapi.util.TestApplicationServer;

import java.util.Map;

public class WireMockContextInitializer
        implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    private static void registerBeans(ConfigurableApplicationContext context) {
        ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();

        TestApplicationServer testApplicationServer =
                new TestApplicationServer();

        beanFactory.registerSingleton(
                "testApplicationServer",
                testApplicationServer
        );

        beanFactory.registerSingleton(
                "oidcMockServer",
                WireMockTestEnvironment.oidc()
        );

        beanFactory.registerSingleton(
                "idamMockServer",
                WireMockTestEnvironment.idam()
        );

        beanFactory.registerSingleton(
                "s2sMockServer",
                WireMockTestEnvironment.s2s()
        );

    }

    @Override
    public void initialize(@NonNull ConfigurableApplicationContext context) {
        WireMockTestEnvironment.start();
        registerBeans(context);
        configureProperties(context);

        context.addApplicationListener(event -> {
            if (event instanceof ContextClosedEvent) {
                WireMockTestEnvironment.stop();
            }
        });
    }

    private void configureProperties(ConfigurableApplicationContext context) {
        TestPropertyValues.of(
                Map.of(
                        "spring.security.oauth2.client.provider.oidc.issuer-uri",
                        WireMockTestEnvironment.oidcIssuer(),

                        "idam.api.url",
                        WireMockTestEnvironment.idamBaseUrl(),

                        "idam.s2s-auth.url",
                        WireMockTestEnvironment.s2sBaseUrl()
                )
        ).applyTo(context);
    }
}