package uk.gov.hmcts.reform.professionalapi.domain;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class FailedPbaReasonTest {

    @Test
    void test_failedPbaReasonNoArgsConstructor() {
        FailedPbaReason failedPbaReason = new FailedPbaReason();

        Set<String> duplicatePaymentAccounts = new HashSet<>();
        Set<String> invalidPaymentAccounts = new HashSet<>();

        String duplicatePaymentAccount = "PBA1234567";
        String invalidPaymentAccount = "PBA1234569";

        duplicatePaymentAccounts.add(duplicatePaymentAccount);
        invalidPaymentAccounts.add(invalidPaymentAccount);

        failedPbaReason.setDuplicatePaymentAccounts(duplicatePaymentAccounts);
        failedPbaReason.setInvalidPaymentAccounts(invalidPaymentAccounts);

        assertThat(failedPbaReason.getDuplicatePaymentAccounts()).hasSize(1);
        assertThat(failedPbaReason.getInvalidPaymentAccounts()).hasSize(1);
    }

    @Test
    void test_failedPbaReasonAllArgsConstructor() {


        Set<String> duplicatePaymentAccounts = new HashSet<>();
        Set<String> invalidPaymentAccounts = new HashSet<>();

        String duplicatePaymentAccount = "PBA1234567";
        String invalidPaymentAccount = "PBA1234569";

        duplicatePaymentAccounts.add(duplicatePaymentAccount);
        invalidPaymentAccounts.add(invalidPaymentAccount);

        FailedPbaReason failedPbaReason = new FailedPbaReason(duplicatePaymentAccounts, invalidPaymentAccounts);

        assertThat(failedPbaReason.getDuplicatePaymentAccounts()).hasSize(1);
        assertThat(failedPbaReason.getInvalidPaymentAccounts()).hasSize(1);
    }


}
