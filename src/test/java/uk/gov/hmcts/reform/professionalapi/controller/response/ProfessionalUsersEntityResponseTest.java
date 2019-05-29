package uk.gov.hmcts.reform.professionalapi.controller.response;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUserStatus.ACTIVE;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;

public class ProfessionalUsersEntityResponseTest {

    private ProfessionalUsersEntityResponse professionalUsersEntityResponse;

    @Test
    @SuppressWarnings("unchecked")
    public void testProfessionalUsersEntityResponse() throws Exception {
        final String dummyFirstName = "Joe";
        final String dummyLastName = "Bloggs";
        final String dummyEmail = "joe.bloggs@email.com";

        ProfessionalUser professionalUser = new ProfessionalUser(
                dummyFirstName,
                dummyLastName,
                dummyEmail,
                ACTIVE,
                new Organisation());
        List<ProfessionalUser> professionalUsers = new ArrayList<>();
        professionalUsers.add(professionalUser);

        professionalUsersEntityResponse = new ProfessionalUsersEntityResponse(professionalUsers);

        ProfessionalUsersResponse professionalUsersResponse = new ProfessionalUsersResponse(professionalUser);
        List<ProfessionalUsersResponse> usersExpected = new ArrayList<>();
        usersExpected.add(professionalUsersResponse);

        Field firstNameField = professionalUsersEntityResponse.getClass().getDeclaredField("users");
        firstNameField.setAccessible(true);
        Object objs = firstNameField.get(professionalUsersEntityResponse);

        assertThat((((List<ProfessionalUsersResponse>)objs).get(0)).getFirstName()).isEqualTo(dummyFirstName);
        assertThat((((List<ProfessionalUsersResponse>)objs).get(0)).getLastName()).isEqualTo(dummyLastName);
        assertThat((((List<ProfessionalUsersResponse>)objs).get(0)).getEmail()).isEqualTo(dummyEmail);
    }
}