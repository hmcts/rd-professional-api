package uk.gov.hmcts.reform.professionalapi;

import static org.apache.commons.lang.RandomStringUtils.randomAlphabetic;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.professionalapi.controller.request.NewUserCreationRequest.aNewUserCreationRequest;

import io.restassured.specification.RequestSpecification;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.professionalapi.controller.request.NewUserCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.response.IdamStatus;
import uk.gov.hmcts.reform.professionalapi.domain.ModifyUserProfileData;
import uk.gov.hmcts.reform.professionalapi.domain.RoleName;
import uk.gov.hmcts.reform.professionalapi.idam.IdamOpenIdClient;
import uk.gov.hmcts.reform.professionalapi.utils.OrganisationFixtures;

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

        List<String> userRoles = new ArrayList<>();
        userRoles.add("pui-user-manager");
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
                .jurisdictions(OrganisationFixtures.createJurisdictions())
                .build();
        professionalApiClient.addNewUserToAnOrganisation(orgIdentifierResponse, hmctsAdmin, userCreationRequest);

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
                    .jurisdictions(OrganisationFixtures.createJurisdictions())
                    .build();
            professionalApiClient.addNewUserToAnOrganisation(orgIdentifierResponse, hmctsAdmin, userCreationRequest);

            return bearerTokenForNonPuiUserManager;
        } else {
            return bearerTokenForNonPuiUserManager;
        }
    }

    @Ignore// Ignoring until OpenId can be tested without hacking config file to point to p.r.
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

        Map<String, Object> newUserResponse = professionalApiClient.addNewUserToAnOrganisation(orgIdentifier, hmctsAdmin, newUserCreationRequest);

        assertThat(newUserResponse).isNotNull();

        Map<String, Object> searchResponse = professionalApiClient.searchOrganisationUsersByStatusInternal(orgIdentifier, hmctsAdmin, HttpStatus.OK);
        List<Map> professionalUsersResponses = (List<Map>) searchResponse.get("users");
        Map professionalUsersResponse = professionalUsersResponses.get(1);

        assertThat(professionalUsersResponse.get("idamStatus")).isNotNull();
        assertThat(professionalUsersResponse.get("userIdentifier")).isNotNull();
        String userId = (String) professionalUsersResponse.get("userIdentifier");

        log.info("User Id::" + userId);
        ModifyUserProfileData modifyUserProfileData = new ModifyUserProfileData();
        RoleName role1 = new RoleName("pui-user-manager");
        Set<RoleName> roles = new HashSet<>();
        roles.add(role1);
        modifyUserProfileData.setRolesAdd(roles);

        Map<String, Object> modifiedUserResponse = professionalApiClient.modifyUserToExistingUserForPrdAdmin(HttpStatus.OK, modifyUserProfileData, orgIdentifier, userId);

        Map<String, Object> searchResponse1 = professionalApiClient.searchOrganisationUsersByStatusInternal(orgIdentifier, hmctsAdmin, HttpStatus.OK);
        List<Map> professionalUsersResponses1 = (List<Map>) searchResponse1.get("users");
        Map professionalUsersResponse1 = professionalUsersResponses1.get(1);
        assertThat(professionalUsersResponse1.get("roles")).isNotNull();

        ArrayList<String> rolesSize = (ArrayList<String>) professionalUsersResponse1.get("roles");
        assertThat(rolesSize.size()).isEqualTo(3);
        assertThat(rolesSize.contains("caseworker,pui-organisation-manager,pui-user-manager"));
    }

    @Ignore// Ignoring until OpenId can be tested without hacking config file to point to p.r.
    @Test
    public void ac2_add_role_existing_user_using_pui_user_manager_for_external_200() {

        Map<String, Object> searchResponse = professionalApiClient.searchOrganisationUsersByStatusExternal(HttpStatus.OK, generateBearerTokenForPuiManager(), "Active");
        List<Map> professionalUsersResponses = (List<Map>) searchResponse.get("users");
        Map professionalUsersResponse = professionalUsersResponses.get(0);
        assertThat(professionalUsersResponse.get("idamStatus")).isNotNull();
        assertThat(professionalUsersResponse.get("userIdentifier")).isNotNull();
        String userId = (String) professionalUsersResponse.get("userIdentifier");

        ModifyUserProfileData modifyUserProfileData = new ModifyUserProfileData();
        RoleName role1 = new RoleName("pui-organisation-manager");
        Set<RoleName> roles = new HashSet<>();
        roles.add(role1);
        modifyUserProfileData.setRolesAdd(roles);
        Map<String, Object> modifiedUserResponse = professionalApiClient.modifyUserToExistingUserForExternal(HttpStatus.OK, modifyUserProfileData, bearerTokenForPuiUserManager, userId);
        Map<String, Object> searchResponse1 = professionalApiClient.searchOrganisationUsersByStatusInternal(orgIdentifierResponse, hmctsAdmin, HttpStatus.OK);
        List<Map> professionalUsersResponses1 = (List<Map>) searchResponse1.get("users");
        Map professionalUsersResponse1 = professionalUsersResponses1.get(1);
        assertThat(professionalUsersResponse1.get("roles")).isNotNull();

        ArrayList<String> rolesSize = (ArrayList<String>) professionalUsersResponse1.get("roles");
        assertThat(rolesSize.size()).isEqualTo(2);
        assertThat(rolesSize.contains("pui-user-manager,pui-organisation-manager"));
    }

    @Ignore// Ignoring until OpenId can be tested without hacking config file to point to p.r.
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
        Map<String, Object> newUserResponse = professionalApiClient.addNewUserToAnOrganisation(orgIdentifier, hmctsAdmin, newUserCreationRequest);

        assertThat(newUserResponse).isNotNull();
        // search active user
        Map<String, Object> searchResponse = professionalApiClient.searchOrganisationUsersByStatusInternal(orgIdentifier, hmctsAdmin, HttpStatus.OK);
        List<Map> professionalUsersResponses = (List<Map>) searchResponse.get("users");
        Map professionalUsersResponse = professionalUsersResponses.get(1);

        assertThat(professionalUsersResponse.get("idamStatus")).isNotNull();
        assertThat(professionalUsersResponse.get("userIdentifier")).isNotNull();
        String userId = (String) professionalUsersResponse.get("userIdentifier");

        log.info("User Id::" + userId);
        //create add roles object
        ModifyUserProfileData modifyUserProfileData = new ModifyUserProfileData();
        modifyUserProfileData.setRolesAdd(createAddRoleName());

        Map<String, Object> modifiedUserResponse = professionalApiClient.modifyUserToExistingUserForPrdAdmin(HttpStatus.OK, modifyUserProfileData, orgIdentifier, userId);
        //search active user
        List<String> rolesSize = searchUserInfo(orgIdentifier);
        assertThat(rolesSize.size()).isEqualTo(3);
        assertThat(rolesSize.contains("caseworker,pui-organisation-manager,pui-user-manager"));

        ModifyUserProfileData deleteRoleReqest = new ModifyUserProfileData();
        deleteRoleReqest.setRolesDelete(createOrDeleteRoleName());
        Map<String, Object> modifiedUserResponseForDelete = professionalApiClient.modifyUserToExistingUserForPrdAdmin(HttpStatus.OK, deleteRoleReqest, orgIdentifier, userId);
        //search active user
        List<String> rolesAfterDelete = searchUserInfo(orgIdentifier);
        assertThat(rolesAfterDelete.size()).isEqualTo(2);
        assertThat(rolesSize.contains("pui-organisation-manager,caseworker"));

    }

    @Ignore// Ignoring until OpenId can be tested without hacking config file to point to p.r.
    @Test
    public void ac4_delete_role_existing_user_using_pui_user_manager_for_external_200() {

        Map<String, Object> searchResponse = professionalApiClient.searchOrganisationUsersByStatusExternal(HttpStatus.OK, generateBearerTokenForPuiManager(), "Active");
        List<Map> professionalUsersResponses = (List<Map>) searchResponse.get("users");
        Map professionalUsersResponse = professionalUsersResponses.get(0);
        assertThat(professionalUsersResponse.get("idamStatus")).isNotNull();
        assertThat(professionalUsersResponse.get("userIdentifier")).isNotNull();
        String userId = (String) professionalUsersResponse.get("userIdentifier");

        ModifyUserProfileData modifyUserProfileData = new ModifyUserProfileData();
        RoleName role1 = new RoleName("pui-organisation-manager");
        Set<RoleName> roles = new HashSet<>();
        roles.add(role1);
        modifyUserProfileData.setRolesAdd(roles);

        Map<String, Object> modifiedUserResponse = professionalApiClient.modifyUserToExistingUserForExternal(HttpStatus.OK, modifyUserProfileData, bearerTokenForPuiUserManager, userId);
        //search active user
        List<String> rolesAfterAdd = searchUserInfo(orgIdentifierResponse);
        assertThat(rolesAfterAdd.size()).isEqualTo(2);
        assertThat(rolesAfterAdd.contains("pui-organisation-manager,caseworker"));

        // roles to delete
        ModifyUserProfileData deleteRoleRequest = new ModifyUserProfileData();
        RoleName roleName = new RoleName("pui-organisation-manager");
        Set<RoleName> rolesDelete = new HashSet<>();
        rolesDelete.add(roleName);
        deleteRoleRequest.setRolesDelete(rolesDelete);

        Map<String, Object> modifiedUserResponseForDelete = professionalApiClient.modifyUserToExistingUserForExternal(HttpStatus.OK, deleteRoleRequest, bearerTokenForPuiUserManager, userId);

        //search active user
        List<String> rolesInfo = searchUserInfo(orgIdentifierResponse);
        assertThat(rolesInfo.size()).isEqualTo(1);
        assertThat(!rolesInfo.contains("pui-organisation-manager"));
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

}
