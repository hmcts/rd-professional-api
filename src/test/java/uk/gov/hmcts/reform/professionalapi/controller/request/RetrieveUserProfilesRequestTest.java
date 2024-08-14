package uk.gov.hmcts.reform.professionalapi.controller.request;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.professionalapi.util.RefDataUtil.randomUUID;

@ExtendWith(MockitoExtension.class)
class RetrieveUserProfilesRequestTest {

    private RetrieveUserProfilesRequest retrieveUserProfilesRequest;
    private final String userId = randomUUID();

    @Test
    void test_RetrieveUserProfilesRequestSetter() {
        retrieveUserProfilesRequest = new RetrieveUserProfilesRequest(null);
        retrieveUserProfilesRequest.setUserIds(singletonList(userId));

        assertThat(retrieveUserProfilesRequest.getUserIds()).hasSize(1);
        assertThat(retrieveUserProfilesRequest.getUserIds().get(0)).isEqualTo(userId);
    }

    @Test
    void test_test_RetrieveUserProfilesRequestBuilder() {
        retrieveUserProfilesRequest = RetrieveUserProfilesRequest.aRetrieveUserProfilesRequest()
                .userIds(singletonList(userId)).build();

        assertThat(retrieveUserProfilesRequest.getUserIds()).hasSize(1);
        assertThat(retrieveUserProfilesRequest.getUserIds().get(0)).isEqualTo(userId);
    }
}
