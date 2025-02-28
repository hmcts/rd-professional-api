package uk.gov.hmcts.reform.professionalapi;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.util.AuthorizationEnabledIntegrationTest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.professionalapi.helper.OrganisationFixtures.someMinimalOrganisationRequest;

class UpdateOrgNameSraIdIntegrationTest extends AuthorizationEnabledIntegrationTest {

    @Test
    void update_name_of_an_active_organisation_should_return_success() {
        //create request to update organisation
        Map<String,String> organisationNameSraUpdate = new HashMap<>();
        organisationNameSraUpdate.put("name","New Org Name");
        //create organisation
        String orgId = getActiveOrganisationId();
        String userId = getUserId(orgId);

        //fetchOrgBefore Update
        Map<String, Object> orgResponseBeforeUpdate = retrievedSavedOrg(orgId);
        //updateName
        Map<String, Object> response = professionalReferenceDataClient
            .updateOrgNameSraId(userId,organisationNameSraUpdate,puiOrgManager);

        assertThat(response.get("http_status")).isEqualTo("204 NO_CONTENT");

        //fetch org After update
        Map<String, Object> orgResponseAfterUpdate = retrievedSavedOrg(orgId);

        //retrieve saved org to verify
        Map<String,Object> responseBody = retrievedSavedOrg(orgId);
        final Object name = responseBody.get("name");
        assertThat(name).isNotNull().isEqualTo("New Org Name");

        final Object existingsraId = orgResponseBeforeUpdate.get("sraId");
        final Object sraId = orgResponseAfterUpdate.get("sraId");
        assertThat(sraId).isNotNull().isEqualTo(existingsraId);

        LocalDateTime updatedDate =  LocalDateTime.parse(responseBody.get("lastUpdated").toString());
        assertThat(updatedDate.toLocalDate()).isEqualTo(LocalDate.now());

        List<HashMap> saveOrgAtributesBefore = (List<HashMap>) orgResponseBeforeUpdate.get("orgAttributes");
        List<HashMap> saveOrgAtributesAfter = (List<HashMap>) orgResponseAfterUpdate.get("orgAttributes");

        //No attributes were saved
        assertThat(saveOrgAtributesBefore).hasSize(0);
        assertThat(saveOrgAtributesAfter).hasSize(0);
        deleteCreatedTestOrganisations(orgId);
    }

    @Test
    void update_sraId_of_an_active_organisation_should_return_success() {

        String newSraId = "New sraId";
        //create request to update organisation
        Map<String,String> organisationNameSraUpdate = new HashMap<>();
        organisationNameSraUpdate.put("sraId",newSraId);
        //create organisation
        String orgId = getActiveOrganisationId();
        String userId = getUserId(orgId);

        //fetchOrgBefore Update
        Map<String, Object> orgResponseBeforeUpdate = retrievedSavedOrg(orgId);
        //update sraid
        Map<String, Object> response = professionalReferenceDataClient
            .updateOrgNameSraId(userId,organisationNameSraUpdate,puiOrgManager);
        assertThat(response.get("http_status")).isEqualTo("204 NO_CONTENT");
        //fetch org After update
        Map<String, Object> orgResponseAfterUpdate = retrievedSavedOrg(orgId);

        //retrieve saved org to verify
        Map<String,Object> responseBody = retrievedSavedOrg(orgId);

        final Object sraId = responseBody.get("sraId");
        assertThat(sraId).isNotNull().isEqualTo(newSraId);

        final Object existingname = orgResponseBeforeUpdate.get("name");
        final Object name = orgResponseAfterUpdate.get("name");
        assertThat(name).isNotNull().isEqualTo(existingname);

        LocalDateTime updatedDate =  LocalDateTime.parse(responseBody.get("lastUpdated").toString());
        assertThat(updatedDate.toLocalDate()).isEqualTo(LocalDate.now());

        List<HashMap> saveOrgAtributesBefore = (List<HashMap>) orgResponseBeforeUpdate.get("orgAttributes");
        List<HashMap> saveOrgAtributesAfter = (List<HashMap>) orgResponseAfterUpdate.get("orgAttributes");

        // attributes were saved in attrib table
        assertThat(saveOrgAtributesBefore).hasSize(0);
        assertThat(saveOrgAtributesAfter).hasSize(1);

        List<HashMap> saveOrgAtributes = (List<HashMap>) orgResponseAfterUpdate.get("orgAttributes");
        HashMap orgAttribSaved = saveOrgAtributes.get(0);
        String key = (String)orgAttribSaved.get("key");
        String value = (String)orgAttribSaved.get("value");

        assertThat(key).isEqualTo("regulators-0");
        assertThat(value).isEqualTo("{\"regulatorType\":\"Solicitor Regulation Authority (SRA)\","
            + "\"organisationRegistrationNumber\":\"" + newSraId + "\"}");

        deleteCreatedTestOrganisations(orgId);
    }

    @Test
    void update_of_an_active_organisation_should_fail_if_name_null() {

        //create request to update organisation
        Map<String,String> organisationNameSraUpdate = new HashMap<>();
        organisationNameSraUpdate.put("name",null);
        //create organisation
        String orgId = getActiveOrganisationId();
        String userId = getUserId(orgId);
        //fetchOrgBefore Update
        Map<String, Object> orgResponseBeforeUpdate = retrievedSavedOrg(orgId);
        //updateName
        Map<String, Object> response = professionalReferenceDataClient
            .updateOrgNameSraId(userId,organisationNameSraUpdate,puiOrgManager);
        //fetch org After update
        Map<String, Object> orgResponseAfterUpdate = retrievedSavedOrg(orgId);

        assertThat(response.get("http_status")).isEqualTo("400");
        assertThat(response.get("response_body")).toString().contains("Organisation name cannot be empty");
        //retrieve saved org to verify that the entire transaction is rolled back , sra id is not saved
        final Object existingname = orgResponseBeforeUpdate.get("name");
        final Object name = orgResponseAfterUpdate.get("name");
        assertThat(name).isNotNull().isEqualTo(existingname);

        final Object existingsraId = orgResponseBeforeUpdate.get("sraId");
        final Object sraId = orgResponseAfterUpdate.get("sraId");
        assertThat(sraId).isNotNull().isEqualTo(existingsraId);

        List<HashMap> saveOrgAtributesBefore = (List<HashMap>) orgResponseBeforeUpdate.get("orgAttributes");
        List<HashMap> saveOrgAtributesAfter = (List<HashMap>) orgResponseAfterUpdate.get("orgAttributes");

        //No attributes were saved
        assertThat(saveOrgAtributesBefore).hasSize(0);
        assertThat(saveOrgAtributesAfter).hasSize(0);

        deleteCreatedTestOrganisations(orgId);
    }

    @Test
    void update_of_an_active_organisation_should_fail_if_name_Empty() {

        //create request to update organisation
        Map<String,String> organisationNameSraUpdate = new HashMap<>();
        organisationNameSraUpdate.put("name","");
        //create organisation
        String orgId = getActiveOrganisationId();
        String userId = getUserId(orgId);
        //fetchOrgBefore Update
        Map<String, Object> orgResponseBeforeUpdate = retrievedSavedOrg(orgId);
        //updateName
        Map<String, Object> response = professionalReferenceDataClient
            .updateOrgNameSraId(userId,organisationNameSraUpdate,puiOrgManager);
        //fetch org After update
        Map<String, Object> orgResponseAfterUpdate = retrievedSavedOrg(orgId);
        assertThat(response.get("http_status")).isEqualTo("400");
        assertThat(response.get("response_body")).toString().contains("Organisation name cannot be empty");
        //retrieve saved org to verify that the entire transaction is rolled back , sra id is not saved
        final Object existingname = orgResponseBeforeUpdate.get("name");
        final Object name = orgResponseAfterUpdate.get("name");
        assertThat(name).isNotNull().isEqualTo(existingname);

        final Object existingsraId = orgResponseBeforeUpdate.get("sraId");
        final Object sraId = orgResponseAfterUpdate.get("sraId");
        assertThat(sraId).isNotNull().isEqualTo(existingsraId);

        List<HashMap> saveOrgAtributesBefore = (List<HashMap>) orgResponseBeforeUpdate.get("orgAttributes");
        List<HashMap> saveOrgAtributesAfter = (List<HashMap>) orgResponseAfterUpdate.get("orgAttributes");

        //No attributes were saved
        assertThat(saveOrgAtributesBefore).hasSize(0);
        assertThat(saveOrgAtributesAfter).hasSize(0);

        deleteCreatedTestOrganisations(orgId);
    }

    @Test
    void update_sraId_if_sraId_Empty_should_return_success() {

        //create request to update organisation
        Map<String,String> organisationNameSraUpdate = new HashMap<>();
        organisationNameSraUpdate.put("sraId","  ");
        //create organisation
        String orgId = getActiveOrganisationId();
        String userId = getUserId(orgId);

        //fetchOrgBefore Update
        Map<String, Object> orgResponseBeforeUpdate = retrievedSavedOrg(orgId);

        //UpdateSraId
        Map<String, Object> response = professionalReferenceDataClient
            .updateOrgNameSraId(userId,organisationNameSraUpdate,puiOrgManager);
        assertThat(response.get("http_status")).isEqualTo("204 NO_CONTENT");

        //fetch org After update
        Map<String, Object> orgResponseAfterUpdate = retrievedSavedOrg(orgId);

        final Object sraId = orgResponseAfterUpdate.get("sraId");
        assertThat(sraId).isNull();

        final Object existingname = orgResponseBeforeUpdate.get("name");
        final Object name = orgResponseAfterUpdate.get("name");
        assertThat(name).isNotNull().isEqualTo(existingname);

        LocalDateTime updatedDate =  LocalDateTime.parse(orgResponseAfterUpdate.get("lastUpdated").toString());
        assertThat(updatedDate.toLocalDate()).isEqualTo(LocalDate.now());

        List<HashMap> saveOrgAtributesBefore = (List<HashMap>) orgResponseBeforeUpdate.get("orgAttributes");
        List<HashMap> saveOrgAtributesAfter = (List<HashMap>) orgResponseAfterUpdate.get("orgAttributes");

        //When sra id is empty or null no error shown but no attribute entry made either
        assertThat(saveOrgAtributesBefore).hasSize(0);
        assertThat(saveOrgAtributesAfter).hasSize(0);

        deleteCreatedTestOrganisations(orgId);
    }

    @Test
    void update_sraId_if_sraId_null_should_return_success() {

        //create request to update organisation
        Map<String,String> organisationNameSraUpdate = new HashMap<>();
        organisationNameSraUpdate.put("sraId",null);
        //create organisation
        String orgId = getActiveOrganisationId();
        String userId = getUserId(orgId);

        //fetchOrgBefore Update
        Map<String, Object> orgResponseBeforeUpdate = retrievedSavedOrg(orgId);

        Map<String, Object> response = professionalReferenceDataClient
            .updateOrgNameSraId(userId,organisationNameSraUpdate,puiOrgManager);
        assertThat(response.get("http_status")).isEqualTo("204 NO_CONTENT");

        //fetch org After update
        Map<String, Object> orgResponseAfterUpdate = retrievedSavedOrg(orgId);

        final Object sraId = orgResponseAfterUpdate.get("sraId");
        assertThat(sraId).isNull();

        final Object existingname = orgResponseBeforeUpdate.get("name");
        final Object name = orgResponseAfterUpdate.get("name");
        assertThat(name).isNotNull().isEqualTo(existingname);

        LocalDateTime updatedDate =  LocalDateTime.parse(orgResponseAfterUpdate.get("lastUpdated").toString());
        assertThat(updatedDate.toLocalDate()).isEqualTo(LocalDate.now());

        List<HashMap> saveOrgAtributesBefore = (List<HashMap>) orgResponseBeforeUpdate.get("orgAttributes");
        List<HashMap> saveOrgAtributesAfter = (List<HashMap>) orgResponseAfterUpdate.get("orgAttributes");

        //When sra id is empty or null no error shown but no attribute entry made either
        assertThat(saveOrgAtributesBefore).hasSize(0);
        assertThat(saveOrgAtributesAfter).hasSize(0);

        deleteCreatedTestOrganisations(orgId);
    }

    @Test
    void update_name_should_return_failure_if_length_too_long() {
        String orgId = getActiveOrganisationId();
        String userId = getUserId(orgId);
        String name = RandomStringUtils.randomAlphabetic(256);
        //create request to update organisation
        Map<String,String> organisationNameSraUpdate = new HashMap<>();
        organisationNameSraUpdate.put("name",name);

        //fetchOrgBefore Update
        Map<String, Object> orgResponseBeforeUpdate = retrievedSavedOrg(orgId);

        Map<String, Object> response = professionalReferenceDataClient
            .updateOrgNameSraId(userId,organisationNameSraUpdate,puiOrgManager);
        assertThat(response.get("http_status")).isEqualTo("400");
        assertThat(response.get("response_body")).toString().contains("Organisation name cannot be more than 255 "
            + "characters");
        //retrieve saved org to verify that the entire transaction is rolled back , sra id is not saved
        Map<String, Object> orgResponseAfterUpdate = retrievedSavedOrg(orgId);
        final Object savedName = orgResponseAfterUpdate.get("name");
        assertThat(savedName).isNotNull().isEqualTo("some-org-name1");

        final Object existingsraId = orgResponseBeforeUpdate.get("sraId");
        final Object sraId = orgResponseAfterUpdate.get("sraId");
        assertThat(sraId).isNotNull().isEqualTo(existingsraId);

        List<HashMap> saveOrgAtributesBefore = (List<HashMap>) orgResponseBeforeUpdate.get("orgAttributes");
        List<HashMap> saveOrgAtributesAfter = (List<HashMap>) orgResponseAfterUpdate.get("orgAttributes");

        //no entry made into attribute table
        assertThat(saveOrgAtributesBefore).hasSize(0);
        assertThat(saveOrgAtributesAfter).hasSize(0);

        deleteCreatedTestOrganisations(orgId);
    }

    @Test
    void update_sraId_should_return_failure_if_length_too_long() {
        String orgId = getActiveOrganisationId();
        String userId = getUserId(orgId);
        String sraId = RandomStringUtils.randomAlphabetic(256);
        //create request to update organisation
        Map<String,String> organisationNameSraUpdate = new HashMap<>();
        organisationNameSraUpdate.put("sraId",sraId);

        //fetchOrgBefore Update
        Map<String, Object> orgResponseBeforeUpdate = retrievedSavedOrg(orgId);

        Map<String, Object> response = professionalReferenceDataClient
            .updateOrgNameSraId(userId,organisationNameSraUpdate,puiOrgManager);

        assertThat(response.get("http_status")).isEqualTo("400");
        assertThat(response.get("response_body")).toString().contains("Organisation sraId cannot be more than "
            + "255 characters");

        //retrieve saved org to verify that the entire transaction is rolled back , sra id is not saved
        Map<String, Object> orgResponseAfterUpdate = retrievedSavedOrg(orgId);
        final Object savedSraId = orgResponseAfterUpdate.get("sraId");
        assertThat(savedSraId).isNotNull().isEqualTo("sra-id1");

        final Object existingname = orgResponseBeforeUpdate.get("name");
        final Object name = orgResponseAfterUpdate.get("name");
        assertThat(name).isNotNull().isEqualTo(existingname);

        List<HashMap> saveOrgAtributesBefore = (List<HashMap>) orgResponseBeforeUpdate.get("orgAttributes");
        List<HashMap> saveOrgAtributesAfter = (List<HashMap>) orgResponseAfterUpdate.get("orgAttributes");

        //no entry made in org attributes table
        assertThat(saveOrgAtributesBefore).hasSize(0);
        assertThat(saveOrgAtributesAfter).hasSize(0);

        deleteCreatedTestOrganisations(orgId);
    }

    @Test
    void update_of_an_active_organisation_should_fail_if_name_and_sra_both_missing_in_request() {

        //create request to update organisation
        Map<String,String> organisationNameSraUpdate = new HashMap<>();

        //create organisation
        String orgId = getActiveOrganisationId();
        String userId = getUserId(orgId);
        //fetchOrgBefore Update
        Map<String, Object> orgResponseBeforeUpdate = retrievedSavedOrg(orgId);

        //updateName
        Map<String, Object> response = professionalReferenceDataClient
            .updateOrgNameSraId(userId,organisationNameSraUpdate,puiOrgManager);

        assertThat(response.get("http_status")).isEqualTo("400");
        assertThat(response.get("response_body")).toString().contains("Request parameters unrecognised");
        //retrieve saved org to verify that the entire transaction is rolled back , sra id is not saved
        Map<String,Object> orgResponseAfterUpdate = retrievedSavedOrg(orgId);
        final Object name = orgResponseAfterUpdate.get("name");
        final Object existingname = orgResponseBeforeUpdate.get("name");
        assertThat(name).isNotNull().isEqualTo("some-org-name1");
        assertThat(name).isNotNull().isEqualTo(existingname);

        final Object sraId = orgResponseAfterUpdate.get("sraId");
        final Object existingsraId = orgResponseBeforeUpdate.get("sraId");
        assertThat(sraId).isNotNull().isEqualTo(existingsraId);

        List<HashMap> saveOrgAtributesBefore = (List<HashMap>) orgResponseBeforeUpdate.get("orgAttributes");
        List<HashMap> saveOrgAtributesAfter = (List<HashMap>) orgResponseAfterUpdate.get("orgAttributes");

        //no entry in org attributes table
        assertThat(saveOrgAtributesBefore).hasSize(0);
        assertThat(saveOrgAtributesAfter).hasSize(0);

        deleteCreatedTestOrganisations(orgId);
    }


    @Test
    void forbidden_acccess_should_return_failure() {
        //create request to update organisation
        Map<String,String> organisationNameSraUpdate = new HashMap<>();
        organisationNameSraUpdate.put("name","Some Org Name");
        organisationNameSraUpdate.put("sraId","Some sraId");

        Map<String, Object> response = professionalReferenceDataClient
            .updateOrgNameSraId("abcdfefee",organisationNameSraUpdate,puiOrgManager);

        assertThat(response.get("http_status")).isEqualTo("403");
        assertThat(response.get("response_body")).toString().contains("Access Denied");

    }

    @Test
    void unauthorised_acccess_should_return_failure() {
        OrganisationCreationRequest organisationCreationRequest = someMinimalOrganisationRequest().build();
        String orgId = createOrganisationWithGivenRequest(organisationCreationRequest);
        updateOrganisationWithGivenRequest(organisationCreationRequest, orgId, caseworkerCaa, ACTIVE);

        String userId = getUserId(orgId);
        //create request to update organisation
        Map<String,String> organisationNameSraUpdate = new HashMap<>();
        organisationNameSraUpdate.put("name","Some Org Name");

        Map<String, Object> response = professionalReferenceDataClient
            .updateOrgNameSraId(userId,organisationNameSraUpdate,caseworkerCaa);

        assertThat(response.get("http_status")).isEqualTo("403");
        assertThat(response.get("response_body")).toString().contains("Access Denied");
        deleteCreatedTestOrganisations(orgId);
    }

    private String getActiveOrganisationId() {
        OrganisationCreationRequest organisationCreationRequest = someMinimalOrganisationRequest().build();
        String organisationIdentifier = createAndActivateOrganisationWithGivenRequest(organisationCreationRequest);
        updateOrganisation(organisationIdentifier, hmctsAdmin, "ACTIVE");

        return organisationIdentifier;
    }

    private String getUserId(String orgId) {
        String userId = updateOrgAndInviteUser(orgId, hmctsAdmin);
        assertThat(userId).isNotNull();
        return userId;
    }

    public Map<String,Object> retrievedSavedOrg(String orgId) {
        return professionalReferenceDataClient
            .retrieveSingleOrganisationForV2Api(orgId,
                hmctsAdmin);
    }

    public void deleteCreatedTestOrganisations(String orgId) {
        professionalReferenceDataClient.deleteOrganisation(hmctsAdmin,orgId);
    }
}