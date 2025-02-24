package uk.gov.hmcts.reform.professionalapi;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.UpdateContactInformationRequest;
import uk.gov.hmcts.reform.professionalapi.util.AuthorizationEnabledIntegrationTest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.professionalapi.helper.OrganisationFixtures.someMinimalOrganisationRequest;

class UpdateContactInformationIntegrationTest extends AuthorizationEnabledIntegrationTest {

    @Test
    void update_contact_information_organisation_should_return_success() {
        //create request to update organisation
        UpdateContactInformationRequest updateContactInformationRequest =
            new UpdateContactInformationRequest("UPRN1",
                "U-addressLine1","U-addressLine2","U-addressLine3",
                "U-townCity","U-county","U-country","U-postCode",
                "","");
        //create organisation
        String orgId = getActiveOrganisationId();
        String userId = getUserId(orgId);
        //updateName
        Map<String, Object> response = professionalReferenceDataClient.updateContactInformation(
            userId,updateContactInformationRequest,hmctsAdmin);

        assertThat(response.get("http_status")).isEqualTo("204 NO_CONTENT");

        //retrieve saved org to verify
        Map<String,Object> responseBody = retrievedSavedOrg(orgId);
        ArrayList<LinkedHashMap<String, Object>> contacts
            = (ArrayList<LinkedHashMap<String, Object>>)responseBody.get("contactInformation");
        assertThat(contacts.get(0).get("addressLine1")).isNotNull().isEqualTo("U-addressLine1");
        assertThat(contacts.get(0).get("addressLine2")).isNotNull().isEqualTo("U-addressLine2");
        assertThat(contacts.get(0).get("addressLine3")).isNotNull().isEqualTo("U-addressLine3");
        assertThat(contacts.get(0).get("uprn")).isNotNull().isEqualTo("UPRN1");
        assertThat(contacts.get(0).get("townCity")).isNotNull().isEqualTo("U-townCity");
        assertThat(contacts.get(0).get("country")).isNotNull().isEqualTo("U-country");
        assertThat(contacts.get(0).get("county")).isNotNull().isEqualTo("U-county");
        assertThat(contacts.get(0).get("postCode")).isNotNull().isEqualTo("U-postCode");

        LocalDateTime updatedDate = LocalDateTime.parse(responseBody.get("lastUpdated").toString());
        assertThat(updatedDate.toLocalDate()).isEqualTo(LocalDate.now());

        deleteCreatedTestOrganisations(orgId);
    }

    @Test
    void update_contact_info_dxAddress_should_return_success() {
        //create request to update organisation
        UpdateContactInformationRequest updateContactInformationRequest =
            new UpdateContactInformationRequest("UPRN1",
                "U-addressLine1","U-addressLine2","U-addressLine3",
                "U-townCity","U-county","U-country","U-postCode",
                "dxNumberU","dxExchangeU");
        //create organisation
        String orgId = getActiveOrganisationId();
        String userId = getUserId(orgId);
        //updateName
        Map<String, Object> response = professionalReferenceDataClient.updateContactInformation(
            userId,updateContactInformationRequest,hmctsAdmin);

        assertThat(response.get("http_status")).isEqualTo("204 NO_CONTENT");

        //retrieve saved org to verify
        Map<String,Object> responseBody = retrievedSavedOrg(orgId);
        ArrayList<LinkedHashMap<String, Object>> contacts
            = (ArrayList<LinkedHashMap<String, Object>>)responseBody.get("contactInformation");
        assertThat(contacts.get(0).get("addressLine1")).isNotNull().isEqualTo("U-addressLine1");
        assertThat(contacts.get(0).get("addressLine2")).isNotNull().isEqualTo("U-addressLine2");
        assertThat(contacts.get(0).get("addressLine3")).isNotNull().isEqualTo("U-addressLine3");
        assertThat(contacts.get(0).get("uprn")).isNotNull().isEqualTo("UPRN1");
        assertThat(contacts.get(0).get("townCity")).isNotNull().isEqualTo("U-townCity");
        assertThat(contacts.get(0).get("country")).isNotNull().isEqualTo("U-country");
        assertThat(contacts.get(0).get("county")).isNotNull().isEqualTo("U-county");
        assertThat(contacts.get(0).get("postCode")).isNotNull().isEqualTo("U-postCode");

        ArrayList dxAddresses = (ArrayList)contacts.get(0).get("dxAddress");
        LinkedHashMap dxAddressMap = ( LinkedHashMap) dxAddresses.get(0);
        assertThat(dxAddressMap.get("dxNumber")).isNotNull().isEqualTo("dxNumberU");
        assertThat(dxAddressMap.get("dxExchange")).isNotNull().isEqualTo("dxExchangeU");

        LocalDateTime updatedDate = LocalDateTime.parse(responseBody.get("lastUpdated").toString());
        assertThat(updatedDate.toLocalDate()).isEqualTo(LocalDate.now());

        deleteCreatedTestOrganisations(orgId);
    }

    @Test
    void update_of_contact_info_should_fail_if_addressline1_null() {
        //create request to update organisation
        UpdateContactInformationRequest updateContactInformationRequest =
            new UpdateContactInformationRequest("UPRN1",
                "","U-addressLine2","U-addressLine3",
                "U-townCity","U-county","U-country","U-postCode",
                "","");
        //create organisation
        String orgId = getActiveOrganisationId();
        String userId = getUserId(orgId);
        //updateName
        Map<String, Object> response = professionalReferenceDataClient.updateContactInformation(
            userId,updateContactInformationRequest,hmctsAdmin);

        assertThat(response).containsEntry("http_status", "400");
        assertThat(response.get("response_body").toString())
            .contains("AddressLine1 cannot be empty");
        deleteCreatedTestOrganisations(orgId);
    }

    @Test
    void update_of_contact_information_should_fail_if_uprn_invalid_lenght() {
        //create request to update organisation
        UpdateContactInformationRequest updateContactInformationRequest =
            new UpdateContactInformationRequest("UPRN16868678678678678678678678678678678678678678768678678678678",
                "U-addressline1","U-addressLine2","U-addressLine3",
                "U-townCity","U-county","U-country","U-postCode",
                "","");
        //create organisation
        String orgId = getActiveOrganisationId();
        String userId = getUserId(orgId);
        //updateName
        Map<String, Object> response = professionalReferenceDataClient.updateContactInformation(
            userId,updateContactInformationRequest,hmctsAdmin);

        assertThat(response).containsEntry("http_status", "400");
        assertThat(response.get("response_body").toString())
            .contains("Uprn must not be greater than 14 characters long");
        deleteCreatedTestOrganisations(orgId);
    }

    @Test
    void update_contactInfo_dxAddress_when_dxNum_empty_failure() {
        //create request to update organisation
        UpdateContactInformationRequest updateContactInformationRequest =
            new UpdateContactInformationRequest("UPRN1",
                "U-addressline1","U-addressLine2","U-addressLine3",
                "U-townCity","U-county","U-country","U-postCode",
                "dxNumberU","");
        //create organisation
        String orgId = getActiveOrganisationId();
        String userId = getUserId(orgId);
        //updateName
        Map<String, Object> response = professionalReferenceDataClient.updateContactInformation(
            userId,updateContactInformationRequest,hmctsAdmin);

        assertThat(response).containsEntry("http_status", "400");
        assertThat(response.get("response_body").toString())
            .contains("Organisation dxExchange cannot be empty or null");
        deleteCreatedTestOrganisations(orgId);
    }

    @Test
    void update_contactInfo_dxAddress_when_dxExchange_empty_failure() {
        //create request to update organisation
        UpdateContactInformationRequest updateContactInformationRequest =
            new UpdateContactInformationRequest("UPRN1",
                "U-addressline1","U-addressLine2","U-addressLine3",
                "U-townCity","U-county","U-country","U-postCode",
                "","dxExchangeU");
        //create organisation
        String orgId = getActiveOrganisationId();
        String userId = getUserId(orgId);
        //updateName
        Map<String, Object> response = professionalReferenceDataClient.updateContactInformation(
            userId,updateContactInformationRequest,hmctsAdmin);

        assertThat(response).containsEntry("http_status", "400");
        assertThat(response.get("response_body").toString())
            .contains("Organisation dxNumber cannot be empty or null");
        deleteCreatedTestOrganisations(orgId);
    }



    @Test
    void forbidden_acccess_should_return_failure() {
        //create request to update organisation
        UpdateContactInformationRequest updateContactInformationRequest =
            new UpdateContactInformationRequest("UPRN1",
                "U-addressline1","U-addressLine2","U-addressLine3",
                "U-townCity","U-county","U-country","U-postCode",
                "","dxExchangeU");
        //create organisation
        String orgId = getActiveOrganisationId();

        //updateName
        Map<String, Object> response = professionalReferenceDataClient.updateContactInformation(
            "sdfgsgdf",updateContactInformationRequest,hmctsAdmin);

        assertThat(response.get("http_status")).isEqualTo("400");
        assertThat(response.get("response_body")).toString().contains("Access Denied");


    }


    @Test
    void unauthorised_acccess_should_return_failure() {
        //create request to update organisation
        UpdateContactInformationRequest updateContactInformationRequest =
            new UpdateContactInformationRequest("UPRN1",
                "U-addressline1","U-addressLine2","U-addressLine3",
                "U-townCity","U-county","U-country","U-postCode",
                "","dxExchangeU");
        //create organisation
        String orgId = getActiveOrganisationId();
        String userId = getUserId(orgId);
        //updateName
        Map<String, Object> response = professionalReferenceDataClient.updateContactInformation(
            userId,updateContactInformationRequest,caseworkerCaa);

        assertThat(response.get("http_status")).isEqualTo("403");
        assertThat(response.get("response_body")).toString().contains("Access Denied");
        deleteCreatedTestOrganisations(orgId);
    }

    private String getActiveOrganisationId() {
        OrganisationCreationRequest organisationCreationRequest = someMinimalOrganisationRequest().build();
        String organisationIdentifier = createAndActivateOrganisationWithGivenRequest(organisationCreationRequest);
        updateOrganisation(organisationIdentifier, hmctsAdmin, "ACTIVE");

        return organisationIdentifier;
    }

    private String getUserId(String orgId) {
        String userId = updateOrgAndInviteUser(orgId, hmctsAdmin);
        assertThat(userId).isNotNull();
        return userId;
    }

    public Map<String,Object> retrievedSavedOrg(String orgId) {
        return  professionalReferenceDataClient.retrieveSingleOrganisation(orgId, hmctsAdmin);
    }

    public void deleteCreatedTestOrganisations(String orgId) {
        professionalReferenceDataClient.deleteOrganisation(hmctsAdmin,orgId);
    }
}
