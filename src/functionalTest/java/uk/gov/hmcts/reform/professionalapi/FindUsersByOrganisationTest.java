package uk.gov.hmcts.reform.professionalapi;

import static org.apache.commons.lang.RandomStringUtils.randomAlphabetic;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.ACTIVE;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.FALSE;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.TRUE;
import static uk.gov.hmcts.reform.professionalapi.controller.request.NewUserCreationRequest.aNewUserCreationRequest;
import static uk.gov.hmcts.reform.professionalapi.helper.OrganisationFixtures.createJurisdictions;

import io.restassured.specification.RequestSpecification;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.professionalapi.controller.constants.IdamStatus;
import uk.gov.hmcts.reform.professionalapi.controller.request.NewUserCreationRequest;
import uk.gov.hmcts.reform.professionalapi.idam.IdamOpenIdClient;

@Ignore
@RunWith(SpringIntegrationSerenityRunner.class)
@ActiveProfiles("functional")
@Slf4j
public class FindUsersByOrganisationTest extends AuthorizationFunctionalTest {

    RequestSpecification bearerTokenForExternalRole;
    RequestSpecification bearerTokenForNonPuiUserManager;


    public RequestSpecification generateBearerTokenForExternalRole(String role) {
        Map<String, Object> response = professionalApiClient.createOrganisation();
        String orgIdentifierResponse = (String) response.get("organisationIdentifier");
        professionalApiClient.updateOrganisation(orgIdentifierResponse, hmctsAdmin);

        List<String> userRoles = new ArrayList<>();
        userRoles.add(role);
        String userEmail = randomAlphabetic(5).toLowerCase() + "@hotmail.com";
        String lastName = "someLastName";
        String firstName = "someName";

        bearerTokenForExternalRole = professionalApiClient.getMultipleAuthHeadersExternal(role, firstName, lastName,
                userEmail);

        NewUserCreationRequest userCreationRequest = aNewUserCreationRequest()
                .firstName(firstName)
                .lastName(lastName)
                .email(userEmail)
                .roles(userRoles)
                .jurisdictions(createJurisdictions())
                .build();
        professionalApiClient.addNewUserToAnOrganisation(orgIdentifierResponse, hmctsAdmin,
                userCreationRequest, HttpStatus.CREATED);

        return bearerTokenForExternalRole;
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

            bearerTokenForNonPuiUserManager = professionalApiClient
                    .getMultipleAuthHeadersExternal(puiCaseManager, firstName, lastName, userEmail);

            NewUserCreationRequest userCreationRequest = aNewUserCreationRequest()
                    .firstName(firstName)
                    .lastName(lastName)
                    .email(userEmail)
                    .roles(userRoles)
                    .jurisdictions(createJurisdictions())
                    .build();
            professionalApiClient.addNewUserToAnOrganisation(orgIdentifierResponse, hmctsAdmin,
                    userCreationRequest, HttpStatus.CREATED);

            return bearerTokenForNonPuiUserManager;
        } else {
            return bearerTokenForNonPuiUserManager;
        }
    }

    @Test
    public void find_users_by_active_organisation_with_showDeleted_False() {
        validateRetrievedUsers(professionalApiClient
                .searchUsersByOrganisation(createAndUpdateOrganisationToActive(hmctsAdmin), hmctsAdmin,
                        "False", HttpStatus.OK, ""), "any",true);
    }

    @Test
    public void find_users_by_active_organisation_with_showDeleted_True() {
        validateRetrievedUsers(professionalApiClient
                .searchUsersByOrganisation(createAndUpdateOrganisationToActive(hmctsAdmin), hmctsAdmin,
                        "True", HttpStatus.OK,""), "any",true);
    }

    @Test
    public void find_users_by_active_organisation_with_showDeleted_invalid() {
        validateRetrievedUsers(professionalApiClient
                .searchUsersByOrganisation(createAndUpdateOrganisationToActive(hmctsAdmin), hmctsAdmin,
                        "invalid", HttpStatus.OK,""), "any",true);
    }

    @Test
    public void find_users_by_active_organisation_with_returnRoles_False() {
        validateRetrievedUsers(professionalApiClient
                .searchUsersByOrganisation(createAndUpdateOrganisationToActive(hmctsAdmin), hmctsAdmin,
                        "False", HttpStatus.OK,"false"), "any",false);
    }

    @Test
    public void find_users_by_active_organisation_with_returnRoles_True() {
        validateRetrievedUsers(professionalApiClient
                .searchUsersByOrganisation(createAndUpdateOrganisationToActive(hmctsAdmin), hmctsAdmin,
                        "True", HttpStatus.OK,"true"), "any",true);
    }

    @Test
    public void find_users_by_active_organisation_with_returnRoles_invalid() {
        professionalApiClient.searchUsersByOrganisation(createAndUpdateOrganisationToActive(hmctsAdmin),
                puiCaseManager,"True", HttpStatus.FORBIDDEN,"");
    }

    @Test
    public void find_users_for_non_active_organisation() {
        Map<String, Object> response = professionalApiClient.createOrganisation();
        String organisationIdentifier = (String) response.get("organisationIdentifier");
        professionalApiClient.searchUsersByOrganisation(organisationIdentifier, hmctsAdmin,"False",
                HttpStatus.NOT_FOUND,"");
    }

    @Test
    public void find_users_for_non_existing_organisation() {
        professionalApiClient.searchUsersByOrganisation("Q1VHDF3", hmctsAdmin,"False",
                HttpStatus.NOT_FOUND,"");
    }

    @Test
    public void ac1_find_all_active_users_with_roles_for_an_org_with_non_pui_user_manager_role_should_rtn_200() {
        Map<String, Object> response = professionalApiClient.searchOrganisationUsersByStatusExternal(HttpStatus.OK,
                generateBearerTokenForNonPuiManager(), "Active");
        validateRetrievedUsers(response, "ACTIVE",true);
    }

    @Test
    public void ac2_shld_rtn_200_and_active_usrs_with_roles_for_an_org_non_pui_user_mgr_role_when_no_status_provided() {
        Map<String, Object> response = professionalApiClient.searchOrganisationUsersByStatusExternal(HttpStatus.OK,
                generateBearerTokenForNonPuiManager(), "");
        validateRetrievedUsers(response, "ACTIVE",true);
    }

    @Test
    public void ac3_find_all_status_users_for_an_organisation_with_pui_user_manager_should_return_200() {
        Map<String, Object> response = professionalApiClient.searchOrganisationUsersByStatusExternal(HttpStatus.OK,
                generateBearerTokenForExternalRole(puiUserManager), "");
        validateRetrievedUsers(response, "ACTIVE", true);
    }

    @Test
    public void ac4_find_all_active_users_for_an_organisation_with_pui_user_manager_should_return_200() {
        Map<String, Object> response = professionalApiClient.searchOrganisationUsersByStatusExternal(HttpStatus.OK,
                generateBearerTokenForExternalRole(puiUserManager), "Active");
        validateRetrievedUsers(response, "ACTIVE", true);
    }

    @Test
    public void ac5_find_all_suspended_usrs_for_an_org_with_pui_usr_mgr_when_no_suspended_user_exists_shld_rtn_404() {
        professionalApiClient.searchOrganisationUsersByStatusExternal(HttpStatus.NOT_FOUND,
                generateBearerTokenForExternalRole(puiUserManager), "Suspended");
    }

    @Test
    public void ac6_find_all_status_usrs_for_an_org_with_pui_user_manager_with_invalid_status_provided_shld_rtn_400() {
        professionalApiClient.searchOrganisationUsersByStatusExternal(HttpStatus.BAD_REQUEST,
                generateBearerTokenForExternalRole(puiUserManager), "INVALID");
    }

    @Test
    public void ac7_find_all_active_users_for_an_organisation_with_invalid_bearer_token_should_return_401() {
        professionalApiClient.searchOrganisationUsersByStatusExternal(HttpStatus.UNAUTHORIZED,
                professionalApiClient.getMultipleAuthHeadersWithEmptyBearerToken(""), "");
    }

    @Test
    public void ac9_find_non_active_status_usrs_for_an_org_non_pui_user_mgr_where_status_not_active_shld_rtn_400() {
        professionalApiClient.searchOrganisationUsersByStatusExternal(HttpStatus.BAD_REQUEST,
                generateBearerTokenForNonPuiManager(), "INVALID");
    }

    @Test
    public void find_all_users_for_an_organisation_with_pagination_should_return_200() {
        String orgIdentifierResponse = createAndUpdateOrganisationToActive(hmctsAdmin);

        List<String> userRoles = new ArrayList<>();
        userRoles.add("pui-case-manager");
        String userEmail = randomAlphabetic(5).toLowerCase() + "@hotmail.com";
        String lastName = "someLastName";
        String firstName = "1Aaron";

        NewUserCreationRequest userCreationRequest = aNewUserCreationRequest()
                .firstName(firstName)
                .lastName(lastName)
                .email(userEmail)
                .roles(userRoles)
                .jurisdictions(createJurisdictions())
                .build();

        professionalApiClient.getMultipleAuthHeadersExternal(puiUserManager, firstName, lastName, userEmail);
        professionalApiClient.addNewUserToAnOrganisation(orgIdentifierResponse, hmctsAdmin, userCreationRequest,
                HttpStatus.CREATED);

        Map<String, Object> searchResponse = professionalApiClient
                .searchUsersByOrganisationWithPagination(orgIdentifierResponse, hmctsAdmin, "False",
                        HttpStatus.OK, "0", "1");

        validateRetrievedUsers(searchResponse, "any", true);
        List<HashMap> professionalUsersResponses = (List<HashMap>) searchResponse.get("users");

        assertThat(professionalUsersResponses.size()).isEqualTo(1);
        assertThat(professionalUsersResponses.get(0).get("firstName")).isEqualTo("1Aaron");

        Map<String, Object> searchResponse2 = professionalApiClient
                .searchUsersByOrganisationWithPagination(orgIdentifierResponse, hmctsAdmin, "False",
                        HttpStatus.OK, "1", "1");

        validateRetrievedUsers(searchResponse2, "any", true);
        List<HashMap> professionalUsersResponses2 = (List<HashMap>) searchResponse2.get("users");
        assertThat(professionalUsersResponses2.size()).isEqualTo(1);
    }

    @Test
    public void find_all_users_for_an_organisation_external_with_pagination_should_return_200() {

        RequestSpecification specification = generateBearerTokenForExternalRole(puiUserManager);
        Map<String, Object> searchResponse = professionalApiClient
                .searchAllActiveUsersByOrganisationExternalWithPagination(HttpStatus.OK, specification,
                        "Active", "0", "1");

        validateRetrievedUsers(searchResponse, "any", true);
        List<HashMap> professionalUsersResponses = (List<HashMap>) searchResponse.get("users");

        assertThat(professionalUsersResponses.size()).isEqualTo(1);

        Map<String, Object> searchResponse2 = professionalApiClient
                .searchAllActiveUsersByOrganisationExternalWithPagination(HttpStatus.OK, specification,
                        "Active", "1", "1");

        validateRetrievedUsers(searchResponse2, "any", true);
        List<HashMap> professionalUsersResponses2 = (List<HashMap>) searchResponse2.get("users");
        assertThat(professionalUsersResponses2.size()).isEqualTo(1);
    }

    @Test
    public void rdcc1439_ac1_find_all_active_users_without_roles_for_an_organisation_should_return_200() {
        Map<String, Object> response = professionalApiClient
                .searchOrganisationUsersByReturnRolesParamExternal(HttpStatus.OK, generateBearerTokenForNonPuiManager(),
                        "false");
        validateRetrievedUsers(response, "ACTIVE", false);
    }

    @Test
    public void rdcc1439_ac2_find_all_active_users_with_roles_for_an_organisation_should_return_200() {
        Map<String, Object> response = professionalApiClient
                .searchOrganisationUsersByReturnRolesParamExternal(HttpStatus.OK,
                        generateBearerTokenForNonPuiManager(), "true");
        validateRetrievedUsers(response, "ACTIVE", true);
    }

    @Test
    public void rdcc1439_ac3_find_all_active_users_with_no_param_given_for_an_organisation_should_return_200() {
        Map<String, Object> response = professionalApiClient
                .searchOrganisationUsersByReturnRolesParamExternal(HttpStatus.OK,
                        generateBearerTokenForNonPuiManager(), "");
        validateRetrievedUsers(response, "ACTIVE", true);
    }

    @Test
    public void rdcc1439_ac4_find_all_active_users_without_appropriate_role_for_an_organisation_should_return_403() {
        Map<String, Object> orgResponse = professionalApiClient.createOrganisation();
        String orgIdentifierResponse = (String) orgResponse.get("organisationIdentifier");
        professionalApiClient.updateOrganisation(orgIdentifierResponse, hmctsAdmin);

        List<String> userRoles = new ArrayList<>();
        userRoles.add("caseworker-caa");
        String userEmail = randomAlphabetic(5).toLowerCase() + "@hotmail.com";
        String lastName = "someLastName";
        String firstName = "someName";

        RequestSpecification bearerTokenForCaseworkerCaa = professionalApiClient
                .getMultipleAuthHeadersExternal("caseworker-caa", firstName, lastName, userEmail);

        professionalApiClient.searchOrganisationUsersByReturnRolesParamExternal(HttpStatus.FORBIDDEN,
                bearerTokenForCaseworkerCaa, "");
    }

    @Test
    public void rdcc1439_ac7_find_all_active_users_with_invalid_param_given_for_an_organisation_should_return_400() {
        Map<String, Object> response = professionalApiClient
                .searchOrganisationUsersByReturnRolesParamExternal(HttpStatus.BAD_REQUEST,
                        generateBearerTokenForNonPuiManager(), "infealfnk");
    }

    @Test
    //RDCC-1531-AC1
    public void find_users_by_active_organisation_with_system_user_role_should_return_active_users() {

        // create active user in sidam
        List<String> roles = new ArrayList<>();
        roles.add(puiUserManager);
        IdamOpenIdClient idamOpenIdClient = new IdamOpenIdClient(configProperties);
        String email = idamOpenIdClient.nextUserEmail();
        NewUserCreationRequest newUserCreationRequest = professionalApiClient.createNewUserRequest();
        newUserCreationRequest.setEmail(email);
        newUserCreationRequest.setRoles(roles);
        idamOpenIdClient
                .createUser(hmctsAdmin, email,
                        newUserCreationRequest.getFirstName(), newUserCreationRequest.getLastName());

        // create and update org
        String organisationIdentifier = createAndUpdateOrganisationToActive(hmctsAdmin);
        // invite new user who is active
        professionalApiClient
                .addNewUserToAnOrganisation(organisationIdentifier, hmctsAdmin, newUserCreationRequest,
                        HttpStatus.CREATED);
        // search
        Map<String, Object> searchResponse = professionalApiClient
                .searchUsersByOrganisation(organisationIdentifier, systemUser, FALSE, HttpStatus.OK, TRUE);
        List<HashMap> professionalUsers = (List<HashMap>) searchResponse.get("users");
        assertThat(professionalUsers.size()).isEqualTo(1);
        validateRetrievedUsers(searchResponse, ACTIVE, false);
    }

    @Test
    //RDCC-1531-AC2
    public void find_users_by_active_org_with_system_user_role_should_return_404_when_users_are_not_active_under_org() {
        professionalApiClient
                .searchUsersByOrganisation(createAndUpdateOrganisationToActive(hmctsAdmin), systemUser, FALSE,
                        HttpStatus.NOT_FOUND, TRUE);
    }

    @Test
    //RDCC-1531-AC3
    public void find_users_by_active_organisation_with_non_permitted_role_should_return_403() {
        professionalApiClient
                .searchUsersByOrganisation(createAndUpdateOrganisationToActive(hmctsAdmin), puiCaseManager, FALSE,
                        HttpStatus.FORBIDDEN, TRUE);
    }

    void validateRetrievedUsers(Map<String, Object> searchResponse, String expectedStatus, Boolean rolesReturned) {
        assertThat(searchResponse.get("users")).asList().isNotEmpty();
        assertThat(searchResponse.get("organisationIdentifier")).isNotNull();
        List<HashMap> professionalUsersResponses = (List<HashMap>) searchResponse.get("users");

        professionalUsersResponses.stream().forEach(user -> {
            assertThat(user.get("idamStatus")).isNotNull();
            assertThat(user.get("userIdentifier")).isNotNull();
            assertThat(user.get("firstName")).isNotNull();
            assertThat(user.get("lastName")).isNotNull();
            assertThat(user.get("email")).isNotNull();
            if (!expectedStatus.equals("any")) {
                assertThat(user.get("idamStatus").equals(expectedStatus));
            }
            if (rolesReturned) {
                if (user.get("idamStatus").equals(IdamStatus.ACTIVE.toString())) {
                    assertThat(user.get("roles")).isNotNull();
                } else {
                    assertThat(user.get("roles")).isNull();
                }
            }
        });
    }
}
