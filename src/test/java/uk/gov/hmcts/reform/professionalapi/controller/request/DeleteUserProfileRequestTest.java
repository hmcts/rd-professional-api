package uk.gov.hmcts.reform.professionalapi.controller.request;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DeleteUserProfileRequestTest {

    @Test
    void test_DeleteUserProfileRequest() {

        Set<String> userIds = new HashSet<>();
        userIds.add(UUID.randomUUID().toString());
        DeleteUserProfilesRequest delUserProfileRequest = new DeleteUserProfilesRequest(userIds);
        assertThat(delUserProfileRequest.getUserIds()).containsAll(userIds);
        assertThat(delUserProfileRequest.getUserIds()).hasSize(1);
    }

}
