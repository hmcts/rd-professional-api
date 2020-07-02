package uk.gov.hmcts.reform.professionalapi.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.Test;

public class PaymentAccountTest {

    @Test
    public void can_set_organisation() {
        Organisation organisation = new Organisation();
        PaymentAccount paymentAccount = new PaymentAccount();

        paymentAccount.setOrganisation(organisation);
        paymentAccount.setLastUpdated(LocalDateTime.now());
        paymentAccount.setCreated(LocalDateTime.now());
        paymentAccount.setId(UUID.randomUUID());
        paymentAccount.setPbaNumber("some-pba-number");

        assertThat(paymentAccount.getOrganisation()).isSameAs(organisation);
        assertThat(paymentAccount.getLastUpdated()).isNotNull();
        assertThat(paymentAccount.getCreated()).isNotNull();
        assertThat(paymentAccount.getId()).isNotNull();
        assertThat(paymentAccount.getPbaNumber()).isNotNull();

    }
}