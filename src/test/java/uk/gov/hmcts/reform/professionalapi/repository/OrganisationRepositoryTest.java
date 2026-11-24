package uk.gov.hmcts.reform.professionalapi.repository;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus;
import uk.gov.hmcts.reform.professionalapi.helper.BaseRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@DataJpaTest
class OrganisationRepositoryTest extends BaseRepository {

    @Test
    void test_findAll() {
        List<Organisation> organisations = organisationRepository.findAll();

        assertThat(organisations).hasSize(1);
        assertThat(organisations.get(0)).isEqualTo(organisation);
        assertThat(organisations.get(0).getOrganisationIdentifier())
                .isEqualTo(organisation.getOrganisationIdentifier());
    }

    @Test
    void test_findByOrganisationIdentifier() {
        Organisation org = organisationRepository.findByOrganisationIdentifier(organisation
                .getOrganisationIdentifier());

        assertThat(org).isEqualTo(organisation);
        assertThat(org.getOrganisationIdentifier()).isEqualTo(organisation.getOrganisationIdentifier());
    }

    @Test
    void test_findByStatus() {
        List<Organisation> activeOrganisations = organisationRepository.findByStatus(OrganisationStatus.ACTIVE);

        assertThat(activeOrganisations).hasSize(1);
        assertThat(activeOrganisations.get(0)).isEqualTo(organisation);
        assertThat(activeOrganisations.get(0).getOrganisationIdentifier()).isEqualTo(organisation
                .getOrganisationIdentifier());
    }

    @Test
    void test_findByName() {
        Organisation org = organisationRepository.findByName(organisation.getName());
        assertThat(org).isEqualTo(organisation);
        assertThat(org.getOrganisationIdentifier()).isEqualTo(organisation.getOrganisationIdentifier());
    }

    @Test
    void test_findByCompanyNumber() {
        Organisation org = organisationRepository.findByCompanyNumber(organisation.getCompanyNumber());
        assertThat(org).isEqualTo(organisation);
        assertThat(org.getOrganisationIdentifier()).isEqualTo(organisation.getOrganisationIdentifier());
    }
}
