package uk.gov.hmcts.reform.professionalapi.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class UserConfiguredAccessTest {

    @Test
    void test_creates_user_account_map_correctly() {
        UserConfiguredAccessId userConfiguredAccessId = new UserConfiguredAccessId();
        boolean enabled = false;
        UserConfiguredAccess userConfiguredAccess = new UserConfiguredAccess(userConfiguredAccessId, enabled);
        assertThat(userConfiguredAccess.getUserConfiguredAccessId()).isEqualTo(userConfiguredAccessId);
        assertThat(userConfiguredAccess.getEnabled()).isEqualTo(enabled);
    }
}
