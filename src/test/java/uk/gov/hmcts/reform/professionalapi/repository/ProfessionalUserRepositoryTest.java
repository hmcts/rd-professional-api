package uk.gov.hmcts.reform.professionalapi.repository;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;
import uk.gov.hmcts.reform.professionalapi.helper.BaseRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.professionalapi.util.RefDataUtil.createPageableObject;

@RunWith(SpringRunner.class)
@DataJpaTest
class ProfessionalUserRepositoryTest extends BaseRepository {

    @Test
    void test_findAll() {
        List<ProfessionalUser> professionalUsers = professionalUserRepository.findAll();

        assertThat(professionalUsers).hasSize(1);
        assertThat(professionalUsers.get(0)).isEqualTo(professionalUser);
        assertThat(professionalUsers.get(0).getId()).isEqualTo(professionalUser.getId());
    }

    @Test
    void test_findById() {
        Optional<ProfessionalUser> profUser = professionalUserRepository.findById(professionalUser.getId());

        assertThat(profUser).contains(professionalUser);
        assertThat(profUser.get().getId()).isEqualTo(professionalUser.getId());
    }

    @Test
    void test_findByEmailAddress() {
        ProfessionalUser profUser = professionalUserRepository.findByEmailAddress(professionalUser.getEmailAddress());

        assertThat(profUser).isEqualTo(professionalUser);
        assertThat(profUser.getId()).isEqualTo(professionalUser.getId());
    }

    @Test
    void test_findByOrganisation() {
        List<ProfessionalUser> profUser = professionalUserRepository.findByOrganisation(professionalUser
                .getOrganisation());

        assertThat(profUser.get(0)).isEqualTo(professionalUser);
        assertThat(profUser.get(0).getId()).isEqualTo(professionalUser.getId());
    }

    @Test
    void test_findByUserIdentifier() {
        ProfessionalUser profUser = professionalUserRepository.findByUserIdentifier(professionalUser
                .getUserIdentifier());

        assertThat(profUser).isEqualTo(professionalUser);
        assertThat(profUser.getId()).isEqualTo(professionalUser.getId());
    }

    @Test
    void test_findByOrganisationAndUserIdentifier() {
        List<ProfessionalUser> profUser = professionalUserRepository.findByOrganisationAndUserIdentifier(
                professionalUser.getOrganisation(), professionalUser.getUserIdentifier());

        assertThat(profUser.get(0)).isEqualTo(professionalUser);
        assertThat(profUser.get(0).getId()).isEqualTo(professionalUser.getId());
    }

    @Test
    void test_findByLastUpdatedGreaterThanEqual() {
        LocalDateTime dateTime = LocalDateTime.now();
        dateTime = dateTime.minusHours(1);

        Pageable pageable = createPageableObject(0, 10, Sort.by(Sort.DEFAULT_DIRECTION, "id"));

        Page<ProfessionalUser> profUserPage = professionalUserRepository
            .findByLastUpdatedGreaterThanEqualAndUserIdIsNotNull(dateTime, pageable);

        List<ProfessionalUser> profUser = profUserPage.getContent();

        assertThat(profUser.get(0)).isEqualTo(professionalUser);
        assertThat(profUser.get(0).getId()).isEqualTo(professionalUser.getId());
    }
}