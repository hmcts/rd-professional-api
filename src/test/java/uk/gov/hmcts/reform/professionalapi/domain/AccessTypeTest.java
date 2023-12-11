package uk.gov.hmcts.reform.professionalapi.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class AccessTypeTest {

    private static final String ACCESS_TYPE_ID = "accessTypeId1";
    private static final String JURISDICTION = "jurisdiction1";
    private static final String ORG_PROFILE_ID = "organisationProfileId1";
    private static final Boolean ENABLED = true;

    @Test
    void test_should_hold_values_after_creation() {
        AccessType accessType = new AccessType();
        accessType.setAccessTypeId(ACCESS_TYPE_ID);
        accessType.setJurisdictionId(JURISDICTION);
        accessType.setOrganisationProfileId(ORG_PROFILE_ID);
        accessType.setEnabled(ENABLED);

        assertThat(accessType.getAccessTypeId()).isEqualTo(ACCESS_TYPE_ID);
        assertThat(accessType.getJurisdictionId()).isEqualTo(JURISDICTION);
        assertThat(accessType.getOrganisationProfileId()).isEqualTo(ORG_PROFILE_ID);
        assertThat(accessType.getEnabled()).isEqualTo(ENABLED);
    }
}
