package uk.gov.hmcts.reform.professionalapi.controller.response;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.professionalapi.controller.constants.IdamStatus;
import uk.gov.hmcts.reform.professionalapi.domain.UserProfile;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class MultipleUserProfilesResponseTest {

    private UserProfile userProfile;
    private GetUserProfileResponse getUserProfileResponse;
    private final List<UserProfile> userProfiles = new ArrayList<>();
    private final List<GetUserProfileResponse> getUserProfileResponses = new ArrayList<>();

    @BeforeEach
    void setUp() {
        userProfile = new UserProfile(UUID.randomUUID().toString(), "test@email.com", "fName",
                "lName", IdamStatus.PENDING);
        getUserProfileResponse = new GetUserProfileResponse();
        userProfiles.add(userProfile);
        getUserProfileResponses.add(getUserProfileResponse);
    }

    @Test
    void test_multipleUserProfileResponse() {
        MultipleUserProfilesResponse multipleUserProfilesResponse = new MultipleUserProfilesResponse(userProfiles,
                true);
        assertThat(multipleUserProfilesResponse.getUserProfiles()).hasSize(1);
        assertThat(multipleUserProfilesResponse.getUserProfiles().get(0).getIdamId()).isEqualTo(userProfile
                .getIdamId());
    }

    @Test
    void test_MultipleUserProfilesResponseSetter() {
        MultipleUserProfilesResponse multipleUserProfilesResponse = new MultipleUserProfilesResponse(userProfiles,
                true);

        multipleUserProfilesResponse.setUserProfiles(getUserProfileResponses);

        assertThat(multipleUserProfilesResponse.getUserProfiles()).isEqualTo(getUserProfileResponses);
    }
}
