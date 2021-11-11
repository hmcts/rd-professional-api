package uk.gov.hmcts.reform.professionalapi.controller.request;

import static java.util.Collections.singleton;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PbaEditRequestTest {

    private String pbaNumber = "PBA0000001";

    @Test
    void test_pbaEditRequestBuilder() {
        PbaEditRequest pbaEditRequest = new PbaEditRequest();
        pbaEditRequest.setPaymentAccounts(singleton(pbaNumber));
        assertThat(pbaEditRequest.getPaymentAccounts()).isEqualTo(singleton(pbaNumber));
    }
}
