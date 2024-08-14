package uk.gov.hmcts.reform.professionalapi.controller.request;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.professionalapi.util.RefDataUtil.randomUUID;

@ExtendWith(MockitoExtension.class)
class DeleteUserProfilesRequestTest {

    @Test
    void test_DeleteUserProfilesRequest() {

        Set<String> userIds = new HashSet<>();
        userIds.add(randomUUID());
        DeleteUserProfilesRequest delUserProfileRequest = new DeleteUserProfilesRequest(userIds);
        assertThat(delUserProfileRequest.getUserIds()).containsAll(userIds);
        assertThat(delUserProfileRequest.getUserIds()).hasSize(1);

        Set<String> userIds1 = new HashSet<>();
        userIds1.add(randomUUID());
        delUserProfileRequest.setUserIds(userIds1);
        assertThat(delUserProfileRequest.getUserIds()).containsAll(userIds1);

        DeleteUserProfilesRequest deleteUserProfilesRequest1 =
                DeleteUserProfilesRequest.aDeleteUserProfilesRequest().userIds(userIds).build();

        assertThat(deleteUserProfilesRequest1.getUserIds()).containsAll(userIds);
    }

}
