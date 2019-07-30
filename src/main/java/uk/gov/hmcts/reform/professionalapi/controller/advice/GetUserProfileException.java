package uk.gov.hmcts.reform.professionalapi.controller.advice;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class GetUserProfileException extends RuntimeException {

    private HttpStatus httpStatus;

    private String errorMessage;

    public GetUserProfileException(HttpStatus httpStatus, String errorMessage) {
        super(errorMessage);
        this.httpStatus = httpStatus;
        this.errorMessage = errorMessage;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

}
