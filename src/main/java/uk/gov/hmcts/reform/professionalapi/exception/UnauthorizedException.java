package uk.gov.hmcts.reform.professionalapi.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.UNAUTHORIZED)
public class UnauthorizedException extends RuntimeException {

    private static final long serialVersionUID = 6L;

    public UnauthorizedException(String  message, Throwable t) {
        super(message, t);

    }
}
