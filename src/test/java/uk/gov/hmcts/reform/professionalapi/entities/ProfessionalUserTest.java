package uk.gov.hmcts.reform.professionalapi.entities;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;

public class ProfessionalUserTest {

    @Test
    public void creates_professional_user_correctly() {
        List<String> roles = new ArrayList<>();
        roles.add("role");

        Organisation organisation = mock(Organisation.class);

        ProfessionalUser professionalUser = new ProfessionalUser(
                                                                 "some-fname",
                                                                 "some-lname",
                                                                 "some-email-address",
                                                                 organisation);

        assertThat(professionalUser.getFirstName()).isEqualTo("some-fname");
        assertThat(professionalUser.getLastName()).isEqualTo("some-lname");
        assertThat(professionalUser.getEmailAddress()).isEqualTo("some-email-address");
        assertThat(professionalUser.getOrganisation()).isEqualTo(organisation);
        assertThat(professionalUser.getLastUpdated()).isNull();
        assertThat(professionalUser.getCreated()).isNull();
        assertThat(professionalUser.getUserIdentifier()).isNull();

        assertThat(professionalUser.getId()).isNull(); // hibernate generated

        professionalUser.setLastUpdated(LocalDateTime.now());

        professionalUser.setCreated(LocalDateTime.now());

        professionalUser.setRoles(roles);

        assertThat(professionalUser.getRoles().size()).isEqualTo(1);

        assertThat(professionalUser.getLastUpdated()).isNotNull();

        assertThat(professionalUser.getCreated()).isNotNull();

        professionalUser.setUserAccountMap(new ArrayList<>());

        assertThat(professionalUser.getUserAccountMap()).isNotNull();


        ProfessionalUser user = new ProfessionalUser();
        assertThat(user).isNotNull();
    }

}