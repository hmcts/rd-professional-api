package uk.gov.hmcts.reform.professionalapi.authchecker.core.resolver;

import static java.util.concurrent.TimeUnit.SECONDS;

import com.google.common.base.Ticker;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.UncheckedExecutionException;

import lombok.SneakyThrows;

public class CachingSubjectResolver<T extends Subject> implements SubjectResolver<T> {

    private final LoadingCache<String, T> subjectCache;

    public CachingSubjectResolver(SubjectResolver<T> delegate, int ttlInSeconds, int maximumSize) {
        this(delegate, ttlInSeconds, maximumSize, Ticker.systemTicker());
    }

    public CachingSubjectResolver(SubjectResolver<T> delegate, int ttlInSeconds, int maximumSize, Ticker ticker) {
        this.subjectCache = CacheBuilder.newBuilder()
                .maximumSize(maximumSize)
                .ticker(ticker)
                .expireAfterWrite(ttlInSeconds, SECONDS)
                .build(CacheLoader.from(delegate::getTokenDetails));
    }

    @Override
    @SneakyThrows
    public T getTokenDetails(String bearerToken) {
        try {
            return subjectCache.getUnchecked(bearerToken);
        } catch (UncheckedExecutionException e) {
            throw e.getCause();
        }
    }
}

