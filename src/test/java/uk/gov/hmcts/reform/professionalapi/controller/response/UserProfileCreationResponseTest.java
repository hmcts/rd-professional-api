package uk.gov.hmcts.reform.professionalapi.controller.response;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.UUID;

import org.junit.Test;
import org.springframework.http.HttpStatus;

public class UserProfileCreationResponseTest {

    private UserProfileCreationResponse userProfileCreationResponse = new UserProfileCreationResponse();
    private String testUuid = UUID.randomUUID().toString();

    @Test
    public void has_mandatory_fields_specified_not_null() {
        userProfileCreationResponse.setIdamId(testUuid);
        userProfileCreationResponse.setIdamRegistrationResponse(201);

        assertThat(userProfileCreationResponse.getIdamId()).isEqualTo(testUuid);
        assertThat(userProfileCreationResponse.getIdamRegistrationResponse()).isEqualTo(201);
    }

    @Test
    public void test_isUserCreated() {
        userProfileCreationResponse.setIdamRegistrationResponse(HttpStatus.CREATED.value());

        assertTrue(userProfileCreationResponse.isUserCreated());
    }
}