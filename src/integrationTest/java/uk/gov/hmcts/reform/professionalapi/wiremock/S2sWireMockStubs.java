package uk.gov.hmcts.reform.professionalapi.wiremock;

import com.github.tomakehurst.wiremock.WireMockServer;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;

public class S2sWireMockStubs {

    private S2sWireMockStubs() {
    }

    public static void registerDefaults(WireMockServer server) {

        server.stubFor(get(urlEqualTo("/details"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("rd_professional_api")));

        server.stubFor(post(urlEqualTo("/lease"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJyZF9wcm9mZXNzaW9uYWxfYXBpIiwiZXhwIjoxNTY0NzU2MzY4fQ"
                                + ".UnRfwq_yGo6tVWEoBldCkD1zFoiMSqqm1rTHqq4f_PuTEHIJj2IHeARw3wOnJG2c3MpjM71ZTFa0RNE4D2"
                                + "AUgA")));
    }
}
