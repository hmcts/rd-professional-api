package uk.gov.hmcts.reform.professionalapi;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.util.AuthorizationEnabledIntegrationTest;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang.RandomStringUtils.randomAlphabetic;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static uk.gov.hmcts.reform.professionalapi.helper.OrganisationFixtures.organisationRequestWithAllFields;
import static uk.gov.hmcts.reform.professionalapi.helper.OrganisationFixtures.organisationRequestWithAllFieldsAreUpdated;


class UpdateOrgSraIntegrationTest extends AuthorizationEnabledIntegrationTest {


    @Test
    void update_sra_of_an_active_organisation_with_prd_admin_role_should_return_200() {
        java.util.Map<String, Object> responseForOrganisationCreation = professionalReferenceDataClient
            .createOrganisation(organisationRequestWithAllFieldsAreUpdated().build());
        String orgIdentifier = (String)responseForOrganisationCreation.get(ORG_IDENTIFIER);

        Map<String, Object> orgResponse = professionalReferenceDataClient
                .updateOrgSra(organisationRequestWithAllFields().sraId(randomAlphabetic(7)).build(),
                    hmctsAdmin,orgIdentifier);
        LinkedHashMap responseBody = (LinkedHashMap)orgResponse.get("response_body");
        List organisations = (List)responseBody.get("organisations");
        LinkedHashMap organisation = (LinkedHashMap)organisations.get(0);
        assertThat(orgResponse).isNotNull();
        assertNotNull(organisation.get("sraId"));
        assertThat(organisation.get("sraId").toString()).contains("sraId");
        assertThat(orgResponse.get("http_status")).isEqualTo(200);

        List organisationAttributes = (List)organisation.get("organisationAttributes");
        //assertThat(organisation.get("organisationAttributes").toString()).contains(updatedSra);
    }

    @Test
    void update_sra_with_bad_request_should_return_400() {
        Map<String, Object> updateResponse = professionalReferenceDataClient
                .updateOrgSra(null,  hmctsAdmin,getOrganisationId());

        assertThat(updateResponse).containsEntry("http_status", "400");
        assertThat(updateResponse.get("response_body").toString())
            .contains("Required request body is missing:");
    }

    @Test
    void update_sra_with_bad_request_OrgId_missing_should_return_400() {
        Map<String, Object> updateResponse = professionalReferenceDataClient
            .updateOrgSra(organisationRequestWithAllFields().build(),  hmctsAdmin,null);

        assertThat(updateResponse).containsEntry("http_status", "400");
        assertThat(updateResponse.get("response_body").toString())
            .contains("The given organisationIdentifier must be 7 Alphanumeric Characters");

    }

    @Test
    void update_sra_with_invalid_name_should_return_400() {

        OrganisationCreationRequest organisationUpdateRequest = organisationRequestWithAllFieldsAreUpdated()
            .sraId("")
            .build();

        Map<String, Object> updateResponse = professionalReferenceDataClient
                .updateOrgSra(organisationUpdateRequest,
                    hmctsAdmin,getOrganisationId());

        assertThat(updateResponse).containsEntry("http_status", "400");
        assertThat(updateResponse.get("response_body").toString()).isEqualTo("Sra Id is required");

    }

    @Test
    void update_sra_with_invalid_name_should_return_200() {
        OrganisationCreationRequest organisationUpdateRequest = organisationRequestWithAllFieldsAreUpdated()
            .sraId("   ")
            .build();

        Map<String, Object> updateResponse = professionalReferenceDataClient
            .updateOrgSra(organisationUpdateRequest,
                hmctsAdmin,getOrganisationId());

        assertThat(updateResponse).containsEntry("http_status", "400");
        assertThat(updateResponse.get("response_body").toString()).isEqualTo("SraId is required");

    }


    private String getOrganisationId() {
        String organisationIdentifier = createOrganisationRequest();
        updateOrganisation(organisationIdentifier, hmctsAdmin, "ACTIVE");

        return organisationIdentifier;
    }

}
