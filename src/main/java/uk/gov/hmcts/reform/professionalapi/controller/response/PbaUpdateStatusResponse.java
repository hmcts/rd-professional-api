package uk.gov.hmcts.reform.professionalapi.controller.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class PbaUpdateStatusResponse {

    @JsonProperty
    String pbaNumber;
    @JsonProperty
    String errorMessage;

    public PbaUpdateStatusResponse(String pbaNumber, String errorMessage) {
        this.pbaNumber = pbaNumber;
        this.errorMessage = errorMessage;
    }

}
