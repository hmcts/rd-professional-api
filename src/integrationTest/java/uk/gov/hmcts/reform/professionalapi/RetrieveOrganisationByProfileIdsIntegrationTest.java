package uk.gov.hmcts.reform.professionalapi;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import uk.gov.hmcts.reform.professionalapi.util.OrganisationProfileIdConstants;
import uk.gov.hmcts.reform.professionalapi.util.OrganisationTypeConstants;

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

    private final String solicitorOrgType =  OrganisationTypeConstants.SOLICITOR_ORG;
    private final String solicitorProfileId = OrganisationProfileIdConstants.SOLICITOR_PROFILE;
    private final String ogdHoOrgType = OrganisationTypeConstants.OGD_HO_ORG;

    @Autowired
    private OrganisationRepository organisationRepository;
    private String organisationV1Identifier;

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

        organisationV1Identifier = createOrganisationRequest();
    }

    @Test
    void when_no_profile_ids_provided_should_all_organisations_and_status_200() {
        // arrange
        OrganisationByProfileIdsRequest organisationByProfileIdsRequest = new OrganisationByProfileIdsRequest();
        Integer pageSize = null;
        UUID searchAfter = null;

        String expectedStatus = "200 OK";
        boolean expectedHasMoreRecords = false;
        int expectedOrganisationsCount = 5; // should be 5 because v1 should be defaulted to solicitor org type

        // act
        Map<String, Object> response =
                professionalReferenceDataClient.retrieveOrganisationsByProfileIds(organisationByProfileIdsRequest,
                        pageSize, searchAfter);

        // assert
        assertSuccessfulResponse(response, expectedOrganisationsCount, expectedStatus, expectedHasMoreRecords,
                true);
    }

    @SuppressWarnings("unchecked")
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
        assertSuccessfulResponse(response, expectedOrganisationsCount, expectedStatus, expectedHasMoreRecords,
                null);

        List<LinkedHashMap> organisationInfoMapList = (List<LinkedHashMap>) response.get("organisationInfo");

        boolean allMatch = organisationInfoMapList.stream()
                .allMatch(org -> ((List<String>) org.get("organisationProfileIds")).contains(solicitorProfileId));
        assertThat(allMatch).isTrue();
    }

    @SuppressWarnings("unchecked")
    @Test
    void when_non_matching_profile_ids_provided_should_return_default_solicitor_organisations_and_status_200() {
        // arrange
        OrganisationByProfileIdsRequest organisationByProfileIdsRequest = new OrganisationByProfileIdsRequest();
        organisationByProfileIdsRequest.setOrganisationProfileIds(List.of("A"));
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
        assertSuccessfulResponse(response, expectedOrganisationsCount, expectedStatus, expectedHasMoreRecords,
                null);

        List<LinkedHashMap> organisationInfoMapList = (List<LinkedHashMap>) response.get("organisationInfo");

        boolean allMatch = organisationInfoMapList.stream()
                .allMatch(org -> ((List<String>) org.get("organisationProfileIds")).contains(solicitorProfileId));
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
        assertSuccessfulResponse(response, expectedOrganisationsCount, expectedStatus, expectedHasMoreRecords,
                null);
    }

    @Test
    void when_search_after_is_given_should_return_matching_organisations_after_search_after_and_status_200() {
        // arrange
        OrganisationByProfileIdsRequest organisationByProfileIdsRequest = new OrganisationByProfileIdsRequest();
        organisationByProfileIdsRequest.setOrganisationProfileIds(List.of(solicitorProfileId));
        Integer pageSize = 1;
        UUID searchAfter = null;

        // make the first call to get the last record and then use it as searchAfter in the next call
        Map<String, Object> response =
                professionalReferenceDataClient.retrieveOrganisationsByProfileIds(organisationByProfileIdsRequest,
                        pageSize, searchAfter);
        assertSuccessfulResponse(response, 1, "200 OK", true,
                null);

        UUID lastRecordInPage = UUID.fromString(response.get("lastRecordInPage").toString());

        // act
        response = professionalReferenceDataClient.retrieveOrganisationsByProfileIds(organisationByProfileIdsRequest,
                pageSize, lastRecordInPage);

        // assert
        assertSuccessfulResponse(response, 1, "200 OK", false,
                null);
    }

    @SuppressWarnings("unchecked")
    private void assertSuccessfulResponse(Map<String, Object> response, int expectedOrganisationsCount,
                                          String expectedStatus, boolean expectedHasMoreRecords,
                                          Boolean checkForV1) {
        String actualStatus = (String) response.get("http_status");
        assertThat(actualStatus).isEqualTo(expectedStatus);

        String json = convertMapToJson(response);
        MultipleOrganisationsResponse typedResponse = convertJsonToResponse(json, MultipleOrganisationsResponse.class);

        assertThat(typedResponse.getOrganisationInfo()).hasSize(expectedOrganisationsCount);
        assertThat(typedResponse.isMoreAvailable()).isEqualTo(expectedHasMoreRecords);

        boolean checkForV1Org = checkForV1 == null ? false : checkForV1;

        if(checkForV1Org) {
            boolean allMatch =
                    typedResponse.getOrganisationInfo().stream()
                            .anyMatch(org -> org.getOrganisationIdentifier().equals(organisationV1Identifier));
            assertThat(allMatch).isTrue();
        }
    }

    public String convertMapToJson(Map<String, Object> map) {
        ObjectMapper objectMapper = new ObjectMapper();
        String json = "";
        try {
            json = objectMapper.writeValueAsString(map);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return json;
    }

    public <T> T convertJsonToResponse(String json, Class<T> type) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false);
        objectMapper.configure(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE, false);
        T response = null;
        try {
            response = objectMapper.readValue(json, type);
        } catch (Exception e) {
            // Log the error message and rethrow the exception or handle it appropriately
            System.err.println("Error processing JSON: " + e.getMessage());
        }
        return response;
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
