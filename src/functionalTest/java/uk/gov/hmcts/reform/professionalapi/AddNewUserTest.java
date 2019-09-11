package uk.gov.hmcts.reform.professionalapi;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
public class AddNewUserTest extends AuthorizationFunctionalTest {


    @Test
    public void add_new_user_to_organisation() {
        Map<String, Object> response = professionalApiClient.createOrganisation();
        String orgIdentifierResponse = (String) response.get("organisationIdentifier");
        assertThat(orgIdentifierResponse).isNotEmpty();

        professionalApiClient.updateOrganisation(orgIdentifierResponse, hmctsAdmin);

        NewUserCreationRequest newUserCreationRequest = professionalApiClient.createNewUserRequest();
        assertThat(newUserCreationRequest).isNotNull();

        Map<String, Object> newUserResponse = professionalApiClient.addNewUserToAnOrganisation(orgIdentifierResponse, hmctsAdmin,newUserCreationRequest);

        assertThat(newUserResponse).isNotNull();
    }

    @Test
    public void tc1_modify_role_existing_user_to_organisation_internal() {


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
        Map<String, Object> modifiedUserResponse =  professionalApiClient.modifyUserRoleToExistingUser(modifyUserProfileData,orgIdentifier,userId);

        Map<String, Object> searchResponse1 = professionalApiClient.searchAllActiveUsersByOrganisation(orgIdentifier, hmctsAdmin, HttpStatus.OK);
        List<HashMap> professionalUsersResponses1 = (List<HashMap>) searchResponse1.get("users");
        HashMap professionalUsersResponse1 = professionalUsersResponses1.get(0);
        assertThat(professionalUsersResponse1.get("roles")).isNotNull();
        assertThat(professionalUsersResponse1.get("roles")).isEqualTo(3);
    }

}
