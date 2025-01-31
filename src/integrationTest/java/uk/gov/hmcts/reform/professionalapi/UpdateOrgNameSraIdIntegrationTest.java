package uk.gov.hmcts.reform.professionalapi;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.util.AuthorizationEnabledIntegrationTest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
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
        //updateName
        Map<String, Object> response = professionalReferenceDataClient
            .updateOrgNameSraId(userId,organisationNameSraUpdate,hmctsAdmin);

        assertThat(response.get("http_status")).isEqualTo("204 NO_CONTENT");

        //retrieve saved org to verify
        Map<String,Object> responseBody = retrievedSavedOrg(orgId);
        final Object name = responseBody.get("name");
        assertThat(name).isNotNull().isEqualTo("New Org Name");

        LocalDateTime updatedDate =  LocalDateTime.parse(responseBody.get("lastUpdated").toString());
        assertThat(updatedDate.toLocalDate()).isEqualTo(LocalDate.now());
        deleteCreatedTestOrganisations(orgId);
    }

    @Test
    void update_sraId_of_an_active_organisation_should_return_success() {

        //create request to update organisation
        Map<String,String> organisationNameSraUpdate = new HashMap<>();
        organisationNameSraUpdate.put("sraId","New sraId");
        //create organisation
        String orgId = getActiveOrganisationId();
        String userId = getUserId(orgId);

        Map<String, Object> response = professionalReferenceDataClient
            .updateOrgNameSraId(userId,organisationNameSraUpdate,hmctsAdmin);
        assertThat(response.get("http_status")).isEqualTo("204 NO_CONTENT");

        //retrieve saved org to verify
        Map<String,Object> responseBody = retrievedSavedOrg(orgId);

        final Object sraId = responseBody.get("sraId");
        assertThat(sraId).isNotNull().isEqualTo("New sraId");

        LocalDateTime updatedDate =  LocalDateTime.parse(responseBody.get("lastUpdated").toString());
        assertThat(updatedDate.toLocalDate()).isEqualTo(LocalDate.now());
        deleteCreatedTestOrganisations(orgId);
    }

    @Test
    void update_of_an_active_organisation_should_fail_if_name_null() {

        //create request to update organisation
        Map<String,String> organisationNameSraUpdate = new HashMap<>();
        organisationNameSraUpdate.put("name","");
        //create organisation
        String orgId = getActiveOrganisationId();
        String userId = getUserId(orgId);
        //updateName
        Map<String, Object> response = professionalReferenceDataClient
            .updateOrgNameSraId(userId,organisationNameSraUpdate,hmctsAdmin);

        assertThat(response.get("http_status")).isEqualTo("400");
        assertThat(response.get("response_body")).toString().contains("Organisation name cannot be empty");
        //retrieve saved org to verify that the erntre transaction ios rolled back , sra id is not saved
        Map<String,Object> responseBody = retrievedSavedOrg(orgId);
        final Object name = responseBody.get("name");
        assertThat(name).isNotNull().isEqualTo("some-org-name1");

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
        //updateName
        Map<String, Object> response = professionalReferenceDataClient
            .updateOrgNameSraId(userId,organisationNameSraUpdate,hmctsAdmin);

        assertThat(response.get("http_status")).isEqualTo("400");
        assertThat(response.get("response_body")).toString().contains("Organisation name cannot be empty");
        //retrieve saved org to verify that the erntre transaction ios rolled back , sra id is not saved
        Map<String,Object> responseBody = retrievedSavedOrg(orgId);
        final Object name = responseBody.get("name");
        assertThat(name).isNotNull().isEqualTo("some-org-name1");

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

        Map<String, Object> response = professionalReferenceDataClient
            .updateOrgNameSraId(userId,organisationNameSraUpdate,hmctsAdmin);
        assertThat(response.get("http_status")).isEqualTo("204 NO_CONTENT");

        //retrieve saved org to verify
        Map<String,Object> responseBody = retrievedSavedOrg(orgId);

        final Object sraId = responseBody.get("sraId");
        assertThat(sraId).isNull();

        LocalDateTime updatedDate =  LocalDateTime.parse(responseBody.get("lastUpdated").toString());
        assertThat(updatedDate.toLocalDate()).isEqualTo(LocalDate.now());
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

        Map<String, Object> response = professionalReferenceDataClient
            .updateOrgNameSraId(userId,organisationNameSraUpdate,hmctsAdmin);
        assertThat(response.get("http_status")).isEqualTo("204 NO_CONTENT");

        //retrieve saved org to verify
        Map<String,Object> responseBody = retrievedSavedOrg(orgId);

        final Object sraId = responseBody.get("sraId");
        assertThat(sraId).isNull();

        LocalDateTime updatedDate =  LocalDateTime.parse(responseBody.get("lastUpdated").toString());
        assertThat(updatedDate.toLocalDate()).isEqualTo(LocalDate.now());
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

        Map<String, Object> response = professionalReferenceDataClient
            .updateOrgNameSraId(userId,organisationNameSraUpdate,hmctsAdmin);

        assertThat(response.get("http_status")).isEqualTo("400");
        assertThat(response.get("response_body")).toString().contains("Organisation name cannot be more than 255 "
            + "characters");
        //retrieve saved org to verify that the erntre transaction ios rolled back , sra id is not saved
        Map<String,Object> responseBody = retrievedSavedOrg(orgId);
        final Object savedName = responseBody.get("name");
        assertThat(savedName).isNotNull().isEqualTo("some-org-name1");

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

        Map<String, Object> response = professionalReferenceDataClient
            .updateOrgNameSraId(userId,organisationNameSraUpdate,hmctsAdmin);

        assertThat(response.get("http_status")).isEqualTo("400");
        assertThat(response.get("response_body")).toString().contains("Organisation sraId cannot be more than "
            + "255 characters");
        //retrieve saved org to verify that the erntre transaction ios rolled back , sra id is not saved
        Map<String,Object> responseBody = retrievedSavedOrg(orgId);
        final Object savedSraId = responseBody.get("sraId");
        assertThat(savedSraId).isNotNull().isEqualTo("sra-id1");

        deleteCreatedTestOrganisations(orgId);
    }

    @Test
    void update_of_an_active_organisation_should_fail_if_name_and_sra_both_missing_in_request() {

        //create request to update organisation
        Map<String,String> organisationNameSraUpdate = new HashMap<>();

        //create organisation
        String orgId = getActiveOrganisationId();
        String userId = getUserId(orgId);
        //updateName
        Map<String, Object> response = professionalReferenceDataClient
            .updateOrgNameSraId(userId,organisationNameSraUpdate,hmctsAdmin);

        assertThat(response.get("http_status")).isEqualTo("400");
        assertThat(response.get("response_body")).toString().contains("Request parameters unrecognised");
        //retrieve saved org to verify that the erntre transaction ios rolled back , sra id is not saved
        Map<String,Object> responseBody = retrievedSavedOrg(orgId);
        final Object name = responseBody.get("name");
        assertThat(name).isNotNull().isEqualTo("some-org-name1");

        deleteCreatedTestOrganisations(orgId);
    }


    @Test
    void forbidden_acccess_should_return_failure() {
        //create request to update organisation
        Map<String,String> organisationNameSraUpdate = new HashMap<>();
        organisationNameSraUpdate.put("name","Some Org Name");
        organisationNameSraUpdate.put("sraId","Some sraId");

        Map<String, Object> response = professionalReferenceDataClient
            .updateOrgNameSraId("abcdfefee",organisationNameSraUpdate,hmctsAdmin);

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
        return  professionalReferenceDataClient.retrieveSingleOrganisation(orgId, hmctsAdmin);
    }

    public void deleteCreatedTestOrganisations(String orgId) {
        professionalReferenceDataClient.deleteOrganisation(hmctsAdmin,orgId);
    }
}