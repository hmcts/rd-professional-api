package uk.gov.hmcts.reform.professionalapi;


import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import net.serenitybdd.annotations.WithTag;
import net.serenitybdd.annotations.WithTags;
import net.serenitybdd.junit5.SerenityJUnit5Extension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.professionalapi.controller.request.NewUserCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrgAttributeRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationOtherOrgsCreationRequest;
import uk.gov.hmcts.reform.professionalapi.util.CustomSerenityJUnit5Extension;
import uk.gov.hmcts.reform.professionalapi.util.ToggleEnable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;
import static uk.gov.hmcts.reform.professionalapi.client.ProfessionalApiClient.createOrganisationRequestForV2;
import static wiremock.org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;

@ExtendWith({CustomSerenityJUnit5Extension.class, SerenityJUnit5Extension.class, SpringExtension.class})
@SpringBootTest
@WithTags({@WithTag("testType:Functional")})
@Slf4j
@SuppressWarnings("unchecked")

class ProfessionalExternalUserFunctionalForV2ApiTest extends AuthorizationFunctionalTest {

    String pumBearerToken;
    String pcmBearerToken;
    String pomBearerToken;
    String pfmBearerToken;
    String caseworkerBearerToken;
    String systemUserBearerToken;
    String extActiveOrgId;
    String activeUserEmail;
    String activeUserId;
    String superUserEmail;
    String superUserId;

    OrganisationOtherOrgsCreationRequest organisationOtherOrgsCreationRequest;
    String firstName = "firstName";
    String lastName = "lastName";

    @Test
    @DisplayName("PRD External Test Scenarios For V2 API")
    @ToggleEnable(mapKey = "OrganisationExternalControllerV2"
        + ".createOrganisationUsingExternalController", withFeature = true)
    void testExternalUserScenario() {
        setUpOrgTestData();
        setUpUserBearerTokens(List.of(puiUserManager, puiCaseManager, puiOrgManager, puiFinanceManager, caseworker));
        retrieveOrganisationPbaScenarios();
        findOrganisationScenarios();
    }

    public void setUpOrgTestData() {
        if (isEmpty(extActiveOrgId)) {
            log.info("Setting up organization...");
            superUserEmail = generateRandomEmail();
            organisationOtherOrgsCreationRequest = createOrganisationRequestForV2();
            organisationOtherOrgsCreationRequest.getSuperUser().setEmail(superUserEmail);

            List<OrgAttributeRequest> orgAttributeRequests = new ArrayList<>();
            OrgAttributeRequest orgAttributeRequest = new OrgAttributeRequest();
            orgAttributeRequest.setKey("regulators-0");
            orgAttributeRequest.setValue("{\"regulatorType\":\"Solicitor Regulation Authority (SRA)\","
                + "\"organisationRegistrationNumber\":\"" + randomAlphabetic(10) + "\"}");

            orgAttributeRequests.add(orgAttributeRequest);
            organisationOtherOrgsCreationRequest.setOrgAttributes(orgAttributeRequests);

            organisationOtherOrgsCreationRequest.setStatus("ACTIVE");
            extActiveOrgId = createAndActivateOrganisationWithGivenRequestV2(organisationOtherOrgsCreationRequest,
                    hmctsAdmin);

            Map<String, Object> searchResponse = professionalApiClient
                    .searchOrganisationUsersByStatusInternal(extActiveOrgId, hmctsAdmin, OK);
            List<Map<String, Object>> professionalUsersResponses =
                    (List<Map<String, Object>>) searchResponse.get("users");
            superUserId = (String) (professionalUsersResponses.get(0)).get("userIdentifier");
        }
    }

    public void setUpUserBearerTokens(List<String> roles) {
        if (roles.contains(puiUserManager)) {
            pumBearerToken = inviteUser(puiUserManager);
        }
        if (roles.contains(puiCaseManager)) {
            pcmBearerToken = inviteUser(puiCaseManager);
        }
        if (roles.contains(puiOrgManager)) {
            pomBearerToken = inviteUser(puiOrgManager);
        }
        if (roles.contains(puiFinanceManager)) {
            pfmBearerToken = inviteUser(puiFinanceManager);
        }
        if (roles.contains(caseworker)) {
            caseworkerBearerToken = inviteUser(caseworker);
        }
        if (roles.contains(systemUser)) {
            systemUserBearerToken = inviteUser(systemUser);
        }
    }


    public void findOrganisationScenarios() {
        findOrgByPfmShouldBeSuccess();
        findOrgByPomShouldBeSuccess();
        findOrgByPumShouldBeSuccess();
        findOrgByPcmShouldBeSuccess();
    }


    public void findOrgByPfmShouldBeSuccess() {
        log.info("findOrgByPfmShouldBeSuccess :: STARTED");
        Map<String, Object> response = professionalApiClient.retrieveOrganisationByOrgIdExternalV2(OK,
                professionalApiClient.getMultipleAuthHeaders(pfmBearerToken));
        assertThat(response.get("paymentAccount")).asList().hasSize(3);
        assertThat(response.get("pendingPaymentAccount")).asList().hasSize(0);
        assertThat(response.get("orgType")).isEqualTo("Solicitor");
        assertThat(response.get("orgAttributes")).isNotNull();
        log.info("findOrgByPfmShouldBeSuccess :: END");
        responseValidate(response);
    }

    public void findOrgByPomShouldBeSuccess() {
        log.info("findOrgByPomShouldBeSuccess :: STARTED");
        Map<String, Object> response = professionalApiClient.retrieveOrganisationByOrgIdExternalV2(OK,
                professionalApiClient.getMultipleAuthHeaders(pomBearerToken));
        assertThat(response.get("paymentAccount")).asList().hasSize(3);
        assertThat(response.get("pendingPaymentAccount")).asList().hasSize(0);
        assertThat(response.get("orgType")).isEqualTo("Solicitor");
        assertThat(response.get("orgAttributes")).isNotNull();
        log.info("findOrgByPomShouldBeSuccess :: END");
    }

    public void findOrgByPumShouldBeSuccess() {
        log.info("findOrgByPumShouldBeSuccess :: STARTED");
        Map<String, Object> response = professionalApiClient.retrieveOrganisationByOrgIdExternalV2(OK,
                professionalApiClient.getMultipleAuthHeaders(pumBearerToken));
        responseValidateV2(response);
        log.info("findOrgByPumShouldBeSuccess :: END");
    }

    public void findOrgByPcmShouldBeSuccess() {
        log.info("findOrgByPcmShouldBeSuccess :: STARTED");
        Map<String, Object> response = professionalApiClient.retrieveOrganisationByOrgIdExternalV2(OK,
                professionalApiClient.getMultipleAuthHeaders(pcmBearerToken));
        responseValidateV2(response);
        log.info("findOrgByPcmShouldBeSuccess :: END");
    }


    public String inviteUser(String role) {
        log.info("Inviting user for role - {}", role);
        List<String> userRoles = new ArrayList<>();
        activeUserEmail = generateRandomEmail();
        userRoles.add(role);
        NewUserCreationRequest newUserCreationRequest = createUserRequest(userRoles);
        newUserCreationRequest.setEmail(activeUserEmail);
        String bearerToken = idamOpenIdClient.getExternalOpenIdToken(puiUserManager,
                "firstName", "lastName", activeUserEmail);

        Map<String, Object> pumInternalUserResponse = professionalApiClient
                .addNewUserToAnOrganisation(extActiveOrgId, hmctsAdmin, newUserCreationRequest, CREATED);
        assertThat(pumInternalUserResponse.get("userIdentifier")).isNotNull();
        activeUserId = (String) pumInternalUserResponse.get("userIdentifier");
        return bearerToken;
    }



    public void retrieveOrganisationPbaScenarios() {
        findOrganisationPbaWithEmailByExternalUserShouldBeSuccess();
        findOrganisationPbaWithEmailThroughHeaderByExternalUserShouldBeSuccess();
        findOrganisationPbaWithoutEmailByExternalUserShouldBeBadRequestV2();
    }

    public void findOrganisationPbaWithEmailByExternalUserShouldBeSuccess() {
        log.info("findOrganisationPbaWithEmailByExternalUserShouldBeSuccess :: STARTED");
        Map<String, Object> orgResponse = professionalApiClient.retrievePaymentAccountsByEmailForExternalV2(
                OK,professionalApiClient.getMultipleAuthHeaders(pumBearerToken),superUserEmail.toLowerCase());

        validatePbaResponse(orgResponse);
        log.info("findOrganisationPbaWithEmailByExternalUserShouldBeSuccess :: END");
    }

    public void findOrganisationPbaWithEmailThroughHeaderByExternalUserShouldBeSuccess() {
        log.info("findOrganisationPbaWithEmailThroughHeaderByExternalUserShouldBeSuccess :: STARTED");
        Map<String, Object> orgResponse = professionalApiClient
                .retrievePaymentAccountsByEmailFromHeaderV2ForExternal(OK,professionalApiClient
                        .getMultipleAuthHeaders(pumBearerToken),superUserEmail.toLowerCase());

        validatePbaResponse(orgResponse);
        log.info("findOrganisationPbaWithEmailThroughHeaderByExternalUserShouldBeSuccess :: END");
    }

    public void findOrganisationPbaWithoutEmailByExternalUserShouldBeBadRequestV2() {
        log.info("findOrganisationPbaWithoutEmailByExternalUserShouldBeBadRequest :: STARTED");


        professionalApiClient.retrievePaymentAccountsWithoutEmailForExternalV2(
                professionalApiClient.getMultipleAuthHeaders(pumBearerToken));

        log.info("findOrganisationPbaWithoutEmailByExternalUserShouldBeBadRequest :: END");
    }


    @Test
    void updateOrganisationNameSraShouldReturnSuccess() {
        setUpOrgTestData();
        setUpUserBearerTokens(List.of(puiOrgManager));
        log.info("updateOrganisationNameShouldReturnSuccess :: STARTED");

        String updateName = randomAlphabetic(10);
        String updateSraId = randomAlphabetic(10);

        Map<String,String> organisationNameSraUpdate = new HashMap<>();
        organisationNameSraUpdate.put("name",updateName);
        organisationNameSraUpdate.put("sraId",updateSraId);

        //retrieve saved organisation by id
        var orgResponseBeforeUpdate = professionalApiClient.retrieveOrganisationDetailsForV2(extActiveOrgId, hmctsAdmin,
            OK);
        assertThat(orgResponseBeforeUpdate).isNotNull();

        //call endpoint to update name as 'updatedname'
        Response orgUpdatedResponse = professionalApiClient.updatesOrganisationDetails(organisationNameSraUpdate,
            professionalApiClient.getMultipleAuthHeaders(pomBearerToken));
        assertNotNull(orgUpdatedResponse);
        assertThat(orgUpdatedResponse.statusCode()).isEqualTo(204);

        //retrieve saved organisation by id
        var orgResponse = professionalApiClient.retrieveOrganisationDetailsForV2(extActiveOrgId, hmctsAdmin, OK);
        assertThat(orgResponse).isNotNull();


        final Object orgName = orgResponse.get("name");
        assertThat(orgName).isNotNull().isEqualTo(updateName);
        final Object sraId = orgResponse.get("sraId");
        assertThat(sraId).isNotNull().isEqualTo(updateSraId);

        LocalDateTime updatedDate = LocalDateTime.parse(orgResponse.get("lastUpdated").toString());
        assertThat(updatedDate.toLocalDate()).isEqualTo(LocalDate.now());


        List<HashMap> saveOrgAtributes = (List<HashMap>) orgResponse.get("orgAttributes");
        HashMap orgAttribSaved = saveOrgAtributes.get(0);
        String key = (String)orgAttribSaved.get("key");
        String value = (String)orgAttribSaved.get("value");

        assertThat(key).isEqualTo("regulators-0");
        assertThat(value).isEqualTo("{\"regulatorType\":\"Solicitor Regulation Authority (SRA)\","
            + "\"organisationRegistrationNumber\":\"" + updateSraId + "\"}");

        log.info("updateOrganisationNameShouldReturnSuccess :: END");

    }



    @Test
    void updateOrganisationSraShouldReturnSuccess() {
        setUpOrgTestData();
        setUpUserBearerTokens(List.of(puiOrgManager));
        log.info("updateOrganisationNameShouldReturnSuccess :: STARTED");

        String updateSraId = randomAlphabetic(10);
        //retrieve saved organisation by id
        var existingOrgResponse = professionalApiClient.retrieveOrganisationDetails(extActiveOrgId, hmctsAdmin, OK);
        assertThat(existingOrgResponse).isNotNull();

        Map<String,String> organisationNameSraUpdate = new HashMap<>();
        organisationNameSraUpdate.put("sraId",updateSraId);

        //call endpoint to update name as 'updatedname'
        Response orgUpdatedResponse = professionalApiClient.updatesOrganisationDetails(organisationNameSraUpdate,
            professionalApiClient.getMultipleAuthHeaders(pomBearerToken));
        assertNotNull(orgUpdatedResponse);
        assertThat(orgUpdatedResponse.statusCode()).isEqualTo(204);

        //retrieve saved organisation by id
        var orgResponse = professionalApiClient.retrieveOrganisationDetailsForV2(extActiveOrgId, hmctsAdmin, OK);
        assertThat(orgResponse).isNotNull();

        final Object sraId = orgResponse.get("sraId");
        assertThat(sraId).isNotNull().isEqualTo(updateSraId);
        final Object orgName = orgResponse.get("name");
        assertThat(orgName).isNotNull().isEqualTo(existingOrgResponse.get("name"));

        LocalDateTime updatedDate = LocalDateTime.parse(orgResponse.get("lastUpdated").toString());
        assertThat(updatedDate.toLocalDate()).isEqualTo(LocalDate.now());

        List<HashMap> saveOrgAtributes = (List<HashMap>) orgResponse.get("orgAttributes");
        HashMap orgAttribSaved = saveOrgAtributes.get(0);
        String key = (String)orgAttribSaved.get("key");
        String value = (String)orgAttribSaved.get("value");

        assertThat(key).isEqualTo("regulators-0");
        assertThat(value).isEqualTo("{\"regulatorType\":\"Solicitor Regulation Authority (SRA)\","
            + "\"organisationRegistrationNumber\":\"" + updateSraId + "\"}");

        log.info("updateOrganisationNameShouldReturnSuccess :: END");

    }


    @Test
    void updateOrganisationSraShouldReturnSuccessIfSraIdEmpty() {
        setUpOrgTestData();
        setUpUserBearerTokens(List.of(puiOrgManager));
        log.info("updateOrganisationNameShouldReturnSuccess :: STARTED");

        String updateSraId = "  ";
        //retrieve saved organisation by id
        var orgBeforeUpdate = professionalApiClient.retrieveOrganisationDetailsForV2(extActiveOrgId, hmctsAdmin, OK);
        assertThat(orgBeforeUpdate).isNotNull();

        Map<String,String> organisationNameSraUpdate = new HashMap<>();
        organisationNameSraUpdate.put("sraId",updateSraId);

        //call endpoint to update name as 'updatedname'
        Response orgUpdatedResponse = professionalApiClient.updatesOrganisationDetails(organisationNameSraUpdate,
            professionalApiClient.getMultipleAuthHeaders(pomBearerToken));
        assertNotNull(orgUpdatedResponse);
        assertThat(orgUpdatedResponse.statusCode()).isEqualTo(204);

        //retrieve saved organisation by id
        var orgResponseAfterUpdate = professionalApiClient.retrieveOrganisationDetailsForV2(extActiveOrgId, hmctsAdmin,
            OK);
        assertThat(orgResponseAfterUpdate).isNotNull();

        // when empty or null SRA id will be updated as null
        final Object sraId = orgResponseAfterUpdate.get("sraId");
        assertThat(sraId).isNull();
        //name will not be changed
        final Object orgName = orgResponseAfterUpdate.get("name");
        assertThat(orgName).isNotNull().isEqualTo(orgBeforeUpdate.get("name"));

        LocalDateTime updatedDate = LocalDateTime.parse(orgResponseAfterUpdate.get("lastUpdated").toString());
        assertThat(updatedDate.toLocalDate()).isEqualTo(LocalDate.now());

        //to check that initially there was one SRAID attribute in the attributes table
        List<HashMap> existingAtributesBeforeUpdate = (List<HashMap>) orgBeforeUpdate.get("orgAttributes");
        HashMap orgAttribBeforeSave = existingAtributesBeforeUpdate.get(0);
        String key = (String)orgAttribBeforeSave.get("key");
        String value = (String)orgAttribBeforeSave.get("value");
        assertThat(key).isEqualTo("regulators-0");
        assertThat(value).contains("\"regulatorType\":\"Solicitor Regulation Authority (SRA)\"");

        // after update when SRA id null or empty the record is deleted
        List<HashMap> attributesAfterUpdate = (List<HashMap>) orgResponseAfterUpdate.get("orgAttributes");
        assertThat(attributesAfterUpdate).hasSize(0);

        log.info("updateOrganisationNameShouldReturnSuccess :: END");

    }

    @Test
    void updateOrganisationNameOnlyShouldReturnSuccess() {
        log.info("updateOrganisationNameOnlyShouldReturnSuccess :: STARTED");
        setUpOrgTestData();
        setUpUserBearerTokens(List.of(puiOrgManager));

        //call endpoint to retrieve existing name for verification
        var existingOrgResponse = professionalApiClient.retrieveOrganisationDetailsForV2(extActiveOrgId, hmctsAdmin,
            OK);
        assertThat(existingOrgResponse).isNotNull();

        String updateName = randomAlphabetic(10);

        Map<String,String> organisationNameSraUpdate = new HashMap<>();
        organisationNameSraUpdate.put("name",updateName);

        //call endpoint to update name
        Response orgUpdatedResponse = professionalApiClient.updatesOrganisationDetails(organisationNameSraUpdate,
            professionalApiClient.getMultipleAuthHeaders(pomBearerToken));
        assertNotNull(orgUpdatedResponse);
        assertThat(orgUpdatedResponse.statusCode()).isEqualTo(204);

        //retrieve saved organisation by id
        var orgResponse = professionalApiClient.retrieveOrganisationDetailsForV2(extActiveOrgId, hmctsAdmin, OK);
        assertThat(orgResponse).isNotNull();

        final Object orgName = orgResponse.get("name");
        assertThat(orgName).isNotNull().isEqualTo(updateName);
        final Object sraId = orgResponse.get("sraId");
        final Object existinsraId = existingOrgResponse.get("sraId");
        assertThat(sraId).isNotNull().isEqualTo(existinsraId);

        LocalDateTime updatedDate = LocalDateTime.parse(orgResponse.get("lastUpdated").toString());
        assertThat(updatedDate.toLocalDate()).isEqualTo(LocalDate.now());

        List<HashMap> saveOrgAtributes = (List<HashMap>) orgResponse.get("orgAttributes");
        HashMap orgAttribSaved = saveOrgAtributes.get(0);
        String key = (String)orgAttribSaved.get("key");
        String value = (String)orgAttribSaved.get("value");

        List<HashMap> existingOrgAtributes = (List<HashMap>) existingOrgResponse.get("orgAttributes");
        HashMap orgAttribOriginal = existingOrgAtributes.get(0);
        String keyBefore = (String)orgAttribOriginal.get("key");
        String valueBefore = (String)orgAttribOriginal.get("value");

        //attribute table not updated
        assertThat(key).isEqualTo(keyBefore);
        assertThat(value).isEqualTo(valueBefore);

        log.info("updateOrganisationNameOnlyShouldReturnSuccess :: END");

    }


    @Test
    void updateOrganisationShouldReturnFailureIfNoNameValueInMapButKeyExists() {
        log.info("updateOrganisationShouldReturnFailureIfNoNameValueInMapButKeyExists :: STARTED");
        setUpOrgTestData();
        setUpUserBearerTokens(List.of(puiOrgManager));
        String updateSraId = randomAlphabetic(10);

        Map<String,String> organisationNameSraUpdate = new HashMap<>();
        organisationNameSraUpdate.put("sraId",updateSraId);
        organisationNameSraUpdate.put("name","");
        //call endpoint to update
        Response orgUpdatedResponse =  professionalApiClient.updatesOrganisationDetails(organisationNameSraUpdate,
            professionalApiClient.getMultipleAuthHeaders(pomBearerToken));
        assertNotNull(orgUpdatedResponse);
        assertThat(orgUpdatedResponse.statusCode()).isEqualTo(400);
        assertThat(orgUpdatedResponse.getBody().prettyPrint()).contains("Organisation name cannot be empty");

        log.info("updateOrganisationShouldReturnFailureIfNoNameValueInMapButKeyExists :: END");
    }


    @Test
    void updateOrganisationSraIdShouldReturnFailureIfTooLong() {
        log.info("updateOrganisationNameSraIdShouldReturnFailureIfTooLong :: STARTED");
        setUpOrgTestData();
        setUpUserBearerTokens(List.of(puiOrgManager));

        //retrieve existing org details
        var existingOrgResponse = professionalApiClient.retrieveOrganisationDetailsForV2(extActiveOrgId, hmctsAdmin,
            OK);
        assertThat(existingOrgResponse).isNotNull();

        String updateSraId = randomAlphabetic(256);
        Map<String,String> organisationNameSraUpdate = new HashMap<>();
        organisationNameSraUpdate.put("sraId",updateSraId);
        //call endpoint to update
        Response orgUpdatedResponse = professionalApiClient.updatesOrganisationDetails(organisationNameSraUpdate,
            professionalApiClient.getMultipleAuthHeaders(pomBearerToken));
        assertNotNull(orgUpdatedResponse);
        assertThat(orgUpdatedResponse.statusCode()).isEqualTo(400);
        assertThat(orgUpdatedResponse.getBody().prettyPrint())
            .contains("Organisation sraId cannot be more than 164 characters is returned");

        //retrieve organisation by id after update to show nothing was saved
        var orgResponse = professionalApiClient.retrieveOrganisationDetailsForV2(extActiveOrgId, hmctsAdmin, OK);
        assertThat(orgResponse).isNotNull();

        final Object orgName = orgResponse.get("name");
        final Object existingname = existingOrgResponse.get("name");
        assertThat(orgName).isNotNull().isEqualTo(existingname);
        final Object sraId = orgResponse.get("sraId");
        final Object existingsraId = existingOrgResponse.get("sraId");
        assertThat(sraId).isNotNull().isEqualTo(existingsraId);

        List<HashMap> saveOrgAtributes = (List<HashMap>) orgResponse.get("orgAttributes");
        HashMap orgAttribSaved = saveOrgAtributes.get(0);
        String key = (String)orgAttribSaved.get("key");
        String value = (String)orgAttribSaved.get("value");

        List<HashMap> existingOrgAtributes = (List<HashMap>) existingOrgResponse.get("orgAttributes");
        HashMap orgAttribOriginal = existingOrgAtributes.get(0);
        String keyBefore = (String)orgAttribOriginal.get("key");
        String valueBefore = (String)orgAttribOriginal.get("value");

        //assert no change in value
        assertThat(key).isEqualTo(keyBefore);
        assertThat(value).isEqualTo(valueBefore);

        log.info("updateOrganisationNameSraIdShouldReturnFailureIfTooLong :: END");
    }


    @Test
    void updateOrganisationNameShouldReturnFailureIfTooLong() {
        log.info("updateOrganisationNameSraIdShouldReturnFailureIfTooLong :: STARTED");
        setUpOrgTestData();
        setUpUserBearerTokens(List.of(puiOrgManager));
        //retrieve existing org details
        var existingOrgResponse = professionalApiClient.retrieveOrganisationDetailsForV2(extActiveOrgId, hmctsAdmin,
            OK);
        assertThat(existingOrgResponse).isNotNull();

        String updateName = randomAlphabetic(256);
        Map<String,String> organisationNameSraUpdate = new HashMap<>();
        organisationNameSraUpdate.put("name",updateName);
        //call endpoint to update
        Response orgUpdatedResponse = professionalApiClient.updatesOrganisationDetails(organisationNameSraUpdate,
            professionalApiClient.getMultipleAuthHeaders(pomBearerToken));
        assertNotNull(orgUpdatedResponse);
        assertThat(orgUpdatedResponse.statusCode()).isEqualTo(400);
        assertThat(orgUpdatedResponse.getBody().prettyPrint())
            .contains("Organisation name cannot be more than 255 characters");


        //call endpoint to show name and sra id were nto changed
        var orgResponseAfterUpdate = professionalApiClient.retrieveOrganisationDetailsForV2(extActiveOrgId, hmctsAdmin,
            OK);
        assertThat(existingOrgResponse).isNotNull();

        final Object orgName = orgResponseAfterUpdate.get("name");
        final Object existingname = existingOrgResponse.get("name");
        assertThat(orgName).isNotNull().isEqualTo(existingname);
        final Object sraId = orgResponseAfterUpdate.get("sraId");
        final Object existingsraId = existingOrgResponse.get("sraId");
        assertThat(sraId).isNotNull().isEqualTo(existingsraId);

        List<HashMap> saveOrgAtributes = (List<HashMap>) orgResponseAfterUpdate.get("orgAttributes");
        HashMap orgAttribSaved = saveOrgAtributes.get(0);
        String key = (String)orgAttribSaved.get("key");
        String value = (String)orgAttribSaved.get("value");

        List<HashMap> existingOrgAtributes = (List<HashMap>) existingOrgResponse.get("orgAttributes");
        HashMap orgAttribOriginal = existingOrgAtributes.get(0);
        String keyBefore = (String)orgAttribOriginal.get("key");
        String valueBefore = (String)orgAttribOriginal.get("value");

        //assert no change in value
        assertThat(key).isEqualTo(keyBefore);
        assertThat(value).isEqualTo(valueBefore);

        log.info("updateOrganisationNameSraIdShouldReturnFailureIfTooLong :: END");
    }



    @Test
    void updateOrganisationNameSraIdShouldReturnFailureIfNoOrgIdForAnyRole() {
        log.info("updateOrganisationNameSraIdShouldReturnFailureIfNoOrgIdForAnyRole :: STARTED");

        setUpOrgTestData();
        String updateName = randomAlphabetic(10);
        String updateSraId = randomAlphabetic(10);

        Map<String,String> organisationNameSraUpdate = new HashMap<>();
        organisationNameSraUpdate.put("name",updateName);
        organisationNameSraUpdate.put("sraId",updateSraId);

        //call endpoint to update with no org id does not create token invalid authentication 401
        Response orgUpdatedResponse = professionalApiClient.updatesOrganisationDetails(organisationNameSraUpdate,
            professionalApiClient.getMultipleAuthHeaders(null));
        assertNotNull(orgUpdatedResponse);
        assertThat(orgUpdatedResponse.statusCode()).isEqualTo(401);
        log.info("updateOrganisationNameSraIdShouldReturnFailureIfNoOrgIdForAnyRole :: END");
    }


    @Test
    void updateOrganisationSraIdShouldReturnFailureIfUnAuthorisedRole() {
        log.info("updateOrganisationSraIdShouldReturnFailureIfUnAuthorised :: STARTED");
        setUpOrgTestData();
        setUpUserBearerTokens(List.of(puiUserManager));

        String updateName = randomAlphabetic(10);
        String updateSraId = randomAlphabetic(10);

        Map<String,String> organisationNameSraUpdate = new HashMap<>();
        organisationNameSraUpdate.put("name",updateName);
        organisationNameSraUpdate.put("sraId",updateSraId);

        //call endpoint to update with user manager role not allowed
        Response orgUpdatedResponse = professionalApiClient.updatesOrganisationDetails(organisationNameSraUpdate,
            professionalApiClient.getMultipleAuthHeaders(puiUserManager));
        assertNotNull(orgUpdatedResponse);
        assertThat(orgUpdatedResponse.statusCode()).isEqualTo(401);

        log.info("updateOrganisationSraIdShouldReturnFailureIfUnAuthorised :: END");
    }



}
