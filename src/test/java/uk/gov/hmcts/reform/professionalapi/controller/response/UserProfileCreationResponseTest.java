package uk.gov.hmcts.reform.professionalapi.controller.response;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class UserProfileCreationResponseTest {

    private UserProfileCreationResponse userProfileCreationResponse = new UserProfileCreationResponse();
    private String testUuid = UUID.randomUUID().toString();

    @Test
    void test_has_mandatory_fields_specified_not_null() {
        userProfileCreationResponse.setIdamId(testUuid);
        userProfileCreationResponse.setIdamRegistrationResponse(201);

        assertThat(userProfileCreationResponse.getIdamId()).isEqualTo(testUuid);
        assertThat(userProfileCreationResponse.getIdamRegistrationResponse()).isEqualTo(201);
    }

    @Test
    void test_isUserCreated() {
        userProfileCreationResponse.setIdamRegistrationResponse(HttpStatus.CREATED.value());

        assertTrue(userProfileCreationResponse.isUserCreated());
    }
}