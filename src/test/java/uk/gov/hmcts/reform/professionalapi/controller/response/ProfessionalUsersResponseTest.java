package uk.gov.hmcts.reform.professionalapi.controller.response;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;

@ExtendWith(MockitoExtension.class)
class ProfessionalUsersResponseTest {

    @Test
    void test_professionalUsers() throws NoSuchFieldException, IllegalAccessException {
        String expectEmailAddress = "dummy@email.com";
        String expectFirstName = "Bob";
        String expectLastName = "Smith";
        ProfessionalUsersResponse professionalUsersResponse
                = new ProfessionalUsersResponse(new ProfessionalUser(expectFirstName, expectLastName,
                expectEmailAddress, null));

        assertThat(professionalUsersResponse.getEmail()).isEqualTo(expectEmailAddress);
        assertThat(professionalUsersResponse.getFirstName()).isEqualTo(expectFirstName);
        assertThat(professionalUsersResponse.getLastName()).isEqualTo(expectLastName);
    }
}