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
import uk.gov.hmcts.reform.professionalapi.domain.ModifyUserProfileData;
import uk.gov.hmcts.reform.professionalapi.domain.RoleName;
import uk.gov.hmcts.reform.professionalapi.util.AuthorizationEnabledIntegrationTest;
import uk.gov.hmcts.reform.professionalapi.utils.OrganisationFixtures;

@Slf4j
public class ModifyUserRoleIntegrationTest extends AuthorizationEnabledIntegrationTest {

    private String settingUpOrganisation(String role) {
        userProfileCreateUserWireMock(HttpStatus.CREATED);
        String organisationIdentifier = createOrganisationRequest();
        updateOrganisation(organisationIdentifier, hmctsAdmin, "ACTIVE");

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
    public void ac1_modify_roles_of_active_users_for_an_active_organisation_with_prd_admin_role_should_return_200() {

        String organisationIdentifier = createOrganisationRequest();
        updateOrganisation(organisationIdentifier, hmctsAdmin, "ACTIVE");
        userProfileCreateUserWireMock(HttpStatus.CREATED);
        List<String> userRoles = new ArrayList<>();
        userRoles.add("pui-case-manager");

        userProfileCreateUserWireMock(HttpStatus.CREATED);

        updateUserProfileRolesMock(HttpStatus.OK);

        ModifyUserProfileData modifyUserProfileData = new ModifyUserProfileData();

        RoleName roleName1 = new RoleName("pui-case-manager");
        RoleName roleName2 = new RoleName("pui-case-organisation");
        Set<RoleName> roles = new HashSet<>();
        roles.add(roleName1);
        roles.add(roleName2);

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

        modifyUserProfileData.setRolesAdd(roles);

        Map<String, Object> response = professionalReferenceDataClient.modifyUserRolesOfOrganisation(modifyUserProfileData, organisationIdentifier, userIdentifier, hmctsAdmin);

        assertThat(response.get("statusCode")).isNotNull();
        assertThat(response.get("statusCode")).isEqualTo(200);
        assertThat(response.get("statusMessage")).isEqualTo("success");
    }

    @Test
    public void ac3_modify_roles_of_active_users_for_an_with_prd_admin_role_should_return_400() {

        String organisationIdentifier = createOrganisationRequest();
        updateOrganisation(organisationIdentifier, hmctsAdmin, "ACTIVE");
        userProfileCreateUserWireMock(HttpStatus.CREATED);
        List<String> userRoles = new ArrayList<>();
        userRoles.add("pui-case-manager");

        userProfileCreateUserWireMock(HttpStatus.CREATED);

        updateUserProfileRolesMock(HttpStatus.BAD_REQUEST);

        ModifyUserProfileData modifyUserProfileData = new ModifyUserProfileData();

        RoleName roleName1 = new RoleName("pui-case-manager");
        RoleName roleName2 = new RoleName("pui-case-organisation");
        Set<RoleName> roles = new HashSet<>();
        roles.add(roleName1);
        roles.add(roleName2);

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

        modifyUserProfileData.setRolesAdd(roles);

        Map<String, Object> response = professionalReferenceDataClient.modifyUserRolesOfOrganisation(modifyUserProfileData, organisationIdentifier, userIdentifier, hmctsAdmin);

        assertThat(response.get("http_status")).isEqualTo("400");
        assertThat(response.get("response_body")).isNotNull();
    }

    @Test
    public void ac9_modify_roles_with_prd_admin_role_should_return_500_internal_server_error() {

        String organisationIdentifier = createOrganisationRequest();
        updateOrganisation(organisationIdentifier, hmctsAdmin, "ACTIVE");
        userProfileCreateUserWireMock(HttpStatus.CREATED);
        List<String> userRoles = new ArrayList<>();
        userRoles.add("pui-case-manager");

        userProfileCreateUserWireMock(HttpStatus.CREATED);

        updateUserProfileRolesMock(HttpStatus.INTERNAL_SERVER_ERROR);

        ModifyUserProfileData modifyUserProfileData = new ModifyUserProfileData();

        RoleName roleName1 = new RoleName("pui-case-manager");
        RoleName roleName2 = new RoleName("pui-case-organisation");
        Set<RoleName> roles = new HashSet<>();
        roles.add(roleName1);
        roles.add(roleName2);

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

        modifyUserProfileData.setRolesAdd(roles);

        Map<String, Object> response = professionalReferenceDataClient.modifyUserRolesOfOrganisation(modifyUserProfileData, organisationIdentifier, userIdentifier, hmctsAdmin);

        assertThat(response.get("http_status")).isEqualTo("500");
        assertThat(response.get("response_body")).isNotNull();
    }

    @Test
    public void ac5_modify_roles_of_active_users_for_an_active_organisation_with_pui_user_manager_role_should_return_200() {

        updateUserProfileRolesMock(HttpStatus.OK);
        ModifyUserProfileData modifyUserProfileData = new ModifyUserProfileData();

        RoleName roleName1 = new RoleName("pui-case-manager");
        RoleName roleName2 = new RoleName("pui-organisation-manager");
        Set<RoleName> roles = new HashSet<>();
        roles.add(roleName1);
        roles.add(roleName2);

        modifyUserProfileData.setRolesAdd(roles);
        String userIdentifier = settingUpOrganisation("pui-user-manager");
        Map<String, Object> response = professionalReferenceDataClient.modifyUserRolesOfOrganisationExternal(modifyUserProfileData, userIdentifier, puiUserManager);

        assertThat(response.get("statusCode")).isNotNull();
        assertThat(response.get("statusMessage")).isEqualTo("success");
    }

    @Test
    public void ac6_modify_roles_of_active_users_for_with_pui_user_manager_role_should_return_400_for_bad_request() {

        updateUserProfileRolesMock(HttpStatus.BAD_REQUEST);
        ModifyUserProfileData modifyUserProfileData = new ModifyUserProfileData();

        RoleName roleName1 = new RoleName("pui-case-manager");
        RoleName roleName2 = new RoleName("pui-organisation-manager");
        Set<RoleName> roles = new HashSet<>();
        roles.add(roleName1);
        roles.add(roleName2);

        modifyUserProfileData.setRolesAdd(roles);
        String userIdentifier = settingUpOrganisation("pui-user-manager");
        Map<String, Object> response = professionalReferenceDataClient.modifyUserRolesOfOrganisationExternal(modifyUserProfileData, userIdentifier, puiUserManager);
        assertThat(response.get("http_status")).isEqualTo("400");
        assertThat(response.get("response_body")).isNotNull();

    }

    @Test
    public void ac8_modify_roles_of_active_users_for_with_pui_user_manager_role_should_return_500_for_Internal_server() {

        updateUserProfileRolesMock(HttpStatus.INTERNAL_SERVER_ERROR);
        ModifyUserProfileData modifyUserProfileData = new ModifyUserProfileData();

        RoleName roleName1 = new RoleName("pui-case-manager");
        RoleName roleName2 = new RoleName("pui-organisation-manager");
        Set<RoleName> roles = new HashSet<>();
        roles.add(roleName1);
        roles.add(roleName2);

        modifyUserProfileData.setRolesAdd(roles);
        String userIdentifier = settingUpOrganisation("pui-user-manager");
        Map<String, Object> response = professionalReferenceDataClient.modifyUserRolesOfOrganisationExternal(modifyUserProfileData, userIdentifier, puiUserManager);

        assertThat(response.get("http_status")).isEqualTo("500");
        assertThat(response.get("response_body")).isNotNull();
    }
}