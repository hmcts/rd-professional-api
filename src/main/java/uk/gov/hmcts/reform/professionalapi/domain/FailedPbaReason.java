package uk.gov.hmcts.reform.professionalapi.domain;

import java.util.Set;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class FailedPbaReason {

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Set<String> duplicatePaymentAccounts;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Set<String> invalidPaymentAccounts;

    public FailedPbaReason(Set<String> duplicatePaymentAccounts, Set<String> invalidPaymentAccounts) {
        this.duplicatePaymentAccounts = duplicatePaymentAccounts;
        this.invalidPaymentAccounts = invalidPaymentAccounts;
    }
}
