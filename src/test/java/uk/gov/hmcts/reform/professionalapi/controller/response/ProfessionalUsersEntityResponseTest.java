package uk.gov.hmcts.reform.professionalapi.controller.response;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;

public class ProfessionalUsersEntityResponseTest {

    @Test
    @SuppressWarnings("unchecked")
    public void test_ProfessionalUsersEntityResponse() throws Exception {
        final String dummyFirstName = "Joe";
        final String dummyLastName = "Bloggs";
        final String dummyEmail = "joe.bloggs@email.com";

        ProfessionalUser professionalUser = new ProfessionalUser(dummyFirstName, dummyLastName, dummyEmail,
                new Organisation());
        ProfessionalUsersResponse professionalResponse = new ProfessionalUsersResponse(professionalUser);
        List<ProfessionalUsersResponse> professionalUsers = new ArrayList<>();
        professionalUsers.add(professionalResponse);

        ProfessionalUsersEntityResponse sut = new ProfessionalUsersEntityResponse();
        sut.setUserProfiles(professionalUsers);

        ProfessionalUsersResponse professionalUsersResponse = new ProfessionalUsersResponse(professionalUser);
        List<ProfessionalUsersResponse> usersExpected = new ArrayList<>();
        usersExpected.add(professionalUsersResponse);

        assertThat(sut.getUserProfiles().get(0).getUserIdentifier()).isEqualTo(professionalUser.getUserIdentifier());
        assertThat(sut.getUserProfiles().get(0).getFirstName()).isEqualTo(professionalUser.getFirstName());
        assertThat(sut.getUserProfiles().get(0).getLastName()).isEqualTo(professionalUser.getLastName());
        assertThat(sut.getUserProfiles().get(0).getEmail()).isEqualTo(professionalUser.getEmailAddress());
    }
}