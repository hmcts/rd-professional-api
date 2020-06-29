package uk.gov.hmcts.reform.professionalapi.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.junit.Test;

public class ProfessionalUserTest {

    @Test
    public void creates_professional_user_correctly() {
        List<String> roles = new ArrayList<>();
        roles.add("pui-user-manager");

        Organisation organisation = new Organisation();
        ProfessionalUser professionalUser = new ProfessionalUser("some-fname", "some-lname", "some-email-address", organisation);

        professionalUser.setLastUpdated(LocalDateTime.now());
        professionalUser.setCreated(LocalDateTime.now());
        professionalUser.setRoles(roles);
        professionalUser.setUserAccountMap(Arrays.asList(new UserAccountMap()));

        assertThat(professionalUser.getFirstName()).isEqualTo("some-fname");
        assertThat(professionalUser.getLastName()).isEqualTo("some-lname");
        assertThat(professionalUser.getEmailAddress()).isEqualTo("some-email-address");
        assertThat(professionalUser.getOrganisation()).isEqualTo(organisation);
        assertThat(professionalUser.getUserIdentifier()).isNull();
        assertThat(professionalUser.getId()).isNull(); // hibernate generated
        assertThat(professionalUser.getRoles().size()).isEqualTo(1);
        assertThat(professionalUser.getRoles().get(0)).isEqualTo("pui-user-manager");
        assertThat(professionalUser.getLastUpdated()).isNotNull();
        assertThat(professionalUser.getCreated()).isNotNull();
        assertThat(professionalUser.getUserAccountMap()).isNotNull();
        assertThat(professionalUser.getUserAccountMap()).isNotEmpty();

        ProfessionalUser user = new ProfessionalUser();
        assertThat(user).isNotNull();
    }

    @Test
    public void test_toSuperUser() {
        Organisation organisation = new Organisation();
        ProfessionalUser professionalUser = new ProfessionalUser("some-fname", "some-lname", "some-email-address", organisation);

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