package uk.gov.hmcts.reform.professionalapi.controller.request;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RetrieveUserProfilesRequestTest {

    private RetrieveUserProfilesRequest retrieveUserProfilesRequest;
    private final String userId = UUID.randomUUID().toString();

    @Test
    void test_RetrieveUserProfilesRequestSetter() {
        retrieveUserProfilesRequest = new RetrieveUserProfilesRequest(null);
        retrieveUserProfilesRequest.setUserIds(singletonList(userId));

        assertThat(retrieveUserProfilesRequest.getUserIds().size()).isEqualTo(1);
        assertThat(retrieveUserProfilesRequest.getUserIds().get(0)).isEqualTo(userId);
    }

    @Test
    void test_test_RetrieveUserProfilesRequestBuilder() {
        retrieveUserProfilesRequest = RetrieveUserProfilesRequest.aRetrieveUserProfilesRequest()
                .userIds(singletonList(userId)).build();

        assertThat(retrieveUserProfilesRequest.getUserIds().size()).isEqualTo(1);
        assertThat(retrieveUserProfilesRequest.getUserIds().get(0)).isEqualTo(userId);
    }
}
