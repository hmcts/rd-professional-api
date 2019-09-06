package uk.gov.hmcts.reform.professionalapi.controller.response;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.Test;

public class UserProfileCreationResponseTest {


    @Test
    public void has_mandatory_fields_specified_not_null() {

        UserProfileCreationResponse userProfileCreationResponse = new UserProfileCreationResponse();
        String testUuid = UUID.randomUUID().toString();
        userProfileCreationResponse.setIdamId(testUuid.toString());
        userProfileCreationResponse.setIdamRegistrationResponse(201);

        assertThat(userProfileCreationResponse.getIdamId()).isEqualTo(testUuid);
        assertThat(userProfileCreationResponse.getIdamRegistrationResponse()).isEqualTo(201);
    }
}