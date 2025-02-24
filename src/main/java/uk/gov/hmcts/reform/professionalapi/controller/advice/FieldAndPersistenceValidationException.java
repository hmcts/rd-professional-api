package uk.gov.hmcts.reform.professionalapi.controller.advice;

import org.springframework.http.HttpStatus;

public class FieldAndPersistenceValidationException extends RuntimeException {

    private final HttpStatus httpStatus;

    private final String errorMessage;

    public FieldAndPersistenceValidationException(HttpStatus httpStatus, String errorMessage) {
        super(errorMessage);
        this.httpStatus = httpStatus;
        this.errorMessage = errorMessage;
    }

    public FieldAndPersistenceValidationException(HttpStatus httpStatus, Exception exception, String errorMessage) {
        super(exception.getMessage(), exception);
        this.httpStatus = httpStatus;
        this.errorMessage = errorMessage;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

}