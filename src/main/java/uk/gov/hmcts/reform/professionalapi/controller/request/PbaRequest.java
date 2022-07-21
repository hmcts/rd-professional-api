package uk.gov.hmcts.reform.professionalapi.controller.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class PbaRequest {

    @JsonProperty(value = "paymentAccounts")
    private Set<String> paymentAccounts;
}