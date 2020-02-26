package uk.gov.hmcts.reform.professionalapi.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.professionalapi.domain.UserAccountMap;
import uk.gov.hmcts.reform.professionalapi.domain.UserAccountMapId;

@RunWith(SpringRunner.class)
@DataJpaTest
@Ignore
public class UserAccountMapRepositoryTest {

    @Autowired
    UserAccountMapRepository userAccountMapRepository;

    UserAccountMapId userAccountMapId = new UserAccountMapId();

    UserAccountMap userAccountMap = new UserAccountMap(userAccountMapId);

    @Before
    public void setUp() {
        userAccountMapRepository.save(userAccountMap);
    }

    @Test
    public void test_findAll() {
        List<UserAccountMap> professionalUsers = userAccountMapRepository.findAll();

        assertThat(professionalUsers).hasSize(1);
        assertThat(professionalUsers.get(0)).isEqualTo(userAccountMap);
    }
}
