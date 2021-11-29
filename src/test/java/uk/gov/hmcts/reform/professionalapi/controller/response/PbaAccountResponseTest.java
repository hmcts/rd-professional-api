package uk.gov.hmcts.reform.professionalapi.controller.response;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.professionalapi.domain.PaymentAccount;

@ExtendWith(MockitoExtension.class)
class PbaAccountResponseTest {

    @Test
    void test_PbaAccountsResponse() {
        PbaAccountResponse pbaAccountResponse = new PbaAccountResponse(new PaymentAccount("pba1234567"));
        assertThat(pbaAccountResponse.getPbaNumber()).isEqualTo("pba1234567");
    }
}
