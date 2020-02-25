package uk.gov.hmcts.reform.professionalapi.controller.response;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.professionalapi.domain.UserProfile;

public class MultipleUserProfilesResponseTest {

    private UserProfile userProfile;
    private GetUserProfileResponse getUserProfileResponse;
    private List<UserProfile> userProfiles = new ArrayList<>();
    private List<GetUserProfileResponse> getUserProfileResponses = new ArrayList<>();

    @Before
    public void setUp() {
        userProfile = new UserProfile(UUID.randomUUID().toString(), "test@email.com", "fName", "lName", IdamStatus.PENDING);
        getUserProfileResponse = new GetUserProfileResponse();
        userProfiles.add(userProfile);
        getUserProfileResponses.add(getUserProfileResponse);
    }

    @Test
    public void multipleUserProfileResponseTest() {
        MultipleUserProfilesResponse multipleUserProfilesResponse = new MultipleUserProfilesResponse(userProfiles, true);
        assertThat(multipleUserProfilesResponse.getUserProfiles().size()).isEqualTo(1);
    }

    @Test
    public void test_MultipleUserProfilesResponseSetter() {
        MultipleUserProfilesResponse multipleUserProfilesResponse = new MultipleUserProfilesResponse(userProfiles, true);

        multipleUserProfilesResponse.setUserProfiles(getUserProfileResponses);

        assertThat(multipleUserProfilesResponse.getUserProfiles()).isEqualTo(getUserProfileResponses);
    }

}
