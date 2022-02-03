package uk.gov.hmcts.reform.professionalapi;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.professionalapi.controller.request.DeleteMultipleAddressRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.domain.ContactInformation;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.repository.ContactInformationRepository;
import uk.gov.hmcts.reform.professionalapi.repository.OrganisationRepository;
import uk.gov.hmcts.reform.professionalapi.util.AuthorizationEnabledIntegrationTest;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.professionalapi.controller.constants.ErrorConstants.MALFORMED_JSON;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.ERROR_MSG_ORG_ADDRESS;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.professionalapi.helper.OrganisationFixtures.organisationRequestWithAllFields;
import static uk.gov.hmcts.reform.professionalapi.helper.OrganisationFixtures.organisationRequestWithMultipleAddressAllFields;


public class DeleteContactInformationIntegrationTest extends AuthorizationEnabledIntegrationTest {

    OrganisationCreationRequest organisationCreationRequest = null;
    Map<String, Object> orgResponse = null;
    String userId = null;
    String orgId = null;

    @Autowired
    OrganisationRepository organisationRepository;

    @Autowired
    ContactInformationRepository contactInformationRepository;

    public void setUpSingleContactAddOrganisationData() {
        organisationCreationRequest = organisationRequestWithAllFields()
                .build();
        commonProcessForORg();
    }

    public String setUpMultipleContactAddOrganisationData() {
        organisationCreationRequest = organisationRequestWithMultipleAddressAllFields()
                .build();
        return commonProcessForORg();
    }

    private String commonProcessForORg() {
        orgResponse = professionalReferenceDataClient.createOrganisation(organisationCreationRequest);
        orgId = (String) orgResponse.get(ORG_IDENTIFIER);
        userId = updateOrgAndInviteUser(orgId, puiOrgManager);
        return orgId;
    }

    @Test
    @SuppressWarnings("unchecked")
    void test_delete_contactInformation_malformed_request() {
        setUpSingleContactAddOrganisationData();
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
        setUpSingleContactAddOrganisationData();
        var deleteMultipleAddressRequest = new DeleteMultipleAddressRequest();
        var requestArrayList = new ArrayList<>(List.of(deleteMultipleAddressRequest));

        Map<String, Object> addressResponse =
                professionalReferenceDataClient
                        .deleteContactInformationAddressOfOrganisation(
                                requestArrayList,
                                puiOrgManager,
                                userId);
        assertThat(addressResponse).isNotNull();
        assertThat(addressResponse).containsEntry("http_status", "400");
        assertThat(addressResponse.get("response_body").toString())
                .contains(MALFORMED_JSON.getErrorMessage());
    }

    @Test
    @SuppressWarnings("unchecked")
    void test_delete_contactInformation_null_address_request() {
        setUpSingleContactAddOrganisationData();
        var deleteMultipleAddressRequest = new DeleteMultipleAddressRequest(null);
        var requestArrayList = new ArrayList<>(List.of(deleteMultipleAddressRequest));
        Map<String, Object> addressResponse =
                professionalReferenceDataClient
                        .deleteContactInformationAddressOfOrganisation(
                                requestArrayList,
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
        setUpSingleContactAddOrganisationData();
        var deleteMultipleAddressRequest = new DeleteMultipleAddressRequest("");
        var requestArrayList = new ArrayList<>(List.of(deleteMultipleAddressRequest));

        Map<String, Object> addressResponse =
                professionalReferenceDataClient
                        .deleteContactInformationAddressOfOrganisation(
                                requestArrayList,
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
        setUpSingleContactAddOrganisationData();
        var deleteMultipleAddressRequest = new DeleteMultipleAddressRequest("");
        var deleteMultipleAddressRequest02 = new DeleteMultipleAddressRequest("test");
        var requestArrayList = new ArrayList<>(List.of(deleteMultipleAddressRequest, deleteMultipleAddressRequest02));

        Map<String, Object> addressResponse =
                professionalReferenceDataClient
                        .deleteContactInformationAddressOfOrganisation(
                                requestArrayList,
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
        setUpSingleContactAddOrganisationData();
        var deleteMultipleAddressRequest = new DeleteMultipleAddressRequest("1234");
        var requestArrayList = new ArrayList<>(List.of(deleteMultipleAddressRequest));
        Map<String, Object> addressResponse =
                professionalReferenceDataClient
                        .deleteContactInformationAddressOfOrganisation(
                                requestArrayList,
                                puiOrgManager,
                                userId);
        assertThat(addressResponse).isNotNull();
        assertThat(addressResponse).containsEntry("http_status", "400");
        assertThat(addressResponse.get("response_body").toString())
                .contains(ERROR_MSG_ORG_ADDRESS);
    }

    @Test
    @SuppressWarnings("unchecked")
    void test_delete_one_contactInformation_from_org_address() {
        String orgId = setUpMultipleContactAddOrganisationData();
        List<ContactInformation> contacts = contactInformationRepository.findAll();

        String addressId = contacts.get(0).getId().toString();
        var deleteMultipleAddressRequest = new DeleteMultipleAddressRequest(addressId);
        var requestArrayList = new ArrayList<>(List.of(deleteMultipleAddressRequest));

        Map<String, Object> addressResponse =
                professionalReferenceDataClient
                        .deleteContactInformationAddressOfOrganisation(
                                requestArrayList,
                                puiOrgManager,
                                userId);
        Organisation organisationAfterDeletion = organisationRepository.findByOrganisationIdentifier(orgId);
        assertThat(organisationAfterDeletion.getContactInformation().size()).isEqualTo(2);
        assertThat(organisationAfterDeletion.getContactInformation().stream()
                .noneMatch(ci -> addressId.equals(ci.getId().toString()))).isTrue();
        assertThat(addressResponse).isNotNull();
        assertThat(addressResponse).containsEntry("http_status", "204 NO_CONTENT");
    }

    @Test
    @SuppressWarnings("unchecked")
    void test_delete_two_contactInformation_from_org_address() {
        String orgId = setUpMultipleContactAddOrganisationData();
        List<ContactInformation> contacts = contactInformationRepository.findAll();
        List<String> addressId = contacts.stream().limit(2).map(ci -> ci.getId().toString())
                .collect(Collectors.toList());

        var deleteMultipleAddressRequest01 = new DeleteMultipleAddressRequest(addressId.get(0));
        var deleteMultipleAddressRequest02 = new DeleteMultipleAddressRequest(addressId.get(1));
        var requestArrayList = new ArrayList<>(List.of(deleteMultipleAddressRequest01, deleteMultipleAddressRequest02));

        Map<String, Object> addressResponse =
                professionalReferenceDataClient
                        .deleteContactInformationAddressOfOrganisation(
                                requestArrayList,
                                puiOrgManager,
                                userId);

        Organisation organisationAfterDeletion = organisationRepository.findByOrganisationIdentifier(orgId);
        assertThat(organisationAfterDeletion.getContactInformation().size()).isEqualTo(1);
        assertThat(organisationAfterDeletion.getContactInformation().stream()
                .noneMatch(ci -> addressId.contains(ci.getId().toString()))).isTrue();
        assertThat(addressResponse).isNotNull();
        assertThat(addressResponse).containsEntry("http_status", "204 NO_CONTENT");
    }

    @Test
    @SuppressWarnings("unchecked")
    void test_delete_three_contactInformation_from_org_address() {
        String orgId = setUpMultipleContactAddOrganisationData();
        List<ContactInformation> contacts = contactInformationRepository.findAll();
        var addressId = contacts.stream()
                .map(ci -> ci.getId().toString()).collect(Collectors.toList());

        var deleteMultipleAddressRequest01 = new DeleteMultipleAddressRequest(addressId.get(0));
        var deleteMultipleAddressRequest02 = new DeleteMultipleAddressRequest(addressId.get(1));
        var deleteMultipleAddressRequest03 = new DeleteMultipleAddressRequest(addressId.get(2));
        var requestArrayList = new ArrayList<>(List.of(deleteMultipleAddressRequest01,
                deleteMultipleAddressRequest02, deleteMultipleAddressRequest03));

        Map<String, Object> addressResponse =
                professionalReferenceDataClient
                        .deleteContactInformationAddressOfOrganisation(
                                requestArrayList,
                                puiOrgManager,
                                userId);
        Organisation organisationAfterDeletion = organisationRepository.findByOrganisationIdentifier(orgId);
        assertThat(organisationAfterDeletion.getContactInformation().size()).isEqualTo(3);
        assertThat(organisationAfterDeletion.getContactInformation().stream()
                .allMatch(ci -> addressId.contains(ci.getId().toString()))).isTrue();
        assertThat(addressResponse).isNotNull();
        assertThat(addressResponse).containsEntry("http_status", "400");
        assertThat(addressResponse.get("response_body").toString())
                .contains(ERROR_MSG_ORG_ADDRESS);
    }


}
