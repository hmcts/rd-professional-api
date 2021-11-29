package uk.gov.hmcts.reform.professionalapi.util;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.extension.ResponseTransformer;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.util.concurrent.TimeUnit;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;


public class WireMockExtension extends WireMockServer implements BeforeEachCallback, AfterEachCallback {

    public WireMockExtension(int port) {
        super(port);
    }

    public WireMockExtension(int port, ResponseTransformer transformer) {
        super(wireMockConfig().extensions(transformer).port(port));
    }

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        start();
    }

    @Override
    public void afterEach(ExtensionContext context) throws InterruptedException {
        stop();
        resetAll();
        TimeUnit.SECONDS.sleep(1);
    }

}
