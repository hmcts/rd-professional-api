package uk.gov.hmcts.reform.professionalapi.controller.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PbaUpdateRequest {

    String pbaNumber;
    String status;
    String statusMessage;

    @JsonCreator
    public PbaUpdateRequest(
            @JsonProperty("pbaNumber") String pbaNumber,
            @JsonProperty("status") String status,
            @JsonProperty("statusMessage") String statusMessage) {

        this.pbaNumber = pbaNumber;
        this.status = status;
        this.statusMessage = statusMessage;
    }

}
