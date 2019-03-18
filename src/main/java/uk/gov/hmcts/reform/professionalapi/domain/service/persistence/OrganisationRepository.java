package uk.gov.hmcts.reform.professionalapi.domain.service.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.gov.hmcts.reform.professionalapi.domain.entities.Organisation;

public interface OrganisationRepository extends JpaRepository<Organisation, String> {

    Organisation findByName(String name);
}
