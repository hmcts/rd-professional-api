package uk.gov.hmcts.reform.professionalapi.controller.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.stream.Collectors;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class LegacyPbaResponse {

    @JsonProperty

    //CHECKSTYLE:OFF: checkstyle:suppresswarningsholder
    private List<String> payment_accounts;
    //CHECKSTYLE:ON: checkstyle:suppresswarningsholder

    public LegacyPbaResponse(List<String> payment_accounts) {
        getPaymentAccounts(payment_accounts);
    }

    private List<String> getPaymentAccounts(List<String> payment_accounts) {

        return this.payment_accounts = payment_accounts.stream().map(payment_account ->
                payment_account).collect(Collectors.toList());
    }
}
