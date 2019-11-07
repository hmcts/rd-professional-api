package uk.gov.hmcts.reform.professionalapi.controller.request;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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
