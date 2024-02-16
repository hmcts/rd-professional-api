package uk.gov.hmcts.reform.professionalapi.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class RefreshUserTest {

    @Test
    void test_should_hold_values_after_creation() {
        OrganisationInfo orgInfo =
                new OrganisationInfo("orgId", OrganisationStatus.ACTIVE, LocalDateTime.now(), List.of("PROFILE"));
        UserAccessType userAccessType = new UserAccessType();
        userAccessType.setAccessTypeId("ACCESS_TYPE_ID");
        userAccessType.setJurisdictionId("JURISDICTION");
        userAccessType.setOrganisationProfileId("ORG_PROFILE_ID");
        userAccessType.setEnabled(true);


        RefreshUser refreshUser = new RefreshUser();
        refreshUser.setUserIdentifier("user-identifier");
        LocalDateTime updated = LocalDateTime.now();
        LocalDateTime deleted = LocalDateTime.now();
        refreshUser.setLastUpdated(updated);
        refreshUser.setOrganisationInfo(orgInfo);
        refreshUser.setUserAccessTypes(List.of(userAccessType));
        refreshUser.setDateTimeDeleted(deleted);

        assertThat(refreshUser.getUserIdentifier()).isEqualTo("user-identifier");
        assertThat(refreshUser.getLastUpdated()).isEqualTo(updated);
        assertThat(refreshUser.getOrganisationInfo()).isEqualTo(orgInfo);
        assertThat(refreshUser.getUserAccessTypes()).hasSize(1);
        assertThat(refreshUser.getUserAccessTypes().get(0)).isEqualTo(userAccessType);
        assertThat(refreshUser.getDateTimeDeleted()).isEqualTo(deleted);
    }
}
