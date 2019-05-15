package uk.gov.hmcts.reform.professionalapi.persistence;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;

@Repository
public interface ProfessionalUserRepository extends JpaRepository<ProfessionalUser, UUID> {
    ProfessionalUser findByEmailAddress(String email);
}
