package uk.gov.hmcts.reform.professionalapi.controller.response;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import uk.gov.hmcts.reform.professionalapi.domain.PaymentAccount;

public class PbaAccountResponseTest {

    @Test
    public void test_PbaAccountsResponse() {
        PbaAccountResponse pbaAccountResponse = new PbaAccountResponse(new PaymentAccount("pba1234567"));
        assertThat(pbaAccountResponse.getPbaNumber()).isEqualTo("pba1234567");
    }
}
