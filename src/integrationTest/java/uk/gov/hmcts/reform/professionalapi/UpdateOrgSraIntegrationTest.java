package uk.gov.hmcts.reform.professionalapi;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationSraUpdateRequest;
import uk.gov.hmcts.reform.professionalapi.util.AuthorizationEnabledIntegrationTest;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang.RandomStringUtils.randomAlphabetic;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;


class UpdateOrgSraIntegrationTest extends AuthorizationEnabledIntegrationTest {


    @Test
    void update_sra_of_an_active_organisation_with_prd_admin_role_should_return_200() {

        String orgIdentifier = getOrganisationId();
        String sraId = randomAlphabetic(7);
        OrganisationSraUpdateRequest organisationSraUpdateRequest =
            new OrganisationSraUpdateRequest(sraId);
        Map<String, Object> orgResponse = professionalReferenceDataClient
                .updateOrgSra(organisationSraUpdateRequest,
                    hmctsAdmin,orgIdentifier);
        assertThat(orgResponse).isNotNull();
        assertThat(orgResponse.get("http_status")).isEqualTo(200);

        Map<String, Object> responseBody = professionalReferenceDataClient
            .retrieveSingleOrganisationForV2Api(orgIdentifier,hmctsAdmin);
        assertThat(responseBody).isNotNull();
        assertNotNull(responseBody.get("sraId"));
        assertThat(responseBody.get("sraId").toString()).isEqualTo(sraId);

        List organisationAttributes = (List)responseBody.get("orgAttributes");
        assertThat(organisationAttributes).isNotNull();

        LinkedHashMap<String, Object> attr = (LinkedHashMap)organisationAttributes.get(0);
        assertThat(attr).isNotNull();
        assertThat(attr.get("key")).isEqualTo("regulators-0");
        assertThat(attr.get("value").toString()).isEqualTo(
            "{\"regulatorType\":\"Solicitor Regulation Authority "
                + "(SRA)\",\"organisationRegistrationNumber\":\"" + sraId + "\"}");

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
            .updateOrgSra(new OrganisationSraUpdateRequest("some-SraId"),  hmctsAdmin,null);

        assertThat(updateResponse).containsEntry("http_status", "400");
        assertThat(updateResponse.get("response_body").toString())
            .contains("The given organisationIdentifier must be 7 Alphanumeric Characters");

    }

    @Test
    void update_sra_with_invalid_name_should_return_400() {

        OrganisationSraUpdateRequest organisationSraUpdateRequest =
            new OrganisationSraUpdateRequest("");

        Map<String, Object> updateResponse = professionalReferenceDataClient
                .updateOrgSra(organisationSraUpdateRequest,
                    hmctsAdmin,getOrganisationId());

        assertThat(updateResponse).containsEntry("http_status", "400");
        assertThat(updateResponse.get("response_body").toString()).contains("SRA Id is required");

    }

    @Test
    void update_sra_with_blank_name_should_return_400() {
        OrganisationSraUpdateRequest organisationSraUpdateRequest =
            new OrganisationSraUpdateRequest("    ");

        Map<String, Object> updateResponse = professionalReferenceDataClient
            .updateOrgSra(organisationSraUpdateRequest,
                hmctsAdmin,getOrganisationId());

        assertThat(updateResponse).containsEntry("http_status", "400");
        assertThat(updateResponse.get("response_body").toString()).contains("SRA Id is required");

    }


    private String getOrganisationId() {
        String organisationIdentifier = createOrganisationRequest();
        updateOrganisation(organisationIdentifier, hmctsAdmin, "ACTIVE");

        return organisationIdentifier;
    }

}
