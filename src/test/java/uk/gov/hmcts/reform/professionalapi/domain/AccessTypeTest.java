package uk.gov.hmcts.reform.professionalapi.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class AccessTypeTest {

    @Test
    void test_should_hold_values_after_creation() {
        AccessType accessType = new AccessType();
        accessType.setJurisdictionId("jurisdiction-id");
        accessType.setOrganisationProfileId("org-profile-id");
        accessType.setAccessTypeId("access-type-id");
        accessType.setEnabled(false);

        assertThat(accessType.getJurisdictionId()).isEqualTo("jurisdiction-id");
        assertThat(accessType.getOrganisationProfileId()).isEqualTo("org-profile-id");
        assertThat(accessType.getAccessTypeId()).isEqualTo("access-type-id");
        assertThat(accessType.isEnabled()).isFalse();
    }
}
