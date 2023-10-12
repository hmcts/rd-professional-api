package uk.gov.hmcts.reform.professionalapi.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.professionalapi.domain.PrdDataloadSchedulerJob;
import uk.gov.hmcts.reform.professionalapi.repository.PrdDataloadSchedulerJobRepository;

@Component
@Slf4j
public class PrdDataLoadSchedulerAudit {

    @Autowired
    PrdDataloadSchedulerJobRepository prdDataloadSchedulerJobRepository;

    @Value("${loggingComponentName}")
    private String loggingComponentName;

    public PrdDataloadSchedulerJob auditSchedulerJobStatus(PrdDataloadSchedulerJob audit) {

        try {

            prdDataloadSchedulerJobRepository.save(audit);
        } catch (Exception e) {
            log.error("{}:: Failure error Message {} in auditSchedulerStatus  ",
                    loggingComponentName, e.getMessage());
        }

        return audit;
    }
}
