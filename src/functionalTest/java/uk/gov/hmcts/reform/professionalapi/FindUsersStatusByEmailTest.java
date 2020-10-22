package uk.gov.hmcts.reform.professionalapi;

import static org.assertj.core.api.Assertions.assertThat;

import io.restassured.specification.RequestSpecification;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import net.thucydides.core.annotations.WithTag;
import net.thucydides.core.annotations.WithTags;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.professionalapi.controller.request.NewUserCreationRequest;

@RunWith(SpringIntegrationSerenityRunner.class)
@WithTags({@WithTag("testType:Functional")})
@ActiveProfiles("functional")
@Slf4j
public class FindUsersStatusByEmailTest extends AuthorizationFunctionalTest {

    String orgId;

    @Before
    public void setUp() {
        if (null == orgId) {
            orgId = createAndUpdateOrganisationToActive(hmctsAdmin);
        }
    }

    @Test
    public void ac1_find_user_status_by_email_with_pui_user_manager_role_should_return_200() {
        List<String> userRoles = new ArrayList<>();
        userRoles.add("pui-case-manager");
        // creating new user request
        NewUserCreationRequest userCreationRequest = createUserRequest(userRoles);
        // creating user in idam with the same email used in the invite user
        // so that status automatically will update in the up
        professionalApiClient.getMultipleAuthHeadersExternal(puiCaseManager, userCreationRequest.getFirstName(),
                userCreationRequest.getLastName(), userCreationRequest.getEmail());

        professionalApiClient.addNewUserToAnOrganisation(orgId, hmctsAdmin, userCreationRequest, HttpStatus.CREATED);

        Map<String, Object> response = professionalApiClient.findUserStatusByEmail(HttpStatus.OK,
                generateBearerTokenFor(puiUserManager), userCreationRequest.getEmail());
        assertThat(response.get("userIdentifier")).isNotNull();

    }

    @Test
    public void findUserStatusByEmailFromHeaderWithPuiCaseManagerRoleShouldReturn200() {
        List<String> userRoles = new ArrayList<>();
        userRoles.add("pui-case-manager");
        // creating new user request
        NewUserCreationRequest userCreationRequest = createUserRequest(userRoles);
        // creating user in idam with the same email used in the invite user
        // so that status automatically will update in the up
        professionalApiClient.getMultipleAuthHeadersExternal(puiCaseManager, userCreationRequest.getFirstName(),
                userCreationRequest.getLastName(), userCreationRequest.getEmail());

        professionalApiClient.addNewUserToAnOrganisation(orgId, hmctsAdmin, userCreationRequest, HttpStatus.CREATED);
        Map<String, Object> response = professionalApiClient.findUserStatusByEmail(HttpStatus.OK,
                generateBearerTokenForEmailHeader(puiUserManager), userCreationRequest.getEmail());
        assertThat(response.get("userIdentifier")).isNotNull();

    }

    @Test
    public void findUserStatusByEmailFromHeaderWithPuiUserManagerRoleShouldReturn200() {
        List<String> userRoles = new ArrayList<>();
        userRoles.add("pui-user-manager");
        // creating new user request
        NewUserCreationRequest userCreationRequest = createUserRequest(userRoles);
        // creating user in idam with the same email used in the invite user
        // so that status automatically will update in the up
        professionalApiClient.getMultipleAuthHeadersExternal(puiUserManager, userCreationRequest.getFirstName(),
                userCreationRequest.getLastName(), userCreationRequest.getEmail());

        professionalApiClient.addNewUserToAnOrganisation(orgId, hmctsAdmin, userCreationRequest, HttpStatus.CREATED);
        Map<String, Object> response = professionalApiClient.findUserStatusByEmail(HttpStatus.OK,
                generateBearerTokenForEmailHeader(puiUserManager), "prd@prdfunctestuser.com");
        assertThat(response.get("userIdentifier")).isNotNull();

    }

    @Test
    public void ac2_find_user_status_by_email_with_pui_case_manager_role_should_return_200_with_user_status_active() {

        List<String> userRoles = new ArrayList<>();
        userRoles.add("pui-case-manager");
        // creating new user request
        NewUserCreationRequest userCreationRequest = createUserRequest(userRoles);
        // creating user in idam with the same email used in the invite user
        // so that status automatically will be update in the up
        String email = userCreationRequest.getEmail();
        RequestSpecification specification = professionalApiClient
                .getMultipleAuthHeadersExternal(puiUserManager,
                        userCreationRequest.getFirstName(),
                        userCreationRequest.getLastName(), userCreationRequest.getEmail());
        // inviting user
        professionalApiClient.addNewUserToAnOrganisation(orgId, hmctsAdmin, userCreationRequest, HttpStatus.CREATED);
        Map<String, Object> response = professionalApiClient.findUserStatusByEmail(HttpStatus.OK,
                specification, email);
        assertThat(response.get("userIdentifier")).isNotNull();
    }

    @Test
    public void ac3_find_usr_status_by_email_with_not_active_pui_finance_mgr_role_should_rtn_status_pending_for_usr() {
        // creating new user request
        List<String> userRoles = new ArrayList<>();
        userRoles.add("pui-finance-manager");
        NewUserCreationRequest userCreationRequest = createUserRequest(userRoles);
        // inviting user
        professionalApiClient.addNewUserToAnOrganisation(orgId, hmctsAdmin, userCreationRequest, HttpStatus.CREATED);
        // find the status of the user
        Map<String, Object> response = professionalApiClient.findUserStatusByEmail(HttpStatus.NOT_FOUND,
                generateBearerTokenForExternalUserRolesSpecified(userRoles), userCreationRequest.getEmail());
        assertThat(response.get("userIdentifier")).isNull();
    }

    @Test
    public void findUsrStatusByEmailFrmHeaderWithPuiCaseManagerRoleShouldReturn200WithUserStatusActive() {

        List<String> userRoles = new ArrayList<>();
        userRoles.add("pui-case-manager");
        // creating new user request
        NewUserCreationRequest userCreationRequest = createUserRequest(userRoles);
        // creating user in idam with the same email used in the invite user
        // so that status automatically will be update in the up
        String email = userCreationRequest.getEmail();
        RequestSpecification specification = generateBearerTokenForExternalUserRolesSpecified(userRoles, email);
        Map<String, Object> response = professionalApiClient.findUserStatusByEmail(HttpStatus.OK,
                specification, email);
        assertThat(response.get("userIdentifier")).isNotNull();
    }

    @Test
    public void findUsrStatusByEmailFrmHeaderWithNotActivePuiFinanceMgrRoleShouldRtnStatusForUsr() {
        // creating new user request
        List<String> userRoles = new ArrayList<>();
        userRoles.add("pui-finance-manager");
        NewUserCreationRequest userCreationRequest = createUserRequest(userRoles);
        // inviting user
        professionalApiClient.addNewUserToAnOrganisation(orgId, hmctsAdmin, userCreationRequest, HttpStatus.CREATED);
        // find the status of the user
        Map<String, Object> response = professionalApiClient.findUserStatusByEmail(HttpStatus.OK,
                generateBearerTokenForEmailHeader(puiFinanceManager), userCreationRequest.getEmail());
        assertThat(response.get("userIdentifier")).isNotNull();
    }

    @Test
    public void findUsrStatusByEmailFrmHeaderWithNotActivePuiFinanceMgrRoleShouldRtnStatusPendingForUsr() {
        // creating new user request
        List<String> userRoles = new ArrayList<>();
        userRoles.add("pui-finance-manager");
        NewUserCreationRequest userCreationRequest = createUserRequest(userRoles);
        // inviting user
        professionalApiClient.addNewUserToAnOrganisation(orgId, hmctsAdmin, userCreationRequest, HttpStatus.CREATED);
        // find the status of the user
        Map<String, Object> response = professionalApiClient.findUserStatusByEmail(HttpStatus.NOT_FOUND,
                generateBearerTokenFor(puiFinanceManager), userCreationRequest.getEmail());
        assertThat(response.get("userIdentifier")).isNull();
    }

    @Test
    public void ac4_find_usr_status_by_email_with_active_pui_organisation_manager_role_should_return_status_for_usr() {

        // creating new user request
        List<String> userRoles = new ArrayList<>();
        userRoles.add("pui-user-manager");
        userRoles.add("pui-finance-manager");
        NewUserCreationRequest userCreationRequest = createUserRequest(userRoles);

        professionalApiClient.getMultipleAuthHeadersExternal(puiCaseManager, userCreationRequest.getFirstName(),
                userCreationRequest.getLastName(), userCreationRequest.getEmail());

        // inviting user
        professionalApiClient.addNewUserToAnOrganisation(orgId, hmctsAdmin, userCreationRequest, HttpStatus.CREATED);
        // find the status of the user
        List<String> userRolesForToken = new ArrayList<>();
        userRolesForToken.add("pui-organisation-manager");

        Map<String, Object> response = professionalApiClient.findUserStatusByEmail(HttpStatus.OK,
                generateBearerTokenForExternalUserRolesSpecified(userRolesForToken), userCreationRequest.getEmail());
        assertThat(response.get("userIdentifier")).isNotNull();
    }

    @Test
    public void findUsrStatusByEmailFrmHeaderWithPuiOrganisationMgrRoleShouldRtnStatusForUsr() {

        // creating new user request
        List<String> userRoles = new ArrayList<>();
        userRoles.add("pui-user-manager");
        userRoles.add("pui-finance-manager");
        NewUserCreationRequest userCreationRequest = createUserRequest(userRoles);

        professionalApiClient.getMultipleAuthHeadersExternal(puiCaseManager, userCreationRequest.getFirstName(),
                userCreationRequest.getLastName(), userCreationRequest.getEmail());

        // inviting user
        professionalApiClient.addNewUserToAnOrganisation(orgId, hmctsAdmin, userCreationRequest, HttpStatus.CREATED);
        // find the status of the user
        List<String> userRolesForToken = new ArrayList<>();
        userRolesForToken.add("pui-organisation-manager");

        Map<String, Object> response = professionalApiClient.findUserStatusByEmail(HttpStatus.OK,
                generateBearerTokenForEmailHeader("pui-organisation-manager"), userCreationRequest.getEmail());
        assertThat(response.get("userIdentifier")).isNotNull();
    }


    @Test
    public void rdcc_719_ac1_find_usr_sts_by_email_caseworker_publiclaw_courtadmin_role_shld_rtn_200__usr_sts_active() {

        // creating new user request
        List<String> userRoles = new ArrayList<>();
        userRoles.add("pui-organisation-manager");
        NewUserCreationRequest userCreationRequest = createUserRequest(userRoles);

        // creating user in idam with the same email used in the invite user
        // so that status automatically will update in the up
        professionalApiClient.getMultipleAuthHeadersExternal(puiOrgManager, userCreationRequest.getFirstName(),
                userCreationRequest.getLastName(), userCreationRequest.getEmail());

        // inviting user
        professionalApiClient.addNewUserToAnOrganisation(orgId, hmctsAdmin, userCreationRequest, HttpStatus.CREATED);

        String email = generateRandomEmail().toLowerCase();
        RequestSpecification bearerTokenForCourtAdmin = professionalApiClient
                .getMultipleAuthHeadersExternal("caseworker-publiclaw-courtadmin", "externalFname",
                        "externalLname", email);

        Map<String, Object> response = professionalApiClient.findUserStatusByEmail(HttpStatus.OK,
                bearerTokenForCourtAdmin, userCreationRequest.getEmail());
        assertThat(response.get("userIdentifier")).isNotNull();
    }

    @Test
    public void rdcc_719_ac2_caseworker_publiclaw_courtadmin_role_should_return_403_when_calling_any_other_endpoint() {
        // creating new user request
        List<String> userRoles = new ArrayList<>();
        userRoles.add("pui-organisation-manager");
        NewUserCreationRequest userCreationRequest = createUserRequest(userRoles);

        // creating user in idam with the same email used in the invite user
        // so that status automatically will update in the up
        professionalApiClient.getMultipleAuthHeadersExternal(puiOrgManager, userCreationRequest.getFirstName(),
                userCreationRequest.getLastName(), userCreationRequest.getEmail());

        String email = generateRandomEmail().toLowerCase();
        RequestSpecification bearerTokenForCourtAdmin = professionalApiClient
                .getMultipleAuthHeadersExternal("caseworker-publiclaw-courtadmin", "externalFname",
                        "externalLname", email);

        // inviting user
        professionalApiClient.addNewUserToAnOrganisationExternal(userCreationRequest, bearerTokenForCourtAdmin,
                HttpStatus.FORBIDDEN);
    }

}
