package uk.gov.hmcts.reform.professionalapi.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class ProfessionalUserTest {

    @Test
    void test_creates_professional_user_correctly() {
        List<String> roles = new ArrayList<>();
        roles.add("pui-user-manager");

        Organisation organisation = new Organisation();
        ProfessionalUser professionalUser = new ProfessionalUser("some-fname", "some-lname",
                "some-email-address", organisation);

        professionalUser.setLastUpdated(LocalDateTime.now());
        professionalUser.setCreated(LocalDateTime.now());
        professionalUser.setRoles(roles);

        assertThat(professionalUser.getFirstName()).isEqualTo("some-fname");
        assertThat(professionalUser.getLastName()).isEqualTo("some-lname");
        assertThat(professionalUser.getEmailAddress()).isEqualTo("some-email-address");
        assertThat(professionalUser.getOrganisation()).isEqualTo(organisation);
        assertThat(professionalUser.getUserIdentifier()).isNull();
        assertThat(professionalUser.getId()).isNull(); // hibernate generated
        assertThat(professionalUser.getRoles()).hasSize(1);
        assertThat(professionalUser.getRoles().get(0)).isEqualTo("pui-user-manager");
        assertThat(professionalUser.getLastUpdated()).isNotNull();
        assertThat(professionalUser.getCreated()).isNotNull();

        ProfessionalUser user = new ProfessionalUser();
        assertThat(user).isNotNull();
    }

    @Test
    void test_toSuperUser() {
        Organisation organisation = new Organisation();
        ProfessionalUser professionalUser = new ProfessionalUser("some-fname", "some-lname",
                "some-email-address", organisation);

        UUID id = UUID.randomUUID();
        professionalUser.setUserIdentifier(id);
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
        assertThat(superUser.getUserIdentifier()).isEqualTo(id);
    }
}