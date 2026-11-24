package uk.gov.hmcts.reform.professionalapi.dataload.service;

import org.apache.camel.CamelContext;

public interface IAuditService {

    void auditSchedulerStatus(final CamelContext camelContext);

    boolean isAuditingCompleted();

    void auditException(final CamelContext camelContext, String exceptionMessage);
}
