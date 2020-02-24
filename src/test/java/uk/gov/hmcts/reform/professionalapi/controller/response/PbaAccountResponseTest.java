package uk.gov.hmcts.reform.professionalapi.controller.response;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import uk.gov.hmcts.reform.professionalapi.domain.PaymentAccount;

public class PbaAccountResponseTest {

    @Test
    public void test_PbaAccountsResponse() {
        PaymentAccount paymentAccount = new PaymentAccount("pba1234567");

        PbaAccountResponse pbaAccountResponse = new PbaAccountResponse(paymentAccount);

        assertThat(pbaAccountResponse.getPbaNumber()).isEqualTo(paymentAccount.getPbaNumber());
    }
}
