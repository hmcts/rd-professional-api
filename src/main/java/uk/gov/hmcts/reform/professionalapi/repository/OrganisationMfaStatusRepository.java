package uk.gov.hmcts.reform.professionalapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.gov.hmcts.reform.professionalapi.domain.OrganisationMfaStatus;

import java.util.UUID;

public interface OrganisationMfaStatusRepository extends JpaRepository<OrganisationMfaStatus, UUID> {
}
