package uk.gov.hmcts.reform.professionalapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.professionalapi.repository.ActiveOrganisationSearchRepository;
import uk.gov.hmcts.reform.professionalapi.repository.ActiveOrganisationSearchRepository.Page;
import uk.gov.hmcts.reform.professionalapi.repository.ActiveOrganisationSearchRepository.Representative;
import uk.gov.hmcts.reform.professionalapi.service.impl.SearchDeadline;

import java.sql.SQLException;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

@DataJpaTest(properties = {
    "spring.datasource.url=${PRD_SEARCH_TEST_JDBC_URL}",
    "spring.datasource.username=${PRD_SEARCH_TEST_DB_USER:prd_search_test}",
    "spring.datasource.password=${PRD_SEARCH_TEST_DB_PASSWORD:prd_search_test}",
    "spring.datasource.driver-class-name=org.postgresql.Driver",
    "spring.flyway.schemas=dbrefdata",
    "spring.flyway.locations=classpath:db/migration,classpath:db/postgres",
    "spring.jpa.show-sql=false"
})
@AutoConfigureTestDatabase(replace = NONE)
@Import(ActiveOrganisationSearchRepository.class)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@EnabledIfEnvironmentVariable(named = "PRD_SEARCH_TEST_JDBC_URL", matches = "jdbc:postgresql:.*")
class ActiveOrganisationSearchPostgresTest {

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private ActiveOrganisationSearchRepository search;

    @Test
    void filtersRealNestedFieldsAndKeepsExactPageOrderAndAdministratorReassignments() {
        var target = organisation(1, "Smith & Co", "ACTIVE", "SRA732");
        final var duplicateName = organisation(2, "SMITH & CO", "ACTIVE", null);
        final var postcodeOnly = organisation(3, "JONES LLP", "ACTIVE", null);
        final var splitPostcode = organisation(4, "Smith Split", "ACTIVE", null);
        final var inactive = organisation(5, "Smith & Co", "PENDING", null);
        final var exactLiteral = organisation(6, "SMI%TH _!\\ Partners", "ACTIVE", null);
        organisation(7, "RAW SMITH SW1A 1AA NAME", "ACTIVE", null);
        organisation(8, "DÉNÉ", "ACTIVE", null);
        final var pendingOnly = organisation(9, "PendOnly", "ACTIVE", null);
        final var whitespacePostcode = organisation(10, "Whitespace", "ACTIVE", null);
        final var blankId = organisation(11, "Fallback", "ACTIVE", null);
        final var noMarker = organisation(12, "Nomarker", "ACTIVE", null);

        contact(target, "ZZ93 7GR");
        final var targetContact = contact(target, "SW1A 1AA");
        contact(target, "SW1A1AA");
        contact(postcodeOnly, "SW1A 1AA");
        contact(splitPostcode, "SW1A");
        contact(splitPostcode, "1AA");
        contact(inactive, "SW1A 1AA");
        contact(exactLiteral, "SW1A 1AA");
        contact(whitespacePostcode, "    ");
        dx(targetContact, "DX-FIRST", "CENTRAL");
        dx(targetContact, "DX-SECOND", "SECOND EXCHANGE");
        pba(target, "PBA-ACCEPTED", "ACCEPTED");
        pba(target, "PBA-PENDING", "PENDING");
        pba(pendingOnly, "PBA-REJECTED", "REJECTED");

        final var original = administrator(target, 100, "current", "Stale");
        final var secondary = administrator(target, 101, "secondary", "Secondary");
        administrator(duplicateName, 102, "absent", "Absent");
        administrator(blankId, 103, "\t ", "Blank");
        administrator(noMarker, 104, "no-marker", "Other");
        jdbc.update("DELETE FROM dbrefdata.user_attribute WHERE professional_user_id = ?", new UUID(0, 104));

        assertThat(search.candidates(deadline())).contains(original).doesNotContain(secondary)
                .extracting(Representative::userIdentifier).doesNotContain("no-marker", "\t ");
        assertSearch("& co", "T000001", "T000002");
        assertSearch("sra732", "T000001");
        assertSearch("zz937gr", "T000001");
        assertSearch("dx-second", "T000001");
        assertSearch("SECOND EXCHANGE", "T000001");
        assertSearch("pba-accepted", "T000001");
        assertSearch("pba-pending");
        assertSearch("pba-rejected");
        assertSearch("%th _!\\", "T000006");
        assertSearch("déNé", "T000008");
        assertSearch("  ", "T000003", "T000006", "T000001", "T000004");
        assertSearch("SW1A 1AA", "T000003", "T000007", "T000006", "T000001");
        assertSearch("smith SW1A 1AA", "T000007", "T000001");
        assertSearch("Smith sw1a1aa  ", "T000001");
        assertSearch("smi%th SW1A 1AA", "T000006");
        assertSearch("smith SW1A", "T000007");

        Page first = page("smith SW1A 1AA", 0, 1, Map.of());
        assertThat(ids(first)).containsExactly("T000007");
        assertThat(first.totalRecords()).isEqualTo(2);
        assertThat(first.moreAvailable()).isTrue();
        Page second = page("smith SW1A 1AA", 1, 1, Map.of());
        assertThat(ids(second)).containsExactly("T000001");
        assertThat(second.totalRecords()).isEqualTo(2);
        assertThat(second.moreAvailable()).isFalse();
        Page past = page("smith SW1A 1AA", 2, 1, Map.of());
        assertThat(ids(past)).isEmpty();
        assertThat(past.totalRecords()).isEqualTo(2);

        var row = second.rows().getFirst();
        assertThat(row.representative()).isEqualTo(original);
        var dto = new ObjectMapper().findAndRegisterModules().valueToTree(row.organisation());
        assertThat(dto.at("/contactInformation").size()).isEqualTo(3);
        assertThat(dto.findValuesAsText("dxNumber")).containsExactlyInAnyOrder("DX-FIRST", "DX-SECOND");
        assertThat(dto.at("/paymentAccount/0").asText()).isEqualTo("PBA-ACCEPTED");
        assertThat(dto.at("/pendingPaymentAccount/0").asText()).isEqualTo("PBA-PENDING");
        assertThat(dto.at("/superUser/firstName").asText()).isEqualTo("Stale");
        assertThat(dto.at("/dateReceived").isMissingNode()).isFalse();
        assertThat(dto.at("/organisationProfileIds").isArray()).isTrue();

        assertThat(ids(page("up-match-only", 0, 10, Map.of(original, true)))).containsExactly("T000001");
        assertThat(ids(page("stale admin", 0, 10, Map.of(original, false)))).containsExactly("T000001");
        assertThat(ids(page("stale admin", 0, 10, Map.of()))).isEmpty();
        assertSearch("Blank Admin", "T000011");
        var noAdmin = new ObjectMapper().findAndRegisterModules().valueToTree(
                page("Nomarker", 0, 10, Map.of()).rows().getFirst().organisation());
        assertThat(noAdmin.at("/superUser").isMissingNode()).isTrue();

        jdbc.update("UPDATE dbrefdata.professional_user SET user_identifier = ? WHERE id = ?",
                "replacement", original.rowId());
        assertThat(ids(page("up-match-only", 0, 10, Map.of(original, true)))).isEmpty();
        assertThat(ids(page("stale admin", 0, 10, Map.of(original, false)))).isEmpty();
        var replacement = new Representative(original.rowId(), "replacement");
        assertThat(ids(page("up-match-only", 0, 10, Map.of(replacement, true)))).containsExactly("T000001");

        IntStream.rangeClosed(20, 30).forEach(id -> organisation(id, "Repeated Page", "ACTIVE", null));
        var eleven = page("Repeated Page", 0, 10, Map.of());
        assertThat(eleven.totalRecords()).isEqualTo(11);
        assertThat(eleven.rows()).hasSize(10);
        assertThat(eleven.moreAvailable()).isTrue();
        assertThat(ids(page("Repeated Page", 10, 10, Map.of()))).containsExactly("T000030");
        assertThat(ids(page("", 0, 2, Map.of()))).containsExactly("T000008", "T000011");
    }

    @Test
    void postgresStatementTimeoutCancelsAQueryWaitingOnALock() throws SQLException {
        try (var connection = jdbc.getDataSource().getConnection(); var statement = connection.createStatement()) {
            connection.setAutoCommit(false);
            statement.execute("LOCK TABLE dbrefdata.organisation IN ACCESS EXCLUSIVE MODE");
            try {
                var deadline = new SearchDeadline(Duration.ofMillis(350));
                assertThatThrownBy(() -> search.loadPage("", 0, 10, Map.of(), deadline))
                        .isInstanceOf(QueryTimeoutException.class);
            } finally {
                connection.rollback();
            }
        }
    }

    @AfterEach
    void removeFixture() {
        var first = new UUID(0, 1);
        var last = new UUID(0, 30);
        jdbc.update("DELETE FROM dbrefdata.organisation_mfa_status WHERE organisation_id BETWEEN ? AND ?", first, last);
        jdbc.update("DELETE FROM dbrefdata.organisation WHERE id BETWEEN ? AND ?", first, last);
    }

    private void assertSearch(String term, String... expectedIds) {
        var page = page(term, 0, 20, Map.of());
        assertThat(ids(page)).as("search %s", term).containsExactly(expectedIds);
        assertThat(page.totalRecords()).isEqualTo(expectedIds.length);
        assertThat(page.moreAvailable()).isFalse();
    }

    private Page page(String term, long offset, int size, Map<Representative, Boolean> classifications) {
        return search.loadPage(term, offset, size, classifications, deadline());
    }

    private static SearchDeadline deadline() {
        return new SearchDeadline(Duration.ofSeconds(5));
    }

    private static List<String> ids(Page page) {
        return page.rows().stream().map(row -> row.organisation().getOrganisationIdentifier()).toList();
    }

    private UUID organisation(int number, String name, String status, String sra) {
        var id = new UUID(0, number);
        jdbc.update("""
                INSERT INTO dbrefdata.organisation (id, name, status, organisation_identifier, sra_id,
                    created, last_updated)
                VALUES (?, ?, ?, ?, ?, now(), now())
                """, id, name, status, "T%06d".formatted(number), sra);
        jdbc.update("INSERT INTO dbrefdata.organisation_mfa_status (organisation_id, created) VALUES (?, now())", id);
        return id;
    }

    private UUID contact(UUID organisation, String postcode) {
        var id = UUID.randomUUID();
        jdbc.update("""
                INSERT INTO dbrefdata.contact_information (id, organisation_id, postcode, address_line1,
                    created, last_updated)
                VALUES (?, ?, ?, 'Synthetic road', now(), now())
                """, id, organisation, postcode);
        return id;
    }

    private void dx(UUID contact, String number, String exchange) {
        jdbc.update("""
                INSERT INTO dbrefdata.dx_address (id, contact_information_id, dx_number, dx_exchange,
                    created, last_updated)
                VALUES (?, ?, ?, ?, now(), now())
                """, UUID.randomUUID(), contact, number, exchange);
    }

    private void pba(UUID organisation, String number, String state) {
        jdbc.update("""
                INSERT INTO dbrefdata.payment_account (id, organisation_id, pba_number, pba_status,
                    created, last_updated)
                VALUES (?, ?, ?, ?, now(), now())
                """, UUID.randomUUID(), organisation, number, state);
    }

    private Representative administrator(UUID org, int number, String identifier, String firstName) {
        var id = new UUID(0, number);
        jdbc.update("""
                INSERT INTO dbrefdata.professional_user (id, organisation_id, user_identifier, first_name,
                    last_name, email_address, created, last_updated)
                VALUES (?, ?, ?, ?, 'Admin', ?, timestamp '2026-01-01', now())
                """, id, org, identifier, firstName, firstName + "@example.test");
        jdbc.update("""
                INSERT INTO dbrefdata.user_attribute (id, professional_user_id, prd_enum_code, prd_enum_type)
                VALUES (?, ?, 4, 'ADMIN_ROLE')
                """, UUID.randomUUID(), id);
        return new Representative(id, identifier);
    }
}
