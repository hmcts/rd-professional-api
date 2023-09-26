package uk.gov.hmcts.reform.professionalapi.dataload.processor;

import lombok.SneakyThrows;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.impl.DefaultCamelContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import uk.gov.hmcts.reform.professionalapi.dataload.route.beans.FileStatus;
import uk.gov.hmcts.reform.professionalapi.dataload.route.beans.RouteProperties;
import uk.gov.hmcts.reform.professionalapi.dataload.util.DataLoadUtil;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.professionalapi.dataload.helper.JrdTestSupport.createRoutePropertiesMock;
import static uk.gov.hmcts.reform.professionalapi.dataload.util.MappingConstants.FAILURE;
import static uk.gov.hmcts.reform.professionalapi.dataload.util.MappingConstants.ROUTE_DETAILS;

public class ParentStateCheckProcessorTest {

    Exchange exchangeMock = mock(Exchange.class);

    CamelContext camelContext = spy(new DefaultCamelContext());

    Message messageMock = mock(Message.class);

    ParentStateCheckProcessor parentStateCheckProcessor = spy(new ParentStateCheckProcessor());

    @BeforeEach
    @SneakyThrows
    public void setUp() {
        when(exchangeMock.getContext()).thenReturn(camelContext);
        when(exchangeMock.getIn()).thenReturn(messageMock);
        when(exchangeMock.getMessage()).thenReturn(messageMock);
        RouteProperties routeProperties = createRoutePropertiesMock();
        routeProperties.setParentFailureEnabled(true);
        routeProperties.setParentFileName("test");
        when(messageMock.getHeader(ROUTE_DETAILS)).thenReturn(routeProperties);

    }

    @SneakyThrows
    @Test
    public void testProcessSetParentHeaderFailed() {
        try (MockedStatic<DataLoadUtil> theMock = mockStatic(DataLoadUtil.class)) {
            theMock.when(() -> DataLoadUtil.getFileDetails(any(), anyString()))
                .thenReturn(FileStatus.builder()
                    .auditStatus(FAILURE).build());
            parentStateCheckProcessor.process(exchangeMock);
            verify(parentStateCheckProcessor).process(exchangeMock);
        }
    }

    @SneakyThrows
    @Test
    public void testProcessSetParentHeaderTrue() {
        parentStateCheckProcessor.process(exchangeMock);
        verify(parentStateCheckProcessor).process(exchangeMock);
    }
}
