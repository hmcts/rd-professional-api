package uk.gov.hmcts.reform.professionalapi.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit4.SpringRunner;

import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;

@RunWith(SpringRunner.class)
@DataJpaTest
@Ignore
public class ProfessionalUserRepositoryTest {

    @Autowired
    ProfessionalUserRepository professionalUserRepository;

    Organisation organisationMock = mock(Organisation.class);

    ProfessionalUser professionalUser = new ProfessionalUser("fName", "lName", "user@test.com", organisationMock);

    @Before
    public void setUp() {
        professionalUserRepository.save(professionalUser);
    }

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