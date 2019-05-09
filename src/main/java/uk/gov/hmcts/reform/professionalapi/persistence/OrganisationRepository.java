package uk.gov.hmcts.reform.professionalapi.persistence;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;

public interface OrganisationRepository extends JpaRepository<Organisation, UUID> {

    Organisation findByName(String name);

    Organisation findByOrganisationIdentifier(UUID organisationIdentifier);

    Organisation findByUsers(ProfessionalUser user);
}
