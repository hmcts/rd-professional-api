package uk.gov.hmcts.reform.professionalapi.controller.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Getter;

@Getter
public class LegacyPbaResponse {

    @JsonProperty
    @SuppressWarnings({"checkstyle:MemberName"})
    private final List<String> paymentAccounts;

    @SuppressWarnings({"checkstyle:ParameterName"})
    public LegacyPbaResponse(List<String> paymentAccounts) {
        this.paymentAccounts = paymentAccounts;
    }
}
