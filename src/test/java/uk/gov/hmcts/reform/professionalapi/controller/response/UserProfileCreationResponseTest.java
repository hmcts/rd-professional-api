package uk.gov.hmcts.reform.professionalapi.controller.response;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.UUID;
import org.junit.Test;
import org.springframework.http.HttpStatus;

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

    @Test
    public void test_isUserCreated() {
        UserProfileCreationResponse userProfileCreationResponse = new UserProfileCreationResponse();
        userProfileCreationResponse.setIdamRegistrationResponse(HttpStatus.CREATED.value());

        Boolean result = userProfileCreationResponse.isUserCreated();
        assertTrue(result);
    }
}