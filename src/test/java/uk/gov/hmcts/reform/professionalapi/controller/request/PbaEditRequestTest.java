package uk.gov.hmcts.reform.professionalapi.controller.request;

import static java.util.Collections.singleton;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class PbaEditRequestTest {

    @Test
    public void test_pbaEditRequestBuilder() {
        PbaRequest pbaEditRequest = new PbaRequest();
        String pbaNumber = "PBA0000001";
        pbaEditRequest.setPaymentAccounts(singleton(pbaNumber));
        assertThat(pbaEditRequest.getPaymentAccounts()).isEqualTo(singleton(pbaNumber));
    }
}
