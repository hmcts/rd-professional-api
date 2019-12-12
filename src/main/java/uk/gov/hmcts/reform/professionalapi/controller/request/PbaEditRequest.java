package uk.gov.hmcts.reform.professionalapi.controller.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Set;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder(builderMethodName = "anPbaEditRequest")
public class PbaEditRequest {

    private Set<String> paymentAccounts;

    @JsonCreator
    public PbaEditRequest(@JsonProperty("paymentAccounts") Set<String> paymentAccounts) {
        this.paymentAccounts = paymentAccounts;
    }
}