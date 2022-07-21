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
class SuperUserTest {

    @Test
    void test_creates_super_user() {
        List<String> roles = new ArrayList<>();
        roles.add("role");

        Organisation organisation = new Organisation();
        SuperUser superUser = new SuperUser("some-fname", "some-lname", "some-email-address", organisation);

        superUser.setLastUpdated(LocalDateTime.now());
        superUser.setCreated(LocalDateTime.now());

        assertThat(superUser.getFirstName()).isEqualTo("some-fname");
        assertThat(superUser.getLastName()).isEqualTo("some-lname");
        assertThat(superUser.getEmailAddress()).isEqualTo("some-email-address");
        assertThat(superUser.getOrganisation()).isEqualTo(organisation);
        assertThat(superUser.getUserIdentifier()).isNull();
        assertThat(superUser.getId()).isNull(); // hibernate generated
        assertThat(superUser.getLastUpdated()).isNotNull();
        assertThat(superUser.getCreated()).isNotNull();

        ProfessionalUser user = new ProfessionalUser();
        assertThat(user).isNotNull();
    }

    @Test
    void test_toProfessionalUser() {
        Organisation organisation = new Organisation();
        SuperUser superUser = new SuperUser("some-fname", "some-lname", "some-email-address", organisation);

        UUID id = UUID.randomUUID();
        superUser.setUserIdentifier(id.toString());
        superUser.setId(id);
        superUser.setCreated(LocalDateTime.now());
        superUser.setLastUpdated(LocalDateTime.now());
        superUser.setDeleted(LocalDateTime.now());

        SuperUser superUserNoArg = new SuperUser();
        assertThat(superUserNoArg).isNotNull();

        ProfessionalUser professionalUser = superUser.toProfessionalUser();
        assertThat(professionalUser.getFirstName()).isEqualTo("some-fname");
        assertThat(professionalUser.getLastName()).isEqualTo("some-lname");
        assertThat(professionalUser.getEmailAddress()).isEqualTo("some-email-address");
        assertThat(professionalUser.getCreated()).isNotNull();
        assertThat(professionalUser.getDeleted()).isNotNull();
        assertThat(professionalUser.getId()).isNotNull();
        assertThat(professionalUser.getLastUpdated()).isNotNull();
        assertThat(professionalUser.getUserIdentifier()).isEqualTo(id.toString());
    }
}