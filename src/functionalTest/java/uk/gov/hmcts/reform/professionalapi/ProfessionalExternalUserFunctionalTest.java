package uk.gov.hmcts.reform.professionalapi;

import io.restassured.specification.RequestSpecification;
import lombok.extern.slf4j.Slf4j;
import net.serenitybdd.annotations.WithTag;
import net.serenitybdd.annotations.WithTags;
import net.serenitybdd.junit5.SerenityJUnit5Extension;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.professionalapi.controller.request.ContactInformationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.DeleteMultipleAddressRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.NewUserCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.PbaRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.UserCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.response.OrganisationMinimalInfoResponse;
import uk.gov.hmcts.reform.professionalapi.domain.UserAccessType;
import uk.gov.hmcts.reform.professionalapi.domain.UserProfileUpdatedData;
import uk.gov.hmcts.reform.professionalapi.util.CustomSerenityJUnit5Extension;
import uk.gov.hmcts.reform.professionalapi.util.ToggleEnable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static org.assertj.core.api.Assertions.assertThat;
import static org.codehaus.groovy.runtime.InvokerHelper.asList;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.TOO_MANY_REQUESTS;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static uk.gov.hmcts.reform.professionalapi.client.ProfessionalApiClient.createContactInformationCreationRequests;
import static uk.gov.hmcts.reform.professionalapi.client.ProfessionalApiClient.createOrganisationRequest;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.IdamStatus.SUSPENDED;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.ACTIVE;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.FALSE;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.TRUE;

@ExtendWith({CustomSerenityJUnit5Extension.class, SerenityJUnit5Extension.class, SpringExtension.class})
@SpringBootTest
@WithTags({@WithTag("testType:Functional")})
@Slf4j
@SuppressWarnings("unchecked")
class ProfessionalExternalUserFunctionalTest extends AuthorizationFunctionalTest {

    String pumBearerToken;
    String pcmBearerToken;
    String pomBearerToken;
    String pfmBearerToken;
    String caseworkerBearerToken;
    String systemUserBearerToken;
    String extActiveOrgId;
    String activeUserEmail;
    String activeUserId;
    String newAddedUserId;
    String superUserEmail;
    String superUserId;
    OrganisationCreationRequest organisationCreationRequest;
    String firstName = "firstName";
    String lastName = "lastName";

    @Test
    @DisplayName("PRD External Test Scenarios")
    void testExternalUserScenario() {
        setUpOrgTestData();
        setUpUserBearerTokens(List.of(puiUserManager, puiCaseManager, puiOrgManager, puiFinanceManager, caseworker));
        inviteUserScenarios();
        retrieveOrganisationPbaScenarios();
        findUsersByOrganisationScenarios();
        findOrganisationScenarios();
        findActiveOrganisationScenarios();
        reinviteUserScenarios();
        findUserStatusByEmailScenarios();
        modifyRolesScenarios();
        suspendUserScenarios();
    }

    @Test
    @DisplayName("PRD Group Access External Test Scenarios")
    void testExternalUserGroupAccessScenario() {
        setUpOrgTestData();
        setUpUserBearerTokens(List.of(puiUserManager));
        addNewUserAccessTypeScenarios();
        updateAndAddNewUserAccessTypeScenarios();
        inviteNewUserToOrganisationScenarios();
    }

    public void setUpOrgTestData() {
        if (isEmpty(extActiveOrgId)) {
            log.info("Setting up organization...");
            superUserEmail = generateRandomEmail();
            organisationCreationRequest = createOrganisationRequest()
                    .superUser(UserCreationRequest.aUserCreationRequest()
                            .firstName(firstName)
                            .lastName(lastName)
                            .email(superUserEmail)
                            .build())
                    .paymentAccount(Set.of("PBA".concat(RandomStringUtils.randomAlphanumeric(7)),
                            "PBA".concat(RandomStringUtils.randomAlphanumeric(7)),
                            "PBA".concat(RandomStringUtils.randomAlphanumeric(7))))
                    .build();

            organisationCreationRequest.setStatus("ACTIVE");
            extActiveOrgId = createAndActivateOrganisationWithGivenRequest(organisationCreationRequest, hmctsAdmin);

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
    }


    public void inviteUserScenarios() {
        inviteUserByPuiUserManagerShouldBeSuccess();
        // below test receives 504 from SIDAM intermittently, needs investigation:
        // inviteUserBySuperUserShouldBeSuccess();
    }

    public void inviteNewUserToOrganisationScenarios() {
        // Adding and verifying user with access type
        addNewUserToAnOrganisationShouldBeSuccess(true);
        findUsersByUserIdentifier(true);

        // Adding and verifying user without access type
        addNewUserToAnOrganisationShouldBeSuccess(false);
        findUsersByUserIdentifier(false);
    }

    public void findUsersByOrganisationScenarios() {
        findUsersByNonPumAndNoStatusProvidedShouldBeSuccess();
        findUsersByPumAndNoStatusProvidedShouldBeSuccess();
        findUsersByPumAndWithStatusProvidedShouldBeSuccess();
        findUsersByPumAndWithStatusSuspendedProvidedShouldReturnNotFound();
        findUsersByInvalidUserAndNoStatusProvidedShouldReturnNotFound();
        findUsersByPcmAndNoRolesRequiredShouldBeSuccess();
        findUsersByPcmAndWithRolesRequiredShouldBeSuccess();
        findUsersBySystemAdminWithoutRolesRequiredShouldBeSuccess();
    }

    public void findOrganisationScenarios() {
        findOrgByPfmShouldBeSuccess();
        findOrgByPomShouldBeSuccess();
        findOrgByPumShouldBeSuccess();
        findOrgByPcmShouldBeSuccess();
        findOrgByUnknownUserShouldReturnForbidden();
    }

    public void findActiveOrganisationScenarios() {
        findActiveOrganisationByPumShouldBeSuccess();
        findActiveOrganisationByCitizenOrCaseWorkerShouldBeSuccess();
    }

    public void findUsersByUserIdentifierWithUserAccessType() {
        findUserWithAccessTypeShouldBeSuccess();
    }

    public void findUsersByUserIdentifier(Boolean hasAccessType) {
        findUserWithIdShouldBeSuccess(hasAccessType);
    }

    public void findUsersByUserIdentifierWithUserAccessTypesAndRoles() {
        findUserWithAccessTypesAndRoleShouldBeSuccess();
    }

    public void findUserStatusByEmailScenarios() {
        findUserStatusByEmailByPumShouldBeSuccess();
        findUserStatusByEmailInHeaderByPumShouldBeSuccess();
        findUserStatusOfPendingUserByEmailShouldReturnNotFound();
    }

    public void reinviteUserScenarios() {
        reinviteActiveUserByPumShouldReturnBadRequest();
        reinviteUserByPumWithinOneHourShouldReturnConflict();
    }

    public void modifyRolesScenarios() {
        addRolesByPumShouldBeSuccess();
        deleteRolesByPumShouldBeSuccess();
        addRolesByPendingExtUserShouldReturnForbidden();
    }

    public void addNewUserAccessTypeScenarios() {
        addUserAccessTypeShouldBeSuccess();
        findUsersByUserIdentifierWithUserAccessType();;
    }

    public void updateAndAddNewUserAccessTypeScenarios() {
        addOrUpdateUserAccessTypeShouldBeSuccess();
        findUsersByUserIdentifierWithUserAccessTypesAndRoles();;
    }

    public void suspendUserScenarios() {
        suspendUserByPumShouldBeSuccess();
    }

    public void inviteUserByPuiUserManagerShouldBeSuccess() {
        log.info("inviteUserByPuiUserManagerShouldBeSuccess :: STARTED");
        Map<String, Object> newUserResponse = professionalApiClient
                .addNewUserToAnOrganisationExternal(createUserRequest(asList(puiCaseManager)),
                        professionalApiClient.getMultipleAuthHeaders(pumBearerToken), CREATED);
        assertThat(newUserResponse.get("userIdentifier")).isNotNull();
        log.info("inviteUserByPuiUserManagerShouldBeSuccess :: END");
    }

    public void addNewUserToAnOrganisationShouldBeSuccess(boolean hasAccessType) {
        log.info("addNewUserToAnOrganisationShouldBeSuccess :: STARTED");
        Map<String, Object> newUserResponse = professionalApiClient
                    .addNewUserToAnOrganisationExternal(createUserRequest(asList(puiCaseManager), hasAccessType),
                            professionalApiClient.getMultipleAuthHeaders(pumBearerToken), CREATED);
        assertThat(newUserResponse.get("userIdentifier")).isNotNull();
        newAddedUserId = newUserResponse.get("userIdentifier").toString();
        log.info("addNewUserToAnOrganisationShouldBeSuccess :: END");
    }

    public void inviteUserBySuperUserShouldBeSuccess() {
        log.info("inviteUserBySuperUserShouldBeSuccess :: STARTED");
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
        createAndActivateOrganisationWithGivenRequest(organisationCreationRequest, hmctsAdmin);

        NewUserCreationRequest newUserCreationRequest = createUserRequest(Arrays.asList("caseworker"));

        Map<String, Object> newUserResponse = professionalApiClient
                .addNewUserToAnOrganisationExternal(newUserCreationRequest, superUserToken, HttpStatus.CREATED);
        assertThat(newUserResponse).isNotNull();
        assertThat(newUserResponse.get("userIdentifier")).isNotNull();
        log.info("inviteUserBySuperUserShouldBeSuccess :: END");
    }

    public void findUsersByNonPumAndNoStatusProvidedShouldBeSuccess() {
        log.info("findUsersByNonPumAndNoStatusProvidedShouldBeSuccess :: STARTED");
        Map<String, Object> response = professionalApiClient.searchOrganisationUsersByStatusExternal(OK,
                professionalApiClient.getMultipleAuthHeaders(pcmBearerToken), "");
        validateRetrievedUsers(response, "ACTIVE", true);
        log.info("findUsersByNonPumAndNoStatusProvidedShouldBeSuccess :: END");
    }

    public void findUsersByPumAndNoStatusProvidedShouldBeSuccess() {
        log.info("findUsersByPumAndNoStatusProvidedShouldBeSuccess :: STARTED");
        Map<String, Object> response = professionalApiClient.searchOrganisationUsersByStatusExternal(OK,
                professionalApiClient.getMultipleAuthHeaders(pumBearerToken), "");
        validateRetrievedUsers(response, "ACTIVE", true);
        log.info("findUsersByPumAndNoStatusProvidedShouldBeSuccess :: END");
    }

    public void findUsersByPumAndWithStatusProvidedShouldBeSuccess() {
        log.info("findUsersByPumAndWithStatusProvidedShouldBeSuccess :: STARTED");
        Map<String, Object> response = professionalApiClient.searchOrganisationUsersByStatusExternal(OK,
                professionalApiClient.getMultipleAuthHeaders(pumBearerToken), "Active");
        validateRetrievedUsers(response, "ACTIVE", true);
        log.info("findUsersByPumAndWithStatusProvidedShouldBeSuccess :: END");
    }

    public void findUsersByPumAndWithStatusSuspendedProvidedShouldReturnNotFound() {
        log.info("findUsersByPumAndWithStatusSuspendedProvidedShouldReturnNotFound :: STARTED");
        professionalApiClient.searchOrganisationUsersByStatusExternal(NOT_FOUND,
                professionalApiClient.getMultipleAuthHeaders(pumBearerToken), "Suspended");
        log.info("findUsersByPumAndWithStatusSuspendedProvidedShouldReturnNotFound :: END");
    }

    public void findUsersByInvalidUserAndNoStatusProvidedShouldReturnNotFound() {
        log.info("findUsersByInvalidUserAndNoStatusProvidedShouldReturnNotFound :: STARTED");
        professionalApiClient.searchOrganisationUsersByStatusExternal(UNAUTHORIZED,
                professionalApiClient.getMultipleAuthHeadersWithEmptyBearerToken(""), "");
        log.info("findUsersByInvalidUserAndNoStatusProvidedShouldReturnNotFound :: END");
    }

    public void findUsersByPcmAndNoRolesRequiredShouldBeSuccess() {
        log.info("findUsersByPcmAndNoRolesRequiredShouldBeSuccess :: STARTED");
        Map<String, Object> response = professionalApiClient
                .searchOrganisationUsersByReturnRolesParamExternal(OK,
                        professionalApiClient.getMultipleAuthHeaders(pcmBearerToken), "false");
        validateRetrievedUsers(response, "ACTIVE", false);
        log.info("findUsersByPcmAndNoRolesRequiredShouldBeSuccess :: END");
    }

    public void findUsersByPcmAndWithRolesRequiredShouldBeSuccess() {
        log.info("findUsersByPcmAndWithRolesRequiredShouldBeSuccess :: STARTED");
        Map<String, Object> response = professionalApiClient
                .searchOrganisationUsersByReturnRolesParamExternal(OK,
                        professionalApiClient.getMultipleAuthHeaders(pcmBearerToken), "true");
        validateRetrievedUsers(response, "ACTIVE", true);
        log.info("findUsersByPcmAndWithRolesRequiredShouldBeSuccess :: END");
    }

    public void findUsersBySystemAdminWithoutRolesRequiredShouldBeSuccess() {
        log.info("findUsersBySystemAdminWithoutRolesRequiredShouldBeSuccess :: STARTED");
        systemUserBearerToken = idamOpenIdClient.getExternalOpenIdToken(systemUser,
                "firstName", "lastName", generateRandomEmail());
        Map<String, Object> searchResponse = professionalApiClient
                .searchUsersByOrganisation(professionalApiClient.getMultipleAuthHeaders(systemUserBearerToken),
                        extActiveOrgId, FALSE, OK, TRUE);

        List<HashMap<String, Object>> professionalUsers = (List<HashMap<String, Object>>) searchResponse.get("users");
        assertThat(professionalUsers.size()).isGreaterThan(1);
        validateRetrievedUsers(searchResponse, ACTIVE, false);
        log.info("findUsersBySystemAdminWithoutRolesRequiredShouldBeSuccess :: END");
    }

    @Test
    @DisplayName("PRD External Test Scenarios: find org users should sort based on first name")
    public void findOrganisationUsersSorted() {
        String superUserEmail = generateRandomEmail();
        OrganisationCreationRequest organisationCreationRequest = createOrganisationRequest()
                .superUser(UserCreationRequest.aUserCreationRequest().firstName(firstName)
                        .lastName(lastName).email(superUserEmail).build())
                .status("ACTIVE").build();

        String extActiveOrgId = createAndActivateOrganisationWithGivenRequest(organisationCreationRequest,
                hmctsAdmin);

        String bearerToken = inviteUser(puiCaseManager, extActiveOrgId, generateRandomEmail(),
                "firstName2", "lastName2");

        NewUserCreationRequest newUserCreationRequest2 = createUserRequest(Arrays.asList(puiCaseManager),
                "lastName1", "firstName1");

        Map<String, Object> newUserResponse2 = professionalApiClient
                .addNewUserToAnOrganisation(extActiveOrgId, puiCaseManager,
                        newUserCreationRequest2, HttpStatus.CREATED);
        assertThat(newUserResponse2).isNotNull();
        assertThat(newUserResponse2.get("userIdentifier")).isNotNull();

        Map<String, Object> response = professionalApiClient
                .searchOrganisationUsersByReturnRolesParamExternal(OK,
                        professionalApiClient.getMultipleAuthHeaders(bearerToken), "false");

        assertThat(response.get("users")).asList().isNotEmpty();
        assertThat(response.get("organisationIdentifier")).isNotNull();
        List<HashMap> professionalUsersResponses = (List<HashMap>) response.get("users");
        assertThat(professionalUsersResponses.size()).isEqualTo(3);

        HashMap firstUser = professionalUsersResponses.get(0);
        assertThat(firstUser.get("firstName")).isEqualTo("firstName");
        assertThat(firstUser.get("lastName")).isEqualTo("lastName");

        HashMap secondUser = professionalUsersResponses.get(1);
        assertThat(secondUser.get("firstName")).isEqualTo("firstName1");
        assertThat(secondUser.get("lastName")).isEqualTo("lastName1");

        HashMap thirdUser = professionalUsersResponses.get(2);
        assertThat(thirdUser.get("firstName")).isEqualTo("firstName2");
        assertThat(thirdUser.get("lastName")).isEqualTo("lastName2");

    }

    public void findOrgByPfmShouldBeSuccess() {
        log.info("findOrgByPfmShouldBeSuccess :: STARTED");
        Map<String, Object> response = professionalApiClient.retrieveOrganisationByOrgIdExternal(OK,
                professionalApiClient.getMultipleAuthHeaders(pfmBearerToken));
        assertThat(response.get("paymentAccount")).asList().hasSize(3);
        assertThat(response.get("pendingPaymentAccount")).asList().hasSize(0);
        log.info("findOrgByPfmShouldBeSuccess :: END");
        responseValidate(response);
    }

    public void findOrgByPomShouldBeSuccess() {
        log.info("findOrgByPomShouldBeSuccess :: STARTED");
        Map<String, Object> response = professionalApiClient.retrieveOrganisationByOrgIdExternal(OK,
                professionalApiClient.getMultipleAuthHeaders(pomBearerToken));
        assertThat(response.get("paymentAccount")).asList().hasSize(3);
        assertThat(response.get("pendingPaymentAccount")).asList().hasSize(0);
        log.info("findOrgByPomShouldBeSuccess :: END");
    }

    public void findOrgByPumShouldBeSuccess() {
        log.info("findOrgByPumShouldBeSuccess :: STARTED");
        Map<String, Object> response = professionalApiClient.retrieveOrganisationByOrgIdExternal(OK,
                professionalApiClient.getMultipleAuthHeaders(pumBearerToken));
        responseValidate(response);
        log.info("findOrgByPumShouldBeSuccess :: END");
    }

    public void findOrgByPcmShouldBeSuccess() {
        log.info("findOrgByPcmShouldBeSuccess :: STARTED");
        Map<String, Object> response = professionalApiClient.retrieveOrganisationByOrgIdExternal(OK,
                professionalApiClient.getMultipleAuthHeaders(pcmBearerToken));
        responseValidate(response);
        log.info("findOrgByPcmShouldBeSuccess :: END");
    }

    public void findOrgByUnknownUserShouldReturnForbidden() {
        log.info("findOrgByUnknownUserShouldReturnForbidden :: STARTED");
        Map<String, Object> response = professionalApiClient.retrieveOrganisationByOrgIdExternal(FORBIDDEN,
                professionalApiClient.getMultipleAuthHeaders(systemUserBearerToken));
        assertThat(response.get("errorMessage")).isNotNull();
        assertThat(response).containsEntry("errorMessage", "9 : Access Denied");
        log.info("findOrgByUnknownUserShouldReturnForbidden :: END");
    }

    public void findUserStatusByEmailByPumShouldBeSuccess() {
        log.info("findUserStatusByEmailByPumShouldBeSuccess :: STARTED");
        Map<String, Object> response = professionalApiClient.findUserStatusByEmail(OK,
                professionalApiClient.getMultipleAuthHeaders(pumBearerToken), activeUserEmail);
        assertThat(response.get("userIdentifier")).isNotNull();
        log.info("findUserStatusByEmailByPumShouldBeSuccess :: END");
    }

    public void findUserStatusByEmailInHeaderByPumShouldBeSuccess() {
        log.info("findUserStatusByEmailInHeaderByPumShouldBeSuccess :: STARTED");
        Map<String, Object> response = professionalApiClient.findUserStatusByEmail(OK,
                professionalApiClient.getMultipleAuthHeaders(pumBearerToken), activeUserEmail);
        assertThat(response.get("userIdentifier")).isNotNull();
        log.info("findUserStatusByEmailInHeaderByPumShouldBeSuccess :: END");
    }

    public void findUserStatusOfPendingUserByEmailShouldReturnNotFound() {
        log.info("findUserStatusOfPendingUserByEmailShouldReturnNotFound :: STARTED");
        Map<String, Object> response = professionalApiClient.findUserStatusByEmail(NOT_FOUND,
                professionalApiClient.getMultipleAuthHeaders(pumBearerToken), superUserEmail);
        assertThat(response.get("userIdentifier")).isNull();
        log.info("findUserStatusOfPendingUserByEmailShouldReturnNotFound :: END");
    }

    public void findActiveOrganisationByPumShouldBeSuccess() {
        log.info("findActiveOrganisationByPumShouldBeSuccess :: STARTED");
        List<OrganisationMinimalInfoResponse> responseList = (List<OrganisationMinimalInfoResponse>)
                professionalApiClient.retrieveAllActiveOrganisationsWithMinimalInfo(
                        professionalApiClient.getMultipleAuthHeaders(pumBearerToken), OK, ACTIVE, true);

        assertThat(responseList.size()).isPositive();
        OrganisationMinimalInfoResponse org = responseList.get(0);
        assertThat(org.getName()).isNotNull();
        assertThat(org.getOrganisationIdentifier()).isNotNull();
        assertThat(org.getContactInformation()).isNotNull();
        log.info("findActiveOrganisationByPumShouldBeSuccess :: END");
    }

    public void findUserWithAccessTypeShouldBeSuccess() {
        log.info("findUserWithAccessTypeShouldBeSuccess :: STARTED");
        Map<String, Object> response = professionalApiClient.searchOrganisationUsersByUserIdExternal(OK,
                professionalApiClient.getMultipleAuthHeaders(pumBearerToken), activeUserId);
        validateAccessTypesInRetrievedUser(response, "ACTIVE",
                true,true, activeUserId);
        log.info("findUserWithAccessTypeShouldBeSuccess :: END");
    }

    public void findUserWithIdShouldBeSuccess(Boolean hasAccessType) {
        log.info("findUserWithIdShouldBeSuccess :: STARTED");
        Map<String, Object> response = professionalApiClient.searchOrganisationUsersByUserIdExternal(OK,
                professionalApiClient.getMultipleAuthHeaders(pumBearerToken), newAddedUserId);
        validateAccessTypesInRetrievedUser(response, "PENDING",
                false, hasAccessType, newAddedUserId);
        log.info("findUserWithIdShouldBeSuccess :: END");
    }

    public void findUserWithAccessTypesAndRoleShouldBeSuccess() {
        log.info("findUserWithAccessTypesAndRoleShouldBeSuccess :: STARTED");
        Map<String, Object> response = professionalApiClient.searchOrganisationUsersByUserIdExternal(OK,
                professionalApiClient.getMultipleAuthHeaders(pumBearerToken), activeUserId);
        validateAccessTypesAndRolesInRetrievedUser(response,  true);
        log.info("findUserWithAccessTypesAndRoleShouldBeSuccess :: END");
    }

    public void findActiveOrganisationByCitizenOrCaseWorkerShouldBeSuccess() {
        log.info("findActiveOrganisationByCitizenOrCaseWorkerShouldBeSuccess :: STARTED");
        List<OrganisationMinimalInfoResponse> responseList = (List<OrganisationMinimalInfoResponse>)
                professionalApiClient.retrieveAllActiveOrganisationsWithMinimalInfo(
                        professionalApiClient.getMultipleAuthHeaders(idamOpenIdClient.getExternalOpenIdTokenWithRetry(
                                Arrays.asList(caseworker, citizen), firstName, lastName, generateRandomEmail())), OK,
                        ACTIVE, true);

        assertThat(responseList.size()).isPositive();
        log.info("findActiveOrganisationByCitizenOrCaseWorkerShouldBeSuccess :: END");
    }

    public void reinviteActiveUserByPumShouldReturnBadRequest() {
        log.info("reinviteActiveUserByPumShouldReturnBadRequest :: STARTED");
        NewUserCreationRequest newUserCreationRequest = professionalApiClient
                .createNewUserRequest(activeUserEmail);
        newUserCreationRequest.setResendInvite(true);
        Map<String, Object> reinviteUserResponse = professionalApiClient
                .addNewUserToAnOrganisationExternal(newUserCreationRequest, professionalApiClient
                        .getMultipleAuthHeaders(pumBearerToken), BAD_REQUEST);
        assertThat((String) reinviteUserResponse.get("errorDescription")).contains("User is not in PENDING state");
        log.info("reinviteActiveUserByPumShouldReturnBadRequest :: END");
    }

    public void reinviteUserByPumWithinOneHourShouldReturnConflict() {
        log.info("reinviteUserByPumWithinOneHourShouldReturnConflict :: STARTED");
        NewUserCreationRequest newUserCreationRequest = professionalApiClient
                .createNewUserRequest(superUserEmail);
        newUserCreationRequest.setResendInvite(true);
        Map<String, Object> reinviteUserResponse = professionalApiClient
                .addNewUserToAnOrganisationExternal(newUserCreationRequest, professionalApiClient
                        .getMultipleAuthHeaders(pumBearerToken), TOO_MANY_REQUESTS);
        assertThat((String) reinviteUserResponse.get("errorDescription"))
                .contains(String.format("The request was last made less than %s minutes ago. Please try after some "
                        + "time", resendInterval));
        log.info("reinviteUserByPumWithinOneHourShouldReturnConflict :: END");
    }

    public void addRolesByPumShouldBeSuccess() {
        log.info("addRolesByPumShouldBeSuccess :: STARTED");
        professionalApiClient.modifyUserToExistingUserForExternal(OK, addRoleRequest(puiOrgManager),
                professionalApiClient.getMultipleAuthHeaders(pumBearerToken), activeUserId);

        Map<String, Object> searchResponse = professionalApiClient
                .searchOrganisationUsersByStatusInternal(extActiveOrgId, hmctsAdmin, OK);
        List<Map<String, Object>> professionalUsersResponses = (List<Map<String, Object>>) searchResponse.get("users");
        Map<String, Object> professionalUsersResponse = getUserById(professionalUsersResponses, activeUserId);
        assertThat(professionalUsersResponse.get("roles")).isNotNull();

        List<String> roles = (List<String>) professionalUsersResponse.get("roles");
        assertThat(roles).contains(puiOrgManager);
        log.info("addRolesByPumShouldBeSuccess :: END");
    }

    public void addUserAccessTypeShouldBeSuccess() {
        log.info("addUserAccessTypeShouldBeSuccess :: STARTED");
        UserAccessType userAccessType = new UserAccessType();
        userAccessType.setAccessTypeId("testAccessTypeId");
        userAccessType.setJurisdictionId("testJurisdictionId");
        userAccessType.setOrganisationProfileId("testOrganisationProfileId");
        userAccessType.setEnabled(true);
        professionalApiClient.modifyUserToExistingUserForExternal(OK, addOrUpdateUserAccessType(userAccessType),
                professionalApiClient.getMultipleAuthHeaders(pumBearerToken), activeUserId);
        log.info("addUserAccessTypeShouldBeSuccess :: END");
    }

    public void addOrUpdateUserAccessTypeShouldBeSuccess() {
        log.info("addOrUpdateUserAccessTypeShouldBeSuccess :: STARTED");
        UserAccessType userAccessType2 = new UserAccessType();
        userAccessType2.setAccessTypeId("testAccessTypeId2");
        userAccessType2.setJurisdictionId("testJurisdictionId2");
        userAccessType2.setOrganisationProfileId("testOrganisationProfileId2");
        userAccessType2.setEnabled(false);

        UserAccessType userAccessType3 = new UserAccessType();
        userAccessType3.setAccessTypeId("testAccessTypeId3");
        userAccessType3.setJurisdictionId("testJurisdictionId3");
        userAccessType3.setOrganisationProfileId("testOrganisationProfileId3");
        userAccessType3.setEnabled(true);

        Set<UserAccessType> userAccessTypeSet = new HashSet<>();
        userAccessTypeSet.add(userAccessType2);
        userAccessTypeSet.add(userAccessType3);
        professionalApiClient.modifyUserToExistingUserForExternal(OK, addOrUpdateUserAccessTypeAndRole(puiOrgManager,
                        userAccessTypeSet),professionalApiClient.getMultipleAuthHeaders(pumBearerToken), activeUserId);
        log.info("addOrUpdateUserAccessTypeShouldBeSuccess :: END");
    }

    public void deleteRolesByPumShouldBeSuccess() {
        log.info("deleteRolesByPumShouldBeSuccess :: STARTED");
        professionalApiClient.modifyUserToExistingUserForExternal(OK, deleteRoleRequest(puiOrgManager),
                professionalApiClient.getMultipleAuthHeaders(pumBearerToken), activeUserId);
        Map<String, Object> searchResponse = professionalApiClient
                .searchOrganisationUsersByStatusInternal(extActiveOrgId, hmctsAdmin, OK);
        List<Map<String, Object>> professionalUsersResponses = (List<Map<String, Object>>) searchResponse.get("users");
        Map professionalUsersResponse = getUserById(professionalUsersResponses, activeUserId);
        assertThat(professionalUsersResponse.get("roles")).isNotNull();

        List<String> modifiedRoles = (List<String>) professionalUsersResponse.get("roles");
        assertThat(modifiedRoles).isNotEmpty();
        assertThat(modifiedRoles).doesNotContain(puiOrgManager);
        log.info("deleteRolesByPumShouldBeSuccess :: STARTED");
    }

    public void addRolesByPendingExtUserShouldReturnForbidden() {
        log.info("addRolesByPendingExtUserShouldReturnForbidden :: STARTED");
        Map<String, Object> modifiedUserResponse = professionalApiClient.modifyUserToExistingUserForExternal(FORBIDDEN,
                addRoleRequest(puiOrgManager),
                professionalApiClient.getMultipleAuthHeaders(pumBearerToken), superUserId);
        assertThat(modifiedUserResponse).containsEntry("errorMessage", "9 : Access Denied");
        assertThat(modifiedUserResponse)
                .containsEntry("errorDescription", "User status must be Active to perform this operation");
        log.info("addRolesByPendingExtUserShouldReturnForbidden :: END");
    }

    public void suspendUserByPumShouldBeSuccess() {
        log.info("suspendUserByPumShouldBeSuccess :: STARTED");
        UserProfileUpdatedData data = getUserStatusUpdateRequest(SUSPENDED);
        professionalApiClient.modifyUserToExistingUserForPrdAdmin(OK, data, extActiveOrgId, activeUserId);
        assertThat(searchUserStatus(extActiveOrgId, activeUserId)).isEqualTo(SUSPENDED.name());
        log.info("suspendUserByPumShouldBeSuccess :: END");
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

    public String inviteUser(String role,
                             String orgId,
                             String email,
                             String firstName,
                             String lastName) {
        List<String> userRoles = new ArrayList<>();
        userRoles.add(role);
        NewUserCreationRequest newUserCreationRequest = createUserRequest(userRoles);
        newUserCreationRequest.setEmail(email);
        String bearerToken = idamOpenIdClient.getExternalOpenIdToken(puiUserManager,
                firstName, lastName, email);

        Map<String, Object> pumInternalUserResponse = professionalApiClient
                .addNewUserToAnOrganisation(orgId, hmctsAdmin, newUserCreationRequest, CREATED);
        assertThat(pumInternalUserResponse.get("userIdentifier")).isNotNull();
        return bearerToken;
    }

    @Test
    @DisplayName("MFA Scenarios")
    @ToggleEnable(mapKey = "OrganisationMfaStatusController.retrieveMfaStatusByUserId", withFeature = true)
    @SuppressWarnings("checkstyle:AbbreviationAsWordInName")
    void findMFAScenario() {
        setUpOrgTestData();
        findMFAByUserIDShouldBeSuccess();
    }

    @SuppressWarnings("checkstyle:AbbreviationAsWordInName")
    public void findMFAByUserIDShouldBeSuccess() {
        log.info("findMFAByUserIDShouldBeSuccess :: STARTED");
        Map<String, Object> mfaStatusResponse = professionalApiClient.findMFAByUserId(OK, superUserId);
        assertThat(mfaStatusResponse).containsEntry("mfa", "EMAIL");
        log.info("findMFAByUserIDShouldBeSuccess :: END");
    }

    public void retrieveOrganisationPbaScenarios() {
        findOrganisationPbaWithoutEmailByExternalUserShouldBeBadRequest();
    }

    public void findOrganisationPbaWithoutEmailByExternalUserShouldBeBadRequest() {
        log.info("findOrganisationPbaWithoutEmailByExternalUserShouldBeBadRequest :: STARTED");


        professionalApiClient.retrievePaymentAccountsWithoutEmailForExternal(
                professionalApiClient.getMultipleAuthHeaders(pumBearerToken));

        log.info("findOrganisationPbaWithoutEmailByExternalUserShouldBeBadRequest :: END");
    }

    @Test
    @ToggleEnable(mapKey = "OrganisationExternalController.deletePaymentAccountsOfOrganisation", withFeature = false)
    @SuppressWarnings("checkstyle:AbbreviationAsWordInName")
    void deletePbaOfExistingOrganisationShouldBeForbiddenWhenLDOff() {
        log.info("deletePbaOfExistingOrganisationShouldBeForbiddenWhenLDOff :: STARTED");

        setUpOrgTestData();
        setUpUserBearerTokens(List.of(puiFinanceManager));

        PbaRequest deletePbaRequest = new PbaRequest();
        deletePbaRequest.setPaymentAccounts(Set.of("PBA0000021", "PBA0000022", "PBA0000023"));

        professionalApiClient.deletePaymentAccountsOfOrganisation(deletePbaRequest,
                professionalApiClient.getMultipleAuthHeaders(pfmBearerToken), FORBIDDEN);

        log.info("deletePbaOfExistingOrganisationShouldBeForbiddenWhenLDOff :: END");
    }

    @Test
    @ToggleEnable(mapKey = "OrganisationExternalController.deletePaymentAccountsOfOrganisation", withFeature = true)
    void deletePbaOfExistingOrganisationShouldBeSuccess() {
        log.info("deletePbaOfExistingOrganisationShouldBeSuccess :: STARTED");

        setUpOrgTestData();
        setUpUserBearerTokens(List.of(puiFinanceManager));

        PbaRequest deletePbaRequest = new PbaRequest();
        deletePbaRequest.setPaymentAccounts(organisationCreationRequest.getPaymentAccount());

        professionalApiClient.deletePaymentAccountsOfOrganisation(deletePbaRequest,
                professionalApiClient.getMultipleAuthHeaders(pfmBearerToken), NO_CONTENT);

        Map<String, Object> response = professionalApiClient.retrieveOrganisationByOrgIdExternal(OK,
                professionalApiClient.getMultipleAuthHeaders(pfmBearerToken));

        var paymentAccounts = (List<String>) response.get("paymentAccount");
        assertThat(paymentAccounts).isEmpty();
        log.info("deletePbaOfExistingOrganisationShouldBeSuccess :: END");
    }

    @Test
    @ToggleEnable(mapKey = "OrganisationExternalController.addPaymentAccountsToOrganisation", withFeature = false)
    @SuppressWarnings("checkstyle:AbbreviationAsWordInName")
    void addPbaOfExistingOrganisationShouldBeForbiddenWhenLDOff() {
        log.info("addPbaOfExistingOrganisationShouldBeForbiddenWhenLDOff :: STARTED");

        setUpOrgTestData();
        setUpUserBearerTokens(List.of(puiFinanceManager));

        PbaRequest pbaRequest = new PbaRequest();
        pbaRequest.setPaymentAccounts(Set.of("PBA0000021", "PBA0000022", "PBA0000023"));

        professionalApiClient.addPaymentAccountsOfOrganisation(pbaRequest,
                professionalApiClient.getMultipleAuthHeaders(pfmBearerToken), FORBIDDEN);

        log.info("addPbaOfExistingOrganisationShouldBeForbiddenWhenLDOff :: END");
    }

    @Test
    @ToggleEnable(mapKey = "OrganisationExternalController.addPaymentAccountsToOrganisation", withFeature = true)
    void addPbaOfExistingOrganisationShouldBeSuccess() {
        log.info("addPaymentAccountsToOrganisation :: STARTED");

        setUpOrgTestData();
        setUpUserBearerTokens(List.of(puiFinanceManager));

        Set<String> paymentAccountsToAdd =  new HashSet<>();
        paymentAccountsToAdd.add("PBA".concat(RandomStringUtils.randomAlphanumeric(7)));

        PbaRequest pbaRequest = new PbaRequest();
        pbaRequest.setPaymentAccounts(paymentAccountsToAdd);

        professionalApiClient.addPaymentAccountsOfOrganisation(pbaRequest,
                professionalApiClient.getMultipleAuthHeaders(pfmBearerToken), CREATED);

        Map<String, Object> response = professionalApiClient.retrieveOrganisationByOrgIdWithPbaStatusExternal(OK,
                "PENDING", professionalApiClient.getMultipleAuthHeaders(pfmBearerToken));

        paymentAccountsToAdd.addAll(organisationCreationRequest.getPaymentAccount());
        paymentAccountsToAdd = paymentAccountsToAdd.stream().map(String::toUpperCase).collect(Collectors.toSet());

        var pendingPaymentAccounts = (List<String>) response.get("pendingPaymentAccount");
        var acceptedPaymentAccounts = (List<String>) response.get("paymentAccount");
        List<String> allStatusPaymentAccounts = new ArrayList<>();
        allStatusPaymentAccounts.addAll(pendingPaymentAccounts);
        allStatusPaymentAccounts.addAll(acceptedPaymentAccounts);

        assertThat(paymentAccountsToAdd).hasSameElementsAs(allStatusPaymentAccounts);
        log.info("addPbaOfExistingOrganisationShouldBeSuccess :: END");
    }

    @Test
    @ToggleEnable(mapKey = "OrganisationExternalController.addContactInformationsToOrganisation", withFeature = false)
    @SuppressWarnings("checkstyle:AbbreviationAsWordInName")
    void testAddContactsInformationsToOrganisationScenariosShouldBeForbiddenWhenLDOff() {
        setUpOrgTestData();
        setUpUserBearerTokens(List.of(puiUserManager, puiCaseManager, puiOrgManager, puiFinanceManager, caseworker));

        log.info("addContactInformationsToOrganisationShouldBeSuccess :: STARTED");

        List<ContactInformationCreationRequest> createContactInformationCreationRequests =
                createContactInformationCreationRequests();
        Map<String, Object> result = professionalApiClient
                .addContactInformationsToOrganisation(createContactInformationCreationRequests,
                        pomBearerToken,FORBIDDEN);

        assertThat(result.get("statusCode")).isNotNull();
        assertThat(result.get("statusCode")).isEqualTo(403);
        log.info("addContactInformationsToOrganisationShouldBeSuccess :: END");
    }

    @Test
    @DisplayName("Add Contact informations to organisations  Test Scenarios")
    @ToggleEnable(mapKey = "OrganisationExternalController.addContactInformationsToOrganisation", withFeature = true)
    @SuppressWarnings("checkstyle:AbbreviationAsWordInName")
    void testAddContactsInformationsToOrganisationScenariosShouldBeSuccessWhenLDON() {
        setUpOrgTestData();
        setUpUserBearerTokens(List.of(puiUserManager, puiCaseManager, puiOrgManager, puiFinanceManager, caseworker));
        invitePuiOrgManagerUserScenarios();
        addContactInformationsToOrganisationScenario(CREATED);

        suspendPuiOrgManagerUserScenarios();
    }

    public void invitePuiOrgManagerUserScenarios() {
        inviteUserByPuiOrgManagerShouldBeSuccess();
    }


    public void suspendPuiOrgManagerUserScenarios() {

        suspendUserByPuiOrgManagerShouldBeSuccess();
    }

    public void addContactInformationsToOrganisationScenario(HttpStatus httpStatus) {
        addContactInformationsToOrganisationShouldBeSuccess(httpStatus);
    }


    public void addContactInformationsToOrganisationShouldBeSuccess(HttpStatus httpStatus) {
        log.info("addContactInformationsToOrganisationShouldBeSuccess :: STARTED");

        List<ContactInformationCreationRequest> createContactInformationCreationRequests =
                createContactInformationCreationRequests();
        Map<String, Object> result = professionalApiClient
                .addContactInformationsToOrganisation(createContactInformationCreationRequests,
                        pomBearerToken,httpStatus);

        assertThat(result.get("statusCode")).isNotNull();
        assertThat(result.get("statusCode")).isEqualTo(201);
        log.info("addContactInformationsToOrganisationShouldBeSuccess :: END");
    }

    public void inviteUserByPuiOrgManagerShouldBeSuccess() {
        log.info("inviteUserByPuiOrgManagerShouldBeSuccess :: STARTED");
        Map<String, Object> newUserResponse = professionalApiClient
                .addNewUserToAnOrganisationExternal(createUserRequest(asList(puiOrgManager)),
                        professionalApiClient.getMultipleAuthHeaders(pomBearerToken), CREATED);
        assertThat(newUserResponse.get("userIdentifier")).isNotNull();
        log.info("inviteUserByPuiOrgManagerShouldBeSuccess :: END");
    }

    public void suspendUserByPuiOrgManagerShouldBeSuccess() {
        log.info("suspendUserByPuiOrgManagerShouldBeSuccess :: STARTED");
        UserProfileUpdatedData data = getUserStatusUpdateRequest(SUSPENDED);
        professionalApiClient.modifyUserToExistingUserForPrdAdmin(OK, data, extActiveOrgId, activeUserId);
        assertThat(searchUserStatus(extActiveOrgId, activeUserId)).isEqualTo(SUSPENDED.name());
        log.info("suspendUserByPuiOrgManagerShouldBeSuccess :: END");
    }

    @Test
    @ToggleEnable(mapKey = "OrganisationExternalController.deleteMultipleAddressesOfOrganisation", withFeature = false)
    @SuppressWarnings("checkstyle:AbbreviationAsWordInName")
    void deleteMultipleAddressesOfOrganisationShouldBeForbiddenWhenLDOff() {
        log.info("deleteMultipleAddressesOfOrganisationShouldBeForbiddenWhenLDOff :: STARTED");

        setUpOrgTestData();
        setUpUserBearerTokens(List.of(puiUserManager, puiCaseManager, puiOrgManager, puiFinanceManager, caseworker));

        var deleteMultipleAddressRequest01 = new DeleteMultipleAddressRequest(UUID.randomUUID().toString());
        var deleteMultipleAddressRequest02 = new DeleteMultipleAddressRequest(UUID.randomUUID().toString());
        var deleteMultipleAddressRequest03 = new DeleteMultipleAddressRequest(UUID.randomUUID().toString());
        var requestArrayList = new ArrayList<>(List.of(deleteMultipleAddressRequest01,
                deleteMultipleAddressRequest02, deleteMultipleAddressRequest03));

        professionalApiClient.deleteMultipleAddressesOfOrganisation(requestArrayList,
                professionalApiClient.getMultipleAuthHeaders(pomBearerToken), FORBIDDEN);
        log.info("deleteMultipleAddressesOfOrganisationShouldBeForbiddenWhenLDOff :: END");
    }

    @Test
    @ToggleEnable(mapKey = "OrganisationExternalController.deleteMultipleAddressesOfOrganisation", withFeature = true)
    void deleteMultipleAddressesOfOrganisationShouldBe404Failure() {
        log.info("deleteMultipleAddressesOfOrganisationShouldBeSuccess :: STARTED");
        setUpOrgTestData();
        setUpUserBearerTokens(List.of(puiUserManager, puiCaseManager, puiOrgManager, puiFinanceManager, caseworker));

        var deleteMultipleAddressRequest = new DeleteMultipleAddressRequest(UUID.randomUUID().toString());
        var requestArrayList = new ArrayList<>(List.of(deleteMultipleAddressRequest));

        professionalApiClient.deleteMultipleAddressesOfOrganisation(requestArrayList,
                professionalApiClient.getMultipleAuthHeaders(pomBearerToken), NOT_FOUND);

        Map<String, Object> response = professionalApiClient.retrieveOrganisationByOrgIdExternal(OK,
                professionalApiClient.getMultipleAuthHeaders(pomBearerToken));
        assertThat(response.get("contactInformation")).asList().hasSize(2);
        log.info("deleteMultipleAddressesOfOrganisationShouldBeSuccess :: END");
    }

    @Test
    @ToggleEnable(mapKey = "OrganisationExternalController.deleteMultipleAddressesOfOrganisation", withFeature = true)
    void deleteMultipleAddressesOfOrganisationShouldBeSuccess() {
        log.info("deleteMultipleAddressesOfOrganisationShouldBeSuccess :: STARTED");
        setUpOrgTestData();
        setUpUserBearerTokens(List.of(puiUserManager, puiCaseManager, puiOrgManager, puiFinanceManager, caseworker));

        Map<String, Object> response = professionalApiClient.retrieveOrganisationByOrgIdExternal(OK,
                professionalApiClient.getMultipleAuthHeaders(pomBearerToken));

        ArrayList<LinkedHashMap<String, Object>> contacts
                = (ArrayList<LinkedHashMap<String, Object>>)response.get("contactInformation");
        List<String> addressId = contacts.stream()
                .limit(1).map(ci -> ci.get("addressId").toString())
                .collect(Collectors.toList());

        var deleteMultipleAddressRequest = new DeleteMultipleAddressRequest(addressId.get(0));
        var requestArrayList = new ArrayList<>(List.of(deleteMultipleAddressRequest));

        professionalApiClient.deleteMultipleAddressesOfOrganisation(requestArrayList,
                professionalApiClient.getMultipleAuthHeaders(pomBearerToken), NO_CONTENT);

        Map<String, Object> responseAfterDel = professionalApiClient.retrieveOrganisationByOrgIdExternal(OK,
                professionalApiClient.getMultipleAuthHeaders(pomBearerToken));
        assertThat(responseAfterDel.get("contactInformation")).asList().hasSize(1);
        log.info("deleteMultipleAddressesOfOrganisationShouldBeSuccess :: END");
    }
}
