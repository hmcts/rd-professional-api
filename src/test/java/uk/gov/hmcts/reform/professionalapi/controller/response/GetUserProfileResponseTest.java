package uk.gov.hmcts.reform.professionalapi.controller.response;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.professionalapi.controller.constants.IdamStatus;
import uk.gov.hmcts.reform.professionalapi.domain.UserProfile;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.professionalapi.util.RefDataUtil.randomUUID;

@ExtendWith(MockitoExtension.class)
class GetUserProfileResponseTest {

    @Test
    void test_GetUserProfileResponseTest() {
        UserProfile userProfile = new UserProfile(randomUUID(), "test@email.com",
                "fName", "lName", IdamStatus.PENDING);

        GetUserProfileResponse getUserProfileResponse = new GetUserProfileResponse(userProfile, false);

        assertThat(getUserProfileResponse.getEmail()).isEqualTo(userProfile.getEmail());
        assertThat(getUserProfileResponse.getFirstName()).isEqualTo(userProfile.getFirstName());
        assertThat(getUserProfileResponse.getLastName()).isEqualTo(userProfile.getLastName());
        assertThat(getUserProfileResponse.getIdamId()).isEqualTo(userProfile.getIdamId());
        assertThat(getUserProfileResponse.getIdamStatus()).isEqualTo(userProfile.getIdamStatus());
    }
}
