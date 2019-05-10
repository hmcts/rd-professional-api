package uk.gov.hmcts.reform.professionalapi.entities;

import static org.assertj.core.api.Java6Assertions.assertThat;

import java.time.LocalDateTime;

import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUserStatus;

public class ProfessionalUserTest {

    private Organisation organisation;

    private ProfessionalUser professionalUser;

    @Before
    public void setUp() {

        organisation = new Organisation();
        professionalUser = new ProfessionalUser();
        professionalUser.setFirstName("some-fname");
        professionalUser.setLastName("some-lname");
        professionalUser.setEmailAddress("some-email-address");
        professionalUser.setStatus(ProfessionalUserStatus.PENDING.name());
        professionalUser.setOrganisation(organisation );
    }

    @Test
    public void creates_professional_user_correctly() {

        assertThat(professionalUser.getFirstName()).isEqualTo("some-fname");
        assertThat(professionalUser.getLastName()).isEqualTo("some-lname");
        assertThat(professionalUser.getStatus()).isEqualTo(ProfessionalUserStatus.PENDING.name());
        assertThat(professionalUser.getEmailAddress()).isEqualTo("some-email-address");
        assertThat(professionalUser.getOrganisation()).isNotNull();

        assertThat(professionalUser.getId()).isNull(); // hibernate generated

        professionalUser.setLastUpdated(LocalDateTime.now());

        professionalUser.setCreated(LocalDateTime.now());

        assertThat(professionalUser.getLastUpdated()).isNotNull();

        assertThat(professionalUser.getCreated()).isNotNull();
    }
}