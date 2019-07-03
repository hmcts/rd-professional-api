package uk.gov.hmcts.reform.professionalapi.controller.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(builderMethodName = "aPbaPaymentAccount")
public class PbaAccountCreationRequest {

    @NotNull
    @Pattern(regexp = "^(?:PBA|pba) [a-zA-Z0-9]{10}+$")
    private final String pbaNumber;

    @JsonCreator
    public PbaAccountCreationRequest(
            @JsonProperty("pbaAccounts") String pbaNumber) {
        this.pbaNumber = pbaNumber;
    }

    public String getPbaNumber() {
        return pbaNumber;
    }
}
