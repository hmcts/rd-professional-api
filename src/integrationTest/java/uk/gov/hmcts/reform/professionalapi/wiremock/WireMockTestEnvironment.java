package uk.gov.hmcts.reform.professionalapi.wiremock;

import com.github.tomakehurst.wiremock.WireMockServer;

import java.util.concurrent.atomic.AtomicBoolean;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

public final class WireMockTestEnvironment {

    private static final WireMockServer OIDC_MOCK_SERVER =
            new WireMockServer(wireMockConfig().dynamicPort());

    private static final WireMockServer IDAM_MOCK_SERVER =
            new WireMockServer(wireMockConfig().dynamicPort().extensions(new IdamResponseTransformer()));

    private static final WireMockServer S2S_MOCK_SERVER =
            new WireMockServer(wireMockConfig().dynamicPort());

    private static final AtomicBoolean STARTED = new AtomicBoolean(false);

    static {
        Runtime.getRuntime().addShutdownHook(
                new Thread(WireMockTestEnvironment::stop)
        );
    }

    private WireMockTestEnvironment() {
    }

    public static void start() {
        if (!STARTED.compareAndSet(false, true)) {
            return;
        }

        try {
            startOidcMockServer();
            startIdamMockServer();
            startS2sMockServer();
        } catch (RuntimeException e) {
            stop();
            throw e;
        }
    }

    private static void startOidcMockServer() {
        if (!OIDC_MOCK_SERVER.isRunning()) {
            OIDC_MOCK_SERVER.start();
            OidcWireMockStubs.registerDefaults(OIDC_MOCK_SERVER);
        }
    }

    private static void startIdamMockServer() {
        if (!IDAM_MOCK_SERVER.isRunning()) {
            IDAM_MOCK_SERVER.start();
            IdamWireMockStubs.registerDefaults(IDAM_MOCK_SERVER);
        }
    }

    private static void startS2sMockServer() {
        if (!S2S_MOCK_SERVER.isRunning()) {
            S2S_MOCK_SERVER.start();
            S2sWireMockStubs.registerDefaults(S2S_MOCK_SERVER);
        }
    }

    public static WireMockServer oidc() {
        return OIDC_MOCK_SERVER;
    }

    public static WireMockServer idam() {
        return IDAM_MOCK_SERVER;
    }

    public static WireMockServer s2s() {
        return S2S_MOCK_SERVER;
    }

    public static String oidcIssuer() {
        return "http://127.0.0.1:" + OIDC_MOCK_SERVER.port() + "/o";
    }

    public static String idamBaseUrl() {
        return "http://127.0.0.1:" + IDAM_MOCK_SERVER.port();
    }

    public static String s2sBaseUrl() {
        return "http://127.0.0.1:" + S2S_MOCK_SERVER.port();
    }

    public static void stop() {
        stopServer(OIDC_MOCK_SERVER);
        stopServer(IDAM_MOCK_SERVER);
        stopServer(S2S_MOCK_SERVER);

        STARTED.set(false);
    }

    private static void stopServer(WireMockServer server) {
        if (server.isRunning()) {
            server.stop();
        }
    }
}
