package uk.gov.hmcts.reform.professionalapi.entities;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import org.junit.Test;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.PaymentAccount;

public class PaymentAccountTest {

    @Test
    public void can_set_organisation() {
        PaymentAccount paymentAccount = new PaymentAccount();

        Organisation organisation = new Organisation();

        paymentAccount.setOrganisation(organisation);

        assertThat(paymentAccount.getOrganisation()).isSameAs(organisation);

        paymentAccount.setLastUpdated(LocalDateTime.now());

        paymentAccount.setCreated(LocalDateTime.now());

        assertThat(paymentAccount.getLastUpdated()).isNotNull();

        assertThat(paymentAccount.getCreated()).isNotNull();
    }

}