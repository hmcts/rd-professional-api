package uk.gov.hmcts.reform.professionalapi.controller.response;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import uk.gov.hmcts.reform.professionalapi.controller.advice.ErrorResponse;

public class ErrorResponseTest {

    @Test
    public void errorResponseTest() {
        ErrorResponse errorDetails = ErrorResponse.builder()
                .errorDescription("desc")
                .errorMessage("msg")
                .timeStamp("time")
                .build();

        assertThat(errorDetails.getErrorDescription().equals("desc"));
        assertThat(errorDetails.getErrorMessage().equals("msg"));
        assertThat(errorDetails.getTimeStamp().equals("time"));
    }

}
