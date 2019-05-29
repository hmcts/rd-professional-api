package uk.gov.hmcts.reform.professionalapi.entities;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.Test;

import uk.gov.hmcts.reform.professionalapi.domain.UserAccountMap;
import uk.gov.hmcts.reform.professionalapi.domain.UserAccountMapId;

public class UserAccountMapTest {

    @Test
    public void creates_user_account_map_correctly() {

        UserAccountMapId userAccountMapId = mock(UserAccountMapId.class);

        UserAccountMap noOrgUserAccountMap = new UserAccountMap();

        assertThat(noOrgUserAccountMap).isNotNull();

        assertThat(noOrgUserAccountMap.getDefaulted()).isEqualTo(false);

        UserAccountMap userAccountMap = new UserAccountMap(userAccountMapId);

        assertThat(userAccountMap.getUserAccountMapId()).isEqualTo(userAccountMapId);

    }
}
