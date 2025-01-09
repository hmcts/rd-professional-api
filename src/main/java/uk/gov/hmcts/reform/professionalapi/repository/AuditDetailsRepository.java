package uk.gov.hmcts.reform.professionalapi.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.professionalapi.domain.Audit;

@Repository
public interface AuditDetailsRepository extends JpaRepository<Audit, UUID> {

}
