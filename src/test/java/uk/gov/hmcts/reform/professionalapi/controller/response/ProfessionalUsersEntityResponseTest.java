package uk.gov.hmcts.reform.professionalapi.controller.response;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class ProfessionalUsersEntityResponseTest {

    private ProfessionalUsersEntityResponse sut;

    @Test
    @SuppressWarnings("unchecked")
    void test_ProfessionalUsersEntityResponse() throws Exception {
        final String dummyFirstName = "testFn";
        final String dummyLastName = "testLn";
        final String dummyEmail = "test@test.com";

        ProfessionalUser professionalUser = new ProfessionalUser(dummyFirstName, dummyLastName, dummyEmail,
                new Organisation());
        professionalUser.setUserIdentifier(UUID.randomUUID());

        ProfessionalUsersResponse professionalResponse = new ProfessionalUsersResponse(professionalUser);
        List<ProfessionalUsersResponse> professionalUsers = new ArrayList<>();
        professionalUsers.add(professionalResponse);

        sut = new ProfessionalUsersEntityResponse();
        sut.setUserProfiles(professionalUsers);

        ProfessionalUsersResponse professionalUsersResponse = new ProfessionalUsersResponse(professionalUser);
        List<ProfessionalUsersResponse> usersExpected = new ArrayList<>();
        usersExpected.add(professionalUsersResponse);

        assertThat(UUID.fromString(sut.getUsers().get(0).getUserIdentifier()))
                .isEqualTo(professionalUser.getUserIdentifier());
        assertThat(sut.getUsers().get(0).getFirstName()).isEqualTo(professionalUser.getFirstName());
        assertThat(sut.getUsers().get(0).getLastName()).isEqualTo(professionalUser.getLastName());
        assertThat(sut.getUsers().get(0).getEmail()).isEqualTo(professionalUser.getEmailAddress());
    }
}