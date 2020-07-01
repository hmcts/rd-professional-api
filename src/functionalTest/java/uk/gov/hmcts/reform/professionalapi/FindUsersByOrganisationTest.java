package uk.gov.hmcts.reform.professionalapi;

import static org.apache.commons.lang.RandomStringUtils.randomAlphabetic;
import static org.assertj.core.api.Assertions.assertThat;
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


@RunWith(SpringIntegrationSerenityRunner.class)
@ActiveProfiles("functional")
@Slf4j
public class FindUsersByOrganisationTest extends AuthorizationFunctionalTest {

    RequestSpecification bearerTokenForPuiUserManager;
    RequestSpecification bearerTokenForNonPuiUserManager;


    public RequestSpecification generateBearerTokenForPuiManager() {
        Map<String, Object> response = professionalApiClient.createOrganisation();
        String orgIdentifierResponse = (String) response.get("organisationIdentifier");
        professionalApiClient.updateOrganisation(orgIdentifierResponse, hmctsAdmin);

        List<String> userRoles = new ArrayList<>();
        userRoles.add("pui-user-manager");
        String userEmail = randomAlphabetic(5).toLowerCase() + "@hotmail.com";
        String lastName = "someLastName";
        String firstName = "someName";

        bearerTokenForPuiUserManager = professionalApiClient.getMultipleAuthHeadersExternal(puiUserManager, firstName, lastName, userEmail);

        NewUserCreationRequest userCreationRequest = aNewUserCreationRequest()
                .firstName(firstName)
                .lastName(lastName)
                .email(userEmail)
                .roles(userRoles)
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
    public void find_users_by_active_organisation_with_showDeleted_False() {
        validateRetrievedUsers(professionalApiClient.searchUsersByOrganisation(createAndUpdateOrganisationToActive(hmctsAdmin), hmctsAdmin, "False", HttpStatus.OK), "any");
    }

    @Test
    public void find_users_by_active_organisation_with_showDeleted_True() {
        validateRetrievedUsers(professionalApiClient.searchUsersByOrganisation(createAndUpdateOrganisationToActive(hmctsAdmin), hmctsAdmin,"True", HttpStatus.OK), "any");
    }

    @Test
    public void find_users_by_active_organisation_with_showDeleted_invalid() {
        validateRetrievedUsers(professionalApiClient.searchUsersByOrganisation(createAndUpdateOrganisationToActive(hmctsAdmin), hmctsAdmin,"invalid", HttpStatus.OK), "any");
    }

    @Test
    public void find_users_for_non_active_organisation() {
        Map<String, Object> response = professionalApiClient.createOrganisation();
        String organisationIdentifier = (String) response.get("organisationIdentifier");
        professionalApiClient.searchUsersByOrganisation(organisationIdentifier, hmctsAdmin,"False", HttpStatus.NOT_FOUND);
    }

    @Test
    public void find_users_for_non_existing_organisation() {
        professionalApiClient.searchUsersByOrganisation("Q1VHDF3", hmctsAdmin,"False", HttpStatus.NOT_FOUND);
    }

    @Test
    public void ac1_find_all_active_users_with_roles_for_an_organisation_with_non_pui_user_manager_role_should_return_200() {
        Map<String, Object> response = professionalApiClient.searchOrganisationUsersByStatusExternal(HttpStatus.OK, generateBearerTokenForNonPuiManager(), "Active");
        validateRetrievedUsers(response, "ACTIVE");
    }

    @Test
    public void ac2_should_return_200_and_active_users_with_roles_for_an_organisation_with_non_pui_user_manager_role_when_no_status_provided() {
        Map<String, Object> response = professionalApiClient.searchOrganisationUsersByStatusExternal(HttpStatus.OK, generateBearerTokenForNonPuiManager(), "");
        validateRetrievedUsers(response, "ACTIVE");
    }

    @Test
    public void ac3_find_all_status_users_for_an_organisation_with_pui_user_manager_should_return_200() {
        Map<String, Object> response = professionalApiClient.searchOrganisationUsersByStatusExternal(HttpStatus.OK, generateBearerTokenForPuiManager(), "");
        validateRetrievedUsers(response, "any");
    }

    @Test
    public void ac4_find_all_active_users_for_an_organisation_with_pui_user_manager_should_return_200() {
        Map<String, Object> response = professionalApiClient.searchOrganisationUsersByStatusExternal(HttpStatus.OK, generateBearerTokenForPuiManager(), "Active");
        validateRetrievedUsers(response, "ACTIVE");
    }

    @Test
    public void ac5_find_all_suspended_users_for_an_organisation_with_pui_user_manager_when_no_suspended_user_exists_should_return_404() {
        professionalApiClient.searchOrganisationUsersByStatusExternal(HttpStatus.NOT_FOUND, generateBearerTokenForPuiManager(), "Suspended");
    }
    
    @Test
    public void ac6_find_all_status_users_for_an_organisation_with_pui_user_manager_with_invalid_status_provided_should_return_400() {
        professionalApiClient.searchOrganisationUsersByStatusExternal(HttpStatus.BAD_REQUEST, generateBearerTokenForPuiManager(), "INVALID");
    }

    @Test
    public void ac7_find_all_active_users_for_an_organisation_with_invalid_bearer_token_should_return_401() {
        professionalApiClient.searchOrganisationUsersByStatusExternal(HttpStatus.UNAUTHORIZED, professionalApiClient.getMultipleAuthHeadersWithEmptyBearerToken(""), "");
    }

    @Test
    public void ac9_find_non_active_status_users_for_an_organisation_with_non_pui_user_manager_where_status_is_not_active_should_return_400() {
        professionalApiClient.searchOrganisationUsersByStatusExternal(HttpStatus.BAD_REQUEST, generateBearerTokenForNonPuiManager(), "INVALID");
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
        professionalApiClient.addNewUserToAnOrganisation(orgIdentifierResponse, hmctsAdmin, userCreationRequest, HttpStatus.CREATED);

        Map<String, Object> searchResponse = professionalApiClient.searchUsersByOrganisationWithPagination(orgIdentifierResponse, hmctsAdmin, "False", HttpStatus.OK, "0", "1");

        validateRetrievedUsers(searchResponse, "any");
        List<HashMap> professionalUsersResponses = (List<HashMap>) searchResponse.get("users");

        assertThat(professionalUsersResponses.size()).isEqualTo(1);
        assertThat(professionalUsersResponses.get(0).get("firstName")).isEqualTo("1Aaron");

        Map<String, Object> searchResponse2 = professionalApiClient.searchUsersByOrganisationWithPagination(orgIdentifierResponse, hmctsAdmin, "False", HttpStatus.OK, "1", "1");

        validateRetrievedUsers(searchResponse2, "any");
        List<HashMap> professionalUsersResponses2 = (List<HashMap>) searchResponse2.get("users");
        assertThat(professionalUsersResponses2.size()).isEqualTo(1);
    }

    @Test
    public void find_all_users_for_an_organisation_external_with_pagination_should_return_200() {

        RequestSpecification specification = generateBearerTokenForPuiManager();
        Map<String, Object> searchResponse = professionalApiClient.searchAllActiveUsersByOrganisationExternalWithPagination(HttpStatus.OK, specification, "Active", "0", "1");

        validateRetrievedUsers(searchResponse, "any");
        List<HashMap> professionalUsersResponses = (List<HashMap>) searchResponse.get("users");

        assertThat(professionalUsersResponses.size()).isEqualTo(1);

        Map<String, Object> searchResponse2 = professionalApiClient.searchAllActiveUsersByOrganisationExternalWithPagination(HttpStatus.OK, specification, "Active", "1", "1");

        validateRetrievedUsers(searchResponse2, "any");
        List<HashMap> professionalUsersResponses2 = (List<HashMap>) searchResponse2.get("users");
        assertThat(professionalUsersResponses2.size()).isEqualTo(1);
    }



    void validateRetrievedUsers(Map<String, Object> searchResponse, String expectedStatus) {
        assertThat(searchResponse.get("users")).asList().isNotEmpty();

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
            if (user.get("idamStatus").equals(IdamStatus.ACTIVE.toString())) {
                assertThat(user.get("roles")).isNotNull();
            } else {
                assertThat(user.get("roles")).isNull();
            }
        });
    }
}
