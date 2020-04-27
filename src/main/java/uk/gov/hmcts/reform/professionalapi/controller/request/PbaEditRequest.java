package uk.gov.hmcts.reform.professionalapi.controller.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Set;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder(builderMethodName = "anPbaEditRequest")
@NoArgsConstructor
public class PbaEditRequest {

    @JsonProperty
    private Set<String> paymentAccounts;

    public PbaEditRequest(Set<String> paymentAccounts) {
        this.paymentAccounts = paymentAccounts;
    }
}