package uk.gov.hmcts.reform.professionalapi.service.impl;

import uk.gov.hmcts.reform.professionalapi.controller.advice.ExternalApiException;

import java.time.Duration;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static org.springframework.http.HttpStatus.GATEWAY_TIMEOUT;

public final class SearchDeadline {

    private final long expiresAt;
    private final SearchCancellation.Registration registration;

    public SearchDeadline(Duration budget) {
        this(System.nanoTime() + budget.toNanos(), null);
    }

    private SearchDeadline(long expiresAt, SearchCancellation.Registration registration) {
        this.expiresAt = expiresAt;
        this.registration = registration;
    }

    SearchDeadline cancellable(SearchCancellation.Registration registration) {
        return new SearchDeadline(expiresAt, registration);
    }

    <T> T admit(Supplier<T> start) {
        if (registration == null) {
            return start.get();
        }
        return registration.admit(() -> {
            remainingNanos();
            return start.get();
        });
    }

    <T> T awaitBatch(BlockingQueue<T> completed) throws InterruptedException {
        if (registration == null) {
            return completed.poll(remainingNanos(), TimeUnit.NANOSECONDS);
        }
        T batch;
        do {
            batch = completed.poll(Math.min(remainingNanos(), TimeUnit.MILLISECONDS.toNanos(250)),
                    TimeUnit.NANOSECONDS);
            if (batch == null) {
                registration.check();
            }
        } while (batch == null);
        return batch;
    }

    void checkCancellation() {
        if (registration != null) {
            registration.check();
        }
    }

    public long remainingNanos() {
        long remaining = expiresAt - System.nanoTime();
        if (remaining <= 0 || Thread.currentThread().isInterrupted()) {
            throw expired();
        }
        return remaining;
    }

    public long remainingMillis() {
        return Math.max(1, TimeUnit.NANOSECONDS.toMillis(remainingNanos()));
    }

    public static ExternalApiException expired() {
        return new ExternalApiException(GATEWAY_TIMEOUT, "Active organisation search timed out");
    }
}
