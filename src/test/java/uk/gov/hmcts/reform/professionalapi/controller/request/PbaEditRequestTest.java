package uk.gov.hmcts.reform.professionalapi.controller.request;

import static java.util.Collections.singleton;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class PbaEditRequestTest {

    private String pbaNumber = "PBA0000001";

    @Test
    public void pbaEditRequestBuilderTest() {
        PbaEditRequest pbaEditRequest = new PbaEditRequest();
        pbaEditRequest.setPaymentAccounts(singleton(pbaNumber));
        assertThat(pbaEditRequest.getPaymentAccounts()).isEqualTo(singleton(pbaNumber));
    }
}
