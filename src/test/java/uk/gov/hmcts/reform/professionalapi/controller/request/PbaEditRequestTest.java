package uk.gov.hmcts.reform.professionalapi.controller.request;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static java.util.Collections.singleton;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class PbaEditRequestTest {

    private final String pbaNumber = "PBA0000001";

    @Test
    void test_pbaEditRequestBuilder() {
        PbaRequest pbaEditRequest = new PbaRequest();
        pbaEditRequest.setPaymentAccounts(singleton(pbaNumber));
        assertThat(pbaEditRequest.getPaymentAccounts()).isEqualTo(singleton(pbaNumber));
    }
}
