package uk.gov.hmcts.reform.professionalapi;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.LENGTH_OF_ORGANISATION_IDENTIFIER;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.ORGANISATION_IDENTIFIER_FORMAT_REGEX;
import static uk.gov.hmcts.reform.professionalapi.helper.OrganisationFixtures.organisationRequestWithAllFields;
import static uk.gov.hmcts.reform.professionalapi.helper.OrganisationFixtures.someMinimalOrganisationRequest;

import java.util.Map;


import org.junit.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.util.AuthorizationEnabledIntegrationTest;

public class DeleteOrganisationIntTest extends AuthorizationEnabledIntegrationTest {

    @Value(("${deleteOrganisationEnabled}"))
    protected boolean deleteOrganisationEnabled;

    @Test
    public void returns_204_when_delete_minimal_pending_organisation_successfully() {

        if (deleteOrganisationEnabled) {
            OrganisationCreationRequest organisationCreationRequest = someMinimalOrganisationRequest().build();

            Map<String, Object> response =
                  professionalReferenceDataClient.createOrganisation(organisationCreationRequest);

            String orgIdentifier = (String) response.get("organisationIdentifier");

            assertThat(orgIdentifier).isNotNull();
            assertThat(orgIdentifier.length()).isEqualTo(LENGTH_OF_ORGANISATION_IDENTIFIER);
            assertThat(orgIdentifier.matches(ORGANISATION_IDENTIFIER_FORMAT_REGEX)).isTrue();

            Map<String, Object> deleteResponse =
                  professionalReferenceDataClient.deleteOrganisation(hmctsAdmin, orgIdentifier);
            assertThat(deleteResponse.get("http_status")).isEqualTo(204);

            Map<String, Object> orgResponse =
                  professionalReferenceDataClient.retrieveSingleOrganisation(orgIdentifier, hmctsAdmin);

            assertThat(orgResponse.get("http_status").toString().contains("OK"));

        }

    }

    @Test
    public void return_forbidden_when_no_role_associated_with_end_point_to_delete_pending_organisation() {

        if (deleteOrganisationEnabled) {
            OrganisationCreationRequest organisationCreationRequest = organisationRequestWithAllFields().build();

            Map<String, Object> response =
                     professionalReferenceDataClient.createOrganisation(organisationCreationRequest);
            String orgIdentifier = (String) response.get("organisationIdentifier");
            assertThat(orgIdentifier).isNotNull();
            Map<String, Object> deleteResponse =
                     professionalReferenceDataClient.deleteOrganisation(puiCaseManager, orgIdentifier);
            assertThat(deleteResponse.get("http_status")).isEqualTo("403");
        }

    }

    @Test
    public void return_404_when_un_known_org_identifier_in_the_request_to_delete_pending_organisation() {

        if (deleteOrganisationEnabled) {
            Map<String, Object> deleteResponse =
                  professionalReferenceDataClient.deleteOrganisation(hmctsAdmin, "O12DEF3");
            assertThat(deleteResponse.get("http_status")).isEqualTo("404");
        }

    }

    @Test
    public void return_400_when_invalid_org_identifier_in_the_request_to_delete_pending_organisation() {

        if (deleteOrganisationEnabled) {
            Map<String, Object> deleteResponse =
                    professionalReferenceDataClient.deleteOrganisation(hmctsAdmin, "O12DEF");
            assertThat(deleteResponse.get("http_status")).isEqualTo("400");
        }

    }

    @Test
    public void returns_400_with_error_msg_when_delete_active_organisation_with_active_user_profile() {
        if (deleteOrganisationEnabled) {
            userProfileCreateUserWireMock(HttpStatus.resolve(201));
            String orgIdentifier = createAndActivateOrganisation();

            Map<String, Object> deleteResponse =
                    professionalReferenceDataClient.deleteOrganisation(hmctsAdmin, orgIdentifier);
            assertThat(deleteResponse.get("http_status")).isEqualTo("400");
            assertThat((String) deleteResponse.get("response_body"))
                    .contains("The organisation admin is not in Pending state");
        }

    }

    @Test
    public void returns_204_when_delete_active_organisation_with_one_pending_user_profile() {

        if (deleteOrganisationEnabled) {
            userProfileCreateUserWireMock(HttpStatus.resolve(201));
            String orgIdentifier = createAndActivateOrganisation();
            getUserProfileByEmailWireMock(HttpStatus.resolve(200));
            deleteUserProfileMock(HttpStatus.resolve(204));
            Map<String, Object> deleteResponse =
                    professionalReferenceDataClient.deleteOrganisation(hmctsAdmin, orgIdentifier);
            assertThat(deleteResponse.get("http_status")).isEqualTo(204);
        }
    }

}
