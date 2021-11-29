package uk.gov.hmcts.reform.professionalapi.controller.response;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;

public class NewUserResponseTest {

    private final String userIdentifier = UUID.randomUUID().toString();
    private NewUserResponse newUserResponse;
    private UserProfileCreationResponse userProfileCreationResponse;

    @Before
    public void setUp() {
        Organisation organisation = new Organisation("Org-Name", OrganisationStatus.PENDING, "sra-id",
                "companyN", false, "www.org.com");
        ProfessionalUser professionalUser = new ProfessionalUser("some-fname", "some-lname",
                "soMeone@somewhere.com", organisation);
        professionalUser.setUserIdentifier(userIdentifier);
        newUserResponse = new NewUserResponse(professionalUser);
        userProfileCreationResponse = new UserProfileCreationResponse();
        userProfileCreationResponse.setIdamId("ACM1QR");
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
  
    @Test
    public void test_NewUserResponse_with_constructor() {
        newUserResponse = new NewUserResponse(userProfileCreationResponse);
        assertThat(newUserResponse.getUserIdentifier()).isEqualTo("ACM1QR");
    }
}
