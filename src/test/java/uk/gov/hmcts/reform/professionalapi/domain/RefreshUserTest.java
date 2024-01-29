package uk.gov.hmcts.reform.professionalapi.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class RefreshUserTest {

    @Test
    void test_should_hold_values_after_creation() {
        OrganisationInfo orgInfo =
                new OrganisationInfo("orgId", OrganisationStatus.ACTIVE, LocalDateTime.now(), List.of("PROFILE"));

        RefreshUser refreshUser = new RefreshUser();
        refreshUser.setUserIdentifier("user-identifier");
        refreshUser.setLastUpdated(LocalDateTime.now());
        refreshUser.setOrganisationInfo(orgInfo);
        refreshUser.setUserAccessTypes(new ArrayList<>());
        refreshUser.setDateTimeDeleted(null);

        assertThat(refreshUser.getUserIdentifier()).isEqualTo("user-identifier");
        assertThat(refreshUser.getLastUpdated()).isNotNull();
        assertThat(refreshUser.getOrganisationInfo()).isNotNull();
        assertThat(refreshUser.getUserAccessTypes()).isNotNull();
        assertThat(refreshUser.getDateTimeDeleted()).isNull();
    }
}
