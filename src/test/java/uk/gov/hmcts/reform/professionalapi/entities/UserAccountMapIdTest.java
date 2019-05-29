package uk.gov.hmcts.reform.professionalapi.entities;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.Test;

import uk.gov.hmcts.reform.professionalapi.domain.PaymentAccount;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;
import uk.gov.hmcts.reform.professionalapi.domain.UserAccountMapId;

public class UserAccountMapIdTest {

    @Test
    public void creates_user_account_map_id_correctly() {

        ProfessionalUser professionalUser = mock(ProfessionalUser.class);

        PaymentAccount paymentAccount = mock(PaymentAccount.class);

        UserAccountMapId noOrgUserAccountMapId = new UserAccountMapId();

        assertThat(noOrgUserAccountMapId).isNotNull();

        UserAccountMapId userAccountMapId = new UserAccountMapId(professionalUser, paymentAccount);

        assertThat(userAccountMapId.getProfessionalUser()).isEqualTo(professionalUser);

        assertThat(userAccountMapId.getPaymentAccount()).isEqualTo(paymentAccount);

    }
}
