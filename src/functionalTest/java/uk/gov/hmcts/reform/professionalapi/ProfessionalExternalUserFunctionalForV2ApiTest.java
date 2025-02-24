package uk.gov.hmcts.reform.professionalapi;


import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import lombok.extern.slf4j.Slf4j;
import net.serenitybdd.annotations.WithTag;
import net.serenitybdd.annotations.WithTags;
import net.serenitybdd.junit5.SerenityJUnit5Extension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.professionalapi.controller.request.NewUserCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationOtherOrgsCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.UpdateContactInformationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.UserCreationRequest;
import uk.gov.hmcts.reform.professionalapi.util.CustomSerenityJUnit5Extension;
import uk.gov.hmcts.reform.professionalapi.util.ToggleEnable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;
import static uk.gov.hmcts.reform.professionalapi.client.ProfessionalApiClient.createOrganisationRequest;
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
        setUpUserBearerTokens(List.of(puiUserManager, puiCaseManager, puiOrgManager, puiFinanceManager,
            caseworker, systemUser));
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
    void updateContactInformationShouldReturnSuccessForOrgManager() {

        setUpOrgTestData();
        setUpUserBearerTokens(List.of(puiOrgManager));
        log.info("updateContactInformationShouldReturnSuccessForOrgManager :: STARTED");

        UpdateContactInformationRequest updateContactInformationRequest =
            new UpdateContactInformationRequest("UPRN1",
                "updatedaddressLine1","updatedaddressLine2","updatedaddressLine3",
                "updatedtownCity","updatedcounty","updatedcountry","RG48TS",
                "","");

        //call endpoint to update contactInformation
        Response orgUpdatedResponse = professionalApiClient.updateContactInformationDetails(
            updateContactInformationRequest,professionalApiClient.getMultipleAuthHeaders(pomBearerToken));
        assertNotNull(orgUpdatedResponse);
        assertThat(orgUpdatedResponse.statusCode()).isEqualTo(204);

        //retrieve saved organisation by id
        var orgResponse = professionalApiClient.retrieveOrganisationDetails(extActiveOrgId, hmctsAdmin, OK);
        assertThat(orgResponse).isNotNull();
        ArrayList<LinkedHashMap<String, Object>> contacts
            = (ArrayList<LinkedHashMap<String, Object>>)orgResponse.get("contactInformation");
        assertThat(contacts.get(0).get("addressLine1")).isNotNull().isEqualTo("updatedaddressLine1");
        assertThat(contacts.get(0).get("addressLine2")).isNotNull().isEqualTo("updatedaddressLine2");
        assertThat(contacts.get(0).get("addressLine3")).isNotNull().isEqualTo("updatedaddressLine3");
        assertThat(contacts.get(0).get("uprn")).isNotNull().isEqualTo("UPRN1");
        assertThat(contacts.get(0).get("townCity")).isNotNull().isEqualTo("updatedtownCity");
        assertThat(contacts.get(0).get("country")).isNotNull().isEqualTo("updatedcountry");
        assertThat(contacts.get(0).get("county")).isNotNull().isEqualTo("updatedcounty");
        assertThat(contacts.get(0).get("postCode")).isNotNull().isEqualTo("RG48TS");

        LocalDateTime updatedDate = LocalDateTime.parse(orgResponse.get("lastUpdated").toString());
        assertThat(updatedDate.toLocalDate()).isEqualTo(LocalDate.now());

        log.info("updateContactInformationShouldReturnSuccessForOrgManager :: END");

    }


    @Test
    public void updateContactInformationShouldReturnSuccessForPrdAdmin() {
        log.info("updateContactInformationShouldReturnSuccessForPrdAdmin :: STARTED");
        setUpOrgTestData();
        String email = generateRandomEmail();
        RequestSpecification superUserToken =
            professionalApiClient.getMultipleAuthHeaders(idamOpenIdClient.getExternalOpenIdTokenWithRetry(
                superUserRoles(), firstName, lastName, email));

        UserCreationRequest superUserRequest = UserCreationRequest
            .aUserCreationRequest()
            .firstName(firstName)
            .lastName(lastName)
            .email(email)
            .build();
        OrganisationCreationRequest organisationCreationRequest = createOrganisationRequest()
            .superUser(superUserRequest)
            .status("ACTIVE").build();
        String organisationIdentifier = createAndActivateOrganisationWithGivenRequest(organisationCreationRequest,
            systemUser);

        NewUserCreationRequest newUserCreationRequest = createUserRequest(Arrays.asList("caseworker"));

        Map<String, Object> newUserResponse = professionalApiClient
            .addNewUserToAnOrganisationExternal(newUserCreationRequest, superUserToken, HttpStatus.CREATED);
        assertThat(newUserResponse).isNotNull();
        assertThat(newUserResponse.get("userIdentifier")).isNotNull();

        UpdateContactInformationRequest updateContactInformationRequest =
            new UpdateContactInformationRequest("UPRN1",
                "updatedaddressLine1","updatedaddressLine2","updatedaddressLine3",
                "updatedtownCity","updatedcounty","updatedcountry","RG48TS",
                "","");
        //call endpoint to update contactInformation
        Response orgUpdatedResponse = professionalApiClient.updateContactInformationDetails(
            updateContactInformationRequest, superUserToken);
        //retrieve saved organisation by id
        var orgResponse = professionalApiClient.retrieveOrganisationDetails(organisationIdentifier, hmctsAdmin, OK);

        assertNotNull(orgUpdatedResponse);
        assertThat(orgUpdatedResponse.statusCode()).isEqualTo(204);

        assertThat(orgResponse).isNotNull();
        ArrayList<LinkedHashMap<String, Object>> contacts
            = (ArrayList<LinkedHashMap<String, Object>>)orgResponse.get("contactInformation");
        assertThat(contacts.get(0).get("addressLine1")).isNotNull().isEqualTo("updatedaddressLine1");
        assertThat(contacts.get(0).get("addressLine2")).isNotNull().isEqualTo("updatedaddressLine2");
        assertThat(contacts.get(0).get("addressLine3")).isNotNull().isEqualTo("updatedaddressLine3");
        assertThat(contacts.get(0).get("uprn")).isNotNull().isEqualTo("UPRN1");
        assertThat(contacts.get(0).get("townCity")).isNotNull().isEqualTo("updatedtownCity");
        assertThat(contacts.get(0).get("country")).isNotNull().isEqualTo("updatedcountry");
        assertThat(contacts.get(0).get("county")).isNotNull().isEqualTo("updatedcounty");
        assertThat(contacts.get(0).get("postCode")).isNotNull().isEqualTo("RG48TS");

        LocalDateTime updatedDate = LocalDateTime.parse(orgResponse.get("lastUpdated").toString());
        assertThat(updatedDate.toLocalDate()).isEqualTo(LocalDate.now());

        log.info("updateContactInformationShouldReturnSuccessForPrdAdmin :: END");
    }

    @Test
    void updateContactInformationWithDxAddressShouldReturnSuccess() {

        setUpOrgTestData();
        setUpUserBearerTokens(List.of(puiOrgManager));
        log.info("updateContactInformationWithDxAddressShouldReturnSuccess :: STARTED");

        UpdateContactInformationRequest updateContactInformationRequest =
            new UpdateContactInformationRequest("UPRN1",
                "updatedaddressLine1","updatedaddressLine2","updatedaddressLine3",
                "updatedtownCity","updatedcounty","updatedcountry","RG48TS",
                "dxUpdatedNum","dxUpdatedExchange");

        //call endpoint to update contactInformation
        Response orgUpdatedResponse = professionalApiClient.updateContactInformationDetails(
            updateContactInformationRequest,professionalApiClient.getMultipleAuthHeaders(pomBearerToken));
        assertNotNull(orgUpdatedResponse);
        assertThat(orgUpdatedResponse.statusCode()).isEqualTo(204);

        //retrieve saved organisation by id
        var orgResponse = professionalApiClient.retrieveOrganisationDetails(extActiveOrgId, hmctsAdmin, OK);
        assertThat(orgResponse).isNotNull();

        ArrayList<LinkedHashMap<String, Object>> contacts
            = (ArrayList<LinkedHashMap<String, Object>>)orgResponse.get("contactInformation");
        assertThat(contacts.get(0).get("addressLine1")).isNotNull().isEqualTo("updatedaddressLine1");
        assertThat(contacts.get(0).get("addressLine2")).isNotNull().isEqualTo("updatedaddressLine2");
        assertThat(contacts.get(0).get("addressLine3")).isNotNull().isEqualTo("updatedaddressLine3");
        assertThat(contacts.get(0).get("uprn")).isNotNull().isEqualTo("UPRN1");
        assertThat(contacts.get(0).get("townCity")).isNotNull().isEqualTo("updatedtownCity");
        assertThat(contacts.get(0).get("country")).isNotNull().isEqualTo("updatedcountry");
        assertThat(contacts.get(0).get("county")).isNotNull().isEqualTo("updatedcounty");
        assertThat(contacts.get(0).get("postCode")).isNotNull().isEqualTo("RG48TS");

        ArrayList dxAddresses = (ArrayList)contacts.get(0).get("dxAddress");
        LinkedHashMap dxAddressMap = ( LinkedHashMap) dxAddresses.get(0);
        assertThat(dxAddressMap.get("dxNumber")).isNotNull().isEqualTo("dxUpdatedNum");
        assertThat(dxAddressMap.get("dxExchange")).isNotNull().isEqualTo("dxUpdatedExchange");

        LocalDateTime updatedDate = LocalDateTime.parse(orgResponse.get("lastUpdated").toString());
        assertThat(updatedDate.toLocalDate()).isEqualTo(LocalDate.now());

        log.info("updateContactInformationWithDxAddressShouldReturnSuccess :: END");

    }


    @Test
    void updateContactInformationFailureUprnLengthIncorrect() {

        setUpOrgTestData();
        setUpUserBearerTokens(List.of(puiOrgManager));
        log.info("updateContactInformationFailureUprnLengthIncorrect :: STARTED");

        UpdateContactInformationRequest updateContactInformationRequest =
            new UpdateContactInformationRequest("UPRN1234567891234567898",
                "updatedaddressLine1","updatedaddressLine2","updatedaddressLine3",
                "updatedtownCity","updatedcounty","updatedcountry","updatedpostCode",
                "dxUpdatedNum","dxUpdatedExchange");

        //call endpoint to update contactInformation
        Response orgUpdatedResponse = professionalApiClient.updateContactInformationDetails(
            updateContactInformationRequest,professionalApiClient.getMultipleAuthHeaders(pomBearerToken));
        assertNotNull(orgUpdatedResponse);
        assertThat(orgUpdatedResponse.statusCode()).isEqualTo(400);
        log.info("updateContactInformationFailureUprnLengthIncorrect :: END");

    }

    @Test
    void updateContactInformationFailureAddressLine1Missing() {
        setUpOrgTestData();
        setUpUserBearerTokens(List.of(puiOrgManager));
        log.info("updateContactInformationFailureAddressLine1Missing :: STARTED");

        UpdateContactInformationRequest updateContactInformationRequest =
            new UpdateContactInformationRequest("UPRN1",
                "","updatedaddressLine2","updatedaddressLine3",
                "updatedtownCity","updatedcounty","updatedcountry","updatedpostCode",
                "dxUpdatedNum","dxUpdatedExchange");

        //call endpoint to update contactInformation
        Response orgUpdatedResponse = professionalApiClient.updateContactInformationDetails(
            updateContactInformationRequest,professionalApiClient.getMultipleAuthHeaders(pomBearerToken));
        assertNotNull(orgUpdatedResponse);
        assertThat(orgUpdatedResponse.statusCode()).isEqualTo(400);
        log.info("updateContactInformationFailureAddressLine1Missing :: END");

    }


    @Test
    void updateContactInfoFailureDxNumberNull() {
        setUpOrgTestData();
        setUpUserBearerTokens(List.of(puiOrgManager));
        log.info("updateContactInfoFailureDxNumberNull :: STARTED");

        UpdateContactInformationRequest updateContactInformationRequest =
            new UpdateContactInformationRequest("UPRN1234567891234567898",
                "updatedaddressLine1","updatedaddressLine2","updatedaddressLine3",
                "updatedtownCity","updatedcounty","updatedcountry","updatedpostCode",
                "","dxUpdatedExchange");

        //call endpoint to update contactInformation
        Response orgUpdatedResponse = professionalApiClient.updateContactInformationDetails(
            updateContactInformationRequest,professionalApiClient.getMultipleAuthHeaders(pomBearerToken));
        assertNotNull(orgUpdatedResponse);
        assertThat(orgUpdatedResponse.statusCode()).isEqualTo(400);
        log.info("updateContactInfoFailureDxNumberNull :: END");

    }

    @Test
    void updateContactInfoFailureDxAddressNull() {
        setUpOrgTestData();
        setUpUserBearerTokens(List.of(puiOrgManager));
        log.info("updateContactInfoFailureDxAddressNull :: STARTED");

        UpdateContactInformationRequest updateContactInformationRequest =
            new UpdateContactInformationRequest("UPRN1234567891234567898",
                "updatedaddressLine1","updatedaddressLine2","updatedaddressLine3",
                "updatedtownCity","updatedcounty","updatedcountry","updatedpostCode",
                "dxNumUpdated","");

        //call endpoint to update contactInformation
        Response orgUpdatedResponse = professionalApiClient.updateContactInformationDetails(
            updateContactInformationRequest,professionalApiClient.getMultipleAuthHeaders(pomBearerToken));
        assertNotNull(orgUpdatedResponse);
        assertThat(orgUpdatedResponse.statusCode()).isEqualTo(400);
        log.info("updateContactInfoFailureDxAddressNull :: END");

    }


}
