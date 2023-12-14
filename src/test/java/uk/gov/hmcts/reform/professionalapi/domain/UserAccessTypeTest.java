package uk.gov.hmcts.reform.professionalapi.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserAccessTypeTest {

    @Test
    void test_AccessType() {
        UserAccessType userAccessType = new UserAccessType(
                "jurisdictionId", "organisationProfileId", "accessTypeId", true);

        assertTrue(userAccessType.getEnabled());
        assertThat(userAccessType.getJurisdictionId()).isEqualTo("jurisdictionId");
        assertThat(userAccessType.getOrganisationProfileId()).isEqualTo("organisationProfileId");
        assertThat(userAccessType.getAccessTypeId()).isEqualTo("accessTypeId");
    }
}
