package uk.gov.hmcts.reform.professionalapi.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.professionalapi.controller.constants.IdamStatus;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static uk.gov.hmcts.reform.professionalapi.util.RefDataUtil.randomUUID;

@ExtendWith(MockitoExtension.class)
class UserProfileTest {

    @Test
    void test_UserProfile() {
        String id = randomUUID();
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
