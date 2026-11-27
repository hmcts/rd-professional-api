package uk.gov.hmcts.reform.professionalapi.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.professionalapi.controller.advice.ExternalApiException;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.BAD_GATEWAY;
import static org.springframework.http.HttpStatus.GATEWAY_TIMEOUT;

class UserProfileMatchClientTest {

    private static final String PATH = "/v1/userprofile/users/match";
    private static final String BEARER = "Bearer caller";
    private static final String PRD_TOKEN = "rd-professional-api-service-token";
    private final AtomicReference<Request> received = new AtomicReference<>();
    private HttpServer server;
    private UserProfileMatchClient client;

    @BeforeEach
    void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.start();
        var tokens = mock(AuthTokenGenerator.class);
        when(tokens.generate()).thenReturn(PRD_TOKEN);
        client = new UserProfileMatchClient("http://127.0.0.1:" + server.getAddress().getPort(),
                new ObjectMapper(), tokens);
    }

    @AfterEach
    void stop() {
        client.shutdown();
        server.stop(0);
    }

    @Test
    void sendsTheRawTermAlongWithExplicitAuthenticationAndDeadline() throws IOException {
        respond(200, """
                {"matchedProfiles":[
                    {"userIdentifier":"non-uuid","firstName":"Current","lastName":"Admin",
                    "email":"current@example.test"}],
                 "unavailableUserIds":["missing"]}
                """, Duration.ZERO);
        var deadline = deadline();
        var result = client.match(List.of("non-uuid", "missing"), " Smith SW1A 1AA ", BEARER,
                client.serviceToken(deadline), deadline);
        assertThat(result.matchedProfiles()).extracting(UserProfileMatchClient.Profile::firstName)
                .containsExactly("Current");
        assertThat(result.unavailableUserIds()).containsExactly("missing");

        Request request = received.get();
        assertThat(request.method()).isEqualTo("POST");
        assertThat(request.headers().getFirst("Authorization")).isEqualTo(BEARER);
        assertThat(request.headers().getFirst("ServiceAuthorization")).isEqualTo(PRD_TOKEN);
        assertThat(request.headers().getFirst("X-Organisation-Search-Deadline")).matches("[0-9]+");
        var mapper = new ObjectMapper();
        assertThat(mapper.readTree(request.body())).isEqualTo(mapper.readTree("""
                {"fullNameContains":" Smith SW1A 1AA ","userIds":["non-uuid","missing"]}
                """));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "{\"matchedProfiles\":[],\"unavailableUserIds\":[123]}",
        "{\"matchedProfiles\":[],\"unavailableUserIds\":[true]}",
        "{\"matchedProfiles\":[{\"userIdentifier\":\"123\",\"firstName\":25,\"lastName\":\"B\",\"email\":\"C\"}],"
            + "\"unavailableUserIds\":[]}",
        "{\"matchedProfiles\":[{\"userIdentifier\":\"123\",\"firstName\":\"A\",\"lastName\":\"B\",\"email\":true}],"
            + "\"unavailableUserIds\":[]}",
        "{\"matchedProfiles\":[],\"unavailableUserIds\":[\"123\",\"123\"]}",
        "{\"matchedProfiles\":[],\"unavailableUserIds\":[\"unrequested\"]}",
        "{\"matchedProfiles\":[{\"userIdentifier\":\"123\",\"firstName\":\"A\",\"lastName\":\"B\",\"email\":\"C\"}],"
            + "\"unavailableUserIds\":[\"123\"]}",
        "{\"matchedProfiles\":[],\"unavailableUserIds\":[]} {}",
        "{\"matchedProfiles\":[]}",
        "null"
    })
    void rejectsMalformedAndInconsistentSuccessfulReplies(String response) {
        respond(200, response, Duration.ZERO);
        assertUpstreamFailure("term");
    }

    @Test
    void requiresCompleteClassificationInEmptyTermMode() {
        respond(200, """
                {"matchedProfiles":[],"unavailableUserIds":[]}
                """, Duration.ZERO);
        assertUpstreamFailure("");
    }

    @ParameterizedTest
    @ValueSource(ints = {404, 500})
    void neverTreatsAnHttpErrorAsAbsence(int status) {
        respond(status, "synthetic sensitive details", Duration.ZERO);
        assertUpstreamFailure("term");
    }

    @Test
    void appliesRemainingBudgetToTransport() {
        respond(200, """
                {"matchedProfiles":[],"unavailableUserIds":[]}
                """, Duration.ofMillis(800));
        assertThatThrownBy(() -> client.match(List.of("123"), "term", BEARER, PRD_TOKEN,
                new SearchDeadline(Duration.ofMillis(200))))
                .isInstanceOfSatisfying(ExternalApiException.class, ex -> assertThat(ex.getHttpStatus())
                        .isEqualTo(GATEWAY_TIMEOUT));
    }

    private void assertUpstreamFailure(String term) {
        assertThatThrownBy(() -> client.match(List.of("123"), term, BEARER, PRD_TOKEN, deadline()))
                .isInstanceOfSatisfying(ExternalApiException.class, ex -> {
                    assertThat(ex.getHttpStatus()).isEqualTo(BAD_GATEWAY);
                    assertThat(ex.getErrorMessage()).isEqualTo("User Profile search dependency failed");
                });
    }

    private void respond(int status, String body, Duration delay) {
        server.createContext(PATH, exchange -> {
            try (exchange) {
                received.set(new Request(exchange.getRequestMethod(), exchange.getRequestHeaders(),
                        new String(exchange.getRequestBody().readAllBytes(), UTF_8)));
                Thread.sleep(delay);
                byte[] bytes = body.getBytes(UTF_8);
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(status, bytes.length);
                exchange.getResponseBody().write(bytes);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        });
    }

    private record Request(String method, Headers headers, String body) {
    }

    private static SearchDeadline deadline() {
        return new SearchDeadline(Duration.ofSeconds(3));
    }
}
