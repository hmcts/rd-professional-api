package uk.gov.hmcts.reform.professionalapi;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.professionalapi.controller.request.ContactInformationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.DeleteMultipleAddressRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.domain.ContactInformation;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.repository.ContactInformationRepository;
import uk.gov.hmcts.reform.professionalapi.repository.OrganisationRepository;
import uk.gov.hmcts.reform.professionalapi.util.AuthorizationEnabledIntegrationTest;

import java.util.List;
import java.util.Map;
import java.util.HashSet;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.ERROR_MSG_REQUEST_IS_EMPTY;
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
    List<ContactInformationCreationRequest> contactInformationCreationRequests = null;

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
        setUpSingleContactAddOrganisationData();
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
        setUpSingleContactAddOrganisationData();
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
        setUpSingleContactAddOrganisationData();
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
        setUpSingleContactAddOrganisationData();
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

    @Test
    @SuppressWarnings("unchecked")
    void test_delete_one_contactInformation_from_org_address() {
        String orgId = setUpMultipleContactAddOrganisationData();
        var deleteMultipleAddressRequest = new DeleteMultipleAddressRequest();
        List<ContactInformation> contactInformations = contactInformationRepository.findAll();
        var addressId = contactInformations.stream().limit(1).map(ci -> ci.getId().toString())
                .collect(Collectors.toSet());
        deleteMultipleAddressRequest.setAddressId(addressId);
        Map<String, Object> addressResponse =
                professionalReferenceDataClient
                        .deleteContactInformationAddressOfOrganisation(
                                deleteMultipleAddressRequest,
                                puiOrgManager,
                                userId);
        Organisation organisationAfterDeletion = organisationRepository.findByOrganisationIdentifier(orgId);
        assertThat(organisationAfterDeletion.getContactInformation().size()).isEqualTo(2);
        assertThat(organisationAfterDeletion.getContactInformation().stream()
                .noneMatch(ci -> addressId.contains(ci.getId().toString()))).isTrue();
        assertThat(addressResponse).isNotNull();
        assertThat(addressResponse).containsEntry("http_status", "204 NO_CONTENT");
    }

    @Test
    @SuppressWarnings("unchecked")
    void test_delete_two_contactInformation_from_org_address() {
        var deleteMultipleAddressRequest = new DeleteMultipleAddressRequest();
        String orgId = setUpMultipleContactAddOrganisationData();
        List<ContactInformation> contactInformations = contactInformationRepository.findAll();
        deleteMultipleAddressRequest.setAddressId(contactInformations.stream().limit(2).map(ci -> ci.getId().toString())
                .collect(Collectors.toSet()));
        Map<String, Object> addressResponse =
                professionalReferenceDataClient
                        .deleteContactInformationAddressOfOrganisation(
                                deleteMultipleAddressRequest,
                                puiOrgManager,
                                userId);
        var addressId = contactInformations.stream().limit(2).map(ci -> ci.getId().toString())
                .collect(Collectors.toSet());
        Organisation organisationAfterDeletion = organisationRepository.findByOrganisationIdentifier(orgId);
        assertThat(organisationAfterDeletion.getContactInformation().size()).isEqualTo(1);
        assertThat(organisationAfterDeletion.getContactInformation().stream()
                .noneMatch(ci -> addressId.contains(ci.getId().toString()))).isTrue();
        assertThat(addressResponse).isNotNull();
        assertThat(addressResponse).containsEntry("http_status", "204 NO_CONTENT");
    }

    @Test
    @SuppressWarnings("unchecked")
    void test_delete_three_contactInformation_from_two_org_address() {
        String orgId = setUpMultipleContactAddOrganisationData();
        var deleteMultipleAddressRequest = new DeleteMultipleAddressRequest();
        List<ContactInformation> contactInformations = contactInformationRepository.findAll();
        var addressId = contactInformations.stream()
                .map(ci -> ci.getId().toString()).collect(Collectors.toSet());
        deleteMultipleAddressRequest.setAddressId(addressId);
        Map<String, Object> addressResponse =
                professionalReferenceDataClient
                        .deleteContactInformationAddressOfOrganisation(
                                deleteMultipleAddressRequest,
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
