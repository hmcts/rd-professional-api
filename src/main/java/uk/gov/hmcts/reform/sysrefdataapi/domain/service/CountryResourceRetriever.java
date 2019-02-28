package uk.gov.hmcts.reform.sysrefdataapi.domain.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sysrefdataapi.domain.entities.Country;

@Service
public class CountryResourceRetriever implements ResourceRetriever {

    private static final Logger LOG = LoggerFactory.getLogger(CountryResourceRetriever.class);

    private JdbcTemplate jdbcTemplate;

    public CountryResourceRetriever(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Country getResource(String id) {

        LOG.info("\n\n ****** Getting Resource ****** with id: {}", id);

        final String query = "SELECT country_name FROM country WHERE id = ?";

        String countryName =
            jdbcTemplate.queryForObject(
                query,
                new Object[]{id},
                String.class);

        LOG.info("\n\n ****** Country Name ******: {}", countryName);

        return new Country(id, countryName);

    }
}
