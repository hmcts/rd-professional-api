package uk.gov.hmcts.reform.professionalapi.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.Test;
import uk.gov.hmcts.reform.professionalapi.controller.request.NewUserCreationRequest;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUserStatus;
import uk.gov.hmcts.reform.professionalapi.util.ProfessionalUserUtil;

import java.util.ArrayList;
import java.util.List;

public class ProfessionalUserUtilTest {

    @Test
    public void testNewProfessionalUser() {

        Organisation mockOrganisation = mock(Organisation.class);

        String firstName = "First";
        String lastName = "Last";
        String email = "FIRST.LAST@EMAIL.COM";
        String status = ProfessionalUserStatus.PENDING.name();
        List<String> roles = new ArrayList<>();

        String expectedEmail = "first.last@email.com";
        String expectedFirstName = "First";
        String expectedLastName = "Last";

        NewUserCreationRequest dummyNewUserCreationRequest = new NewUserCreationRequest(firstName, lastName, email, status, roles);

        ProfessionalUser professionalUser = ProfessionalUserUtil.createProfessionalUser(dummyNewUserCreationRequest, mockOrganisation);

        assertThat(professionalUser.getEmailAddress()).isEqualTo(expectedEmail);
        assertThat(professionalUser.getFirstName()).isEqualTo(expectedFirstName);
        assertThat(professionalUser.getLastName()).isEqualTo(expectedLastName);
    }
}
