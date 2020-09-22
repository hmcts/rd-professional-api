package uk.gov.hmcts.reform.professionalapi;

import static org.apache.commons.lang.RandomStringUtils.randomAlphabetic;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.professionalapi.controller.response.ProfessionalUsersResponse;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;
import uk.gov.hmcts.reform.professionalapi.util.AuthorizationEnabledIntegrationTest;


public class FindUserByEmailTest extends AuthorizationEnabledIntegrationTest {

    @Before
    public void setUp() {
        Organisation organisation = new Organisation("some-org-name", null, "PENDING",
                null, null, null);
        ProfessionalUser superUser = new ProfessionalUser("some-fname", "some-lname",
                "soMeone@somewhere.com", organisation);
        organisationRepository.save(organisation);
        professionalUserRepository.save(superUser);
    }


    @Test
    public void search_returns_valid_user_with_organisation_status_as_active() {
        userProfileCreateUserWireMock(HttpStatus.CREATED);
        String organisationIdentifier = createOrganisationRequest();
        updateOrganisation(organisationIdentifier, hmctsAdmin, "ACTIVE");

        userProfileCreateUserWireMock(HttpStatus.CREATED);

        Map<String, Object> response = professionalReferenceDataClient.findUsersByOrganisation(organisationIdentifier,
                "True", hmctsAdmin);

        assertThat(response.get("http_status")).isEqualTo("200 OK");
        assertThat(((List<ProfessionalUsersResponse>) response.get("users")).size()).isGreaterThan(0);

        List<HashMap> professionalUsersResponses = (List<HashMap>) response.get("users");
        HashMap professionalUsersResponse = professionalUsersResponses.get(0);

        assertThat(professionalUsersResponse.get("userIdentifier")).isNotNull();
        assertThat(professionalUsersResponse.get("firstName")).isNotNull();
        assertThat(professionalUsersResponse.get("lastName")).isNotNull();
        assertThat(professionalUsersResponse.get("email")).isNotNull();
        assertThat(((List)professionalUsersResponse.get("roles")).size()).isEqualTo(1);
    }

    @Test
    public void returns_404_when_email_not_found() {
        Map<String, Object> response =
                professionalReferenceDataClient.findUserByEmail("someone@nowhere.com", hmctsAdmin);

        assertThat(response.get("http_status")).isEqualTo("404");
    }


    @Test
    public void returns_404_when_organisation_status_is_not_active() {
        Map<String, Object> response =
                professionalReferenceDataClient.findUserByEmail("someone@somewhere.com", hmctsAdmin);

        assertThat(response.get("http_status")).isEqualTo("404");
    }

    @Test
    public void return_user_by_email_regardless_of_case() {
        userProfileCreateUserWireMock(HttpStatus.CREATED);
        String organisationIdentifier = createOrganisationRequest();
        updateOrganisation(organisationIdentifier, hmctsAdmin, "ACTIVE");

        userProfileCreateUserWireMock(HttpStatus.CREATED);

        Map<String, Object> response =
                professionalReferenceDataClient.findUserByEmail("SOMEONE@SOMEWHERE.COM", hmctsAdmin);

        assertThat(response.get("http_status")).isEqualTo("404");
    }

    @Test
    public void find_user_status_by_user_email_address_for_organisation_status_as_active() {
        userProfileCreateUserWireMock(HttpStatus.CREATED);
        String organisationIdentifier = createOrganisationRequest();
        updateOrganisation(organisationIdentifier, hmctsAdmin, "ACTIVE");

        List<String> userRoles = new ArrayList<>();
        userRoles.add("pui-finance-manager");
        String userEmail = randomAlphabetic(5).toLowerCase() + "@hotmail.com";
        userProfileCreateUserWireMock(HttpStatus.CREATED);

        String userIdentifier = retrieveSuperUserIdFromOrganisationId(organisationIdentifier);

        Map<String, Object> newUserResponse =
                professionalReferenceDataClient.addUserToOrganisationWithUserId(organisationIdentifier,
                        inviteUserCreationRequest(userEmail, userRoles), hmctsAdmin, userIdentifier);

        String userIdentifierResponse = (String) newUserResponse.get("userIdentifier");
        assertThat(userIdentifierResponse).isNotNull();
        Map<String, Object> response = professionalReferenceDataClient.findUserStatusByEmail(userEmail, puiUserManager);

        assertThat(response.get("http_status")).isEqualTo("200 OK");
        assertThat(response.get("userIdentifier")).isNotNull();

    }


    @Test
    public void should_throw_403_for_prd_admin_find_user_status_by_user_email_address_for_org_status_as_active() {
        userProfileCreateUserWireMock(HttpStatus.CREATED);
        String organisationIdentifier = createOrganisationRequest();
        updateOrganisation(organisationIdentifier, hmctsAdmin, "ACTIVE");

        List<String> userRoles = new ArrayList<>();
        userRoles.add("pui-finance-manager");
        String userEmail = randomAlphabetic(5).toLowerCase() + "@hotmail.com";

        userProfileCreateUserWireMock(HttpStatus.CREATED);
        Map<String, Object> newUserResponse =
                professionalReferenceDataClient.addUserToOrganisation(organisationIdentifier,
                        inviteUserCreationRequest(userEmail, userRoles), hmctsAdmin);

        String userIdentifierResponse = (String) newUserResponse.get("userIdentifier");

        Map<String, Object> response = professionalReferenceDataClient.findUserStatusByEmail(userEmail, hmctsAdmin);

        assertThat(response.get("http_status")).isEqualTo("403");

    }

    @Test
    public void shld_give_bad_request_4_invalid_email_to_find_usr_status_by_usr_email_id_for_org_status_as_active() {
        userProfileCreateUserWireMock(HttpStatus.CREATED);
        String organisationIdentifier = createOrganisationRequest();
        updateOrganisation(organisationIdentifier, hmctsAdmin, "ACTIVE");

        List<String> userRoles = new ArrayList<>();
        userRoles.add("pui-finance-manager");
        String userEmail = randomAlphabetic(5).toLowerCase() + "@hotmail.com";

        userProfileCreateUserWireMock(HttpStatus.CREATED);
        Map<String, Object> newUserResponse =
                professionalReferenceDataClient.addUserToOrganisation(organisationIdentifier,
                        inviteUserCreationRequest(userEmail, userRoles), hmctsAdmin);

        String userIdentifierResponse = (String) newUserResponse.get("userIdentifier");
        Map<String, Object> response = professionalReferenceDataClient.findUserStatusByEmail("@@" + userEmail,
                puiUserManager);

        assertThat(response.get("http_status")).isEqualTo("400");

    }
}
