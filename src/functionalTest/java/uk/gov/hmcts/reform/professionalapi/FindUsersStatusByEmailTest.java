package uk.gov.hmcts.reform.professionalapi;

import static org.apache.commons.lang.RandomStringUtils.randomAlphabetic;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.professionalapi.controller.request.UserCreationRequest.aUserCreationRequest;
import static uk.gov.hmcts.reform.professionalapi.helper.OrganisationFixtures.createJurisdictions;
import static uk.gov.hmcts.reform.professionalapi.helper.OrganisationFixtures.someMinimalOrganisationRequest;

import io.restassured.specification.RequestSpecification;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.professionalapi.controller.request.NewUserCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest;

@RunWith(SpringIntegrationSerenityRunner.class)
@ActiveProfiles("functional")
@Slf4j
public class FindUsersStatusByEmailTest extends AuthorizationFunctionalTest {

    @Before
    public void createAndUpdateOrganisation() {
        List<String> userRoles = new ArrayList<>();
        userRoles.add("pui-user-manager");
        String userEmail = randomAlphabetic(5).toLowerCase() + "@hotmail.com";
        String lastName = "someLastName";
        String firstName = "someName";

        // creating user in idam with the same email used to create Organisation so that status is already Active in UP
        bearerToken = professionalApiClient.getMultipleAuthHeadersExternal(hmctsAdmin, firstName, lastName, userEmail);

        //create organisation with the same Super User Email
        OrganisationCreationRequest organisationCreationRequest = someMinimalOrganisationRequest().superUser(aUserCreationRequest()
                .firstName(firstName)
                .lastName(lastName)
                .email(userEmail)
                .jurisdictions(createJurisdictions())
                .build()).build();

        Map<String, Object> response = professionalApiClient.createOrganisation(organisationCreationRequest);
        String orgIdentifierResponse = (String) response.get("organisationIdentifier");
        professionalApiClient.updateOrganisation(orgIdentifierResponse, hmctsAdmin);
    }

    @Test
    public void ac1_find_user_status_by_email_with_pui_user_manager_role_should_return_200() {
        String orgId =  createAndUpdateOrganisationToActive(hmctsAdmin);
        List<String> userRoles = new ArrayList<>();
        userRoles.add("pui-case-manager");
        // creating new user request
        NewUserCreationRequest userCreationRequest = createUserRequest(userRoles);
        // creating user in idam with the same email used in the invite user so that status automatically will update in the up
        professionalApiClient.getMultipleAuthHeadersExternal(puiCaseManager, userCreationRequest.getFirstName(), userCreationRequest.getLastName(), userCreationRequest.getEmail());

        professionalApiClient.addNewUserToAnOrganisation(orgId, hmctsAdmin, userCreationRequest, HttpStatus.CREATED, bearerToken);
        Map<String, Object> response = professionalApiClient.findUserStatusByEmail(HttpStatus.OK, generateBearerTokenFor(puiUserManager), userCreationRequest.getEmail());
        assertThat(response.get("userIdentifier")).isNotNull();

    }

    @Test
    public void ac2_find_user_status_by_email_with_pui_case_manager_role_should_return_200_with_user_status_active() {
        String orgId =  createAndUpdateOrganisationToActive(hmctsAdmin);

        List<String> userRoles = new ArrayList<>();
        userRoles.add("pui-case-manager");
        // creating new user request
        NewUserCreationRequest userCreationRequest = createUserRequest(userRoles);
        // creating user in idam with the same email used in the invite user so that status automatically will be update in the up
        professionalApiClient.getMultipleAuthHeadersExternal(puiUserManager, userCreationRequest.getFirstName(), userCreationRequest.getLastName(), userCreationRequest.getEmail());
        // inviting user
        professionalApiClient.addNewUserToAnOrganisation(orgId, hmctsAdmin, userCreationRequest,  HttpStatus.CREATED, bearerToken);
        String email = userCreationRequest.getEmail().toUpperCase();
        Map<String, Object> response = professionalApiClient.findUserStatusByEmail(HttpStatus.OK, generateBearerTokenForExternalUserRolesSpecified(userRoles),email);
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
        professionalApiClient.addNewUserToAnOrganisation(orgId, hmctsAdmin, userCreationRequest, HttpStatus.CREATED, bearerToken);
        // find the status of the user
        Map<String, Object> response = professionalApiClient.findUserStatusByEmail(HttpStatus.NOT_FOUND, generateBearerTokenForExternalUserRolesSpecified(userRoles), userCreationRequest.getEmail());
        assertThat(response.get("userIdentifier")).isNull();
    }

    @Test
    public void ac4_find_user_status_by_email_with_active_pui_organisation_manager_role_should_return_status_for_user() {

        // creating new user request
        List<String> userRoles = new ArrayList<>();
        userRoles.add("pui-user-manager");
        userRoles.add("pui-finance-manager");
        NewUserCreationRequest userCreationRequest = createUserRequest(userRoles);

        professionalApiClient.getMultipleAuthHeadersExternal(puiCaseManager, userCreationRequest.getFirstName(), userCreationRequest.getLastName(), userCreationRequest.getEmail());

        String orgId =  createAndUpdateOrganisationToActive(hmctsAdmin);
        // inviting user
        professionalApiClient.addNewUserToAnOrganisation(orgId, hmctsAdmin, userCreationRequest, HttpStatus.CREATED, bearerToken);
        // find the status of the user
        List<String> userRolesForToken = new ArrayList<>();
        userRolesForToken.add("pui-organisation-manager");

        Map<String, Object> response = professionalApiClient.findUserStatusByEmail(HttpStatus.OK, generateBearerTokenForExternalUserRolesSpecified(userRolesForToken), userCreationRequest.getEmail());
        assertThat(response.get("userIdentifier")).isNotNull();
    }

    @Test
    public void rdcc_719_ac1_find_user_status_by_email_with_caseworker_publiclaw_courtadmin_role_should_return_200_with_user_status_active() {

        String orgId =  createAndUpdateOrganisationToActive(hmctsAdmin);
        assertThat(orgId).isNotNull();

        // creating new user request
        List<String> userRoles = new ArrayList<>();
        userRoles.add("pui-organisation-manager");
        NewUserCreationRequest userCreationRequest = createUserRequest(userRoles);

        // creating user in idam with the same email used in the invite user so that status automatically will update in the up
        professionalApiClient.getMultipleAuthHeadersExternal(puiOrgManager, userCreationRequest.getFirstName(), userCreationRequest.getLastName(), userCreationRequest.getEmail());

        // inviting user
        professionalApiClient.addNewUserToAnOrganisation(orgId, hmctsAdmin, userCreationRequest, HttpStatus.CREATED, bearerToken);

        String email = randomAlphabetic(10) + "@usersearch.test".toLowerCase();
        RequestSpecification bearerTokenForCourtAdmin = professionalApiClient.getMultipleAuthHeadersExternal("caseworker-publiclaw-courtadmin", "externalFname", "externalLname", email);

        Map<String, Object> response = professionalApiClient.findUserStatusByEmail(HttpStatus.OK, bearerTokenForCourtAdmin, userCreationRequest.getEmail());
        assertThat(response.get("userIdentifier")).isNotNull();
    }

    @Test
    public void rdcc_719_ac2_caseworker_publiclaw_courtadmin_role_should_return_403_when_calling_any_other_endpoint() {

        String orgId =  createAndUpdateOrganisationToActive(hmctsAdmin);
        assertThat(orgId).isNotNull();

        // creating new user request
        List<String> userRoles = new ArrayList<>();
        userRoles.add("pui-organisation-manager");
        NewUserCreationRequest userCreationRequest = createUserRequest(userRoles);

        // creating user in idam with the same email used in the invite user so that status automatically will update in the up
        professionalApiClient.getMultipleAuthHeadersExternal(puiOrgManager, userCreationRequest.getFirstName(), userCreationRequest.getLastName(), userCreationRequest.getEmail());

        String email = randomAlphabetic(10) + "@usersearch.test".toLowerCase();
        RequestSpecification bearerTokenForCourtAdmin = professionalApiClient.getMultipleAuthHeadersExternal("caseworker-publiclaw-courtadmin", "externalFname", "externalLname", email);

        // inviting user
        professionalApiClient.addNewUserToAnOrganisationExternal(userCreationRequest, bearerTokenForCourtAdmin, HttpStatus.FORBIDDEN);
    }

}
