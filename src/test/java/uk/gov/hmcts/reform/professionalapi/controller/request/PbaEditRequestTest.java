package uk.gov.hmcts.reform.professionalapi.controller.request;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class PbaEditRequestTest {

    @Test
    public void pbaEditRequestBuilderTest() {
        List<String> paymentAccounts = new ArrayList<>();
        paymentAccounts.add("PBA0000001");

        PbaEditRequest pbaEditRequest = PbaEditRequest.anPbaEditRequest()
                .paymentAccounts(paymentAccounts)
                .build();

        pbaEditRequest.setPaymentAccounts(paymentAccounts);

        assertThat(pbaEditRequest.getPaymentAccounts()).isEqualTo(paymentAccounts);

    }

}
