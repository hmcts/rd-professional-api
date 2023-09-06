package uk.gov.hmcts.reform.professionalapi.dataload.service;

import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

import static java.lang.Long.parseLong;
import static java.lang.System.currentTimeMillis;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static uk.gov.hmcts.reform.professionalapi.dataload.util.DataLoadUtil.getFileDetails;
import static uk.gov.hmcts.reform.professionalapi.dataload.util.DataLoadUtil.isFileExecuted;
import static uk.gov.hmcts.reform.professionalapi.dataload.util.MappingConstants.DB_FILE_NAME;
import static uk.gov.hmcts.reform.professionalapi.dataload.util.MappingConstants.DB_SCHEDULER_START_TIME;
import static uk.gov.hmcts.reform.professionalapi.dataload.util.MappingConstants.DB_STATUS;
import static uk.gov.hmcts.reform.professionalapi.dataload.util.MappingConstants.FAILURE;
import static uk.gov.hmcts.reform.professionalapi.dataload.util.MappingConstants.SCHEDULER_NAME;
import static uk.gov.hmcts.reform.professionalapi.dataload.util.MappingConstants.SCHEDULER_START_TIME;
import static uk.gov.hmcts.reform.professionalapi.dataload.util.MappingConstants.SUCCESS;
import static uk.gov.hmcts.reform.professionalapi.dataload.util.MappingConstants.TABLE_NAME;

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.CamelContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import uk.gov.hmcts.reform.professionalapi.dataload.route.beans.FileStatus;
import uk.gov.hmcts.reform.professionalapi.dataload.service.dto.Audit;

/**
 * This AuditServiceImpl auditing scheduler/file details and logging exceptions.
 *
 * @since 2020-10-27
 */
@Slf4j
@Component
public class AuditServiceImpl implements IAuditService {

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    @Value("${scheduler-insert-sql}")
    protected String schedulerInsertSql;

    @Autowired
    protected PlatformTransactionManager platformTransactionManager;

    @Value("${scheduler-audit-select}")
    protected String getSchedulerAuditDetails;

    @Value("${scheduler-audit-prev-day: "
            + "select count(*) from dataload_schedular_audit where date(scheduler_start_time) = current_date }")
    protected String prevDayAuditDetails;

    @Value("${invalid-exception-sql}")
    String invalidExceptionSql;

    @Value("${archival-file-names}")
    List<String> archivalFileNames;

    @Value("${route.judicial-user-profile-orchestration.file-name:Personal}")
    String fileName;

    /**
     * Capture and log scheduler details with file status.
     *
     * @param camelContext CamelContext
     */
    public void auditSchedulerStatus(final CamelContext camelContext) {

        List<FileStatus> fileStatuses = archivalFileNames.stream().filter(file ->
            isFileExecuted(camelContext, file)).map(s -> getFileDetails(camelContext, s)).collect(toList());

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("Auditing scheduler details");

        Map<String, String> globalOptions = camelContext.getGlobalOptions();
        String schedulerName = globalOptions.get(SCHEDULER_NAME);

        Timestamp schedulerStartTime = new Timestamp(parseLong((globalOptions.get(SCHEDULER_START_TIME))));


        for (FileStatus fileStatus : fileStatuses) {
            String fileAuditStatus = isNotEmpty(fileStatus.getAuditStatus()) ? fileStatus.getAuditStatus() : SUCCESS;
            jdbcTemplate.update(schedulerInsertSql, schedulerName, fileStatus.getFileName(),
                schedulerStartTime,
                new Timestamp(currentTimeMillis()),
                fileAuditStatus);
        }

        TransactionStatus status = platformTransactionManager.getTransaction(def);
        platformTransactionManager.commit(status);
    }

    /**
     * Capture & log scheduler exceptions.
     *
     * @param camelContext CamelContext
     */
    public void auditException(final CamelContext camelContext, String exceptionMessage) {
        Map<String, String> globalOptions = camelContext.getGlobalOptions();
        Timestamp schedulerStartTime = new Timestamp(parseLong((globalOptions.get(SCHEDULER_START_TIME))));
        String schedulerName = globalOptions.get(SCHEDULER_NAME);
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();

        Object[] params = new Object[]{camelContext.getGlobalOptions().get(TABLE_NAME),
            schedulerStartTime, schedulerName, exceptionMessage, new Timestamp(currentTimeMillis())};
        //separate transaction manager required for auditing as it is independent form route
        //Transaction
        jdbcTemplate.update(invalidExceptionSql, params);
        TransactionStatus status = platformTransactionManager.getTransaction(def);
        platformTransactionManager.commit(status);
    }

    /**
     * check auditing is done/not on the current day.
     *
     * @return boolean
     */
    public boolean isAuditingCompleted() {
        return
            Optional.ofNullable(jdbcTemplate.queryForObject(getSchedulerAuditDetails, Integer.class))
                    .orElse(0) > 0;
    }

    /**
     * check auditing is done/not on the previous day.
     *
     * @return boolean
     */
    public boolean isAuditingCompletedPrevDay(Optional<Date> fileTimeStamp) {
        Predicate<Audit> failure = audit -> audit.getStatus().equals(FAILURE);

        return hasDataIngestionRunAfterFileUpload(fileTimeStamp)
                && getPreviousDayAudits().stream().noneMatch(failure);
    }

    public boolean hasDataIngestionRunAfterFileUpload(Optional<Date> fileTimeStamp) {

        Optional<Date> prevDaySchedulerStarTime = getPreviousDayAudits().stream()
                .filter(audit -> audit.getFileName().equals(fileName))
                .map(Audit::getSchedulerStartTime)
                .findFirst();

        return prevDaySchedulerStarTime
                .map(date -> date.after(
                        fileTimeStamp.orElseThrow(() -> new IllegalArgumentException("File Timestamp not found!"))))
                .orElse(false);
    }

    private List<Audit> getPreviousDayAudits() {
        return jdbcTemplate.query(prevDayAuditDetails,
            (ResultSet rs, int rowNum) -> {
                Audit audit = new Audit();
                audit.setFileName(rs.getString(DB_FILE_NAME));
                audit.setSchedulerStartTime(new Date(rs.getTimestamp(DB_SCHEDULER_START_TIME).getTime()));
                audit.setStatus(rs.getString(DB_STATUS));
                return audit;
            });
    }
}
