package uk.gov.hmcts.reform.professionalapi;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import uk.gov.hmcts.reform.professionalapi.controller.request.ContactInformationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.DeleteMultipleAddressRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.util.AuthorizationEnabledIntegrationTest;

import java.util.List;
import java.util.Map;
import java.util.HashSet;

import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.ERROR_MSG_REQUEST_IS_EMPTY;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ErrorConstants.MALFORMED_JSON;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.ERROR_MSG_ORG_ADDRESS;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.professionalapi.helper.OrganisationFixtures.organisationRequestWithAllFields;

public class DeleteContactInformationIntegrationTest extends AuthorizationEnabledIntegrationTest {

    OrganisationCreationRequest organisationCreationRequest = null;
    Map<String, Object> orgResponse = null;
    String userId = null;
    String orgId = null;
    List<ContactInformationCreationRequest> contactInformationCreationRequests = null;

    @BeforeEach
    public void setUpOrganisationData() {

        organisationCreationRequest = organisationRequestWithAllFields()
                .build();
        orgResponse = professionalReferenceDataClient.createOrganisation(organisationCreationRequest);

        orgId = (String) orgResponse.get(ORG_IDENTIFIER);

        userId = updateOrgAndInviteUser(orgId, puiOrgManager);

    }

    @Test
    @SuppressWarnings("unchecked")
    void test_delete_contactInformation_malformed_request() {
        Map<String, Object> addressResponse =
                professionalReferenceDataClient
                        .deleteContactInformationAddressOfOrganisation(
                                null,
                                puiOrgManager,
                                userId);

        assertThat(addressResponse).isNotNull();
        assertThat(addressResponse).containsEntry("http_status", "400");
        assertThat(addressResponse.get("response_body").toString())
                .contains(MALFORMED_JSON.getErrorMessage());
    }

    @Test
    @SuppressWarnings("unchecked")
    void test_delete_contactInformation_empty_request() {
        var deleteMultipleAddressRequest = new DeleteMultipleAddressRequest();

        Map<String, Object> addressResponse =
                professionalReferenceDataClient
                        .deleteContactInformationAddressOfOrganisation(
                                deleteMultipleAddressRequest,
                                puiOrgManager,
                                userId);

        assertThat(addressResponse).isNotNull();
        assertThat(addressResponse).containsEntry("http_status", "400");
        assertThat(addressResponse.get("response_body").toString())
                .contains(ERROR_MSG_REQUEST_IS_EMPTY);
    }

    @Test
    @SuppressWarnings("unchecked")
    void test_delete_contactInformation_null_address_request() {
        var addressId = new HashSet<String>();
        var deleteMultipleAddressRequest = new DeleteMultipleAddressRequest();

        addressId.add(null);
        deleteMultipleAddressRequest.setAddressId(addressId);

        Map<String, Object> addressResponse =
                professionalReferenceDataClient
                        .deleteContactInformationAddressOfOrganisation(
                                deleteMultipleAddressRequest,
                                puiOrgManager,
                                userId);

        assertThat(addressResponse).isNotNull();
        assertThat(addressResponse).containsEntry("http_status", "400");
        assertThat(addressResponse.get("response_body").toString())
                .contains(MALFORMED_JSON.getErrorMessage());
    }

    @Test
    @SuppressWarnings("unchecked")
    void test_delete_contactInformation_empty_address_request() {
        var addressId = new HashSet<String>();
        var deleteMultipleAddressRequest = new DeleteMultipleAddressRequest();

        addressId.add("");
        deleteMultipleAddressRequest.setAddressId(addressId);

        Map<String, Object> addressResponse =
                professionalReferenceDataClient
                        .deleteContactInformationAddressOfOrganisation(
                                deleteMultipleAddressRequest,
                                puiOrgManager,
                                userId);

        assertThat(addressResponse).isNotNull();
        assertThat(addressResponse).containsEntry("http_status", "400");
        assertThat(addressResponse.get("response_body").toString())
                .contains(MALFORMED_JSON.getErrorMessage());
    }

    @Test
    @SuppressWarnings("unchecked")
    void test_delete_contactInformation_one_empty_address_request() {
        var addressId = new HashSet<String>();
        var deleteMultipleAddressRequest = new DeleteMultipleAddressRequest();

        addressId.add("test");
        addressId.add("");
        deleteMultipleAddressRequest.setAddressId(addressId);

        Map<String, Object> addressResponse =
                professionalReferenceDataClient
                        .deleteContactInformationAddressOfOrganisation(
                                deleteMultipleAddressRequest,
                                puiOrgManager,
                                userId);

        assertThat(addressResponse).isNotNull();
        assertThat(addressResponse).containsEntry("http_status", "400");
        assertThat(addressResponse.get("response_body").toString())
                .contains(MALFORMED_JSON.getErrorMessage());
    }

    @Test
    @SuppressWarnings("unchecked")
    void test_delete_contactInformation_only_one_address_request() {
        var addressId = new HashSet<String>();
        var deleteMultipleAddressRequest = new DeleteMultipleAddressRequest();

        addressId.add("1234");
        deleteMultipleAddressRequest.setAddressId(addressId);

        Map<String, Object> addressResponse =
                professionalReferenceDataClient
                        .deleteContactInformationAddressOfOrganisation(
                                deleteMultipleAddressRequest,
                                puiOrgManager,
                                userId);

        assertThat(addressResponse).isNotNull();
        assertThat(addressResponse).containsEntry("http_status", "400");
        assertThat(addressResponse.get("response_body").toString())
                .contains(ERROR_MSG_ORG_ADDRESS);
    }

    /*@Test
    @SuppressWarnings("unchecked")
    void test_delete_one_contactInformation_from_two_org_address() {
        var addressId = new HashSet<String>();
        var deleteMultipleAddressRequest = new DeleteMultipleAddressRequest();

        addressId.add("1234");
        deleteMultipleAddressRequest.setAddressId(addressId);

        //List<ContactInformationCreationRequest> contactInformationList = createContactInformationCreationRequest();


        Map<String, Object> addressResponse =
                professionalReferenceDataClient
                        .deleteContactInformationAddressOfOrganisation(
                                deleteMultipleAddressRequest,
                                puiOrgManager,
                                userId);

        assertThat(addressResponse).isNotNull();
        assertThat(addressResponse).containsEntry("http_status", "400");
        assertThat(addressResponse.get("response_body").toString())
                .contains(ERROR_MSG_ORG_ADDRESS);
    }*/






    /*private List<ContactInformationCreationRequest> createContactInformationCreationRequest() {
        return Arrays.asList(aContactInformationCreationRequest()
                        .addressLine1("")
                        .addressLine2("addressLine2")
                        .addressLine3("addressLine3")
                        .country("country")
                        .county("county")
                        .townCity("town-city")
                        .postCode("some-post-code")
                        .dxAddress(Arrays.asList(dxAddressCreationRequest()
                                .dxNumber("DX 1234567890")
                                .dxExchange("dxExchange01").build()))
                        .build(),

                aContactInformationCreationRequest()
                        .uprn("uprn2")
                        .addressLine1("")
                        .addressLine2("addressLine2")
                        .addressLine3("addressLine3")
                        .country("country")
                        .county("county")
                        .townCity("town-city")
                        .postCode("some-post-code")
                        .dxAddress(Arrays.asList(dxAddressCreationRequest()
                                .dxNumber("DX 2345678901")
                                .dxExchange("dxExchange02").build()))
                        .build());
    }*/

}
