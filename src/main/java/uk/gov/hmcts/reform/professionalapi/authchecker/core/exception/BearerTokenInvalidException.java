package uk.gov.hmcts.reform.professionalapi.authchecker.core.exception;

public class BearerTokenInvalidException extends AuthCheckerException {
    public BearerTokenInvalidException(Throwable cause) {
        super(cause);
    }
}
