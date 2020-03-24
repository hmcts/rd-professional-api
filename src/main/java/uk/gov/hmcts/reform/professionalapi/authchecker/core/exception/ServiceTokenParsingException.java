package uk.gov.hmcts.reform.professionalapi.authchecker.core.exception;

public class ServiceTokenParsingException extends RuntimeException {
    public ServiceTokenParsingException() {
    }

    public ServiceTokenParsingException(Throwable cause) {
        super(cause);
    }
}

