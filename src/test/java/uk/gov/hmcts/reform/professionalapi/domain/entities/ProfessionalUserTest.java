package uk.gov.hmcts.reform.professionalapi.domain.entities;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.Test;

public class ProfessionalUserTest extends AbstractEntityTest{

    @Test
    public void creates_professional_user_correctly() {

        Organisation organisation = mock(Organisation.class);

        ProfessionalUser professionalUser = new ProfessionalUser(
                "some-fname",
                "some-lname",
                "some-email-address",
                "some-status",
                organisation);

        assertThat(professionalUser.getFirstName()).isEqualTo("some-fname");
        assertThat(professionalUser.getLastName()).isEqualTo("some-lname");
        assertThat(professionalUser.getStatus()).isEqualTo("some-status");
        assertThat(professionalUser.getEmailAddress()).isEqualTo("some-email-address");
        assertThat(professionalUser.getOrganisation()).isEqualTo(organisation);

        assertThat(professionalUser.getId()).isNull(); // hibernate generated
    }

    @Override
    protected Object getBeanInstance() {
        return new ProfessionalUser();
    }
}