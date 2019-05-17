package uk.gov.hmcts.reform.professionalapi.persistence;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUserStatus;

@Repository
public interface ProfessionalUserRepository extends JpaRepository<ProfessionalUser, UUID> {
    ProfessionalUser findByEmailAddress(String email);
    List<ProfessionalUser> findByOrganisationIdentifierAndStatusNot(UUID organisationIdentifier, ProfessionalUserStatus status);
    List<ProfessionalUser> findByOrganisationIdentifier(UUID organisationIdentifier);
}
