package uk.gov.hmcts.reform.professionalapi;

import static org.apache.commons.lang.RandomStringUtils.randomAlphabetic;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.professionalapi.controller.request.NewUserCreationRequest;
import uk.gov.hmcts.reform.professionalapi.domain.ModifyUserProfileData;
import uk.gov.hmcts.reform.professionalapi.domain.RoleName;
import uk.gov.hmcts.reform.professionalapi.idam.IdamClient;

@RunWith(SpringIntegrationSerenityRunner.class)
@ActiveProfiles("functional")
@Slf4j
public class ModifyRolesForUserTest extends AuthorizationFunctionalTest {

    @Test
    public void ac1_modify_role_existing_user_to_organisation_internal() {

        Map<String, Object> response = professionalApiClient.createOrganisation();

        String orgIdentifier = (String) response.get("organisationIdentifier");
        assertThat(orgIdentifier).isNotEmpty();

        professionalApiClient.updateOrganisation(orgIdentifier, hmctsAdmin);

        IdamClient idamClient = new IdamClient(configProperties);
        String email = idamClient.createUser("pui-case-manager");

        NewUserCreationRequest newUserCreationRequest = professionalApiClient.createNewUserRequest(email);

        assertThat(newUserCreationRequest).isNotNull();

        Map<String, Object> newUserResponse = professionalApiClient.addNewUserToAnOrganisation(orgIdentifier, hmctsAdmin, newUserCreationRequest);

        assertThat(newUserResponse).isNotNull();

        Map<String, Object> searchResponse = professionalApiClient.searchAllActiveUsersByOrganisation(orgIdentifier, hmctsAdmin, HttpStatus.OK);
        List<HashMap> professionalUsersResponses = (List<HashMap>) searchResponse.get("users");
        HashMap professionalUsersResponse = professionalUsersResponses.get(0);

        assertThat(professionalUsersResponse.get("idamStatus")).isNotNull();
        assertThat(professionalUsersResponse.get("userIdentifier")).isNotNull();
        String userId = (String) professionalUsersResponse.get("userIdentifier");

        ModifyUserProfileData modifyUserProfileData = new ModifyUserProfileData();
        RoleName role1 = new RoleName("pui-user-manager");

        Set<RoleName> roles = new HashSet<>();
        roles.add(role1);

        modifyUserProfileData.setRolesAdd(roles);
        Map<String, Object> modifiedUserResponse =  professionalApiClient.modifyUserRoleToExistingUserForPrdAdmin(HttpStatus.OK,modifyUserProfileData,orgIdentifier,userId);

        Map<String, Object> searchResponse1 = professionalApiClient.searchAllActiveUsersByOrganisation(orgIdentifier, hmctsAdmin, HttpStatus.OK);
        List<HashMap> professionalUsersResponses1 = (List<HashMap>) searchResponse1.get("users");
        HashMap professionalUsersResponse1 = professionalUsersResponses1.get(0);
        assertThat(professionalUsersResponse1.get("roles")).isNotNull();
        assertThat(professionalUsersResponse1.get("roles")).isEqualTo(3);
    }

    @Test
    public void ac2_invalid_role_access_to_method_through_forbidden_for_internal() {


        Map<String, Object> response = professionalApiClient.createOrganisation();
        String orgIdentifier = (String) response.get("organisationIdentifier");
        assertThat(orgIdentifier).isNotEmpty();

        professionalApiClient.updateOrganisation(orgIdentifier, hmctsAdmin);

        IdamClient idamClient = new IdamClient(configProperties);

        String email = idamClient.createUser("pui-case-manager");

        NewUserCreationRequest newUserCreationRequest = professionalApiClient.createNewUserRequest(email);

        assertThat(newUserCreationRequest).isNotNull();

        Map<String, Object> newUserResponse = professionalApiClient.addNewUserToAnOrganisation(orgIdentifier, hmctsAdmin, newUserCreationRequest);

        assertThat(newUserResponse).isNotNull();

        Map<String, Object> searchResponse = professionalApiClient.searchAllActiveUsersByOrganisation(orgIdentifier, hmctsAdmin, HttpStatus.OK);
        List<HashMap> professionalUsersResponses = (List<HashMap>) searchResponse.get("users");
        HashMap professionalUsersResponse = professionalUsersResponses.get(0);

        assertThat(professionalUsersResponse.get("idamStatus")).isNotNull();
        assertThat(professionalUsersResponse.get("userIdentifier")).isNotNull();
        String userId = (String) professionalUsersResponse.get("userIdentifier");

        ModifyUserProfileData modifyUserProfileData = new ModifyUserProfileData();
        RoleName role1 = new RoleName("pui-user-manager");


        Set<RoleName> roles = new HashSet<>();
        roles.add(role1);

        modifyUserProfileData.setRolesAdd(roles);
        Map<String, Object> modifiedUserResponse =  professionalApiClient.modifyUserRoleToExistingUserForInternal(HttpStatus.FORBIDDEN,modifyUserProfileData,generateBearerTokenForPuiManager(),orgIdentifier,userId);


    }

    @Test
    public void ac3_modify_role_existing_user_external_bad_request() {


        Map<String, Object> response = professionalApiClient.createOrganisation();
        String orgIdentifier = (String) response.get("organisationIdentifier");
        assertThat(orgIdentifier).isNotEmpty();

        professionalApiClient.updateOrganisation(orgIdentifier, hmctsAdmin);

        List<String> userRoles = new ArrayList<>();
        userRoles.add("pui-case-manager");
        String userEmail = randomAlphabetic(5).toLowerCase() + "@hotmail.com";
        String lastName = "someLastName";
        String firstName = "someName";

        bearerTokenForPuiUserManager = professionalApiClient.getMultipleAuthHeadersExternal(puiUserManager, firstName, lastName, userEmail);

        log.info("Bearer token generated for pui user manager:::: " + bearerTokenForNonPuiUserManager);

        NewUserCreationRequest newUserCreationRequest = professionalApiClient.createNewUserRequest(userEmail);

        assertThat(newUserCreationRequest).isNotNull();

        Map<String, Object> newUserResponse = professionalApiClient.addNewUserToAnOrganisation(orgIdentifier, hmctsAdmin, newUserCreationRequest);

        assertThat(newUserResponse).isNotNull();

        Map<String, Object> searchResponse = professionalApiClient.searchAllActiveUsersByOrganisation(orgIdentifier, hmctsAdmin, HttpStatus.OK);
        List<HashMap> professionalUsersResponses = (List<HashMap>) searchResponse.get("users");
        HashMap professionalUsersResponse = professionalUsersResponses.get(0);

        assertThat(professionalUsersResponse.get("idamStatus")).isNotNull();
        assertThat(professionalUsersResponse.get("userIdentifier")).isNotNull();
        String userId = (String) professionalUsersResponse.get("userIdentifier");

        ModifyUserProfileData modifyUserProfileData = new ModifyUserProfileData();
        RoleName role1 = new RoleName("pui-user-manager");

        Set<RoleName> roles = new HashSet<>();
        roles.add(role1);

        modifyUserProfileData.setRolesAdd(roles);
        Map<String, Object> modifiedUserResponse =  professionalApiClient.modifyUserRoleToExistingUserForPrdAdmin(HttpStatus.BAD_REQUEST,modifyUserProfileData,orgIdentifier,"");
    }

}
