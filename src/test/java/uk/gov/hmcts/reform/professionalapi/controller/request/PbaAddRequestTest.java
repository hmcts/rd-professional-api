package uk.gov.hmcts.reform.professionalapi.controller.request;

import org.junit.Test;

import static java.util.Collections.singleton;
import static org.assertj.core.api.Assertions.assertThat;

public class PbaAddRequestTest {

    private String pbaNumber = "PBA0000001";

    @Test
    public void test_pbaAddRequestBuilder() {
        PbaAddRequest pbaAddRequest = new PbaAddRequest();
        pbaAddRequest.setPaymentAccounts(singleton(pbaNumber));
        assertThat(pbaAddRequest.getPaymentAccounts()).isEqualTo(singleton(pbaNumber));
    }
}
