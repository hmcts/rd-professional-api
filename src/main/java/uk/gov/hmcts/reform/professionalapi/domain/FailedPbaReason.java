package uk.gov.hmcts.reform.professionalapi.domain;

import java.util.Set;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class FailedPbaReason {

    private Set<String> duplicatePaymentAccounts;
    private Set<String> invalidPaymentAccounts;

    public FailedPbaReason(Set<String> duplicatePaymentAccounts, Set<String> invalidPaymentAccounts) {
        this.duplicatePaymentAccounts = duplicatePaymentAccounts;
        this.invalidPaymentAccounts = invalidPaymentAccounts;
    }
}
