package uk.gov.hmcts.reform.professionalapi.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
class UserConfiguredAccessIdTest {

    @Test
    void test_creates_user_configured_access_id_correctly() {
        ProfessionalUser professionalUser = new ProfessionalUser();

        String jurisdictionId = "jurisdictionId";
        String orgProfileId = "orgProfileId";
        String accessTypeId = "accessTypeId";
        UserConfiguredAccessId userConfiguredAccessId = new UserConfiguredAccessId(professionalUser,
                jurisdictionId, orgProfileId, accessTypeId);
        assertThat(userConfiguredAccessId.getProfessionalUser()).isEqualTo(professionalUser);
        assertThat(userConfiguredAccessId.getJurisdictionId()).isEqualTo(jurisdictionId);
        assertThat(userConfiguredAccessId.getOrganisationProfileId()).isEqualTo(orgProfileId);
        assertThat(userConfiguredAccessId.getAccessTypeId()).isEqualTo(accessTypeId);
    }
}
