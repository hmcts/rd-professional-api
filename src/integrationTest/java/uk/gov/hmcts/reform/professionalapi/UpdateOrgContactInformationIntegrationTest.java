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


        assertThat(updateResponse).containsEntry("http_status", 200);
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

        assertThat(updateResponse).containsEntry("http_status", 200);
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

        assertThat(updateResponse).containsEntry("http_status", 200);
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

    public void deleteOrganisation(String orgIdentifier) {
        Map<String, Object> deleteResponse = professionalReferenceDataClient.deleteOrganisation(hmctsAdmin,
            orgIdentifier);

        assertThat(deleteResponse.get("http_status")).isEqualTo(204);
    }

}
