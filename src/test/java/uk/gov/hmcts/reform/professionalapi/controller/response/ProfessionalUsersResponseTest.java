package uk.gov.hmcts.reform.professionalapi.controller.response;

import org.junit.Test;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;


public class ProfessionalUsersResponseTest {


    @Test
    public void professionalUsersTest() throws NoSuchFieldException, IllegalAccessException {

        String expectEmailAddress = "dummy@email.com";
        ProfessionalUser user = new ProfessionalUser();
        user.setEmailAddress(expectEmailAddress);
        ProfessionalUsersResponse professionalUsersResponse = new ProfessionalUsersResponse(user);

        String email = "";

    }
}