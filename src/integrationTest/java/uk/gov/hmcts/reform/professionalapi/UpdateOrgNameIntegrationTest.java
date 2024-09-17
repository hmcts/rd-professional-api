package uk.gov.hmcts.reform.professionalapi;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationNameUpdateRequest;
import uk.gov.hmcts.reform.professionalapi.util.AuthorizationEnabledIntegrationTest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.professionalapi.helper.OrganisationFixtures.someMinimalOrganisationRequest;

class UpdateOrgNameIntegrationTest extends AuthorizationEnabledIntegrationTest {


    @Test
    void update_name_of_an_active_organisation_should_return_success_if_all_requests_updated() {
        //create organisation
        String orgId1 = getActiveOrganisationId();
        String orgId2 = getActiveOrganisationId();

        //create request to update organisation
        OrganisationNameUpdateRequest organisationNameUpdateRequest = createOrganisationNameUpdateRequest(
            "updatedName1","updatedName1",orgId1,orgId2);
        //updateName
        Map<String, Object> orgUpdatedNameResponse = professionalReferenceDataClient
                .updateOrgName(organisationNameUpdateRequest,hmctsAdmin);

        LinkedHashMap responses = (LinkedHashMap)orgUpdatedNameResponse.get("response_body");
        assertThat(responses.get("status")).isEqualTo("success");
        assertThat(responses.get("message")).isEqualTo("All names updated successfully");

        verifyRetrievedOrg(orgId1,"updatedName1");
        verifyRetrievedOrg(orgId2,"updatedName2");

        deleteCreatedTestOrganisations(orgId1,  orgId2);
    }

    @Test
    void update_name_of_an_active_organisation_should_return_partial_success_if_any_fail() {
        //create organisation
        String orgId1 = getActiveOrganisationId();
        String orgId2 = getActiveOrganisationId();

        //create request to update organisation
        OrganisationNameUpdateRequest organisationNameUpdateRequest = createOrganisationNameUpdateRequest(
            "updatedName1","",orgId1,orgId2);

        Map<String, Object> orgUpdatedNameResponse = professionalReferenceDataClient
            .updateOrgName(organisationNameUpdateRequest,hmctsAdmin);

        LinkedHashMap responses = (LinkedHashMap)orgUpdatedNameResponse.get("response_body");
        assertThat(responses.get("status")).isEqualTo("partial_success");

        //verify successfully saved
        verifyRetrievedOrg(orgId1,"updatedName1");

        //verify error response
        ArrayList responseList = (ArrayList)responses.get("names");
        LinkedHashMap result  = (LinkedHashMap)responseList.get(1);
        assertThat(result.get("organisationId")).isEqualTo(orgId2);
        assertThat(result.get("status")).isEqualTo("failure");
        assertThat(result.get("statusCode")).isEqualTo(400);
        assertThat(result.get("message")).isEqualTo("Organisation name is missing");

        deleteCreatedTestOrganisations(orgId1,  orgId2);
    }

    @Test
    void update_name_missing_in_all_requests_should_return_failure() {
        //create organisation
        String orgId1 = getActiveOrganisationId();
        String orgId2 = getActiveOrganisationId();

        //create request to update organisation
        OrganisationNameUpdateRequest organisationNameUpdateRequest = createOrganisationNameUpdateRequest(
            "",null,orgId1,orgId2);

        Map<String, Object> orgUpdatedNameResponse = professionalReferenceDataClient
                .updateOrgName(organisationNameUpdateRequest,hmctsAdmin);

        LinkedHashMap responses = (LinkedHashMap)orgUpdatedNameResponse.get("response_body");

        assertThat(responses.get("status")).isEqualTo("failure");

        ArrayList responseList = (ArrayList)responses.get("names");
        LinkedHashMap firstResult  = (LinkedHashMap)responseList.get(0);
        LinkedHashMap secondResult  = (LinkedHashMap)responseList.get(1);
        assertThat(secondResult.get("organisationId")).isEqualTo(orgId2);
        assertThat(secondResult.get("status")).isEqualTo("failure");
        assertThat(secondResult.get("statusCode")).isEqualTo(400);
        assertThat(secondResult.get("message")).isEqualTo("Organisation name is missing");

        assertThat(firstResult.get("organisationId")).isEqualTo(orgId1);
        assertThat(firstResult.get("status")).isEqualTo("failure");
        assertThat(firstResult.get("statusCode")).isEqualTo(400);
        assertThat(firstResult.get("message")).isEqualTo("Organisation name is missing");

        deleteCreatedTestOrganisations(orgId1,  orgId2);
    }

    @Test
    void orgId_missing_in_all_requests_should_return_failure() {
        //create request to update organisation
        OrganisationNameUpdateRequest organisationNameUpdateRequest = createOrganisationNameUpdateRequest(
              "updatedName1","updatedName2","1235874596555555",null);

        Map<String, Object> orgUpdatedNameResponse = professionalReferenceDataClient
                .updateOrgName(organisationNameUpdateRequest,hmctsAdmin);

        LinkedHashMap responses = (LinkedHashMap)orgUpdatedNameResponse.get("response_body");

        assertThat(responses.get("status")).isEqualTo("failure");

        ArrayList responseList = (ArrayList)responses.get("names");
        LinkedHashMap firstResult  = (LinkedHashMap)responseList.get(0);
        LinkedHashMap secondResult  = (LinkedHashMap)responseList.get(1);
        assertThat(secondResult.get("organisationId")).isEqualTo("");
        assertThat(secondResult.get("status")).isEqualTo("failure");
        assertThat(secondResult.get("statusCode")).isEqualTo(400);
        assertThat(secondResult.get("message")).isEqualTo("Organisation id is missing");

        assertThat(firstResult.get("organisationId")).isEqualTo("1235874596555555");
        assertThat(firstResult.get("status")).isEqualTo("failure");
        assertThat(firstResult.get("statusCode")).isEqualTo(400);
        assertThat(firstResult.get("message")).isEqualTo(
            "The given organisationIdentifier must be 7 Alphanumeric Characters");

    }


    /* @Test
      void update_name_with_exception_during_save_returns_500_partial_success() {
          //create organisation
          String orgId1 = getActiveOrganisationId();
          String organisationNameViolatingDatabaseMaxLengthConstraint = RandomStringUtils.random(296);

          //create request to update organisation
          OrganisationNameUpdateRequest organisationNameUpdateRequest= new OrganisationNameUpdateRequest();
          List<OrganisationNameUpdateRequest.OrganisationNameUpdateData> organisationNameUpdateDataList
              = new ArrayList<>();
          OrganisationNameUpdateRequest.OrganisationNameUpdateData organisationNameUpdateData1 =
          new OrganisationNameUpdateRequest.OrganisationNameUpdateData

              (organisationNameViolatingDatabaseMaxLengthConstraint,orgId1);
          organisationNameUpdateDataList.add(organisationNameUpdateData1 );
          organisationNameUpdateRequest.setOrganisationNameUpdateDataList(organisationNameUpdateDataList);

          ResponseEntity<Map> orgUpdatedNameResponse = professionalReferenceDataClient
              .updateOrgNameException(organisationNameUpdateRequest,hmctsAdmin);*/

    /* orgUpdatedNameResponse.body().as(Map.class).get("response_body");

          assertThat(responses.get("status")).isEqualTo("failure");

          ArrayList responseList = (ArrayList)responses.get("names");
          LinkedHashMap result  = (LinkedHashMap)responseList.get(1);
          assertThat(result.get("organisationId")).isEqualTo(orgId1);
          assertThat(result.get("status")).isEqualTo("failure");
          assertThat(result.get("statusCode")).isEqualTo(500);
          assertThat(result.get("message")).isEqualTo("Organisation name is missing");*/

    //     professionalReferenceDataClient.deleteOrganisation( hmctsAdmin,orgId1);

    // }


    private String getActiveOrganisationId() {
        OrganisationCreationRequest organisationCreationRequest2 = someMinimalOrganisationRequest().build();
        String organisationIdentifier = createAndActivateOrganisationWithGivenRequest(organisationCreationRequest2);
        updateOrganisation(organisationIdentifier, hmctsAdmin, "ACTIVE");

        return organisationIdentifier;
    }

    public OrganisationNameUpdateRequest createOrganisationNameUpdateRequest(String name1,String name2,String orgId1,
                                                                             String orgId2) {
        OrganisationNameUpdateRequest organisationNameUpdateRequest = new OrganisationNameUpdateRequest();
        List<OrganisationNameUpdateRequest.OrganisationNameUpdateData> organisationNameUpdateDataList
            = new ArrayList<>();
        OrganisationNameUpdateRequest.OrganisationNameUpdateData organisationNameUpdateData1 =
            new OrganisationNameUpdateRequest.OrganisationNameUpdateData(name1,orgId1);
        OrganisationNameUpdateRequest.OrganisationNameUpdateData organisationNameUpdateData2 =
            new OrganisationNameUpdateRequest.OrganisationNameUpdateData(name2,orgId2);
        organisationNameUpdateDataList.add(organisationNameUpdateData1);
        organisationNameUpdateDataList.add(organisationNameUpdateData2);
        organisationNameUpdateRequest.setOrganisationNameUpdateDataList(organisationNameUpdateDataList);

        return organisationNameUpdateRequest;
    }

    public void verifyRetrievedOrg(String orgId,String expected) {
        Map<String, Object> responseBody =
            professionalReferenceDataClient.retrieveSingleOrganisation(orgId, hmctsAdmin);
        final Object name = responseBody.get("name");
        assertThat(name).isNotNull().isEqualTo(expected);

        LocalDateTime updatedDate =  LocalDateTime.parse(responseBody.get("lastUpdated").toString());
        assertThat(updatedDate.toLocalDate()).isEqualTo(LocalDate.now());
    }

    public void deleteCreatedTestOrganisations(String orgId1, String orgId2) {
        professionalReferenceDataClient.deleteOrganisation(hmctsAdmin,orgId1);
        professionalReferenceDataClient.deleteOrganisation(hmctsAdmin,orgId2);
    }
}
