package uk.gov.hmcts.reform.professionalapi;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.professionalapi.controller.request.NewUserCreationRequest.aNewUserCreationRequest;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.professionalapi.controller.request.NewUserCreationRequest;
import uk.gov.hmcts.reform.professionalapi.domain.RoleName;
import uk.gov.hmcts.reform.professionalapi.domain.UserProfileUpdatedData;
import uk.gov.hmcts.reform.professionalapi.util.AuthorizationEnabledIntegrationTest;
import uk.gov.hmcts.reform.professionalapi.utils.OrganisationFixtures;


@Slf4j
public class DeleteUserRoleIntegrationTest extends AuthorizationEnabledIntegrationTest {

    private String settingUpOrganisation(String role) {
        userProfileCreateUserWireMock(HttpStatus.CREATED);
        String organisationIdentifier = createOrganisationRequest();
        updateOrganisation(organisationIdentifier, hmctsAdmin, ACTIVE);

        List<String> userRoles = new ArrayList<>();
        userRoles.add(role);
        NewUserCreationRequest userCreationRequest = aNewUserCreationRequest()
                .firstName("someName")
                .lastName("someLastName")
                .email(randomAlphabetic(5) + "@email.com")
                .roles(userRoles)
                .jurisdictions(OrganisationFixtures.createJurisdictions())
                .build();

        userProfileCreateUserWireMock(HttpStatus.CREATED);
        Map<String, Object> newUserResponse =
                professionalReferenceDataClient.addUserToOrganisation(organisationIdentifier, userCreationRequest, hmctsAdmin);

        return ((String) newUserResponse.get("userIdentifier"));
    }

    @Test
    public void ac1_modify_roles_of_active_users_and_delete_for_an_active_organisation_with_prd_admin_role_should_return_200() {

        String organisationIdentifier = createOrganisationRequest();
        updateOrganisation(organisationIdentifier, hmctsAdmin, ACTIVE);
        userProfileCreateUserWireMock(HttpStatus.CREATED);
        List<String> userRoles = new ArrayList<>();
        userRoles.add(puiCaseManager);

        userProfileCreateUserWireMock(HttpStatus.CREATED);

        updateUserProfileRolesMock(HttpStatus.OK);

        NewUserCreationRequest userCreationRequest = aNewUserCreationRequest()
                .firstName("someName")
                .lastName("someLastName")
                .email(randomAlphabetic(5) + "@email.com")
                .roles(userRoles)
                .jurisdictions(OrganisationFixtures.createJurisdictions())
                .build();

        Map<String, Object> newUserResponse =
                professionalReferenceDataClient.addUserToOrganisation(organisationIdentifier, userCreationRequest, hmctsAdmin);

        String userIdentifier = (String) newUserResponse.get("userIdentifier");
        UserProfileUpdatedData userProfileUpdatedData = createAddRolesUserProfileData();

        Map<String, Object> response = professionalReferenceDataClient.modifyUserRolesOfOrganisation(userProfileUpdatedData, organisationIdentifier, userIdentifier, hmctsAdmin);

        assertThat(response.get("http_status")).isNotNull();
        assertThat(response.get("http_status")).isEqualTo("200 OK");

        UserProfileUpdatedData deleteUserProfileData = createDeleteRolesUserProfileData();
        Map<String, Object> responseForDelete = professionalReferenceDataClient.modifyUserRolesOfOrganisation(deleteUserProfileData, organisationIdentifier, userIdentifier, hmctsAdmin);

        assertThat(response.get("http_status")).isNotNull();
        assertThat(response.get("http_status")).isEqualTo("200 OK");

    }

    //TODO review validation with biz requirements
    //@Test
    public void ac3_delete_roles_of_active_users_for_an_with_prd_admin_role_should_return_400() {

        String organisationIdentifier = createOrganisationRequest();
        updateOrganisation(organisationIdentifier, hmctsAdmin, ACTIVE);
        userProfileCreateUserWireMock(HttpStatus.CREATED);
        List<String> userRoles = new ArrayList<>();
        userRoles.add(puiCaseManager);

        userProfileCreateUserWireMock(HttpStatus.CREATED);

        updateUserProfileRolesMock(HttpStatus.BAD_REQUEST);

        NewUserCreationRequest userCreationRequest = aNewUserCreationRequest()
                .firstName("someName")
                .lastName("someLastName")
                .email(randomAlphabetic(5) + "@email.com")
                .roles(userRoles)
                .jurisdictions(OrganisationFixtures.createJurisdictions())
                .build();

        Map<String, Object> newUserResponse =
                professionalReferenceDataClient.addUserToOrganisation(organisationIdentifier, userCreationRequest, hmctsAdmin);

        String userIdentifier = (String) newUserResponse.get("userIdentifier");

        UserProfileUpdatedData userProfileUpdatedData = new UserProfileUpdatedData();
        RoleName roleName1 = new RoleName(" ");
        Set<RoleName> rolesDelete = new HashSet<>();
        rolesDelete.add(roleName1);
        userProfileUpdatedData.setRolesDelete(rolesDelete);

        Map<String, Object> response = professionalReferenceDataClient.modifyUserRolesOfOrganisation(userProfileUpdatedData, organisationIdentifier, userIdentifier, hmctsAdmin);

        assertThat(response.get("http_status")).isEqualTo("400");
        assertThat(response.get("response_body")).isNotNull();
    }

    @Test
    public void ac4_modify_roles_of_active_users_with_other_role_should_return_403() {

        updateUserProfileRolesMock(HttpStatus.OK);
        UserProfileUpdatedData userProfileUpdatedData = createAddRolesUserProfileData();
        String userIdentifier = settingUpOrganisation(puiUserManager);
        Map<String, Object> response = professionalReferenceDataClient.modifyUserRolesOfOrganisationExternal(userProfileUpdatedData, userIdentifier, puiCaseManager);

        assertThat(response.get("http_status")).isEqualTo("403");
        assertThat(response.get("response_body")).isNotNull();
    }

    @Test
    public void ac5_add_and_delete_roles_of_active_users_for_an_active_organisation_with_pui_user_manager_role_should_return_200() {

        updateUserProfileRolesMock(HttpStatus.OK);
        UserProfileUpdatedData userProfileUpdatedData = createAddRolesUserProfileData();
        String userIdentifier = settingUpOrganisation(puiUserManager);
        Map<String, Object> response = professionalReferenceDataClient.modifyUserRolesOfOrganisationExternal(userProfileUpdatedData, userIdentifier, puiUserManager);

        assertThat(response.get("http_status")).isNotNull();
        assertThat(response.get("http_status")).isEqualTo("200 OK");

        UserProfileUpdatedData deleteUserProfileData = createDeleteRolesUserProfileData();
        Map<String, Object> responseForDelete = professionalReferenceDataClient.modifyUserRolesOfOrganisationExternal(deleteUserProfileData, userIdentifier, puiUserManager);

        log.info("responseForDelete :: " + responseForDelete);
        assertThat(response.get("http_status")).isNotNull();
        assertThat(response.get("http_status")).isEqualTo("200 OK");

    }

    //TODO review validation with biz requirements
    //@Test
    public void ac6_delete_roles_of_active_users_for_with_pui_user_manager_role_should_return_400_for_bad_request() {

        updateUserProfileRolesMock(HttpStatus.BAD_REQUEST);
        UserProfileUpdatedData userProfileUpdatedData = new UserProfileUpdatedData();
        RoleName roleName1 = new RoleName(" ");
        Set<RoleName> deleteRoles = new HashSet<>();
        deleteRoles.add(roleName1);
        userProfileUpdatedData.setRolesDelete(deleteRoles);
        String userIdentifier = settingUpOrganisation("pui-user-manager");
        Map<String, Object> response = professionalReferenceDataClient.modifyUserRolesOfOrganisationExternal(userProfileUpdatedData, userIdentifier, puiUserManager);
        assertThat(response.get("http_status")).isEqualTo("400");
        assertThat(response.get("response_body")).isNotNull();

    }

    @Test
    public void ac8_delete_roles_of_active_users_for_with_pui_user_manager_role_should_return_500_for_Internal_server() {

        updateUserProfileRolesMock(HttpStatus.INTERNAL_SERVER_ERROR);
        UserProfileUpdatedData userProfileUpdatedData = createDeleteRolesUserProfileData();
        String userIdentifier = settingUpOrganisation("pui-user-manager");
        Map<String, Object> response = professionalReferenceDataClient.modifyUserRolesOfOrganisationExternal(userProfileUpdatedData, userIdentifier, puiUserManager);

        log.info("response :: " + response);
        verifyDeleteRolesResponse(response);

    }

    @Test
    public void ac9_delete_roles_with_prd_admin_role_should_return_500_internal_server_error() {

        String organisationIdentifier = createOrganisationRequest();
        updateOrganisation(organisationIdentifier, hmctsAdmin, ACTIVE);
        userProfileCreateUserWireMock(HttpStatus.CREATED);
        List<String> userRoles = new ArrayList<>();
        userRoles.add("pui-case-manager");

        userProfileCreateUserWireMock(HttpStatus.CREATED);

        updateUserProfileRolesMock(HttpStatus.INTERNAL_SERVER_ERROR);

        NewUserCreationRequest userCreationRequest = aNewUserCreationRequest()
                .firstName("someName")
                .lastName("someLastName")
                .email(randomAlphabetic(5) + "@email.com")
                .roles(userRoles)
                .jurisdictions(OrganisationFixtures.createJurisdictions())
                .build();

        Map<String, Object> newUserResponse =
                professionalReferenceDataClient.addUserToOrganisation(organisationIdentifier, userCreationRequest, hmctsAdmin);

        String userIdentifier = (String) newUserResponse.get("userIdentifier");

        UserProfileUpdatedData userProfileUpdatedData = createDeleteRolesUserProfileData();

        Map<String, Object> response = professionalReferenceDataClient.modifyUserRolesOfOrganisation(userProfileUpdatedData, organisationIdentifier, userIdentifier, hmctsAdmin);

        verifyDeleteRolesResponse(response);

    }

    private UserProfileUpdatedData createAddRolesUserProfileData() {

        UserProfileUpdatedData userProfileUpdatedData = new UserProfileUpdatedData();
        RoleName roleName1 = new RoleName(puiCaseManager);
        RoleName roleName2 = new RoleName(puiOrgManager);
        Set<RoleName> roles = new HashSet<>();
        roles.add(roleName1);
        roles.add(roleName2);

        userProfileUpdatedData.setRolesAdd(roles);
        return userProfileUpdatedData;
    }

    private UserProfileUpdatedData createDeleteRolesUserProfileData() {

        UserProfileUpdatedData userProfileUpdatedData = new UserProfileUpdatedData();
        RoleName roleName1 = new RoleName(puiOrgManager);
        Set<RoleName> roles = new HashSet<>();
        roles.add(roleName1);
        userProfileUpdatedData.setRolesDelete(roles);
        return userProfileUpdatedData;
    }

    private void verifyDeleteRolesResponse(Map<String, Object> response) {

        assertThat(response.get("roleDeletionResponse")).isNotNull();
        List<Map<String, Object>> deleteRolesResponse = (List<Map<String, Object>>) response.get("roleDeletionResponse");
        Map<String, Object>  deleteRoleResponse = deleteRolesResponse.get(0);
        assertThat(deleteRoleResponse.get("idamStatusCode")).isEqualTo("500");
        assertThat(deleteRoleResponse.get("idamMessage")).isEqualTo("Internal Server Error");
    }
}