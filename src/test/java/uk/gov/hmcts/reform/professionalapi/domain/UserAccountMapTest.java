package uk.gov.hmcts.reform.professionalapi.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserAccountMapTest {

    @Test
    void test_creates_user_account_map_correctly() {
        UserAccountMap noOrgUserAccountMap = new UserAccountMap();
        assertThat(noOrgUserAccountMap).isNotNull();

        UserAccountMapId userAccountMapId = new UserAccountMapId();
        UserAccountMap userAccountMap = new UserAccountMap(userAccountMapId);
        assertThat(userAccountMap.getUserAccountMapId()).isEqualTo(userAccountMapId);
    }
}
