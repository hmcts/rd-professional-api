package uk.gov.hmcts.reform.professionalapi.controller.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.stream.Collectors;

public class LegacyPbaResponse {

    @JsonProperty
    @SuppressWarnings({"checkstyle:MemberName"})
    private List<String> payment_accounts;

    @SuppressWarnings({"checkstyle:ParameterName"})
    public LegacyPbaResponse(List<String> payment_accounts) {
        getPaymentAccounts(payment_accounts);
    }

    @SuppressWarnings({"checkstyle:ParameterName"})
    private List<String> getPaymentAccounts(List<String> payment_accounts) {

        return this.payment_accounts = payment_accounts.stream().map(payment_account ->
                        payment_account).collect(Collectors.toList());
    }
}
