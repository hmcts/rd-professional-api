package uk.gov.hmcts.reform.professionalapi;

import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.util.AuthorizationEnabledIntegrationTest;

import static org.apache.commons.lang3.StringUtils.SPACE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.LENGTH_OF_ORGANISATION_IDENTIFIER;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.ORGANISATION_IDENTIFIER_FORMAT_REGEX;
import static uk.gov.hmcts.reform.professionalapi.helper.OrganisationFixtures.organisationRequestWithAllFields;
import static uk.gov.hmcts.reform.professionalapi.helper.OrganisationFixtures.someMinimalOrganisationRequest;
import static uk.gov.hmcts.reform.professionalapi.util.FeatureConditionEvaluation.FORBIDDEN_EXCEPTION_LD;

@RunWith(SpringRunner.class)
public class DeleteOrganisationIntTest extends AuthorizationEnabledIntegrationTest {

    private String orgIdentifier;

    @Test
    public void returns_204_when_delete_minimal_pending_organisation_successfully() {

        Map<String, Object> deleteResponse = deleteOrganization();

        assertThat(deleteResponse.get("http_status")).isEqualTo(204);

        Map<String, Object> orgResponse = professionalReferenceDataClient.retrieveSingleOrganisation(orgIdentifier,
            hmctsAdmin);

        assertThat(orgResponse.get("http_status").toString().contains("OK"));
    }

    @Test
    public void returns_LaunchDarkly_Forbidden_when_delete_minimal_pending_organisation_with_invalid_flag() {
        Map<String, String> launchDarklyMap = new HashMap<>();
        launchDarklyMap.put("OrganisationInternalController.deleteOrganisation",
            "test-flag-1");
        when(featureToggleService.isFlagEnabled(anyString(), anyString())).thenReturn(false);
        when(featureToggleService.getLaunchDarklyMap()).thenReturn(launchDarklyMap);
        Map<String, Object> errorResponseMap = deleteOrganization();
        assertThat(errorResponseMap.get("http_status")).isEqualTo("403");
        assertThat((String) errorResponseMap.get("response_body"))
            .contains("test-flag-1".concat(SPACE).concat(FORBIDDEN_EXCEPTION_LD));
    }

    @Test
    public void return_forbidden_when_no_role_associated_with_end_point_to_delete_pending_organisation() {

        OrganisationCreationRequest organisationCreationRequest = organisationRequestWithAllFields().build();

        Map<String, Object> response =
            professionalReferenceDataClient.createOrganisation(organisationCreationRequest);
        String orgIdentifier = (String) response.get("organisationIdentifier");
        assertThat(orgIdentifier).isNotNull();
        Map<String, Object> deleteResponse =
            professionalReferenceDataClient.deleteOrganisation(puiCaseManager, orgIdentifier);
        assertThat(deleteResponse.get("http_status")).isEqualTo("403");
    }

    @Test
    public void return_404_when_un_known_org_identifier_in_the_request_to_delete_pending_organisation() {

        Map<String, Object> deleteResponse =
            professionalReferenceDataClient.deleteOrganisation(hmctsAdmin, "O12DEF3");
        assertThat(deleteResponse.get("http_status")).isEqualTo("404");
    }

    @Test
    public void return_400_when_invalid_org_identifier_in_the_request_to_delete_pending_organisation() {

        Map<String, Object> deleteResponse =
            professionalReferenceDataClient.deleteOrganisation(hmctsAdmin, "O12DEF");
        assertThat(deleteResponse.get("http_status")).isEqualTo("400");
    }

    @Test
    public void returns_400_with_error_msg_when_delete_active_organisation_with_active_user_profile() {
        userProfileCreateUserWireMock(HttpStatus.resolve(201));
        String orgIdentifier = createAndActivateOrganisation();

        Map<String, Object> deleteResponse =
            professionalReferenceDataClient.deleteOrganisation(hmctsAdmin, orgIdentifier);
        assertThat(deleteResponse.get("http_status")).isEqualTo("400");
        assertThat((String) deleteResponse.get("response_body"))
            .contains("The organisation admin is not in Pending state");

    }

    @Test
    public void returns_204_when_delete_active_organisation_with_one_pending_user_profile() {

        userProfileCreateUserWireMock(HttpStatus.resolve(201));
        String orgIdentifier = createAndActivateOrganisation();
        getUserProfileByEmailWireMock(HttpStatus.resolve(200));
        deleteUserProfileMock(HttpStatus.resolve(204));
        Map<String, Object> deleteResponse =
            professionalReferenceDataClient.deleteOrganisation(hmctsAdmin, orgIdentifier);
        assertThat(deleteResponse.get("http_status")).isEqualTo(204);
    }

    private Map<String, Object> deleteOrganization() {

        OrganisationCreationRequest organisationCreationRequest = someMinimalOrganisationRequest().build();

        Map<String, Object> response =
            professionalReferenceDataClient.createOrganisation(organisationCreationRequest);

        orgIdentifier = (String) response.get("organisationIdentifier");

        assertThat(orgIdentifier).isNotNull();
        assertThat(orgIdentifier.length()).isEqualTo(LENGTH_OF_ORGANISATION_IDENTIFIER);
        assertThat(orgIdentifier.matches(ORGANISATION_IDENTIFIER_FORMAT_REGEX)).isTrue();
        return professionalReferenceDataClient.deleteOrganisation(hmctsAdmin, orgIdentifier);
    }
}
