package uk.gov.hmcts.reform.professionalapi.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AccessTypeTest {

    @Test
    void test_fromUserConfiguredAccess() {
        ProfessionalUser professionalUser = new ProfessionalUser();
        UserConfiguredAccessId userConfiguredAccessId = new UserConfiguredAccessId(professionalUser,
                "jurisdictionId", "organisationProfileId", "accessTypeId");
        UserConfiguredAccess userConfiguredAccess = new UserConfiguredAccess(userConfiguredAccessId, true);

        AccessType accessType = AccessType.fromUserConfiguredAccess(userConfiguredAccess);

        assertTrue(accessType.getEnabled());
        assertThat(accessType.getJurisdictionId()).isEqualTo("jurisdictionId");
        assertThat(accessType.getOrganisationProfileId()).isEqualTo("organisationProfileId");
        assertThat(accessType.getAccessTypeId()).isEqualTo("accessTypeId");
    }
}
