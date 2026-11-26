package uk.gov.hmcts.reform.professionalapi.controller.advice;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonPropertyOrder({"errorMessage", "errorDescription", "timeStamp"})
public class ErrorResponse {

    private String errorMessage;

    private String errorDescription;

    private String timeStamp;


}