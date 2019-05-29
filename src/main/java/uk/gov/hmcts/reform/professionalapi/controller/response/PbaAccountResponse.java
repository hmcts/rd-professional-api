package uk.gov.hmcts.reform.professionalapi.controller.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.hmcts.reform.professionalapi.domain.PaymentAccount;

public class PbaAccountResponse {

    @JsonProperty
    private String pbaNumber;


    public PbaAccountResponse(PaymentAccount paymentAccount) {
        getPbaAccountResponse(paymentAccount);
    }

    private void getPbaAccountResponse(PaymentAccount paymentAccount) {
        this.pbaNumber = paymentAccount.getPbaNumber();
    }
}
