package uk.gov.hmcts.reform.professionalapi;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.util.AuthorizationEnabledIntegrationTest;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static uk.gov.hmcts.reform.professionalapi.helper.OrganisationFixtures.organisationRequestWithAllFields;
import static uk.gov.hmcts.reform.professionalapi.helper.OrganisationFixtures.organisationRequestWithAllFieldsAreUpdated;


class UpdateOrgNameIntegrationTest extends AuthorizationEnabledIntegrationTest {


    @Test
    void update_name_of_an_active_organisation_with_prd_admin_role_should_return_200() {
        String orgIdentifier = getOrganisationId();

        Map<String, Object> orgResponse = professionalReferenceDataClient
                .updateOrgName(organisationRequestWithAllFields().name("updatedName").build(),
                    hmctsAdmin,orgIdentifier);
        LinkedHashMap responseBody = (LinkedHashMap)orgResponse.get("response_body");
        List organisations = (List)responseBody.get("organisations");
        LinkedHashMap organisation = (LinkedHashMap)organisations.get(0);
        assertThat(orgResponse).isNotNull();
        assertNotNull(organisation.get("name"));
        assertThat(organisation.get("name").toString()).isEqualTo("updatedName");
        assertThat(orgResponse.get("http_status")).isEqualTo(200);

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
        String orgIdentifier = getOrganisationId();

        Map<String, Object> updateResponse = professionalReferenceDataClient
            .updateOrgName(organisationRequestWithAllFields().name("updatedName").build(),
                hmctsAdmin,null);

        assertThat(updateResponse).containsEntry("http_status", "400");
        assertThat(updateResponse.get("response_body").toString())
            .contains("The given organisationIdentifier must be 7 Alphanumeric Characters");

    }

    @Test
    void update_name_with_invalid_name_should_return_400() {
        String orgIdentifier = getOrganisationId();

        OrganisationCreationRequest organisationUpdateRequest = organisationRequestWithAllFieldsAreUpdated()
            .name("")
            .build();

        Map<String, Object> updateResponse = professionalReferenceDataClient
                .updateOrgName(organisationUpdateRequest, hmctsAdmin,orgIdentifier);

        assertThat(updateResponse).containsEntry("http_status", "400");
        assertThat(updateResponse.get("response_body").toString()).contains("Name is required");

    }

    @Test
    void update_name_with_blank_name_should_return_400() {
        String orgIdentifier = getOrganisationId();

        OrganisationCreationRequest organisationUpdateRequest = organisationRequestWithAllFieldsAreUpdated()
            .name("   ")
            .build();

        Map<String, Object> updateResponse = professionalReferenceDataClient
            .updateOrgName(organisationUpdateRequest,
                hmctsAdmin,orgIdentifier);

        assertThat(updateResponse).containsEntry("http_status", "400");
        assertThat(updateResponse.get("response_body").toString()).contains("Name is required");

    }

    private String getOrganisationId() {
        String organisationIdentifier = createOrganisationRequest();
        updateOrganisation(organisationIdentifier, hmctsAdmin, "ACTIVE");

        return organisationIdentifier;
    }


}
