package uk.gov.hmcts.reform.professionalapi.controller.request;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.Test;

public class RetrieveUserProfilesRequestTest {

    @Test
    public void test_RetrieveUserProfilesRequestSetter() {
        RetrieveUserProfilesRequest retrieveUserProfilesRequest = new RetrieveUserProfilesRequest(null);

        List<String> userIds = new ArrayList<>();
        userIds.add(UUID.randomUUID().toString());

        retrieveUserProfilesRequest.setUserIds(userIds);

        assertThat(retrieveUserProfilesRequest.getUserIds()).isEqualTo(userIds);
    }

    @Test
    public void test_test_RetrieveUserProfilesRequestBuilder() {
        List<String> userIds = new ArrayList<>();
        userIds.add(UUID.randomUUID().toString());

        RetrieveUserProfilesRequest retrieveUserProfilesRequest = RetrieveUserProfilesRequest.aRetrieveUserProfilesRequest()
                .userIds(userIds)
                .build();

        assertThat(retrieveUserProfilesRequest.getUserIds()).isEqualTo(userIds);
    }

}
