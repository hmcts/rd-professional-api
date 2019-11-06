package uk.gov.hmcts.reform.professionalapi.controller.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder(builderMethodName = "anPbaEditRequest")
public class PbaEditRequest {

    private List<String> paymentAccounts;

    @JsonCreator
    public PbaEditRequest(@JsonProperty("paymentAccounts") List<String> paymentAccount) {

        this.paymentAccounts = paymentAccounts;

    }
}
