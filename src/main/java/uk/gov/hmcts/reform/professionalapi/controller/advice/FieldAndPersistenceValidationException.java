package uk.gov.hmcts.reform.professionalapi.controller.advice;

import org.springframework.http.HttpStatus;

public class FieldAndPersistenceValidationException extends RuntimeException {

    private final HttpStatus httpStatus;

    public FieldAndPersistenceValidationException(HttpStatus httpStatus, String errorMessage) {
        super(errorMessage);
        this.httpStatus = httpStatus;
    }

    public FieldAndPersistenceValidationException(HttpStatus httpStatus, Exception exception, String errorMessage) {
        super(errorMessage, exception);
        this.httpStatus = httpStatus;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

}