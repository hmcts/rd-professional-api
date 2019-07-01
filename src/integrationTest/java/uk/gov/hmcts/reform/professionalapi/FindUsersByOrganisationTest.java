package uk.gov.hmcts.reform.professionalapi;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import org.springframework.beans.factory.annotation.Value;
import uk.gov.hmcts.reform.professionalapi.controller.response.ProfessionalUsersResponse;
import uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUserStatus;
import uk.gov.hmcts.reform.professionalapi.util.AuthorizationEnabledIntegrationTest;


public class FindUsersByOrganisationTest extends AuthorizationEnabledIntegrationTest {


    @Value("${exui.role.hmcts-admin}")
    private String value;


    @Test
    public void can_retrieve_users_with_showDeleted_true_should_return_status_200() {

        String organisationIdentifier = createOrganisationRequest();
        updateOrganisation(organisationIdentifier, puiCaseManager, OrganisationStatus.ACTIVE);
        Map<String, Object> response = professionalReferenceDataClient.findUsersByOrganisation(organisationIdentifier,"True", puiCaseManager);
        validateUsers(response);
    }

    @Test
    public void can_retrieve_users_with_showDeleted_false_should_return_status_200() {
        String organisationIdentifier = createOrganisationRequest();
        updateOrganisation(organisationIdentifier, puiCaseManager, OrganisationStatus.ACTIVE);
        Map<String, Object> response = professionalReferenceDataClient.findUsersByOrganisation(organisationIdentifier,"False", puiCaseManager);
        validateUsers(response);
    }

    @Test
    public void can_retrieve_users_with_showDeleted_null_should_return_status_200() {
        String organisationIdentifier = createOrganisationRequest();
        updateOrganisation(organisationIdentifier, puiCaseManager, OrganisationStatus.ACTIVE);
        Map<String, Object> response = professionalReferenceDataClient.findUsersByOrganisation(organisationIdentifier,null, puiCaseManager);
        validateUsers(response);

    }

    @Test
    public void retrieve_users_with_pending_organisation_status_should_return_no_users_and_return_status_404() {
        String organisationIdentifier = createOrganisationRequest();
        Map<String, Object> response = professionalReferenceDataClient.findUsersByOrganisation(organisationIdentifier,"True", puiCaseManager);
        assertThat(response.get("http_status")).isEqualTo("404");
    }

    @Test
    public void retrieve_users_with_invalid_organisationIdentifier_should_return_status_404() {
        Map<String, Object> response = professionalReferenceDataClient.findUsersByOrganisation("123","False", puiCaseManager);
        assertThat(response.get("http_status")).isEqualTo("404");

    }

    @Test
    public void retrieve_users_with_non_existing_organisationIdentifier_should_return_status_404() {
        Map<String, Object> response = professionalReferenceDataClient.findUsersByOrganisation("A1B2C3D","False", puiCaseManager);
        assertThat(response.get("http_status")).isEqualTo("404");
    }

    private void validateUsers(Map<String, Object> response) {
        assertThat(response.get("http_status")).isEqualTo("200 OK");
        assertThat(((List<ProfessionalUsersResponse>) response.get("users")).size()).isGreaterThan(0);

        List<HashMap> professionalUsersResponses = (List<HashMap>) response.get("users");
        HashMap professionalUsersResponse = professionalUsersResponses.get(0);

        assertThat(professionalUsersResponse.get("userIdentifier")).isNotNull();
        assertThat(professionalUsersResponse.get("firstName")).isNotNull();
        assertThat(professionalUsersResponse.get("lastName")).isNotNull();
        assertThat(professionalUsersResponse.get("email")).isNotNull();
        assertThat(((List)professionalUsersResponse.get("roles")).size()).isEqualTo(0);
    }
}
