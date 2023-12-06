package uk.gov.hmcts.reform.professionalapi.dataload.util;


import org.apache.commons.lang3.StringUtils;

import java.util.function.Predicate;

public class MappingConstants {

    private MappingConstants() {
    }

    public static final String DATE_FORMAT = "yyyy-MM-dd hh:mm:ss";

    public static final String SCHEDULER_START_TIME = "start-time";

    public static final long MILLIS_IN_A_DAY = 86400000;

    public static final String START_ROUTE = "start-route";

    public static final String DIRECT_JRD = "direct:JRD";

    public static final String ROUTE = "route";

    public static final String INSERT_SQL = "insert-sql";

    public static final String UPDATE_SQL = "update-sql";

    public static final String DELETE_SQL = "delete-sql";

    public static final String DEFERRED_SQL = "deferred-sql";

    public static final String TRUNCATE_SQL = "truncate-sql";

    public static final String BLOBPATH = "blob-path";

    public static final String PROCESSOR = "processor-class";

    public static final String CSVBINDER = "csv-binder-object";

    public static final String MAPPER = "mapper-class";

    public static final String MAPPING_METHOD = "getMap";

    public static final String SQL_DELIMITER = "##";

    public static final String ID = "id";

    public static final String FILE_NAME = "file-name";

    public static final String TABLE_NAME = "table-name";

    public static final String HEADER_EXCEPTION = "header-exception";

    public static final String ROUTE_DETAILS = "routedetails";

    public static final String DIRECT_ROUTE = "direct:";

    public static final String TRUNCATE_ROUTE_PREFIX = "truncate";

    public static final String IS_EXCEPTION_HANDLED = "is-exception-handled";

    public static final String SCHEDULER_STATUS = "SchedulerStatus";

    public static final String SCHEDULER_NAME = "SchedulerName";

    public static final String PARTIAL_SUCCESS = "PartialSuccess";

    public static final String FAILURE = "Failure";

    public static final String SUCCESS = "Success";

    public static final String NOT_STALE_FILE = "NotStale";

    public static final String ERROR_MESSAGE = "ErrorMessage";

    public static final String IS_FILE_STALE = "ISFILESTALE";

    public static final String FILE_NOT_EXISTS = "%s file does not exist in azure storage account";

    public static final String STALE_FILE_ERROR = "%s file with timestamp %s not loaded due to file stale error";

    public static final String EXECUTION_FAILED = "%s Execution Failed for files ";

    public static final String CSV_HEADERS_EXPECTED = "csv-headers-expected";

    public static final String IS_HEADER_VALIDATION_ENABLED = "header-validation-enabled";

    public static final String COMA = ",";

    public static final String PARENT_FAILURE_ENABLED = "parent-failure-enabled";

    public static final String IS_PARENT_FAILED = "ISPARENTFAILED";

    //parent-name
    public static final String PARENT_NAME = "parent-name";

    public static final Predicate<String> IS_NOT_BLANK = StringUtils::isNotBlank;
    public static final Predicate<String> IS_START_ROUTE_JRD = startRoute -> startRoute.equals(DIRECT_JRD);
    public static final String DB_FILE_NAME = "file_name";
    public static final String DB_SCHEDULER_START_TIME = "scheduler_start_time";
    public static final String DB_STATUS = "status";
    public static final String IS_READY_TO_AUDIT = "IS_READY_TO_AUDIT";
    public static final String DATE_TIME_FORMAT = "dd-MM-yyyy HH:mm:ss";
}
