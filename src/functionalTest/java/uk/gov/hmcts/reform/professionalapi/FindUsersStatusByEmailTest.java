package uk.gov.hmcts.reform.professionalapi;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.professionalapi.controller.request.NewUserCreationRequest;

@RunWith(SpringIntegrationSerenityRunner.class)
@ActiveProfiles("functional")
@Slf4j
public class FindUsersStatusByEmailTest extends AuthorizationFunctionalTest {


    @Test
    public void ac1_find_user_status_by_email_with_pui_user_manager_role_should_return_200() {
        String orgId =  createAndUpdateOrganisationToActive(hmctsAdmin);
        List<String> userRoles = new ArrayList<>();
        userRoles.add("pui-case-manager");
        // creating new user request
        NewUserCreationRequest userCreationRequest = createUserRequest(userRoles);
        // creating user in idam with the same email used in the invite user so that status automatically will update in the up
        professionalApiClient.getMultipleAuthHeadersExternal(puiCaseManager, userCreationRequest.getFirstName(), userCreationRequest.getLastName(), userCreationRequest.getEmail());

        professionalApiClient.addNewUserToAnOrganisation(orgId, hmctsAdmin, userCreationRequest, HttpStatus.CREATED);
        Map<String, Object> response = professionalApiClient.findUserStatusByEmail(HttpStatus.OK, generateBearerTokenForPuiManager(), userCreationRequest.getEmail());
        assertThat(response.get("userIdentifier")).isNotNull();

    }



    @Test
    public void ac2_find_user_status_by_email_with_pui_case_manager_role_should_return_200_with_user_status_active() {
        String orgId =  createAndUpdateOrganisationToActive(hmctsAdmin);

        List<String> userRoles = new ArrayList<>();
        userRoles.add("pui-case-manager");
        // creating new user request
        NewUserCreationRequest userCreationRequest = createUserRequest(userRoles);
        // creating user in idam with the same email used in the invite user so that status automatically will update in the up
        professionalApiClient.getMultipleAuthHeadersExternal(puiUserManager, userCreationRequest.getFirstName(), userCreationRequest.getLastName(), userCreationRequest.getEmail());
        // inviting user
        professionalApiClient.addNewUserToAnOrganisation(orgId, hmctsAdmin, userCreationRequest, HttpStatus.CREATED);
        Map<String, Object> response = professionalApiClient.findUserStatusByEmail(HttpStatus.OK, generateBearerTokenForExternalUserRolesSpecified(userRoles), userCreationRequest.getEmail());
        assertThat(response.get("userIdentifier")).isNotNull();
    }

    @Test
    public void ac3_find_user_status_by_email_with_not_active_pui_finance_manager_role_should_return_status_pending_for_user() {
        String orgId =  createAndUpdateOrganisationToActive(hmctsAdmin);
        // creating new user request
        List<String> userRoles = new ArrayList<>();
        userRoles.add("pui-finance-manager");
        NewUserCreationRequest userCreationRequest = createUserRequest(userRoles);
        // inviting user
        professionalApiClient.addNewUserToAnOrganisation(orgId, hmctsAdmin, userCreationRequest, HttpStatus.CREATED);
        // find the status of the user
        Map<String, Object> response = professionalApiClient.findUserStatusByEmail(HttpStatus.NOT_FOUND, generateBearerTokenForExternalUserRolesSpecified(userRoles), userCreationRequest.getEmail());
        assertThat(response.get("userIdentifier")).isNull();
    }

    @Test
    public void ac4_find_user_status_by_email_with_active__pui_organisation_manager_role_should_return_status_for_user() {

        // creating new user request
        List<String> userRoles = new ArrayList<>();
        userRoles.add("pui-user-manager");
        userRoles.add("pui-finance-manager");
        NewUserCreationRequest userCreationRequest = createUserRequest(userRoles);

        professionalApiClient.getMultipleAuthHeadersExternal(puiCaseManager, userCreationRequest.getFirstName(), userCreationRequest.getLastName(), userCreationRequest.getEmail());

        String orgId =  createAndUpdateOrganisationToActive(hmctsAdmin);
        // inviting user
        professionalApiClient.addNewUserToAnOrganisation(orgId, hmctsAdmin, userCreationRequest, HttpStatus.CREATED);
        // find the status of the user
        List<String> userRolesForToken = new ArrayList<>();
        userRolesForToken.add("pui-organisation-manager");

        Map<String, Object> response = professionalApiClient.findUserStatusByEmail(HttpStatus.OK, generateBearerTokenForExternalUserRolesSpecified(userRolesForToken), userCreationRequest.getEmail());
        assertThat(response.get("userIdentifier")).isNotNull();
    }

    @Test
    public void rdcc_719_ac1_find_user_status_by_email_with_caseworker_publiclaw_courtadmin_role_should_return_200_with_user_status_active() {

        String orgId =  createAndUpdateOrganisationToActive(hmctsAdmin);

        List<String> userRoles = new ArrayList<>();
        userRoles.add("caseworker-publiclaw-courtadmin");
        // creating new user request
        NewUserCreationRequest userCreationRequest = createUserRequest(userRoles);
        // creating user in idam with the same email used in the invite user so that status automatically will update in the up
        professionalApiClient.getMultipleAuthHeadersExternal(puiUserManager, userCreationRequest.getFirstName(), userCreationRequest.getLastName(), userCreationRequest.getEmail());
        // inviting user
        professionalApiClient.addNewUserToAnOrganisation(orgId, hmctsAdmin, userCreationRequest, HttpStatus.CREATED);
        Map<String, Object> response = professionalApiClient.findUserStatusByEmail(HttpStatus.OK, generateBearerTokenForExternalUserRolesSpecified(userRoles), userCreationRequest.getEmail());
        assertThat(response.get("userIdentifier")).isNotNull();
    }

}
