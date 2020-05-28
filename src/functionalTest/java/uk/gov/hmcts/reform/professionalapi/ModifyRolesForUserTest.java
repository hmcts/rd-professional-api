package uk.gov.hmcts.reform.professionalapi;

import static org.apache.commons.lang.RandomStringUtils.randomAlphabetic;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.professionalapi.controller.request.NewUserCreationRequest.aNewUserCreationRequest;
import static uk.gov.hmcts.reform.professionalapi.helper.OrganisationFixtures.createJurisdictions;

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
@ActiveProfiles("functional")
@Slf4j
public class ModifyRolesForUserTest extends AuthorizationFunctionalTest {


    RequestSpecification bearerTokenForPuiUserManager;
    RequestSpecification bearerTokenForNonPuiUserManager;
    String orgIdentifierResponse;

    public RequestSpecification generateBearerTokenForPuiManager() {
        Map<String, Object> response = professionalApiClient.createOrganisation();
        orgIdentifierResponse = (String) response.get("organisationIdentifier");
        professionalApiClient.updateOrganisation(orgIdentifierResponse, hmctsAdmin);

        String userEmail = randomAlphabetic(5).toLowerCase() + "@hotmail.com";
        String lastName = "someLastName";
        String firstName = "someName";

        bearerTokenForPuiUserManager = professionalApiClient.getMultipleAuthHeadersExternal(puiUserManager, firstName, lastName, userEmail);

        List<String> userRoles1 = new ArrayList<>();
        userRoles1.add("pui-organisation-manager");
        NewUserCreationRequest userCreationRequest = aNewUserCreationRequest()
                .firstName(firstName)
                .lastName(lastName)
                .email(userEmail)
                .roles(userRoles1)
                .jurisdictions(createJurisdictions())
                .build();
        professionalApiClient.addNewUserToAnOrganisation(orgIdentifierResponse, hmctsAdmin, userCreationRequest, HttpStatus.CREATED);

        return bearerTokenForPuiUserManager;
    }

    public RequestSpecification generateBearerTokenForNonPuiManager() {

        if (bearerTokenForNonPuiUserManager == null) {

            Map<String, Object> response = professionalApiClient.createOrganisation();
            String orgIdentifierResponse = (String) response.get("organisationIdentifier");
            professionalApiClient.updateOrganisation(orgIdentifierResponse, hmctsAdmin);

            List<String> userRoles = new ArrayList<>();
            userRoles.add("pui-case-manager");
            String userEmail = randomAlphabetic(5).toLowerCase() + "@hotmail.com";
            String lastName = "someLastName";
            String firstName = "someName";

            bearerTokenForNonPuiUserManager = professionalApiClient.getMultipleAuthHeadersExternal(puiCaseManager, firstName, lastName, userEmail);

            NewUserCreationRequest userCreationRequest = aNewUserCreationRequest()
                    .firstName(firstName)
                    .lastName(lastName)
                    .email(userEmail)
                    .roles(userRoles)
                    .jurisdictions(createJurisdictions())
                    .build();
            professionalApiClient.addNewUserToAnOrganisation(orgIdentifierResponse, hmctsAdmin, userCreationRequest, HttpStatus.CREATED);

            return bearerTokenForNonPuiUserManager;
        } else {
            return bearerTokenForNonPuiUserManager;
        }
    }

    @Test
    public void ac1_modify_role_existing_user_to_organisation_internal() {

        Map<String, Object> response = professionalApiClient.createOrganisation();

        String orgIdentifier = (String) response.get("organisationIdentifier");
        assertThat(orgIdentifier).isNotEmpty();

        professionalApiClient.updateOrganisation(orgIdentifier, hmctsAdmin);

        IdamOpenIdClient idamOpenIdClient = new IdamOpenIdClient(configProperties);
        String email = idamOpenIdClient.createUser("pui-organisation-manager");
        NewUserCreationRequest newUserCreationRequest = professionalApiClient.createNewUserRequest(email);

        assertThat(newUserCreationRequest).isNotNull();

        Map<String, Object> newUserResponse = professionalApiClient.addNewUserToAnOrganisation(orgIdentifier, hmctsAdmin, newUserCreationRequest, HttpStatus.CREATED);

        assertThat(newUserResponse).isNotNull();

        Map<String, Object> searchResponse = professionalApiClient.searchOrganisationUsersByStatusInternal(orgIdentifier, hmctsAdmin, HttpStatus.OK);
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

        Map<String, Object> modifiedUserResponse = professionalApiClient.modifyUserToExistingUserForPrdAdmin(HttpStatus.OK, userProfileUpdatedData, orgIdentifier, userId);
        searchResponse = professionalApiClient.searchOrganisationUsersByStatusInternal(orgIdentifier, hmctsAdmin, HttpStatus.OK);

        List<Map> professionalUsersResponses1 = (List<Map>) searchResponse.get("users");
        Map professionalUsersResponse1 = getActiveUser(professionalUsersResponses1);

        assertThat(professionalUsersResponse1.get("roles")).isNotNull();

        List<String> rolesSize = (List) professionalUsersResponse1.get("roles");
        assertThat(rolesSize.size()).isEqualTo(3);
        assertThat(rolesSize).contains("caseworker").contains("pui-organisation-manager").contains("pui-user-manager");
    }

    @Test
    public void ac2_add_role_existing_user_using_pui_user_manager_for_external_200() {

        Map<String, Object> searchResponse = professionalApiClient.searchOrganisationUsersByStatusExternal(HttpStatus.OK, generateBearerTokenForPuiManager(), "Active");
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
        professionalApiClient.modifyUserToExistingUserForExternal(HttpStatus.OK, userProfileUpdatedData, bearerTokenForPuiUserManager, userId);
        Map<String, Object> searchResponse1 = professionalApiClient.searchOrganisationUsersByStatusInternal(orgIdentifierResponse, hmctsAdmin, HttpStatus.OK);
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
        String email = idamOpenIdClient.createUser("pui-organisation-manager");
        NewUserCreationRequest newUserCreationRequest = professionalApiClient.createNewUserRequest(email);

        assertThat(newUserCreationRequest).isNotNull();
        // inviting the user
        Map<String, Object> newUserResponse = professionalApiClient.addNewUserToAnOrganisation(orgIdentifier, hmctsAdmin, newUserCreationRequest, HttpStatus.CREATED);

        assertThat(newUserResponse).isNotNull();
        // search active user
        Map<String, Object> searchResponse = professionalApiClient.searchOrganisationUsersByStatusInternal(orgIdentifier, hmctsAdmin, HttpStatus.OK);
        List<Map> professionalUsersResponses = (List<Map>) searchResponse.get("users");
        Map professionalUsersResponse = getActiveUser(professionalUsersResponses);

        assertThat(professionalUsersResponse.get("idamStatus")).isNotNull();
        assertThat(professionalUsersResponse.get("userIdentifier")).isNotNull();
        String userId = (String) professionalUsersResponse.get("userIdentifier");

        //create add roles object
        UserProfileUpdatedData userProfileUpdatedData = new UserProfileUpdatedData();
        userProfileUpdatedData.setRolesAdd(createAddRoleName());

        Map<String, Object> modifiedUserResponse = professionalApiClient.modifyUserToExistingUserForPrdAdmin(HttpStatus.OK, userProfileUpdatedData, orgIdentifier, userId);
        //search active user
        List<String> rolesSize = searchUserInfo(orgIdentifier);
        assertThat(rolesSize.size()).isEqualTo(3);
        assertThat(rolesSize).contains("caseworker").contains("pui-organisation-manager").contains("pui-user-manager");

        UserProfileUpdatedData deleteRoleReqest = new UserProfileUpdatedData();
        deleteRoleReqest.setRolesDelete(createOrDeleteRoleName());
        Map<String, Object> modifiedUserResponseForDelete = professionalApiClient.modifyUserToExistingUserForPrdAdmin(HttpStatus.OK, deleteRoleReqest, orgIdentifier, userId);
        //search active user
        List<String> rolesAfterDelete = searchUserInfo(orgIdentifier);
        assertThat(rolesAfterDelete.size()).isEqualTo(2);
        assertThat(rolesSize).contains("pui-organisation-manager").contains("caseworker");

    }


    @Test
    public void ac4_delete_role_existing_user_using_pui_user_manager_for_external_200() {

        Map<String, Object> searchResponse = professionalApiClient.searchOrganisationUsersByStatusExternal(HttpStatus.OK, generateBearerTokenForPuiManager(), "Active");
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

        Map<String, Object> modifiedUserResponse = professionalApiClient.modifyUserToExistingUserForExternal(HttpStatus.OK, userProfileUpdatedData, bearerTokenForPuiUserManager, userId);
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

        professionalApiClient.modifyUserToExistingUserForExternal(HttpStatus.OK, deleteRoleRequest, bearerTokenForPuiUserManager, userId);

        //search active user
        List<String> rolesInfo = searchUserInfo(orgIdentifierResponse);
        assertThat(rolesInfo.size()).isEqualTo(1);
        //assertThat(!rolesInfo.contains("pui-organisation-manager"));
    }

    @Test
    public void should_get_400_when_modify_roles_for_pending_user_internal() {

        String orgIdentifier = createAndUpdateOrganisationToActive(hmctsAdmin);

        Map<String, Object> createUserResponse = professionalApiClient.addNewUserToAnOrganisation(orgIdentifier, hmctsAdmin,
                professionalApiClient.createNewUserRequest("nonactiveuser@somewhere.com"), HttpStatus.CREATED);

        Map<String, Object> modifiedUserResponse = professionalApiClient.modifyUserToExistingUserForPrdAdmin(HttpStatus.BAD_REQUEST, getUserProfileAddRoleRequest(),
                orgIdentifier, (String)createUserResponse.get("userIdentifier"));

        assertThat(modifiedUserResponse.get("errorDescription")).isEqualTo("UserId status is not active");
        assertThat(modifiedUserResponse.get("errorMessage")).isEqualTo("3 : There is a problem with your request. Please check and try again");
    }

    @Test
    public void should_get_400_when_modify_roles_for_unknown_user_internal() {

        Map<String, Object> modifiedUserResponse = professionalApiClient.modifyUserToExistingUserForPrdAdmin(HttpStatus.NOT_FOUND, getUserProfileAddRoleRequest(),
                createAndUpdateOrganisationToActive(hmctsAdmin), UUID.randomUUID().toString());

        assertThat(modifiedUserResponse.get("errorDescription")).isEqualTo("ResourceNotFoundException- could not find user profile");
        assertThat(modifiedUserResponse.get("errorMessage")).isEqualTo("4 : Resource not found");
    }

    @Test
    public void should_get_403_when_non_active_external_user_modify_roles() {

        String externalUserEmail = "nonactiveuser1234@somewhere.com";
        String orgIdentifier = createAndUpdateOrganisationToActive(hmctsAdmin);

        //create test sidam user and add same user in org
        idamOpenIdClient.createUser(puiUserManager, externalUserEmail, "firstName", "lastName");
        Map<String, Object> createUserResponse = professionalApiClient.addNewUserToAnOrganisation(orgIdentifier, hmctsAdmin,
                professionalApiClient.createNewUserRequest(externalUserEmail), HttpStatus.CREATED);
        String userId = (String)createUserResponse.get("userIdentifier");

        //update status to suspended so that while adding roles by ext user will be non active
        UserProfileUpdatedData updateStatusRequest = getUserStatusUpdateRequest(IdamStatus.SUSPENDED);
        professionalApiClient.modifyUserToExistingUserForPrdAdmin(HttpStatus.OK, updateStatusRequest, orgIdentifier, userId);

        //use external suspended user to add roles should give 403 back
        Map<String, Object> modifiedUserResponse = professionalApiClient.modifyUserToExistingUserForExternal(HttpStatus.FORBIDDEN, getUserProfileAddRoleRequest(),
                professionalApiClient.getMultipleAuthHeaders(idamOpenIdClient.getOpenIdToken(externalUserEmail)), userId);

        assertThat(modifiedUserResponse.get("errorDescription")).isEqualTo("User status must be Active to perform this operation");
        assertThat(modifiedUserResponse.get("errorMessage")).isEqualTo("9 : Access Denied");
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

        Map<String, Object> searchResponse = professionalApiClient.searchOrganisationUsersByStatusInternal(orgIdentifier, hmctsAdmin, HttpStatus.OK);
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

    private Map getActiveUser(List<Map> professionalUsersResponses) {

        Map activeUserMap = null;

        for (Map userMap : professionalUsersResponses) {
            if (userMap.get("idamStatus").equals(IdamStatus.ACTIVE.name())) {
                activeUserMap = userMap;
            }
        }
        return activeUserMap;
    }

}
