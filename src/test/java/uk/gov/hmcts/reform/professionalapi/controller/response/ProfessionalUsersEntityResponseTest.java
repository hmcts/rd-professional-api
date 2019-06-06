package uk.gov.hmcts.reform.professionalapi.controller.response;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.Test;

import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUserStatus;

public class ProfessionalUsersEntityResponseTest {

    private ProfessionalUsersEntityResponse sut;

    @Test
    @SuppressWarnings("unchecked")
    public void testProfessionalUsersEntityResponse() throws Exception {
        final int lengthOfUuid = 36;
        final String dummyFirstName = "Joe";
        final String dummyLastName = "Bloggs";
        final String dummyEmail = "joe.bloggs@email.com";
        final ProfessionalUserStatus dummyStatus = ProfessionalUserStatus.ACTIVE;

        ProfessionalUser professionalUser = new ProfessionalUser(
                dummyFirstName,
                dummyLastName,
                dummyEmail,
                dummyStatus,
                new Organisation());
        List<ProfessionalUser> professionalUsers = new ArrayList<>();
        professionalUsers.add(professionalUser);

        sut = new ProfessionalUsersEntityResponse(professionalUsers);

        ProfessionalUsersResponse professionalUsersResponse = new ProfessionalUsersResponse(professionalUser);
        List<ProfessionalUsersResponse> usersExpected = new ArrayList<>();
        usersExpected.add(professionalUsersResponse);

        Field firstNameField = sut.getClass().getDeclaredField("users");
        firstNameField.setAccessible(true);
        Object obj = ((List<ProfessionalUsersResponse>) firstNameField.get(sut)).get(0);

        Field f = obj.getClass().getDeclaredField("userIdentifier");
        f.setAccessible(true);
        final UUID userIdentifier = (UUID) f.get(obj);

        f = obj.getClass().getDeclaredField("firstName");
        f.setAccessible(true);
        final String firstName = (String) f.get(obj);

        f = obj.getClass().getDeclaredField("lastName");
        f.setAccessible(true);
        final String lastName = (String) f.get(obj);

        f = obj.getClass().getDeclaredField("email");
        f.setAccessible(true);
        final String email = (String) f.get(obj);

        f = obj.getClass().getDeclaredField("status");
        f.setAccessible(true);
        final ProfessionalUserStatus status = (ProfessionalUserStatus) f.get(obj);

        assertThat(userIdentifier.toString().length()).isEqualTo(lengthOfUuid);
        assertThat(firstName).isEqualTo(dummyFirstName);
        assertThat(lastName).isEqualTo(dummyLastName);
        assertThat(email).isEqualTo(dummyEmail);
        assertThat(status).isEqualTo(dummyStatus);
    }
}