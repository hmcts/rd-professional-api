package uk.gov.hmcts.reform.professionalapi.controller.advice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

class ExternalApiExceptionTest {

    @Test
    void test_getExternalApiException() {
        ExternalApiException externalApiException = new ExternalApiException(BAD_REQUEST, "BAD REQUEST");

        assertThat(externalApiException.getHttpStatus()).hasToString("400 BAD_REQUEST");
        assertThat(externalApiException.getErrorMessage()).isEqualTo("BAD REQUEST");
    }
}
