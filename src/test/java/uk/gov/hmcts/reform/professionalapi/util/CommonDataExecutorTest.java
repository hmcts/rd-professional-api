package uk.gov.hmcts.reform.professionalapi.util;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.util.ReflectionTestUtils.setField;
import static uk.gov.hmcts.reform.professionalapi.dataload.util.MappingConstants.IS_READY_TO_AUDIT;

import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.impl.DefaultCamelContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.professionalapi.dataload.service.AuditServiceImpl;
import uk.gov.hmcts.reform.professionalapi.dataload.util.PrdDataExecutor;

@ExtendWith(MockitoExtension.class)
class CommonDataExecutorTest {
    PrdDataExecutor prdDataExecutor = new PrdDataExecutor();

    PrdDataExecutor prdDataExecutorSpy = spy(prdDataExecutor);

    CamelContext camelContext = new DefaultCamelContext();

    AuditServiceImpl auditService = mock(AuditServiceImpl.class);

    ProducerTemplate producerTemplate = mock(ProducerTemplate.class);

    @BeforeEach
    void init() {
        setField(prdDataExecutorSpy, "auditService", auditService);
    }

    @Test
    void testExecute() {
        doNothing().when(producerTemplate).sendBody(Mockito.any());
        doNothing().when(auditService).auditSchedulerStatus(camelContext);
        prdDataExecutorSpy.execute(camelContext, "test", "test");
        verify(prdDataExecutorSpy, times(1)).execute(camelContext, "test", "test");
    }

    @Test
    void testExecute_AuditDisabled() {
        doNothing().when(producerTemplate).sendBody(Mockito.any());
        doNothing().when(auditService).auditSchedulerStatus(camelContext);
        camelContext.getGlobalOptions().put(IS_READY_TO_AUDIT, Boolean.FALSE.toString());
        prdDataExecutorSpy.execute(camelContext, "test", "test");
        verify(prdDataExecutorSpy, times(1)).execute(camelContext, "test", "test");
        verify(auditService, times(0)).auditSchedulerStatus(camelContext);
    }

    @Test
    void testExecute_AuditEnabled() {
        doNothing().when(producerTemplate).sendBody(Mockito.any());
        doNothing().when(auditService).auditSchedulerStatus(camelContext);
        camelContext.getGlobalOptions().put(IS_READY_TO_AUDIT, Boolean.TRUE.toString());
        prdDataExecutorSpy.execute(camelContext, "test", "test");
        verify(prdDataExecutorSpy, times(1)).execute(camelContext, "test", "test");
        verify(auditService, times(1)).auditSchedulerStatus(camelContext);
    }

    @Test
    void testExecute_NoAuditPreference() {
        doNothing().when(producerTemplate).sendBody(Mockito.any());
        doNothing().when(auditService).auditSchedulerStatus(camelContext);
        camelContext.getGlobalOptions().put(IS_READY_TO_AUDIT, null);
        prdDataExecutorSpy.execute(camelContext, "test", "test");
        verify(prdDataExecutorSpy, times(1)).execute(camelContext, "test", "test");
        verify(auditService, times(0)).auditSchedulerStatus(camelContext);
    }

    @Test
    void testExecuteException() {
        doNothing().when(auditService).auditSchedulerStatus(camelContext);
        prdDataExecutorSpy.execute(camelContext, "test", "test");
        verify(prdDataExecutorSpy, times(1)).execute(camelContext, "test", "test");
    }
}
