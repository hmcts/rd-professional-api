package uk.gov.hmcts.reform.professionalapi.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserAccountMapTest {

    @Test
    void test_creates_user_account_map_correctly() {
        UserAccountMap noOrgUserAccountMap = new UserAccountMap();
        assertThat(noOrgUserAccountMap).isNotNull();
        assertFalse(noOrgUserAccountMap.getDefaulted());

        UserAccountMapId userAccountMapId = new UserAccountMapId();
        UserAccountMap userAccountMap = new UserAccountMap(userAccountMapId);
        assertThat(userAccountMap.getUserAccountMapId()).isEqualTo(userAccountMapId);
        assertThat(userAccountMap.getDefaulted()).isFalse();

        UserAccountMap userAccountMapMock = mock(UserAccountMap.class);
        when(userAccountMapMock.getDefaulted()).thenReturn(true);
        assertTrue(userAccountMapMock.getDefaulted());
    }
}
