package uk.gov.hmcts.reform.professionalapi;

import static org.assertj.core.api.Assertions.assertThat;
import static org.codehaus.groovy.runtime.InvokerHelper.asList;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.TOO_MANY_REQUESTS;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static uk.gov.hmcts.reform.professionalapi.client.ProfessionalApiClient.createOrganisationRequest;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.IdamStatus.SUSPENDED;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.ACTIVE;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.FALSE;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.TRUE;
import static uk.gov.hmcts.reform.professionalapi.controller.request.UserCreationRequest.aUserCreationRequest;

import io.restassured.specification.RequestSpecification;
import lombok.extern.slf4j.Slf4j;
import net.thucydides.core.annotations.WithTag;
import net.thucydides.core.annotations.WithTags;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.professionalapi.controller.request.NewUserCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.response.OrganisationMinimalInfoResponse;
import uk.gov.hmcts.reform.professionalapi.domain.UserProfileUpdatedData;
import uk.gov.hmcts.reform.professionalapi.util.CustomSerenityRunner;
import uk.gov.hmcts.reform.professionalapi.util.ToggleEnable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RunWith(CustomSerenityRunner.class)
@WithTags({@WithTag("testType:Functional")})
@Slf4j
@SuppressWarnings("unchecked")
public class ProfessionalExternalUserFunctionalTest extends AuthorizationFunctionalTest {

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
    OrganisationCreationRequest organisationCreationRequest;
    String firstName = "firstName";
    String lastName = "lastName";

    @Test
    public void testExternalUserScenario() {
        setUpOrgTestData();
        setUpUserBearerTokens();
        inviteUserScenarios();
        findUsersByOrganisationScenarios();
        findOrganisationScenarios();
        retrieveOrganisationPbaScenarios();
        findActiveOrganisationScenarios();
        reinviteUserScenarios();
        findUserStatusByEmailScenarios();
        modifyRolesScenarios();
        suspendUserScenarios();
    }

    public void setUpOrgTestData() {
        superUserEmail = generateRandomEmail();
        organisationCreationRequest = createOrganisationRequest()
                .superUser(aUserCreationRequest()
                        .firstName(firstName)
                        .lastName(lastName)
                        .email(superUserEmail)
                        .build())
                .build();

        organisationCreationRequest.setStatus("ACTIVE");
        extActiveOrgId = createAndctivateOrganisationWithGivenRequest(organisationCreationRequest, hmctsAdmin);

        Map<String, Object> searchResponse = professionalApiClient
                .searchOrganisationUsersByStatusInternal(extActiveOrgId, hmctsAdmin, OK);
        List<Map<String, Object>> professionalUsersResponses = (List<Map<String, Object>>) searchResponse.get("users");
        superUserId = (String) (professionalUsersResponses.get(0)).get("userIdentifier");
    }

    public void setUpUserBearerTokens() {
        pumBearerToken = inviteUser(puiUserManager);
        pcmBearerToken = inviteUser(puiCaseManager);
        pomBearerToken = inviteUser(puiOrgManager);
        pfmBearerToken = inviteUser(puiFinanceManager);
        caseworkerBearerToken = inviteUser(caseworker);
    }

    public void inviteUserScenarios() {
        inviteUserByPuiUserManagerShouldBeSuccess();
        // below test receives 504 from SIDAM intermittently, needs investigation:
        // inviteUserBySuperUserShouldBeSuccess();
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

    public void inviteUserBySuperUserShouldBeSuccess() {
        log.info("inviteUserBySuperUserShouldBeSuccess :: STARTED");
        String email = generateRandomEmail();
        RequestSpecification superUserToken =
                professionalApiClient.getMultipleAuthHeaders(idamOpenIdClient.getExternalOpenIdTokenWithRetry(
                        superUserRoles(), firstName, lastName, email));

        OrganisationCreationRequest organisationCreationRequest = createOrganisationRequest()
                .superUser(aUserCreationRequest().firstName(firstName).lastName(lastName).email(email).build())
                .status("ACTIVE").build();
        createAndctivateOrganisationWithGivenRequest(organisationCreationRequest, hmctsAdmin);

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

    public void findOrgByPfmShouldBeSuccess() {
        log.info("findOrgByPfmShouldBeSuccess :: STARTED");
        Map<String, Object> response = professionalApiClient.retrieveOrganisationByOrgIdExternal(OK,
                professionalApiClient.getMultipleAuthHeaders(pfmBearerToken));
        assertThat(response.get("paymentAccount")).asList().hasSize(3);
        log.info("findOrgByPfmShouldBeSuccess :: END");
        responseValidate(response);
    }

    public void findOrgByPomShouldBeSuccess() {
        log.info("findOrgByPomShouldBeSuccess :: STARTED");
        Map<String, Object> response = professionalApiClient.retrieveOrganisationByOrgIdExternal(OK,
                professionalApiClient.getMultipleAuthHeaders(pomBearerToken));
        assertThat(response.get("paymentAccount")).asList().hasSize(3);
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
        assertThat(response.get("errorMessage")).isEqualTo("9 : Access Denied");
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

        assertThat(responseList.size()).isGreaterThanOrEqualTo(1);
        OrganisationMinimalInfoResponse org = responseList.get(0);
        assertThat(org.getName()).isNotNull();
        assertThat(org.getOrganisationIdentifier()).isNotNull();
        assertThat(org.getContactInformation()).isNotNull();
        log.info("findActiveOrganisationByPumShouldBeSuccess :: END");
    }

    public void findActiveOrganisationByCitizenOrCaseWorkerShouldBeSuccess() {
        log.info("findActiveOrganisationByCitizenOrCaseWorkerShouldBeSuccess :: STARTED");
        List<OrganisationMinimalInfoResponse> responseList = (List<OrganisationMinimalInfoResponse>)
                professionalApiClient.retrieveAllActiveOrganisationsWithMinimalInfo(
                        professionalApiClient.getMultipleAuthHeaders(idamOpenIdClient.getExternalOpenIdTokenWithRetry(
                                Arrays.asList(caseworker, citizen), firstName, lastName, generateRandomEmail())), OK,
                        ACTIVE, true);

        assertThat(responseList.size()).isGreaterThanOrEqualTo(1);
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
        assertThat(modifiedRoles).doesNotContain(puiOrgManager);
        log.info("deleteRolesByPumShouldBeSuccess :: STARTED");
    }

    public void addRolesByPendingExtUserShouldReturnForbidden() {
        log.info("addRolesByPendingExtUserShouldReturnForbidden :: STARTED");
        Map<String, Object> modifiedUserResponse = professionalApiClient.modifyUserToExistingUserForExternal(FORBIDDEN,
                addRoleRequest(puiOrgManager),
                professionalApiClient.getMultipleAuthHeaders(pumBearerToken), superUserId);
        assertThat(modifiedUserResponse.get("errorMessage")).isEqualTo("9 : Access Denied");
        assertThat(modifiedUserResponse.get("errorDescription"))
                .isEqualTo("User status must be Active to perform this operation");
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

    @Test
    @ToggleEnable(mapKey = "OrganisationMfaStatusController.retrieveMfaStatusByUserId", withFeature = true)
    public void findMFAScenario() {
        setUpOrgTestData();
        findMFAByUserIDShouldBeSuccess();
    }

    public void findMFAByUserIDShouldBeSuccess() {
        log.info("findMFAByUserIDShouldBeSuccess :: STARTED");
        Map<String, Object> mfaStatusResponse = professionalApiClient.findMFAByUserId(OK, superUserId);
        assertThat(mfaStatusResponse.get("mfa")).isEqualTo("EMAIL");
        log.info("findMFAByUserIDShouldBeSuccess :: END");
    }

    public void retrieveOrganisationPbaScenarios() {
       findOrganisationPbaWithEmailByExternalUserShouldBeSuccess();
    }

    public void findOrganisationPbaWithEmailByExternalUserShouldBeSuccess() {
        log.info("findOrganisationPbaWithEmailByExternalUserShouldBeSuccess :: STARTED");

        Map<String, Object> orgResponse = professionalApiClient.retrievePaymentAccountsByEmailForExternal(OK,
                professionalApiClient.getMultipleAuthHeaders(pumBearerToken), activeUserEmail);
        validatePbaResponse(orgResponse);
        log.info("findOrganisationPbaWithEmailByExternalUserShouldBeSuccess :: END");
    }

}
