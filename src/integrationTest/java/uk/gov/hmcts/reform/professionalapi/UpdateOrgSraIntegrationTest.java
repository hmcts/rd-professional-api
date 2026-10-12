package uk.gov.hmcts.reform.professionalapi;

import org.apache.commons.lang.RandomStringUtils;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationSraUpdateRequest;
import uk.gov.hmcts.reform.professionalapi.util.AuthorizationEnabledIntegrationTest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang.RandomStringUtils.randomAlphabetic;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.professionalapi.helper.OrganisationFixtures.someMinimalOrganisationRequest;


class UpdateOrgSraIntegrationTest extends AuthorizationEnabledIntegrationTest {


    @Test
    void update_sra_of_an_active_organisation_should_return_success_if_all_requests_updated() {
        //create organisation
        String orgId1 = getActiveOrganisationId();
        String orgId2 = getActiveOrganisationId();
        String sraId1 = randomAlphabetic(7);
        String sraId2 = randomAlphabetic(7);
        //create request to update organisation
        OrganisationSraUpdateRequest organisationSraUpdateRequest = createOrganisationSraUpdateRequest(
            sraId1,sraId2,orgId1,orgId2);
        //updateSra
        Map<String, Object> orgUpdatedSraResponse = professionalReferenceDataClient
            .updateOrgSra(organisationSraUpdateRequest,hmctsAdmin);

        LinkedHashMap responses = (LinkedHashMap)orgUpdatedSraResponse.get("response_body");
        assertThat(responses.get("status")).isEqualTo("success");
        assertThat(responses.get("message")).isEqualTo("All sraIds updated successfully");

        verifyRetrievedOrg(orgId1,sraId1);
        verifyRetrievedOrg(orgId2,sraId2);

        deleteCreatedTestOrganisations(orgId1,  orgId2);
    }

    @Test
    void update_Sra_of_an_active_organisation_should_return_partial_success_if_any_fail() {
        //create organisation
        String orgId1 = getActiveOrganisationId();
        String orgId2 = getActiveOrganisationId();
        String sraId1 = randomAlphabetic(7);

        //create request to update organisation
        OrganisationSraUpdateRequest organisationSraUpdateRequest = createOrganisationSraUpdateRequest(
            sraId1,"",orgId1,orgId2);

        Map<String, Object> orgUpdatedSraResponse = professionalReferenceDataClient
            .updateOrgSra(organisationSraUpdateRequest,hmctsAdmin);

        LinkedHashMap responses = (LinkedHashMap)orgUpdatedSraResponse.get("response_body");
        assertThat(responses.get("status")).isEqualTo("partial_success");

        //verify successfully saved
        verifyRetrievedOrg(orgId1,sraId1);

        //verify error response
        ArrayList responseList = (ArrayList)responses.get("sraIds");
        LinkedHashMap result  = (LinkedHashMap)responseList.get(0);
        assertThat(result.get("organisationId")).isEqualTo(orgId1);
        assertThat(result.get("status")).isEqualTo("success");
        assertThat(result.get("statusCode")).isEqualTo(200);
        assertThat(result.get("message")).isEqualTo("Organisation Attributes updated successfully");
        LinkedHashMap result1  = (LinkedHashMap)responseList.get(1);
        assertThat(result1.get("organisationId")).isEqualTo(orgId1);
        assertThat(result1.get("status")).isEqualTo("success");
        assertThat(result1.get("statusCode")).isEqualTo(200);
        assertThat(result1.get("message")).isEqualTo("SraId updated successfully");
        LinkedHashMap result2  = (LinkedHashMap)responseList.get(2);
        assertThat(result2.get("organisationId")).isEqualTo(orgId2);
        assertThat(result2.get("status")).isEqualTo("failure");
        assertThat(result2.get("statusCode")).isEqualTo(400);
        assertThat(result2.get("message")).isEqualTo("Organisation sraId is missing");

        deleteCreatedTestOrganisations(orgId1,  orgId2);
    }

    @Test
    void update_Sra_missing_in_all_requests_should_return_failure() {
        //create organisation
        String orgId1 = getActiveOrganisationId();
        String orgId2 = getActiveOrganisationId();

        //create request to update organisation
        OrganisationSraUpdateRequest organisationSraUpdateRequest = createOrganisationSraUpdateRequest(
            "",null,orgId1,orgId2);

        Map<String, Object> orgUpdatedSraResponse = professionalReferenceDataClient
            .updateOrgSra(organisationSraUpdateRequest,hmctsAdmin);

        LinkedHashMap responses = (LinkedHashMap)orgUpdatedSraResponse.get("response_body");

        assertThat(responses.get("status")).isEqualTo("failure");

        ArrayList responseList = (ArrayList)responses.get("sraIds");
        LinkedHashMap firstResult  = (LinkedHashMap)responseList.get(0);
        LinkedHashMap secondResult  = (LinkedHashMap)responseList.get(1);
        assertThat(secondResult.get("organisationId")).isEqualTo(orgId2);
        assertThat(secondResult.get("status")).isEqualTo("failure");
        assertThat(secondResult.get("statusCode")).isEqualTo(400);
        assertThat(secondResult.get("message")).isEqualTo("Organisation sraId is missing");

        assertThat(firstResult.get("organisationId")).isEqualTo(orgId1);
        assertThat(firstResult.get("status")).isEqualTo("failure");
        assertThat(firstResult.get("statusCode")).isEqualTo(400);
        assertThat(firstResult.get("message")).isEqualTo("Organisation sraId is missing");

        deleteCreatedTestOrganisations(orgId1,  orgId2);
    }

    @Test
    void orgId_missing_in_all_requests_should_return_failure() {
        String sraId1 = randomAlphabetic(7);
        String sraId2 = randomAlphabetic(7);
        //create request to update organisation
        OrganisationSraUpdateRequest organisationSraUpdateRequest = createOrganisationSraUpdateRequest(
            sraId1,sraId2,"1235874596555555",null);

        Map<String, Object> orgUpdatedSraResponse = professionalReferenceDataClient
            .updateOrgSra(organisationSraUpdateRequest,hmctsAdmin);

        LinkedHashMap responses = (LinkedHashMap)orgUpdatedSraResponse.get("response_body");

        assertThat(responses.get("status")).isEqualTo("failure");

        ArrayList responseList = (ArrayList)responses.get("sraIds");
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



    @Test
    void update_Sra_with_exception_during_save_returns_500_partial_success() {
        //create organisation
        String orgId1 = getActiveOrganisationId();
        String orgId2 = getActiveOrganisationId();

        String sraId2 = randomAlphabetic(7);
        //create request to update organisation
        String organisationSraViolatingDatabaseMaxLengthConstraint = RandomStringUtils.random(296);
        OrganisationSraUpdateRequest organisationSraUpdateRequest = createOrganisationSraUpdateRequest(
            organisationSraViolatingDatabaseMaxLengthConstraint,sraId2,orgId1,orgId2);
        //create request to update organisation

        Map<String, Object> orgUpdatedSraResponse = professionalReferenceDataClient
            .updateOrgSra(organisationSraUpdateRequest,hmctsAdmin);
        LinkedHashMap responses = (LinkedHashMap)orgUpdatedSraResponse.get("response_body");

        assertThat(responses.get("status")).isEqualTo("partial_success");

        ArrayList responseList = (ArrayList)responses.get("sraIds");
        LinkedHashMap result  = (LinkedHashMap)responseList.get(0);
        assertThat(result.get("organisationId")).isEqualTo(orgId1);
        assertThat(result.get("status")).isEqualTo("failure");
        assertThat(result.get("statusCode")).isEqualTo(500);
        assertThat(result.get("message").toString().contains(
            "Failed to update the sraId for the given organisationIdentifier. Reason :"));

        LinkedHashMap result1  = (LinkedHashMap)responseList.get(1);
        assertThat(result1.get("organisationId")).isEqualTo(orgId2);
        assertThat(result1.get("status")).isEqualTo("success");
        assertThat(result1.get("statusCode")).isEqualTo(200);
        assertThat(result1.get("message")).isEqualTo("Organisation Attributes updated successfully");
        verifyRetrievedOrg(orgId2,sraId2);

        LinkedHashMap result2  = (LinkedHashMap)responseList.get(2);
        assertThat(result2.get("organisationId")).isEqualTo(orgId2);
        assertThat(result2.get("status")).isEqualTo("success");
        assertThat(result2.get("statusCode")).isEqualTo(200);
        assertThat(result2.get("message")).isEqualTo("SraId updated successfully");
        verifyRetrievedOrg(orgId2,sraId2);

        deleteCreatedTestOrganisations(orgId1,  orgId2);

    }

    private String getActiveOrganisationId() {
        OrganisationCreationRequest organisationCreationRequest2 = someMinimalOrganisationRequest().build();
        String organisationIdentifier = createAndActivateOrganisationWithGivenRequest(organisationCreationRequest2);
        updateOrganisation(organisationIdentifier, hmctsAdmin, "ACTIVE");

        return organisationIdentifier;
    }

    public OrganisationSraUpdateRequest createOrganisationSraUpdateRequest(String sra1, String sra2, String orgId1,
                                                                           String orgId2) {
        OrganisationSraUpdateRequest organisationSraUpdateRequest = new OrganisationSraUpdateRequest();
        List<OrganisationSraUpdateRequest.OrganisationSraUpdateData> organisationSraUpdateDataList
            = new ArrayList<>();
        OrganisationSraUpdateRequest.OrganisationSraUpdateData organisationSraUpdateData1 =
            new OrganisationSraUpdateRequest.OrganisationSraUpdateData(orgId1,sra1);
        OrganisationSraUpdateRequest.OrganisationSraUpdateData organisationSraUpdateData2 =
            new OrganisationSraUpdateRequest.OrganisationSraUpdateData(orgId2,sra2);
        organisationSraUpdateDataList.add(organisationSraUpdateData1);
        organisationSraUpdateDataList.add(organisationSraUpdateData2);
        organisationSraUpdateRequest.setOrganisationSraUpdateDataList(organisationSraUpdateDataList);

        return organisationSraUpdateRequest;
    }

    public void verifyRetrievedOrg(String orgId,String sraId) {

        Map<String, Object> responseBody =
            professionalReferenceDataClient.retrieveSingleOrganisationForV2Api(orgId, hmctsAdmin);

        final Object Sra = responseBody.get("sraId");
        assertThat(Sra.toString()).isEqualTo(sraId);
        List organisationAttributes = (List)responseBody.get("orgAttributes");
        assertThat(organisationAttributes).isNotNull();

        LinkedHashMap<String, Object> attr = (LinkedHashMap)organisationAttributes.get(0);
        assertThat(attr).isNotNull();
        assertThat(attr.get("key")).isEqualTo("regulators-0");
        assertThat(attr.get("value").toString()).isEqualTo(
            "{\"regulatorType\":\"Solicitor Regulation Authority "
                + "(SRA)\",\"organisationRegistrationNumber\":\"" + sraId + "\"}");



        LocalDateTime updatedDate =  LocalDateTime.parse(responseBody.get("lastUpdated").toString());
        assertThat(updatedDate.toLocalDate()).isEqualTo(LocalDate.now());
    }

    public void deleteCreatedTestOrganisations(String orgId1, String orgId2) {
        professionalReferenceDataClient.deleteOrganisation(hmctsAdmin,orgId1);
        professionalReferenceDataClient.deleteOrganisation(hmctsAdmin,orgId2);
    }



}
