package uk.gov.hmcts.reform.professionalapi;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.professionalapi.controller.request.NewUserCreationRequest.aNewUserCreationRequest;

import io.restassured.specification.RequestSpecification;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import lombok.extern.slf4j.Slf4j;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import net.thucydides.core.annotations.WithTag;
import net.thucydides.core.annotations.WithTags;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.professionalapi.controller.constants.IdamStatus;
import uk.gov.hmcts.reform.professionalapi.controller.request.NewUserCreationRequest;
import uk.gov.hmcts.reform.professionalapi.domain.RoleName;
import uk.gov.hmcts.reform.professionalapi.domain.UserProfileUpdatedData;

import uk.gov.hmcts.reform.professionalapi.idam.IdamOpenIdClient;

@RunWith(SpringIntegrationSerenityRunner.class)
@WithTags({@WithTag("testType:Functional")})
@ActiveProfiles("functional")
@Slf4j
public class ModifyRolesForUserTest extends AuthorizationFunctionalTest {


    RequestSpecification bearerTokenForPuiUserManager;
    String orgIdentifierResponse;

    public RequestSpecification generateBearerTokenForPuiManager() {
        Map<String, Object> response = professionalApiClient.createOrganisation();
        orgIdentifierResponse = (String) response.get("organisationIdentifier");
        professionalApiClient.updateOrganisation(orgIdentifierResponse, hmctsAdmin);

        String userEmail = generateRandomEmail();
        String lastName = "someLastName";
        String firstName = "someName";

        bearerTokenForPuiUserManager = professionalApiClient.getMultipleAuthHeadersExternal(puiUserManager,
                firstName, lastName, userEmail);

        List<String> userRoles1 = new ArrayList<>();
        userRoles1.add("pui-organisation-manager");
        NewUserCreationRequest userCreationRequest = aNewUserCreationRequest()
                .firstName(firstName)
                .lastName(lastName)
                .email(userEmail)
                .roles(userRoles1)
                .build();
        professionalApiClient.addNewUserToAnOrganisation(orgIdentifierResponse, hmctsAdmin, userCreationRequest,
                HttpStatus.CREATED);

        return bearerTokenForPuiUserManager;
    }

    @Test
    public void ac1_modify_role_existing_user_to_organisation_internal() {

        Map<String, Object> response = professionalApiClient.createOrganisation();

        String orgIdentifier = (String) response.get("organisationIdentifier");
        assertThat(orgIdentifier).isNotEmpty();

        professionalApiClient.updateOrganisation(orgIdentifier, hmctsAdmin);

        IdamOpenIdClient idamOpenIdClient = new IdamOpenIdClient(configProperties);
        Map<String, String> userCreds = idamOpenIdClient.createUser(addRoles("pui-organisation-manager"));
        NewUserCreationRequest newUserCreationRequest = professionalApiClient
                .createNewUserRequest(userCreds.get(EMAIL));

        assertThat(newUserCreationRequest).isNotNull();

        Map<String, Object> newUserResponse = professionalApiClient.addNewUserToAnOrganisation(orgIdentifier,
                hmctsAdmin, newUserCreationRequest, HttpStatus.CREATED);

        assertThat(newUserResponse).isNotNull();

        Map<String, Object> searchResponse = professionalApiClient
                .searchOrganisationUsersByStatusInternal(orgIdentifier, hmctsAdmin, HttpStatus.OK);
        List<Map> professionalUsersResponses = (List<Map>) searchResponse.get("users");
        Map professionalUsersResponse = getActiveUser(professionalUsersResponses);

        assertThat(professionalUsersResponse.get("idamStatus")).isNotNull();
        assertThat(professionalUsersResponse.get("userIdentifier")).isNotNull();
        String userId = (String) professionalUsersResponse.get("userIdentifier");

        UserProfileUpdatedData userProfileUpdatedData = new UserProfileUpdatedData();
        RoleName role1 = new RoleName("pui-user-manager");
        Set<RoleName> roles = new HashSet<>();
        roles.add(role1);
        userProfileUpdatedData.setRolesAdd(roles);

        Map<String, Object> modifiedUserResponse = professionalApiClient
                .modifyUserToExistingUserForPrdAdmin(HttpStatus.OK, userProfileUpdatedData, orgIdentifier, userId);
        searchResponse = professionalApiClient.searchOrganisationUsersByStatusInternal(orgIdentifier,
                hmctsAdmin, HttpStatus.OK);

        List<Map> professionalUsersResponses1 = (List<Map>) searchResponse.get("users");
        Map professionalUsersResponse1 = getActiveUser(professionalUsersResponses1);

        assertThat(professionalUsersResponse1.get("roles")).isNotNull();

        List<String> rolesSize = (List) professionalUsersResponse1.get("roles");
        assertThat(rolesSize.size()).isEqualTo(3);
        assertThat(rolesSize).contains("caseworker").contains("pui-organisation-manager").contains("pui-user-manager");
    }

    @Test
    public void ac2_add_role_existing_user_using_pui_user_manager_for_external_200() {

        Map<String, Object> searchResponse = professionalApiClient
                .searchOrganisationUsersByStatusExternal(HttpStatus.OK, generateBearerTokenForPuiManager(),
                        "Active");
        List<Map> professionalUsersResponses = (List<Map>) searchResponse.get("users");
        Map professionalUsersResponse = professionalUsersResponses.get(0);
        assertThat(professionalUsersResponse.get("idamStatus")).isNotNull();
        assertThat(professionalUsersResponse.get("userIdentifier")).isNotNull();
        String userId = (String) professionalUsersResponse.get("userIdentifier");

        UserProfileUpdatedData userProfileUpdatedData = new UserProfileUpdatedData();
        RoleName role1 = new RoleName("pui-organisation-manager");
        Set<RoleName> roles = new HashSet<>();
        roles.add(role1);
        userProfileUpdatedData.setRolesAdd(roles);
        professionalApiClient.modifyUserToExistingUserForExternal(HttpStatus.OK,
                userProfileUpdatedData, bearerTokenForPuiUserManager, userId);
        Map<String, Object> searchResponse1 = professionalApiClient
                .searchOrganisationUsersByStatusInternal(orgIdentifierResponse, hmctsAdmin, HttpStatus.OK);
        List<Map> professionalUsersResponses1 = (List<Map>) searchResponse1.get("users");
        Map professionalUsersResponse1 = getActiveUser(professionalUsersResponses1);
        assertThat(professionalUsersResponse1.get("roles")).isNotNull();

        List<String> rolesSize = (List<String>) professionalUsersResponse1.get("roles");
        assertThat(rolesSize.size()).isEqualTo(2);
        assertThat(rolesSize).contains("pui-user-manager").contains("pui-organisation-manager");
    }


    @Test
    public void ac3_delete_role_existing_user_to_organisation_internal() {
        Map<String, Object> response = professionalApiClient.createOrganisation();

        String orgIdentifier = (String) response.get("organisationIdentifier");
        assertThat(orgIdentifier).isNotEmpty();

        professionalApiClient.updateOrganisation(orgIdentifier, hmctsAdmin);

        IdamOpenIdClient idamOpenIdClient = new IdamOpenIdClient(configProperties);
        Map<String, String> userCreds = idamOpenIdClient.createUser(addRoles("pui-organisation-manager"));
        NewUserCreationRequest newUserCreationRequest = professionalApiClient
                .createNewUserRequest(userCreds.get(EMAIL));

        assertThat(newUserCreationRequest).isNotNull();
        // inviting the user
        Map<String, Object> newUserResponse = professionalApiClient.addNewUserToAnOrganisation(orgIdentifier,
                hmctsAdmin, newUserCreationRequest, HttpStatus.CREATED);

        assertThat(newUserResponse).isNotNull();
        // search active user
        Map<String, Object> searchResponse = professionalApiClient
                .searchOrganisationUsersByStatusInternal(orgIdentifier, hmctsAdmin, HttpStatus.OK);
        List<Map> professionalUsersResponses = (List<Map>) searchResponse.get("users");
        Map professionalUsersResponse = getActiveUser(professionalUsersResponses);

        assertThat(professionalUsersResponse.get("idamStatus")).isNotNull();
        assertThat(professionalUsersResponse.get("userIdentifier")).isNotNull();
        String userId = (String) professionalUsersResponse.get("userIdentifier");

        //create add roles object
        UserProfileUpdatedData userProfileUpdatedData = new UserProfileUpdatedData();
        userProfileUpdatedData.setRolesAdd(createAddRoleName());

        Map<String, Object> modifiedUserResponse = professionalApiClient
                .modifyUserToExistingUserForPrdAdmin(HttpStatus.OK, userProfileUpdatedData, orgIdentifier, userId);
        //search active user
        List<String> rolesSize = searchUserInfo(orgIdentifier);
        assertThat(rolesSize.size()).isEqualTo(3);
        assertThat(rolesSize).contains("caseworker").contains("pui-organisation-manager").contains("pui-user-manager");

        UserProfileUpdatedData deleteRoleReqest = new UserProfileUpdatedData();
        deleteRoleReqest.setRolesDelete(createOrDeleteRoleName());
        Map<String, Object> modifiedUserResponseForDelete = professionalApiClient
                .modifyUserToExistingUserForPrdAdmin(HttpStatus.OK, deleteRoleReqest, orgIdentifier, userId);
        //search active user
        List<String> rolesAfterDelete = searchUserInfo(orgIdentifier);
        assertThat(rolesAfterDelete.size()).isEqualTo(2);
        assertThat(rolesSize).contains("pui-organisation-manager").contains("caseworker");

    }


    @Test
    public void ac4_delete_role_existing_user_using_pui_user_manager_for_external_200() {

        Map<String, Object> searchResponse = professionalApiClient
                .searchOrganisationUsersByStatusExternal(HttpStatus.OK, generateBearerTokenForPuiManager(),
                        "Active");
        List<Map> professionalUsersResponses = (List<Map>) searchResponse.get("users");
        Map professionalUsersResponse = professionalUsersResponses.get(0);
        assertThat(professionalUsersResponse.get("idamStatus")).isNotNull();
        assertThat(professionalUsersResponse.get("userIdentifier")).isNotNull();
        String userId = (String) professionalUsersResponse.get("userIdentifier");

        UserProfileUpdatedData userProfileUpdatedData = new UserProfileUpdatedData();
        RoleName role1 = new RoleName("pui-organisation-manager");
        Set<RoleName> roles = new HashSet<>();
        roles.add(role1);
        userProfileUpdatedData.setRolesAdd(roles);

        Map<String, Object> modifiedUserResponse = professionalApiClient
                .modifyUserToExistingUserForExternal(HttpStatus.OK, userProfileUpdatedData,
                        bearerTokenForPuiUserManager, userId);
        //search active user
        List<String> rolesAfterAdd = searchUserInfo(orgIdentifierResponse);
        assertThat(rolesAfterAdd.size()).isEqualTo(2);
        assertThat(rolesAfterAdd).contains("pui-organisation-manager").contains("pui-user-manager");

        // roles to delete
        UserProfileUpdatedData deleteRoleRequest = new UserProfileUpdatedData();
        RoleName roleName = new RoleName("pui-organisation-manager");
        Set<RoleName> rolesDelete = new HashSet<>();
        rolesDelete.add(roleName);
        deleteRoleRequest.setRolesDelete(rolesDelete);

        professionalApiClient.modifyUserToExistingUserForExternal(HttpStatus.OK, deleteRoleRequest,
                bearerTokenForPuiUserManager, userId);

        //search active user
        List<String> rolesInfo = searchUserInfo(orgIdentifierResponse);
        assertThat(rolesInfo.size()).isEqualTo(1);
        //assertThat(!rolesInfo.contains("pui-organisation-manager"));
    }

    @Test
    public void should_get_400_when_modify_roles_for_pending_user_internal() {

        Map<String, Object> createUserResponse = professionalApiClient.addNewUserToAnOrganisation(activeOrgId,
                hmctsAdmin, professionalApiClient.createNewUserRequest(), HttpStatus.CREATED);
        Map<String, Object> modifiedUserResponse = professionalApiClient
                .modifyUserToExistingUserForPrdAdmin(HttpStatus.BAD_REQUEST, getUserProfileAddRoleRequest(),
                        activeOrgId, (String) createUserResponse.get("userIdentifier"));
        assertThat(modifiedUserResponse.get("errorDescription")).isEqualTo("UserId status is not active");
        assertThat(modifiedUserResponse.get("errorMessage"))
                .isEqualTo("3 : There is a problem with your request. Please check and try again");
    }

    @Test
    public void should_get_404_when_modify_roles_for_unknown_user_internal() {
        String unknownUserId = UUID.randomUUID().toString();
        Map<String, Object> modifiedUserResponse = professionalApiClient
                .modifyUserToExistingUserForPrdAdmin(HttpStatus.NOT_FOUND, getUserProfileAddRoleRequest(), activeOrgId,
                        unknownUserId);
        assertThat(modifiedUserResponse.get("errorDescription"))
                .isEqualTo("could not find user profile for userId: or status is not active " + unknownUserId);
        assertThat(modifiedUserResponse.get("errorMessage")).isEqualTo("4 : Resource not found");
    }

    @Test
    public void should_get_403_when_non_active_external_user_modify_roles() {

        //create test sidam user and add same user in org
        IdamOpenIdClient idamOpenIdClient = new IdamOpenIdClient(configProperties);
        Map<String, String> pumUserCreds = idamOpenIdClient.createUser(addRoles(puiUserManager));
        String userId = (String) professionalApiClient.addNewUserToAnOrganisation(activeOrgId, hmctsAdmin,
                professionalApiClient.createNewUserRequest(pumUserCreds.get(EMAIL)),
                HttpStatus.CREATED).get("userIdentifier");
        RequestSpecification bearerToken = professionalApiClient
                .getMultipleAuthHeaders(idamOpenIdClient
                        .getOpenIdToken(pumUserCreds.get(EMAIL), pumUserCreds.get(CREDS)));
        //update status to suspended so that while adding roles by ext user will be non active
        professionalApiClient.modifyUserToExistingUserForPrdAdmin(HttpStatus.OK,
                getUserStatusUpdateRequest(IdamStatus.SUSPENDED), activeOrgId, userId);
        //use external suspended user to add roles should give 403 back
        professionalApiClient.modifyUserToExistingUserForExternal(HttpStatus.INTERNAL_SERVER_ERROR,
                getUserProfileAddRoleRequest(),
                bearerToken, userId);
    }

    @Test
    public void should_get_403_when_external_user_modify_roles_of_non_active_user() {

        IdamOpenIdClient idamOpenIdClient = new IdamOpenIdClient(configProperties);
        Map<String, String> pumUserCreds = idamOpenIdClient.createUser(addRoles(puiUserManager));
        professionalApiClient.addNewUserToAnOrganisation(activeOrgId, hmctsAdmin,
                professionalApiClient.createNewUserRequest(pumUserCreds.get(EMAIL)), HttpStatus.CREATED);

        String pendingUserId = (String) professionalApiClient.addNewUserToAnOrganisation(activeOrgId, hmctsAdmin,
                professionalApiClient.createNewUserRequest(generateRandomEmail()),
                HttpStatus.CREATED).get("userIdentifier");

        Map<String, Object> modifiedUserResponse = professionalApiClient
                .modifyUserToExistingUserForExternal(HttpStatus.FORBIDDEN, getUserProfileAddRoleRequest(),
                        professionalApiClient
                                .getMultipleAuthHeaders(idamOpenIdClient
                                        .getOpenIdToken(pumUserCreds.get(EMAIL), pumUserCreds.get(CREDS))),
                        pendingUserId);
        assertThat(modifiedUserResponse.get("errorMessage")).isEqualTo("9 : Access Denied");
        assertThat(modifiedUserResponse.get("errorDescription"))
                .isEqualTo("User status must be Active to perform this operation");
    }

    public UserProfileUpdatedData getUserProfileAddRoleRequest() {
        UserProfileUpdatedData userProfileUpdatedData = new UserProfileUpdatedData();
        userProfileUpdatedData.setRolesAdd(createAddRoleName());
        return userProfileUpdatedData;
    }

    private Set<RoleName> createOrDeleteRoleName() {

        RoleName roleName = new RoleName("pui-user-manager");
        Set<RoleName> roles = new HashSet<>();
        roles.add(roleName);
        return roles;
    }

    private Set<RoleName> createAddRoleName() {

        RoleName roleName = new RoleName("pui-user-manager");
        Set<RoleName> roles = new HashSet<>();
        roles.add(roleName);
        return roles;
    }

    private List<String> searchUserInfo(String orgIdentifier) {

        Map<String, Object> searchResponse = professionalApiClient
                .searchOrganisationUsersByStatusInternal(orgIdentifier, hmctsAdmin, HttpStatus.OK);
        List<Map> professionalUsersResponses = (List<Map>) searchResponse.get("users");

        List<String> rolesToBeReturned = new ArrayList<>();
        professionalUsersResponses.forEach(user -> {
            if (IdamStatus.ACTIVE.name().equalsIgnoreCase((String) user.get("idamStatus"))) {
                assertThat(user.get("roles")).isNotNull();
                rolesToBeReturned.addAll((Collection<? extends String>) user.get("roles"));
            }
        });
        return rolesToBeReturned;
    }

    private List<String> searchUserInfoExternal(String bearer) {

        Map<String, Object> searchResponse = professionalApiClient
                .searchOrganisationUsersByStatusExternal(HttpStatus.OK,
                        professionalApiClient.getMultipleAuthHeaders(bearer), "Active");
        List<Map> professionalUsersResponses = (List<Map>) searchResponse.get("users");

        List<String> rolesToBeReturned = new ArrayList<>();
        professionalUsersResponses.forEach(user -> {
            if (IdamStatus.ACTIVE.name().equalsIgnoreCase((String) user.get("idamStatus"))) {
                assertThat(user.get("roles")).isNotNull();
                rolesToBeReturned.addAll((Collection<? extends String>) user.get("roles"));
            }
        });
        return rolesToBeReturned;
    }
}
