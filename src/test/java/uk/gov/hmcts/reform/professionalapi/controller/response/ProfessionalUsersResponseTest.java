package uk.gov.hmcts.reform.professionalapi.controller.response;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Field;
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

        Field f = professionalUsersResponse.getClass().getDeclaredField("email");
        f.setAccessible(true);
        email = (String) f.get(professionalUsersResponse);

        assertEquals(email, expectEmailAddress);
    }
}