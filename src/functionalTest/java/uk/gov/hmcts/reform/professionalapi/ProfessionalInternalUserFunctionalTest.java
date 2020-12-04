package uk.gov.hmcts.reform.professionalapi;

import static org.apache.commons.lang.RandomStringUtils.randomAlphabetic;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.TOO_MANY_REQUESTS;
import static uk.gov.hmcts.reform.professionalapi.client.ProfessionalApiClient.createOrganisationRequest;
import static uk.gov.hmcts.reform.professionalapi.controller.request.UserCreationRequest.aUserCreationRequest;

import lombok.extern.slf4j.Slf4j;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import net.thucydides.core.annotations.WithTag;
import net.thucydides.core.annotations.WithTags;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.professionalapi.controller.constants.IdamStatus;
import uk.gov.hmcts.reform.professionalapi.controller.request.NewUserCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.PbaEditRequest;
import uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus;
import uk.gov.hmcts.reform.professionalapi.domain.RoleName;
import uk.gov.hmcts.reform.professionalapi.domain.UserProfileUpdatedData;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RunWith(SpringIntegrationSerenityRunner.class)
@WithTags({@WithTag("testType:Functional")})
@Slf4j
public class ProfessionalInternalUserFunctionalTest extends AuthorizationFunctionalTest {

    String intActiveOrgId;
    String superUserEmail;
    String invitedUserEmail;
    String invitedUserId;

    OrganisationCreationRequest organisationCreationRequest;

    @Value("${resendInterval}")
    protected String resendInterval;

    /*
    Create Organisation
    Approve Org
    Invite User to Org
    Find user by Org
    Get Org
    Get PBA
     */

    /*
    Add/Modify roles to user --done
    Find User by email--done
    Reinvite User--done
    Edit PBA--done
    --Modify status of the user
    --Delete Organisation
    --Get all active org
     */

    @Test
    public void testInternalUserScenario() {
        setUpTestData();
        //create and approve org already taken care in AuthorizationFunctionalTest in BeforeClass
        inviteUserScenarios();
        findUsersByOrganisationScenarios();
        findOrganisationScenarios();
        retrieveOrganisationPbaScenarios();
        modifyUserRolesScenarios();

        reinviteUserScenarios();
        editPbaScenarios();
        //Find User by email is only external
        //Get all orgs are only external scenarios

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
        // TODO: super user fails 
        //superUserBearerToken = professionalApiClient.getMultipleAuthHeaders(
        //getExternalSuperUserTokenWithRetry(superUserEmail, "firstName", "lastName"));
        intActiveOrgId = createAndUpdateOrganisationToActive(hmctsAdmin, organisationCreationRequest);

        List<String> roles = new ArrayList<>();
        roles.add(puiCaseManager);
        roles.add(puiOrgManager);
        roles.add(puiFinanceManager);
        idamOpenIdClient.createUser(roles, invitedUserEmail, "firstName", "lastName");
 
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
        findAllOrganisationsByInternalUserShouldBeSuccess();
        findOrganisationByIdByInternalUserShouldBeSuccess();
        findActiveAndPendingOrganisationsByInternalUserShouldBeSuccess();
        findActiveOrganisationsByInternalUserShouldBeSuccess();
        findPendingOrganisationsByInternalUserShouldBeSuccess();
    }

    public void retrieveOrganisationPbaScenarios() {
        findOrganisationPbaWithEmailByInternalUserShouldBeSuccess();
        findOrganisationPbaWithEmailThroughHeaderByInternalUserShouldBeSuccess();
    }

    public void modifyUserRolesScenarios() {
        addRolesToUserShouldBeSuccess();
        deleteRolesOfUserShouldBeSuccess();
    }

    public void reinviteUserScenarios() {
        reinviteActiveUserShouldReturnBadRequest();
        reinviteUserWithinOneHourShouldReturnConflict();
    }

    public void editPbaScenarios() {
        editPaymentAccountsShouldReturnSuccess();
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
                hmctsAdmin, newUserCreationRequest, HttpStatus.NOT_FOUND);
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
    
    public void findAllOrganisationsByInternalUserShouldBeSuccess() {
        log.info("findAllOrganisationsByInternalUserShouldBeSuccess :: STARTED");
        Map<String, Object> response = professionalApiClient.retrieveAllOrganisations(hmctsAdmin);
        assertThat(response.get("organisations")).isNotNull();
        Assertions.assertThat(response.size()).isGreaterThanOrEqualTo(1);
        log.info("findAllOrganisationsByInternalUserShouldBeSuccess :: END");
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
                .modifyUserToExistingUserForPrdAdmin(HttpStatus.OK, userProfileUpdatedData, intActiveOrgId, invitedUserId);
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
                .modifyUserToExistingUserForPrdAdmin(HttpStatus.OK, userProfileUpdatedData, intActiveOrgId, invitedUserId);
        assertThat(modifiedUserResponse).isNotNull().hasSize(3);
        assertThat(((Map)((List)modifiedUserResponse.get("roleDeletionResponse")).get(0)).get("idamStatusCode")).isEqualTo("204");
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













    public void validateRoles(List<String> rolesToValidate) {
        Map<String, Object> searchResponse = professionalApiClient.searchOrganisationUsersByStatusInternal(intActiveOrgId,
                hmctsAdmin, HttpStatus.OK);
        List<Map> professionalUsersResponses1 = (List<Map>) searchResponse.get("users");
        Map professionalUsersResponse1 = getActiveUser(professionalUsersResponses1);
        assertThat(professionalUsersResponse1.get("roles")).isNotNull();
        List<String> rolesSize = (List) professionalUsersResponse1.get("roles");
        assertThat(rolesSize.size()).isEqualTo(rolesToValidate.size());
        assertThat(rolesSize).containsAll(rolesToValidate);
    }

    public void validatePbaResponse(Map<String, Object> response) {
        List<String> pbaList = (List)((Map)response.get("organisationEntityResponse")).get("paymentAccount");
        assertThat(pbaList).hasSize(3);
    }

    public void validateSingleOrgResponse(Map<String, Object> response, String status) {

        Assertions.assertThat(response.size()).isGreaterThanOrEqualTo(1);
        assertThat(response.get("organisationIdentifier")).isNotNull();
        assertThat(response.get("name")).isNotNull();
        assertThat(response.get("status")).isEqualTo(status);
        assertThat(response.get("sraId")).isNotNull();
        assertThat(response.get("sraRegulated")).isNotNull();
        assertThat(response.get("companyNumber")).isNotNull();
        assertThat(response.get("companyUrl")).isNotNull();
        assertThat(response.get("superUser")).isNotNull();
        assertThat(response.get("paymentAccount")).isNotNull();
        assertThat(response.get("contactInformation")).isNotNull();

    }

    void validateRetrievedUsers(Map<String, Object> searchResponse, String expectedStatus, Boolean rolesReturned) {
        assertThat(searchResponse.get("users")).asList().isNotEmpty();
        assertThat(searchResponse.get("organisationIdentifier")).isNotNull();
        List<HashMap> professionalUsersResponses = (List<HashMap>) searchResponse.get("users");

        professionalUsersResponses.forEach(user -> {
            assertThat(user.get("idamStatus")).isNotNull();
            assertThat(user.get("userIdentifier")).isNotNull();
            assertThat(user.get("firstName")).isNotNull();
            assertThat(user.get("lastName")).isNotNull();
            assertThat(user.get("email")).isNotNull();
            if (!expectedStatus.equals("any")) {
                assertThat(user.get("idamStatus").equals(expectedStatus));
            }
            if (rolesReturned) {
                if (user.get("idamStatus").equals(IdamStatus.ACTIVE.toString())) {
                    assertThat(user.get("roles")).isNotNull();
                } else {
                    assertThat(user.get("roles")).isNull();
                }
            }
        });
    }


}
