package uk.gov.hmcts.reform.professionalapi;

import org.junit.Test;
import uk.gov.hmcts.reform.professionalapi.controller.request.MfaUpdateRequest;
import uk.gov.hmcts.reform.professionalapi.domain.MFAStatus;
import uk.gov.hmcts.reform.professionalapi.util.AuthorizationEnabledIntegrationTest;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class UpdateOrgMfaStatusIntegrationTest extends AuthorizationEnabledIntegrationTest {

    @Test
    public void update_mfa_status_of_an_active_organisation_with_prd_admin_role_should_return_200() {
        Map<String, Object> updateResponse = professionalReferenceDataClient
                .updateOrgMfaStatus(createMfaUpdateRequest(), getOrganisationId(), hmctsAdmin);

        assertThat(updateResponse.get("http_status")).isNotNull();
        assertThat(updateResponse.get("http_status")).isEqualTo("200 OK");
    }

    @Test
    public void update_mfa_status_with_bad_request_should_return_400() {
        Map<String, Object> updateResponse = professionalReferenceDataClient
                .updateOrgMfaStatus(null, getOrganisationId(), hmctsAdmin);

        assertThat(updateResponse.get("http_status")).isNotNull();
        assertThat(updateResponse.get("http_status")).isEqualTo("400");
    }

    @Test
    public void update_mfa_status_with_invalid_mfa_should_return_400() {
        MfaUpdateRequest mfaUpdateRequest = new MfaUpdateRequest(null);
        Map<String, Object> updateResponse = professionalReferenceDataClient
                .updateOrgMfaStatus(mfaUpdateRequest, getOrganisationId(), hmctsAdmin);

        assertThat(updateResponse.get("http_status")).isEqualTo("400");
        assertThat(updateResponse.get("response_body").toString())
                .contains("The MFA status value provided is not valid. "
                + "Please provide a valid value for the MFA preference of the organisation and try again");
    }

    @Test
    public void update_mfa_status_when_organisation_not_active_should_return_400() {
        String pendingOrganisationId = createOrganisationRequest();
        updateOrganisation(pendingOrganisationId, hmctsAdmin, "PENDING");

        Map<String, Object> updateResponse = professionalReferenceDataClient
                .updateOrgMfaStatus(createMfaUpdateRequest(), pendingOrganisationId, hmctsAdmin);

        assertThat(updateResponse.get("http_status")).isEqualTo("400");
        assertThat(updateResponse.get("response_body").toString())
                .contains("The requested Organisation is not 'Active'");
    }

    @Test
    public void update_mfa_status_when_organisation_not_found_should_return_404() {
        Map<String, Object> updateResponse = professionalReferenceDataClient
                .updateOrgMfaStatus(createMfaUpdateRequest(), UUID.randomUUID().toString(), hmctsAdmin);

        assertThat(updateResponse.get("http_status")).isEqualTo("404");
        assertThat(updateResponse.get("response_body").toString())
                .contains("The requested Organisation does not exist");
    }

    @Test
    public void update_mfa_status_with_invalid_user_roles_should_return_403() {
        Map<String, Object> response = professionalReferenceDataClient
                .updateOrgMfaStatus(createMfaUpdateRequest(), getOrganisationId(), "Invalid Role");
        assertThat(response.get("http_status")).isEqualTo("403");
    }

    @Test
    public void update_mfa_status_returns_401_when_invalid_authentication() {
        Map<String, Object> response = professionalReferenceDataClient
                .updateOrgMfaStatusUnauthorised(createMfaUpdateRequest(), getOrganisationId(), hmctsAdmin);
        assertThat(response.get("http_status")).isEqualTo("401");
    }

    private MfaUpdateRequest createMfaUpdateRequest() {
        return new MfaUpdateRequest(MFAStatus.NONE);
    }

    private String getOrganisationId() {
        String organisationIdentifier = createOrganisationRequest();
        updateOrganisation(organisationIdentifier, hmctsAdmin, "ACTIVE");

        return organisationIdentifier;
    }
}
