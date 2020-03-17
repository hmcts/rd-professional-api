package uk.gov.hmcts.reform.professionalapi.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.professionalapi.domain.UserAccountMap;
import uk.gov.hmcts.reform.professionalapi.helper.RepositorySetUp;

@RunWith(SpringRunner.class)
@DataJpaTest
public class UserAccountMapRepositoryTest extends RepositorySetUp {

    @Test
    public void test_findAll() {
        List<UserAccountMap> professionalUsers = userAccountMapRepository.findAll();

        assertThat(professionalUsers).hasSize(1);
        assertThat(professionalUsers.get(0).getUserAccountMapId().getProfessionalUser()).isEqualTo(professionalUser);
        assertThat(professionalUsers.get(0).getUserAccountMapId().getPaymentAccount()).isEqualTo(paymentAccount);
    }
}
