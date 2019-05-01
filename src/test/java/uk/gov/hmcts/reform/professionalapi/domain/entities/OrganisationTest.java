package uk.gov.hmcts.reform.professionalapi.domain.entities;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.Test;

public class OrganisationTest {

    @Test
    public void creates_organisation_correctly() {

        Organisation organisation = new Organisation("some-name", "some-status");

        assertThat(organisation.getName()).isEqualTo("some-name");
        assertThat(organisation.getStatus()).isEqualTo("some-status");

        assertThat(organisation.getId()).isNull(); // hibernate generated
        assertThat(organisation.getLastUpdated()).isNull();
        assertThat(organisation.getCreated()).isNull();
    }

    @Test
    public void adds_users_correctly() {

        ProfessionalUser professionalUser = mock(ProfessionalUser.class);

        Organisation organisation = new Organisation();
        organisation.addProfessionalUser(professionalUser);

        assertThat(organisation.getUsers())
                .containsExactly(professionalUser);
        assertThat(organisation.getLastUpdated()).isNull();
        assertThat(organisation.getCreated()).isNull();
    }

    @Test
    public void adds_payment_account_correctly() {

        PaymentAccount paymentAccount = mock(PaymentAccount.class);

        Organisation organisation = new Organisation();
        organisation.addPaymentAccount(paymentAccount);

        assertThat(organisation.getPaymentAccounts())
                .containsExactly(paymentAccount);
        assertThat(organisation.getLastUpdated()).isNull();
        assertThat(organisation.getCreated()).isNull();
    }
}