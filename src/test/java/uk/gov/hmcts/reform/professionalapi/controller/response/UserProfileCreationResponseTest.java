package uk.gov.hmcts.reform.professionalapi.controller.response;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class UserProfileCreationResponseTest {

    private final UserProfileCreationResponse userProfileCreationResponse = new UserProfileCreationResponse();
    private final String testUuid = UUID.randomUUID().toString();

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