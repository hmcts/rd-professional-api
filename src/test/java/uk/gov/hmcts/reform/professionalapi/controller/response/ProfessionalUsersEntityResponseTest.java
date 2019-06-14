package uk.gov.hmcts.reform.professionalapi.controller.response;

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

    }
}