package uk.gov.hmcts.reform.professionalapi;

import org.junit.Test;
import uk.gov.hmcts.reform.professionalapi.controller.request.MfaUpdateRequest;
import uk.gov.hmcts.reform.professionalapi.domain.MFAStatus;
import uk.gov.hmcts.reform.professionalapi.util.AuthorizationEnabledIntegrationTest;

import java.util.HashMap;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.SPACE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.INVALID_MFA_VALUE;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.ORG_NOT_ACTIVE;
import static uk.gov.hmcts.reform.professionalapi.util.FeatureConditionEvaluation.FORBIDDEN_EXCEPTION_LD;

public class UpdateOrgMfaStatusIntegrationTest extends AuthorizationEnabledIntegrationTest {

    @Test
    public void update_mfa_status_of_an_active_organisation_with_prd_admin_role_should_return_200() {
        Map<String, Object> updateResponse = professionalReferenceDataClient
                .updateOrgMfaStatus(createMfaUpdateRequest(), getOrganisationId(), hmctsAdmin);

        assertThat(updateResponse).containsEntry("http_status", "200 OK");
    }

    @Test
    public void update_mfa_status_with_bad_request_should_return_400() {
        Map<String, Object> updateResponse = professionalReferenceDataClient
                .updateOrgMfaStatus(null, getOrganisationId(), hmctsAdmin);

        assertThat(updateResponse).containsEntry("http_status", "400");
    }

    @Test
    public void update_mfa_status_with_invalid_mfa_should_return_400() {
        Map<String, Object> updateResponse = professionalReferenceDataClient
                .updateOrgMfaStatus(null, getOrganisationId(), hmctsAdmin);

        assertThat(updateResponse).containsEntry("http_status", "400");
        assertThat(updateResponse.get("response_body").toString()).contains(INVALID_MFA_VALUE);
    }

    @Test
    public void update_mfa_status_when_organisation_not_active_should_return_400() {
        String pendingOrganisationId = createOrganisationRequest();
        updateOrganisation(pendingOrganisationId, hmctsAdmin, "PENDING");

        Map<String, Object> updateResponse = professionalReferenceDataClient
                .updateOrgMfaStatus(createMfaUpdateRequest(), pendingOrganisationId, hmctsAdmin);

        assertThat(updateResponse).containsEntry("http_status", "400");
        assertThat(updateResponse.get("response_body").toString())
                .contains(ORG_NOT_ACTIVE);
    }

    @Test
    public void update_mfa_status_when_organisation_not_found_should_return_404() {
        Map<String, Object> updateResponse = professionalReferenceDataClient
                .updateOrgMfaStatus(createMfaUpdateRequest(), "ABCDEF7", hmctsAdmin);

        assertThat(updateResponse).containsEntry("http_status", "404");
        assertThat(updateResponse.get("response_body").toString())
                .contains("No Organisation was found with the given organisationIdentifier");
    }

    @Test
    public void update_mfa_status_with_invalid_user_roles_should_return_403() {
        Map<String, Object> updateResponse = professionalReferenceDataClient
                .updateOrgMfaStatus(createMfaUpdateRequest(), getOrganisationId(), "Invalid Role");
        assertThat(updateResponse).containsEntry("http_status", "403");
    }

    @Test
    public void update_mfa_status_returns_401_when_invalid_authentication() {
        Map<String, Object> updateResponse = professionalReferenceDataClient
                .updateOrgMfaStatusUnauthorised(createMfaUpdateRequest(), getOrganisationId(), hmctsAdmin);
        assertThat(updateResponse).containsEntry("http_status", "401");
    }

    @Test
    public void returns_launchDarkly_forbidden_when_update_mfa_status_with_invalid_flag() {
        Map<String, String> launchDarklyMap = new HashMap<>();
        launchDarklyMap.put("OrganisationInternalController.updateOrgMfaStatus",
                "test-update-mfa-flag");
        when(featureToggleService.isFlagEnabled(anyString(), anyString())).thenReturn(false);
        when(featureToggleService.getLaunchDarklyMap()).thenReturn(launchDarklyMap);
        Map<String, Object> errorResponseMap = professionalReferenceDataClient
                .updateOrgMfaStatus(createMfaUpdateRequest(), getOrganisationId(), hmctsAdmin);

        assertThat(errorResponseMap).containsEntry("http_status", "403");
        assertThat((String) errorResponseMap.get("response_body"))
                .contains("test-update-mfa-flag".concat(SPACE).concat(FORBIDDEN_EXCEPTION_LD));
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
