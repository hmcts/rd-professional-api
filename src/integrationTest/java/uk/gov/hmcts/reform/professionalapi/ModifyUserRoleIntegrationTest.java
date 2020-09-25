package uk.gov.hmcts.reform.professionalapi;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.assertj.core.api.Assertions.assertThat;
import static org.codehaus.groovy.runtime.InvokerHelper.asList;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.professionalapi.domain.RoleName;
import uk.gov.hmcts.reform.professionalapi.domain.UserProfileUpdatedData;

import uk.gov.hmcts.reform.professionalapi.util.AuthorizationEnabledIntegrationTest;


@Slf4j
public class ModifyUserRoleIntegrationTest extends AuthorizationEnabledIntegrationTest {

    @Test
    public void ac1_modify_roles_of_active_users_for_an_active_organisation_with_prd_admin_role_should_return_200() {

        String organisationIdentifier = createOrganisationRequest();
        updateOrganisation(organisationIdentifier, hmctsAdmin, ACTIVE);
        userProfileCreateUserWireMock(HttpStatus.CREATED);
        List<String> userRoles = new ArrayList<>();
        userRoles.add(puiCaseManager);

        userProfileCreateUserWireMock(HttpStatus.CREATED);

        updateUserProfileRolesMock(HttpStatus.OK);

        Map<String, Object> newUserResponse =
                professionalReferenceDataClient.addUserToOrganisation(organisationIdentifier,
                        inviteUserCreationRequest(randomAlphabetic(5) + "@email.com", userRoles),
                        hmctsAdmin);

        String userIdentifier = (String) newUserResponse.get("userIdentifier");
        UserProfileUpdatedData userProfileUpdatedData = createModifyUserProfileData();

        Map<String, Object> response = professionalReferenceDataClient
                .modifyUserRolesOfOrganisation(userProfileUpdatedData, organisationIdentifier, userIdentifier,
                        hmctsAdmin);
        assertThat(response.get("http_status")).isNotNull();
        assertThat(response.get("http_status")).isEqualTo("200 OK");

    }

    @Test
    public void ac3_modify_roles_of_active_users_for_an_with_prd_admin_role_should_return_400() {

        String organisationIdentifier = createOrganisationRequest();
        updateOrganisation(organisationIdentifier, hmctsAdmin, ACTIVE);
        userProfileCreateUserWireMock(HttpStatus.CREATED);
        List<String> userRoles = new ArrayList<>();
        userRoles.add(puiCaseManager);

        userProfileCreateUserWireMock(HttpStatus.CREATED);

        updateUserProfileRolesMock(HttpStatus.BAD_REQUEST);

        Map<String, Object> newUserResponse =
                professionalReferenceDataClient.addUserToOrganisation(organisationIdentifier,
                        inviteUserCreationRequest(randomAlphabetic(5) + "@email.com", userRoles),
                        hmctsAdmin);

        String userIdentifier = (String) newUserResponse.get("userIdentifier");

        UserProfileUpdatedData userProfileUpdatedData = new UserProfileUpdatedData();
        RoleName roleName1 = new RoleName(" ");
        Set<RoleName> rolesAdd = new HashSet<>();
        rolesAdd.add(roleName1);
        userProfileUpdatedData.setRolesAdd(rolesAdd);
        Map<String, Object> response = professionalReferenceDataClient
                .modifyUserRolesOfOrganisation(userProfileUpdatedData, organisationIdentifier, userIdentifier,
                        hmctsAdmin);

        assertThat(response.get("http_status")).isEqualTo("400");
        assertThat(response.get("response_body"))
                .isEqualTo("{\"errorMessage\":\"400\",\"errorDescription\":\"BAD REQUEST\",\"timeStamp\":\"23:10\"}");

    }

    @Test
    public void ac4_modify_roles_of_active_users_with_other_role_should_return_403() {

        updateUserProfileRolesMock(HttpStatus.OK);
        UserProfileUpdatedData userProfileUpdatedData = createModifyUserProfileData();
        String userIdentifier = settingUpOrganisation(puiUserManager);
        Map<String, Object> response = professionalReferenceDataClient
                .modifyUserRolesOfOrganisationExternal(userProfileUpdatedData, userIdentifier, puiCaseManager);

        assertThat(response.get("http_status")).isEqualTo("403");

    }

    @Test
    public void ac5_modify_roles_of_active_users_for_an_active_organisation_with_pui_user_mgr_role_should_rtn_200() {

        updateUserProfileRolesMock(HttpStatus.OK);
        UserProfileUpdatedData userProfileUpdatedData = createModifyUserProfileData();
        String userIdentifier = settingUpOrganisation(puiUserManager);
        Map<String, Object> response = professionalReferenceDataClient
                .modifyUserRolesOfOrganisationExternal(userProfileUpdatedData, userIdentifier, puiUserManager);
        assertThat(response.get("http_status")).isNotNull();
        assertThat(response.get("http_status")).isEqualTo("200 OK");
    }

    //TODO review validation with biz requirements
    //@Test
    public void ac6_modify_roles_of_active_users_for_with_pui_user_manager_role_should_return_400_for_bad_request() {

        updateUserProfileRolesMock(HttpStatus.BAD_REQUEST);
        UserProfileUpdatedData userProfileUpdatedData = new UserProfileUpdatedData();
        RoleName roleName1 = new RoleName(" ");
        Set<RoleName> roles = new HashSet<>();
        roles.add(roleName1);

        userProfileUpdatedData.setRolesAdd(roles);
        String userIdentifier = settingUpOrganisation("pui-user-manager");
        Map<String, Object> response = professionalReferenceDataClient
                .modifyUserRolesOfOrganisationExternal(userProfileUpdatedData, userIdentifier, puiUserManager);
        assertThat(response.get("http_status")).isEqualTo("400");
        assertThat(response.get("response_body")).isNotNull();

    }

    @Test
    public void ac8_modify_roles_of_active_users_for_with_pui_user_manager_role_should_rtn_500_for_Internal_server() {

        updateUserProfileRolesMock(HttpStatus.INTERNAL_SERVER_ERROR);
        UserProfileUpdatedData userProfileUpdatedData = createModifyUserProfileData();
        String userIdentifier = settingUpOrganisation("pui-user-manager");
        Map<String, Object> response = professionalReferenceDataClient
                .modifyUserRolesOfOrganisationExternal(userProfileUpdatedData, userIdentifier, puiUserManager);

        verifyDeleteRolesResponse(response);
    }

    @Test
    public void ac9_modify_roles_with_prd_admin_role_should_return_500_internal_server_error() {

        String organisationIdentifier = createOrganisationRequest();
        updateOrganisation(organisationIdentifier, hmctsAdmin, ACTIVE);
        userProfileCreateUserWireMock(HttpStatus.CREATED);
        List<String> userRoles = new ArrayList<>();
        userRoles.add("pui-case-manager");

        userProfileCreateUserWireMock(HttpStatus.CREATED);

        updateUserProfileRolesMock(HttpStatus.INTERNAL_SERVER_ERROR);

        Map<String, Object> newUserResponse =
                professionalReferenceDataClient.addUserToOrganisation(organisationIdentifier,
                        inviteUserCreationRequest(randomAlphabetic(5) + "@email.com", userRoles),
                        hmctsAdmin);

        String userIdentifier = (String) newUserResponse.get("userIdentifier");

        UserProfileUpdatedData userProfileUpdatedData = createModifyUserProfileData();

        Map<String, Object> response = professionalReferenceDataClient
                .modifyUserRolesOfOrganisation(userProfileUpdatedData, organisationIdentifier, userIdentifier,
                        hmctsAdmin);

        verifyDeleteRolesResponse(response);

    }

    @Test
    public void ac10_modify_roles_of_active_users_for_an_organisation_with_invalid_org_id_should_return_400() {
        String organisationIdentifier = createOrganisationRequest();
        updateOrganisation(organisationIdentifier, hmctsAdmin, ACTIVE);
        userProfileCreateUserWireMock(HttpStatus.CREATED);

        userProfileCreateUserWireMock(HttpStatus.CREATED);
        updateUserProfileRolesMock(HttpStatus.OK);

        Map<String, Object> newUserResponse = professionalReferenceDataClient
                .addUserToOrganisation(organisationIdentifier,
                        inviteUserCreationRequest(randomAlphabetic(5) + "@email.com",
                                asList(puiCaseManager)), hmctsAdmin);
        String userIdentifier = (String) newUserResponse.get("userIdentifier");

        Map<String, Object> response = professionalReferenceDataClient
                .modifyUserRolesOfOrganisation(createModifyUserProfileData(), "%7C", userIdentifier, hmctsAdmin);
        assertThat(response.get("http_status")).isNotNull();
        assertThat(response.get("http_status")).isEqualTo("400");

        Map<String, Object> response1 = professionalReferenceDataClient
                .modifyUserRolesOfOrganisation(createModifyUserProfileData(), "$nvalid-org-id", userIdentifier,
                        hmctsAdmin);
        assertThat(response1.get("http_status")).isNotNull();
        assertThat(response1.get("http_status")).isEqualTo("400");


        Map<String, Object> response2 = professionalReferenceDataClient
                .modifyUserRolesOfOrganisation(createModifyUserProfileData(), ",nvalid-org-id", userIdentifier,
                        hmctsAdmin);
        assertThat(response2.get("http_status")).isNotNull();
        assertThat(response2.get("http_status")).isEqualTo("400");
    }

    private UserProfileUpdatedData createModifyUserProfileData() {

        UserProfileUpdatedData userProfileUpdatedData = new UserProfileUpdatedData();
        RoleName roleName1 = new RoleName(puiCaseManager);
        RoleName roleName2 = new RoleName(puiOrgManager);
        Set<RoleName> roles = new HashSet<>();
        roles.add(roleName1);
        roles.add(roleName2);

        userProfileUpdatedData.setRolesAdd(roles);
        return userProfileUpdatedData;
    }

    private void verifyDeleteRolesResponse(Map<String, Object> response) {

        assertThat(response.get("roleAdditionResponse")).isNotNull();
        Map<String, Object>  addRolesResponse =  (Map<String, Object>)response.get("roleAdditionResponse");

        assertThat(addRolesResponse.get("idamStatusCode")).isEqualTo("500");
        assertThat(addRolesResponse.get("idamMessage")).isEqualTo("Internal Server Error");
    }
}