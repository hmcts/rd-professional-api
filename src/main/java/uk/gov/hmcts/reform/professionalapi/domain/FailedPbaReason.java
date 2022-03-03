package uk.gov.hmcts.reform.professionalapi.domain;

import java.util.Set;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FailedPbaReason {

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Set<String> duplicatePaymentAccounts;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Set<String> invalidPaymentAccounts;

}
