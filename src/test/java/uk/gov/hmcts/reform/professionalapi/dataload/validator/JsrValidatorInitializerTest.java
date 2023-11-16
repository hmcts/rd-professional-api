package uk.gov.hmcts.reform.professionalapi.dataload.validator;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.impl.DefaultCamelContext;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import uk.gov.hmcts.reform.professionalapi.dataload.binder.BulkCustomerDetails;
import uk.gov.hmcts.reform.professionalapi.dataload.route.beans.RouteProperties;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.ReflectionTestUtils.setField;
import static uk.gov.hmcts.reform.professionalapi.dataload.util.MappingConstants.ROUTE_DETAILS;
import static uk.gov.hmcts.reform.professionalapi.dataload.util.MappingConstants.SCHEDULER_START_TIME;

class JsrValidatorInitializerTest {

    static JsrValidatorInitializer<BulkCustomerDetails> bulkCustomerDetailsJsrValidatorInitializer
        = new JsrValidatorInitializer<>();

    @BeforeAll
    public static void beforeAll() throws Exception {
        bulkCustomerDetailsJsrValidatorInitializer.initializeFactory();
    }

    @Test
    void testValidate() {
        List<BulkCustomerDetails> bulkCustomerDetails = new ArrayList<>();
        Date currentDate = new Date();
        LocalDateTime dateTime = LocalDateTime.now();
        BulkCustomerDetails bulkCustomer = createBulCustomerMock();
        bulkCustomerDetails.add(bulkCustomer);
        JsrValidatorInitializer<BulkCustomerDetails> bulkCustomerDetailsJsrValidatorInitializerSpy
            = spy(bulkCustomerDetailsJsrValidatorInitializer);
        bulkCustomerDetailsJsrValidatorInitializerSpy.validate(bulkCustomerDetails);
        verify(bulkCustomerDetailsJsrValidatorInitializerSpy, times(1)).validate(any());
    }

    @Test
    void testAuditJsrExceptions() {
        Exchange exchange = mock(Exchange.class);
        Message message = mock(Message.class);
        RouteProperties routeProperties = new RouteProperties();
        routeProperties.setTableName("test");
        when(exchange.getIn()).thenReturn(message);
        CamelContext camelContext = new DefaultCamelContext();
        Map<String, String> map = new HashMap<>();
        map.put(SCHEDULER_START_TIME, String.valueOf(new Date().getTime()));
        camelContext.setGlobalOptions(map);

        when(message.getHeader(ROUTE_DETAILS)).thenReturn(routeProperties);
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        final PlatformTransactionManager platformTransactionManager = mock(PlatformTransactionManager.class);
        final TransactionStatus transactionStatus = mock(TransactionStatus.class);
        setField(bulkCustomerDetailsJsrValidatorInitializer, "platformTransactionManager",
            platformTransactionManager);
        setField(bulkCustomerDetailsJsrValidatorInitializer, "jdbcTemplate", jdbcTemplate);
        setField(bulkCustomerDetailsJsrValidatorInitializer, "camelContext", camelContext);
        setField(bulkCustomerDetailsJsrValidatorInitializer, "jsrThresholdLimit", 5);

        JsrValidatorInitializer<BulkCustomerDetails> bulkCustomerDetailsJsrValidatorInitializerSpy
            = spy(bulkCustomerDetailsJsrValidatorInitializer);

        List<BulkCustomerDetails> bulkCustomerDetails = new ArrayList<>();
        BulkCustomerDetails bulkCustomerDetails1 = createBulCustomerMock();
        bulkCustomerDetails1.setOrganisationId(null);
        bulkCustomerDetails.add(bulkCustomerDetails1);
        bulkCustomerDetailsJsrValidatorInitializerSpy.validate(bulkCustomerDetails);
        int[][] intArray = new int[1][];
        when(jdbcTemplate.batchUpdate(anyString(), anyList(), anyInt(), any())).thenReturn(intArray);
        when(platformTransactionManager.getTransaction(any())).thenReturn(transactionStatus);
        doNothing().when(platformTransactionManager).commit(transactionStatus);

        bulkCustomerDetailsJsrValidatorInitializerSpy.auditJsrExceptions(exchange);
        verify(bulkCustomerDetailsJsrValidatorInitializerSpy, times(1)).validate(any());
    }

    public static BulkCustomerDetails createBulCustomerMock() {

        BulkCustomerDetails bulkCustomerDetails = new BulkCustomerDetails();
        bulkCustomerDetails.setBulkCustomerId("bulkCustomerId");
        bulkCustomerDetails.setOrganisationId("orgId");
        bulkCustomerDetails.setPbaNumber("pbaNumber");
        bulkCustomerDetails.setSidamId("sidamId");
        return bulkCustomerDetails;
    }
}
