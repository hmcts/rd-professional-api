package uk.gov.hmcts.reform.professionalapi;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrgAttributeRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationByProfileIdsRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationOtherOrgsCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.response.MultipleOrganisationsResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.OrganisationByProfileResponse;
import uk.gov.hmcts.reform.professionalapi.repository.OrganisationRepository;
import uk.gov.hmcts.reform.professionalapi.util.AuthorizationEnabledIntegrationTest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.professionalapi.controller.request.ContactInformationCreationRequest.aContactInformationCreationRequest;
import static uk.gov.hmcts.reform.professionalapi.controller.request.DxAddressCreationRequest.dxAddressCreationRequest;
import static uk.gov.hmcts.reform.professionalapi.controller.request.UserCreationRequest.aUserCreationRequest;

class RetrieveOrganisationByProfileIdsIntegrationTest extends AuthorizationEnabledIntegrationTest {

    private final String solicitorOrgType = "SOLICITOR-ORG";
    private final String solicitorProfileId = "SOLICITOR_PROFILE";
    private final String ogdHoOrgType = "OGD-HO-ORG";

    @Autowired
    private OrganisationRepository organisationRepository;

    @BeforeEach
    public void setup() {
        organisationRepository.deleteAll();

        OrganisationOtherOrgsCreationRequest request1 = this.createUniqueOrganisationRequest("TstSO1", "SRA123",
                "PBA1234561", "super-email1@gmail.com", solicitorOrgType);
        professionalReferenceDataClient.createOrganisationV2(request1);

        OrganisationOtherOrgsCreationRequest request2 = this.createUniqueOrganisationRequest("TstSO2", "SRA124",
                "PBA1234562", "super-email2@gmail.com", solicitorOrgType);
        professionalReferenceDataClient.createOrganisationV2(request2);

        OrganisationOtherOrgsCreationRequest request3 = this.createUniqueOrganisationRequest("TestOG1", "SRA125",
                "PBA1234563", "super-email3@gmail.com", ogdHoOrgType);
        professionalReferenceDataClient.createOrganisationV2(request3);

        OrganisationOtherOrgsCreationRequest request4 = this.createUniqueOrganisationRequest("TestOG2", "SRA126",
                "PBA1234564", "super-email4@gmail.com", ogdHoOrgType);
        professionalReferenceDataClient.createOrganisationV2(request4);
    }

    @Test
    void when_no_profile_ids_provided_should_all_organisations_and_status_200() {
        // arrange
        OrganisationByProfileIdsRequest organisationByProfileIdsRequest = new OrganisationByProfileIdsRequest();
        Integer pageSize = null;
        UUID searchAfter = null;

        String expectedStatus = "200 OK";
        boolean expectedHasMoreRecords = false;
        int expectedOrganisationsCount = 4;

        // act
        Map<String, Object> response =
                professionalReferenceDataClient.retrieveOrganisationsByProfileIds(organisationByProfileIdsRequest,
                        pageSize, searchAfter);

        // assert
        assertSuccessfulResponse(response, expectedOrganisationsCount, expectedStatus, expectedHasMoreRecords);
    }

    @Test
    void when_profile_ids_provided_should_return_matching_organisations_and_status_200() {
        // arrange
        OrganisationByProfileIdsRequest organisationByProfileIdsRequest = new OrganisationByProfileIdsRequest();
        organisationByProfileIdsRequest.setOrganisationProfileIds(List.of(solicitorProfileId));
        Integer pageSize = null;
        UUID searchAfter = null;

        String expectedStatus = "200 OK";
        boolean expectedHasMoreRecords = false;
        int expectedOrganisationsCount = 2;

        // act
        Map<String, Object> response =
                professionalReferenceDataClient.retrieveOrganisationsByProfileIds(organisationByProfileIdsRequest,
                        pageSize, searchAfter);

        // assert
        assertSuccessfulResponse(response, expectedOrganisationsCount, expectedStatus, expectedHasMoreRecords);

        List<LinkedHashMap> organisationInfoMapList = (List<LinkedHashMap>) response.get("organisationInfo");

        boolean allMatch = organisationInfoMapList.stream()
                .allMatch(org -> ((List<String>) org.get("organisationProfileId")).contains(solicitorProfileId));
        assertThat(allMatch).isTrue();
    }

    @Test
    void when_profile_ids_and_page_size_is_provided_should_matching_organisations_as_page_and_status_200() {
        // arrange
        OrganisationByProfileIdsRequest organisationByProfileIdsRequest = new OrganisationByProfileIdsRequest();
        organisationByProfileIdsRequest.setOrganisationProfileIds(List.of(solicitorProfileId));
        Integer pageSize = 1;
        UUID searchAfter = null;
        int expectedOrganisationsCount = 1;

        String expectedStatus = "200 OK";
        boolean expectedHasMoreRecords = true;

        // act
        Map<String, Object> response =
                professionalReferenceDataClient.retrieveOrganisationsByProfileIds(organisationByProfileIdsRequest,
                        pageSize, searchAfter);

        // assert
        assertSuccessfulResponse(response, expectedOrganisationsCount, expectedStatus, expectedHasMoreRecords);
    }

    private void assertSuccessfulResponse(Map<String, Object> response, int expectedOrganisationsCount,
                                          String expectedStatus, boolean expectedHasMoreRecords) {
        String actualStatus = (String) response.get("http_status");
        assertThat(actualStatus).isEqualTo(expectedStatus);

        MultipleOrganisationsResponse typedResponse = new MultipleOrganisationsResponse();
        boolean actualHasMoreRecords = (boolean) response.get("moreAvailable");

        List<OrganisationByProfileResponse> actualOrganisationInfo =
                (List<OrganisationByProfileResponse>) response.get("organisationInfo");

        typedResponse.setMoreAvailable(actualHasMoreRecords);
        typedResponse.setOrganisationInfo(actualOrganisationInfo);
        if (response.get("lastRecordInPage") != null) {
            typedResponse.setLastRecordInPage(UUID.fromString((String) response.get("lastRecordInPage")));
        } else {
            typedResponse.setLastRecordInPage(null);
        }

        assertThat(typedResponse.getOrganisationInfo().size()).isEqualTo(expectedOrganisationsCount);
        assertThat(typedResponse.isMoreAvailable()).isEqualTo(expectedHasMoreRecords);
    }

    @Test
    void when_an_invalid_page_size_is_provided_should_return_status_400_and_error_messages() {
        // arrange
        OrganisationByProfileIdsRequest organisationByProfileIdsRequest = new OrganisationByProfileIdsRequest();
        organisationByProfileIdsRequest.setOrganisationProfileIds(List.of(solicitorProfileId));
        Integer pageSize = -1;
        UUID searchAfter = null;

        String expectedStatus = "400";
        String expectedErrorMessage = "Invalid pageSize";

        // act
        Map<String, Object> response = professionalReferenceDataClient
                .retrieveOrganisationsByProfileIds(organisationByProfileIdsRequest, pageSize, searchAfter);

        // assert
        String actualStatus = (String) response.get("http_status");
        assertThat(actualStatus).isEqualTo(expectedStatus);
        String actualResponseBody = (String) response.get("response_body");
        assertThat(actualResponseBody).contains(expectedErrorMessage);
    }

    @Test
    void when_an_search_after_is_provided_should_return_status_400_and_error_messages() {
        // arrange
        OrganisationByProfileIdsRequest organisationByProfileIdsRequest = new OrganisationByProfileIdsRequest();
        organisationByProfileIdsRequest.setOrganisationProfileIds(List.of(solicitorProfileId));
        Integer pageSize = 1;
        UUID searchAfter = UUID.fromString("00000000-0000-0000-0000-000000000000");

        String expectedStatus = "400";
        String expectedErrorMessage = "Invalid searchAfter";

        // act
        Map<String, Object> response = professionalReferenceDataClient
                .retrieveOrganisationsByProfileIds(organisationByProfileIdsRequest, pageSize, searchAfter);

        // assert
        String actualStatus = (String) response.get("http_status");
        assertThat(actualStatus).isEqualTo(expectedStatus);
        String actualResponseBody = (String) response.get("response_body");
        assertThat(actualResponseBody).contains(expectedErrorMessage);
    }

    private OrganisationOtherOrgsCreationRequest createUniqueOrganisationRequest(String companyNumber, String sraId,
                                                                                 String paymentAccount,
                                                                                 String superUserEmail,
                                                                                 String orgType) {
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
