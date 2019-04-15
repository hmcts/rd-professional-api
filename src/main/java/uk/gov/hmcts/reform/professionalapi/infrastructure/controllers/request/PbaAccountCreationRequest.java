package uk.gov.hmcts.reform.professionalapi.infrastructure.controllers.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

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
