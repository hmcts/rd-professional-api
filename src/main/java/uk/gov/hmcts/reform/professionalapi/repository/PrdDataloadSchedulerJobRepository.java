package uk.gov.hmcts.reform.professionalapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.gov.hmcts.reform.professionalapi.domain.PrdDataloadSchedulerJob;

public interface PrdDataloadSchedulerJobRepository extends JpaRepository<PrdDataloadSchedulerJob, Long> {

    PrdDataloadSchedulerJob findFirstByOrderByIdDesc();
}
