package uk.gov.hmcts.reform.professionalapi.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.professionalapi.domain.UserAttribute;
import uk.gov.hmcts.reform.professionalapi.helper.RepositorySetUp;

@RunWith(SpringRunner.class)
@DataJpaTest
public class UserAttributeRepositoryTest extends RepositorySetUp {

    @Test
    public void test_findAll() {
        List<UserAttribute> professionalUsers = userAttributeRepository.findAll();

        assertThat(professionalUsers).hasSize(1);
        assertThat(professionalUsers.get(0)).isEqualTo(userAttribute);
    }

    @Test
    public void test_findById() {
        Optional<UserAttribute> profUser = userAttributeRepository.findById(userAttribute.getId());

        assertThat(profUser.get()).isEqualTo(userAttribute);
    }
}