package uk.gov.hmcts.reform.professionalapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.professionalapi.domain.Audit;

import java.util.UUID;

@Repository
public interface AuditDetailsRepository extends JpaRepository<Audit, UUID> {

}
