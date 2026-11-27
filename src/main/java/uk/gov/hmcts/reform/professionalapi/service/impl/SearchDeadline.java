package uk.gov.hmcts.reform.professionalapi.service.impl;

import uk.gov.hmcts.reform.professionalapi.controller.advice.ExternalApiException;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.springframework.http.HttpStatus.GATEWAY_TIMEOUT;

public final class SearchDeadline {

    private final long expiresAt;

    public SearchDeadline(Duration budget) {
        expiresAt = System.nanoTime() + budget.toNanos();
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
