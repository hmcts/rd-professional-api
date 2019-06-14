package uk.gov.hmcts.reform.professionalapi.controller.response;

import org.junit.Test;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.professionalapi.controller.advice.ErrorResponse;

public class ErrorResponseTest {

    @Test
    public void testErrorResponse() throws Exception {
        ErrorResponse errorDetails = new ErrorResponse(42, HttpStatus.OK, "msg", "desc","time");
    }

}
