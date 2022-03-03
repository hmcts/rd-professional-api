package uk.gov.hmcts.reform.professionalapi.controller.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PbaUpdateStatusResponse {

    @JsonProperty
    private String pbaNumber;
    @JsonProperty
    private String errorMessage;

}
