package uk.gov.hmcts.reform.professionalapi.util;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.extension.ResponseTransformer;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;


public class WireMockExtension extends WireMockServer implements BeforeAllCallback, AfterAllCallback {

    public WireMockExtension(int port) {
        super(port);
    }

    public WireMockExtension(int port, ResponseTransformer transformer) {
        super(wireMockConfig().extensions(transformer).port(port));
    }

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        start();
    }

    @Override
    public void afterAll(ExtensionContext context) throws InterruptedException {
        stop();
    }

}
