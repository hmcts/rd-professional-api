package uk.gov.hmcts.reform.professionalapi.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.professionalapi.domain.UserAccountMap;
import uk.gov.hmcts.reform.professionalapi.helper.BaseRepository;

@RunWith(SpringRunner.class)
@DataJpaTest
public class UserAccountMapRepositoryTest extends BaseRepository {

    @Test
    public void test_findAll() {
        List<UserAccountMap> professionalUsers = userAccountMapRepository.findAll();

        assertThat(professionalUsers).hasSize(1);
        assertThat(professionalUsers.get(0).getUserAccountMapId().getProfessionalUser()).isEqualTo(professionalUser);
        assertThat(professionalUsers.get(0).getUserAccountMapId().getProfessionalUser().getId()).isEqualTo(professionalUser.getId());
        assertThat(professionalUsers.get(0).getUserAccountMapId().getPaymentAccount()).isEqualTo(paymentAccount);
        assertThat(professionalUsers.get(0).getUserAccountMapId().getPaymentAccount().getPbaNumber()).isEqualTo(paymentAccount.getPbaNumber());
    }

    @Test
    public void test_findByUserAccountMapId() {
        Optional<UserAccountMap> user = userAccountMapRepository.findByUserAccountMapId(userAccountMap.getUserAccountMapId());

        assertThat(user).isPresent();
        assertThat(user.get().getUserAccountMapId().getProfessionalUser()).isEqualTo(professionalUser);
        assertThat(user.get().getUserAccountMapId().getProfessionalUser().getId()).isEqualTo(professionalUser.getId());
        assertThat(user.get().getUserAccountMapId().getPaymentAccount()).isEqualTo(paymentAccount);
        assertThat(user.get().getUserAccountMapId().getPaymentAccount().getPbaNumber()).isEqualTo(paymentAccount.getPbaNumber());
    }
}
