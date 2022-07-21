package uk.gov.hmcts.reform.professionalapi.repository;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.professionalapi.domain.UserAttribute;
import uk.gov.hmcts.reform.professionalapi.helper.BaseRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@DataJpaTest
class UserAttributeRepositoryTest extends BaseRepository {

    @Test
    void test_findAll() {
        List<UserAttribute> professionalUsers = userAttributeRepository.findAll();

        assertThat(professionalUsers).hasSize(1);
        assertThat(professionalUsers.get(0)).isEqualTo(userAttribute);
        assertThat(professionalUsers.get(0).getPrdEnum()).isEqualTo(userAttribute.getPrdEnum());
        assertThat(professionalUsers.get(0).getPrdEnum().getPrdEnumId()).isEqualTo(userAttribute.getPrdEnum()
                .getPrdEnumId());
        assertThat(professionalUsers.get(0).getProfessionalUser()).isEqualTo(userAttribute.getProfessionalUser());
        assertThat(professionalUsers.get(0).getProfessionalUser().getId()).isEqualTo(userAttribute
                .getProfessionalUser().getId());
    }

    @Test
    void test_findById() {
        Optional<UserAttribute> profUser = userAttributeRepository.findById(userAttribute.getId());

        assertThat(profUser).contains(userAttribute);
        assertThat(profUser.get().getPrdEnum()).isEqualTo(userAttribute.getPrdEnum());
        assertThat(profUser.get().getPrdEnum().getPrdEnumId()).isEqualTo(userAttribute.getPrdEnum().getPrdEnumId());
        assertThat(profUser.get().getProfessionalUser().getId()).isEqualTo(userAttribute.getProfessionalUser().getId());
    }
}