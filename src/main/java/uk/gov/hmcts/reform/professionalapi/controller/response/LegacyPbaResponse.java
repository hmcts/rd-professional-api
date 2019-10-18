package uk.gov.hmcts.reform.professionalapi.controller.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Getter;

@Getter
public class LegacyPbaResponse {

    @JsonProperty
    @SuppressWarnings({"checkstyle:MemberName"})
    private final List<String> payment_accounts;

    @SuppressWarnings({"checkstyle:ParameterName"})
    public LegacyPbaResponse(List<String> payment_accounts) {
        this.payment_accounts = payment_accounts;
    }
}
