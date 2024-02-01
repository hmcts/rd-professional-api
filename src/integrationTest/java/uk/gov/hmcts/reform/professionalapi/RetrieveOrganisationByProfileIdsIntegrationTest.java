package uk.gov.hmcts.reform.professionalapi;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationByProfileIdsRequest;
import uk.gov.hmcts.reform.professionalapi.controller.response.OrganisationByProfileResponse;
import uk.gov.hmcts.reform.professionalapi.util.AuthorizationEnabledIntegrationTest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class RetrieveOrganisationByProfileIdsIntegrationTest extends AuthorizationEnabledIntegrationTest {

    @Test
    void retrieve_all_organisations_when_no_profile_ids_provided_should_all_organisations_and_status_200() {
        // arrange
        OrganisationByProfileIdsRequest organisationByProfileIdsRequest = new OrganisationByProfileIdsRequest();
        Integer pageSize = null;
        UUID searchAfter = null;

        String expectedStatus = "200 OK";
        UUID expectedLastRecordIdInPage = null;
        boolean expectedHasMoreRecords = false;
        List<OrganisationByProfileResponse> expectedOrganisationInfo = new ArrayList<>();


        // TODO: seed some organisations with a variety of orgType values

        // act
        Map<String, Object> response = professionalReferenceDataClient.retrieveOrganisationsByProfileIds(organisationByProfileIdsRequest, pageSize, searchAfter);

        // assert
        assertSuccessfulResponse(response, expectedOrganisationInfo, expectedStatus, expectedLastRecordIdInPage, expectedHasMoreRecords);
    }

    @Test
    void retrieve_all_organisations_when_profile_ids_provided_should_all_organisations_with_matching_org_types_and_status_200() {
        // arrange
        OrganisationByProfileIdsRequest organisationByProfileIdsRequest = new OrganisationByProfileIdsRequest();
        organisationByProfileIdsRequest.setOrganisationProfileIds(List.of("SOLICITOR_PROFILE"));
        Integer pageSize = null;
        UUID searchAfter = null;

        String expectedStatus = "200 OK";
        UUID expectedLastRecordIdInPage = null;
        boolean expectedHasMoreRecords = false;
        List<OrganisationByProfileResponse> expectedOrganisationInfo = new ArrayList<>();

        // TODO: seed some organisations with a variety of orgType values

        // act
        Map<String, Object> response = professionalReferenceDataClient.retrieveOrganisationsByProfileIds(organisationByProfileIdsRequest, pageSize, searchAfter);

        // assert
        assertSuccessfulResponse(response, expectedOrganisationInfo, expectedStatus, expectedLastRecordIdInPage, expectedHasMoreRecords);
    }

    @Test
    void retrieve_all_organisations_when_profile_ids_and_page_size_is_provided_should_all_organisations_with_matching_org_types_and_status_200() {
        // arrange
        OrganisationByProfileIdsRequest organisationByProfileIdsRequest = new OrganisationByProfileIdsRequest();
        organisationByProfileIdsRequest.setOrganisationProfileIds(List.of("SOLICITOR_PROFILE"));
        Integer pageSize = 1;
        UUID searchAfter = null;

        String expectedStatus = "200 OK";
        UUID expectedLastRecordIdInPage = null; // TODO: set this to the last record ID in the first page
        boolean expectedHasMoreRecords = true;
        List<OrganisationByProfileResponse> expectedOrganisationInfo = new ArrayList<>();

        // TODO: seed some organisations with a variety of orgType values

        // act
        Map<String, Object> response = professionalReferenceDataClient.retrieveOrganisationsByProfileIds(organisationByProfileIdsRequest, pageSize, searchAfter);

        // assert
        assertSuccessfulResponse(response, expectedOrganisationInfo, expectedStatus, expectedLastRecordIdInPage, expectedHasMoreRecords);
    }

    private void assertSuccessfulResponse(Map<String, Object> response, List<OrganisationByProfileResponse> expectedOrganisationInfo, String expectedStatus, UUID expectedLastRecordIdInPage, boolean expectedHasMoreRecords) {
        String actualStatus = (String) response.get("http_status");
        assertThat(actualStatus).isEqualTo(expectedStatus);

        UUID actualLastRecordIdInPage = (UUID) response.get("lastRecordInPage");
        assertThat(actualLastRecordIdInPage).isEqualTo(expectedLastRecordIdInPage);

        boolean actualHasMoreRecords = (boolean) response.get("moreAvailable");
        assertThat(actualHasMoreRecords).isEqualTo(expectedHasMoreRecords);

        List<OrganisationByProfileResponse> actualOrganisationInfo = (List<OrganisationByProfileResponse>) response.get("organisationInfo");
    }

    @Test
    void retrieve_all_organisations_when_an_invalid_page_size_is_provided_should_all_organisations_with_matching_org_types_and_status_400() {
        // arrange
        OrganisationByProfileIdsRequest organisationByProfileIdsRequest = new OrganisationByProfileIdsRequest();
        organisationByProfileIdsRequest.setOrganisationProfileIds(List.of("SOLICITOR_PROFILE"));
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
        organisationByProfileIdsRequest.setOrganisationProfileIds(List.of("SOLICITOR_PROFILE"));
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

//    private void Setup() {
//        OrganisationOtherOrgsCreationRequest request1 = otherOrganisationRequestWithAllFields();
//        request1.setOrgType("SOLICITORPROFILE");
//        Map<String, Object> response1 = professionalReferenceDataClient.createOrganisationV2(request1);
//        UUID org1Id = (UUID) response1.get("organisationIdentifier");
//        OrganisationOtherOrgsCreationRequest request2 = otherOrganisationRequestWithAllFields();
//        request2.setOrgType("SOLICITOR_PROFILE");
//        OrganisationOtherOrgsCreationRequest request3 = otherOrganisationRequestWithAllFields();
//        request3.setOrgType("OGD_HO_PROFILE");
//        OrganisationOtherOrgsCreationRequest request4 = otherOrganisationRequestWithAllFields();
//        request3.setOrgType("OGD_HO_PROFILE");
//    }
}
