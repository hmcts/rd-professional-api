package uk.gov.hmcts.reform.professionalapi.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class RefreshUserTest {

    @Test
    void test_should_hold_values_after_creation() {
        RefreshUser refreshUser = new RefreshUser();
        refreshUser.setUserIdentifier("user-identifier");
        refreshUser.setLastUpdated(LocalDateTime.now());
        refreshUser.setOrganisationIdentifier("org-identifier");
        refreshUser.setUserAccessTypes(new ArrayList<>());

        assertThat(refreshUser.getUserIdentifier()).isEqualTo("user-identifier");
        assertThat(refreshUser.getLastUpdated()).isNotNull();
        assertThat(refreshUser.getOrganisationIdentifier()).isEqualTo("org-identifier");
        assertThat(refreshUser.getUserAccessTypes()).isNotNull();
    }
}
