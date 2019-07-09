package uk.gov.hmcts.reform.professionalapi.controller.response;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;

public class ProfessionalUsersResponseTest {


    @Test
    public void professionalUsersTest() throws NoSuchFieldException, IllegalAccessException {

        final String expectEmailAddress = "dummy@email.com";
        final String expectFirstName = "Bob";
        final String expectLastName = "Smith";

        ProfessionalUser user = new ProfessionalUser();
        user.setEmailAddress(expectEmailAddress);
        user.setFirstName(expectFirstName);
        user.setLastName(expectLastName);
        ProfessionalUsersResponse professionalUsersResponse = new ProfessionalUsersResponse(user);

        assertThat(professionalUsersResponse.getEmail()).isEqualTo(expectEmailAddress);
        assertThat(professionalUsersResponse.getFirstName()).isEqualTo(expectFirstName);
        assertThat(professionalUsersResponse.getLastName()).isEqualTo(expectLastName);
    }
}