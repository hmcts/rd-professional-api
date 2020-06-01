package uk.gov.hmcts.reform.professionalapi.controller.request;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.junit.Test;

public class DeleteUserProfileRequestTest {

    @Test
    public void test_DeleteUserProfileRequest() {

        Set<String> userIds = new HashSet<>();
        userIds.add(UUID.randomUUID().toString());
        String id = UUID.randomUUID().toString();
        DeleteUserProfilesRequest delUserProfileRequest = new DeleteUserProfilesRequest(userIds);
        assertThat(delUserProfileRequest.getUserIds()).containsAll(userIds);
        assertThat(delUserProfileRequest.getUserIds().size()).isEqualTo(1);
    }

}
