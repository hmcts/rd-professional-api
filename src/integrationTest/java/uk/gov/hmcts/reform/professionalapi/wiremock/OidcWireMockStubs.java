package uk.gov.hmcts.reform.professionalapi.wiremock;

import com.github.tomakehurst.wiremock.WireMockServer;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static uk.gov.hmcts.reform.professionalapi.util.KeyGenUtil.getDynamicJwksResponse;

public final class OidcWireMockStubs {

    private OidcWireMockStubs() {
    }

    public static void registerDefaults(WireMockServer server) {

        String issuer = "http://127.0.0.1:" + server.port() + "/o";

        String jwksUri = issuer + "/jwks";

        registerOpenIdConfiguration(server, issuer, jwksUri);

        registerJwksRoot(server);

        registerJwks(server);
    }

    private static void registerJwks(WireMockServer server) {
        server.stubFor(
                get(urlEqualTo("/o/jwks"))
                        .willReturn(
                                aResponse()
                                        .withStatus(200)
                                        .withHeader("Content-Type", "application/json")
                                        .withBody(getDynamicJwksResponse())
                        )
        );
    }

    private static void registerJwksRoot(WireMockServer server) {
        server.stubFor(
                get(urlEqualTo("/o"))
                        .willReturn(
                                aResponse()
                                        .withStatus(200)
                                        .withHeader("Content-Type", "application/json")
                                        .withBody(getDynamicJwksResponse())
                        )
        );
    }

    private static void registerOpenIdConfiguration(WireMockServer server, String issuer, String jwksUri) {
        server.stubFor(
                get(urlEqualTo("/o/.well-known/openid-configuration"))
                        .willReturn(
                                aResponse()
                                        .withStatus(200)
                                        .withHeader("Content-Type", "application/json")
                                        .withBody("""
                                                  {
                                                    "issuer": "%s",
                                                    "authorization_endpoint": "%s/authorize",
                                                    "token_endpoint": "%s/token",
                                                    "jwks_uri": "%s",
                                                    "subject_types_supported": ["public"],
                                                    "response_types_supported": ["code"],
                                                    "grant_types_supported": ["authorization_code"],
                                                    "id_token_signing_alg_values_supported": ["RS256"],
                                                    "scopes_supported": ["openid"]
                                                  }
                                                  """.formatted(
                                                        issuer,
                                                        issuer,
                                                        issuer,
                                                        jwksUri
                                                )
                                        )
                        )
        );
    }
}