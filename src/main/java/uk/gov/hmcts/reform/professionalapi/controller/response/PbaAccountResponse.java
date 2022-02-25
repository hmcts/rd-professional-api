package uk.gov.hmcts.reform.professionalapi.controller.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.gov.hmcts.reform.professionalapi.domain.PaymentAccount;

@Getter
@Setter
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

}
