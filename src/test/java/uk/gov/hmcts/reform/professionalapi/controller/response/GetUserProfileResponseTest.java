package uk.gov.hmcts.reform.professionalapi.controller.response;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.professionalapi.controller.constants.IdamStatus;
import uk.gov.hmcts.reform.professionalapi.domain.UserProfile;

@ExtendWith(MockitoExtension.class)
class GetUserProfileResponseTest {

    @Test
    void test_GetUserProfileResponseTest() {
        UserProfile userProfile = new UserProfile(UUID.randomUUID().toString(), "test@email.com",
                "fName", "lName", IdamStatus.PENDING);

        GetUserProfileResponse getUserProfileResponse = new GetUserProfileResponse(userProfile, false);

        assertThat(getUserProfileResponse.getEmail()).isEqualTo(userProfile.getEmail());
        assertThat(getUserProfileResponse.getFirstName()).isEqualTo(userProfile.getFirstName());
        assertThat(getUserProfileResponse.getLastName()).isEqualTo(userProfile.getLastName());
        assertThat(getUserProfileResponse.getIdamId()).isEqualTo(userProfile.getIdamId());
        assertThat(getUserProfileResponse.getIdamStatus()).isEqualTo(userProfile.getIdamStatus());
    }
}
