package uk.gov.hmcts.reform.professionalapi.controller.response;

import java.util.UUID;
import org.junit.Test;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUserStatus;


public class ProfessionalUsersResponseTest {


    @Test
    public void professionalUsersTest() {

        ProfessionalUser user = new ProfessionalUser();
        user.setEmailAddress("rotimi@gmail.com");
        user.setFirstName("Test");
        user.setLastName("best");
        user.setUserIdentifier(UUID.randomUUID());
        user.setStatus(ProfessionalUserStatus.ACTIVE);
        ProfessionalUsersResponse professionalUsersResponse  = new ProfessionalUsersResponse(user);
        // TODO
        // assertThat(professionalUsersResponse.getEmail()).isEqualTo(user.getEmailAddress());
        // assertThat(professionalUsersResponse.getLastName()).isEqualTo(user.getLastName());
        // assertThat(professionalUsersResponse.getFirstName()).isEqualTo(user.getFirstName());
        // assertThat(professionalUsersResponse.getUserIdentifier()).isEqualTo(user.getUserIdentifier());
        // assertThat(professionalUsersResponse.getStatus()).isEqualTo(user.getStatus());
    }
}