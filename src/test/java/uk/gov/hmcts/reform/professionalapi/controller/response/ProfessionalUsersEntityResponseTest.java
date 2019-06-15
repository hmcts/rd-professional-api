package uk.gov.hmcts.reform.professionalapi.controller.response;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

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

        assertThat(sut.getUsers().get(0).getUserIdentifier()).isEqualTo(professionalUser.getUserIdentifier());
        assertThat(sut.getUsers().get(0).getFirstName()).isEqualTo(professionalUser.getFirstName());
        assertThat(sut.getUsers().get(0).getLastName()).isEqualTo(professionalUser.getLastName());
        assertThat(sut.getUsers().get(0).getEmail()).isEqualTo(professionalUser.getEmailAddress());
    }
}