package uk.gov.hmcts.reform.professionalapi.authchecker.parser.idam.service.token;

public class ServiceTokenParsingException extends RuntimeException {
    public ServiceTokenParsingException() {
    }

    public ServiceTokenParsingException(Throwable cause) {
        super(cause);
    }
}

