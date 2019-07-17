package uk.gov.hmcts.reform.professionalapi.controller.advice;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
public class ErrorResponse {

    private String errorMessage;

    private String errorDescription;

    private String timeStamp;

    public ErrorResponse(String errorMessage, String errorDescription, String timeStamp) {
        this.errorMessage = errorMessage;
        this.errorDescription = errorDescription;
        this.timeStamp = timeStamp;
    }
}