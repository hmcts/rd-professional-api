package uk.gov.hmcts.reform.professionalapi.controller.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import javax.validation.constraints.NotNull;

@Getter
@Builder(builderMethodName = "aPbaPaymentAccount")
public class PbaAccountCreationRequest {

    @NotNull
    private final String pbaNumber;

    @JsonCreator
    public PbaAccountCreationRequest(
            @JsonProperty("pbaAccounts") String pbaNumber) {
        this.pbaNumber = pbaNumber;
    }

}