package uk.gov.hmcts.reform.professionalapi.persistence;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;

@Repository
public interface OrganisationRepository extends JpaRepository<Organisation, UUID> {

    Organisation findByName(String name);

    Organisation findByOrganisationIdentifier(UUID organisationIdentifier);
}
