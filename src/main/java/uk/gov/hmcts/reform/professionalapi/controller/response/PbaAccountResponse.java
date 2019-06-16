package uk.gov.hmcts.reform.professionalapi.controller.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.hmcts.reform.professionalapi.domain.PaymentAccount;
import uk.gov.hmcts.reform.professionalapi.util.PbaAccountUtil;

public class PbaAccountResponse {



    @JsonProperty
    private String pbaNumber;


    public PbaAccountResponse(PaymentAccount paymentAccount) {
        getPbaAccountResponse(paymentAccount);
    }

    public void getPbaAccountResponse(PaymentAccount paymentAccount) {

        this.pbaNumber = PbaAccountUtil.removeEmptySpaces(paymentAccount.getPbaNumber());
    }

    public String getPbaNumber() {
        return pbaNumber;
    }
}
