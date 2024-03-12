package uk.gov.hmcts.reform.professionalapi;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.util.AuthorizationEnabledIntegrationTest;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.professionalapi.helper.OrganisationFixtures.organisationRequestWithAllFields;
import static uk.gov.hmcts.reform.professionalapi.helper.OrganisationFixtures.organisationRequestWithAllFieldsAreUpdated;


class UpdateOrgNameSraIdIntegrationTest extends AuthorizationEnabledIntegrationTest {


    @Test
    void update_name_or_sra_of_an_active_organisation_with_prd_admin_role_should_return_200() {
        Map<String, Object> updateResponse = professionalReferenceDataClient
                .updateOrgNameSraIdStatus(organisationRequestWithAllFields().build(),  hmctsAdmin,getOrganisationId());

        assertThat(updateResponse).containsEntry("http_status", 200);
    }

    @Test
    void update_name_or_sra_with_bad_request_should_return_400() {
        Map<String, Object> updateResponse = professionalReferenceDataClient
                .updateOrgNameSraIdStatus(null,  hmctsAdmin,getOrganisationId());

        assertThat(updateResponse).containsEntry("http_status", "400");
    }

    @Test
    void update_name_or_sra_with_invalid_name_and_sra_should_return_400() {
        OrganisationCreationRequest organisationUpdateRequest = organisationRequestWithAllFieldsAreUpdated()
            .name("")
            .sraId(null)
            .build();
        Map<String, Object> updateResponse = professionalReferenceDataClient
                .updateOrgNameSraIdStatus(organisationUpdateRequest,  hmctsAdmin,getOrganisationId());

        assertThat(updateResponse).containsEntry("http_status", "400");
        assertThat(updateResponse.get("response_body").toString()).contains("Name or SRA Id not found");
    }

    @Test
    void update_name_or_sra_with_invalid_name_and_valid_sra_should_return_200() {
        OrganisationCreationRequest organisationUpdateRequest = organisationRequestWithAllFieldsAreUpdated()
            .name(null)
            .build();
        Map<String, Object> updateResponse = professionalReferenceDataClient
            .updateOrgNameSraIdStatus(organisationUpdateRequest,  hmctsAdmin,getOrganisationId());

        assertThat(updateResponse).containsEntry("http_status", 200);
    }

    @Test
    void update_name_or_sra_with_invalid_sra_and_valid_name_should_return_200() {
        OrganisationCreationRequest organisationUpdateRequest = organisationRequestWithAllFieldsAreUpdated()
            .sraId(null)
            .build();
        Map<String, Object> updateResponse = professionalReferenceDataClient
            .updateOrgNameSraIdStatus(organisationUpdateRequest,  hmctsAdmin,getOrganisationId());

        assertThat(updateResponse).containsEntry("http_status", 200);
    }

    private String getOrganisationId() {
        String organisationIdentifier = createOrganisationRequest();
        updateOrganisation(organisationIdentifier, hmctsAdmin, "ACTIVE");

        return organisationIdentifier;
    }
}
