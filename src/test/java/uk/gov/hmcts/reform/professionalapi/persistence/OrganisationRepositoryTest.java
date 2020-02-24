package uk.gov.hmcts.reform.professionalapi.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus;

@RunWith(SpringRunner.class)
@DataJpaTest
@Ignore
public class OrganisationRepositoryTest {

    @Autowired
    OrganisationRepository organisationRepository;

    Organisation organisation = new Organisation("Org-Name", OrganisationStatus.PENDING, "sra-id", "companyN", false, "www.org.com");

    @Before
    public void setUp() {
        organisationRepository.save(organisation);
    }

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
        Organisation activeOrg = new Organisation("Org-Name", OrganisationStatus.ACTIVE, "sra-id", "companyN", false, "www.org.com");
        organisationRepository.save(activeOrg);

        List<Organisation> activeOrganisations = organisationRepository.findByStatus(OrganisationStatus.ACTIVE);

        assertThat(activeOrganisations).hasSize(1);
        assertThat(activeOrganisations.get(0)).isEqualTo(activeOrg);
    }
}
