package uk.gov.hmcts.reform.professionalapi.service.impl;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import uk.gov.hmcts.reform.professionalapi.controller.advice.ExternalApiException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.UUID;
import java.util.function.Supplier;

import static org.springframework.http.HttpStatus.CONFLICT;

@Component
public class SearchCancellation {

    private static final String INSERT = """
            INSERT INTO dbrefdata.active_search_request (request_id, caller_digest, cancelled)
            VALUES (?, ?, ?)
            ON CONFLICT (request_id, caller_digest)
            """;
    private static final String STATUS = """
            SELECT cancelled FROM dbrefdata.active_search_request
            WHERE request_id = ? AND caller_digest = ?
            """;

    private final JdbcTemplate jdbc;
    private final TransactionTemplate transaction;

    public SearchCancellation(JdbcTemplate jdbc, PlatformTransactionManager manager) {
        this.jdbc = jdbc;
        this.transaction = new TransactionTemplate(manager);
        transaction.setTimeout(2);
    }

    Registration register(UUID id, String bearer) {
        String caller = digest(bearer);
        int inserted = transaction.execute(status -> {
            purgeExpired();
            return jdbc.update(INSERT + "DO NOTHING", id, caller, false);
        });
        if (inserted == 0) {
            throw cancelled();
        }
        return new Registration(id, caller);
    }

    public void cancel(UUID id, String bearer) {
        String caller = digest(bearer);
        transaction.executeWithoutResult(status -> {
            purgeExpired();
            // A tombstone also handles cancellation arriving before the search is admitted.
            jdbc.update(INSERT + "DO UPDATE SET cancelled = true", id, caller, true);
        });
    }

    private void purgeExpired() {
        jdbc.update("DELETE FROM dbrefdata.active_search_request WHERE expires_at < CURRENT_TIMESTAMP");
    }

    private static String digest(String bearer) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(bearer.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(ex);
        }
    }

    private static ExternalApiException cancelled() {
        return new ExternalApiException(CONFLICT, "Active organisation search was cancelled or already started");
    }

    final class Registration {
        private final UUID id;
        private final String caller;

        private Registration(UUID id, String caller) {
            this.id = id;
            this.caller = caller;
        }

        void check() {
            transaction.executeWithoutResult(status -> verifyStatus(STATUS));
        }

        <T> T admit(Supplier<T> start) {
            return transaction.execute(status -> {
                // Cancel takes the conflicting row lock before acknowledging the request.
                verifyStatus(STATUS + "FOR SHARE");
                return start.get();
            });
        }

        private void verifyStatus(String sql) {
            if (Boolean.TRUE.equals(jdbc.query(sql, rows -> !rows.next() || rows.getBoolean(1), id, caller))) {
                throw cancelled();
            }
        }
    }
}
