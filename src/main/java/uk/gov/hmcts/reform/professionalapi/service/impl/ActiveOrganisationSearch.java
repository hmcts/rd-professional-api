package uk.gov.hmcts.reform.professionalapi.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PreDestroy;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.professionalapi.controller.advice.ExternalApiException;
import uk.gov.hmcts.reform.professionalapi.controller.response.OrganisationsDetailResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.SuperUserResponse;
import uk.gov.hmcts.reform.professionalapi.repository.ActiveOrganisationSearchRepository;
import uk.gov.hmcts.reform.professionalapi.repository.ActiveOrganisationSearchRepository.Representative;
import uk.gov.hmcts.reform.professionalapi.repository.ActiveOrganisationSearchRepository.Row;
import uk.gov.hmcts.reform.professionalapi.service.impl.UserProfileMatchClient.MatchResponse;
import uk.gov.hmcts.reform.professionalapi.service.impl.UserProfileMatchClient.Profile;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE;

@Service
public class ActiveOrganisationSearch {

    private static final int BATCH_SIZE = 1000;
    private static final int BATCHES_PER_REQUEST = 4;
    private static final int SHARED_BATCH_WORKERS = 16;

    private final ThreadPoolExecutor searches = new ThreadPoolExecutor(32, 32, 30, TimeUnit.SECONDS,
            new SynchronousQueue<>(), Thread.ofVirtual().name("active-search-", 0).factory());
    private final ThreadPoolExecutor batches = new ThreadPoolExecutor(SHARED_BATCH_WORKERS, SHARED_BATCH_WORKERS, 30,
            TimeUnit.SECONDS, new ArrayBlockingQueue<>(64), Thread.ofVirtual().name("active-search-up-", 0).factory());
    private final ThreadPoolExecutor details = new ThreadPoolExecutor(4, 4, 30, TimeUnit.SECONDS,
            new SynchronousQueue<>(), Thread.ofVirtual().name("active-detail-up-", 0).factory());

    private final ActiveOrganisationSearchRepository repository;
    private final UserProfileMatchClient profileClient;
    private final ObjectMapper mapper;
    private final SearchCancellation cancellations;

    public ActiveOrganisationSearch(ActiveOrganisationSearchRepository repository, UserProfileMatchClient profileClient,
                                    ObjectMapper mapper, SearchCancellation cancellations) {
        this.repository = repository;
        this.profileClient = profileClient;
        this.mapper = mapper;
        this.cancellations = cancellations;
    }

    public Response search(String term, int page, int size, String bearer, SearchDeadline deadline) {
        return search(term, page, size, bearer, deadline, null);
    }

    public Response search(String term, int page, int size, String bearer, SearchDeadline deadline, UUID searchId) {
        return withDeadline(searches, () -> {
            deadline.remainingNanos();
            var budget = searchId == null ? deadline : deadline.cancellable(cancellations.register(searchId, bearer));
            return runSearch(term, (page - 1L) * size, size, bearer, budget);
        }, deadline);
    }

    public void cancel(UUID searchId, String bearer) {
        cancellations.cancel(searchId, bearer);
    }

    Profile profileForInternalDetail(String userId, String bearer) {
        var deadline = new SearchDeadline(Duration.ofSeconds(25));
        return withDeadline(details,
                () -> profileClient.match(List.of(userId), "", bearer, profileClient.serviceToken(deadline), deadline)
                        .matchedProfiles().stream().findFirst().orElse(null), deadline);
    }

    private <T> T withDeadline(ThreadPoolExecutor executor, Callable<T> operation, SearchDeadline deadline) {
        final Future<T> search;
        try {
            search = executor.submit(operation);
        } catch (RejectedExecutionException ex) {
            throw overloaded();
        }
        try {
            return search.get(deadline.remainingNanos(), TimeUnit.NANOSECONDS);
        } catch (TimeoutException ex) {
            throw SearchDeadline.expired();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw SearchDeadline.expired();
        } catch (ExecutionException ex) {
            throw searchFailure(ex);
        } finally {
            search.cancel(true);
        }
    }

    private Response runSearch(String term, long offset, int size, String bearer, SearchDeadline deadline) {
        var candidates = term.isEmpty() ? List.<Representative>of() : repository.candidates(deadline);
        var matched = new HashMap<Representative, Profile>();
        var classifications = new HashMap<Representative, Boolean>();
        String s2s = null;
        if (!candidates.isEmpty()) {
            s2s = profileClient.serviceToken(deadline);
            var classification = lookup(candidates.stream().map(Representative::userIdentifier).toList(), term,
                    bearer, s2s, deadline);
            for (Representative rep : candidates) {
                Profile profile = classification.get(rep.userIdentifier());
                if (profile != null) {
                    matched.put(rep, profile);
                    classifications.put(rep, true);
                } else if (classification.containsKey(rep.userIdentifier())) {
                    classifications.put(rep, false);
                }
            }
        }

        var page = repository.loadPage(term, offset, size, classifications, deadline);
        var remaining = page.rows().stream().map(Row::representative)
                .filter(rep -> rep.userIdentifier() != null && !classifications.containsKey(rep))
                .map(Representative::userIdentifier).toList();
        Map<String, Profile> enrichment = remaining.isEmpty() ? Map.of()
                : lookup(remaining, "", bearer, s2s == null ? profileClient.serviceToken(deadline) : s2s, deadline);

        for (Row row : page.rows()) {
            var rep = row.representative();
            Profile profile = matched.get(rep);
            if (profile == null && rep.userIdentifier() != null) {
                profile = enrichment.get(rep.userIdentifier());
            }
            if (profile != null) {
                row.organisation().setSuperUser(new SuperUserResponse(profile.firstName(), profile.lastName(),
                        profile.email()));
            }
        }

        var body = new OrganisationsDetailResponse(page.rows().stream().map(Row::organisation).toList());
        body.setMoreAvailable(page.moreAvailable());
        deadline.remainingNanos();
        deadline.checkCancellation();
        try {
            var json = mapper.writeValueAsBytes(body);
            deadline.remainingNanos();
            return new Response(page.totalRecords(), json);
        } catch (JsonProcessingException ex) {
            throw new ExternalApiException(INTERNAL_SERVER_ERROR, "Could not encode organisation search result");
        }
    }

    private Map<String, Profile> lookup(List<String> userIds, String term, String bearer, String s2s,
                                        SearchDeadline deadline) {
        var ids = userIds.stream().distinct().toList();
        Map<String, Profile> classified = new HashMap<>();
        var pending = new ArrayList<Batch>(BATCHES_PER_REQUEST);
        var completed = new LinkedBlockingQueue<Batch>();
        int next = 0;
        try {
            while (next < ids.size() || !pending.isEmpty()) {
                while (next < ids.size() && pending.size() < BATCHES_PER_REQUEST) {
                    deadline.remainingNanos();
                    int end = Math.min(next + BATCH_SIZE, ids.size());
                    var batchIds = ids.subList(next, end);
                    var batch = new Batch(() -> profileClient.match(batchIds, term, bearer, s2s, deadline), completed);
                    batches.execute(batch);
                    pending.add(batch);
                    next = end;
                }
                var batch = deadline.awaitBatch(completed);
                if (batch == null) {
                    throw SearchDeadline.expired();
                }
                pending.remove(batch);
                MatchResponse response = batch.get();
                response.matchedProfiles().forEach(profile -> classified.put(profile.userIdentifier(), profile));
                response.unavailableUserIds().forEach(id -> classified.put(id, null));
            }
            return classified;
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw SearchDeadline.expired();
        } catch (ExecutionException ex) {
            throw searchFailure(ex);
        } catch (RejectedExecutionException ex) {
            throw overloaded();
        } finally {
            pending.forEach(batch -> {
                batch.cancel(true);
                batches.remove(batch);
            });
        }
    }

    private static ExternalApiException searchFailure(ExecutionException ex) {
        if (ex.getCause() instanceof ExternalApiException failure) {
            return failure;
        }
        if (ex.getCause() instanceof QueryTimeoutException) {
            return SearchDeadline.expired();
        }
        return new ExternalApiException(INTERNAL_SERVER_ERROR, "Could not complete organisation search");
    }

    private static ExternalApiException overloaded() {
        return new ExternalApiException(SERVICE_UNAVAILABLE, "Active organisation search capacity reached");
    }

    @PreDestroy
    void shutdown() {
        searches.shutdownNow();
        batches.shutdownNow();
        details.shutdownNow();
    }

    private static final class Batch extends FutureTask<MatchResponse> {

        private final LinkedBlockingQueue<Batch> completed;

        private Batch(Callable<MatchResponse> match, LinkedBlockingQueue<Batch> completed) {
            super(match);
            this.completed = completed;
        }

        @Override
        protected void done() {
            completed.add(this);
        }
    }

    public static final class Response {
        private final long totalRecords;
        private final byte[] json;

        public Response(long totalRecords, byte[] json) {
            this.totalRecords = totalRecords;
            this.json = json;
        }

        public long totalRecords() {
            return totalRecords;
        }

        public byte[] json() {
            return json;
        }
    }
}
