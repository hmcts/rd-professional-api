package uk.gov.hmcts.reform.professionalapi.domain.service.persistence;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import uk.gov.hmcts.reform.professionalapi.domain.entities.ProfessionalUser;

public interface ProfessionalUserRepository extends JpaRepository<ProfessionalUser, UUID> {
}
