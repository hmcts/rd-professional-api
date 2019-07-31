package uk.gov.hmcts.reform.professionalapi.controller.response;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class MultipleUserProfilesResponseTest {

    private UserProfile userProfileMock = mock(UserProfile.class);

    @Test
    public void multipleUserProfileResponseTest() {
        List<UserProfile> userProfiles = new ArrayList<>();
        userProfiles.add(userProfileMock);

        MultipleUserProfilesResponse multipleUserProfilesResponse = new MultipleUserProfilesResponse(userProfiles, true);

        assertThat(multipleUserProfilesResponse.getUserProfiles().size()).isEqualTo(1);
    }

}
