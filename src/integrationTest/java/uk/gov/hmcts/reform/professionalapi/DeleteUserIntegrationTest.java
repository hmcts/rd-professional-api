package uk.gov.hmcts.reform.professionalapi;

import groovy.util.logging.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.professionalapi.controller.request.InvalidRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.UserDeletionRequest;
import uk.gov.hmcts.reform.professionalapi.domain.UserProfileUpdatedData;
import uk.gov.hmcts.reform.professionalapi.util.AuthorizationEnabledIntegrationTest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static uk.gov.hmcts.reform.professionalapi.helper.OrganisationFixtures.someMinimalOrganisationRequest;

@Slf4j
class DeleteUserIntegrationTest extends AuthorizationEnabledIntegrationTest {

    @Test
    void delete_user_non_existing_emails_should_return_400() {
        List<String> emails =  Arrays.asList("incorrect@mailinator.com","incorrect1@mailinator.com");
        UserDeletionRequest userDeletionRequest = new UserDeletionRequest(emails);
        Map<String, Object> updateResponse = professionalReferenceDataClient
            .deleteUser(userDeletionRequest,hmctsAdmin);
        assertThat(updateResponse.get("http_status").toString()).contains("400");
        assertThat(updateResponse.get("response_body").toString()).contains("Email addresses provided do not exist");

    }
    @Test
    void delete_user_should_return_200() {

        setUpUsersToBeDeleted();

        List<String> emails =  Arrays.asList("james.invite@gmaiil.co.uk","vegxjyrwd9f@mailinator.com");
        UserDeletionRequest userDeletionRequest =
            new UserDeletionRequest(emails);

        Map<String, Object> updateResponse = professionalReferenceDataClient
            .deleteUser(userDeletionRequest,hmctsAdmin);

        assertThat(updateResponse.get("http_status")).isNotNull();
        assertThat(updateResponse.get("http_status")).isEqualTo("200 OK");

    }



    @Test
    void delete_user_with_bad_request_should_return_400() {
        Map<String, Object> updateResponse = professionalReferenceDataClient
            .deleteUser(null,  hmctsAdmin);

        assertThat(updateResponse).containsEntry("http_status", "400");

    }

    @Test
    void delete_user_with_empty_emails_should_return_400() {
        UserDeletionRequest userDeletionRequest = new UserDeletionRequest(new ArrayList<String>());

        Map<String, Object> updateResponse = professionalReferenceDataClient
            .deleteUser(userDeletionRequest,  hmctsAdmin);

        assertThat(updateResponse).containsEntry("http_status", "400");
        assertThat(updateResponse.get("response_body").toString())
            .contains("Please provide both email addresses");


    }


    private void setUpUsersToBeDeleted(){
        OrganisationCreationRequest organisationCreationRequest = someMinimalOrganisationRequest().build();
        Map<String, Object> response = professionalReferenceDataClient.createOrganisation(organisationCreationRequest);
        String orgId = (String) response.get(ORG_IDENTIFIER);

        List userRoles = new ArrayList<>();
        userRoles.add("pui-user-manager");

        String userIdentifier = retrieveSuperUserIdFromOrganisationId(orgId);

        Map<String, Object> newUserResponse =
            professionalReferenceDataClient.addUserToOrganisationWithUserId(orgId,
                inviteUserCreationRequest("james.invite@gmaiil.co.uk", userRoles),
                hmctsAdmin, userIdentifier);

        assertThat(newUserResponse.get("http_status")).isNotNull();
        assertThat(newUserResponse.get("http_status")).isEqualTo("200 OK");
        Map<String, Object> newUserResponse1 =
            professionalReferenceDataClient.addUserToOrganisationWithUserId(orgId,
                inviteUserCreationRequest("vegxjyrwd9f@mailinator.com", userRoles),
                hmctsAdmin, userIdentifier);

        assertThat(newUserResponse1.get("http_status")).isNotNull();
        assertThat(newUserResponse1.get("http_status")).isEqualTo("200 OK");
    }

}