package uk.gov.hmcts.reform.professionalapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.professionalapi.domain.PrdDataSchedularAudit;

import java.time.LocalDateTime;

@Repository
public interface PrdSchedularAuditRepository extends JpaRepository<PrdDataSchedularAudit, Long> {


    @Query(value = "SELECT MAX(scheduler_end_time) FROM dbrefdata.dataload_schedular_audit "
        + " WHERE scheduler_name = 'PRD Route' ", nativeQuery = true)
    LocalDateTime findLatestSchedularEndTime();

}
