package uk.gov.hmcts.reform.professionalapi;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.professionalapi.controller.request.DxAddressCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.util.AuthorizationEnabledIntegrationTest;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.professionalapi.helper.OrganisationFixtures.organisationRequestWithAllFields;

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

    private Map<String, Object> deleteDxAddress(DxAddressCreationRequest dxAddressCreationRequest) {
        OrganisationCreationRequest organisationCreationRequest = organisationRequestWithAllFields().build();
        String orgIdentifier = createAndActivateOrganisationWithGivenRequest(organisationCreationRequest);
        return professionalReferenceDataClient.deleteDxAddress(hmctsAdmin, orgIdentifier, dxAddressCreationRequest);
    }
}
