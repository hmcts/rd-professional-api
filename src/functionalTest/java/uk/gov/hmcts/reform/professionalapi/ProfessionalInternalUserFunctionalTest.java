package uk.gov.hmcts.reform.professionalapi;

import static org.apache.commons.lang.RandomStringUtils.randomAlphabetic;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.TOO_MANY_REQUESTS;
import static uk.gov.hmcts.reform.professionalapi.client.ProfessionalApiClient.createOrganisationRequest;
import static uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest.anOrganisationCreationRequest;
import static uk.gov.hmcts.reform.professionalapi.controller.request.UserCreationRequest.aUserCreationRequest;
import static uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus.REVIEW;

import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import net.thucydides.core.annotations.WithTag;
import net.thucydides.core.annotations.WithTags;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.professionalapi.controller.constants.IdamStatus;
import uk.gov.hmcts.reform.professionalapi.controller.request.MfaUpdateRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.NewUserCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.PbaEditRequest;
import uk.gov.hmcts.reform.professionalapi.domain.MFAStatus;
import uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus;
import uk.gov.hmcts.reform.professionalapi.domain.RoleName;
import uk.gov.hmcts.reform.professionalapi.domain.UserProfileUpdatedData;
import uk.gov.hmcts.reform.professionalapi.util.CustomSerenityRunner;
import uk.gov.hmcts.reform.professionalapi.util.ToggleEnable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RunWith(CustomSerenityRunner.class)
@WithTags({@WithTag("testType:Functional")})
@Slf4j
@SuppressWarnings("unchecked")
public class ProfessionalInternalUserFunctionalTest extends AuthorizationFunctionalTest {

    String intActiveOrgId;
    String superUserEmail;
    String invitedUserEmail;
    String invitedUserId;
    OrganisationCreationRequest organisationCreationRequest;

    @Test
    public void testInternalUserScenario() {
        setUpTestData();
        createOrganisationScenario();
        inviteUserScenarios();
        findUsersByOrganisationScenarios();
        findOrganisationScenarios();
        retrieveOrganisationPbaScenarios();
        modifyUserRolesScenarios();
        reinviteUserScenarios();
        editPbaScenarios();
        deleteOrganisationScenarios();
        updateOrgStatusShouldBeSuccess();
    }

    public void setUpTestData() {
        superUserEmail = generateRandomEmail();
        invitedUserEmail = generateRandomEmail();
        organisationCreationRequest = createOrganisationRequest()
                .superUser(aUserCreationRequest()
                        .firstName("firstName")
                        .lastName("lastName")
                        .email(superUserEmail)
                        .build())
                .build();
        intActiveOrgId = createAndUpdateOrganisationToActive(hmctsAdmin, organisationCreationRequest);

        List<String> roles = new ArrayList<>();
        roles.add(puiCaseManager);
        roles.add(puiOrgManager);
        roles.add(puiFinanceManager);
        idamOpenIdClient.createUser(roles, invitedUserEmail, "firstName", "lastName");
 
    }

    public void createOrganisationScenario() {
        createOrganisationWithoutS2STokenShouldReturnAuthorised();
    }

    public void inviteUserScenarios() {
        NewUserCreationRequest newUserCreationRequest = inviteUserByInternalUser();
        inviteUserWithInvalidRolesShouldReturnNotFound();
        inviteUserWithDuplicateUserShouldReturnConflict(newUserCreationRequest);
    }

    public void findUsersByOrganisationScenarios() {
        findUsersByInternalUserWithRolesShouldReturnSuccess();
        findUsersByInternalUserWithoutRolesShouldReturnSuccess();
        findUsersByInternalUserWithPaginationShouldReturnSuccess();
    }

    public void findOrganisationScenarios() {
        findOrganisationByIdByInternalUserShouldBeSuccess();
        findActiveAndPendingOrganisationsByInternalUserShouldBeSuccess();
        findActiveOrganisationsByInternalUserShouldBeSuccess();
        findPendingOrganisationsByInternalUserShouldBeSuccess();
    }

    public void retrieveOrganisationPbaScenarios() {
        findOrganisationPbaWithEmailByInternalUserShouldBeSuccess();
        findOrganisationPbaWithEmailThroughHeaderByInternalUserShouldBeSuccess();
        findOrganisationPbaWithoutEmailByInternalUserShouldBeBadRequest();
    }

    public void modifyUserRolesScenarios() {
        addRolesToUserShouldBeSuccess();
        deleteRolesOfUserShouldBeSuccess();
    }

    public void reinviteUserScenarios() {
        reinviteActiveUserShouldReturnBadRequest();
        reinviteUserWithinOneHourShouldReturnConflict();
        reinviteSuperUserWithinOneHourShouldReturnTooManyRequest();
    }

    public void editPbaScenarios() {
        editPaymentAccountsShouldReturnSuccess();
    }

    public void deleteOrganisationScenarios() {
        deletePendingOrganisationShouldReturnSuccess();
        deleteActiveOrganisationShouldReturnSuccess();
    }

    public void createOrganisationWithoutS2STokenShouldReturnAuthorised() {
        Response response =
                professionalApiClient.createOrganisationWithoutS2SToken(anOrganisationCreationRequest().build());
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
    }

    public NewUserCreationRequest inviteUserByInternalUser() {
        log.info("inviteUserByInternalUser :: STARTED");
        NewUserCreationRequest newUserCreationRequest = professionalApiClient.createNewUserRequest(invitedUserEmail);
        Map<String, Object> newUserResponse = professionalApiClient.addNewUserToAnOrganisation(intActiveOrgId,
                hmctsAdmin, newUserCreationRequest, HttpStatus.CREATED);
        assertThat(newUserResponse).isNotNull();
        assertThat(newUserResponse.get("userIdentifier")).isNotNull();
        invitedUserId = (String) newUserResponse.get("userIdentifier");
        log.info("inviteUserByInternalUser :: END");
        return newUserCreationRequest;
    }

    public void inviteUserWithInvalidRolesShouldReturnNotFound() {
        log.info("inviteUserWithInvalidRolesShouldReturnNotFound :: STARTED");
        NewUserCreationRequest newUserCreationRequest = professionalApiClient.createNewUserRequest();
        List<String> roles = new ArrayList<>();
        roles.add("unknown");
        newUserCreationRequest.setRoles(roles);
        Map<String, Object> newUserResponse = professionalApiClient.addNewUserToAnOrganisation(intActiveOrgId,
                hmctsAdmin, newUserCreationRequest, NOT_FOUND);
        log.info("inviteUserWithInvalidRolesShouldReturnNotFound :: END");
        assertThat(newUserResponse).isNotNull();
    }

    public void inviteUserWithDuplicateUserShouldReturnConflict(NewUserCreationRequest existingUserCreationRequest) {
        log.info("inviteUserWithDuplicateUserShouldReturnConflict :: STARTED");
        NewUserCreationRequest newUserCreationRequest = professionalApiClient.createNewUserRequest();
        newUserCreationRequest.setEmail(existingUserCreationRequest.getEmail());
        Map<String, Object> newUserResponse = professionalApiClient.addNewUserToAnOrganisation(intActiveOrgId,
                hmctsAdmin, newUserCreationRequest, HttpStatus.CONFLICT);
        assertThat((String) newUserResponse.get("errorDescription")).contains("409 User already exists");
        log.info("inviteUserWithDuplicateUserShouldReturnConflict :: END");
    }
    
    public void findUsersByInternalUserWithRolesShouldReturnSuccess() {
        log.info("findUsersByInternalUserWithRolesShouldReturnSuccess :: STARTED");
        validateRetrievedUsers(professionalApiClient
                .searchUsersByOrganisation(intActiveOrgId, hmctsAdmin, "True",
                        OK, ""), "any", true);
        log.info("findUsersByInternalUserWithRolesShouldReturnSuccess :: END");
    }

    public void findUsersByInternalUserWithoutRolesShouldReturnSuccess() {
        log.info("findUsersByInternalUserWithoutRolesShouldReturnSuccess :: STARTED");
        validateRetrievedUsers(professionalApiClient.searchUsersByOrganisation(intActiveOrgId, hmctsAdmin,
                "False", OK, "false"), "any", false);
        log.info("findUsersByInternalUserWithoutRolesShouldReturnSuccess :: END");
    }

    public void findUsersByInternalUserWithPaginationShouldReturnSuccess() {
        log.info("findUsersByInternalUserWithPaginationShouldReturnSuccess :: STARTED");
        Map<String, Object> searchResponse = professionalApiClient
                .searchUsersByOrganisationWithPagination(intActiveOrgId, hmctsAdmin, "False",
                        OK, "0", "1");

        validateRetrievedUsers(searchResponse, "any", true);
        List<HashMap> professionalUsersResponses = (List<HashMap>) searchResponse.get("users");

        assertThat(professionalUsersResponses.size()).isEqualTo(1);

        Map<String, Object> searchResponse2 = professionalApiClient
                .searchUsersByOrganisationWithPagination(intActiveOrgId, hmctsAdmin, "False",
                        OK, "1", "1");

        validateRetrievedUsers(searchResponse2, "any", true);
        List<HashMap> professionalUsersResponses2 = (List<HashMap>) searchResponse2.get("users");
        assertThat(professionalUsersResponses2.size()).isEqualTo(1);
        log.info("findUsersByInternalUserWithPaginationShouldReturnSuccess :: END");
    }

    public void findOrganisationByIdByInternalUserShouldBeSuccess() {
        log.info("findOrganisationByIdByInternalUserShouldBeSuccess :: STARTED");
        validateSingleOrgResponse(professionalApiClient.retrieveOrganisationDetails(
                intActiveOrgId, hmctsAdmin, OK), "ACTIVE");
        log.info("findOrganisationByIdByInternalUserShouldBeSuccess :: END");
    }

    public void findActiveAndPendingOrganisationsByInternalUserShouldBeSuccess() {
        log.info("findActiveAndPendingOrganisationsByInternalUserShouldBeSuccess :: STARTED");
        professionalApiClient.createOrganisation();
        Map<String, Object> finalResponse = professionalApiClient.retrieveAllOrganisations(hmctsAdmin);
        assertThat(finalResponse.get("organisations")).isNotNull();
        Assertions.assertThat(finalResponse.size()).isGreaterThanOrEqualTo(1);
        log.info("findActiveAndPendingOrganisationsByInternalUserShouldBeSuccess :: END");
    }

    public void findActiveOrganisationsByInternalUserShouldBeSuccess() {
        log.info("findActiveOrganisationsByInternalUserShouldBeSuccess :: STARTED");
        Map<String, Object> response = professionalApiClient
                .retrieveOrganisationDetailsByStatus(OrganisationStatus.PENDING.name(), hmctsAdmin);
        assertThat(response.size()).isGreaterThanOrEqualTo(1);
        log.info("findActiveOrganisationsByInternalUserShouldBeSuccess :: END");
    }

    public void findPendingOrganisationsByInternalUserShouldBeSuccess() {
        log.info("findPendingOrganisationsByInternalUserShouldBeSuccess :: STARTED");
        Map<String, Object> response = professionalApiClient
                .retrieveOrganisationDetailsByStatus(OrganisationStatus.ACTIVE.name(), hmctsAdmin);
        assertThat(response.get("organisations")).isNotNull();
        assertThat(response.size()).isGreaterThanOrEqualTo(1);
        log.info("findPendingOrganisationsByInternalUserShouldBeSuccess :: END");
    }
    
    public void findOrganisationPbaWithEmailByInternalUserShouldBeSuccess() {
        log.info("findOrganisationPbaWithEmailByInternalUserShouldBeSuccess :: STARTED");
        Map<String, Object> orgResponse = professionalApiClient.retrievePaymentAccountsByEmail(
                superUserEmail.toLowerCase(), hmctsAdmin);
        validatePbaResponse(orgResponse);
        log.info("findOrganisationPbaWithEmailByInternalUserShouldBeSuccess :: END");
    }

    public void findOrganisationPbaWithoutEmailByInternalUserShouldBeBadRequest() {
        log.info("findOrganisationPbaWithoutEmailByInternalUserShouldBeBadRequest :: STARTED");
        professionalApiClient.retrievePaymentAccountsWithoutEmailForInternal();
        log.info("findOrganisationPbaWithoutEmailByInternalUserShouldBeBadRequest :: END");
    }

    public void findOrganisationPbaWithEmailThroughHeaderByInternalUserShouldBeSuccess() {
        log.info("findOrganisationPbaWithEmailThroughHeaderByInternalUserShouldBeSuccess :: STARTED");
        Map<String, Object> orgResponse = professionalApiClient
                .retrievePaymentAccountsByEmailFromHeader(superUserEmail.toLowerCase(), hmctsAdmin);
        validatePbaResponse(orgResponse);
        log.info("findOrganisationPbaWithEmailThroughHeaderByInternalUserShouldBeSuccess :: END");
    }

    public void addRolesToUserShouldBeSuccess() {
        log.info("addRolesToUserShouldBeSuccess :: STARTED");
        UserProfileUpdatedData userProfileUpdatedData = new UserProfileUpdatedData();
        RoleName role1 = new RoleName(puiUserManager);
        Set<RoleName> roles = new HashSet<>();
        roles.add(role1);
        userProfileUpdatedData.setRolesAdd(roles);
        Map<String, Object> modifiedUserResponse = professionalApiClient
                .modifyUserToExistingUserForPrdAdmin(HttpStatus.OK, userProfileUpdatedData, intActiveOrgId,
                        invitedUserId);
        assertThat(modifiedUserResponse).isNotNull().hasSize(3);
        assertThat(((Map)modifiedUserResponse.get("roleAdditionResponse")).get("idamStatusCode")).isEqualTo("201");
        List<String> rolesToValidate = new ArrayList<>();
        rolesToValidate.add(puiUserManager);
        rolesToValidate.add(puiOrgManager);
        rolesToValidate.add(puiCaseManager);
        rolesToValidate.add(puiFinanceManager);
        rolesToValidate.add("caseworker");
        validateRoles(rolesToValidate);
        log.info("addRolesToUserShouldBeSuccess :: END");
    }

    public void deleteRolesOfUserShouldBeSuccess() {
        log.info("deleteRolesOfUserShouldBeSuccess :: STARTED");
        UserProfileUpdatedData userProfileUpdatedData = new UserProfileUpdatedData();
        RoleName role1 = new RoleName("caseworker");
        Set<RoleName> roles = new HashSet<>();
        roles.add(role1);
        userProfileUpdatedData.setRolesDelete(roles);
        Map<String, Object> modifiedUserResponse = professionalApiClient
                .modifyUserToExistingUserForPrdAdmin(
                        HttpStatus.OK, userProfileUpdatedData, intActiveOrgId, invitedUserId);
        assertThat(modifiedUserResponse).isNotNull().hasSize(3);
        assertThat(((Map)((List)modifiedUserResponse.get("roleDeletionResponse")).get(0)).get("idamStatusCode"))
                .isEqualTo("204");
        List<String> rolesToValidate = new ArrayList<>();
        rolesToValidate.add(puiUserManager);
        rolesToValidate.add(puiOrgManager);
        rolesToValidate.add(puiCaseManager);
        rolesToValidate.add(puiFinanceManager);
        validateRoles(rolesToValidate);
        log.info("deleteRolesOfUserShouldBeSuccess :: END");

    }

    public void reinviteActiveUserShouldReturnBadRequest() {
        log.info("reinviteActiveUserShouldReturnBadRequest :: STARTED");
        NewUserCreationRequest reInviteUserCreationRequest = professionalApiClient
                .createReInviteUserRequest(invitedUserEmail);
        Map<String, Object> reinviteUserResponse = professionalApiClient
                .addNewUserToAnOrganisation(intActiveOrgId, hmctsAdmin, reInviteUserCreationRequest, BAD_REQUEST);
        assertThat((String) reinviteUserResponse.get("errorDescription")).contains("User is not in PENDING state");
        log.info("reinviteActiveUserShouldReturnBadRequest :: END");
    }

    public void reinviteUserWithinOneHourShouldReturnConflict() {
        log.info("reinviteUserWithinOneHourShouldReturnConflict :: STARTED");
        NewUserCreationRequest reInviteUserCreationRequest = professionalApiClient
                .createReInviteUserRequest(superUserEmail);
        Map<String, Object> reinviteUserResponse = professionalApiClient
                .addNewUserToAnOrganisation(intActiveOrgId, hmctsAdmin, reInviteUserCreationRequest,
                        TOO_MANY_REQUESTS);
        assertThat((String) reinviteUserResponse.get("errorDescription"))
                .contains(String.format("The request was last made less than %s minutes ago. Please try after some"
                        + " time", resendInterval));
        log.info("reinviteUserWithinOneHourShouldReturnConflict :: END");
    }

    public void reinviteSuperUserWithinOneHourShouldReturnTooManyRequest() {
        log.info("reinviteSuperUserWithinOneHourShouldReturnTooManyRequest :: STARTED");
        NewUserCreationRequest newUserCreationRequest = professionalApiClient.createReInviteUserRequest(superUserEmail);
        Map<String, Object> reinviteUserResponse = professionalApiClient
                .addNewUserToAnOrganisation(intActiveOrgId, hmctsAdmin, newUserCreationRequest, TOO_MANY_REQUESTS);
        assertThat((String) reinviteUserResponse.get("errorDescription"))
                .contains(String.format("The request was last made less than %s minutes ago. Please try after "
                        + "some time", resendInterval));
        log.info("reinviteSuperUserWithinOneHourShouldReturnTooManyRequest :: END");
    }

    public void editPaymentAccountsShouldReturnSuccess() {
        log.info("editPaymentAccountsShouldReturnSuccess :: STARTED");
        String oldPba = organisationCreationRequest.getPaymentAccount().iterator().next();
        String pba1 = "PBA" + randomAlphabetic(7);
        String pba2 = "PBA" + randomAlphabetic(7);
        Set<String> paymentAccountsEdit = new HashSet<>();
        paymentAccountsEdit.add(oldPba);
        paymentAccountsEdit.add(pba1);
        paymentAccountsEdit.add(pba2);
        PbaEditRequest pbaEditRequest = new PbaEditRequest();
        pbaEditRequest.setPaymentAccounts(paymentAccountsEdit);
        Map<String, Object> pbaResponse = professionalApiClient.editPbaAccountsByOrgId(
                pbaEditRequest, intActiveOrgId, hmctsAdmin);
        assertThat(pbaResponse).isNotEmpty();

        Map<String, Object> org = professionalApiClient.retrieveOrganisationDetails(intActiveOrgId, hmctsAdmin, OK);
        assertThat((List)org.get("paymentAccount")).contains(oldPba.toUpperCase())
                .contains(pba1.toUpperCase())
                .contains(pba2.toUpperCase());
        log.info("editPaymentAccountsShouldReturnSuccess :: END");
    }

    public void modifyUserStatusToSuspendedShouldReturnSuccess() {
        log.info("modifyUserStatusToSuspendedShouldReturnSuccess :: STARTED");
        UserProfileUpdatedData data = getUserStatusUpdateRequest(IdamStatus.SUSPENDED);
        professionalApiClient.modifyUserToExistingUserForPrdAdmin(HttpStatus.OK, data, activeOrgId, invitedUserId);
        String status = searchUserStatus(activeOrgId, invitedUserId);
        assertThat(status).isEqualTo(IdamStatus.SUSPENDED.name());
        log.info("modifyUserStatusToSuspendedShouldReturnSuccess :: END");
    }

    public void deletePendingOrganisationShouldReturnSuccess() {
        log.info("modifyUserStatusToSuspendedShouldReturnSuccess :: STARTED");
        Map<String, Object> response = professionalApiClient.createOrganisation();
        String pendingOrgIdentifier = (String) response.get("organisationIdentifier");
        assertThat(pendingOrgIdentifier).isNotEmpty();
        professionalApiClient.deleteOrganisation(pendingOrgIdentifier, hmctsAdmin, NO_CONTENT);
        professionalApiClient.retrieveOrganisationDetails(pendingOrgIdentifier, hmctsAdmin, NOT_FOUND);
        log.info("modifyUserStatusToSuspendedShouldReturnSuccess :: END");
    }

    public void deleteActiveOrganisationShouldReturnSuccess() {
        log.info("deleteActiveOrganisationShouldReturnSuccess :: STARTED");
        String orgIdentifierResponse = createAndUpdateOrganisationToActive(hmctsAdmin);
        professionalApiClient.deleteOrganisation(orgIdentifierResponse, hmctsAdmin, NO_CONTENT);
        professionalApiClient.retrieveOrganisationDetails(orgIdentifierResponse, hmctsAdmin, NOT_FOUND);
        log.info("deleteActiveOrganisationShouldReturnSuccess :: END");
    }

    public void validateRoles(List<String> rolesToValidate) {
        Map<String, Object> searchResponse = professionalApiClient.searchOrganisationUsersByStatusInternal(
                intActiveOrgId, hmctsAdmin, HttpStatus.OK);
        List<Map> professionalUsersResponses1 = (List<Map>) searchResponse.get("users");
        Map professionalUsersResponse1 = getActiveUser(professionalUsersResponses1);
        assertThat(professionalUsersResponse1.get("roles")).isNotNull();
        List<String> rolesSize = (List) professionalUsersResponse1.get("roles");
        assertThat(rolesSize.size()).isEqualTo(rolesToValidate.size());
        assertThat(rolesSize).containsAll(rolesToValidate);
    }

    @Test
    @ToggleEnable(mapKey = "OrganisationInternalController.updateOrgMfaStatus", withFeature = true)
    public void updateOrgMfaScenario() {
        setUpTestData();
        updateOrgMfaShouldBeSuccess();
    }

    public void updateOrgMfaShouldBeSuccess() {
        log.info("updateOrgMFAShouldBeSuccess :: STARTED");

        Map<String, Object> searchResponse = professionalApiClient
                .searchOrganisationUsersByStatusInternal(intActiveOrgId, hmctsAdmin, OK);
        List<Map<String, Object>> professionalUsersResponses = (List<Map<String, Object>>) searchResponse.get("users");
        String superUserId = (String) (professionalUsersResponses.get(0)).get("userIdentifier");

        MFAStatus status = MFAStatus.NONE;
        MfaUpdateRequest mfaUpdateRequest = new MfaUpdateRequest(status);

        professionalApiClient.updateOrgMfaStatus(mfaUpdateRequest, intActiveOrgId, hmctsAdmin, OK);

        Map<String, Object> findOrgMfaStatusResponse = professionalApiClient.findMFAByUserId(OK, superUserId);
        assertThat(findOrgMfaStatusResponse.get("mfa")).isEqualTo(status.toString());

        log.info("updateOrgMFAShouldBeSuccess :: END");
    }

    @Test
    @ToggleEnable(mapKey = "OrganisationInternalController.updateOrgMfaStatus", withFeature = false)
    public void updateOrgMfaShouldReturn403WhenToggledOff() throws IOException {
        log.info("updateOrgMFAShouldReturn403 :: STARTED");

        setUpTestData();

        MFAStatus status = MFAStatus.NONE;
        MfaUpdateRequest mfaUpdateRequest = new MfaUpdateRequest(status);

        professionalApiClient.updateOrgMfaStatus(mfaUpdateRequest, intActiveOrgId, hmctsAdmin, FORBIDDEN);

        log.info("updateOrgMFAShouldReturn403 :: END");
    }

    public void updateOrgStatusShouldBeSuccess() {
        log.info("updateOrgStatusShouldBeSuccess :: STARTED");

        Map<String, Object> response = professionalApiClient.createOrganisation();
        String orgIdentifier = (String) response.get("organisationIdentifier");
        String statusMessage = "Company in review";

        professionalApiClient.updateOrganisationToReview(orgIdentifier, statusMessage, hmctsAdmin);

        Map<String, Object> orgResponse = professionalApiClient.retrieveOrganisationDetails(orgIdentifier, hmctsAdmin, OK);
        assertEquals(REVIEW, orgResponse.get("status"));
        assertEquals(statusMessage, orgResponse.get("statusMessage"));

        log.info("updateOrgStatusShouldBeSuccess :: END");
    }
}
