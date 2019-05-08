package uk.gov.hmcts.reform.professionalapi.service.persistence;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;

public interface ProfessionalUserRepository extends JpaRepository<ProfessionalUser, UUID> {
}
