package uk.gov.hmcts.reform.professionalapi.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class UserConfiguredAccessTest {

    @Test
    void test_user_configured_access_nall_args() {
        UserConfiguredAccessId userConfiguredAccessId = new UserConfiguredAccessId();
        boolean enabled = true;
        UserConfiguredAccess userConfiguredAccess = new UserConfiguredAccess(userConfiguredAccessId, enabled);
        assertThat(userConfiguredAccess.getUserConfiguredAccessId()).isEqualTo(userConfiguredAccessId);
        assertThat(userConfiguredAccess.getEnabled()).isEqualTo(enabled);
    }

    @Test
    void test_user_configured_access_no_argsEnabled() {
        UserConfiguredAccessId userConfiguredAccessId = new UserConfiguredAccessId();
        boolean enabled = true;
        UserConfiguredAccess userConfiguredAccess = new UserConfiguredAccess();
        userConfiguredAccess.setUserConfiguredAccessId(userConfiguredAccessId);
        userConfiguredAccess.setEnabled(enabled);
        assertThat(userConfiguredAccess.getUserConfiguredAccessId()).isEqualTo(userConfiguredAccessId);
        assertThat(userConfiguredAccess.getEnabled()).isEqualTo(enabled);
    }

    @Test
    void test_user_configured_access_no_argsDisabled() {
        UserConfiguredAccessId userConfiguredAccessId = new UserConfiguredAccessId();
        boolean enabled = false;
        UserConfiguredAccess userConfiguredAccess = new UserConfiguredAccess();
        userConfiguredAccess.setUserConfiguredAccessId(userConfiguredAccessId);
        userConfiguredAccess.setEnabled(enabled);
        assertThat(userConfiguredAccess.getUserConfiguredAccessId()).isEqualTo(userConfiguredAccessId);
        assertThat(userConfiguredAccess.getEnabled()).isEqualTo(enabled);
    }
}
