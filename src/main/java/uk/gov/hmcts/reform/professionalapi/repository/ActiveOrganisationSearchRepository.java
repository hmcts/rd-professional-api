package uk.gov.hmcts.reform.professionalapi.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.SqlArrayValue;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;
import uk.gov.hmcts.reform.professionalapi.controller.response.OrganisationEntityResponse;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.SuperUser;
import uk.gov.hmcts.reform.professionalapi.service.impl.SearchDeadline;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.regex.Pattern;

@Repository
public class ActiveOrganisationSearchRepository {

    private static final Pattern TRAILING_POSTCODE = Pattern.compile(
            "(?iU)(?:^|\\s)(GIR ?0AA|[A-PR-UWYZ](?:[0-9][0-9A-HJKPSTUW]?|[A-HK-Y][0-9][0-9ABEHMNPRVWXY]?)"
                    + " ?[0-9][ABD-HJLNP-UW-Z]{2})\\s*\\z");

    private static final String REPRESENTATIVE = """
            LEFT JOIN LATERAL (
                SELECT s.id, s.user_identifier, s.first_name, s.last_name
                FROM dbrefdata.super_user_view s
                WHERE s.organisation_id = o.id
                ORDER BY s.created ASC NULLS LAST, s.id ASC
                LIMIT 1
            ) r ON true
            """;

    private static final String USABLE_ID = "r.user_identifier !~ '^[[:space:]]*$'";

    private static final String CLASSIFICATION = """
            WITH classification AS (
                SELECT * FROM unnest(:rowIds, :userIds, :matched) AS p(row_id, user_identifier, matched)
            )
            """;

    private static final String SEARCH_FILTER = """
            LEFT JOIN classification p ON p.row_id = r.id AND p.user_identifier = r.user_identifier
            WHERE o.status = 'ACTIVE' AND (
                lower(o.name) LIKE lower(:term) ESCAPE '!'
                OR lower(o.sra_id) LIKE lower(:term) ESCAPE '!'
                OR EXISTS (
                    SELECT 1 FROM dbrefdata.contact_information c
                    WHERE c.organisation_id = o.id AND replace(c.postcode, ' ', '') <> ''
                      AND lower(replace(c.postcode, ' ', '')) LIKE lower(:postcodeTerm) ESCAPE '!'
                )
                OR EXISTS (
                    SELECT 1 FROM dbrefdata.payment_account a
                    WHERE a.organisation_id = o.id AND a.pba_status = 'ACCEPTED'
                      AND lower(a.pba_number) LIKE lower(:term) ESCAPE '!'
                )
                OR EXISTS (
                    SELECT 1 FROM dbrefdata.contact_information c
                    JOIN dbrefdata.dx_address d ON d.contact_information_id = c.id
                    WHERE c.organisation_id = o.id
                      AND (lower(d.dx_number) LIKE lower(:term) ESCAPE '!'
                           OR lower(d.dx_exchange) LIKE lower(:term) ESCAPE '!')
                )
                OR p.matched
                OR ((p.matched = false OR not coalesce(%s, false))
                    AND lower(r.first_name || ' ' || r.last_name) LIKE lower(:term) ESCAPE '!')
            """.formatted(USABLE_ID);

    private static final String COMBINED_FILTER = """
                OR (lower(o.name) LIKE lower(:namePattern) ESCAPE '!'
                    AND EXISTS (
                        SELECT 1 FROM dbrefdata.contact_information c
                        WHERE c.organisation_id = o.id
                          AND lower(replace(c.postcode, ' ', '')) = lower(:compactPostcode)
                    )
                )
            """;

    private static final String ACTIVE_ORGANISATIONS = "FROM dbrefdata.organisation o\n";
    private static final String ACTIVE_FILTER = "WHERE o.status = 'ACTIVE'\n";
    private static final String PAGE_ORDER = "ORDER BY lower(o.name), o.id OFFSET :offset LIMIT :size\n";
    private static final String PAGE_SELECT = "SELECT o.id, r.id AS row_id, CASE WHEN " + USABLE_ID
            + " THEN r.user_identifier END AS user_identifier\n";

    private final NamedParameterJdbcTemplate jdbc;
    private final PlatformTransactionManager transactionManager;

    @PersistenceContext
    private EntityManager entityManager;

    public ActiveOrganisationSearchRepository(NamedParameterJdbcTemplate jdbc,
                                              PlatformTransactionManager transactionManager) {
        this.jdbc = jdbc;
        this.transactionManager = transactionManager;
    }

    public List<Representative> candidates(SearchDeadline deadline) {
        return inSnapshot(deadline, () -> jdbc.query("SELECT r.id AS row_id, r.user_identifier\n"
                        + ACTIVE_ORGANISATIONS + REPRESENTATIVE + ACTIVE_FILTER + "AND " + USABLE_ID + " ORDER BY o.id",
                (rs, row) -> new Representative(rs.getObject("row_id", UUID.class), rs.getString("user_identifier"))));
    }

    public Page loadPage(String term, long offset, int size, Map<Representative, Boolean> classifications,
                         SearchDeadline deadline) {
        return inSnapshot(deadline, () -> selectPage(term, offset, size, classifications, deadline));
    }

    private Page selectPage(String term, long offset, int size, Map<Representative, Boolean> classifications,
                            SearchDeadline deadline) {
        var params = new MapSqlParameterSource().addValue("offset", offset).addValue("size", size);
        String countQuery = "SELECT count(*)\n" + ACTIVE_ORGANISATIONS + ACTIVE_FILTER;
        String pageQuery = PAGE_SELECT + "FROM (SELECT o.id, o.name\n" + ACTIVE_ORGANISATIONS
                + ACTIVE_FILTER + PAGE_ORDER + ") o\n" + REPRESENTATIVE + "ORDER BY lower(o.name), o.id\n";

        if (!term.isEmpty()) {
            var entries = new ArrayList<>(classifications.entrySet());
            params.addValue("term", literalContains(term))
                    .addValue("postcodeTerm", literalContains(term.replace(" ", "")))
                    .addValue("rowIds", new SqlArrayValue("uuid",
                            entries.stream().map(e -> e.getKey().rowId()).toArray()))
                    .addValue("userIds", new SqlArrayValue("text",
                            entries.stream().map(e -> e.getKey().userIdentifier()).toArray()))
                    .addValue("matched", new SqlArrayValue("boolean", entries.stream().map(Map.Entry::getValue)
                            .toArray()));
            String search = ACTIVE_ORGANISATIONS + REPRESENTATIVE + SEARCH_FILTER
                    + combinedFilter(term, params) + ")\n";
            countQuery = CLASSIFICATION + "SELECT count(*)\n" + search;
            pageQuery = CLASSIFICATION + PAGE_SELECT + search + PAGE_ORDER;
        }

        long total = jdbc.queryForObject(countQuery, params, Long.class);
        if (offset >= total) {
            return new Page(List.of(), total, false);
        }
        applyStatementTimeout(deadline);
        var selections = jdbc.query(pageQuery, params, (rs, row) -> new Selection(rs.getObject("id", UUID.class),
                new Representative(rs.getObject("row_id", UUID.class), rs.getString("user_identifier"))));

        List<Row> rows = new ArrayList<>(selections.size());
        for (int first = 0; first < selections.size(); first += 1000) {
            rows.addAll(hydrate(selections.subList(first, Math.min(first + 1000, selections.size())), deadline));
        }
        return new Page(rows, total, offset + rows.size() < total);
    }

    private List<Row> hydrate(List<Selection> selections, SearchDeadline deadline) {
        applyStatementTimeout(deadline);
        var organisations = entityManager.createQuery("from Organisation o where o.id in :ids", Organisation.class)
                .setParameter("ids", selections.stream().map(Selection::organisationId).toList())
                .getResultList();
        var organisationsById = new HashMap<UUID, Organisation>();
        organisations.forEach(org -> organisationsById.put(org.getId(), org));
        var representativeIds = selections.stream().map(s -> s.representative().rowId())
                .filter(Objects::nonNull).toList();
        var usersById = new HashMap<UUID, SuperUser>();
        if (!representativeIds.isEmpty()) {
            applyStatementTimeout(deadline);
            var users = entityManager.createQuery("from super_user_view u where u.id in :ids", SuperUser.class)
                    .setParameter("ids", representativeIds).getResultList();
            users.forEach(user -> usersById.put(user.getId(), user));
        }
        return selections.stream().map(selected -> {
            deadline.remainingNanos();
            Organisation organisation = organisationsById.get(selected.organisationId());
            if (organisation == null) {
                throw new IllegalStateException("Selected organisation was absent from the search snapshot");
            }
            Representative rep = selected.representative();
            SuperUser user = usersById.get(rep.rowId());
            if (rep.rowId() != null && user == null) {
                throw new IllegalStateException("Selected administrator was absent from the search snapshot");
            }
            return new Row(new OrganisationEntityResponse(organisation, true, true, false, user), rep);
        }).toList();
    }

    private <T> T inSnapshot(SearchDeadline deadline, Supplier<T> read) {
        var transaction = new TransactionTemplate(transactionManager);
        transaction.setReadOnly(true);
        transaction.setIsolationLevel(TransactionDefinition.ISOLATION_REPEATABLE_READ);
        transaction.setTimeout((int) ((deadline.remainingMillis() + 999) / 1000));
        return transaction.execute(status -> {
            applyStatementTimeout(deadline);
            T result = read.get();
            deadline.remainingNanos();
            return result;
        });
    }

    private void applyStatementTimeout(SearchDeadline deadline) {
        jdbc.queryForObject("SELECT set_config('statement_timeout', :timeout, true)",
                Map.of("timeout", Long.toString(deadline.remainingMillis())), String.class);
    }

    private static String combinedFilter(String term, MapSqlParameterSource params) {
        var postcode = TRAILING_POSTCODE.matcher(term);
        if (!postcode.find()) {
            return "";
        }
        String name = term.substring(0, postcode.start()).strip();
        if (name.isEmpty()) {
            return "";
        }
        params.addValue("namePattern", literalContains(name));
        params.addValue("compactPostcode", postcode.group(1).replace(" ", ""));
        return COMBINED_FILTER;
    }

    private static String literalContains(String value) {
        return "%" + value.replace("!", "!!").replace("%", "!%").replace("_", "!_") + "%";
    }

    private record Selection(UUID organisationId, Representative representative) {
    }

    public record Representative(UUID rowId, String userIdentifier) {
    }

    public record Row(OrganisationEntityResponse organisation, Representative representative) {
    }

    public record Page(List<Row> rows, long totalRecords, boolean moreAvailable) {
    }
}
