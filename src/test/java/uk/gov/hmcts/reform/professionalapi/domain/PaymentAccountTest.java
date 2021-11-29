package uk.gov.hmcts.reform.professionalapi.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.PBA_STATUS_MESSAGE_ACCEPTED;
import static uk.gov.hmcts.reform.professionalapi.domain.PbaStatus.ACCEPTED;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PaymentAccountTest {

    @Test
    void test_can_set_organisation() {
        Organisation organisation = new Organisation();
        PaymentAccount paymentAccount = new PaymentAccount();
        List<UserAccountMap> userAccountMap = new ArrayList<>();
        userAccountMap.add(new UserAccountMap());

        paymentAccount.setOrganisation(organisation);
        paymentAccount.setLastUpdated(LocalDateTime.now());
        paymentAccount.setCreated(LocalDateTime.now());
        paymentAccount.setId(UUID.randomUUID());
        paymentAccount.setPbaNumber("some-pba-number");
        paymentAccount.setPbaStatus(ACCEPTED);
        paymentAccount.setStatusMessage(PBA_STATUS_MESSAGE_ACCEPTED);
        paymentAccount.setOrganisationId(UUID.randomUUID());

        assertThat(paymentAccount.getOrganisation()).isSameAs(organisation);
        assertThat(paymentAccount.getLastUpdated()).isNotNull();
        assertThat(paymentAccount.getCreated()).isNotNull();
        assertThat(paymentAccount.getId()).isNotNull();
        assertThat(paymentAccount.getPbaNumber()).isNotNull();
        assertThat(paymentAccount.getPbaStatus()).isNotNull();
        assertThat(paymentAccount.getStatusMessage()).isNotNull();
        assertThat(paymentAccount.getOrganisationId()).isNotNull();
        assertThat(paymentAccount.getStatusMessage()).isNotEmpty();
        assertThat(paymentAccount.getStatusMessage()).isEqualTo(PBA_STATUS_MESSAGE_ACCEPTED);
    }
}