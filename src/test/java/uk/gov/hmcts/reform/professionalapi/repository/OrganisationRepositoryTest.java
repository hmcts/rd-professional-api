package uk.gov.hmcts.reform.professionalapi.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus;
import uk.gov.hmcts.reform.professionalapi.helper.RepositorySetUp;

@RunWith(SpringRunner.class)
@DataJpaTest
public class OrganisationRepositoryTest extends RepositorySetUp {

    @Test
    public void test_findAll() {
        List<Organisation> organisations = organisationRepository.findAll();

        assertThat(organisations).hasSize(1);
        assertThat(organisations.get(0)).isEqualTo(organisation);
    }

    @Test
    public void test_findByOrganisationIdentifier() {
        Organisation org = organisationRepository.findByOrganisationIdentifier(organisation.getOrganisationIdentifier());

        assertThat(org).isEqualTo(organisation);
    }

    @Test
    public void test_findByStatus() {
        List<Organisation> activeOrganisations = organisationRepository.findByStatus(OrganisationStatus.ACTIVE);

        assertThat(activeOrganisations).hasSize(1);
        assertThat(activeOrganisations.get(0)).isEqualTo(organisation);
    }
}
