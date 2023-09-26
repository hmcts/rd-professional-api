package uk.gov.hmcts.reform.professionalapi.dataload.processor;

import lombok.SneakyThrows;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.context.ApplicationContext;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.professionalapi.dataload.exception.RouteFailedException;
import uk.gov.hmcts.reform.professionalapi.dataload.route.beans.RouteProperties;
import uk.gov.hmcts.reform.professionalapi.dataload.util.MappingConstants;

import java.io.InputStream;
import java.util.HashMap;

import static org.apache.camel.spring.util.ReflectionUtils.setField;
import static org.apache.commons.io.IOUtils.toInputStream;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class HeaderValidationProcessorTest {
    Exchange exchangeMock = mock(Exchange.class);
    Message messageMock = mock(Message.class);
    RouteProperties routePropertiesMock = mock(RouteProperties.class);
    ApplicationContext applicationContextMock = mock(ApplicationContext.class);
    CamelContext camelContext = mock(CamelContext.class);
    HeaderValidationProcessor headerValidationProcessor = spy(new HeaderValidationProcessor());

    static class BinderObject {
    }

    @BeforeEach
    public void setUp() throws Exception {
        when(exchangeMock.getIn()).thenReturn(messageMock);
        when(exchangeMock.getIn().getHeader(MappingConstants.ROUTE_DETAILS)).thenReturn(routePropertiesMock);
        setField(headerValidationProcessor.getClass()
            .getDeclaredField("applicationContext"), headerValidationProcessor, applicationContextMock);
        setField(headerValidationProcessor.getClass()
            .getDeclaredField("camelContext"), headerValidationProcessor, camelContext);
    }

    @SneakyThrows
    @Test
    public void testProcess() {

        String msgBody = "filed1";
        InputStream inputStream = toInputStream(msgBody, "UTF-8");
        when(exchangeMock.getIn().getBody(InputStream.class)).thenReturn(inputStream);

        when(exchangeMock.getMessage()).thenReturn(messageMock);
        when(applicationContextMock.getBean(routePropertiesMock.getBinder())).thenReturn(BinderObject.class);
        headerValidationProcessor.process(exchangeMock);
        verify(headerValidationProcessor).process(exchangeMock);
    }

    @SneakyThrows
    @Test
    public void testProcessException() {

        String msgBody = "filed1,field2";
        InputStream inputStream = toInputStream(msgBody, "UTF-8");
        when(exchangeMock.getIn().getBody(InputStream.class)).thenReturn(inputStream);

        when(exchangeMock.getMessage()).thenReturn(messageMock);
        when(camelContext.getGlobalOptions()).thenReturn(new HashMap<>());
        BinderObject binderObject = new BinderObject();
        when(applicationContextMock.getBean(routePropertiesMock.getBinder())).thenReturn(binderObject);
        assertThrows(RouteFailedException.class, () -> headerValidationProcessor.process(exchangeMock));
        verify(headerValidationProcessor).process(exchangeMock);
    }

    @SneakyThrows
    @Test
    public void testProcessHeaders() {

        String cvsHeaderExpected = "ePIMS_ID,Site_Name,Court_Name,Court_Status,Court_Open_Date";

        RouteProperties routePropertiesMock = mock(RouteProperties.class);
        ReflectionTestUtils.setField(routePropertiesMock,"isHeaderValidationEnabled","true");
        ReflectionTestUtils.setField(routePropertiesMock,"csvHeadersExpected",cvsHeaderExpected);

        RouteProperties routePropertiesSpy =  Mockito.spy(routePropertiesMock);


        when(exchangeMock.getIn().getHeader(MappingConstants.ROUTE_DETAILS)).thenReturn(routePropertiesSpy);

        String msgBody = "\ufeffePIMS_ID,Site_Name,Court_Name,Court_Status,Court_Open_Date";


        InputStream inputStream = toInputStream(msgBody, "UTF-8");

        when(exchangeMock.getIn().getBody(InputStream.class)).thenReturn(inputStream);
        when(exchangeMock.getMessage()).thenReturn(messageMock);
        when(camelContext.getGlobalOptions()).thenReturn(new HashMap<>());
        BinderObject binderObject = new BinderObject();
        when(applicationContextMock.getBean(routePropertiesSpy.getBinder())).thenReturn(binderObject);
        headerValidationProcessor.process(exchangeMock);

        verify(headerValidationProcessor).process(exchangeMock);
    }
}