package uk.gov.hmcts.reform.professionalapi.authchecker.core.exception;

public class AuthCheckerException extends RuntimeException {

    public AuthCheckerException() {
    }

    public AuthCheckerException(String message) {
        super(message);
    }

    public AuthCheckerException(String message, Throwable cause) {
        super(message, cause);
    }

    public AuthCheckerException(Throwable cause) {
        super(cause);
    }

    public AuthCheckerException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}

