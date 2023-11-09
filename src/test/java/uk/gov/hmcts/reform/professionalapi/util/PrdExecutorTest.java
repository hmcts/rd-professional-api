package uk.gov.hmcts.reform.professionalapi.util;

import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.impl.DefaultCamelContext;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.professionalapi.dataload.service.AuditServiceImpl;
import uk.gov.hmcts.reform.professionalapi.dataload.util.DataLoadUtil;
import uk.gov.hmcts.reform.professionalapi.dataload.util.PrdDataExecutor;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.util.ReflectionTestUtils.setField;
import static uk.gov.hmcts.reform.professionalapi.dataload.util.MappingConstants.IS_READY_TO_AUDIT;

@ExtendWith(MockitoExtension.class)
class PrdExecutorTest {
    PrdDataExecutor prdDataExecutor = new PrdDataExecutor();

    PrdDataExecutor prdDataExecutorSpy = spy(prdDataExecutor);

    CamelContext camelContext = new DefaultCamelContext();

    AuditServiceImpl auditService = mock(AuditServiceImpl.class);

    ProducerTemplate producerTemplate = spy(ProducerTemplate.class);

    DataLoadUtil dataLoadUtil = mock(DataLoadUtil.class);

    @BeforeEach
    void init() {
        setField(prdDataExecutorSpy, "auditService", auditService);
    }

    @Test
    void testExecute() {
        setField(prdDataExecutorSpy, "dataLoadUtil", dataLoadUtil);
        setField(prdDataExecutorSpy, "producerTemplate", producerTemplate);
        doNothing().when(auditService).auditSchedulerStatus(camelContext);
        prdDataExecutorSpy.execute(camelContext, "test", "test");
        verify(prdDataExecutorSpy, times(1))
            .execute(camelContext, "test", "test");
        verify(producerTemplate,times(1)).sendBody(anyString(),any());
        verify(dataLoadUtil,times(1)).setGlobalConstant(any(),anyString());
        verify(producerTemplate,times(1)).start();
        Assert.assertEquals("Success",prdDataExecutorSpy.execute(camelContext, "test", "test"));
    }

    @Test
    void testExecute_AuditDisabled() {
        setField(prdDataExecutorSpy, "dataLoadUtil", dataLoadUtil);
        setField(prdDataExecutorSpy, "producerTemplate", producerTemplate);
        doNothing().when(auditService).auditSchedulerStatus(camelContext);
        camelContext.getGlobalOptions().put(IS_READY_TO_AUDIT, Boolean.FALSE.toString());
        prdDataExecutorSpy.execute(camelContext, "test", "test");
        verify(prdDataExecutorSpy, times(1)).execute(camelContext, "test", "test");
        verify(auditService, times(0)).auditSchedulerStatus(camelContext);
    }

    @Test
    void testExecute_AuditEnabled() {
        setField(prdDataExecutorSpy, "dataLoadUtil", dataLoadUtil);
        setField(prdDataExecutorSpy, "producerTemplate", producerTemplate);
        doNothing().when(auditService).auditSchedulerStatus(camelContext);
        camelContext.getGlobalOptions().put(IS_READY_TO_AUDIT, Boolean.TRUE.toString());
        prdDataExecutorSpy.execute(camelContext, "test", "test");
        verify(prdDataExecutorSpy, times(1)).execute(camelContext, "test", "test");
        verify(auditService, times(1)).auditSchedulerStatus(camelContext);
    }

    @Test
    void testExecute_NoAuditPreference() {
        setField(prdDataExecutorSpy, "dataLoadUtil", dataLoadUtil);
        setField(prdDataExecutorSpy, "producerTemplate", producerTemplate);
        doNothing().when(auditService).auditSchedulerStatus(camelContext);
        camelContext.getGlobalOptions().put(IS_READY_TO_AUDIT, null);
        prdDataExecutorSpy.execute(camelContext, "test", "test");
        verify(prdDataExecutorSpy, times(1)).execute(camelContext, "test", "test");
        verify(auditService, times(0)).auditSchedulerStatus(camelContext);
    }

    @Test
    void testExecuteException() {
        setField(prdDataExecutorSpy, "dataLoadUtil", dataLoadUtil);
        setField(prdDataExecutorSpy, "producerTemplate", producerTemplate);
        doNothing().when(auditService).auditSchedulerStatus(camelContext);
        prdDataExecutorSpy.execute(camelContext, "test", "test");
        verify(prdDataExecutorSpy, times(1)).execute(camelContext, "test", "test");
    }

    @Test
    void testStop() throws Exception {
        setField(prdDataExecutorSpy, "dataLoadUtil", dataLoadUtil);
        setField(prdDataExecutorSpy, "producerTemplate", producerTemplate);
        doNothing().when(auditService).auditSchedulerStatus(camelContext);
        prdDataExecutorSpy.stop(camelContext, "test", "test");
        verify(prdDataExecutorSpy, times(1))
            .stop(camelContext, "test", "test");
        verify(dataLoadUtil,times(1)).removeGlobalConstant(any());
        verify(producerTemplate,times(1)).stop();
        Assert.assertEquals("Success",prdDataExecutorSpy.stop(camelContext, "test", "test"));
    }
}
