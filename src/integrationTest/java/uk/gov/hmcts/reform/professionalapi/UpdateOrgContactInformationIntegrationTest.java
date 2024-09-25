package uk.gov.hmcts.reform.professionalapi;

import jdk.jfr.Description;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.professionalapi.controller.request.ContactInformationUpdateRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.util.AuthorizationEnabledIntegrationTest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang.RandomStringUtils.randomAlphabetic;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.professionalapi.controller.request.DxAddressCreationRequest.dxAddressCreationRequest;
import static uk.gov.hmcts.reform.professionalapi.helper.OrganisationFixtures.organisationRequestWithAllFields;
import static uk.gov.hmcts.reform.professionalapi.helper.OrganisationFixtures.organisationRequestWithMultipleContactInformations;
import static uk.gov.hmcts.reform.professionalapi.helper.OrganisationFixtures.someMinimalOrganisationRequest;


class UpdateOrgContactInformationIntegrationTest extends AuthorizationEnabledIntegrationTest {



    @Test
    @Description("Multiple Requests - each org id has single existing contact - update all successfully")
    void SingleExistingContactUpdateContactWithDxAddressForAllOrgsSuccess() {

        String orgId1 = createActiveOrganisationWithSingleContact();
        String orgId2 = createActiveOrganisationWithSingleContact();

        ContactInformationUpdateRequest contactInformationCreationRequest =
            createContactInformationUpdateRequestWithDxAddress(orgId1,"addressLine1",
                "addressLine3","uprn1",orgId2,"addLine1","addLine3","uprn2");

        Map<String, Object> updateResponse = professionalReferenceDataClient
            .updateOrgContactInformation(contactInformationCreationRequest,
                hmctsAdmin,true,true,null);

        assertThat(updateResponse).containsEntry("http_status", 200);
        LinkedHashMap responses = (LinkedHashMap)updateResponse.get("response_body");
        assertThat(responses.get("status")).isEqualTo("success");
        assertThat(responses.get("message")).isEqualTo("All contactInformations updated successfully");

        LinkedHashMap savedContactInformation1 = retrieveContacts(orgId1);
        LinkedHashMap savedContactInformation2 = retrieveContacts(orgId2);
        verifyContactInfo(savedContactInformation1,
            contactInformationCreationRequest.getContactInformationUpdateData().get(0));
        verifyDxAddress(savedContactInformation1,
            contactInformationCreationRequest.getContactInformationUpdateData().get(0));

        verifyContactInfo(savedContactInformation2,
            contactInformationCreationRequest.getContactInformationUpdateData().get(1));
        verifyDxAddress(savedContactInformation2,
            contactInformationCreationRequest.getContactInformationUpdateData().get(1));

        deleteCreatedTestOrganisations(orgId1,  orgId2);
    }


    @Test
    void shouldReturn400WhenContactInformationRequestIsNull() {
        Map<String, Object> updateResponse = professionalReferenceDataClient
            .updateOrgContactInformation(null,
                hmctsAdmin,true,true,null);

        assertThat(updateResponse).containsEntry("http_status", "400");
        assertThat(updateResponse.get("response_body").toString()).contains("Request is empty");
    }

    @Test
    @Description("Multiple Requests - 1st req has missing add line 1 - 2nd req has invalid uprn")
    void shouldReturn400WhenAddressLine1IsNullAndUprnInvalid() {
        String orgId1 = createActiveOrganisationWithSingleContact();
        String orgId2 = createActiveOrganisationWithSingleContact();

        ContactInformationUpdateRequest contactInformationCreationRequest =
            createContactInformationUpdateRequestWithDxAddress(orgId1,"",
                "addressLine3","uprn1",orgId2,null,"addLine3",
                randomAlphabetic(17));

        Map<String, Object> updateResponse = professionalReferenceDataClient
            .updateOrgContactInformation(contactInformationCreationRequest, hmctsAdmin,
                true,true,null);

        assertThat(updateResponse.get("status")).isEqualTo("failure");

        ArrayList responseList = (ArrayList)updateResponse.get("responses");
        LinkedHashMap firstResult  = (LinkedHashMap)responseList.get(0);
        LinkedHashMap secondResult  = (LinkedHashMap)responseList.get(1);
        assertThat(secondResult.get("organisationId")).isEqualTo(orgId1);
        assertThat(secondResult.get("status")).isEqualTo("failure");
        assertThat(secondResult.get("statusCode")).isEqualTo(400);
        assertThat(secondResult.get("message")).isEqualTo("AddressLine1 cannot be empty");

        assertThat(firstResult.get("organisationId")).isEqualTo(orgId2);
        assertThat(firstResult.get("status")).isEqualTo("failure");
        assertThat(firstResult.get("statusCode")).isEqualTo(400);
        assertThat(firstResult.get("message")).isEqualTo(
            "Uprn must not be greater than 14 characters long");

        deleteCreatedTestOrganisations(orgId1,  orgId2);
    }

    @Test
    @Description("Multiple Requests - 1st req has missing orgid - 2nd req has nonexisting orgid")
    void shouldReturn400ForNonExistingOrganisation() {
        String orgId2 = createActiveOrganisationWithSingleContact();

        ContactInformationUpdateRequest contactInformationCreationRequest =
            createContactInformationUpdateRequestWithDxAddress(null,"",
                "addressLine3","uprn1","ABCDEF7",null,"addLine3",
                randomAlphabetic(17));

        Map<String, Object> updateResponse = professionalReferenceDataClient
            .updateOrgContactInformation(contactInformationCreationRequest, hmctsAdmin,
                true,true,null);

        assertThat(updateResponse.get("status")).isEqualTo("failure");

        ArrayList responseList = (ArrayList)updateResponse.get("responses");
        LinkedHashMap firstResult  = (LinkedHashMap)responseList.get(0);
        assertThat(firstResult.get("status")).isEqualTo("failure");
        assertThat(firstResult.get("statusCode")).isEqualTo(400);
        assertThat(firstResult.get("message")).isEqualTo("Organisation id is missing");

        assertThat(firstResult.get("organisationId")).isEqualTo(orgId2);
        assertThat(firstResult.get("status")).isEqualTo("failure");
        assertThat(firstResult.get("statusCode")).isEqualTo(400);
        assertThat(firstResult.get("message")).isEqualTo(
            "No Organisation was found with the given organisationIdentifier");
        professionalReferenceDataClient.deleteOrganisation(hmctsAdmin,orgId2);
    }


    @Test
    @Description("Multiple Requests - Partial success -1st Dx add missing ,2nd DxAdd invalid , 3rd Success")
    void shouldReturn200WhenUpdateMissing_InvalidAndSuccessfullySavedDxAddress() {

        String orgId1 = createActiveOrganisationWithSingleContact();
        String orgId2 = createActiveOrganisationWithSingleContact();
        String orgId3 = createActiveOrganisationWithSingleContact();

        ContactInformationUpdateRequest contactInformationUpdateRequest = new ContactInformationUpdateRequest();
        List<ContactInformationUpdateRequest.ContactInformationUpdateData> contactInformationUpdateDataList
            = new ArrayList<>();
        contactInformationUpdateDataList.add(
            new ContactInformationUpdateRequest.ContactInformationUpdateData(
                orgId1,"uprn1","addressLine1",
                "addressLine2","addressLine3", "som1-town-city",
                "some-county1","some-country1","som1-post-code",null));
        contactInformationUpdateDataList.add(new ContactInformationUpdateRequest.ContactInformationUpdateData(
            orgId2,"uprn2","addLine1",
            "addLine2","addLine3", "som2-town-city",
            "some-county2","some-country2","som2-post-code",Arrays.asList
            (dxAddressCreationRequest().dxNumber(randomAlphabetic(17)).
                dxExchange(randomAlphabetic(17)).build())));

            ContactInformationUpdateRequest.ContactInformationUpdateData  contactInformationUpdateData =
                new ContactInformationUpdateRequest.ContactInformationUpdateData(
                orgId3,null,null,null,null,null,null,
                null,null,Arrays.asList(dxAddressCreationRequest()
                .dxNumber("DX 1234567890-1")
                .dxExchange("dxExchange-1").build()));
        contactInformationUpdateDataList.add(contactInformationUpdateData);
        contactInformationUpdateRequest.setContactInformationUpdateData(contactInformationUpdateDataList);

        Map<String, Object> updateResponse = professionalReferenceDataClient
            .updateOrgContactInformation(contactInformationUpdateRequest,
                hmctsAdmin,true,false,"addId");

        assertThat(updateResponse.get("status")).isEqualTo("partial_success");

        ArrayList responseList = (ArrayList)updateResponse.get("responses");
        LinkedHashMap firstResult  = (LinkedHashMap)responseList.get(0);
        assertThat(firstResult.get("status")).isEqualTo("failure");
        assertThat(firstResult.get("statusCode")).isEqualTo(400);
        assertThat(firstResult.get("message")).isEqualTo("DX Number or DX Exchange cannot be empty");

        LinkedHashMap secondResult  = (LinkedHashMap)responseList.get(1);
        assertThat(secondResult.get("organisationId")).isEqualTo(orgId2);
        assertThat(secondResult.get("status")).isEqualTo("failure");
        assertThat(secondResult.get("statusCode")).isEqualTo(400);
        assertThat(secondResult.get("message")).isEqualTo(
            "DX Number (max=13) or DX Exchange (max=40) has invalid length");

        LinkedHashMap thirdResult  = (LinkedHashMap)responseList.get(3);
        assertThat(thirdResult.get("organisationId")).isEqualTo(orgId3);
        assertThat(thirdResult.get("status")).isEqualTo("success");
        assertThat(thirdResult.get("statusCode")).isEqualTo(200);
        assertThat(thirdResult.get("message")).isEqualTo(
            "dxAddress updated successfully");
        LinkedHashMap savedContactInformation = retrieveContacts(orgId3);
        verifyDxAddress(savedContactInformation,contactInformationUpdateData);

        deleteCreatedTestOrganisations(orgId1,  orgId2);
    }

    @Test
    @Description("Multiple existing contacts for each req found but addressid is missing and dxadd and contact info" +
        " are both false")
    void shouldReturn400WhenMultipleContactsMissingAddId() {

        String orgId1 = createActiveOrganisationWithMultipleContact();
        String orgId2 = createActiveOrganisationWithSingleContact();

        ContactInformationUpdateRequest contactInformationCreationRequest =
            createContactInformationUpdateRequestWithDxAddress(orgId1,"addressLine1",
                "addressLine3","uprn1",orgId2,null,"addLine3",
                randomAlphabetic(17));

        Map<String, Object> updateResponse = professionalReferenceDataClient
            .updateOrgContactInformation(contactInformationCreationRequest, hmctsAdmin,
                false,false,null);

        ArrayList responseList = (ArrayList)updateResponse.get("responses");
        LinkedHashMap firstResult  = (LinkedHashMap)responseList.get(0);
        assertThat(firstResult.get("status")).isEqualTo("failure");
        assertThat(firstResult.get("organisationId")).isEqualTo(orgId1);
        assertThat(firstResult.get("statusCode")).isEqualTo(400);
        assertThat(firstResult.get("message")).isEqualTo(" Multiple addresses found for organisation . " +
            "Please enter specific address id of the contact information to be updated");
        LinkedHashMap secondResult  = (LinkedHashMap)responseList.get(1);
        assertThat(secondResult.get("status")).isEqualTo("failure");
        assertThat(secondResult.get("organisationId")).isEqualTo(orgId2);
        assertThat(secondResult.get("statusCode")).isEqualTo(400);
        assertThat(secondResult.get("message")).isEqualTo(" dxAddressUpdate and " +
            "contactInformationUpdate are both false . Cannot update contact information");

        deleteCreatedTestOrganisations(orgId1,  orgId2);

    }


    @Test
    @Description("Multiple Requests - 1st success only ContactInfo saved - 2nd fail AddLine1 missing")
    void shouldReturn200PartialSuccessWhenUpdateSingleAddressWithoutDxAddressChangeAndInActiveOrgId() {
        String orgId1 = createActiveOrganisationWithSingleContact();

        ContactInformationUpdateRequest contactInformationCreationRequest =
            createContactInformationUpdateRequestWithOutDxAddress(orgId1,"addressLine1",
                "addressLine3","uprn1","KRGPTFT",null,"addLine3","uprn2");

        Map<String, Object> updateResponse = professionalReferenceDataClient
            .updateOrgContactInformation(contactInformationCreationRequest,
                hmctsAdmin,false,true,null);

        assertThat(updateResponse).containsEntry("http_status", 200);
        LinkedHashMap responses = (LinkedHashMap)updateResponse.get("response_body");
        assertThat(responses.get("status")).isEqualTo("partial_success");

        ArrayList responseList = (ArrayList)updateResponse.get("responses");
        LinkedHashMap firstResult  = (LinkedHashMap)responseList.get(0);
        assertThat(firstResult.get("organisationId")).isEqualTo(orgId1);
        assertThat(firstResult.get("status")).isEqualTo("success");
        assertThat(firstResult.get("statusCode")).isEqualTo(200);
        assertThat(firstResult.get("message")).isEqualTo("contactInformation updated successfully");

        LinkedHashMap savedContactInformation1 = retrieveContacts(orgId1);

        verifyContactInfo(savedContactInformation1,
            contactInformationCreationRequest.getContactInformationUpdateData().get(0));
        verifyDxAddress(savedContactInformation1,
            contactInformationCreationRequest.getContactInformationUpdateData().get(0));

        LinkedHashMap secondResult  = (LinkedHashMap)responseList.get(1);
        assertThat(secondResult.get("organisationId")).isEqualTo("KRGPTFT");
        assertThat(secondResult.get("status")).isEqualTo("failure");
        assertThat(secondResult.get("statusCode")).isEqualTo(400);
        assertThat(secondResult.get("message")).isEqualTo("");

        professionalReferenceDataClient.deleteOrganisation(hmctsAdmin,orgId1);
    }



    //to update DX Exchange field alone
    @Test
    void shoudlReturn200ForOnlyDxNumberChangeOrOnlyDxExchange() {
        String orgId1 = createActiveOrganisationWithSingleContact();
        String orgId2 = createActiveOrganisationWithSingleContact();

        ContactInformationUpdateRequest contactInformationUpdateRequest = new ContactInformationUpdateRequest();
        List<ContactInformationUpdateRequest.ContactInformationUpdateData> contactInformationUpdateDataList
            = new ArrayList<>();
        ContactInformationUpdateRequest.ContactInformationUpdateData contactInformationUpdateData =
            new ContactInformationUpdateRequest.ContactInformationUpdateData(
                orgId1,null,null,null,null,null,null,
                null,null,Arrays.asList(dxAddressCreationRequest().dxNumber("DX 1234567890-1")
                .dxExchange("dxExchange").build()));
        ContactInformationUpdateRequest.ContactInformationUpdateData contactInformationUpdateData1 =
            new ContactInformationUpdateRequest.ContactInformationUpdateData(
                orgId2,null,null,null,null,null,null,
                null,null,Arrays.asList(dxAddressCreationRequest()
                .dxNumber("DX 1234567890")
                .dxExchange("dxExchange-1").build()));
        contactInformationUpdateDataList.add(contactInformationUpdateData);
            contactInformationUpdateDataList.add(contactInformationUpdateData1);
        contactInformationUpdateRequest.setContactInformationUpdateData(contactInformationUpdateDataList);

        Map<String, Object> updateResponse = professionalReferenceDataClient
            .updateOrgContactInformation(contactInformationUpdateRequest,
                hmctsAdmin,true,false,null);

        assertThat(updateResponse).containsEntry("http_status", 200);
        LinkedHashMap responses = (LinkedHashMap)updateResponse.get("response_body");
        assertThat(responses.get("status")).isEqualTo("success");
        assertThat(responses.get("message")).isEqualTo("All sraIds updated successfully");

        LinkedHashMap savedContactInformation1 = retrieveContacts(orgId1);
        LinkedHashMap savedContactInformation2 = retrieveContacts(orgId2);

        verifyDxAddress(savedContactInformation1,contactInformationUpdateData);
        verifyDxAddress(savedContactInformation2,contactInformationUpdateData1);



        deleteCreatedTestOrganisations(orgId1,  orgId2);

    }


    private String createActiveOrganisationWithMultipleContact() {
        OrganisationCreationRequest organisationCreationRequest2 = organisationRequestWithMultipleContactInformations().build();
        String organisationIdentifier = createAndActivateOrganisationWithGivenRequest(organisationCreationRequest2);
        updateOrganisation(organisationIdentifier, hmctsAdmin, "ACTIVE");

        return organisationIdentifier;
    }

    private String createActiveOrganisationWithSingleContact() {
        OrganisationCreationRequest organisationCreationRequest = someMinimalOrganisationRequest().build();
        String organisationIdentifier = createAndActivateOrganisationWithGivenRequest(organisationCreationRequest);
        updateOrganisation(organisationIdentifier, hmctsAdmin, "ACTIVE");

        return organisationIdentifier;
    }



    public ContactInformationUpdateRequest createContactInformationUpdateRequestWithDxAddress( String orgId1,
                                                                                               String addressLine1,
                                                                                               String addressLine3,
                                                                                               String uprn1,
                                                                                               String orgId2,
                                                                                               String addLine1,
                                                                                               String addLine3,
                                                                                               String uprn2) {
        ContactInformationUpdateRequest contactInformationUpdateRequest = new ContactInformationUpdateRequest();
        List<ContactInformationUpdateRequest.ContactInformationUpdateData> contactInformationUpdateDataList
            = new ArrayList<>();
        contactInformationUpdateDataList.add(new ContactInformationUpdateRequest.ContactInformationUpdateData(
                orgId1,uprn1,addressLine1,
                "addressLine2",addressLine3, "som1-town-city",
                "some-county1","some-country1","som1-post-code", Arrays.asList
                (dxAddressCreationRequest().dxNumber("DX 1234567890").dxExchange("dxExchange-1").build())));
        contactInformationUpdateDataList.add(new ContactInformationUpdateRequest.ContactInformationUpdateData(
                orgId2,uprn2,addLine1,
                "addLine2",addLine3, "som2-town-city",
                "some-county2","some-country2","som2-post-code",Arrays.asList
                (dxAddressCreationRequest().dxNumber("DX 2234567890").dxExchange("dxExchange-2").build())));
        contactInformationUpdateRequest.setContactInformationUpdateData(contactInformationUpdateDataList);

        return contactInformationUpdateRequest;
    }

    public ContactInformationUpdateRequest createContactInformationUpdateRequestWithOutDxAddress( String orgId1,
                                                                                                  String addressLine1,
                                                                                                  String addressLine3,
                                                                                                  String uprn1,
                                                                                                  String orgId2,
                                                                                                  String addLine1,
                                                                                                  String addLine3,
                                                                                                  String uprn2) {
        ContactInformationUpdateRequest contactInformationUpdateRequest = new ContactInformationUpdateRequest();
        List<ContactInformationUpdateRequest.ContactInformationUpdateData> contactInformationUpdateDataList
            = new ArrayList<>();
        contactInformationUpdateDataList.add(
            new ContactInformationUpdateRequest.ContactInformationUpdateData(
                orgId1,uprn1,addressLine1,
                "addressLine2",addressLine3, "som1-town-city",
                "some-county1","some-country1","som1-post-code",null));
        contactInformationUpdateDataList.add(
            new ContactInformationUpdateRequest.ContactInformationUpdateData(
                orgId2,uprn2,addLine1,
                "addLine2",addLine3, "som2-town-city",
                "some-county2","some-country2","som2-post-code",null));

        contactInformationUpdateRequest.setContactInformationUpdateData(contactInformationUpdateDataList);
        return contactInformationUpdateRequest;
    }

    public LinkedHashMap retrieveContacts(String organisationIdentifier){
        java.util.Map<String, Object> retrieveOrganisationResponse = professionalReferenceDataClient
            .retrieveSingleOrganisation(organisationIdentifier, hmctsAdmin);

        List savedContacts = (List)retrieveOrganisationResponse.get("contactInformation");
        return (LinkedHashMap)savedContacts.get(0);

    }

    public void verifyContactInfo(LinkedHashMap savedContactInformation,
                                  ContactInformationUpdateRequest.ContactInformationUpdateData contactInformationUpdateData)
    {
        assertThat(savedContactInformation.get("addressLine1").toString())
            .isEqualTo(contactInformationUpdateData.getAddressLine1());
        assertThat(savedContactInformation.get("addressLine2").toString())
            .isEqualTo(contactInformationUpdateData.getAddressLine2());
        assertThat(savedContactInformation.get("addressLine3").toString())
            .isEqualTo(contactInformationUpdateData.getAddressLine3());
        assertThat(savedContactInformation.get("country").toString())
            .isEqualTo(contactInformationUpdateData.getCountry());
        assertThat(savedContactInformation.get("postCode").toString())
            .isEqualTo(contactInformationUpdateData.getPostCode());
        LocalDateTime updatedDate =  LocalDateTime.parse(savedContactInformation.get("lastUpdated").toString());
        assertThat(updatedDate.toLocalDate()).isEqualTo(LocalDate.now());

    }

    public void verifyDxAddress(LinkedHashMap savedContactInformation,ContactInformationUpdateRequest.
        ContactInformationUpdateData contactInformationUpdateData){
        List dxAdd = (List)savedContactInformation.get("dxAddress");
        LinkedHashMap dxAddress = (LinkedHashMap)dxAdd.get(1);

        assertThat((String)dxAddress.get("dxExchange"))
            .isEqualTo(contactInformationUpdateData.getDxAddress().get(0).getDxExchange());
        assertThat((String)dxAddress.get("dxNumber"))
            .isEqualTo(contactInformationUpdateData.getDxAddress().get(0).getDxNumber());
        LocalDateTime updatedDxDate =  LocalDateTime.parse(dxAddress.get("lastUpdated").toString());
        assertThat(updatedDxDate.toLocalDate()).isEqualTo(LocalDate.now());

    }

    public void deleteCreatedTestOrganisations(String orgId1, String orgId2) {
        professionalReferenceDataClient.deleteOrganisation(hmctsAdmin,orgId1);
        professionalReferenceDataClient.deleteOrganisation(hmctsAdmin,orgId2);
    }


}
