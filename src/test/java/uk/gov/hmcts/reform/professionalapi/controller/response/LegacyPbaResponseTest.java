package uk.gov.hmcts.reform.professionalapi.controller.response;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;

import org.junit.Test;
import uk.gov.hmcts.reform.professionalapi.domain.PaymentAccount;

public class LegacyPbaResponseTest {

    @Test
    public void test_LegacyPbaResponse() {
        PaymentAccount paymentAccount = new PaymentAccount("pba1234567");

        LegacyPbaResponse legacyPbaResponse = new LegacyPbaResponse(Collections.singletonList(paymentAccount
                .getPbaNumber()));

        assertThat(legacyPbaResponse.getPayment_accounts().size()).isEqualTo(1);
        assertThat(legacyPbaResponse.getPayment_accounts().get(0)).isEqualTo(paymentAccount.getPbaNumber());
    }
}
