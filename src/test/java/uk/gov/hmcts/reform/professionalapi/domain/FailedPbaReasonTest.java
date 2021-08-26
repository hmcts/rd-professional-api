package uk.gov.hmcts.reform.professionalapi.domain;

import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class FailedPbaReasonTest {

    @Test
    public void test_failedPbaReasonNoArgsConstructor() {
        FailedPbaReason failedPbaReason = new FailedPbaReason();

        Set<String> duplicatePaymentAccounts = new HashSet<>();
        Set<String> invalidPaymentAccounts = new HashSet<>();

        String duplicatePaymentAccount = "PBA1234567";
        String invalidPaymentAccount = "PBA1234569";

        duplicatePaymentAccounts.add(duplicatePaymentAccount);
        invalidPaymentAccounts.add(invalidPaymentAccount);

        failedPbaReason.setDuplicatePaymentAccounts(duplicatePaymentAccounts);
        failedPbaReason.setInvalidPaymentAccounts(invalidPaymentAccounts);

        assertThat(failedPbaReason.getDuplicatePaymentAccounts().size()).isEqualTo(1);
        assertThat(failedPbaReason.getInvalidPaymentAccounts().size()).isEqualTo(1);
    }

    @Test
    public void test_failedPbaReasonAllArgsConstructor() {


        Set<String> duplicatePaymentAccounts = new HashSet<>();
        Set<String> invalidPaymentAccounts = new HashSet<>();

        String duplicatePaymentAccount = "PBA1234567";
        String invalidPaymentAccount = "PBA1234569";

        duplicatePaymentAccounts.add(duplicatePaymentAccount);
        invalidPaymentAccounts.add(invalidPaymentAccount);

        FailedPbaReason failedPbaReason = new FailedPbaReason(duplicatePaymentAccounts, invalidPaymentAccounts);

        assertThat(failedPbaReason.getDuplicatePaymentAccounts().size()).isEqualTo(1);
        assertThat(failedPbaReason.getInvalidPaymentAccounts().size()).isEqualTo(1);
    }


}
