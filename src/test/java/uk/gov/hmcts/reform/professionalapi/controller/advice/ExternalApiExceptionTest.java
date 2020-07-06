package uk.gov.hmcts.reform.professionalapi.controller.advice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

import org.junit.Test;

public class ExternalApiExceptionTest {

    @Test
    public void test_getExternalApiException() {
        ExternalApiException externalApiException = new ExternalApiException(BAD_REQUEST, "BAD REQUEST");

        assertThat(externalApiException.getHttpStatus().toString()).isEqualTo("400 BAD_REQUEST");
        assertThat(externalApiException.getErrorMessage()).isEqualTo("BAD REQUEST");
    }
}
