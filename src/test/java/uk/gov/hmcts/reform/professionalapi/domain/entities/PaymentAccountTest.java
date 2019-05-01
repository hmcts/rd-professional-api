package uk.gov.hmcts.reform.professionalapi.domain.entities;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class PaymentAccountTest {

    @Test
    public void can_set_user() {
        PaymentAccount paymentAccount = new PaymentAccount();

        ProfessionalUser user = new ProfessionalUser();

        paymentAccount.addUser(user);

        assertThat(paymentAccount.getUsers().get(0)).isSameAs(user);
        assertThat(paymentAccount.getOrganisation()).isNull();
        assertThat(paymentAccount.getId()).isNull();
        assertThat(paymentAccount.getPbaNumber()).isNull();
        assertThat(paymentAccount.getLastUpdated()).isNull();
        assertThat(paymentAccount.getCreated()).isNull();
    }

    @Test
    public void can_set_organisation() {
        PaymentAccount paymentAccount = new PaymentAccount();

        Organisation organisation = new Organisation();

        paymentAccount.setOrganisation(organisation);

        assertThat(paymentAccount.getOrganisation()).isSameAs(organisation);
        assertThat(paymentAccount.getUsers()).isNull();
        assertThat(paymentAccount.getId()).isNull();
        assertThat(paymentAccount.getPbaNumber()).isNull();
        assertThat(paymentAccount.getLastUpdated()).isNull();
        assertThat(paymentAccount.getCreated()).isNull();
    }

    @Test
    public void can_get_pba_number() {
        String pbaNumber = "123456";
        PaymentAccount paymentAccount = new PaymentAccount(pbaNumber);

        assertThat(paymentAccount.getPbaNumber()).isEqualTo(pbaNumber);

        assertThat(paymentAccount.getOrganisation()).isNull();
        assertThat(paymentAccount.getUsers()).isNull();
        assertThat(paymentAccount.getId()).isNull();
        assertThat(paymentAccount.getLastUpdated()).isNull();
        assertThat(paymentAccount.getCreated()).isNull();
    }
}