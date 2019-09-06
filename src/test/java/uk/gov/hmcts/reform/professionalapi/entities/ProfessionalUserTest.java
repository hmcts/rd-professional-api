package uk.gov.hmcts.reform.professionalapi.entities;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.Test;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;
import uk.gov.hmcts.reform.professionalapi.domain.SuperUser;

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

    @Test
    public void test_toSuperUser() {

        Organisation organisation = mock(Organisation.class);
        ProfessionalUser professionalUser = new ProfessionalUser(
                "some-fname",
                "some-lname",
                "some-email-address",
                organisation);

        UUID id = UUID.randomUUID();
        professionalUser.setUserIdentifier(id.toString());
        professionalUser.setId(id);
        professionalUser.setCreated(LocalDateTime.now());
        professionalUser.setLastUpdated(LocalDateTime.now());
        professionalUser.setDeleted(LocalDateTime.now());

        SuperUser superUser = professionalUser.toSuperUser();
        assertThat(superUser.getFirstName()).isEqualTo("some-fname");
        assertThat(superUser.getLastName()).isEqualTo("some-lname");
        assertThat(superUser.getEmailAddress()).isEqualTo("some-email-address");
        assertThat(superUser.getCreated()).isNotNull();
        assertThat(superUser.getDeleted()).isNotNull();
        assertThat(superUser.getId()).isNotNull();
        assertThat(superUser.getLastUpdated()).isNotNull();
        assertThat(superUser.getUserIdentifier()).isEqualTo(id.toString());
    }

}