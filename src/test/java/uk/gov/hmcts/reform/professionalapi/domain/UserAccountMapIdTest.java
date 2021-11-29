package uk.gov.hmcts.reform.professionalapi.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserAccountMapIdTest {

    @Test
    void test_creates_user_account_map_id_correctly() {
        ProfessionalUser professionalUser = new ProfessionalUser();
        PaymentAccount paymentAccount = new PaymentAccount();

        UserAccountMapId noOrgUserAccountMapId = new UserAccountMapId();
        assertThat(noOrgUserAccountMapId).isNotNull();

        UserAccountMapId userAccountMapId = new UserAccountMapId(professionalUser, paymentAccount);
        assertThat(userAccountMapId.getProfessionalUser()).isEqualTo(professionalUser);
        assertThat(userAccountMapId.getPaymentAccount()).isEqualTo(paymentAccount);
    }
}
