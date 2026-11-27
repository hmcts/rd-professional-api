package uk.gov.hmcts.reform.professionalapi.service.impl;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.cfg.CoercionAction;
import com.fasterxml.jackson.databind.cfg.CoercionInputShape;
import com.fasterxml.jackson.databind.type.LogicalType;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.professionalapi.controller.advice.ExternalApiException;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.BAD_GATEWAY;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.authorisation.filters.ServiceAuthFilter.AUTHORISATION;

@Component
public class UserProfileMatchClient {

    private final URI endpoint;
    private final ObjectMapper mapper;
    private final ObjectReader responseReader;
    private final AuthTokenGenerator tokenGenerator;
    private final HttpClient http = HttpClient.newBuilder().build();

    public UserProfileMatchClient(@Value("${userProfUrl}") String baseUrl, ObjectMapper mapper,
                                  AuthTokenGenerator tokenGenerator) {
        int end = baseUrl.length();
        while (end > 0 && baseUrl.charAt(end - 1) == '/') {
            end--;
        }
        this.endpoint = URI.create(baseUrl.substring(0, end) + "/v1/userprofile/users/match");
        this.mapper = mapper;
        var strictResponseMapper = mapper.copy();
        strictResponseMapper.coercionConfigFor(LogicalType.Textual)
                .setCoercion(CoercionInputShape.Boolean, CoercionAction.Fail)
                .setCoercion(CoercionInputShape.Integer, CoercionAction.Fail)
                .setCoercion(CoercionInputShape.Float, CoercionAction.Fail);
        responseReader = strictResponseMapper.readerFor(MatchResponse.class)
                .with(DeserializationFeature.FAIL_ON_TRAILING_TOKENS);
        this.tokenGenerator = tokenGenerator;
    }

    public String serviceToken(SearchDeadline deadline) {
        deadline.remainingNanos();
        try {
            return tokenGenerator.generate();
        } catch (RuntimeException ex) {
            deadline.remainingNanos();
            throw upstreamFailure();
        }
    }

    public MatchResponse match(List<String> userIds, String term, String bearer, String s2s,
                               SearchDeadline deadline) {
        try {
            byte[] json = mapper.writeValueAsBytes(new MatchRequest(userIds, term));
            HttpRequest request = HttpRequest.newBuilder(endpoint)
                    .timeout(Duration.ofNanos(deadline.remainingNanos()))
                    .header(AUTHORIZATION, bearer)
                    .header(AUTHORISATION, s2s)
                    .header("X-Organisation-Search-Deadline",
                            Long.toString(System.currentTimeMillis() + deadline.remainingMillis()))
                    .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                    .POST(HttpRequest.BodyPublishers.ofByteArray(json))
                    .build();

            HttpResponse<byte[]> response = send(request, deadline);
            if (response.statusCode() != 200) {
                throw upstreamFailure();
            }
            MatchResponse match = responseReader.readValue(response.body());
            validate(userIds, term.isEmpty(), match);
            deadline.remainingNanos();
            return match;
        } catch (IOException ex) {
            throw upstreamFailure();
        }
    }

    private HttpResponse<byte[]> send(HttpRequest request, SearchDeadline deadline) {
        var future = deadline.admit(() -> http.sendAsync(request, response -> response.statusCode() == 200
                ? HttpResponse.BodySubscribers.ofByteArray() : HttpResponse.BodySubscribers.replacing(new byte[0])));
        try {
            return future.get(deadline.remainingNanos(), TimeUnit.NANOSECONDS);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw SearchDeadline.expired();
        } catch (TimeoutException ex) {
            throw SearchDeadline.expired();
        } catch (ExecutionException ex) {
            if (ex.getCause() instanceof HttpTimeoutException) {
                throw SearchDeadline.expired();
            }
            throw upstreamFailure();
        } finally {
            future.cancel(true);
        }
    }

    private static void validate(List<String> requestedIds, boolean allRequired, MatchResponse response) {
        if (response == null || response.matchedProfiles() == null || response.unavailableUserIds() == null) {
            throw upstreamFailure();
        }
        var remaining = new HashSet<>(requestedIds);
        for (Profile profile : response.matchedProfiles()) {
            if (profile == null || profile.firstName() == null || profile.lastName() == null || profile.email() == null
                    || !remaining.remove(profile.userIdentifier())) {
                throw upstreamFailure();
            }
        }
        for (String absentId : response.unavailableUserIds()) {
            if (!remaining.remove(absentId)) {
                throw upstreamFailure();
            }
        }
        if (allRequired && !remaining.isEmpty()) {
            throw upstreamFailure();
        }
    }

    private static ExternalApiException upstreamFailure() {
        return new ExternalApiException(BAD_GATEWAY, "User Profile search dependency failed");
    }

    @PreDestroy
    void shutdown() {
        http.shutdownNow();
    }

    private record MatchRequest(List<String> userIds, String fullNameContains) {
    }

    public record MatchResponse(List<Profile> matchedProfiles, List<String> unavailableUserIds) {
    }

    public record Profile(String userIdentifier, String firstName, String lastName, String email) {
    }
}
