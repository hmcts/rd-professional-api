package uk.gov.hmcts.reform.professionalapi.controller.request;

import static java.util.Collections.singleton;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PbaEditRequestTest {

    @Test
    void test_pbaEditRequestBuilder() {
        PbaRequest pbaEditRequest = new PbaRequest();
        String pbaNumber = "PBA0000001";
        pbaEditRequest.setPaymentAccounts(singleton(pbaNumber));
        assertThat(pbaEditRequest.getPaymentAccounts()).isEqualTo(singleton(pbaNumber));
    }
}
