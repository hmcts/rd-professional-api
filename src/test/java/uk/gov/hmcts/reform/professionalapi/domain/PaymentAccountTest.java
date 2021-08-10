package uk.gov.hmcts.reform.professionalapi.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.PBA_STATUS_MESSAGE;
import static uk.gov.hmcts.reform.professionalapi.domain.PbaStatus.ACCEPTED;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.Test;

public class PaymentAccountTest {

    @Test
    public void test_can_set_organisation() {
        Organisation organisation = new Organisation();
        PaymentAccount paymentAccount = new PaymentAccount();
        List<UserAccountMap> userAccountMap = new ArrayList<>();
        userAccountMap.add(new UserAccountMap());

        paymentAccount.setOrganisation(organisation);
        paymentAccount.setLastUpdated(LocalDateTime.now());
        paymentAccount.setCreated(LocalDateTime.now());
        paymentAccount.setId(UUID.randomUUID());
        paymentAccount.setPbaNumber("some-pba-number");
        paymentAccount.setPbaStatus(ACCEPTED.name());
        paymentAccount.setStatusMessage(PBA_STATUS_MESSAGE);

        assertThat(paymentAccount.getOrganisation()).isSameAs(organisation);
        assertThat(paymentAccount.getLastUpdated()).isNotNull();
        assertThat(paymentAccount.getCreated()).isNotNull();
        assertThat(paymentAccount.getId()).isNotNull();
        assertThat(paymentAccount.getPbaNumber()).isNotNull();
    }
}