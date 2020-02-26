package uk.gov.hmcts.reform.professionalapi.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.professionalapi.domain.PrdEnum;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;
import uk.gov.hmcts.reform.professionalapi.domain.UserAttribute;

@RunWith(SpringRunner.class)
@DataJpaTest
@Ignore
public class UserAttributeRepositoryTest {

    @Autowired
    UserAttributeRepository userAttributeRepository;

    ProfessionalUser professionalUser = new ProfessionalUser();
    PrdEnum prdEnum = new PrdEnum();


    UserAttribute userAttribute = new UserAttribute(professionalUser, prdEnum);

    @Before
    public void setUp() {
        userAttributeRepository.save(userAttribute);
    }

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