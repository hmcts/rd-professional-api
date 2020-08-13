package uk.gov.hmcts.reform.professionalapi.controller.request.processor;


import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.microsoft.applicationinsights.telemetry.RequestTelemetry;
import com.microsoft.applicationinsights.telemetry.Telemetry;
import java.net.URL;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import uk.gov.hmcts.reform.professionalapi.controller.request.InvalidRequest;

public class TelemetryRequestProcessorTest {

    TelemetryRequestProcessor telemetryRequestProcessor = new TelemetryRequestProcessor();

    RequestTelemetry requestTelemetry = new RequestTelemetry();

    @Test
    public void testProcess() throws Exception {
        requestTelemetry.setUrl(new URL("http://test.com?email=abcdef.xyz@test.com"));
        telemetryRequestProcessor.process(requestTelemetry);
        Assertions.assertThat(requestTelemetry.getUrlString()).isEqualTo("http://test.com?email=ab******yz@test.com");
    }

    @Test
    public void testProcessPathParams() throws Exception {
        requestTelemetry.setUrl(new URL("http://test.com/abc@test.com"));
        telemetryRequestProcessor.process(requestTelemetry);
        Assertions.assertThat(requestTelemetry.getUrlString()).isEqualTo("http://test.com/a**@test.com");
    }

    @Test
    public void testProcessPathParamsAndQueryParams() throws Exception {
        requestTelemetry.setUrl(new URL("http://test.com/abc@test.com?email=abc%40test.com"));
        telemetryRequestProcessor.process(requestTelemetry);
        Assertions.assertThat(requestTelemetry.getUrlString()).isEqualTo("http://test.com/a**@test.com?email=a**@test.com");
    }

    @Test
    public void testProcessNullRequest() {
        Telemetry telemetry = mock(Telemetry.class);
        TelemetryRequestProcessor telemetryRequestProcessor = mock(TelemetryRequestProcessor.class);
        telemetryRequestProcessor.process(telemetry);
        verify(telemetryRequestProcessor, times(1)).process(telemetry);
    }

    @Test(expected = InvalidRequest.class)
    public void testProcessInvalidUrl() {
        RequestTelemetry telemetry = mock(RequestTelemetry.class);
        telemetryRequestProcessor.process(telemetry);
        verify(telemetryRequestProcessor, times(1)).process(telemetry);
    }
}