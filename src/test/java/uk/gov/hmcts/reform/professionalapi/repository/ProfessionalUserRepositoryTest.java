package uk.gov.hmcts.reform.professionalapi.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;
import uk.gov.hmcts.reform.professionalapi.helper.RepositorySetUp;

@RunWith(SpringRunner.class)
@DataJpaTest
public class ProfessionalUserRepositoryTest extends RepositorySetUp {

    @Test
    public void test_findAll() {
        List<ProfessionalUser> professionalUsers = professionalUserRepository.findAll();

        assertThat(professionalUsers).hasSize(1);
        assertThat(professionalUsers.get(0)).isEqualTo(professionalUser);
    }

    @Test
    public void test_findById() {
        Optional<ProfessionalUser> profUser = professionalUserRepository.findById(professionalUser.getId());

        assertThat(profUser.get()).isEqualTo(professionalUser);
    }
}