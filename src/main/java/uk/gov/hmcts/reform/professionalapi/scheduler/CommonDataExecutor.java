package uk.gov.hmcts.reform.professionalapi.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.CamelContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.data.ingestion.camel.service.AuditServiceImpl;
import uk.gov.hmcts.reform.data.ingestion.camel.util.RouteExecutor;

import static org.apache.commons.lang.StringUtils.isNotBlank;
import static uk.gov.hmcts.reform.data.ingestion.camel.util.MappingConstants.ERROR_MESSAGE;
import static uk.gov.hmcts.reform.data.ingestion.camel.util.MappingConstants.FAILURE;

@Slf4j
@Component
@RefreshScope
public class CommonDataExecutor extends RouteExecutor {

    private static final String IS_READY_TO_AUDIT = "IS_READY_TO_AUDIT";

    @Value("${logging-component-name}")
    String logComponentName;

    @Autowired
    AuditServiceImpl auditService;

    /**
     * Execute Route and Insert data in Audit Table.
     *
     * @param camelContext  contain Auditing Camel Context
     * @param schedulerName Name of the Scheduler
     * @param route         Name of the Route
     * @return Job Status Success or Failure
     */
    @Override
    public String execute(CamelContext camelContext, String schedulerName, String route) {
        try {
            return super.execute(camelContext, schedulerName, route);
        } catch (Exception ex) {
            var errorMessage = camelContext.getGlobalOptions().get(ERROR_MESSAGE);
            auditService.auditException(camelContext, errorMessage);
            log.error("{}:: {} failed:: {}", logComponentName, schedulerName, errorMessage);
            return FAILURE;
        } finally {
            var isReadyToAudit = camelContext.getGlobalOption(IS_READY_TO_AUDIT);
            if (isNotBlank(isReadyToAudit) && Boolean.TRUE.toString().equalsIgnoreCase(isReadyToAudit)) {
                auditService.auditSchedulerStatus(camelContext);
            }
        }
    }
}
