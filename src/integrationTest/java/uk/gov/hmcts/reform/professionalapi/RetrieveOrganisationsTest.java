package uk.gov.hmcts.reform.professionalapi;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest.anOrganisationCreationRequest;
import static uk.gov.hmcts.reform.professionalapi.controller.request.UserCreationRequest.aUserCreationRequest;
import static uk.gov.hmcts.reform.professionalapi.utils.OrganisationFixtures.organisationRequestWithAllFields;
import static uk.gov.hmcts.reform.professionalapi.utils.OrganisationFixtures.organisationRequestWithAllFieldsAreUpdated;

import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import org.junit.Test;

import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus;
import uk.gov.hmcts.reform.professionalapi.util.Service2ServiceEnabledIntegrationTest;

@Slf4j
public class RetrieveOrganisationsTest extends Service2ServiceEnabledIntegrationTest {

    @SuppressWarnings("unchecked")
    @Test
    public void persists_and_returns_organisation_details() {

        String orgIdentifierResponse = createOrganisationRequest(OrganisationStatus.PENDING.name().toLowerCase());
        assertThat(orgIdentifierResponse).isNotEmpty();
        Map<String, Object> orgResponse =
                professionalReferenceDataClient.retrieveSingleOrganisation(orgIdentifierResponse);

        assertThat(orgResponse.get("http_status").toString().contains("OK"));
        assertThat(orgResponse.get("organisationIdentifier")).isEqualTo(orgIdentifierResponse);

        assertThat(orgResponse.get("name")).isEqualTo("some-org-name");
        assertThat(orgResponse.get("sraId")).isEqualTo("sra-id");
        assertThat(orgResponse.get("sraRegulated")).isEqualTo(false);
        assertThat(orgResponse.get("companyUrl")).isEqualTo("company-url");
        assertThat(orgResponse.get("companyNumber")).isEqualTo("company");

        Map<String, Object> superUser = ((List<Map<String, Object>>) orgResponse.get("superUser")).get(0);
        assertThat(superUser.get("firstName")).isEqualTo("some-fname");
        assertThat(superUser.get("lastName")).isEqualTo("some-lname");
        assertThat(superUser.get("email")).isEqualTo("someone@somewhere.com");

        List<Map<String, Object>> accounts = (List<Map<String, Object>>) orgResponse.get("pbaAccounts");
        assertThat(accounts.get(0).get("pbaNumber")).isEqualTo("pbaNumber-1");

        Map<String, Object> contactInfo = ((List<Map<String, Object>>) orgResponse.get("contactInformation")).get(0);
        assertThat(contactInfo.get("addressLine1")).isEqualTo("addressLine1");
        assertThat(contactInfo.get("addressLine2")).isEqualTo("addressLine2");
        assertThat(contactInfo.get("addressLine3")).isEqualTo("addressLine3");
        assertThat(contactInfo.get("county")).isEqualTo("county");
        assertThat(contactInfo.get("country")).isEqualTo("country");
        assertThat(contactInfo.get("townCity")).isEqualTo("town-city");
        assertThat(contactInfo.get("postCode")).isEqualTo("some-post-code");

        Map<String, Object> dxAddress = ((List<Map<String, Object>>) contactInfo.get("dxAddress")).get(0);
        assertThat(dxAddress.get("dxNumber")).isEqualTo("DX 1234567890");
        assertThat(dxAddress.get("dxExchange")).isEqualTo("dxExchange");

        log.info("RetrieveOrganisationsTest:Received response to retrieve an organisation details...");
    }

    @Test
    public void persists_and_returns_all_organisations() {
        professionalReferenceDataClient.createOrganisation(anOrganisationCreationRequest()
                .name("some-org-name")
                .superUser(aUserCreationRequest()
                        .firstName("some-fname")
                        .lastName("some-lname")
                        .email("someone@somewhere.com")
                        .build())
                .build());
        professionalReferenceDataClient.createOrganisation(anOrganisationCreationRequest()
                .name("some-other-org-name")
                .superUser(aUserCreationRequest()
                        .firstName("some-fname")
                        .lastName("some-lname")
                        .email("someoneElse@somewhere.com")
                        .build())
                .build());

        Map<String, Object> orgResponse =
                professionalReferenceDataClient.retrieveAllOrganisations();
        assertThat(orgResponse.get("http_status").toString().contains("OK"));
        assertThat(((List<?>) orgResponse.get("organisations")).size()).isEqualTo(2);
    }

    @Test
    public void error_if_organisation_id_invalid() {
        Map<String, Object> response = professionalReferenceDataClient.retrieveSingleOrganisation("They're taking the hobbits to Isengard!");
        assertThat(response.get("http_status")).isEqualTo("400");
    }

    @Test
    public void error_if_organisation_id_not_found() {
        Map<String, Object> response = professionalReferenceDataClient.retrieveSingleOrganisation("19591f16-8503-4f25-a119-5c22c024e9be");
        assertThat(response.get("http_status")).isEqualTo("404");
    }



    @Test
    public void persists_and_returns_all_organisations_details_by_pending_status() {

        String organisationIdentifier = createOrganisationRequest(OrganisationStatus.PENDING.name().toLowerCase());
        assertThat(organisationIdentifier).isNotEmpty();
        Map<String, Object> orgResponse =
                professionalReferenceDataClient.retrieveAllOrganisationDetailsByStatusTest(OrganisationStatus.PENDING.name());
        assertThat(orgResponse.get("organisations")).isNotNull();
        assertThat(orgResponse.get("organisations")).asList().isNotEmpty();
        assertThat(orgResponse.get("http_status").toString().contains("OK"));
    }

    @Test
    public void persists_and_returns_all_organisations_details_by_active_status() {
        Map<String, Object> orgResponse;
        String organisationIdentifier = createOrganisationRequest(OrganisationStatus.ACTIVE.name().toLowerCase());
        assertThat(organisationIdentifier).isNotEmpty();
        orgResponse =
                professionalReferenceDataClient.retrieveAllOrganisationDetailsByStatusTest(OrganisationStatus.ACTIVE.name());
        assertThat(orgResponse.get("http_status").toString().contains("OK"));

        OrganisationCreationRequest organisationUpdateRequest = organisationRequestWithAllFieldsAreUpdated()
                .status(OrganisationStatus.ACTIVE).build();

        Map<String, Object> responseForOrganisationUpdate =
                professionalReferenceDataClient.updateOrganisation(organisationUpdateRequest, organisationIdentifier);

        assertThat(responseForOrganisationUpdate.get("http_status")).isEqualTo(200);
        orgResponse =
                professionalReferenceDataClient.retrieveAllOrganisationDetailsByStatusTest(OrganisationStatus.ACTIVE.name());

        assertThat(orgResponse.get("organisations")).asList().isNotEmpty();
        assertThat(orgResponse.get("http_status").toString().contains("OK"));
    }

    @Test
    public void persists_and_return_empty_organisation_details_when_no_status_found_in_the_db() {

        String organisationIdentifier = createOrganisationRequest(OrganisationStatus.ACTIVE.name().toLowerCase());
        assertThat(organisationIdentifier).isNotEmpty();
        Map<String, Object> orgResponse =
                professionalReferenceDataClient.retrieveAllOrganisationDetailsByStatusTest(OrganisationStatus.ACTIVE.name());
        assertThat(orgResponse.get("http_status").toString().contains("OK"));
    }

    @Test
    public void return_404_when_invalid_status_send_in_the_request_param() {

        String organisationIdentifier = createOrganisationRequest(OrganisationStatus.ACTIVE.name().toLowerCase());
        assertThat(organisationIdentifier).isNotEmpty();
        Map<String, Object> orgResponse =
                professionalReferenceDataClient.retrieveAllOrganisationDetailsByStatusTest("ACTIV");
        assertThat(orgResponse.get("http_status").toString().contains("404"));
    }

    private String createOrganisationRequest(String status) {
        OrganisationCreationRequest organisationCreationRequest = null;
        if (status.equals("active")) {
            organisationCreationRequest = organisationRequestWithAllFields().status(OrganisationStatus.ACTIVE).build();
        } else if (status.equals("pending")) {
            organisationCreationRequest = organisationRequestWithAllFields().build();
        }
        Map<String, Object> responseForOrganisationCreation = professionalReferenceDataClient.createOrganisation(organisationCreationRequest);
        return (String) responseForOrganisationCreation.get("organisationIdentifier");
    }


}
