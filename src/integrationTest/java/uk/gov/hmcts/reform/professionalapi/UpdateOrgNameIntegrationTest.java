package uk.gov.hmcts.reform.professionalapi;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationNameUpdateRequest;
import uk.gov.hmcts.reform.professionalapi.util.AuthorizationEnabledIntegrationTest;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;


class UpdateOrgNameIntegrationTest extends AuthorizationEnabledIntegrationTest {


    @Test
    void update_name_of_an_active_organisation_with_prd_admin_role_should_return_200() {
        String orgIdentifier = getOrganisationId();
        OrganisationNameUpdateRequest organisationNameUpdateRequest =
            new OrganisationNameUpdateRequest("updatedName");
        Map<String, Object> orgUpdatedNameResponse = professionalReferenceDataClient
                .updateOrgName(organisationNameUpdateRequest,
                    hmctsAdmin,orgIdentifier);
        assertThat(orgUpdatedNameResponse.get("http_status")).isEqualTo(200);
        Map<String, Object> responseBody = professionalReferenceDataClient
            .retrieveSingleOrganisation(orgIdentifier,hmctsAdmin);
        assertNotNull(responseBody.get("name"));
        assertThat(responseBody.get("name").toString()).isEqualTo("updatedName");

    }

    @Test
    void update_name_with_bad_request_should_return_400() {
        Map<String, Object> updateResponse = professionalReferenceDataClient
                .updateOrgName(null,  hmctsAdmin,getOrganisationId());

        assertThat(updateResponse).containsEntry("http_status", "400");
        assertThat(updateResponse.get("response_body").toString())
            .contains("Required request body is missing:");
    }

    @Test
    void update_name_with_bad_request_OrgId_missing_should_return_400() {
   
        Map<String, Object> updateResponse = professionalReferenceDataClient
            .updateOrgName(new OrganisationNameUpdateRequest("some-Name"),
                hmctsAdmin,null);

        assertThat(updateResponse).containsEntry("http_status", "400");
        assertThat(updateResponse.get("response_body").toString())
            .contains("The given organisationIdentifier must be 7 Alphanumeric Characters");

    }

    @Test
    void update_name_with_invalid_name_should_return_400() {

        Map<String, Object> updateResponse = professionalReferenceDataClient
                .updateOrgName(new OrganisationNameUpdateRequest(""), hmctsAdmin,getOrganisationId());

        assertThat(updateResponse).containsEntry("http_status", "400");
        assertThat(updateResponse.get("response_body").toString()).contains("Name is required");

    }

    @Test
    void update_name_with_blank_name_should_return_400() {

        Map<String, Object> updateResponse = professionalReferenceDataClient
            .updateOrgName(new OrganisationNameUpdateRequest("    "),
                hmctsAdmin,getOrganisationId());

        assertThat(updateResponse).containsEntry("http_status", "400");
        assertThat(updateResponse.get("response_body").toString()).contains("Name is required");

    }

    private String getOrganisationId() {
        String organisationIdentifier = createOrganisationRequest();
        updateOrganisation(organisationIdentifier, hmctsAdmin, "ACTIVE");

        return organisationIdentifier;
    }


}
