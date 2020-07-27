package uk.gov.hmcts.reform.professionalapi.controller.response;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;

public class ProfessionalUsersResponseTest {

    private final String expectEmailAddress = "dummy@email.com";
    private final String expectFirstName = "Bob";
    private final String expectLastName = "Smith";

    @Test
    public void test_professionalUsers() throws NoSuchFieldException, IllegalAccessException {
        ProfessionalUsersResponse professionalUsersResponse
                = new ProfessionalUsersResponse(new ProfessionalUser(expectFirstName, expectLastName,
                expectEmailAddress, null));

        assertThat(professionalUsersResponse.getEmail()).isEqualTo(expectEmailAddress);
        assertThat(professionalUsersResponse.getFirstName()).isEqualTo(expectFirstName);
        assertThat(professionalUsersResponse.getLastName()).isEqualTo(expectLastName);
    }
}