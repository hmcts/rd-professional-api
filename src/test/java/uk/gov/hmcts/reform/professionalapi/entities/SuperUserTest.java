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



public class SuperUserTest {

    @Test
    public void creates_super_user() {
        List<String> roles = new ArrayList<>();
        roles.add("role");

        Organisation organisation = mock(Organisation.class);

        SuperUser superUser = new SuperUser(
                                                                 "some-fname",
                                                                 "some-lname",
                                                                 "some-email-address",
                                                                 organisation);

        assertThat(superUser.getFirstName()).isEqualTo("some-fname");
        assertThat(superUser.getLastName()).isEqualTo("some-lname");
        assertThat(superUser.getEmailAddress()).isEqualTo("some-email-address");
        assertThat(superUser.getOrganisation()).isEqualTo(organisation);
        assertThat(superUser.getLastUpdated()).isNull();
        assertThat(superUser.getCreated()).isNull();
        assertThat(superUser.getUserIdentifier()).isNull();

        assertThat(superUser.getId()).isNull(); // hibernate generated

        superUser.setLastUpdated(LocalDateTime.now());

        superUser.setCreated(LocalDateTime.now());
        assertThat(superUser.getLastUpdated()).isNotNull();

        assertThat(superUser.getCreated()).isNotNull();

        ProfessionalUser user = new ProfessionalUser();
        assertThat(user).isNotNull();
    }

    @Test
    public void test_toProfessionalUser() {

        Organisation organisation = mock(Organisation.class);
        SuperUser superUser = new SuperUser(
                "some-fname",
                "some-lname",
                "some-email-address",
                organisation);
        UUID id = UUID.randomUUID();
        superUser.setUserIdentifier(id.toString());
        superUser.setId(id);
        superUser.setCreated(LocalDateTime.now());
        superUser.setLastUpdated(LocalDateTime.now());
        superUser.setDeleted(LocalDateTime.now());


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