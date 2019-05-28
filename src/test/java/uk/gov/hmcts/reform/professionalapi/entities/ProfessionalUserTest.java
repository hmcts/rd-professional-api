package uk.gov.hmcts.reform.professionalapi.entities;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static uk.gov.hmcts.reform.professionalapi.generator.ProfessionalApiGenerator.LENGTH_OF_UUID;

import java.time.LocalDateTime;
import java.util.ArrayList;

import org.junit.Test;

import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUserStatus;

public class ProfessionalUserTest {

    @Test
    public void creates_professional_user_correctly() {

        Organisation organisation = mock(Organisation.class);

        ProfessionalUser professionalUser = new ProfessionalUser(
                                                                 "some-fname",
                                                                 "some-lname",
                                                                 "some-email-address",
                                                                 ProfessionalUserStatus.PENDING,
                                                                 organisation);

        assertThat(professionalUser.getFirstName()).isEqualTo("some-fname");
        assertThat(professionalUser.getLastName()).isEqualTo("some-lname");
        assertThat(professionalUser.getStatus()).isEqualTo(ProfessionalUserStatus.PENDING);
        assertThat(professionalUser.getEmailAddress()).isEqualTo("some-email-address");
        assertThat(professionalUser.getOrganisation()).isEqualTo(organisation);
        assertThat(professionalUser.getLastUpdated()).isNull();
        assertThat(professionalUser.getCreated()).isNull();
        assertThat(professionalUser.getUserIdentifier()).isNotNull();
        assertThat(professionalUser.getUserIdentifier().toString().length()).isEqualTo(LENGTH_OF_UUID);

        assertThat(professionalUser.getId()).isNull(); // hibernate generated

        professionalUser.setLastUpdated(LocalDateTime.now());

        professionalUser.setCreated(LocalDateTime.now());

        assertThat(professionalUser.getLastUpdated()).isNotNull();

        assertThat(professionalUser.getCreated()).isNotNull();

        professionalUser.setUserAccountMap(new ArrayList<>());

        assertThat(professionalUser.getUserAccountMap()).isNotNull();


        ProfessionalUser user = new ProfessionalUser();
        assertThat(user).isNotNull();
    }

}