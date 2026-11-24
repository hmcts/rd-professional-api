package uk.gov.hmcts.reform.professionalapi.dataload.service;


import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.support.SimpleRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import uk.gov.hmcts.reform.professionalapi.dataload.service.dto.Audit;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.ReflectionTestUtils.setField;
import static uk.gov.hmcts.reform.professionalapi.dataload.util.MappingConstants.DB_FILE_NAME;
import static uk.gov.hmcts.reform.professionalapi.dataload.util.MappingConstants.DB_SCHEDULER_START_TIME;
import static uk.gov.hmcts.reform.professionalapi.dataload.util.MappingConstants.DB_STATUS;
import static uk.gov.hmcts.reform.professionalapi.dataload.util.MappingConstants.FAILURE;
import static uk.gov.hmcts.reform.professionalapi.dataload.util.MappingConstants.SCHEDULER_NAME;
import static uk.gov.hmcts.reform.professionalapi.dataload.util.MappingConstants.SCHEDULER_START_TIME;
import static uk.gov.hmcts.reform.professionalapi.dataload.util.MappingConstants.SUCCESS;

public class AuditServiceImplTest {

    JdbcTemplate mockJdbcTemplate = mock(JdbcTemplate.class);
    AuditServiceImpl dataLoadAuditUnderTest = spy(new AuditServiceImpl());
    PlatformTransactionManager platformTransactionManager = mock(PlatformTransactionManager.class);
    TransactionStatus transactionStatus = mock(TransactionStatus.class);

    Exchange exchange = mock(Exchange.class);
    CamelContext camelContext = spy(new DefaultCamelContext());
    SimpleRegistry registry = spy(new SimpleRegistry());

    final String schedulerName = "judicial_main_scheduler";

    public static Map<String, String> getGlobalOptions(String schedulerName) {
        Map<String, String> globalOptions = new HashMap<>();
        globalOptions.put("ORCHESTRATED_ROUTE", "JUDICIAL_REF_DATA_ORCHESTRATION");
        globalOptions.put(SCHEDULER_START_TIME, String.valueOf(new Date().getTime()));
        globalOptions.put(SCHEDULER_NAME, schedulerName);
        return globalOptions;
    }

    @BeforeEach
    public void setUp() {
        List<String> files = new ArrayList<>();
        files.add("test");
        files.add("test1");
        setField(dataLoadAuditUnderTest, "jdbcTemplate", mockJdbcTemplate);
        setField(dataLoadAuditUnderTest, "platformTransactionManager", platformTransactionManager);
        setField(dataLoadAuditUnderTest, "invalidExceptionSql", "select * from appointment");
        setField(dataLoadAuditUnderTest, "archivalFileNames", files);
        setField(dataLoadAuditUnderTest, "fileName", "Personal");
        setField(dataLoadAuditUnderTest, "prevDayAuditDetails",
                "select file_name, scheduler_start_time, status from dataload_schedular_audit "
                        + "where date(scheduler_start_time) = current_date - INTEGER '1'");
        //when(camelContext.getRegistry()).thenReturn(registry);
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testSchedulerAuditUpdate() throws Exception {

        Map<String, String> globalOptions = getGlobalOptions(schedulerName);
        when(exchange.getContext()).thenReturn(camelContext);
        when(exchange.getContext().getGlobalOptions()).thenReturn(globalOptions);
        when(mockJdbcTemplate.update(anyString(), anyString(), any(), any(), any())).thenReturn(1);
        when(platformTransactionManager.getTransaction(any())).thenReturn(transactionStatus);
        doNothing().when(platformTransactionManager).commit(transactionStatus);
        String[] files = new String[]{"test"};
        dataLoadAuditUnderTest.auditSchedulerStatus(camelContext);
        verify(dataLoadAuditUnderTest).auditSchedulerStatus(camelContext);
        verify(exchange, times(1)).getContext();
        verify(camelContext, times(1)).getGlobalOptions();
        verify(platformTransactionManager, times(1)).getTransaction(any());
        verify(platformTransactionManager, times(1)).commit(transactionStatus);
    }

    @Test
    public void testAuditException() {
        Map<String, String> globalOptions = getGlobalOptions(schedulerName);
        when(exchange.getContext()).thenReturn(camelContext);
        when(exchange.getContext().getGlobalOptions()).thenReturn(globalOptions);
        when(mockJdbcTemplate.update(any(), any(Object[].class))).thenReturn(1);
        when(platformTransactionManager.getTransaction(any())).thenReturn(transactionStatus);
        doNothing().when(platformTransactionManager).commit(transactionStatus);
        dataLoadAuditUnderTest.auditException(camelContext, "exceptionMessage");
        verify(dataLoadAuditUnderTest).auditException(camelContext, "exceptionMessage");
        verify(exchange, times(1)).getContext();
        verify(mockJdbcTemplate, times(1)).update(any(), (Object[]) any());
        verify(platformTransactionManager, times(1)).getTransaction(any());
        verify(platformTransactionManager, times(1)).commit(transactionStatus);
    }

    @SuppressWarnings("unchecked")
    @Test
    void should_return_false_when_previous_day_scheduler_start_time_is_before_file_timestamp() {
        Optional<Date> fileTS = Optional.of(getDate(2021, 11, 16, 8, 30, 0));
        Date utilDate = getDate(2021, 11, 15, 10, 0, 0);
        java.sql.Timestamp prevDaySchedulerST = new java.sql.Timestamp(utilDate.getTime());

        when(mockJdbcTemplate.query(anyString(), any(RowMapper.class))).thenAnswer(invocation -> {
            ResultSet rs = mock(ResultSet.class);

            when(rs.getString(DB_FILE_NAME))
                    .thenReturn("AdditionalInfoRoles",
                                "Appointments",
                                        "Authorisations",
                                        "BaseLocations",
                                        "Locations",
                                        "Personal");

            when(rs.getTimestamp(DB_SCHEDULER_START_TIME))
                    .thenReturn(prevDaySchedulerST,
                                prevDaySchedulerST,
                                prevDaySchedulerST,
                                prevDaySchedulerST,
                                prevDaySchedulerST,
                                prevDaySchedulerST);

            when(rs.getString(DB_STATUS))
                    .thenReturn(SUCCESS,
                                SUCCESS,
                                SUCCESS,
                                SUCCESS,
                                SUCCESS,
                                SUCCESS);

            RowMapper<Audit> rowMapper = (RowMapper<Audit>) invocation.getArgument(1);
            return List.of(Objects.requireNonNull(rowMapper.mapRow(rs, 0)),
                    Objects.requireNonNull(rowMapper.mapRow(rs, 1)),
                    Objects.requireNonNull(rowMapper.mapRow(rs, 2)),
                    Objects.requireNonNull(rowMapper.mapRow(rs, 3)),
                    Objects.requireNonNull(rowMapper.mapRow(rs, 4)),
                    Objects.requireNonNull(rowMapper.mapRow(rs, 5)));
        });

        assertFalse(dataLoadAuditUnderTest.isAuditingCompletedPrevDay(fileTS));
    }

    @SuppressWarnings("unchecked")
    @Test
    void should_return_true_when_previous_day_scheduler_start_time_is_after_file_timestamp_with_success() {
        Optional<Date> fileTS = Optional.of(getDate(2021, 11, 16, 8, 30, 0));
        Date utilDate = getDate(2021, 11, 16, 10, 0, 0);
        java.sql.Timestamp prevDaySchedulerST = new java.sql.Timestamp(utilDate.getTime());

        when(mockJdbcTemplate.query(anyString(), any(RowMapper.class))).thenAnswer(invocation -> {
            ResultSet rs = mock(ResultSet.class);

            when(rs.getString(DB_FILE_NAME))
                    .thenReturn("AdditionalInfoRoles",
                            "Appointments",
                            "Authorisations",
                            "BaseLocations",
                            "Locations",
                            "Personal");

            when(rs.getTimestamp(DB_SCHEDULER_START_TIME))
                    .thenReturn(prevDaySchedulerST,
                            prevDaySchedulerST,
                            prevDaySchedulerST,
                            prevDaySchedulerST,
                            prevDaySchedulerST,
                            prevDaySchedulerST);

            when(rs.getString(DB_STATUS))
                    .thenReturn(SUCCESS,
                            SUCCESS,
                            SUCCESS,
                            SUCCESS,
                            SUCCESS,
                            SUCCESS);

            RowMapper<Audit> rowMapper = (RowMapper<Audit>) invocation.getArgument(1);

            return List.of(Objects.requireNonNull(rowMapper.mapRow(rs, 0)),
                    Objects.requireNonNull(rowMapper.mapRow(rs, 1)),
                    Objects.requireNonNull(rowMapper.mapRow(rs, 2)),
                    Objects.requireNonNull(rowMapper.mapRow(rs, 3)),
                    Objects.requireNonNull(rowMapper.mapRow(rs, 4)),
                    Objects.requireNonNull(rowMapper.mapRow(rs, 5)));
        });

        assertTrue(dataLoadAuditUnderTest.isAuditingCompletedPrevDay(fileTS));
    }

    @SuppressWarnings("unchecked")
    @Test
    void should_return_false_when_previous_day_scheduler_start_time_is_after_file_timestamp_with_failure() {
        Optional<Date> fileTS = Optional.of(getDate(2021, 11, 16, 8, 30, 0));
        Date utilDate = getDate(2021, 11, 16, 10, 0, 0);
        java.sql.Timestamp prevDaySchedulerST = new java.sql.Timestamp(utilDate.getTime());

        when(mockJdbcTemplate.query(anyString(), any(RowMapper.class))).thenAnswer(invocation -> {
            ResultSet rs = mock(ResultSet.class);

            when(rs.getString(DB_FILE_NAME))
                    .thenReturn("AdditionalInfoRoles",
                            "Appointments",
                            "Authorisations",
                            "BaseLocations",
                            "Locations",
                            "Personal");

            when(rs.getTimestamp(DB_SCHEDULER_START_TIME))
                    .thenReturn(prevDaySchedulerST,
                            prevDaySchedulerST,
                            prevDaySchedulerST,
                            prevDaySchedulerST,
                            prevDaySchedulerST,
                            prevDaySchedulerST);

            when(rs.getString(DB_STATUS))
                    .thenReturn(SUCCESS,
                            FAILURE,
                            SUCCESS,
                            SUCCESS,
                            SUCCESS,
                            SUCCESS);

            RowMapper<Audit> rowMapper = (RowMapper<Audit>) invocation.getArgument(1);

            return List.of(Objects.requireNonNull(rowMapper.mapRow(rs, 0)),
                    Objects.requireNonNull(rowMapper.mapRow(rs, 1)),
                    Objects.requireNonNull(rowMapper.mapRow(rs, 2)),
                    Objects.requireNonNull(rowMapper.mapRow(rs, 3)),
                    Objects.requireNonNull(rowMapper.mapRow(rs, 4)),
                    Objects.requireNonNull(rowMapper.mapRow(rs, 5)));
        });

        assertFalse(dataLoadAuditUnderTest.isAuditingCompletedPrevDay(fileTS));
    }

    private static Date getDate(int year, int month, int day, int hour, int minute, int second) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month);
        cal.set(Calendar.DAY_OF_MONTH, day);
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, minute);
        cal.set(Calendar.SECOND, second);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }
}