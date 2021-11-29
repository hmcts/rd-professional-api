package uk.gov.hmcts.reform.professionalapi.domain;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class AddPbaResponseTest {

    @Test
    void test_addPbaResponseNoArgsConstructor() {

        FailedPbaReason failedPbaReason = new FailedPbaReason();
        Set<String> duplicatePaymentAccounts = new HashSet<>();
        Set<String> invalidPaymentAccounts = new HashSet<>();

        String duplicatePaymentAccount = "PBA1234567";
        String invalidPaymentAccount = "PBA1234569";

        duplicatePaymentAccounts.add(duplicatePaymentAccount);
        invalidPaymentAccounts.add(invalidPaymentAccount);

        failedPbaReason.setDuplicatePaymentAccounts(duplicatePaymentAccounts);
        failedPbaReason.setInvalidPaymentAccounts(invalidPaymentAccounts);

        AddPbaResponse addPbaResponse = new AddPbaResponse();
        String success = "Success";
        addPbaResponse.setMessage(success);
        addPbaResponse.setReason(failedPbaReason);

        assertThat(addPbaResponse.getReason().getInvalidPaymentAccounts().size()).isEqualTo(1);
        assertThat(addPbaResponse.getReason().getDuplicatePaymentAccounts().size()).isEqualTo(1);
        assertThat(addPbaResponse.getMessage()).isEqualTo(success);
    }

    @Test
    void test_addPbaResponseAllArgsConstructor() {
        FailedPbaReason failedPbaReason = new FailedPbaReason();

        Set<String> duplicatePaymentAccounts = new HashSet<>();
        Set<String> invalidPaymentAccounts = new HashSet<>();

        String duplicatePaymentAccount = "PBA1234567";
        String invalidPaymentAccount = "PBA1234569";

        duplicatePaymentAccounts.add(duplicatePaymentAccount);
        invalidPaymentAccounts.add(invalidPaymentAccount);

        failedPbaReason.setDuplicatePaymentAccounts(duplicatePaymentAccounts);
        failedPbaReason.setInvalidPaymentAccounts(invalidPaymentAccounts);

        String success = "Success";
        AddPbaResponse addPbaResponse = new AddPbaResponse(success, failedPbaReason);

        assertThat(addPbaResponse.getReason().getInvalidPaymentAccounts().size()).isEqualTo(1);
        assertThat(addPbaResponse.getReason().getDuplicatePaymentAccounts().size()).isEqualTo(1);
        assertThat(addPbaResponse.getMessage()).isEqualTo(success);
    }
    
}
