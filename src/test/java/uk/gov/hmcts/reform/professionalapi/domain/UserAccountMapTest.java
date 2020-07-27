package uk.gov.hmcts.reform.professionalapi.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;

public class UserAccountMapTest {

    @Test
    public void test_creates_user_account_map_correctly() {
        UserAccountMap noOrgUserAccountMap = new UserAccountMap();
        assertThat(noOrgUserAccountMap).isNotNull();
        assertThat(noOrgUserAccountMap.getDefaulted()).isFalse();

        UserAccountMapId userAccountMapId = new UserAccountMapId();
        UserAccountMap userAccountMap = new UserAccountMap(userAccountMapId);
        assertThat(userAccountMap.getUserAccountMapId()).isEqualTo(userAccountMapId);

        UserAccountMap userAccountMapMock = mock(UserAccountMap.class);
        when(userAccountMapMock.getDefaulted()).thenReturn(true);
        assertThat(userAccountMapMock.getDefaulted()).isTrue();
    }
}
