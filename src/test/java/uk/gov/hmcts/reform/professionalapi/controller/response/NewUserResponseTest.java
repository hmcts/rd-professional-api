package uk.gov.hmcts.reform.professionalapi.controller.response;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;

public class NewUserResponseTest {

    private String userIdentifier = "userIdentifier";
    private ProfessionalUser professionalUser;
    private Organisation organisation;
    private NewUserResponse newUserResponse;

    @Before
    public void setUp() {
        organisation = new Organisation("Org-Name", OrganisationStatus.PENDING, "sra-id", "companyN", false, "www.org.com");
        professionalUser = new ProfessionalUser("some-fname", "some-lname", "soMeone@somewhere.com", organisation);
        professionalUser.setUserIdentifier(userIdentifier);
        newUserResponse = new NewUserResponse(professionalUser);
    }

    @Test
    public void test_getUserIdentifier() {
        assertThat(newUserResponse.getUserIdentifier()).isEqualTo(userIdentifier);
    }

    @Test
    public void test_userIdentifier_with_setter() {
        newUserResponse.setUserIdentifier(userIdentifier);
        newUserResponse.setIdamStatus("ACTIVE");

        assertThat(newUserResponse.getUserIdentifier()).isEqualTo(userIdentifier);
        assertThat(newUserResponse.getIdamStatus()).isEqualTo("ACTIVE");
    }
}
