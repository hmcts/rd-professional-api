package uk.gov.hmcts.reform.professionalapi.controller.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.professionalapi.domain.PaymentAccount;

@NoArgsConstructor
public class PbaAccountResponse {

    @JsonProperty
    protected String pbaNumber;

    public PbaAccountResponse(PaymentAccount paymentAccount) {
        getPbaAccountResponse(paymentAccount);
    }

    public void getPbaAccountResponse(PaymentAccount paymentAccount) {
        this.pbaNumber = paymentAccount.getPbaNumber();
    }

    public String getPbaNumber() {
        return pbaNumber;
    }
}
