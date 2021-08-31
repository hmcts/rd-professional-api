package uk.gov.hmcts.reform.professionalapi.controller.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PbaRequest {

    @JsonProperty(value = "paymentAccounts")
    private Set<String> paymentAccounts;
}