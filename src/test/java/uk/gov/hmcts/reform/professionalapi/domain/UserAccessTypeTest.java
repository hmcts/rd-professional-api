package uk.gov.hmcts.reform.professionalapi.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class UserAccessTypeTest {

    private static final String ACCESS_TYPE_ID = "accessTypeId1";
    private static final String JURISDICTION = "jurisdiction1";
    private static final String ORG_PROFILE_ID = "organisationProfileId1";
    private static final Boolean ENABLED = true;

    @Test
    void test_should_hold_values_after_creation() {
        UserAccessType userAccessType = new UserAccessType();
        userAccessType.setAccessTypeId(ACCESS_TYPE_ID);
        userAccessType.setJurisdictionId(JURISDICTION);
        userAccessType.setOrganisationProfileId(ORG_PROFILE_ID);
        userAccessType.setEnabled(ENABLED);

        assertThat(userAccessType.getAccessTypeId()).isEqualTo(ACCESS_TYPE_ID);
        assertThat(userAccessType.getJurisdictionId()).isEqualTo(JURISDICTION);
        assertThat(userAccessType.getOrganisationProfileId()).isEqualTo(ORG_PROFILE_ID);
        assertThat(userAccessType.getEnabled()).isEqualTo(ENABLED);
    }
}
