package uk.gov.hmcts.reform.professionalapi;

import groovy.util.logging.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.UserDeletionRequest;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;
import uk.gov.hmcts.reform.professionalapi.util.AuthorizationEnabledIntegrationTest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static uk.gov.hmcts.reform.professionalapi.helper.OrganisationFixtures.organisationRequestWithAllFields;
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







}