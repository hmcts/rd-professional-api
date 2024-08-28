package uk.gov.hmcts.reform.professionalapi.controller.response;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.professionalapi.util.RefDataUtil.generateRandomUUID;

@ExtendWith(MockitoExtension.class)
class NewUserResponseTest {

    private final UUID userIdentifier = generateRandomUUID();
    private ProfessionalUser professionalUser;
    private Organisation organisation;
    private NewUserResponse newUserResponse;
    private UserProfileCreationResponse userProfileCreationResponse;

    @BeforeEach
    void setUp() {
        organisation = new Organisation("Org-Name", OrganisationStatus.PENDING, "sra-id",
                "companyN", false, "www.org.com");
        professionalUser = new ProfessionalUser("some-fname", "some-lname",
                "soMeone@somewhere.com", organisation);
        professionalUser.setUserIdentifier(userIdentifier);
        newUserResponse = new NewUserResponse(professionalUser);
        userProfileCreationResponse = new UserProfileCreationResponse();
        userProfileCreationResponse.setIdamId("ACM1QR");
    }

    @Test
    void test_getUserIdentifier() {
        assertThat(newUserResponse.getUserIdentifier()).isEqualTo(userIdentifier.toString());
    }

    @Test
    void test_userIdentifier_with_setter() {
        newUserResponse.setUserIdentifier(userIdentifier.toString());
        newUserResponse.setIdamStatus("ACTIVE");

        assertThat(newUserResponse.getUserIdentifier()).isEqualTo(userIdentifier.toString());
        assertThat(newUserResponse.getIdamStatus()).isEqualTo("ACTIVE");
    }

    @Test
    void test_NewUserResponse_with_constructor() {
        newUserResponse = new NewUserResponse(userProfileCreationResponse);
        assertThat(newUserResponse.getUserIdentifier()).isEqualTo("ACM1QR");
    }
}
