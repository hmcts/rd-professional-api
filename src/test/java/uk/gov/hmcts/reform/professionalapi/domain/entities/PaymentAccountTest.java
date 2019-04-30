package uk.gov.hmcts.reform.professionalapi.domain.entities;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class PaymentAccountTest {

    @Test
    public void can_set_organisation() {
        PaymentAccount paymentAccount = new PaymentAccount();

        Organisation organisation = new Organisation();

        paymentAccount.setOrganisation(organisation);

        assertThat(paymentAccount.getOrganisation()).isSameAs(organisation);
    }
}