package uk.gov.hmcts.reform.professionalapi.audit;

import java.time.LocalDateTime;

import static org.slf4j.LoggerFactory.getLogger;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import uk.gov.hmcts.reform.professionalapi.domain.Audit;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.repository.AuditDetailsRepository;

@Aspect
@Service
public class AuditDetailsAspect {
    @Autowired
    AuditDetailsRepository auditDetailsRepository;

    private static final Logger LOG = getLogger(AuditDetailsAspect.class);

    /**
     * Log details of organisation address update after the service method is executed.
     */
    @AfterReturning(
        value = "execution(* uk.gov.hmcts.reform.professionalapi.service.impl.OrganisationServiceImpl.updateOrganisationAddress(..))",
        returning = "result"
    )
    public void logAfterUpdate(JoinPoint joinPoint, Object result) {
        Audit auditDetails = new Audit();
        // Extract method name
        String methodName = joinPoint.getSignature().getName();

        // Log method name
        System.out.println("****************Method '{}' executed for updating organisation address."+ methodName);

        // Log input arguments
        Object[] args = joinPoint.getArgs();
        if (args != null && args.length == 2) {
            String orgIdentifier = ((Organisation) args[0]).getOrganisationIdentifier();
            String changeDetails = args[1].toString();
            auditDetails.setOrganisationIdentifier(orgIdentifier);
            auditDetails.setChangeDetails(changeDetails);
            System.out.println("****************Existing Organisation Id: {}"+ ((Organisation) args[0]).getOrganisationIdentifier());
            System.out.println("****************Address Update Details: {}"+ args[1]);

        }

        // Log returned response
        if (result instanceof ResponseEntity) {
            ResponseEntity<?> response = (ResponseEntity<?>) result;
            System.out.println("****************Response Status: {}"+ response.getStatusCode());
            System.out.println("****************Response Body: {}"+ response.getBody());
        } else {
            System.out.println("****************Method returned: {}"+ result);
        }
        auditDetails.setUpdatedBy(args[2].toString());
        auditDetails.setChangeAction(methodName);
        auditDetails.setLastUpdated(LocalDateTime.now());
        Audit savedAuditInformation = auditDetailsRepository.save(auditDetails);
    }
}


