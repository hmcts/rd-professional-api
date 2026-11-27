package uk.gov.hmcts.reform.professionalapi.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.professionalapi.controller.advice.ExternalApiException;
import uk.gov.hmcts.reform.professionalapi.controller.response.OrganisationEntityResponse;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.SuperUser;
import uk.gov.hmcts.reform.professionalapi.repository.ActiveOrganisationSearchRepository;
import uk.gov.hmcts.reform.professionalapi.repository.ActiveOrganisationSearchRepository.Page;
import uk.gov.hmcts.reform.professionalapi.repository.ActiveOrganisationSearchRepository.Representative;
import uk.gov.hmcts.reform.professionalapi.repository.ActiveOrganisationSearchRepository.Row;
import uk.gov.hmcts.reform.professionalapi.service.impl.UserProfileMatchClient.MatchResponse;
import uk.gov.hmcts.reform.professionalapi.service.impl.UserProfileMatchClient.Profile;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.BAD_GATEWAY;
import static org.springframework.http.HttpStatus.GATEWAY_TIMEOUT;

class ActiveOrganisationSearchTest {

    private static final String BEARER = "Bearer test";
    private static final String SERVICE_TOKEN = "prd-token";
    private static final MatchResponse NO_MATCHES = new MatchResponse(List.of(), List.of());

    private final ActiveOrganisationSearchRepository repository = mock(ActiveOrganisationSearchRepository.class);
    private final UserProfileMatchClient profiles = mock(UserProfileMatchClient.class);
    private final ObjectMapper mapper = new ObjectMapper();
    private final ActiveOrganisationSearch search = new ActiveOrganisationSearch(repository, profiles, mapper,
            mock(SearchCancellation.class));

    @AfterEach
    void shutdown() {
        search.shutdown();
    }

    @Test
    void detailStillResolvesItsProfileWhenBothSearchPoolsAreFull() throws Exception {
        var release = new CountDownLatch(1);
        var active = new CountDownLatch(48);
        Runnable block = () -> {
            active.countDown();
            try {
                release.await();
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        };
        var coordinators = (ThreadPoolExecutor) ReflectionTestUtils.getField(search, "searches");
        var batches = (ThreadPoolExecutor) ReflectionTestUtils.getField(search, "batches");
        var profile = new Profile("view-id", "Current", "Admin", "current@example.test");
        when(profiles.serviceToken(any())).thenReturn(SERVICE_TOKEN);
        when(profiles.match(eq(List.of("view-id")), eq(""), eq(BEARER), eq(SERVICE_TOKEN), any()))
                .thenReturn(new MatchResponse(List.of(profile), List.of()));

        try {
            IntStream.range(0, 32).forEach(ignored -> coordinators.execute(block));
            IntStream.range(0, 80).forEach(ignored -> batches.execute(block));
            assertThat(active.await(5, TimeUnit.SECONDS)).isTrue();
            assertThat(coordinators.getActiveCount()).isEqualTo(32);
            assertThat(batches.getQueue().remainingCapacity()).isZero();
            assertThat(search.profileForInternalDetail("view-id", BEARER)).isEqualTo(profile);
        } finally {
            release.countDown();
        }
    }

    @Test
    void processesLastBatchAndDisplaysTheMatchingSnapshotWithoutRefetching() throws Exception {
        var candidates = candidates(4001);
        var last = candidates.getLast();
        var first = candidates.getFirst();
        var match = new Profile(last.userIdentifier(), "Current", "Admin", "current@example.test");
        var page = new Page(List.of(row(first, "Fallback"), row(last, "Stale")), 2, false);
        when(repository.candidates(any())).thenReturn(candidates);
        when(profiles.serviceToken(any())).thenReturn(SERVICE_TOKEN);
        when(profiles.match(anyList(), eq("term"), eq(BEARER), eq(SERVICE_TOKEN), any()))
                .thenAnswer(call -> {
                    List<String> ids = call.getArgument(0);
                    if (ids.contains(first.userIdentifier())) {
                        return new MatchResponse(List.of(), List.of(first.userIdentifier()));
                    }
                    return ids.contains(last.userIdentifier()) ? new MatchResponse(List.of(match), List.of())
                            : NO_MATCHES;
                });
        when(repository.loadPage(eq("term"), eq(0L), eq(10), eq(Map.of(first, false, last, true)), any()))
                .thenReturn(page);

        var response = search.search("term", 1, 10, BEARER, deadline());
        var json = mapper.readTree(response.json());
        assertThat(response.totalRecords()).isEqualTo(2);
        assertThat(json.at("/organisations/0/superUser/firstName").asText()).isEqualTo("Fallback");
        assertThat(json.at("/organisations/1/superUser/firstName").asText()).isEqualTo("Current");
        verify(profiles, times(5)).match(anyList(), eq("term"), eq(BEARER), eq(SERVICE_TOKEN), any());
        verify(profiles, never()).match(anyList(), eq(""), anyString(), anyString(), any());
    }

    @Test
    void browsesOnlyPageIdsAndAcceptsExplicitlyUnavailableProfiles() throws Exception {
        var rep = candidates(1).getFirst();
        var row = row(rep, "Stored");
        when(repository.loadPage(eq(""), eq(100L), eq(10), eq(Map.of()), any()))
                .thenReturn(new Page(List.of(row), 103, true));
        when(profiles.serviceToken(any())).thenReturn(SERVICE_TOKEN);
        when(profiles.match(eq(List.of(rep.userIdentifier())), eq(""), eq(BEARER), eq(SERVICE_TOKEN), any()))
                .thenReturn(new MatchResponse(List.of(), List.of(rep.userIdentifier())));

        var result = search.search("", 11, 10, BEARER, deadline());
        var json = mapper.readTree(result.json());
        assertThat(result.totalRecords()).isEqualTo(103);
        assertThat(json.at("/moreAvailable").asBoolean()).isTrue();
        assertThat(json.at("/organisations/0/superUser/firstName").asText()).isEqualTo("Stored");
        verify(repository, never()).candidates(any());
    }

    @Test
    void enrichesAReplacementOnlyAsAPageHit() throws Exception {
        var former = candidates(1).getFirst();
        var replacement = new Representative(former.rowId(), "replacement");
        var stale = new Profile(former.userIdentifier(), "Former", "Admin", "former@example.test");
        var current = new Profile(replacement.userIdentifier(), "Current", "Admin", "current@example.test");
        when(repository.candidates(any())).thenReturn(List.of(former));
        when(profiles.serviceToken(any())).thenReturn(SERVICE_TOKEN);
        when(profiles.match(eq(List.of(former.userIdentifier())), eq("term"), eq(BEARER), eq(SERVICE_TOKEN), any()))
                .thenReturn(new MatchResponse(List.of(stale), List.of()));
        when(repository.loadPage(eq("term"), eq(0L), eq(10), eq(Map.of(former, true)), any()))
                .thenReturn(new Page(List.of(row(replacement, "Stored")), 1, false));
        when(profiles.match(eq(List.of(replacement.userIdentifier())), eq(""), eq(BEARER), eq(SERVICE_TOKEN), any()))
                .thenReturn(new MatchResponse(List.of(current), List.of()));

        var result = search.search("term", 1, 10, BEARER, deadline());
        assertThat(mapper.readTree(result.json()).at("/organisations/0/superUser/firstName").asText())
                .isEqualTo("Current");
    }

    @Test
    void failsClosedWhenTheLastBatchFails() {
        var candidates = candidates(4001);
        when(repository.candidates(any())).thenReturn(candidates);
        when(profiles.serviceToken(any())).thenReturn(SERVICE_TOKEN);
        when(profiles.match(anyList(), eq("term"), eq(BEARER), eq(SERVICE_TOKEN), any())).thenAnswer(call -> {
            List<String> ids = call.getArgument(0);
            if (ids.size() == 1) {
                throw new ExternalApiException(BAD_GATEWAY, "User Profile search dependency failed");
            }
            return NO_MATCHES;
        });

        assertThatThrownBy(() -> search.search("term", 1, 10, BEARER, deadline()))
                .isInstanceOfSatisfying(ExternalApiException.class, ex -> assertThat(ex.getHttpStatus())
                        .isEqualTo(BAD_GATEWAY));
        verify(repository, never()).loadPage(anyString(), anyLong(), eq(10), anyMap(), any());
    }

    @Test
    void deadlineStopsRunningBatchesAndPreventsFurtherWork() throws Exception {
        var candidates = candidates(5000);
        var calls = new AtomicInteger();
        var interrupted = new CountDownLatch(4);
        var blocked = new CountDownLatch(1);
        when(repository.candidates(any())).thenReturn(candidates);
        when(profiles.serviceToken(any())).thenReturn(SERVICE_TOKEN);
        when(profiles.match(anyList(), eq("term"), eq(BEARER), eq(SERVICE_TOKEN), any())).thenAnswer(call -> {
            calls.incrementAndGet();
            try {
                blocked.await();
                return NO_MATCHES;
            } catch (InterruptedException ex) {
                interrupted.countDown();
                throw SearchDeadline.expired();
            }
        });

        try {
            assertThatThrownBy(() -> search.search("term", 1, 10, BEARER,
                    new SearchDeadline(Duration.ofSeconds(1))))
                    .isInstanceOfSatisfying(ExternalApiException.class, ex -> assertThat(ex.getHttpStatus())
                            .isEqualTo(GATEWAY_TIMEOUT));
            assertThat(interrupted.await(2, TimeUnit.SECONDS)).isTrue();
            assertThat(calls.get()).isEqualTo(4);
            verify(repository, never()).loadPage(anyString(), anyLong(), eq(10), anyMap(), any());
        } finally {
            blocked.countDown();
        }
    }

    @Test
    void sharesOneInFlightLimitAcrossIndependentSearches() throws Exception {
        var candidates = candidates(4000);
        var running = new AtomicInteger();
        var peak = new AtomicInteger();
        var firstWave = new CountDownLatch(16);
        var release = new CountDownLatch(1);
        when(repository.candidates(any())).thenReturn(candidates);
        when(repository.loadPage(anyString(), anyLong(), eq(10), anyMap(), any()))
                .thenReturn(new Page(List.of(), 0, false));
        when(profiles.serviceToken(any())).thenReturn(SERVICE_TOKEN);
        when(profiles.match(anyList(), anyString(), eq(BEARER), eq(SERVICE_TOKEN), any())).thenAnswer(call -> {
            peak.accumulateAndGet(running.incrementAndGet(), Math::max);
            firstWave.countDown();
            try {
                release.await();
                return NO_MATCHES;
            } finally {
                running.decrementAndGet();
            }
        });

        try (var clients = Executors.newVirtualThreadPerTaskExecutor()) {
            var requests = IntStream.range(0, 6)
                    .mapToObj(i -> clients.submit(() -> search.search("term " + i, 1, 10, BEARER, deadline())))
                    .toList();
            try {
                assertThat(firstWave.await(2, TimeUnit.SECONDS)).isTrue();
            } finally {
                release.countDown();
            }
            for (var result : requests) {
                assertThat(result.get(2, TimeUnit.SECONDS).totalRecords()).isZero();
            }
            assertThat(peak.get()).isEqualTo(16);
        }
    }

    private static SearchDeadline deadline() {
        return new SearchDeadline(Duration.ofSeconds(5));
    }

    private static List<Representative> candidates(int size) {
        return IntStream.range(0, size).mapToObj(i -> new Representative(new UUID(0, i), "user-" + i)).toList();
    }

    private static Row row(Representative rep, String firstName) {
        var organisation = new Organisation();
        var stored = new SuperUser(firstName, "Admin", "stored@example.test", organisation);
        return new Row(new OrganisationEntityResponse(organisation, true, true, false, stored), rep);
    }
}
