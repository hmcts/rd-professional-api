package uk.gov.hmcts.reform.professionalapi.consumer;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.PactSpecVersion;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.professionalapi.service.impl.SearchDeadline;
import uk.gov.hmcts.reform.professionalapi.service.impl.UserProfileMatchClient;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(PactConsumerTestExt.class)
@PactTestFor(providerName = "rd_user_profile_api_service", pactVersion = PactSpecVersion.V3)
class UserProfileMatchConsumerTest {
    @Pact(consumer = "rd_professional_api")
    public RequestResponsePact matched(PactDslWithProvider builder) {
        return interaction(builder, "Scoped name match classifies match nonmatch and unavailable", "smith", """
                {"matchedProfiles":[{"userIdentifier":"synthetic-a","firstName":"Ada","lastName":"Smith",
                "email":"ada@example.invalid"}],"unavailableUserIds":["synthetic-c"]}
                """);
    }

    @Pact(consumer = "rd_professional_api")
    public RequestResponsePact page(PactDslWithProvider builder) {
        return interaction(builder, "Scoped page display partitions all requested ids", "", """
                {"matchedProfiles":[{"userIdentifier":"synthetic-a","firstName":"Ada","lastName":"Smith",
                "email":"ada@example.invalid"},{"userIdentifier":"synthetic-b","firstName":"Grace",
                "lastName":"Jones","email":"grace@example.invalid"}],"unavailableUserIds":["synthetic-c"]}
                """);
    }

    @Pact(consumer = "rd_professional_api")
    public RequestResponsePact empty(PactDslWithProvider builder) {
        return interaction(builder, "Scoped name match returns successful zero matches", "absent", """
                {"matchedProfiles":[],"unavailableUserIds":["synthetic-c"]}
                """);
    }

    private RequestResponsePact interaction(PactDslWithProvider builder, String state, String term, String response) {
        return builder.given(state).uponReceiving(state).path("/v1/userprofile/users/match").method("POST")
                .headers(Map.of("Content-Type", "application/json", "Authorization", "Bearer test-token",
                        "ServiceAuthorization", "Bearer s2s-token"))
                .matchHeader("X-Organisation-Search-Deadline", "[0-9]{13}", "4102444800000")
                .body("{\"userIds\":[\"synthetic-a\",\"synthetic-b\",\"synthetic-c\"],"
                        + "\"fullNameContains\":\"" + term + "\"}")
                .willRespondWith().status(200).headers(Map.of("Content-Type", "application/json"))
                .body(response).toPact();
    }

    @Test
    @PactTestFor(pactMethod = "matched")
    void matchesWithTheActualClient(MockServer server) {
        var result = match(server, "smith");
        assertThat(result.matchedProfiles()).extracting(UserProfileMatchClient.Profile::userIdentifier)
                .containsExactly("synthetic-a");
        assertThat(result.unavailableUserIds()).containsExactly("synthetic-c");
    }

    @Test
    @PactTestFor(pactMethod = "page")
    void displaysWithTheActualClient(MockServer server) {
        assertThat(match(server, "").matchedProfiles()).hasSize(2);
    }

    @Test
    @PactTestFor(pactMethod = "empty")
    void returnsZeroMatchesWithTheActualClient(MockServer server) {
        assertThat(match(server, "absent").matchedProfiles()).isEmpty();
    }

    private UserProfileMatchClient.MatchResponse match(MockServer server, String term) {
        var tokens = mock(AuthTokenGenerator.class);
        when(tokens.generate()).thenReturn("Bearer s2s-token");
        var client = new UserProfileMatchClient(server.getUrl(), new ObjectMapper(), tokens);
        var deadline = new SearchDeadline(Duration.ofSeconds(10));
        try {
            return client.match(List.of("synthetic-a", "synthetic-b", "synthetic-c"), term, "Bearer test-token",
                    client.serviceToken(deadline), deadline);
        } finally {
            ReflectionTestUtils.invokeMethod(client, "shutdown");
        }
    }
}
