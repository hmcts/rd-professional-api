package uk.gov.hmcts.reform.professionalapi.repository;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.professionalapi.domain.OrganisationMfaStatus;
import uk.gov.hmcts.reform.professionalapi.helper.BaseRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;


@RunWith(SpringRunner.class)
@DataJpaTest
class OrganisationMfaStatusRepositoryTest extends BaseRepository {

    @Test
    void test_findAll() {
        List<OrganisationMfaStatus> organisationMfaStatuses = organisationMfaStatusRepository.findAll();

        assertThat(organisationMfaStatuses).hasSize(1);
        assertThat(organisationMfaStatuses.get(0)).isEqualTo(organisationMfaStatus);
        assertThat(organisationMfaStatuses.get(0).getOrganisationId())
                .isEqualTo(organisationMfaStatus.getOrganisationId());
    }

    @Test
    void test_findById() {
        Optional<OrganisationMfaStatus> orgMfaStatus
                = organisationMfaStatusRepository.findById(organisationMfaStatus.getOrganisationId());

        assertThat(orgMfaStatus).contains(organisationMfaStatus);
        assertThat(orgMfaStatus.get().getOrganisationId()).isEqualTo(organisationMfaStatus.getOrganisationId());
    }


}
