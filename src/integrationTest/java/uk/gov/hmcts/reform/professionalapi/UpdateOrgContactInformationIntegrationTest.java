package uk.gov.hmcts.reform.professionalapi;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.professionalapi.controller.request.ContactInformationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.util.AuthorizationEnabledIntegrationTest;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.professionalapi.helper.OrganisationFixtures.createContactInformationRequest;
import static uk.gov.hmcts.reform.professionalapi.helper.OrganisationFixtures.organisationRequestWithAllFieldsAreUpdated;
import static uk.gov.hmcts.reform.professionalapi.helper.OrganisationFixtures.organisationRequestWithMultipleAddressAllFields;

class UpdateOrgContactInformationIntegrationTest extends AuthorizationEnabledIntegrationTest {

    @Test
    void update_addressline1_for_organisation_with_prd_admin_role_should_return_200() {
        ContactInformationCreationRequest contactInformationCreationRequest =
            createContactInformationRequest().build();

        Map<String, Object> updateResponse = professionalReferenceDataClient
            .updateOrgContactInformation(contactInformationCreationRequest,
                hmctsAdmin,createOrganisationRequest());

        assertThat(updateResponse).containsEntry("http_status", 200);
    }

    @Test
    void update_contact_with_bad_request_should_return_400() {
        Map<String, Object> updateResponse = professionalReferenceDataClient
            .updateOrgContactInformation(null,  hmctsAdmin,createOrganisationRequest());

        assertThat(updateResponse).containsEntry("http_status", "400");
    }

    @Test
    void update_contact_information_invalid_addressline1_should_return_400() {
        ContactInformationCreationRequest contactInformationCreationRequest =
            createContactInformationRequest().addressLine1(null).build();

        Map<String, Object> updateResponse = professionalReferenceDataClient
            .updateOrgContactInformation(contactInformationCreationRequest,
                hmctsAdmin,createOrganisationRequest());

        assertThat(updateResponse).containsEntry("http_status", "400");
        assertThat(updateResponse.get("response_body").toString()).contains(
        "[Field error in object 'contactInformationCreationRequest' on field 'addressLine1'");
    }

    @Test
    void update_contact_information_multiple_requests_existing_for_org_should_return_200() {

        ContactInformationCreationRequest contactInformationCreationRequest =
            createContactInformationRequest().build();

        java.util.Map<String, Object> responseForOrganisationCreation = professionalReferenceDataClient
            .createOrganisation(organisationRequestWithMultipleAddressAllFields().build());


        Map<String, Object> updateResponse = professionalReferenceDataClient
            .updateOrgContactInformation(contactInformationCreationRequest,
                hmctsAdmin,(String)responseForOrganisationCreation.get(ORG_IDENTIFIER));

        assertThat(updateResponse).containsEntry("http_status", 200);
    }

    @Test
    void update_contact_information_single_requests_existing_for_org_should_return_200() {

        ContactInformationCreationRequest contactInformationCreationRequest =
            createContactInformationRequest().build();


        java.util.Map<String, Object> responseForOrganisationCreation = professionalReferenceDataClient
            .createOrganisation(organisationRequestWithAllFieldsAreUpdated().build());


        Map<String, Object> updateResponse = professionalReferenceDataClient
            .updateOrgContactInformation(contactInformationCreationRequest,
                hmctsAdmin,(String)responseForOrganisationCreation.get(ORG_IDENTIFIER));

        assertThat(updateResponse).containsEntry("http_status", 200);
    }

    @Test
    void update_contact_information_for_non_existing_organisation_should_return_400() {
        ContactInformationCreationRequest contactInformationCreationRequest =
            createContactInformationRequest().build();

        Map<String, Object> updateResponse = professionalReferenceDataClient
            .updateOrgContactInformation(contactInformationCreationRequest,  hmctsAdmin,"ABCDEF7");

        assertThat(updateResponse).containsEntry("http_status", "404");
        assertThat(updateResponse.get("response_body").toString())
            .contains("errorMessage\":\"4 : Resource not found\",\"errorDescription\":\"Organisation does not exist");

    }

    @Test
    void update_contact_information_invalid_request_should_return_400() {

        Map<String, Object> updateResponse = professionalReferenceDataClient
            .updateOrgContactInformation(new ContactInformationCreationRequest(null,null,
                null,null,null,null,null,
                null,null), hmctsAdmin,createOrganisationRequest());

        assertThat(updateResponse).containsEntry("http_status", "404");
        assertThat(updateResponse.get("response_body").toString())
            .contains("validation on an argument failed");

    }

}
