package uk.gov.hmcts.reform.professionalapi.controller.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.gov.hmcts.reform.professionalapi.domain.PaymentAccount;
import uk.gov.hmcts.reform.professionalapi.domain.PbaStatus;

import static java.util.Objects.isNull;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FetchPbaByStatusResponse extends PbaAccountResponse {

    @JsonProperty
    private String status;
    @JsonProperty
    private String statusMessage;
    @JsonProperty
    private String dateCreated;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String dateAccepted;

    public FetchPbaByStatusResponse(PaymentAccount paymentAccount) {
        this.pbaNumber = paymentAccount.getPbaNumber();
        this.status = isNull(paymentAccount.getPbaStatus()) ? null : paymentAccount.getPbaStatus().toString();
        this.statusMessage = paymentAccount.getStatusMessage();
        this.dateCreated = paymentAccount.getCreated().toString();
        if (PbaStatus.ACCEPTED.equals(paymentAccount.getPbaStatus())) {
            this.dateAccepted = paymentAccount.getLastUpdated().toString();
        }
    }
}
