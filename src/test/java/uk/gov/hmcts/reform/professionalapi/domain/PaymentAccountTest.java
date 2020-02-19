package uk.gov.hmcts.reform.professionalapi.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;

import org.junit.Test;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.PaymentAccount;
import uk.gov.hmcts.reform.professionalapi.domain.UserAccountMap;

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

        paymentAccount.setId(UUID.randomUUID());

        paymentAccount.setPbaNumber("some-pba-number");

        assertThat(paymentAccount.getId()).isNotNull();

        assertThat(paymentAccount.getPbaNumber()).isNotNull();

        paymentAccount.setUserAccountMap(new ArrayList<UserAccountMap>());

        assertThat(paymentAccount.getUserAccountMap()).isNotNull();


    }

}