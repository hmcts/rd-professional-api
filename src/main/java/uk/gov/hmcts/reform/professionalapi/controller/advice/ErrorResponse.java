package uk.gov.hmcts.reform.professionalapi.controller.advice;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ErrorResponse {

    private final int errorCode;

    private final HttpStatus status;

    private final String errorMessage;

    private final String errorDescription;

    private final String timeStamp;

    public ErrorResponse(int errorCode, HttpStatus status, String errorMessage, String errorDescription, String timeStamp) {
        this.errorCode = errorCode;
        this.status = status;
        this.errorMessage = errorMessage;
        this.errorDescription = errorDescription;
        this.timeStamp = timeStamp;
    }

}