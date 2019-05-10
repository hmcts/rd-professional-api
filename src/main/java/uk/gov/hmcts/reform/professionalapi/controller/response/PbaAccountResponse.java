package uk.gov.hmcts.reform.professionalapi.controller.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.professionalapi.domain.PaymentAccount;

@NoArgsConstructor
public class PbaAccountResponse {

    @JsonProperty
    private String pbaNumber;


    public PbaAccountResponse(PaymentAccount paymentAccount) {

        this.pbaNumber = paymentAccount.getPbaNumber();
    }
}
