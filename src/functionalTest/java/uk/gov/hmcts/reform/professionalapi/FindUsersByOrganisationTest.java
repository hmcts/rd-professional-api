package uk.gov.hmcts.reform.professionalapi;

import static org.apache.commons.lang.RandomStringUtils.randomAlphabetic;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.professionalapi.controller.request.NewUserCreationRequest.aNewUserCreationRequest;

import io.restassured.specification.RequestSpecification;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.professionalapi.controller.request.NewUserCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.response.IdamStatus;
import uk.gov.hmcts.reform.professionalapi.utils.OrganisationFixtures;

@RunWith(SpringIntegrationSerenityRunner.class)
@ActiveProfiles("functional")
@Slf4j
public class FindUsersByOrganisationTest extends AuthorizationFunctionalTest {

    RequestSpecification bearerTokenForPuiUserManager;
    RequestSpecification bearerTokenForNonPuiUserManager;


    public RequestSpecification generateBearerTokenForPuiManager() {
        Map<String, Object> response = professionalApiClient.createOrganisation();
        log.info("RESPONSE:::::" + response);
        String orgIdentifierResponse = (String) response.get("organisationIdentifier");
        professionalApiClient.updateOrganisation(orgIdentifierResponse, hmctsAdmin);

        log.info("RESPONSE AFTER UPDATE");


        List<String> userRoles = new ArrayList<>();
        userRoles.add("pui-user-manager");
        String userEmail = randomAlphabetic(5).toLowerCase() + "@hotmail.com";
        String lastName = "someLastName";
        String firstName = "someName";

        bearerTokenForPuiUserManager = professionalApiClient.getMultipleAuthHeadersExternal(puiUserManager, firstName, lastName, userEmail);

        log.info("Bearer token generated for non pui user manager:::: " + bearerTokenForPuiUserManager);

        NewUserCreationRequest userCreationRequest = aNewUserCreationRequest()
                .firstName(firstName)
                .lastName(lastName)
                .email(userEmail)
                .roles(userRoles)
                .jurisdictions(OrganisationFixtures.createJurisdictions())
                .build();
        Map<String, Object> newUserResponse = professionalApiClient.addNewUserToAnOrganisation(orgIdentifierResponse, hmctsAdmin, userCreationRequest);

        log.info("NEW USER RESPONSE::::::::::" + newUserResponse);


        return bearerTokenForPuiUserManager;
    }

    public RequestSpecification generateBearerTokenForNonPuiManager() {
        if (bearerTokenForNonPuiUserManager == null) {

            Map<String, Object> response = professionalApiClient.createOrganisation();
            log.info("RESPONSE:::::" + response);
            String orgIdentifierResponse = (String) response.get("organisationIdentifier");
            professionalApiClient.updateOrganisation(orgIdentifierResponse, hmctsAdmin);

            log.info("RESPONSE AFTER UPDATE");

            List<String> userRoles = new ArrayList<>();
            userRoles.add("pui-case-manager");
            String userEmail = randomAlphabetic(5).toLowerCase() + "@hotmail.com";
            String lastName = "someLastName";
            String firstName = "someName";

            bearerTokenForNonPuiUserManager = professionalApiClient.getMultipleAuthHeadersExternal(puiCaseManager, firstName, lastName, userEmail);

            log.info("Bearer token generated for non pui user manager:::: " + bearerTokenForNonPuiUserManager);

            NewUserCreationRequest userCreationRequest = aNewUserCreationRequest()
                    .firstName(firstName)
                    .lastName(lastName)
                    .email(userEmail)
                    .roles(userRoles)
                    .jurisdictions(OrganisationFixtures.createJurisdictions())
                    .build();
            Map<String, Object> newUserResponse = professionalApiClient.addNewUserToAnOrganisation(orgIdentifierResponse, hmctsAdmin, userCreationRequest);

            log.info("NEW USER RESPONSE::::::::::" + newUserResponse);

            return bearerTokenForNonPuiUserManager;
        } else {
            return bearerTokenForNonPuiUserManager;
        }
    }

    @Test
    public void find_users_by_active_organisation_with_showDeleted_False() {

        validateUsers(professionalApiClient.searchUsersByOrganisation(createAndUpdateOrganisationToActive(hmctsAdmin), hmctsAdmin, "False", HttpStatus.OK), true);
    }

    @Test
    public void find_users_by_active_organisation_with_showDeleted_True() {
        validateUsers(professionalApiClient.searchUsersByOrganisation(createAndUpdateOrganisationToActive(hmctsAdmin), hmctsAdmin,"True", HttpStatus.OK), true);
    }

    @Test
    public void find_users_by_active_organisation_with_showDeleted_invalid() {
        validateUsers(professionalApiClient.searchUsersByOrganisation(createAndUpdateOrganisationToActive(hmctsAdmin), hmctsAdmin,"invalid", HttpStatus.OK), true);
    }

    @Test
    public void find_users_for_non_active_organisation() {
        Map<String, Object> response = professionalApiClient.createOrganisation();
        String organisationIdentifier = (String) response.get("organisationIdentifier");
        Map<String, Object> searchResponse = professionalApiClient.searchUsersByOrganisation(organisationIdentifier, hmctsAdmin,"False", HttpStatus.NOT_FOUND);
    }

    @Test
    public void find_users_for_non_existing_organisation() {
        professionalApiClient.searchUsersByOrganisation("Q1VHDF3", hmctsAdmin,"False", HttpStatus.NOT_FOUND);
    }

    @Test
    public void ac1_find_all_active_users_without_roles_for_an_organisation_with_non_pui_user_manager_role_should_return_200() {
        Map<String, Object> response = professionalApiClient.searchAllActiveUsersByOrganisationExternal(HttpStatus.OK, generateBearerTokenForNonPuiManager(), "Active");
        assertThat(response.get("users")).asList().isNotEmpty();
        List<HashMap> professionalUsersResponses = (List<HashMap>) response.get("users");
        HashMap professionalUsersResponse = professionalUsersResponses.get(0);
        assertThat(professionalUsersResponse.get("idamStatus")).isEqualTo(IdamStatus.ACTIVE);
        validateRetrievedUsers(response, false);
    }

    @Test
    public void ac2_find_all_status_users_without_roles_for_an_organisation_with_non_pui_user_manager_role_should_return_400() {
        Map<String, Object> response = professionalApiClient.searchAllActiveUsersByOrganisationExternal(HttpStatus.BAD_REQUEST, generateBearerTokenForNonPuiManager(), " ");
    }

    @Test
    public void ac3_find_all_status_users_for_an_organisation_with_pui_user_manager_should_return_200() {
        Map<String, Object> response = professionalApiClient.searchAllActiveUsersByOrganisationExternal(HttpStatus.OK, generateBearerTokenForPuiManager(), "");
        validateRetrievedUsers(response, false);
    }

    @Test
    public void ac4_find_all_active_users_for_an_organisation_with_pui_user_manager_should_return_200() {
        Map<String, Object> response = professionalApiClient.searchAllActiveUsersByOrganisationExternal(HttpStatus.OK, generateBearerTokenForPuiManager(), "Active");
        validateRetrievedUsers(response, false);
    }

    @Test
    @Ignore
    public void ac5_find_all_suspended_users_for_an_organisation_with_pui_user_manager_when_no_suspended_user_exists_should_return_404() {
        professionalApiClient.searchAllActiveUsersByOrganisationExternal(HttpStatus.NOT_FOUND, generateBearerTokenForPuiManager(), "Suspended");
    }

    @Test
    public void ac6_find_all_status_users_for_an_organisation_with_pui_user_manager_with_invalid_status_provided_should_return_400() {
        professionalApiClient.searchAllActiveUsersByOrganisationExternal(HttpStatus.BAD_REQUEST, generateBearerTokenForPuiManager(), "INVALID");
    }

    @Test
    public void ac7_find_all_active_users_for_an_organisation_with_invalid_bearer_token_should_return_403() {
        String invalidBearerToken = "Bearer eyJ0eXAiOiJKV1QiLCJ6aXAiOiJOT05FIiwia2lkIjoiS0N4QmRlaHNIVUY2OTc4U2l6dklTRXhjWDBFP"
                + "SIsImFsZyI6IlJTMjU2In0.eyJzdWIiOiJmcmVnLXRlc3QtdXNlci0xdTdGTm9kQ0tYQGZlZW1haWwuY29tIiwiYXV0aF9sZXZlbCI6MCwiYX"
                + "VkaXRUcmFja2luZ0lkIjoiNWRjMmVlYjQtZjc2OS00ZWM3LTliZjgtZDE0YjNlMTMzMGE5IiwiaXNzIjoiaHR0cHM6Ly9mb3JnZXJvY2stYW0"
                + "uc2VydmljZS5jb3JlLWNvbXB1dGUtaWRhbS1kZW1vLmludGVybmFsOjg0NDMvb3BlbmFtL29hdXRoMi9obWN0cyIsInRva2VuTmFtZSI6ImFjY"
                + "2Vzc190b2tlbiIsInRva2VuX3R5cGUiOiJCZWFyZXIiLCJhdXRoR3JhbnRJZCI6ImE4NGYxOWNiLTI4NzQtNGE4Zi04MTJlLTkzYjQ4YWEyYzd"
                + "lNyIsImF1ZCI6InJkLXByb2Zlc3Npb25hbC1hcGkiLCJuYmYiOjE1NjY3MjI3NzIsImdyYW50X3R5cGUiOiJhdXRob3JpemF0aW9uX2NvZGUi"
                + "LCJzY29wZSI6WyJvcGVuaWQiLCJwcm9maWxlIiwicm9sZXMiLCJjcmVhdGUtdXNlciIsIm1hbmFnZS11c2VyIl0sImF1dGhfdGltZSI6MTU2Njcy"
                + "Mjc1NTAwMCwicmVhbG0iOiIvaG1jdHMiLCJleHAiOjE1NjY3NTE1NzIsImlhdCI6MTU2NjcyMjc3MiwiZXhwaXJlc19pbiI6Mjg4MDAsImp0aSI6I"
                + "jRlOTg5ZDIxLWU5MjUtNGE4NS04MDI4LTc0MDI4Nzg5N2EyMiJ9.UhLAn6SVZOSz0BIdKXsgfoCHeVX0QfBlpXWwaS8rYlzo56P5kvW7hHG3rhiz8Z"
                + "L68Q2rUUb-KnhQHWaJ7tHTLelH1j4pMIVGORj6k1fk0ib4gKSTj6-Y9y7SCaBnZqCX5ahBIKBnG9xGbWo7w6Jv4QPhxtQmHzMHhhWnD_4NVyKaFW-P3"
                + "uLEOK4d-z4FEDFKgQvlYOMAqrjMjvS-XivSLpKf__FcQ4uXT87x5NkLvFJsVyF_QZmJmmZWUB51Oh7XsrCswRDWua6d6cJK-3nuRwhEoOUGpLobuLKH"
                + "JpX1O5y_hrudPCq_q0m5W1Iqq_xEcNaOg-dqrtpEPXtebfCVZw";

        professionalApiClient.searchAllActiveUsersByOrganisationExternal(HttpStatus.FORBIDDEN, professionalApiClient.getMultipleAuthHeaders(invalidBearerToken), "");
    }

    @Test
    public void ac8_find_all_active_users_for_an_organisation_with_user_profile_unavailable_should_return_500() {
        //cannot be tested
    }

    @Test
    public void ac9_find_all_status_users_for_an_organisation_with_non_pui_user_manager_where_status_is_not_active_should_return_400() {
        professionalApiClient.searchAllActiveUsersByOrganisationExternal(HttpStatus.BAD_REQUEST, generateBearerTokenForNonPuiManager(), "");
    }

    void validateRetrievedUsers(Map<String, Object> searchResponse, Boolean rolesRequired) {
        assertThat(searchResponse.get("users")).asList().isNotEmpty();

        List<HashMap> professionalUsersResponses = (List<HashMap>) searchResponse.get("users");
        HashMap professionalUsersResponse = professionalUsersResponses.get(0);

        assertThat(professionalUsersResponse.get("idamStatus")).isNotNull();
        assertThat(professionalUsersResponse.get("userIdentifier")).isNotNull();
        assertThat(professionalUsersResponse.get("firstName")).isNotNull();
        assertThat(professionalUsersResponse.get("lastName")).isNotNull();
        assertThat(professionalUsersResponse.get("email")).isNotNull();
        if (rolesRequired) {
            assertThat(professionalUsersResponse.get("roles")).isNotNull();
        } else {
            assertThat(professionalUsersResponse.get("roles")).isNull();
        }
    }
}
