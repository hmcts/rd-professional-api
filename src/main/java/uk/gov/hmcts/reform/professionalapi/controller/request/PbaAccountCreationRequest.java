package uk.gov.hmcts.reform.professionalapi.controller.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.validation.constraints.NotNull;

import lombok.Builder;
import lombok.Getter;
import uk.gov.hmcts.reform.professionalapi.util.PbaAccountUtil;

@Getter
@Builder(builderMethodName = "aPbaPaymentAccount")
public class PbaAccountCreationRequest {

    @NotNull
    private final String pbaNumber;

    @JsonCreator
    public PbaAccountCreationRequest(
            @JsonProperty("pbaAccounts") String pbaNumber) {
        this.pbaNumber = PbaAccountUtil.removeEmptySpaces(pbaNumber);
    }

    public String getPbaNumber() {
        return pbaNumber;
    }
}