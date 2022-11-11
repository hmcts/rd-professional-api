package uk.gov.hmcts.reform.professionalapi.controller.response;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class ProfessionalUsersResponseTest {

    private final String expectEmailAddress = "dummy@email.com";
    private final String expectFirstName = "Bob";
    private final String expectLastName = "Smith";

    @Test
    void test_professionalUsers() throws NoSuchFieldException, IllegalAccessException {
        ProfessionalUser professionalUser = new ProfessionalUser(expectFirstName, expectLastName,
                expectEmailAddress, null);
        professionalUser.setUserIdentifier(UUID.randomUUID());
        ProfessionalUsersResponse professionalUsersResponse
                = new ProfessionalUsersResponse(professionalUser);

        assertThat(professionalUsersResponse.getEmail()).isEqualTo(expectEmailAddress);
        assertThat(professionalUsersResponse.getFirstName()).isEqualTo(expectFirstName);
        assertThat(professionalUsersResponse.getLastName()).isEqualTo(expectLastName);
    }
}