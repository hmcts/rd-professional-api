package uk.gov.hmcts.reform.professionalapi;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.professionalapi.controller.request.ContactInformationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.util.AuthorizationEnabledIntegrationTest;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.professionalapi.helper.OrganisationFixtures.createContactInformationRequest;
import static uk.gov.hmcts.reform.professionalapi.helper.OrganisationFixtures.createContactInformationRequestWithOnlyDxAddress;
import static uk.gov.hmcts.reform.professionalapi.helper.OrganisationFixtures.createContactInformationRequestWithoutDxAddress;
import static uk.gov.hmcts.reform.professionalapi.helper.OrganisationFixtures.getContactInformationList;
import static uk.gov.hmcts.reform.professionalapi.helper.OrganisationFixtures.getContactInformationListWithoutDxAddress;
import static uk.gov.hmcts.reform.professionalapi.helper.OrganisationFixtures.organisationRequestWithAllFieldsAreUpdated;
import static uk.gov.hmcts.reform.professionalapi.helper.OrganisationFixtures.organisationRequestWithMultipleAddressAllFields;

class UpdateOrgContactInformationIntegrationTest extends AuthorizationEnabledIntegrationTest {

    @Test
    void shouldReturn200WhenUpdateSingleAddressWithDxAddress() {
        String organisationIdentifier = createOrganisationRequest();
        ContactInformationCreationRequest contactInformationCreationRequest =
            createContactInformationRequest().build();

        Map<String, Object> updateResponse = professionalReferenceDataClient
            .updateOrgContactInformation(contactInformationCreationRequest,
                hmctsAdmin,organisationIdentifier,true,true,"");

        assertThat(updateResponse).containsEntry("http_status", 200);

        java.util.Map<String, Object> retrieveOrganisationResponse = professionalReferenceDataClient
            .retrieveSingleOrganisation(organisationIdentifier, hmctsAdmin);

        List existingContacts = (List)retrieveOrganisationResponse.get("contactInformation");
        LinkedHashMap existing = (LinkedHashMap)existingContacts.get(0);

        assertThat(existing.get("addressLine1").toString())
            .isEqualTo(contactInformationCreationRequest.getAddressLine1());
        assertThat(existing.get("addressLine2").toString())
            .isEqualTo(contactInformationCreationRequest.getAddressLine2());
        assertThat(existing.get("addressLine3").toString())
            .isEqualTo(contactInformationCreationRequest.getAddressLine3());
        assertThat(existing.get("country").toString())
            .isEqualTo(contactInformationCreationRequest.getCountry());
        assertThat(existing.get("postCode").toString())
            .isEqualTo(contactInformationCreationRequest.getPostCode());

        deleteOrganisation(organisationIdentifier);
    }

    @Test
    void shouldReturn400WhenContactInformationRequestIsNull() {
        String organisationIdentifier = createOrganisationRequest();
        Map<String, Object> updateResponse = professionalReferenceDataClient
            .updateOrgContactInformation(null,  hmctsAdmin,organisationIdentifier,
                true,true,"");

        assertThat(updateResponse).containsEntry("http_status", "400");
        deleteOrganisation(organisationIdentifier);
    }

    @Test
    void shouldReturn400InvalidDatainRequest() {
        String organisationIdentifier = createOrganisationRequest();
        ContactInformationCreationRequest contactInformationCreationRequest =
            createContactInformationRequest().addressLine1(null).build();

        Map<String, Object> updateResponse = professionalReferenceDataClient
            .updateOrgContactInformation(contactInformationCreationRequest,
                hmctsAdmin,organisationIdentifier,true,true,"");

        assertThat(updateResponse).containsEntry("http_status", "400");
        assertThat(updateResponse.get("response_body").toString()).contains(
            "Field error in object 'contactInformationCreationRequest' on field 'addressLine1'");
        deleteOrganisation(organisationIdentifier);
    }

    @Test
    void shouldReturn200WhenUpdateMultipleAddressesWithoutDxAddress() {

        List<ContactInformationCreationRequest> contactInformationCreationRequest =
            getContactInformationListWithoutDxAddress();

        java.util.Map<String, Object> responseForOrganisationCreation = professionalReferenceDataClient
            .createOrganisation(organisationRequestWithMultipleAddressAllFields().build());
        String orgIdentifier = (String)responseForOrganisationCreation.get(ORG_IDENTIFIER);
        java.util.Map<String, Object> retrieveOrganisationResponse = professionalReferenceDataClient
            .retrieveSingleOrganisation(orgIdentifier, hmctsAdmin);

        List existingContacts = (List)retrieveOrganisationResponse.get("contactInformation");
        LinkedHashMap existing = (LinkedHashMap)existingContacts.get(0);
        String addressId = existing.get("addressId").toString();

        Map<String, Object> updateResponse = professionalReferenceDataClient
            .updateOrgContactInformation(contactInformationCreationRequest.get(0),
                hmctsAdmin,orgIdentifier,false,true,
                addressId);

        java.util.Map<String, Object> retrieveOrganisationResponseUpdated = professionalReferenceDataClient
            .retrieveSingleOrganisation(orgIdentifier, hmctsAdmin);

        List existingContactsUpdated = (List)retrieveOrganisationResponseUpdated.get("contactInformation");
        LinkedHashMap existingUpdated = (LinkedHashMap)existingContactsUpdated.get(0);

        assertThat(updateResponse).containsEntry("http_status", 200);

        assertThat(existingUpdated.get("addressLine1").toString())
            .isEqualTo(contactInformationCreationRequest.get(0).getAddressLine1());
        assertThat(existingUpdated.get("addressLine2").toString())
            .isEqualTo(contactInformationCreationRequest.get(0).getAddressLine2());
        assertThat(existingUpdated.get("addressLine3").toString())
            .isEqualTo(contactInformationCreationRequest.get(0).getAddressLine3());
        assertThat(existingUpdated.get("country").toString())
            .isEqualTo(contactInformationCreationRequest.get(0).getCountry());
        assertThat(existingUpdated.get("postCode").toString())
            .isEqualTo(contactInformationCreationRequest.get(0).getPostCode());

        deleteOrganisation(orgIdentifier);
    }

    @Test
    void shouldReturn200WhenUpdateMultipleAddressesWithDxAddress() {

        List<ContactInformationCreationRequest> contactInformationCreationRequest =
            getContactInformationList();

        java.util.Map<String, Object> responseForOrganisationCreation = professionalReferenceDataClient
            .createOrganisation(organisationRequestWithMultipleAddressAllFields().build());
        String orgIdentifier = (String)responseForOrganisationCreation.get(ORG_IDENTIFIER);
        java.util.Map<String, Object> retrieveOrganisationResponse = professionalReferenceDataClient
            .retrieveSingleOrganisation(orgIdentifier, hmctsAdmin);
        List existingContacts = (List)retrieveOrganisationResponse.get("contactInformation");
        LinkedHashMap existing = (LinkedHashMap)existingContacts.get(0);

        Map<String, Object> updateResponse = professionalReferenceDataClient
            .updateOrgContactInformation(contactInformationCreationRequest.get(0),
                hmctsAdmin,orgIdentifier,true,true,
                existing.get("addressId").toString()
                );

        java.util.Map<String, Object> retrieveOrganisationResponseAfterUpdate = professionalReferenceDataClient
            .retrieveSingleOrganisation(orgIdentifier, hmctsAdmin);
        List existingContactsUpdated = (List)retrieveOrganisationResponseAfterUpdate.get("contactInformation");
        LinkedHashMap existingUpdated = (LinkedHashMap)existingContactsUpdated.get(0);
        List dxAddressesUpdated = (List)existingUpdated.get("dxAddress");
        LinkedHashMap dxAdd = (LinkedHashMap)dxAddressesUpdated.get(1);
        String dxNumber = (String)dxAdd.get("dxNumber").toString();
        String dxExchange = (String)dxAdd.get("dxExchange").toString();

        assertThat(updateResponse).containsEntry("http_status", 200);
        assertThat(dxNumber).isEqualTo(contactInformationCreationRequest.get(0)
                .getDxAddress().get(0).getDxNumber());
        assertThat(dxExchange).isEqualTo(contactInformationCreationRequest.get(0)
                .getDxAddress().get(0).getDxExchange());


        deleteOrganisation(orgIdentifier);
    }


    @Test
    void shouldReturn200WhenUpdateSingleAddressWithoutDxAddress() {
        ContactInformationCreationRequest contactInformationCreationRequest =
            createContactInformationRequestWithoutDxAddress().build();


        java.util.Map<String, Object> responseForOrganisationCreation = professionalReferenceDataClient
            .createOrganisation(organisationRequestWithAllFieldsAreUpdated().build());
        String orgIdentifier = (String)responseForOrganisationCreation.get(ORG_IDENTIFIER);

        Map<String, Object> updateResponse = professionalReferenceDataClient
            .updateOrgContactInformation(contactInformationCreationRequest,
                hmctsAdmin,orgIdentifier,false,
                true,"");

        assertThat(updateResponse).containsEntry("http_status", 200);

        java.util.Map<String, Object> retrieveOrganisationResponseAfterUpdate = professionalReferenceDataClient
            .retrieveSingleOrganisation(orgIdentifier, hmctsAdmin);
        List existingContactsUpdated = (List)retrieveOrganisationResponseAfterUpdate.get("contactInformation");
        LinkedHashMap existingUpdated = (LinkedHashMap)existingContactsUpdated.get(0);

        assertThat(existingUpdated.get("addressLine1").toString())
            .isEqualTo(contactInformationCreationRequest.getAddressLine1());
        assertThat(existingUpdated.get("addressLine2").toString())
            .isEqualTo(contactInformationCreationRequest.getAddressLine2());
        assertThat(existingUpdated.get("addressLine3").toString())
            .isEqualTo(contactInformationCreationRequest.getAddressLine3());
        assertThat(existingUpdated.get("country").toString())
            .isEqualTo(contactInformationCreationRequest.getCountry());
        assertThat(existingUpdated.get("postCode").toString())
            .isEqualTo(contactInformationCreationRequest.getPostCode());

        deleteOrganisation(orgIdentifier);
    }

    @Test
    void shoudlReturn200ForSingleRecordDxAddressGivenNoOtherAddressChange() {
        ContactInformationCreationRequest contactInformationCreationRequest =
            createContactInformationRequestWithOnlyDxAddress().build();

        java.util.Map<String, Object> responseForOrganisationCreation = professionalReferenceDataClient
            .createOrganisation(organisationRequestWithAllFieldsAreUpdated().build());
        String orgIdentifier = (String)responseForOrganisationCreation.get(ORG_IDENTIFIER);

        Map<String, Object> updateResponse = professionalReferenceDataClient
            .updateOrgContactInformation(contactInformationCreationRequest,
                hmctsAdmin,orgIdentifier,true,
                false,"");

        java.util.Map<String, Object> retrieveOrganisationResponseAfterUpdate = professionalReferenceDataClient
            .retrieveSingleOrganisation(orgIdentifier, hmctsAdmin);
        List existingContactsUpdated = (List)retrieveOrganisationResponseAfterUpdate.get("contactInformation");
        LinkedHashMap existingUpdated = (LinkedHashMap)existingContactsUpdated.get(0);
        List dxAddressesUpdated = (List)existingUpdated.get("dxAddress");
        LinkedHashMap dxAdd = (LinkedHashMap)dxAddressesUpdated.get(1);
        String dxNumber = (String)dxAdd.get("dxNumber").toString();
        String dxExchange = (String)dxAdd.get("dxExchange").toString();

        assertThat(updateResponse).containsEntry("http_status", 200);
        assertThat(dxNumber).isEqualTo(contactInformationCreationRequest
            .getDxAddress().get(0).getDxNumber());
        assertThat(dxExchange).isEqualTo(contactInformationCreationRequest
            .getDxAddress().get(0).getDxExchange());
        deleteOrganisation(orgIdentifier);
    }

    @Test
    void shouldReturn400ForNonExistingOrganisation() {
        ContactInformationCreationRequest contactInformationCreationRequest =
            createContactInformationRequest().build();

        Map<String, Object> updateResponse = professionalReferenceDataClient
            .updateOrgContactInformation(contactInformationCreationRequest,  hmctsAdmin,
                "ABCDEF7",true,true,"");

        assertThat(updateResponse).containsEntry("http_status", "404");
        assertThat(updateResponse.get("response_body").toString())
            .contains("errorMessage\":\"4 : Resource not found\",\"errorDescription\":\"Organisation does not exist");

    }

    @Test
    void shoudlReturn400ForInvalidContactInformation() {
        String organisationIdentifier = createOrganisationRequest();
        Map<String, Object> updateResponse = professionalReferenceDataClient
            .updateOrgContactInformation(new ContactInformationCreationRequest(null,null,
                null,null,null,null,null,
                null,null), hmctsAdmin,organisationIdentifier,true,
                true,"");

        assertThat(updateResponse).containsEntry("http_status", "400");
        assertThat(updateResponse.get("response_body").toString())
            .contains("validation on an argument failed");
        deleteOrganisation(organisationIdentifier);
    }

    @Test
    void shoudlReturn400ForMissingDxAddress() {
        ContactInformationCreationRequest contactInformationCreationRequest =
            createContactInformationRequestWithoutDxAddress().build();
        String organisationIdentifier = createOrganisationRequest();
        Map<String, Object> updateResponse = professionalReferenceDataClient
            .updateOrgContactInformation(contactInformationCreationRequest, hmctsAdmin,organisationIdentifier,
                true,true,"");

        assertThat(updateResponse).containsEntry("http_status", "500");
        assertThat(updateResponse.get("response_body").toString())
            .contains("Cannot invoke \\\"java.util.List.isEmpty()\\\" because \\\"dxAddressList\\\" is null");
        deleteOrganisation(organisationIdentifier);
    }


    public void deleteOrganisation(String orgIdentifier) {
        Map<String, Object> deleteResponse = professionalReferenceDataClient.deleteOrganisation(hmctsAdmin,
            orgIdentifier);

        assertThat(deleteResponse.get("http_status")).isEqualTo(204);
    }

}
