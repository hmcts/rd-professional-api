package uk.gov.hmcts.reform.professionalapi;

import org.bouncycastle.asn1.cryptopro.CryptoProObjectIdentifiers;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.jcajce.provider.symmetric.util.PBE;
import org.bouncycastle.util.Integers;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.professionalapi.controller.request.ContactInformationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.domain.DxAddress;
import uk.gov.hmcts.reform.professionalapi.util.AuthorizationEnabledIntegrationTest;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.professionalapi.helper.OrganisationFixtures.contactInformationWithOnlyAddressLine3Changed;
import static uk.gov.hmcts.reform.professionalapi.helper.OrganisationFixtures.contactInformationWithOnlyDxExchangeChanged;
import static uk.gov.hmcts.reform.professionalapi.helper.OrganisationFixtures.contactInformationWithOnlyDxNumberChanged;
import static uk.gov.hmcts.reform.professionalapi.helper.OrganisationFixtures.createContactInformationRequest;
import static uk.gov.hmcts.reform.professionalapi.helper.OrganisationFixtures.createContactInformationRequestWithOnlyDxAddress;
import static uk.gov.hmcts.reform.professionalapi.helper.OrganisationFixtures.createContactInformationRequestWithoutDxAddress;
import static uk.gov.hmcts.reform.professionalapi.helper.OrganisationFixtures.getContactInformationList;
import static uk.gov.hmcts.reform.professionalapi.helper.OrganisationFixtures.getContactInformationWithoutDxAddress;
import static uk.gov.hmcts.reform.professionalapi.helper.OrganisationFixtures.organisationRequestWithAllFields;
import static uk.gov.hmcts.reform.professionalapi.helper.OrganisationFixtures.organisationRequestWithAllFieldsAreUpdated;


class UpdateOrgContactInformationIntegrationTest extends AuthorizationEnabledIntegrationTest {

    @Test
    void shouldReturn200WhenUpdateSingleAddressWithDxAddress() {

        String organisationIdentifier = createActiveOrganisation();

        ContactInformationCreationRequest contactInformationCreationRequest =
            createContactInformationRequest().build();

        Map<String, Object> updateResponse = professionalReferenceDataClient
            .updateOrgContactInformation(contactInformationCreationRequest,
                hmctsAdmin,organisationIdentifier,true,true,null);

        assertThat(updateResponse).containsEntry("http_status", 200);

        java.util.Map<String, Object> retrieveOrganisationResponse = professionalReferenceDataClient
            .retrieveSingleOrganisation(organisationIdentifier, hmctsAdmin);

        List savedContacts = (List)retrieveOrganisationResponse.get("contactInformation");
        LinkedHashMap savedContactInformation = (LinkedHashMap)savedContacts.get(0);

        assertThat(savedContactInformation.get("addressLine1").toString())
            .isEqualTo(contactInformationCreationRequest.getAddressLine1());
        assertThat(savedContactInformation.get("addressLine2").toString())
            .isEqualTo(contactInformationCreationRequest.getAddressLine2());
        assertThat(savedContactInformation.get("addressLine3").toString())
            .isEqualTo(contactInformationCreationRequest.getAddressLine3());
        assertThat(savedContactInformation.get("country").toString())
            .isEqualTo(contactInformationCreationRequest.getCountry());
        assertThat(savedContactInformation.get("postCode").toString())
            .isEqualTo(contactInformationCreationRequest.getPostCode());

        List dxAdd = (List)savedContactInformation.get("dxAddress");
        LinkedHashMap dxAddress = (LinkedHashMap)dxAdd.get(1);

        assertThat((String)dxAddress.get("dxExchange"))
            .isEqualTo(contactInformationCreationRequest.getDxAddress().get(0).getDxExchange());
        assertThat((String)dxAddress.get("dxNumber"))
            .isEqualTo(contactInformationCreationRequest.getDxAddress().get(0).getDxNumber());
    }

    @Test
    void shouldReturn400WhenContactInformationRequestIsNull() {
        String organisationIdentifier = createActiveOrganisation();
        Map<String, Object> updateResponse = professionalReferenceDataClient
            .updateOrgContactInformation(null,  hmctsAdmin,organisationIdentifier,
                true,true,null);

        assertThat(updateResponse).containsEntry("http_status", "400");
        assertThat(updateResponse.get("response_body").toString()).contains("Required request body is missing");

    }

    @Test
    void shouldReturn400WhenAddressLine1IsNull() {
        String organisationIdentifier = createActiveOrganisation();
        ContactInformationCreationRequest contactInformationCreationRequest = new ContactInformationCreationRequest(
            null, null, null, null, null, null,
            null, null,null);
        Map<String, Object> updateResponse = professionalReferenceDataClient
            .updateOrgContactInformation(contactInformationCreationRequest, hmctsAdmin,organisationIdentifier,
                true,true,null);

        assertThat(updateResponse).containsEntry("http_status", "400");
        assertThat(updateResponse.get("response_body").toString()).contains("AddressLine1 is required");

    }


    @Test
    void shouldReturn200WhenUpdateMultipleAddressesWithoutDxAddress() {

        ContactInformationCreationRequest contactInformationCreationRequest = getContactInformationWithoutDxAddress();

        Map ids = getAddressIdFromSavedCreatedOrg();

        Map<String, Object> updateResponse = professionalReferenceDataClient
            .updateOrgContactInformation(contactInformationCreationRequest,
                hmctsAdmin,ids.get("activeOrgId").toString(),false,true,
                ids.get("addressId").toString());
        assertThat(updateResponse).containsEntry("http_status", 200);

        java.util.Map<String, Object> retrieveSavedOrganisationResponseUpdated = professionalReferenceDataClient
            .retrieveSingleOrganisation(ids.get("activeOrgId").toString(), hmctsAdmin);

        List existingContactsUpdated = (List)retrieveSavedOrganisationResponseUpdated.get("contactInformation");
        LinkedHashMap savedUpdated = (LinkedHashMap)existingContactsUpdated.get(0);

        assertThat(savedUpdated.get("addressLine1").toString())
            .isEqualTo(contactInformationCreationRequest.getAddressLine1());
        assertThat(savedUpdated.get("addressLine2").toString())
            .isEqualTo(contactInformationCreationRequest.getAddressLine2());
        assertThat(savedUpdated.get("addressLine3").toString())
            .isEqualTo(contactInformationCreationRequest.getAddressLine3());
        assertThat(savedUpdated.get("country").toString())
            .isEqualTo(contactInformationCreationRequest.getCountry());
        assertThat(savedUpdated.get("postCode").toString())
            .isEqualTo(contactInformationCreationRequest.getPostCode());
        assertThat(savedUpdated.get("county").toString())
            .isEqualTo(contactInformationCreationRequest.getCounty());

    }

    @Test
    void shouldReturn200WhenUpdateMultipleAddressesWithDxAddress() {

        List<ContactInformationCreationRequest> contactInformationCreationRequest =
            getContactInformationList();
        Map ids = getAddressIdFromSavedCreatedOrg();

        Map<String, Object> updateResponse = professionalReferenceDataClient
            .updateOrgContactInformation(contactInformationCreationRequest.get(0),
                hmctsAdmin,ids.get("activeOrgId").toString(),true,true,
                ids.get("addressId").toString());

        java.util.Map<String, Object> retrieveOrganisationResponseAfterUpdate = professionalReferenceDataClient
            .retrieveSingleOrganisation(ids.get("activeOrgId").toString(), hmctsAdmin);
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

    }


    @Test
    void shouldReturn200WhenUpdateSingleAddressWithoutDxAddress() {
        ContactInformationCreationRequest contactInformationCreationRequest =
            createContactInformationRequestWithoutDxAddress().build();

        String orgIdentifier = createActiveOrganisation();

        Map<String, Object> updateResponse = professionalReferenceDataClient
            .updateOrgContactInformation(contactInformationCreationRequest,
                hmctsAdmin,orgIdentifier,false,
                true,null);

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

    }

    @Test
    void shoudlReturn200ForSingleRecordDxAddressGivenNoOtherAddressChange() {
        ContactInformationCreationRequest contactInformationCreationRequest =
            createContactInformationRequestWithOnlyDxAddress().build();

        String orgIdentifier = createActiveOrganisation();
        Map<String, Object> updateResponse = professionalReferenceDataClient
            .updateOrgContactInformation(contactInformationCreationRequest,
                hmctsAdmin,orgIdentifier,true,
                false,null);

        assertThat(updateResponse).containsEntry("http_status", 200);

        java.util.Map<String, Object> retrieveOrganisationResponseAfterUpdate = professionalReferenceDataClient
            .retrieveSingleOrganisation(orgIdentifier, hmctsAdmin);
        List existingContactsUpdated = (List)retrieveOrganisationResponseAfterUpdate.get("contactInformation");
        LinkedHashMap existingUpdated = (LinkedHashMap)existingContactsUpdated.get(0);
        List dxAddressesUpdated = (List)existingUpdated.get("dxAddress");
        LinkedHashMap savedDxAdd = (LinkedHashMap)dxAddressesUpdated.get(1);

        assertThat(savedDxAdd.get("dxNumber").toString()).isEqualTo(contactInformationCreationRequest
            .getDxAddress().get(0).getDxNumber());
        assertThat(savedDxAdd.get("dxExchange").toString()).isEqualTo(contactInformationCreationRequest
            .getDxAddress().get(0).getDxExchange());

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
            .contains("No Organisation was found with the given organisationIdentifier");

    }

    @Test
    void shoudlReturn400ForInvalidContactInformation() {
        String organisationIdentifier = createActiveOrganisation();
        Map<String, Object> updateResponse = professionalReferenceDataClient
            .updateOrgContactInformation(new ContactInformationCreationRequest(null,null,
                null,null,null,null,null,
                null,null), hmctsAdmin,organisationIdentifier,true,
                true,"");

        assertThat(updateResponse).containsEntry("http_status", "400");
        assertThat(updateResponse.get("response_body").toString())
            .contains("validation on an argument failed");

    }

    @Test
    void shoudlReturn400ForMissingDxAddress() {
        ContactInformationCreationRequest contactInformationCreationRequest =
            createContactInformationRequestWithoutDxAddress().build();
        String orgIdentifier = createActiveOrganisation();
        Map<String, Object> updateResponse = professionalReferenceDataClient
            .updateOrgContactInformation(contactInformationCreationRequest, hmctsAdmin,orgIdentifier,
                true,true,null);

        assertThat(updateResponse).containsEntry("http_status", "400");
        assertThat(updateResponse.get("response_body").toString())
            .contains("Invalid Contact informationDX Number or DX Exchange cannot be empty");

    }
    //to update DX Exchange field alone
    @Test
    void shoudlReturn200ForOnlyDxNumberChange() {

        String orgIdentifier = createActiveOrganisation();
        java.util.Map<String, Object> retrieveCreatedOrganisationResponse = professionalReferenceDataClient
            .retrieveSingleOrganisation(orgIdentifier, hmctsAdmin);

        List existingContacts = (List)retrieveCreatedOrganisationResponse.get("contactInformation");
        LinkedHashMap existingContact = (LinkedHashMap)existingContacts.get(0);
        List dxAddresses = (List)existingContact.get("dxAddress");
        DxAddress existingDxAddress = (DxAddress)dxAddresses.get(0);

        ContactInformationCreationRequest contactInformationCreationRequest =
            contactInformationWithOnlyDxNumberChanged().build();
        Map<String, Object> updateResponse = professionalReferenceDataClient
            .updateOrgContactInformation(contactInformationCreationRequest,
                hmctsAdmin,orgIdentifier,true,
                false,null);
        assertThat(updateResponse).containsEntry("http_status", 200);


        java.util.Map<String, Object> retrieveOrganisationResponseAfterUpdate = professionalReferenceDataClient
            .retrieveSingleOrganisation(orgIdentifier, hmctsAdmin);
        List existingContactsUpdated = (List)retrieveOrganisationResponseAfterUpdate.get("contactInformation");
        LinkedHashMap existingUpdated = (LinkedHashMap)existingContactsUpdated.get(0);
        List dxAddressesUpdated = (List)existingUpdated.get("dxAddress");
        LinkedHashMap savedDxAdd = (LinkedHashMap)dxAddressesUpdated.get(1);

        assertThat(savedDxAdd.get("dxNumber").toString()).isEqualTo(contactInformationCreationRequest
            .getDxAddress().get(0).getDxNumber());
        assertThat(savedDxAdd.get("dxExchange").toString()).isEqualTo(existingDxAddress.getDxExchange());

    }

    @Test
    void shoudlReturn200ForOnlyDxExchangeChange() {
        ContactInformationCreationRequest contactInformationCreationRequest =
            contactInformationWithOnlyDxExchangeChanged().build();
        String orgIdentifier = createActiveOrganisation();
        java.util.Map<String, Object> retrieveSavedOrganisationResponse = professionalReferenceDataClient
            .retrieveSingleOrganisation(orgIdentifier, hmctsAdmin);

        List existingSavedContacts = (List)retrieveSavedOrganisationResponse.get("contactInformation");
        LinkedHashMap existingSavedContact = (LinkedHashMap)existingSavedContacts.get(0);
        List dxAddresses = (List)existingSavedContact.get("dxAddress");
        DxAddress existingDxAddress = (DxAddress)dxAddresses.get(0);
        Map<String, Object> updateResponse = professionalReferenceDataClient
            .updateOrgContactInformation(contactInformationCreationRequest,
                hmctsAdmin,orgIdentifier,true,
                false,null);

        assertThat(updateResponse).containsEntry("http_status", 200);

        java.util.Map<String, Object> retrieveOrganisationResponseAfterUpdate = professionalReferenceDataClient
            .retrieveSingleOrganisation(orgIdentifier, hmctsAdmin);
        List existingContactsUpdated = (List)retrieveOrganisationResponseAfterUpdate.get("contactInformation");
        LinkedHashMap existingUpdated = (LinkedHashMap)existingContactsUpdated.get(0);
        List dxAddressesUpdated = (List)existingUpdated.get("dxAddress");
        LinkedHashMap savedDxAdd = (LinkedHashMap)dxAddressesUpdated.get(1);

        assertThat(savedDxAdd.get("dxExchange").toString()).isEqualTo(contactInformationCreationRequest
            .getDxAddress().get(0).getDxExchange());
        assertThat(savedDxAdd.get("dxNumber").toString()).isEqualTo(existingDxAddress.getDxNumber());

    }

    void shoudlReturn200ForOnlyAddressLine3Change() {
        ContactInformationCreationRequest contactInformationCreationRequest =
            contactInformationWithOnlyAddressLine3Changed().build();
        String orgIdentifier = createActiveOrganisation();

        java.util.Map<String, Object> retrieveSavedOrganisationResponse = professionalReferenceDataClient
            .retrieveSingleOrganisation(orgIdentifier, hmctsAdmin);

        List existingCreatedContacts = (List)retrieveSavedOrganisationResponse.get("contactInformation");
        LinkedHashMap existingCreatedContact = (LinkedHashMap)existingCreatedContacts.get(0);
        List dxAddresses = (List)existingCreatedContact.get("dxAddress");
        DxAddress existingDxAddress = (DxAddress)dxAddresses.get(0);

        Map<String, Object> updateResponse = professionalReferenceDataClient
            .updateOrgContactInformation(contactInformationCreationRequest,
                hmctsAdmin,orgIdentifier,false,
                true,null);

        assertThat(updateResponse).containsEntry("http_status", 200);

        java.util.Map<String, Object> retrieveOrganisationResponseAfterUpdate = professionalReferenceDataClient
            .retrieveSingleOrganisation(orgIdentifier, hmctsAdmin);
        List existingContactsUpdated = (List)retrieveOrganisationResponseAfterUpdate.get("contactInformation");
        LinkedHashMap existingUpdated = (LinkedHashMap)existingContactsUpdated.get(0);
        List dxAddressesUpdated = (List)existingUpdated.get("dxAddress");
        LinkedHashMap savedDxAdd = (LinkedHashMap)dxAddressesUpdated.get(1);

        assertThat(existingUpdated.get("addressLine3").toString())
            .isEqualTo(contactInformationCreationRequest.getAddressLine3());


        assertThat(existingUpdated.get("addressLine1").toString())
            .isEqualTo(existingCreatedContact.get("addressLine1"));
        assertThat(existingUpdated.get("addressLine2").toString())
            .isEqualTo(existingCreatedContact.get("addressLine2"));
        assertThat(existingUpdated.get("country").toString())
            .isEqualTo(existingCreatedContact.get("country"));
        assertThat(existingUpdated.get("postCode").toString())
            .isEqualTo(existingCreatedContact.get("postCode"));
        assertThat(existingUpdated.get("county").toString())
            .isEqualTo(existingCreatedContact.get("county"));
        assertThat(savedDxAdd.get("dxExchange").toString()).isEqualTo(existingDxAddress.getDxExchange());
        assertThat(savedDxAdd.get("dxNumber").toString()).isEqualTo(existingDxAddress.getDxNumber());
    }


    @Test
    void shoudlReturn400missingDxAddress() {
        ContactInformationCreationRequest contactInformationCreationRequest =
            createContactInformationRequestWithoutDxAddress().build();
        String orgIdentifier = createActiveOrganisation();
        Map<String, Object> updateResponse = professionalReferenceDataClient
            .updateOrgContactInformation(contactInformationCreationRequest, hmctsAdmin,orgIdentifier,
                null,true,null);

        assertThat(updateResponse).containsEntry("http_status", "400");
        assertThat(updateResponse.get("response_body").toString())
            .contains("dxAddressUpdateRequired value is required");
    }
    //add below test cases for dx address update, sometimes we may have to update only one field:

    //to update DX Exchange field alone
    //to update DX address number alone
    //add test case to update single address details without dx address, AddressLine3 and assert updated address details
    //add test case for missing dxAddressUpdateRequired query param to update dx address and assert bad request with error message "dxAddressUpdateRequired value is required"

   private String createActiveOrganisation(){
       OrganisationCreationRequest organisationCreationRst = organisationRequestWithAllFields().build();
       return createAndActivateOrganisationWithGivenRequest(organisationCreationRst);

   }

   private Map getAddressIdFromSavedCreatedOrg(){
        String activeOrgId = createActiveOrganisation();
       java.util.Map<String, Object> retrieveOrganisationResponse = professionalReferenceDataClient
           .retrieveSingleOrganisation(activeOrgId, hmctsAdmin);

       List existingSavedContacts = (List)retrieveOrganisationResponse.get("contactInformation");
       LinkedHashMap existingSavedContact = (LinkedHashMap)existingSavedContacts.get(0);
       Map ids = new HashMap();
       ids.put("activeOrgId", activeOrgId);
       ids.put("addressId", existingSavedContact.get("addressId").toString());
       return ids;
   }

}
