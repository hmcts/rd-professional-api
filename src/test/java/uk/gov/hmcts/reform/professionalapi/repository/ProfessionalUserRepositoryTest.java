package uk.gov.hmcts.reform.professionalapi.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;
import uk.gov.hmcts.reform.professionalapi.helper.BaseRepository;

@RunWith(SpringRunner.class)
@DataJpaTest
public class ProfessionalUserRepositoryTest extends BaseRepository {

    @Test
    public void test_findAll() {
        List<ProfessionalUser> professionalUsers = professionalUserRepository.findAll();

        assertThat(professionalUsers).hasSize(1);
        assertThat(professionalUsers.get(0)).isEqualTo(professionalUser);
        assertThat(professionalUsers.get(0).getId()).isEqualTo(professionalUser.getId());
    }

    @Test
    public void test_findById() {
        Optional<ProfessionalUser> profUser = professionalUserRepository.findById(professionalUser.getId());

        assertThat(profUser).contains(professionalUser);
        assertThat(profUser.get().getId()).isEqualTo(professionalUser.getId());
    }

    @Test
    public void test_findByEmailAddress() {
        ProfessionalUser profUser = professionalUserRepository.findByEmailAddress(professionalUser.getEmailAddress());

        assertThat(profUser).isEqualTo(professionalUser);
        assertThat(profUser.getId()).isEqualTo(professionalUser.getId());
    }

    @Test
    public void test_findByOrganisation() {
        List<ProfessionalUser> profUser = professionalUserRepository.findByOrganisation(professionalUser.getOrganisation());

        assertThat(profUser.get(0)).isEqualTo(professionalUser);
        assertThat(profUser.get(0).getId()).isEqualTo(professionalUser.getId());
    }

    @Test
    public void test_findByUserIdentifier() {
        ProfessionalUser profUser = professionalUserRepository.findByUserIdentifier(professionalUser.getUserIdentifier());

        assertThat(profUser).isEqualTo(professionalUser);
        assertThat(profUser.getId()).isEqualTo(professionalUser.getId());
    }
}