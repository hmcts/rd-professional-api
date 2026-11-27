package uk.gov.hmcts.reform.professionalapi.service.impl;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.professionalapi.controller.advice.ExternalApiException;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

@DataJpaTest(properties = {
    "spring.datasource.url=jdbc:tc:postgresql:16:///dbrefdata",
    "spring.datasource.username=prd_search_test",
    "spring.datasource.password=prd_search_test",
    "spring.datasource.driver-class-name=org.testcontainers.jdbc.ContainerDatabaseDriver",
    "spring.flyway.schemas=dbrefdata",
    "spring.flyway.locations=classpath:db/migration,classpath:db/postgres",
    "spring.jpa.show-sql=false"
})
@AutoConfigureTestDatabase(replace = NONE)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class SearchCancellationPostgresTest {

    private static final String BEARER = "Bearer synthetic-caller";

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private PlatformTransactionManager manager;

    @Test
    void differentReplicaCanCancelAndBlockEveryLaterBatch() throws Exception {
        var searchId = UUID.randomUUID();
        var search = new SearchCancellation(jdbc, manager).register(searchId, BEARER);
        var otherReplica = new SearchCancellation(jdbc, manager);
        var entered = new CountDownLatch(1);
        var release = new CountDownLatch(1);
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            var firstBatch = executor.submit(() -> search.admit(() -> {
                entered.countDown();
                try {
                    assertThat(release.await(3, SECONDS)).isTrue();
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException(ex);
                }
                return "started";
            }));
            try {
                assertThat(entered.await(1, SECONDS)).isTrue();
                var cancellation = executor.submit(() -> otherReplica.cancel(searchId, BEARER));
                assertThat(waitForCancellationToReachTheRowLock()).isTrue();
                assertThat(cancellation).isNotDone();
                release.countDown();
                cancellation.get(3, SECONDS);
                assertThat(firstBatch.get(1, SECONDS)).isEqualTo("started");
                assertThatThrownBy(() -> search.admit(() -> {
                    throw new AssertionError("A new batch reached dispatch after cancellation");
                })).isInstanceOf(ExternalApiException.class);
            } finally {
                release.countDown();
            }
        }
    }

    private boolean waitForCancellationToReachTheRowLock() throws InterruptedException {
        for (int attempt = 0; attempt < 40; attempt++) {
            var waiting = jdbc.queryForObject("""
                    SELECT EXISTS (SELECT 1 FROM pg_stat_activity WHERE wait_event_type = 'Lock'
                        AND query LIKE 'INSERT INTO dbrefdata.active_search_request%DO UPDATE SET cancelled = true')
                    """, Boolean.class);
            if (Boolean.TRUE.equals(waiting)) {
                return true;
            }
            Thread.sleep(25);
        }
        return false;
    }

    @Test
    void anEarlyCancellationIsIdempotentAndCannotAffectAnotherCaller() {
        var searchId = UUID.randomUUID();
        var cancellations = new SearchCancellation(jdbc, manager);
        cancellations.cancel(searchId, BEARER);
        cancellations.cancel(searchId, BEARER);
        assertThatThrownBy(() -> cancellations.register(searchId, BEARER)).isInstanceOf(ExternalApiException.class);
        assertThat(cancellations.register(searchId, "Bearer another-caller").admit(() -> "dispatch"))
                .isEqualTo("dispatch");
    }
}
