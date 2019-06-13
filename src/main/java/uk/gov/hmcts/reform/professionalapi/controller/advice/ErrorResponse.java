package uk.gov.hmcts.reform.professionalapi.controller.advice;

import lombok.Builder;
import org.springframework.http.HttpStatus;


@Builder
public class ErrorResponse {

    private final int errorCode;

    private final HttpStatus status;

    private final String errorMessage;

    private final String errorDescription;

    private final String timeStamp;
}