package uk.gov.hmcts.reform.professionalapi.controller.advice;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ErrorResponseTest {

    @Test
    void test_ErrorResponse() {
        String expectMsg = "msg";
        String expectDesc = "desc";
        String expectTs = "time";

        ErrorResponse errorDetails = ErrorResponse.builder()
                .errorDescription("desc")
                .errorMessage(expectMsg)
                .timeStamp("time")
                .build();

        assertThat(errorDetails).isNotNull();
        assertThat(errorDetails.getErrorMessage()).isEqualTo(expectMsg);
        assertThat(errorDetails.getTimeStamp()).isEqualTo(expectTs);
        assertThat(errorDetails.getErrorDescription()).isEqualTo(expectDesc);
    }

    @Test
    void test_NoArgsConstructor() {
        ErrorResponse errorResponse = new ErrorResponse();
        assertThat(errorResponse).isNotNull();
    }

}
