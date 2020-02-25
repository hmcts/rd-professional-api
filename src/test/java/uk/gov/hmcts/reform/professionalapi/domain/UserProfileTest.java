package uk.gov.hmcts.reform.professionalapi.domain;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.util.UUID;

import org.junit.Test;
import uk.gov.hmcts.reform.professionalapi.controller.response.IdamStatus;

public class UserProfileTest {

    @Test
    public void test_UserProfile() {
        String id = UUID.randomUUID().toString();
        String email = "test@email.com";
        String firstName = "fName";
        String lastName = "lName";
        IdamStatus idamStatus = IdamStatus.PENDING;

        UserProfile userProfile = new UserProfile(id, email, firstName, lastName, idamStatus);

        assertThat(userProfile.getIdamId()).isEqualTo(id);
        assertThat(userProfile.getEmail()).isEqualTo(email);
        assertThat(userProfile.getFirstName()).isEqualTo(firstName);
        assertThat(userProfile.getLastName()).isEqualTo(lastName);
        assertThat(userProfile.getIdamStatus()).isEqualTo(idamStatus);
    }
}
