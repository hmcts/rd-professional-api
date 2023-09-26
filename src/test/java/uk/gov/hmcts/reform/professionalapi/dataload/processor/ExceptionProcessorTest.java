package uk.gov.hmcts.reform.professionalapi.dataload.processor;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.spi.Registry;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import uk.gov.hmcts.reform.professionalapi.dataload.route.beans.FileStatus;
import uk.gov.hmcts.reform.professionalapi.dataload.route.beans.RouteProperties;
import uk.gov.hmcts.reform.professionalapi.dataload.service.IAuditService;

import java.util.HashMap;

import static org.apache.camel.Exchange.EXCEPTION_CAUGHT;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.professionalapi.dataload.util.MappingConstants.ROUTE_DETAILS;

public class ExceptionProcessorTest extends CamelTestSupport {


    @InjectMocks
    ExceptionProcessor exceptionProcessor;

    Exchange exchangeMock = mock(Exchange.class);

    Message messageMock = mock(Message.class);

    CamelContext context = mock(CamelContext.class);

    Registry registryMock = mock(Registry.class);

    ApplicationContext applicationContext = mock(ConfigurableApplicationContext.class);

    ConfigurableListableBeanFactory configurableListableBeanFactory = mock(ConfigurableListableBeanFactory.class);

    IAuditService auditService = mock(IAuditService.class);

    FileResponseProcessor fileResponseProcessor = mock(FileResponseProcessor.class);

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testProcess() throws Exception {

        RouteProperties routePropertiesMock = mock(RouteProperties.class);
        when(exchangeMock.getContext()).thenReturn(context);
        when(context.getGlobalOptions()).thenReturn(new HashMap<>());
        when(context.getRegistry()).thenReturn(registryMock);
        when(registryMock.lookupByName(anyString())).thenReturn(FileStatus.builder().fileName("test").build());
        when(exchangeMock.getProperty(EXCEPTION_CAUGHT)).thenReturn(new RuntimeException("test Exception"));
        when(exchangeMock.getIn()).thenReturn(messageMock);
        when(messageMock.getHeader(ROUTE_DETAILS)).thenReturn(routePropertiesMock);
        doNothing().when(auditService).auditException(any(), anyString());
        doNothing().when(fileResponseProcessor).process(any());
        when(((ConfigurableApplicationContext)
            applicationContext).getBeanFactory()).thenReturn(configurableListableBeanFactory);
        exceptionProcessor.process(exchangeMock);
    }
}