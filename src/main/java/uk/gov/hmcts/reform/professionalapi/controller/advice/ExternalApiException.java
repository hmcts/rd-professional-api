package uk.gov.hmcts.reform.professionalapi.controller.advice;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ExternalApiException extends RuntimeException {

    private HttpStatus httpStatus;

    private String errorMessage;

    public ExternalApiException(HttpStatus httpStatus, String errorMessage) {
        super(errorMessage);
        this.httpStatus = httpStatus;
        this.errorMessage = errorMessage;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

}
