package uk.gov.hmcts.reform.professionalapi;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.professionalapi.controller.constants.IdamStatus;
import uk.gov.hmcts.reform.professionalapi.controller.request.NewUserCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.response.ProfessionalUsersResponse;
import uk.gov.hmcts.reform.professionalapi.util.AuthorizationEnabledIntegrationTest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.assertj.core.api.Assertions.assertThat;


class FindUsersByOrganisationIntegrationTest extends AuthorizationEnabledIntegrationTest {

    @Test
    void can_retrieve_users_with_showDeleted_true_should_return_status_200() {
        String organisationIdentifier = createOrganisationRequest();
        updateOrganisation(organisationIdentifier, hmctsAdmin, "ACTIVE");
        Map<String, Object> response = professionalReferenceDataClient.findUsersByOrganisation(organisationIdentifier,
                "True", hmctsAdmin);
        validateUsers(response, 3, true);
    }

    @Test
    void can_retrieve_users_with_showDeleted_false_should_return_status_200() {
        String organisationIdentifier = createOrganisationRequest();
        updateOrganisation(organisationIdentifier, hmctsAdmin, "ACTIVE");
        Map<String, Object> response = professionalReferenceDataClient.findUsersByOrganisation(organisationIdentifier,
                "False", hmctsAdmin);
        validateUsers(response, 3, true);
    }

    @Test
    void can_retrieve_users_with_showDeleted_null_should_return_status_200() {
        String organisationIdentifier = createOrganisationRequest();
        updateOrganisation(organisationIdentifier, hmctsAdmin, "ACTIVE");
        Map<String, Object> response = professionalReferenceDataClient.findUsersByOrganisation(organisationIdentifier,
                null, hmctsAdmin);
        validateUsers(response, 3, true);
    }

    @Test
    void can_retrieve_users_with_showDeleted_invalid_should_return_status_200() {
        String organisationIdentifier = createOrganisationRequest();
        updateOrganisation(organisationIdentifier, hmctsAdmin, "ACTIVE");
        Map<String, Object> response = professionalReferenceDataClient.findUsersByOrganisation(organisationIdentifier,
                "INVALID", hmctsAdmin);
        validateUsers(response, 3, true);
    }

    @Test
    void retrieve_users_with_pending_organisation_status_should_return_no_users_and_return_status_404() {
        String organisationIdentifier = createOrganisationRequest();
        Map<String, Object> response = professionalReferenceDataClient
                .findUsersByOrganisation(organisationIdentifier, "True", hmctsAdmin);
        assertThat(response.get("http_status")).isEqualTo("404");
    }

    @Test
    void retrieve_users_with_invalid_organisationIdentifier_should_return_status_400() {
        Map<String, Object> response = professionalReferenceDataClient
                .findUsersByOrganisation("123", "False", hmctsAdmin);
        assertThat(response.get("http_status")).isEqualTo("400");

    }

    @Test
    void retrieve_users_with_non_existing_organisationIdentifier_should_return_status_404() {
        Map<String, Object> response = professionalReferenceDataClient
                .findUsersByOrganisation("A1B2C3D", "False", hmctsAdmin);
        assertThat(response.get("http_status")).isEqualTo("404");
    }

    @Test
    void retrieve_newly_deleted_user_with_showDeleted_true() {
        userProfileCreateUserWireMock(HttpStatus.CREATED);
        String organisationIdentifier = createOrganisationRequest();
        updateOrganisation(organisationIdentifier, hmctsAdmin, "ACTIVE");

        userProfileCreateUserWireMock(HttpStatus.CREATED);

        Map<String, Object> response = professionalReferenceDataClient
                .findUsersByOrganisation(organisationIdentifier, "True", hmctsAdmin);

        assertThat(response.get("http_status")).isEqualTo("200 OK");
        assertThat(((List<ProfessionalUsersResponse>) response.get("users")).size()).isGreaterThan(0);

        List<HashMap> professionalUsersResponses = (List<HashMap>) response.get("users");
        HashMap professionalUsersResponse = professionalUsersResponses.get(2);

        assertThat(professionalUsersResponse.get(USER_IDENTIFIER)).isNotNull();
        assertThat(professionalUsersResponse.get("firstName")).isEqualTo("testFn");
        assertThat(professionalUsersResponse.get("lastName")).isEqualTo("O");
        assertThat(professionalUsersResponse.get("email")).isEqualTo("dummy@email.com");
        assertThat(professionalUsersResponse.get("idamStatus")).isEqualTo("DELETED");
        assertThat(professionalUsersResponse.get("idamStatusCode")).isEqualTo("404");
        assertThat(professionalUsersResponse.get("idamMessage")).isEqualTo("16 Resource not found");
        assertThat(((List) professionalUsersResponse.get("roles")).size()).isEqualTo(0);

        HashMap professionalUsersResponse1 = professionalUsersResponses.get(0);
        assertThat(professionalUsersResponse1.get("idamStatusCode")).isEqualTo("0");
        assertThat(professionalUsersResponse1.get("idamMessage")).isEqualTo("");
    }

    @Test
    void retrieve_active_users_for_an_organisation_with_non_pui_user_manager_role_should_return_200() {
        String id = settingUpOrganisation("pui-case-manager");
        Map<String, Object> response = professionalReferenceDataClient.findAllUsersForOrganisationByStatus(
                "false", "Active", puiCaseManager, id);
        validateUsers(response, 2, true);
    }

    @Test
    void retrieve_active_users_for_an_organisation_with_pui_user_manager_role_should_return_200() {
        String id = settingUpOrganisation("pui-user-manager");
        Map<String, Object> response = professionalReferenceDataClient.findAllUsersForOrganisationByStatus(
                "false", "Active", puiUserManager, id);
        validateUsers(response, 2, true);
    }

    @Test
    void retrieve_deleted_users_for_an_organisation_with_pui_user_manager_role_should_return_400() {
        String id = settingUpOrganisation("pui-user-manager");
        Map<String, Object> response = professionalReferenceDataClient.findAllUsersForOrganisationByStatus(
                "false", "Deleted", puiUserManager, id);
        assertThat(response.get("http_status")).isEqualTo("400");
    }

    @Test
    void retrieve_all_users_for_an_organisation_with_pui_user_manager_role_should_return_200() {
        String id = settingUpOrganisation("pui-user-manager");
        Map<String, Object> response = professionalReferenceDataClient.findAllUsersForOrganisationByStatus(
                "false", "", puiUserManager, id);
        validateUsers(response, 3, true);
    }

    @Test
    void retrieve_all_users_for_an_organisation_with_pui_user_manager_role_with_invalid_status_return_400() {
        String id = settingUpOrganisation("pui-user-manager");
        Map<String, Object> response = professionalReferenceDataClient.findAllUsersForOrganisationByStatus(
                "false", "INVALID", puiUserManager, id);
        assertThat(response.get("http_status")).isEqualTo("400");
    }

    @Test
    void retrieve_all_users_for_an_organisation_with_pagination() {
        String organisationIdentifier = createOrganisationRequest();
        updateOrganisation(organisationIdentifier, hmctsAdmin, "ACTIVE");
        List<String> userRoles = new ArrayList<>();
        userRoles.add("pui-user-manager");

        String userIdentifier = retrieveSuperUserIdFromOrganisationId(organisationIdentifier);

        NewUserCreationRequest userCreationRequest = inviteUserCreationRequest(randomAlphabetic(5)
                + "@email.com", userRoles);
        userProfileCreateUserWireMock(HttpStatus.CREATED);
        professionalReferenceDataClient.addUserToOrganisationWithUserId(organisationIdentifier, userCreationRequest,
                hmctsAdmin, userIdentifier);
        NewUserCreationRequest userCreationRequest1 = inviteUserCreationRequest(randomAlphabetic(6)
                + "@email.com", userRoles);
        userProfileCreateUserWireMock(HttpStatus.CREATED);
        professionalReferenceDataClient.addUserToOrganisationWithUserId(organisationIdentifier, userCreationRequest1,
                hmctsAdmin, userIdentifier);
        NewUserCreationRequest userCreationRequest2 = inviteUserCreationRequest(randomAlphabetic(7)
                + "@email.com", userRoles);
        userProfileCreateUserWireMock(HttpStatus.CREATED);
        professionalReferenceDataClient.addUserToOrganisationWithUserId(organisationIdentifier, userCreationRequest2,
                hmctsAdmin, userIdentifier);
        NewUserCreationRequest userCreationRequest3 = inviteUserCreationRequest(randomAlphabetic(8)
                + "@email.com", userRoles);
        userProfileCreateUserWireMock(HttpStatus.CREATED);
        professionalReferenceDataClient.addUserToOrganisationWithUserId(organisationIdentifier, userCreationRequest3,
                hmctsAdmin, userIdentifier);

        Map<String, Object> response = professionalReferenceDataClient.findUsersByOrganisation(organisationIdentifier,
                "False", hmctsAdmin);

        assertThat(((List<ProfessionalUsersResponse>) response.get("users")).size()).isEqualTo(3);

        Map<String, Object> response2 = professionalReferenceDataClient
                .findUsersByOrganisationWithPaginationInformation(organisationIdentifier, "False",
                        hmctsAdmin);

        assertThat(((List<ProfessionalUsersResponse>) response2.get("users")).size()).isEqualTo(3);
    }

    @Test
    void retrieve_all_users_for_an_organisation_with_userIdentifier() {
        String organisationIdentifier = createOrganisationRequest();
        updateOrganisation(organisationIdentifier, hmctsAdmin, "ACTIVE");
        List<String> userRoles = new ArrayList<>();
        userRoles.add("pui-user-manager");

        String userIdentifier = retrieveSuperUserIdFromOrganisationId(organisationIdentifier);

        NewUserCreationRequest userCreationRequest = inviteUserCreationRequest(randomAlphabetic(5)
                + "@email.com", userRoles);
        userProfileCreateUserWireMock(HttpStatus.CREATED);
        professionalReferenceDataClient.addUserToOrganisationWithUserId(organisationIdentifier, userCreationRequest,
                hmctsAdmin, userIdentifier);
        NewUserCreationRequest userCreationRequest1 = inviteUserCreationRequest(randomAlphabetic(6)
                + "@email.com", userRoles);
        userProfileCreateUserWireMock(HttpStatus.CREATED);
        Map<String, Object> profUser = professionalReferenceDataClient.addUserToOrganisationWithUserId(
                organisationIdentifier, userCreationRequest1, hmctsAdmin, userIdentifier);

        Map<String, Object> response = professionalReferenceDataClient.findUsersByOrganisationAndUserIdentifier(
                organisationIdentifier, hmctsAdmin, profUser.get("userIdentifier").toString());

        assertThat(((List<ProfessionalUsersResponse>) response.get("users")).size()).isGreaterThan(0);

    }

    @Test
    void ac1_find_all_active_users_without_roles_for_an_organisation_should_return_200() {
        String id = settingUpOrganisation("pui-user-manager");
        Map<String, Object> response = professionalReferenceDataClient.findUsersByOrganisationWithReturnRoles(
                "false", puiCaseManager, id);
        validateUsers(response, 2, false);
    }

    @Test
    void ac2_find_all_active_users_with_roles_for_an_organisation_should_return_200() {
        String id = settingUpOrganisation("pui-user-manager");
        Map<String, Object> response = professionalReferenceDataClient.findUsersByOrganisationWithReturnRoles(
                "true", puiCaseManager, id);
        validateUsers(response, 2, true);
    }

    @Test
    void ac3_find_all_active_users_with_no_param_given_for_an_organisation_should_return_200() {
        String id = settingUpOrganisation("pui-user-manager");
        Map<String, Object> response = professionalReferenceDataClient.findUsersByOrganisationWithReturnRoles(
                "", puiCaseManager, id);
        validateUsers(response, 2, true);
    }

    @Test
    void ac4_find_all_active_users_without_appropriate_role_for_an_organisation_should_return_403() {
        String id = settingUpOrganisation("pui-user-manager");
        Map<String, Object> response = professionalReferenceDataClient.findUsersByOrganisationWithReturnRoles(
                "", "caseworker-caa", id);
        assertThat(response.get("http_status")).isEqualTo("403");
    }

    @Test
    void ac7_find_all_active_users_for_an_organisation_with_invalid_param_should_return_400() {
        String id = settingUpOrganisation("pui-user-manager");
        Map<String, Object> response = professionalReferenceDataClient.findUsersByOrganisationWithReturnRoles(
                "infealfnk", puiCaseManager, id);
        assertThat(response.get("http_status")).isEqualTo("400");
    }

    private void validateUsers(Map<String, Object> response, int expectedUserCount, Boolean rolesReturned) {

        assertThat(response.get("http_status")).isEqualTo("200 OK");
        assertThat(response.get(ORG_IDENTIFIER)).isNotNull();
        assertThat(((List<ProfessionalUsersResponse>) response.get("users")).size()).isEqualTo(expectedUserCount);
        List<HashMap> professionalUsersResponses = (List<HashMap>) response.get("users");

        professionalUsersResponses.stream().forEach(user -> {
            assertThat(user.get(USER_IDENTIFIER)).isNotNull();
            assertThat(user.get("firstName")).isNotNull();
            assertThat(user.get("lastName")).isNotNull();
            assertThat(user.get("email")).isNotNull();
            assertThat(user.get("idamStatus")).isNotNull();
            if (rolesReturned) {
                if (user.get("idamStatus").equals(IdamStatus.ACTIVE.toString())) {
                    assertThat(((List) user.get("roles")).size()).isEqualTo(1);
                } else {
                    assertThat(((List) user.get("roles"))).isEmpty();
                }
            }
        });
    }

    @Test
    void can_retrieve_users_when_false_should_return_status_200_without_roles() {
        Map<String, Object> response = professionalReferenceDataClient.findUsersByOrganisation(
                createAndActivateOrganisation(), "True", hmctsAdmin, "false");
        validateUsers(response, 3, false);
    }

    @Test
    void can_retrieve_users_when_true_should_return_status_200_with_roles() {
        Map<String, Object> response = professionalReferenceDataClient.findUsersByOrganisation(
                createAndActivateOrganisation(), "True", hmctsAdmin, "true");
        validateUsers(response, 3, true);
    }

    @Test
    void can_retrieve_users_when_default_should_return_status_200_with_roles() {
        Map<String, Object> response = professionalReferenceDataClient.findUsersByOrganisation(
                createAndActivateOrganisation(), "True", hmctsAdmin, null);
        validateUsers(response, 3, true);
    }

    @Test
    void retrieve_active_users_for_an_organisation_with_invalid_bearer_token_should_return_401() {
        Map<String, Object> response = professionalReferenceDataClient.findUsersByOrganisationWithoutAuthHeaders(
                createAndActivateOrganisation(), "True", null);
        assertThat(response.get("http_status")).isEqualTo("401");
    }

    @Test
    void cannot_retrieve_users_when_invalid_user_roles_should_return_status_403() {
        Map<String, Object> response = professionalReferenceDataClient.findUsersByOrganisation(
                createAndActivateOrganisation(), "True", "InvalidRole", null);
        assertThat(response.get("http_status")).isEqualTo("403");
    }

    @Test
    void can_retrieve_users_when_param_is_invalid_should_return_status_400_with_roles() {
        Map<String, Object> response = professionalReferenceDataClient.findUsersByOrganisation(
                createAndActivateOrganisation(), "True", hmctsAdmin, "thisisinvalid");
        assertThat(response.get("http_status")).isEqualTo("400");
    }

    @Test
    void retrieve_active_users_only_for_an_organisation_with_pui_caa_role_should_return_200() {
        String id = settingUpOrganisation("pui-caa");
        Map<String, Object> response = professionalReferenceDataClient
                .findAllUsersForOrganisationByStatus("false", "Active", puiCaa, id);
        validateUsers(response, 2, true);
    }

    @Test
    void retrieve_pending_users_only_for_an_organisation_with_pui_caa_role_should_return_400() {
        String id = createOrganisationRequest();
        Map<String, Object> response = professionalReferenceDataClient
                .findAllUsersForOrganisationByStatus("false", "PENDING", puiCaa, id);
        assertThat(response.get("http_status")).isEqualTo("400");
    }

    @Test
    void retrieve_users_for_an_organisation_with_system_roles_should_return_404_when_users_are_not_active() {
        Map<String, Object> response = professionalReferenceDataClient
                .findUsersByOrganisation(createOrganisationRequest(), "false", systemUser);
        assertThat(response.get("http_status")).isEqualTo("404");
    }

    @Test
    void retrieve_all_users_for_an_organisation_with_system_role_role_should_return_200() {
        Map<String, Object> response = professionalReferenceDataClient
                .findUsersByOrganisation(createAndActivateOrganisation(), "false", systemUser);
        validateUsers(response, 2, false);
    }

    @Test
    void retrieve_all_users_for_an_organisation_with_invalid_role_role_should_return_403() {
        Map<String, Object> response = professionalReferenceDataClient
                .findUsersByOrganisation(createAndActivateOrganisation(), "false", "invalidRole");
        assertThat(response.get("http_status")).isEqualTo("403");
        assertThat((String) response.get("response_body"))
                .contains("{\"errorMessage\":\"9 : Access Denied\",\"errorDescription\":\"Access Denied\"");
    }

    @Test
    void retrieve_all_users_for_an_organisation_with_userIdentifier_sorted() {
        String organisationIdentifier = createOrganisationRequest();
        updateOrganisation(organisationIdentifier, hmctsAdmin, "ACTIVE");
        List<String> userRoles = new ArrayList<>();
        userRoles.add("pui-user-manager");

        String userIdentifier = retrieveSuperUserIdFromOrganisationId(organisationIdentifier);

        NewUserCreationRequest userCreationRequest = inviteUserCreationRequest(randomAlphabetic(5)
                + "@email.com", "testLastName2", "testFirstName2", userRoles);
        userProfileCreateUserWireMock("testFirstName2", "testLastName2", "dummy@email.com",
                "testFirstName1", "testLastName1", "dummy@email.com", HttpStatus.CREATED);
        professionalReferenceDataClient.addUserToOrganisationWithUserId(organisationIdentifier, userCreationRequest,
                hmctsAdmin, userIdentifier);
        NewUserCreationRequest userCreationRequest1 = inviteUserCreationRequest(randomAlphabetic(6)
                + "@email.com", "testLastName1", "testFirstName1", userRoles);
        userProfileCreateUserWireMock("testFirstName2", "testLastName2", "dummy@email.com",
                "testFirstName1", "testLastName1", "dummy@email.com", HttpStatus.CREATED);
        Map<String, Object> profUser = professionalReferenceDataClient.addUserToOrganisationWithUserId(
                organisationIdentifier, userCreationRequest1, hmctsAdmin, userIdentifier);

        Map<String, Object> response = professionalReferenceDataClient.findUsersByOrganisationAndUserIdentifier(
                organisationIdentifier, hmctsAdmin, profUser.get("userIdentifier").toString());
        assertThat(((List<ProfessionalUsersResponse>) response.get("users")).size()).isEqualTo(3);

        assertThat(response.get("http_status")).isEqualTo("200 OK");
        assertThat(response.get(ORG_IDENTIFIER)).isNotNull();
        List<HashMap> professionalUsersResponses = (List<HashMap>) response.get("users");

        HashMap firstUser = professionalUsersResponses.get(0);
        assertThat(firstUser.get(USER_IDENTIFIER)).isNotNull();
        assertThat(firstUser.get("firstName")).isEqualTo("testFirstName1");
        assertThat(firstUser.get("lastName")).isEqualTo("testLastName1");

        HashMap secondUser = professionalUsersResponses.get(1);
        assertThat(secondUser.get(USER_IDENTIFIER)).isNotNull();
        assertThat(secondUser.get("firstName")).isEqualTo("testFirstName2");
        assertThat(secondUser.get("lastName")).isEqualTo("testLastName2");
    }
}
