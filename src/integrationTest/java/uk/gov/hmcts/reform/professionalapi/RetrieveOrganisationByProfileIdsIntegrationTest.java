package uk.gov.hmcts.reform.professionalapi;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrgAttributeRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationByProfileIdsRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationOtherOrgsCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.response.OrganisationByProfileResponse;
import uk.gov.hmcts.reform.professionalapi.util.AuthorizationEnabledIntegrationTest;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.professionalapi.controller.request.ContactInformationCreationRequest.aContactInformationCreationRequest;
import static uk.gov.hmcts.reform.professionalapi.controller.request.DxAddressCreationRequest.dxAddressCreationRequest;
import static uk.gov.hmcts.reform.professionalapi.controller.request.UserCreationRequest.aUserCreationRequest;

class RetrieveOrganisationByProfileIdsIntegrationTest extends AuthorizationEnabledIntegrationTest {

    private final String solicitorOrgType = "SOLICITOR-ORG";
    private final String ogdHoOrgType = "OGD-HO-ORG";

    @BeforeEach
    public void setup() {
        OrganisationOtherOrgsCreationRequest request1 = this.CreateUniqueOrganisationRequest("TstSO1", "SRA123", "PBA1234561", "super-email1@gmail.com", solicitorOrgType);
        professionalReferenceDataClient.createOrganisationV2(request1);

        OrganisationOtherOrgsCreationRequest request2 = this.CreateUniqueOrganisationRequest("TstSO2", "SRA124", "PBA1234562", "super-email2@gmail.com", solicitorOrgType);
        professionalReferenceDataClient.createOrganisationV2(request2);

        OrganisationOtherOrgsCreationRequest request3 = this.CreateUniqueOrganisationRequest("TestOG1", "SRA125", "PBA1234563", "super-email3@gmail.com", ogdHoOrgType);
        professionalReferenceDataClient.createOrganisationV2(request3);

        OrganisationOtherOrgsCreationRequest request4 = this.CreateUniqueOrganisationRequest("TestOG2", "SRA126", "PBA1234564", "super-email4@gmail.com", ogdHoOrgType);
        professionalReferenceDataClient.createOrganisationV2(request4);
    }

    @Test
    void retrieve_all_organisations_when_no_profile_ids_provided_should_all_organisations_and_status_200() {
        // arrange
        OrganisationByProfileIdsRequest organisationByProfileIdsRequest = new OrganisationByProfileIdsRequest();
        Integer pageSize = null;
        UUID searchAfter = null;

        String expectedStatus = "200 OK";
        boolean expectedHasMoreRecords = false;
        int expectedOrganisationsCount = 5; // 4 seeded in setup and 1

        // act
        Map<String, Object> response = professionalReferenceDataClient.retrieveOrganisationsByProfileIds(organisationByProfileIdsRequest, pageSize, searchAfter);

        // assert
        assertSuccessfulResponse(response, expectedOrganisationsCount, expectedStatus, expectedHasMoreRecords);
    }

    @Test
    void retrieve_all_organisations_when_profile_ids_provided_should_all_organisations_with_matching_org_types_and_status_200() {
        // arrange
        OrganisationByProfileIdsRequest organisationByProfileIdsRequest = new OrganisationByProfileIdsRequest();
        organisationByProfileIdsRequest.setOrganisationProfileIds(List.of(solicitorOrgType));
        Integer pageSize = null;
        UUID searchAfter = null;

        String expectedStatus = "200 OK";
        boolean expectedHasMoreRecords = false;
        int expectedOrganisationsCount = 2;

        // TODO: seed some organisations with a variety of orgType values

        // act
        Map<String, Object> response = professionalReferenceDataClient.retrieveOrganisationsByProfileIds(organisationByProfileIdsRequest, pageSize, searchAfter);

        // assert
        assertSuccessfulResponse(response, expectedOrganisationsCount, expectedStatus, expectedHasMoreRecords);
    }

    @Test
    void retrieve_all_organisations_when_profile_ids_and_page_size_is_provided_should_all_organisations_with_matching_org_types_and_status_200() {
        // arrange
        OrganisationByProfileIdsRequest organisationByProfileIdsRequest = new OrganisationByProfileIdsRequest();
        organisationByProfileIdsRequest.setOrganisationProfileIds(List.of(solicitorOrgType));
        Integer pageSize = 1;
        UUID searchAfter = null;
        int expectedOrganisationsCount = 1;

        String expectedStatus = "200 OK";
        boolean expectedHasMoreRecords = true;

        // TODO: seed some organisations with a variety of orgType values

        // act
        Map<String, Object> response = professionalReferenceDataClient.retrieveOrganisationsByProfileIds(organisationByProfileIdsRequest, pageSize, searchAfter);

        // assert
        assertSuccessfulResponse(response, expectedOrganisationsCount, expectedStatus, expectedHasMoreRecords);
    }

    private void assertSuccessfulResponse(Map<String, Object> response, int expectedOrganisationsCount, String expectedStatus, boolean expectedHasMoreRecords) {
        String actualStatus = (String) response.get("http_status");
        assertThat(actualStatus).isEqualTo(expectedStatus);

        boolean actualHasMoreRecords = (boolean) response.get("moreAvailable");
        assertThat(actualHasMoreRecords).isEqualTo(expectedHasMoreRecords);

        List<OrganisationByProfileResponse> actualOrganisationInfo = (List<OrganisationByProfileResponse>) response.get("organisationInfo");
        assertThat(actualOrganisationInfo.size()).isEqualTo(expectedOrganisationsCount);
    }

    @Test
    void retrieve_all_organisations_when_an_invalid_page_size_is_provided_should_all_organisations_with_matching_org_types_and_status_400() {
        // arrange
        OrganisationByProfileIdsRequest organisationByProfileIdsRequest = new OrganisationByProfileIdsRequest();
        organisationByProfileIdsRequest.setOrganisationProfileIds(List.of(solicitorOrgType));
        Integer pageSize = -1;
        UUID searchAfter = null;

        String expectedStatus = "400";
        String expectedErrorMessage = "Invalid pageSize";

        // TODO: seed some organisations with a variety of orgType values

        // act
        Map<String, Object> response = professionalReferenceDataClient.retrieveOrganisationsByProfileIds(organisationByProfileIdsRequest, pageSize, searchAfter);

        // assert
        String actualStatus = (String) response.get("http_status");
        assertThat(actualStatus).isEqualTo(expectedStatus);
        String actualResponseBody = (String) response.get("response_body");
        assertThat(actualResponseBody).contains(expectedErrorMessage);
    }

    @Test
    void retrieve_all_organisations_when_an_invalid_search_after_is_provided_should_all_organisations_with_matching_org_types_and_status_400() {
        // arrange
        OrganisationByProfileIdsRequest organisationByProfileIdsRequest = new OrganisationByProfileIdsRequest();
        organisationByProfileIdsRequest.setOrganisationProfileIds(List.of(solicitorOrgType));
        Integer pageSize = 1;
        UUID searchAfter = UUID.fromString("00000000-0000-0000-0000-000000000000");

        String expectedStatus = "400";
        String expectedErrorMessage = "Invalid searchAfter";

        // act
        Map<String, Object> response = professionalReferenceDataClient.retrieveOrganisationsByProfileIds(organisationByProfileIdsRequest, pageSize, searchAfter);

        // assert
        String actualStatus = (String) response.get("http_status");
        assertThat(actualStatus).isEqualTo(expectedStatus);
        String actualResponseBody = (String) response.get("response_body");
        assertThat(actualResponseBody).contains(expectedErrorMessage);
    }

    private OrganisationOtherOrgsCreationRequest CreateUniqueOrganisationRequest(String companyNumber, String sraId, String paymentAccount, String superUserEmail, String orgType) {
        Set<String> paymentAccounts = new HashSet<>();
        paymentAccounts.add(paymentAccount);

        List<OrgAttributeRequest> orgAttributeRequests = new ArrayList<>();

        OrgAttributeRequest orgAttributeRequest = new OrgAttributeRequest();

        orgAttributeRequest.setKey("testKey");
        orgAttributeRequest.setValue("testValue");

        orgAttributeRequests.add(orgAttributeRequest);

        return
                new OrganisationOtherOrgsCreationRequest("some-org-name",
                        "PENDING",
                        "test",
                        sraId,
                        "false",
                        companyNumber,
                        "company-url",
                        aUserCreationRequest()
                                .firstName("some-fname")
                                .lastName("some-lname")
                                .email(superUserEmail)
                                .build(),
                        paymentAccounts,
                        Collections
                                .singletonList(aContactInformationCreationRequest()
                                        .addressLine1("addressLine1")
                                        .addressLine2("addressLine2")
                                        .addressLine3("addressLine3")
                                        .country("country")
                                        .county("county")
                                        .townCity("town-city")
                                        .uprn("uprn")
                                        .postCode("some-post-code")
                                        .dxAddress(Collections
                                                .singletonList(dxAddressCreationRequest()
                                                        .dxNumber("DX 1234567890")
                                                        .dxExchange("dxExchange").build()))
                                        .build()),
                        orgType,
                        orgAttributeRequests);
    }
}
