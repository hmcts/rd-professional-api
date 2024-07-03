package uk.gov.hmcts.reform.professionalapi;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.professionalapi.controller.request.DxAddressCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.util.AuthorizationEnabledIntegrationTest;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.professionalapi.helper.OrganisationFixtures.organisationRequestWithAllFields;
import static uk.gov.hmcts.reform.professionalapi.helper.OrganisationFixtures.someMinimalOrganisationRequest;

class DeleteDxAddressTest extends AuthorizationEnabledIntegrationTest {

    @Test
    void returns_204_when_delete_dx_address_successfully() {
        Map<String, Object> deleteResponse = deleteDxAddress(new DxAddressCreationRequest("DX 1234567890",
                "dxExchange"));
        assertThat(deleteResponse.get("http_status")).isEqualTo(204);
    }

    @Test
    void returns_400_when_dx_number_is_invalid() {

        Map<String, Object> deleteResponse = deleteDxAddress(new DxAddressCreationRequest("invalidDxNumber",
                "dxExchange"));
        assertThat(deleteResponse.get("http_status")).isEqualTo(400);
    }

    @Test
    void returns_404_when_deleting_empty_contact_list() {

        OrganisationCreationRequest orgCreationRequest = someMinimalOrganisationRequest().build();
        String orgIdentifier = createAndActivateOrganisationWithGivenRequest(orgCreationRequest);

        Map<String, Object> deleteResponse = professionalReferenceDataClient.deleteDxAddress(hmctsAdmin, orgIdentifier,
                 new DxAddressCreationRequest("DX 1234567890","dxExchange"));

        assertThat(deleteResponse.get("http_status")).isEqualTo(404);
        assertThat((String) deleteResponse.get("response_body")).contains("No contact information  found");
    }

    @Test
    void returns_400_when_deleting_non_exist_dx_number() {
        OrganisationCreationRequest organisationCreationRequest = someMinimalOrganisationRequest().build();
        String orgIdentifier = createAndActivateOrganisationWithGivenRequest(organisationCreationRequest);

        Map<String, Object> deleteResponse = professionalReferenceDataClient.deleteDxAddress(hmctsAdmin, orgIdentifier,
                 new DxAddressCreationRequest("DX 1234567890","dxExchange"));

        assertThat(deleteResponse.get("http_status")).isEqualTo(400);
        assertThat((String) deleteResponse.get("response_body")).contains("No dx address found for organisation");
    }

    private Map<String, Object> deleteDxAddress(DxAddressCreationRequest dxAddressCreationRequest) {
        OrganisationCreationRequest organisationCreationRequest = organisationRequestWithAllFields().build();
        String orgIdentifier = createAndActivateOrganisationWithGivenRequest(organisationCreationRequest);
        return professionalReferenceDataClient.deleteDxAddress(hmctsAdmin, orgIdentifier, dxAddressCreationRequest);
    }
}
