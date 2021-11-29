package uk.gov.hmcts.reform.professionalapi.controller.response;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.UUID;

import org.junit.Test;
import org.springframework.http.HttpStatus;

public class UserProfileCreationResponseTest {

    private final UserProfileCreationResponse userProfileCreationResponse = new UserProfileCreationResponse();
    private final String testUuid = UUID.randomUUID().toString();

    @Test
    public void test_has_mandatory_fields_specified_not_null() {
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