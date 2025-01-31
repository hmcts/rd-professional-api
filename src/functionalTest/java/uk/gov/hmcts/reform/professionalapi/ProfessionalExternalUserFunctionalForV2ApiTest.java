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
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationOtherOrgsCreationRequest;
import uk.gov.hmcts.reform.professionalapi.util.CustomSerenityJUnit5Extension;
import uk.gov.hmcts.reform.professionalapi.util.ToggleEnable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang.RandomStringUtils.randomAlphabetic;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;
import static uk.gov.hmcts.reform.professionalapi.client.ProfessionalApiClient.createOrganisationRequestForV2;

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

        //call endpoint to update name as 'updatedname'
        Response orgUpdatedResponse =  professionalApiClient.updatesOrganisationDetails(organisationNameSraUpdate,
            professionalApiClient.getMultipleAuthHeaders(pomBearerToken));
        assertNotNull(orgUpdatedResponse);
        assertThat(orgUpdatedResponse.statusCode()).isEqualTo(204);

        //retrieve saved organisation by id
        var orgResponse = professionalApiClient.retrieveOrganisationDetails(extActiveOrgId, hmctsAdmin, OK);
        assertThat(orgResponse).isNotNull();

        final Object orgName = orgResponse.get("name");
        assertThat(orgName).isNotNull().isEqualTo(updateName);
        final Object sraId = orgResponse.get("sraId");
        assertThat(sraId).isNotNull().isEqualTo(updateSraId);

        LocalDateTime updatedDate =  LocalDateTime.parse(orgResponse.get("lastUpdated").toString());
        assertThat(updatedDate.toLocalDate()).isEqualTo(LocalDate.now());

        log.info("updateOrganisationNameShouldReturnSuccess :: END");

    }

    @Test
    void updateOrganisationSraShouldReturnSuccess() {
        setUpOrgTestData();
        setUpUserBearerTokens(List.of(puiOrgManager));
        log.info("updateOrganisationNameShouldReturnSuccess :: STARTED");

        String updateSraId = randomAlphabetic(10);

        Map<String,String> organisationNameSraUpdate = new HashMap<>();
        organisationNameSraUpdate.put("sraId",updateSraId);

        //call endpoint to update name as 'updatedname'
        Response orgUpdatedResponse =  professionalApiClient.updatesOrganisationDetails(organisationNameSraUpdate,
            professionalApiClient.getMultipleAuthHeaders(pomBearerToken));
        assertNotNull(orgUpdatedResponse);
        assertThat(orgUpdatedResponse.statusCode()).isEqualTo(204);

        //retrieve saved organisation by id
        var orgResponse = professionalApiClient.retrieveOrganisationDetails(extActiveOrgId, hmctsAdmin, OK);
        assertThat(orgResponse).isNotNull();

        final Object sraId = orgResponse.get("sraId");
        assertThat(sraId).isNotNull().isEqualTo(updateSraId);

        LocalDateTime updatedDate =  LocalDateTime.parse(orgResponse.get("lastUpdated").toString());
        assertThat(updatedDate.toLocalDate()).isEqualTo(LocalDate.now());

        log.info("updateOrganisationNameShouldReturnSuccess :: END");

    }

    @Test
    void updateOrganisationNameOnlyShouldReturnSuccess() {
        log.info("updateOrganisationNameOnlyShouldReturnSuccess :: STARTED");
        setUpOrgTestData();
        setUpUserBearerTokens(List.of(puiOrgManager));

        String updateName = randomAlphabetic(10);

        Map<String,String> organisationNameSraUpdate = new HashMap<>();
        organisationNameSraUpdate.put("name",updateName);

        //call endpoint to update name
        Response orgUpdatedResponse =  professionalApiClient.updatesOrganisationDetails(organisationNameSraUpdate,
            professionalApiClient.getMultipleAuthHeaders(pomBearerToken));
        assertNotNull(orgUpdatedResponse);
        assertThat(orgUpdatedResponse.statusCode()).isEqualTo(204);

        //retrieve saved organisation by id
        var orgResponse = professionalApiClient.retrieveOrganisationDetails(extActiveOrgId, hmctsAdmin, OK);
        assertThat(orgResponse).isNotNull();

        final Object orgName = orgResponse.get("name");
        assertThat(orgName).isNotNull().isEqualTo(updateName);

        LocalDateTime updatedDate =  LocalDateTime.parse(orgResponse.get("lastUpdated").toString());
        assertThat(updatedDate.toLocalDate()).isEqualTo(LocalDate.now());

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
    void updateOrganisationShouldReturnSuccessIfNoNameAddedInMap() {
        log.info("updateOrganisationShouldReturnSuccessIfNoNameAddedInMap :: STARTED");
        setUpOrgTestData();
        setUpUserBearerTokens(List.of(puiOrgManager));
        String updateSraId = randomAlphabetic(10);

        Map<String,String> organisationNameSraUpdate = new HashMap<>();
        organisationNameSraUpdate.put("sraId",updateSraId);

        //call endpoint to update
        Response orgUpdatedResponse =  professionalApiClient.updatesOrganisationDetails(organisationNameSraUpdate,
            professionalApiClient.getMultipleAuthHeaders(pomBearerToken));
        assertNotNull(orgUpdatedResponse);
        assertThat(orgUpdatedResponse.statusCode()).isEqualTo(204);

        //retrieve saved organisation by id
        var orgResponse = professionalApiClient.retrieveOrganisationDetails(extActiveOrgId, hmctsAdmin, OK);
        assertThat(orgResponse).isNotNull();

        final Object orgName = orgResponse.get("sraId");
        assertThat(orgName).isNotNull().isEqualTo(updateSraId);

        LocalDateTime updatedDate =  LocalDateTime.parse(orgResponse.get("lastUpdated").toString());
        assertThat(updatedDate.toLocalDate()).isEqualTo(LocalDate.now());

        log.info("updateOrganisationShouldReturnSuccessIfNoNameAddedInMap :: END");
    }


    @Test
    void updateOrganisationSraIdShouldReturnFailureIfTooLong() {
        log.info("updateOrganisationNameSraIdShouldReturnFailureIfTooLong :: STARTED");
        setUpOrgTestData();
        setUpUserBearerTokens(List.of(puiOrgManager));
        String updateSraId = randomAlphabetic(258);
        Map<String,String> organisationNameSraUpdate = new HashMap<>();
        organisationNameSraUpdate.put("sraId",updateSraId);
        //call endpoint to update
        Response orgUpdatedResponse =  professionalApiClient.updatesOrganisationDetails(organisationNameSraUpdate,
            professionalApiClient.getMultipleAuthHeaders(pomBearerToken));
        assertNotNull(orgUpdatedResponse);
        assertThat(orgUpdatedResponse.statusCode()).isEqualTo(400);
        assertThat(orgUpdatedResponse.getBody().prettyPrint())
            .contains("Organisation sraId cannot be more than 255 characters");

        log.info("updateOrganisationNameSraIdShouldReturnFailureIfTooLong :: END");
    }

    @Test
    void updateOrganisationNameShouldReturnFailureIfTooLong() {
        log.info("updateOrganisationNameSraIdShouldReturnFailureIfTooLong :: STARTED");
        setUpOrgTestData();
        setUpUserBearerTokens(List.of(puiOrgManager));
        String updateName = randomAlphabetic(258);
        Map<String,String> organisationNameSraUpdate = new HashMap<>();
        organisationNameSraUpdate.put("name",updateName);
        //call endpoint to update
        Response orgUpdatedResponse =  professionalApiClient.updatesOrganisationDetails(organisationNameSraUpdate,
            professionalApiClient.getMultipleAuthHeaders(pomBearerToken));
        assertNotNull(orgUpdatedResponse);
        assertThat(orgUpdatedResponse.statusCode()).isEqualTo(400);
        assertThat(orgUpdatedResponse.getBody().prettyPrint())
            .contains("Organisation name cannot be more than 255 characters");

        log.info("updateOrganisationNameSraIdShouldReturnFailureIfTooLong :: END");
    }

    @Test
    void updateOrganisationNameSraIdShouldReturnFailureIfNoOrgId() {
        log.info("updateOrganisationNameSraIdShouldReturnFailureIfNoOrgId :: STARTED");

        setUpOrgTestData();
        setUpUserBearerTokens(List.of(puiOrgManager));
        log.info("updateOrganisationNameShouldReturnSuccess :: STARTED");

        String updateName = randomAlphabetic(10);
        String updateSraId = randomAlphabetic(10);

        Map<String,String> organisationNameSraUpdate = new HashMap<>();
        organisationNameSraUpdate.put("name",updateName);
        organisationNameSraUpdate.put("sraId",updateSraId);

        //call endpoint to update with no org id does not create token invalid authentication 401
        Response orgUpdatedResponse =  professionalApiClient.updatesOrganisationDetails(organisationNameSraUpdate,
            professionalApiClient.getMultipleAuthHeaders(null));
        assertNotNull(orgUpdatedResponse);
        assertThat(orgUpdatedResponse.statusCode()).isEqualTo(401);
        log.info("updateOrganisationNameSraIdShouldReturnFailureIfNoOrgId :: END");
    }


    @Test
    void updateOrganisationSraIdShouldReturnFailureIfUnAuthorised() {
        log.info("updateOrganisationSraIdShouldReturnFailureIfUnAuthorised :: STARTED");
        setUpOrgTestData();
        setUpUserBearerTokens(List.of(puiUserManager));

        String updateName = randomAlphabetic(10);
        String updateSraId = randomAlphabetic(10);

        Map<String,String> organisationNameSraUpdate = new HashMap<>();
        organisationNameSraUpdate.put("name",updateName);
        organisationNameSraUpdate.put("sraId",updateSraId);

        //call endpoint to update with user manager role not allowed
        Response orgUpdatedResponse =  professionalApiClient.updatesOrganisationDetails(organisationNameSraUpdate,
            professionalApiClient.getMultipleAuthHeaders(puiUserManager));
        assertNotNull(orgUpdatedResponse);
        assertThat(orgUpdatedResponse.statusCode()).isEqualTo(401);

        log.info("updateOrganisationSraIdShouldReturnFailureIfUnAuthorised :: END");
    }

}
