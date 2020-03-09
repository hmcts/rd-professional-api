package uk.gov.hmcts.reform.professionalapi.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class UserAccountMapIdTest {

    @Test
    public void creates_user_account_map_id_correctly() {
        ProfessionalUser professionalUser = new ProfessionalUser();
        PaymentAccount paymentAccount = new PaymentAccount();

        UserAccountMapId noOrgUserAccountMapId = new UserAccountMapId();
        assertThat(noOrgUserAccountMapId).isNotNull();

        UserAccountMapId userAccountMapId = new UserAccountMapId(professionalUser, paymentAccount);
        assertThat(userAccountMapId.getProfessionalUser()).isEqualTo(professionalUser);
        assertThat(userAccountMapId.getPaymentAccount()).isEqualTo(paymentAccount);
    }
}
