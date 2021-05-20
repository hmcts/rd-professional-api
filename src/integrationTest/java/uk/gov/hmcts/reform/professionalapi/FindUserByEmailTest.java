package uk.gov.hmcts.reform.professionalapi;

import static org.apache.commons.lang.RandomStringUtils.randomAlphabetic;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.professionalapi.controller.response.ProfessionalUsersResponse;
import uk.gov.hmcts.reform.professionalapi.util.AuthorizationEnabledIntegrationTest;


public class FindUserByEmailTest extends AuthorizationEnabledIntegrationTest {

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

        assertThat(professionalUsersResponse.get(USER_IDENTIFIER)).isNotNull();
        assertThat(professionalUsersResponse.get("firstName")).isNotNull();
        assertThat(professionalUsersResponse.get("lastName")).isNotNull();
        assertThat(professionalUsersResponse.get("email")).isNotNull();
        assertThat(((List)professionalUsersResponse.get("roles")).size()).isEqualTo(1);
    }

    @Test
    public void find_user_status_by_user_email_address_for_organisation_status_as_active_with_pui_user_manager() {
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

        String userIdentifierResponse = (String) newUserResponse.get(USER_IDENTIFIER);
        assertThat(userIdentifierResponse).isNotNull();
        Map<String, Object> response = professionalReferenceDataClient.findUserStatusByEmail(userEmail, puiUserManager);

        assertThat(response.get("http_status")).isEqualTo("200 OK");
        assertThat(response.get(USER_IDENTIFIER)).isNotNull();
    }

    @Test
    public void find_user_status_by_user_email_address_for_organisation_status_as_active_with_pui_case_manager() {
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

        String userIdentifierResponse = (String) newUserResponse.get(USER_IDENTIFIER);
        assertThat(userIdentifierResponse).isNotNull();
        Map<String, Object> response = professionalReferenceDataClient.findUserStatusByEmail(userEmail, puiCaseManager);

        assertThat(response.get("http_status")).isEqualTo("200 OK");
        assertThat(response.get(USER_IDENTIFIER)).isNotNull();
    }

    @Test
    public void find_user_status_by_user_email_address_for_organisation_status_as_active_with_pui_finance_manager() {
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

        String userIdentifierResponse = (String) newUserResponse.get(USER_IDENTIFIER);
        assertThat(userIdentifierResponse).isNotNull();
        Map<String, Object> response =
                professionalReferenceDataClient.findUserStatusByEmail(userEmail, puiFinanceManager);

        assertThat(response.get("http_status")).isEqualTo("200 OK");
        assertThat(response.get(USER_IDENTIFIER)).isNotNull();
    }

    @Test
    public void find_usr_status_by_usr_email_address_for_organisation_status_as_active_with_pui_organisation_manager() {
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

        String userIdentifierResponse = (String) newUserResponse.get(USER_IDENTIFIER);
        assertThat(userIdentifierResponse).isNotNull();
        Map<String, Object> response = professionalReferenceDataClient.findUserStatusByEmail(userEmail, puiOrgManager);

        assertThat(response.get("http_status")).isEqualTo("200 OK");
        assertThat(response.get(USER_IDENTIFIER)).isNotNull();
    }

    @Test
    public void find_usr_status_by_usr_email_address_for_org_status_as_active_with_caseworker_publiclaw_courtadmin() {
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

        String userIdentifierResponse = (String) newUserResponse.get(USER_IDENTIFIER);
        assertThat(userIdentifierResponse).isNotNull();
        Map<String, Object> response =
                professionalReferenceDataClient.findUserStatusByEmail(userEmail, "caseworker-publiclaw-courtadmin");

        assertThat(response.get("http_status")).isEqualTo("200 OK");
        assertThat(response.get(USER_IDENTIFIER)).isNotNull();
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

        String userIdentifierResponse = (String) newUserResponse.get(USER_IDENTIFIER);

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

        String userIdentifierResponse = (String) newUserResponse.get(USER_IDENTIFIER);
        Map<String, Object> response = professionalReferenceDataClient.findUserStatusByEmail("@@" + userEmail,
                puiUserManager);

        assertThat(response.get("http_status")).isEqualTo("400");

    }
}
