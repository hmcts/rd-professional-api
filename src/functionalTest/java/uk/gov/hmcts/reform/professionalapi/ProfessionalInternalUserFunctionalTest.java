package uk.gov.hmcts.reform.professionalapi;

import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import net.thucydides.core.annotations.WithTag;
import net.thucydides.core.annotations.WithTags;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.lib.util.serenity5.SerenityTest;
import uk.gov.hmcts.reform.professionalapi.controller.constants.IdamStatus;
import uk.gov.hmcts.reform.professionalapi.controller.request.DxAddressCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.MfaUpdateRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.NewUserCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.PbaRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.PbaUpdateRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.UpdatePbaRequest;
import uk.gov.hmcts.reform.professionalapi.controller.response.FetchPbaByStatusResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.OrganisationsWithPbaStatusResponse;
import uk.gov.hmcts.reform.professionalapi.domain.MFAStatus;
import uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus;
import uk.gov.hmcts.reform.professionalapi.domain.PbaStatus;
import uk.gov.hmcts.reform.professionalapi.domain.RoleName;
import uk.gov.hmcts.reform.professionalapi.domain.UserProfileUpdatedData;
import uk.gov.hmcts.reform.professionalapi.util.DateUtils;
import uk.gov.hmcts.reform.professionalapi.util.FeatureToggleConditionExtension;
import uk.gov.hmcts.reform.professionalapi.util.ToggleEnable;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;
import static org.apache.commons.lang.RandomStringUtils.randomAlphabetic;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.TOO_MANY_REQUESTS;
import static uk.gov.hmcts.reform.professionalapi.client.ProfessionalApiClient.createOrganisationRequest;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.PBA_STATUS_MESSAGE_ACCEPTED;
import static uk.gov.hmcts.reform.professionalapi.controller.request.ContactInformationCreationRequest.aContactInformationCreationRequest;
import static uk.gov.hmcts.reform.professionalapi.controller.request.DxAddressCreationRequest.dxAddressCreationRequest;
import static uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest.anOrganisationCreationRequest;
import static uk.gov.hmcts.reform.professionalapi.controller.request.UserCreationRequest.aUserCreationRequest;
import static uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus.REVIEW;
import static uk.gov.hmcts.reform.professionalapi.util.DateUtils.convertStringToLocalDate;
import static uk.gov.hmcts.reform.professionalapi.util.DateUtils.generateRandomDate;

@SerenityTest
@SpringBootTest
@WithTags({@WithTag("testType:Functional")})
@Slf4j
@SuppressWarnings("unchecked")
class ProfessionalInternalUserFunctionalTest extends AuthorizationFunctionalTest {

    String intActiveOrgId;
    String superUserEmail;
    String invitedUserEmail;
    String invitedUserId;
    List<String> invitedUserIds = new ArrayList<>();
    String lastRecordIdInPage;
    OrganisationCreationRequest organisationCreationRequest;

    @Test
    @DisplayName("PRD Internal Test Scenarios")
    void testInternalUserScenario() {
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
        deleteDxAddressScenarios();
        updateOrgStatusScenarios();
    }

    @Test
    @DisplayName("PRD Internal Test for Group Access Scenarios")
    void testGroupAccessInternalScenario() {
        String sinceDateTime = generateRandomDate(null, "30");
        log.info("sinceDateTime set is : {} ", sinceDateTime);
        setUpTestData();
        createOrganisationScenario();
        inviteMultipleUserScenarios();
        findUserInternalScenarios();
        findOrganisationWithSinceDateScenarios(sinceDateTime);
    }

    public void inviteMultipleUserScenarios() {
        inviteUserByAnInternalOrgUser(generateRandomEmail());
        for (int i = 0; i < 4; i++) {
            inviteUserByAnInternalOrgUser(generateRandomEmail());
        }
    }

    public void findUserInternalScenarios() {
        String sinceDateTime = generateRandomDate(null, "5");
        log.info("sinceDateTime set is : {} ", sinceDateTime);
        findByUserIdOrAndSinceDate(null, invitedUserIds.get(0));
        findByUserIdOrAndSinceDate(sinceDateTime, null);
        findByUserIdOrAndSinceDate(sinceDateTime, invitedUserId);
        findByUserIdOrAndSinceDate(null, null);

        findBySinceDatePageSizeOrAndSearchAfter(sinceDateTime, "3", null);
        findBySinceDatePageSizeOrAndSearchAfter(sinceDateTime, "1", lastRecordIdInPage);
        findBySinceDatePageSizeOrAndSearchAfter(sinceDateTime, null, lastRecordIdInPage);

        findByUserIdInvalidS2SToken(invitedUserIds.get(0));
        findByUserIdNotFound("non-existing-id");
    }

    public void inviteUserByAnInternalOrgUser(String email) {
        log.info("inviteUserByAnInternalOrgUser :: STARTED");
        NewUserCreationRequest newUserCreationRequest = professionalApiClient.createNewUserRequest(email);
        Map<String, Object> newUserResponse = professionalApiClient.addNewUserToAnOrganisation(intActiveOrgId,
                hmctsAdmin, newUserCreationRequest, HttpStatus.CREATED);
        assertThat(newUserResponse).isNotNull();
        assertThat(newUserResponse.get("userIdentifier")).isNotNull();
        invitedUserId = (String) newUserResponse.get("userIdentifier");
        invitedUserIds.add(invitedUserId);
        log.info("inviteUserByAnInternalOrgUser :: END");
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
        findPendingAndActiveOrganisationsByInternalUserShouldBeSuccess();
        findPendingAndReviewOrganisationsByInternalUserShouldBeSuccess();
        findOrganisationByUserIdByInternalUserShouldBeSuccess();
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

    public void deleteDxAddressScenarios(){
        deleteDxAddressShouldReturnSuccess();
        deleteEmptyContactListShouldReturnError();
        deleteNonExistDxAddressShouldReturnError();
    }

    @Test
    public void deleteDxAddressShouldReturnSuccess() {
        log.info("deleteDxAddressShouldReturnSuccess :: STARTED");
        String orgIdentifier = createAndUpdateOrganisationToActive(hmctsAdmin);
        JsonPath jsonPath = professionalApiClient.retrieveOrganisationDetails(orgIdentifier, hmctsAdmin, OK);
        String dxNumber = jsonPath.get("contactInformation[0].dxAddress[0].dxNumber");
        String dxExchange = jsonPath.get("contactInformation[0].dxAddress[0].dxExchange");
        Response deleteDxAddressResponse = professionalApiClient
                .deleteDxAddress(new DxAddressCreationRequest(dxNumber, dxExchange),
                orgIdentifier, NO_CONTENT);
        assertThat(deleteDxAddressResponse).isNotNull();
        assertThat(deleteDxAddressResponse.getStatusCode()).isEqualTo(204);
        JsonPath orgResponse = professionalApiClient.retrieveOrganisationDetails(orgIdentifier, hmctsAdmin, OK);
        assertThat(orgResponse).isNotNull();
        assertNotEquals(dxNumber, orgResponse.get("contactInformation[0].dxAddress[0].dxNumber"));
        log.info("deleteDxAddressShouldReturnSuccess :: END");
    }

    @Test
    public void deleteEmptyContactListShouldReturnError() {
        log.info("deleteEmptyContactListShouldReturnError :: STARTED");
        OrganisationCreationRequest orgCreationRequest = anOrganisationCreationRequest()
                .name("some-org-name")
                .status("ACTIVE")
                .superUser(aUserCreationRequest()
                        .firstName("some-fname")
                        .lastName("some-lname")
                        .email(generateRandomEmail().toLowerCase())
                        .build()).contactInformation(new LinkedList<>()).build();

        String orgIdentifier = createAndUpdateOrganisationToActive(hmctsAdmin, orgCreationRequest);

        Map<String, Object> deleteDxAddressResponse = professionalApiClient.deleteDxAddressWithResponse(
                new DxAddressCreationRequest("DX 1234567890", "dxExchange"),orgIdentifier);
        assertThat((String) deleteDxAddressResponse.get("errorDescription")).contains("No contact information  found");
        log.info("deleteEmptyContactListShouldReturnError :: END");
    }

    @Test
    public void deleteNonExistDxAddressShouldReturnError() {
        log.info("deleteNonExistDxAddressShouldReturnError :: STARTED");
        OrganisationCreationRequest orgCreationRequest = anOrganisationCreationRequest()
                .name("some-org-name")
                .status("ACTIVE")
                .superUser(aUserCreationRequest()
                        .firstName("some-fname")
                        .lastName("some-lname")
                        .email(generateRandomEmail().toLowerCase())
                        .build())
                .contactInformation(Arrays.asList(aContactInformationCreationRequest().addressLine1("address1")
                        .dxAddress(Arrays.asList(dxAddressCreationRequest()
                                .dxNumber("DX 1234567890")
                                .dxExchange("dxExchange").build()))
                        .build()))
                .build();

        String orgIdentifier = createAndUpdateOrganisationToActive(hmctsAdmin, orgCreationRequest);

        professionalApiClient.deleteDxAddress(new DxAddressCreationRequest("DX 1234567890", "dxExchange"),
                        orgIdentifier, HttpStatus.NO_CONTENT);
        Map<String, Object> deleteDxAddressResponse = professionalApiClient.deleteDxAddressWithResponse(
                new DxAddressCreationRequest("DX 1234567890", "dxExchange"),orgIdentifier);

        assertThat((String) deleteDxAddressResponse.get("errorDescription"))
                .contains("No dx address found for organisation");

        log.info("deleteNonExistDxAddressShouldReturnError :: END");
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

    public void findByUserIdOrAndSinceDate(String sinceDate, String userId) {
        log.info("findByUserIdOrAndSinceDate :: STARTED");
        Map<String, Object> testResults = professionalApiClient
                .retrieveUsersBySinceDateOrAndUserId(sinceDate, userId);
        if ((userId != null && sinceDate != null) || (userId == null && sinceDate == null)) {
            assertThat(testResults.get("errorDescription")).isEqualTo("001 missing/invalid parameter");
        } else {
            lastRecordIdInPage = (String) testResults.get("lastRecordInPage");
            assertNotNull(lastRecordIdInPage);
            validateRetrievedUsersDetails(testResults, null, sinceDate);
        }
        log.info("findByUserIdOrAndSinceDate :: END");
    }

    public void findBySinceDatePageSizeOrAndSearchAfter(String sinceDate, String pageSize, String searchAfter) {
        log.info("findBySinceDatePageSizeOrAndSearchAfter :: STARTED");

        Map<String, Object> testResults = professionalApiClient
                .retrieveUsersBySinceDatePageSizeOrAndSearchAfter(sinceDate, pageSize, searchAfter);
        List<HashMap> users = (List<HashMap>) testResults.get("users");
        lastRecordIdInPage = (String) testResults.get("lastRecordInPage");

        if (pageSize != null) {
            assertThat(users.size()).isEqualTo(Integer.parseInt(pageSize));
            validateRetrievedUsersDetails(testResults, pageSize, sinceDate);
        } else if (searchAfter != null && pageSize == null) {
            assertThat(testResults.get("errorDescription"))
                    .isEqualTo("002 missing/invalid page information");
        }
        log.info("findBySinceDatePageSizeOrAndSearchAfter :: END");
    }

    public void findByUserIdInvalidS2SToken(String userId) {
        log.info("findByUserIdWithInvalidS2SToken :: STARTED");
        professionalApiClient
                .retrieveUserByUnAuthorizedS2sToken(userId);
    }

    public void findByUserIdNotFound(String userId) {
        log.info("findByUserIdWithInvalidS2SToken :: STARTED");
        professionalApiClient
                .retrieveUserByIdNotFound(userId);
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
        var response = professionalApiClient.retrieveOrganisationDetails(
                intActiveOrgId, hmctsAdmin, OK);
        assertThat(response).isNotNull();
        verifyOrganisationDetails(response);
        log.info("findOrganisationByIdByInternalUserShouldBeSuccess :: END");
    }

    public void findOrganisationByUserIdByInternalUserShouldBeSuccess() {
        log.info("findOrganisationByUserIdByInternalUserShouldBeSuccess :: STARTED");
        var usersByOrganisationResponse =
                professionalApiClient.searchUsersByOrganisation(intActiveOrgId, hmctsAdmin,
                        "False", OK, "false");
        assertThat(usersByOrganisationResponse)
                .isNotNull()
                .hasSizeGreaterThanOrEqualTo(1);

        var users = (List<Map<String, Object>>) usersByOrganisationResponse.get("users");
        var userId = (String) users.get(0).get("userIdentifier");
        var response = professionalApiClient.retrieveOrganisationByUserId(
                userId, OK);

        verifyOrganisationDetails(response);

        log.info("findOrganisationByUserIdByInternalUserShouldBeSuccess :: END");
    }

    public void findActiveAndPendingOrganisationsByInternalUserShouldBeSuccess() {
        log.info("findActiveAndPendingOrganisationsByInternalUserShouldBeSuccess :: STARTED");
        professionalApiClient.createOrganisation();
        Map<String, Object> finalResponse = professionalApiClient.retrieveAllOrganisations(hmctsAdmin);
        assertThat(finalResponse.get("organisations")).isNotNull();
        Assertions.assertThat(finalResponse.size()).isPositive();
        log.info("findActiveAndPendingOrganisationsByInternalUserShouldBeSuccess :: END");
    }

    public void findActiveOrganisationsByInternalUserShouldBeSuccess() {
        log.info("findActiveOrganisationsByInternalUserShouldBeSuccess :: STARTED");
        Map<String, Object> response = professionalApiClient
                .retrieveOrganisationDetailsByStatus(OrganisationStatus.PENDING.name(), hmctsAdmin);
        assertThat(response.size()).isPositive();
        log.info("findActiveOrganisationsByInternalUserShouldBeSuccess :: END");
    }

    public void findPendingOrganisationsByInternalUserShouldBeSuccess() {
        log.info("findPendingOrganisationsByInternalUserShouldBeSuccess :: STARTED");
        Map<String, Object> response = professionalApiClient
                .retrieveOrganisationDetailsByStatus(OrganisationStatus.ACTIVE.name(), hmctsAdmin);
        assertThat(response.get("organisations")).isNotNull();
        assertThat(response.size()).isPositive();
        log.info("findPendingOrganisationsByInternalUserShouldBeSuccess :: END");
    }

    public void findPendingAndActiveOrganisationsByInternalUserShouldBeSuccess() {
        log.info("findPendingAndActiveOrganisationsByInternalUserShouldBeSuccess :: STARTED");
        Map<String, Object> response = professionalApiClient
                .retrieveOrganisationDetailsByStatus("ACTIVE,PENDING", hmctsAdmin);
        assertThat(response.get("organisations")).isNotNull();
        assertThat(response.size()).isGreaterThanOrEqualTo(1);
        assertThat(response.get("organisations").toString()).contains("status=ACTIVE");
        assertThat(response.get("organisations").toString()).contains("status=PENDING");
        log.info("findPendingAndActiveOrganisationsByInternalUserShouldBeSuccess :: END");
    }

    public void findPendingAndReviewOrganisationsByInternalUserShouldBeSuccess() {
        log.info("findPendingAndReviewOrganisationsByInternalUserShouldBeSuccess :: STARTED");

        Map<String, Object> response = professionalApiClient.createOrganisation();
        String orgIdentifier = (String) response.get("organisationIdentifier");
        String statusMessage = "Company in review";

        professionalApiClient
                .updateOrganisationToReview(orgIdentifier, statusMessage, hmctsAdmin);
        Map<String, Object> orgresponse = professionalApiClient
                .retrieveOrganisationDetailsByStatus("PENDING,REVIEW", hmctsAdmin);


        assertThat(orgresponse.get("organisations")).isNotNull();
        assertThat(orgresponse.size()).isGreaterThanOrEqualTo(1);
        assertThat(orgresponse.get("organisations").toString()).contains("status=PENDING");
        assertThat(orgresponse.get("organisations").toString()).contains("status=REVIEW");
        log.info("findPendingAndReviewOrganisationsByInternalUserShouldBeSuccess :: END");
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
        assertThat(((Map) modifiedUserResponse.get("roleAdditionResponse")).get("idamStatusCode")).isEqualTo("201");
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
        assertThat(((Map) ((List) modifiedUserResponse.get("roleDeletionResponse")).get(0)).get("idamStatusCode"))
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
        PbaRequest pbaEditRequest = new PbaRequest();
        pbaEditRequest.setPaymentAccounts(paymentAccountsEdit);
        Map<String, Object> pbaResponse = professionalApiClient.editPbaAccountsByOrgId(
                pbaEditRequest, intActiveOrgId, hmctsAdmin);
        assertThat(pbaResponse).isNotEmpty();

        JsonPath response = professionalApiClient.retrieveOrganisationDetails(intActiveOrgId, hmctsAdmin, OK);
        assertThat(response).isNotNull();
        assertThat(response.getList("paymentAccount")).contains(oldPba.toUpperCase())
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
    @ExtendWith(FeatureToggleConditionExtension.class)
    @ToggleEnable(mapKey = "OrganisationInternalController.updateOrgMfaStatus", withFeature = true)
    void updateOrgMfaScenario() {
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
        assertThat(findOrgMfaStatusResponse).containsEntry("mfa", status.toString());

        log.info("updateOrgMFAShouldBeSuccess :: END");
    }

    @Test
    @DisplayName("Update Organisation's MFA should return 403 when toggled off")
    @ExtendWith(FeatureToggleConditionExtension.class)
    @ToggleEnable(mapKey = "OrganisationInternalController.updateOrgMfaStatus", withFeature = false)
    void updateOrgMfaShouldReturn403WhenToggledOff() {
        log.info("updateOrgMFAShouldReturn403 :: STARTED");

        setUpTestData();

        MFAStatus status = MFAStatus.NONE;
        MfaUpdateRequest mfaUpdateRequest = new MfaUpdateRequest(status);

        professionalApiClient.updateOrgMfaStatus(mfaUpdateRequest, intActiveOrgId, hmctsAdmin, FORBIDDEN);

        log.info("updateOrgMFAShouldReturn403 :: END");
    }

    public void  updateOrgStatusScenarios() {
        updateOrgStatusShouldBeSuccess();
    }

    public void updateOrgStatusShouldBeSuccess() {
        log.info("updateOrgStatusShouldBeSuccess :: STARTED");

        Map<String, Object> response = professionalApiClient.createOrganisation();
        String orgIdentifier = (String) response.get("organisationIdentifier");
        String statusMessage = "Company in review";

        professionalApiClient.updateOrganisationToReview(orgIdentifier, statusMessage, hmctsAdmin);

        JsonPath jsonPath = professionalApiClient.retrieveOrganisationDetails(orgIdentifier, hmctsAdmin, OK);
        assertThat(jsonPath).isNotNull();
        assertEquals(REVIEW.toString(), jsonPath.get("status"));
        assertEquals(statusMessage, jsonPath.get("statusMessage"));

        log.info("updateOrgStatusShouldBeSuccess :: END");
    }

    @Test
    @ToggleEnable(mapKey = "OrganisationInternalController.retrieveOrgByPbaStatus", withFeature = true)
    @ExtendWith(FeatureToggleConditionExtension.class)
    void retrieveOrgsByPbaStatusScenario() {
        setUpTestData();
        findOrganisationByPbaStatusShouldBeSuccessWithPbas();
    }

    public void findOrganisationByPbaStatusShouldBeSuccessWithPbas() {
        log.info("findOrganisationByPbaStatusShouldBeSuccess :: STARTED");

        var orgsResponse = (List<OrganisationsWithPbaStatusResponse>)
                professionalApiClient.findOrganisationsByPbaStatus(OK, PbaStatus.ACCEPTED);

        assertNotNull(orgsResponse);
        assertThat(orgsResponse.size()).isPositive();
        assertThat(orgsResponse.stream()
                .filter(p -> p.getPbaNumbers().stream()
                        .anyMatch(r -> r.getStatusMessage() == null || !r.getStatusMessage()
                                .equals(PBA_STATUS_MESSAGE_ACCEPTED))))
                .allSatisfy(org -> org.getStatus().isActive());
        assertThat(orgsResponse.stream().filter(o -> o.getSuperUser() != null)).hasSizeGreaterThan(0);

        var pbaByStatusResponses = new ArrayList<FetchPbaByStatusResponse>();
        orgsResponse.forEach(org -> pbaByStatusResponses.addAll(org.getPbaNumbers()));

        assertNotNull(orgsResponse.get(0).getOrganisationName());
        assertThat(pbaByStatusResponses.size()).isPositive();
        assertThat(pbaByStatusResponses.stream()).allMatch(pba -> pba.getStatus().equals("ACCEPTED"));
        assertThat(pbaByStatusResponses).allMatch(pba -> nonNull(pba.getDateAccepted()));

        log.info("findOrganisationByPbaStatusShouldBeSuccess :: END");
    }

    @Test
    @ToggleEnable(mapKey = "OrganisationInternalController.retrieveOrgByPbaStatus", withFeature = false)
    @ExtendWith(FeatureToggleConditionExtension.class)
    void findOrganisationByPbaStatusShouldReturn403WhenToggledOff() {
        log.info("findOrganisationByPbaStatusShouldReturn403WhenToggledOff :: STARTED");

        professionalApiClient.findOrganisationsByPbaStatus(OK, PbaStatus.ACCEPTED);

        log.info("findOrganisationByPbaStatusShouldReturn403WhenToggledOff :: END");

    }

    @Test
    @ToggleEnable(mapKey = "OrganisationInternalController.updateAnOrganisationsRegisteredPbas", withFeature = true)
    @ExtendWith(FeatureToggleConditionExtension.class)
    void updatePaymentAccountsShouldReturnSuccess() {
        log.info("updatePaymentAccountsShouldReturnSuccess :: STARTED");
        setUpTestData();

        Map<String, Object> orgResponse = professionalApiClient.retrievePaymentAccountsByEmail(
                superUserEmail.toLowerCase(), hmctsAdmin);

        List<String> pbaList = (List) ((Map) orgResponse.get("organisationEntityResponse")).get("paymentAccount");

        String orgPba = pbaList.get(0);

        List<PbaUpdateRequest> pbaRequestList = new ArrayList<>();

        pbaRequestList.add(new PbaUpdateRequest(orgPba, PbaStatus.REJECTED.name(), ""));

        UpdatePbaRequest updatePbaRequest = new UpdatePbaRequest();
        updatePbaRequest.setPbaRequestList(pbaRequestList);

        Map<String, Object> updatePbaResponse =
                professionalApiClient.updatePbas(updatePbaRequest, intActiveOrgId, hmctsAdmin, OK);

        assertThat(updatePbaResponse).isNotNull();
        assertThat(updatePbaResponse.get("pbaUpdateStatusResponses")).isNull();

        Map<String, Object> orgResponse1 = professionalApiClient.retrievePaymentAccountsByEmail(
                superUserEmail.toLowerCase(), hmctsAdmin);

        List<String> updatedPbaList =
                (List) ((Map) orgResponse1.get("organisationEntityResponse")).get("paymentAccount");

        assertThat(updatedPbaList).doesNotContain(orgPba);

        log.info("updatePaymentAccountsShouldReturnSuccess :: END");
    }

    @Test
    @ToggleEnable(mapKey = "OrganisationInternalController.updateAnOrganisationsRegisteredPbas", withFeature = true)
    @ExtendWith(FeatureToggleConditionExtension.class)
    void updatePaymentAccountsShouldReturnPartialSuccess() {
        log.info("updatePaymentAccountsShouldReturnPartialSuccess :: STARTED");
        setUpTestData();

        Map<String, Object> orgResponse = professionalApiClient.retrievePaymentAccountsByEmail(
                superUserEmail.toLowerCase(), hmctsAdmin);
        List<String> pbaList = (List) ((Map) orgResponse.get("organisationEntityResponse")).get("paymentAccount");

        List<PbaUpdateRequest> pbaRequestList = new ArrayList<>();

        pbaList.forEach(pba -> pbaRequestList.add(new PbaUpdateRequest(pba, PbaStatus.REJECTED.name(), "")));

        pbaRequestList.add(new PbaUpdateRequest("PBA123", PbaStatus.ACCEPTED.name(), ""));
        pbaRequestList.add(new PbaUpdateRequest("PBA456", PbaStatus.ACCEPTED.name(), ""));

        UpdatePbaRequest updatePbaRequest = new UpdatePbaRequest();
        updatePbaRequest.setPbaRequestList(pbaRequestList);

        Map<String, Object> updatePbaResponse =
                professionalApiClient.updatePbas(updatePbaRequest, intActiveOrgId, hmctsAdmin, OK);

        assertThat(updatePbaResponse).isNotNull();
        assertThat(updatePbaResponse.get("pbaUpdateStatusResponses")).isNotNull();
        assertThat(updatePbaResponse.get("message")).hasToString("Some of the PBAs updated successfully");
        List pbaResponses = (List) updatePbaResponse.get("pbaUpdateStatusResponses");
        assertThat(pbaResponses.size()).isEqualTo(2);
        assertThat(updatePbaResponse.get("pbaUpdateStatusResponses").toString())
                .contains("PBA numbers must start with PBA/pba and be followed by 7 alphanumeric characters");

        log.info("updatePaymentAccountsShouldReturnPartialSuccess :: END");
    }

    @Test
    @ToggleEnable(mapKey = "OrganisationInternalController.updateAnOrganisationsRegisteredPbas", withFeature = false)
    @ExtendWith(FeatureToggleConditionExtension.class)
    void updatePaymentAccountsShouldReturnForbiddenWhenToggledOff() {
        log.info("updatePaymentAccountsShouldReturnForbiddenWhenToggledOff :: STARTED");
        List<PbaUpdateRequest> pbaRequestList = new ArrayList<>();

        pbaRequestList.add(new PbaUpdateRequest("PBA123", PbaStatus.ACCEPTED.name(), ""));
        pbaRequestList.add(new PbaUpdateRequest("PBA456", PbaStatus.ACCEPTED.name(), ""));

        UpdatePbaRequest updatePbaRequest = new UpdatePbaRequest();
        updatePbaRequest.setPbaRequestList(pbaRequestList);

        professionalApiClient.updatePbas(updatePbaRequest, intActiveOrgId, hmctsAdmin, FORBIDDEN);

        log.info("updatePaymentAccountsShouldReturnForbiddenWhenToggledOff :: END");
    }

    @Test
    void findOrganisationsWithPaginationShouldReturnSuccess() {
        log.info("findOrganisationsWithPaginationShouldReturnSuccess :: STARTED");
        professionalApiClient.createOrganisation();
        Map<String, Object> organisations = professionalApiClient
            .retrieveAllOrganisationsWithPagination(hmctsAdmin, "1", "2");

        assertThat(organisations).isNotNull().hasSize(2);

        log.info("findOrganisationsWithPaginationShouldReturnSuccess :: END");
    }

    //Will add below code as soon as RDCC-7024 will be pushed in Dev branch
    /*@Test
    @DisplayName("PRD OrganisationDetail for bulk Customer when Org is ACTIVE")
    void findOrganisationsForBulkCustomerShouldReturnSuccess() {
        log.info("findOrganisationsForBulkCustomerShouldReturnSuccess :: STARTED");
        String bulkCustomerId = "6601e79e-3169-461d-a751-59a33a5sdfk";
        String sidamId = "6601e79e-3169-461d-a751-60a33a5sdf4";
        Map<String, Object> organisations = professionalApiClient
                .retrieveOrganisationForBulkCustomerDetails(bulkCustomerId, civilAdmin,sidamId, OK);

        assertThat(organisations).isNotNull().hasSize(3);
        assertThat(organisations.get("organisationId")).toString().contains("c5e5c75d-cced-4e57-97c8-e359ce33a855");
        assertThat(organisations.get("organisationName")).toString().contains("ORGTESTUSER-PBA-ACCEPTED");
        assertThat(organisations.get("paymentAccount")).toString().contains("pba-3234569");


        log.info("findOrganisationsForBulkCustomerShouldReturnSuccess :: END");
    }

    @Test
    @DisplayName("PRD OrganisationDetail for bulk Customer when PBA statis is not ACCEPTED")
    void findOrganisationsForBulkCustomerForPbaStatusIsNotAccepted() {
        log.info("findOrganisationsForBulkCustomerForPBAStatusIsNotAccepted :: STARTED");
        String bulkCustomerId = "6601e79e-3169-461d-a751-59a33a5sdfj";
        String sidamId = "6601e79e-3169-461d-a751-60a33a5sdf6";
        Map<String, Object> organisations = professionalApiClient
                .retrieveOrganisationForBulkCustomerDetails(bulkCustomerId, civilAdmin, sidamId, OK);

        assertThat(organisations).isNotNull().hasSize(3);
        assertThat(organisations.get("organisationId")).toString().contains("c5e5c75d-cced-4e57-97c8-e359ce33a857");
        assertThat(organisations.get("organisationName")).toString().contains("ORGTESTUSER-PBA-ACCEPTED");
        assertThat(organisations.get("paymentAccount")).toString().contains("");


        log.info("findOrganisationsForBulkCustomerForPBAStatusIsNotAccepted :: END");
    }

    @Test
    @DisplayName("PRD OrganisationDetail for bulk Customer when Org is PENDING")
    void findOrganisationsForBulkCustomerForPendingOrg() {
        log.info("findOrganisationsForBulkCustomerForPendingOrg :: STARTED");
        String bulkCustomerId = "6601e79e-3169-461d-a751-59a33a5sdfl";
        String sidamId = "6601e79e-3169-461d-a751-60a33a5sdf5";
        Map<String, Object> organisations = professionalApiClient
                .retrieveOrganisationForBulkCustomerDetails(bulkCustomerId,civilAdmin, sidamId, NOT_FOUND);

        assertThat(organisations.get("status")).toString().contains("No Organisations found");


        log.info("findOrganisationsForBulkCustomerForPendingOrg :: END");
    }*/

    @Test
    @DisplayName("PRD Internal Delete Organisation with status REVIEW Test Scenarios")
    void testInternalOrganisationDeleteScenario() {

        Map<String, Object> response = professionalApiClient.createOrganisation();
        String orgIdentifier = (String) response.get("organisationIdentifier");
        String statusMessage = "Company in review";

        professionalApiClient
                .updateOrganisationToReview(orgIdentifier, statusMessage, hmctsAdmin);

        JsonPath orgResponse = professionalApiClient.retrieveOrganisationDetails(orgIdentifier, hmctsAdmin, OK);
        assertEquals(REVIEW.toString(), orgResponse.get("status"));
        assertEquals(statusMessage, orgResponse.get("statusMessage"));

        professionalApiClient.deleteOrganisation(orgIdentifier, hmctsAdmin, NO_CONTENT);

        professionalApiClient
                .retrieveOrganisationDetails(orgIdentifier, hmctsAdmin, NOT_FOUND);
    }

    private static void verifyOrganisationDetails(JsonPath response) {

        String companyUrl = response.get("companyUrl");
        assertThat(companyUrl)
                .isNotNull()
                .endsWith("-prd-func-test-company-url");

        String organisationIdentifier = response.get("organisationIdentifier");
        assertThat(organisationIdentifier)
                .isNotNull();

        Map<String, String> superUser = response.get("superUser");
        assertThat(superUser)
                .isNotNull();

        String firstName = superUser.get("firstName");
        assertThat(firstName)
                .isNotNull()
                .isEqualTo("firstName");

        String lastName = superUser.get("lastName");
        assertThat(lastName)
                .isNotNull()
                .endsWith("lastName");

        String email = superUser.get("email");
        assertThat(email)
                .isNotNull()
                .endsWith("@prdfunctestuser.com");

        String sraId = response.get("sraId");
        assertThat(sraId)
                .isNotNull()
                .endsWith("-prd-func-test-sra-id");

        String companyNumber = response.get("companyNumber");
        assertThat(companyNumber)
                .isNotNull()
                .endsWith("com");

        String dateReceived = response.get("dateReceived");
        assertThat(dateReceived)
                .isNotNull();

        String dateApproved = response.get("dateApproved");
        assertThat(dateApproved)
                .isNotNull();

        String name = response.get("name");
        assertThat(name)
                .isNotNull()
                .endsWith("-prd-func-test-name");

        Boolean sraRegulated = response.get("sraRegulated");
        assertThat(sraRegulated)
                .isNotNull()
                .isEqualTo(false);

        String status = response.get("status");
        assertThat(status)
                .isNotNull()
                .isEqualTo("ACTIVE");

        List<String> pendingPaymentAccount = response.getList("pendingPaymentAccount");
        assertThat(pendingPaymentAccount)
                .isNotNull()
                .isEmpty();

        verifyContactInformationDetails(response);
    }

    public void findOrganisationWithSinceDateScenarios(String sinceDate) {
        findOrganisationBySinceDateInternalShouldBeSuccess(sinceDate, null, null);
        findOrganisationBySinceDateInternalShouldBeSuccess(sinceDate, "1", "2");
    }

    public void findOrganisationBySinceDateInternalShouldBeSuccess(String sinceDate, String page,
                                                                   String pageSize) {
        log.info("findOrganisationBySinceDateInternalShouldBeSuccess :: STARTED");
        Map<String, Object> response = professionalApiClient.retrieveOrganisationDetailsBySinceDate(
                sinceDate, page, pageSize);
        assertThat(response).isNotNull();
        verifyOrganisationDetailsBySinceDate(response, pageSize, sinceDate);
        log.info("findOrganisationBySinceDateInternalShouldBeSuccess :: END");
    }

    private static void verifyOrganisationDetailsBySinceDate(Map<String, Object> response,
                                                             String pageSize, String sinceDate) {

        List<HashMap> organisations = (List<HashMap>) response.get("organisations");

        assertThat(organisations)
                .isNotNull()
                .isNotEmpty();

        if (pageSize != null) {
            assertThat(organisations)
                    .hasSize(Integer.parseInt(pageSize));
        }
        assertThat(response.get("moreAvailable"))
                .isNotNull();

        LocalDateTime sinceLocalDateTime = convertStringToLocalDate(sinceDate);

        organisations.forEach(org -> {

            assertThat(org.get("organisationIdentifier"))
                    .isNotNull();

            assertThat(org.get("lastUpdated"))
                    .isNotNull();

            String dateString = (String) org.get("lastUpdated");
            String formattedDateString = DateUtils.formatDateString(dateString);
            LocalDateTime responseLocalDateTime = convertStringToLocalDate(formattedDateString);
            assertTrue(responseLocalDateTime.isAfter(sinceLocalDateTime));

            List<String> organisationProfileIds = (ArrayList<String>) org.get("organisationProfileIds");

            if (organisationProfileIds != null) {

                assertThat(organisationProfileIds)
                        .isNotEmpty()
                        .hasSizeGreaterThan(0);

                assertThat(organisationProfileIds.get(0))
                        .isEqualTo("SOLICITOR_PROFILE");
            }
        });
    }

    private static void verifyOrganisationDetailsBySinceDateV2(Map<String, Object> response,
                                                               String pageSize, String sinceDate) {

        List<HashMap> organisations = (List<HashMap>) response.get("organisations");

        assertThat(organisations)
                .isNotNull()
                .isNotEmpty();

        if (pageSize != null) {
            assertThat(organisations)
                    .hasSize(Integer.parseInt(pageSize));
        }
        assertThat(response.get("moreAvailable"))
                .isNotNull();

        LocalDateTime sinceLocalDateTime = convertStringToLocalDate(sinceDate);

        organisations.forEach(org -> {

            assertThat(org.get("organisationIdentifier"))
                    .isNotNull();

            assertThat(org.get("lastUpdated"))
                    .isNotNull();

            String dateString = (String) org.get("lastUpdated");
            String formattedDateString = DateUtils.formatDateString(dateString);
            LocalDateTime responseLocalDateTime = convertStringToLocalDate(formattedDateString);
            assertTrue(responseLocalDateTime.isAfter(sinceLocalDateTime));

            List<String> organisationProfileIds = (ArrayList<String>) org.get("organisationProfileIds");
            assertThat(organisationProfileIds)
                    .isNotEmpty()
                    .hasSizeGreaterThan(0);

            assertThat(organisationProfileIds.get(0))
                    .isEqualTo("SOLICITOR_PROFILE");
        });
    }


    private static void verifyContactInformationDetails(JsonPath response) {

        List<Map<String, Object>> contactInformation = response.getList("contactInformation");
        assertThat(contactInformation)
                .isNotNull()
                .hasSize(2);

        contactInformation = sortByValue(contactInformation, "addressLine1");

        final Map<String, Object> contactInformation1 = contactInformation.get(0);
        final Map<String, Object> contactInformation2 = contactInformation.get(1);

        String firstAddressUprn = (String) contactInformation1.get("uprn");
        assertThat(firstAddressUprn)
                .isNotNull()
                .isEqualTo("uprn");

        String firstAddressCountry = (String) contactInformation1.get("country");
        assertThat(firstAddressCountry)
                .isNotNull()
                .isEqualTo("some-country");

        String firstAddressCreated = (String) contactInformation1.get("created");
        assertThat(firstAddressCreated)
                .isNotNull();

        String firstAddressTownCity = (String) contactInformation1.get("townCity");
        assertThat(firstAddressTownCity)
                .isNotNull()
                .isEqualTo("some-town-city");

        String firstAddressCounty = (String) contactInformation1.get("county");
        assertThat(firstAddressCounty)
                .isNotNull()
                .isEqualTo("some-county");

        String firstAddressAddressLine1 = (String) contactInformation1.get("addressLine1");
        assertThat(firstAddressAddressLine1)
                .isNotNull()
                .isEqualTo("addLine1");

        assertThat(firstAddressCountry)
                .isNotNull()
                .isEqualTo("some-country");

        String firstAddressAddressLine2 = (String) contactInformation1.get("addressLine2");
        assertThat(firstAddressAddressLine2)
                .isNotNull()
                .isEqualTo("addLine2");

        String firstAddressPostCode1 = (String) contactInformation1.get("postCode");
        assertThat(firstAddressPostCode1)
                .isNotNull()
                .isEqualTo("some-post-code");

        String firstAddressAddressLine3 = (String) contactInformation1.get("addressLine3");
        assertThat(firstAddressAddressLine3)
                .isNotNull()
                .isEqualTo("addLine3");

        String firstAddressAddressId = (String) contactInformation1.get("addressId");
        assertThat(firstAddressAddressId)
                .isNotNull();

        List<Map<String, Object>> firstAddressDxAddress =
                (List<Map<String, Object>>) contactInformation1.get("dxAddress");
        assertThat(firstAddressDxAddress)
                .isNotNull()
                .hasSize(2);

        firstAddressDxAddress = sortByValue(firstAddressDxAddress, "dxNumber");

        final Map<String, Object> firstAddressDxAddress1 = firstAddressDxAddress.get(0);
        final Map<String, Object> firstAddressDxAddress2 = firstAddressDxAddress.get(1);

        Object firstAddressDxNumber1 = firstAddressDxAddress1.get("dxNumber");
        assertThat(firstAddressDxNumber1)
                .isNotNull()
                .isEqualTo("DX 123452222");

        Object firstAddressDxExchange1 = firstAddressDxAddress1.get("dxExchange");
        assertThat(firstAddressDxExchange1)
                .isNotNull()
                .isEqualTo("dxExchange");

        Object firstAddressDxNumber2 = firstAddressDxAddress2.get("dxNumber");
        assertThat(firstAddressDxNumber2)
                .isNotNull()
                .isEqualTo("DX 123456333");

        Object firstAddressDxExchange2 = firstAddressDxAddress2.get("dxExchange");
        assertThat(firstAddressDxExchange2)
                .isNotNull()
                .isEqualTo("dxExchange");

        String secondAddressUprn = (String) contactInformation2.get("uprn");
        assertThat(secondAddressUprn)
                .isNotNull()
                .isEqualTo("uprn1");

        String secondAddressCountry = (String) contactInformation2.get("country");
        assertThat(secondAddressCountry)
                .isNotNull()
                .isEqualTo("some-country");

        String secondAddressCreated = (String) contactInformation2.get("created");
        assertThat(secondAddressCreated)
                .isNotNull();

        String secondAddressTownCity = (String) contactInformation2.get("townCity");
        assertThat(secondAddressTownCity)
                .isNotNull()
                .isEqualTo("some-town-city");

        String secondAddressCounty = (String) contactInformation2.get("county");
        assertThat(secondAddressCounty)
                .isNotNull()
                .isEqualTo("some-county");

        String secondAddressAddressLine1 = (String) contactInformation2.get("addressLine1");
        assertThat(secondAddressAddressLine1)
                .isNotNull()
                .isEqualTo("addressLine1");

        String secondAddressAddressLine2 = (String) contactInformation2.get("addressLine2");
        assertThat(secondAddressAddressLine2)
                .isNotNull()
                .isEqualTo("addressLine2");

        String secondAddressPostCode = (String) contactInformation2.get("postCode");
        assertThat(secondAddressPostCode)
                .isNotNull()
                .isEqualTo("some-post-code");

        String secondAddressAddressLine3 = (String) contactInformation2.get("addressLine3");
        assertThat(secondAddressAddressLine3)
                .isNotNull()
                .isEqualTo("addressLine3");

        String secondAddressAddressId = (String) contactInformation2.get("addressId");
        assertThat(secondAddressAddressId)
                .isNotNull();

        List<Map<String, Object>> secondAddressDxAddress =
                (List<Map<String, Object>>) contactInformation2.get("dxAddress");
        assertThat(secondAddressDxAddress)
                .isNotNull()
                .hasSize(3);

        secondAddressDxAddress = sortByValue(secondAddressDxAddress, "dxNumber");

        final Map<String, Object> secondAddressDxAddress1 = secondAddressDxAddress.get(0);
        final Map<String, Object> secondAddressDxAddress2 = secondAddressDxAddress.get(1);
        final Map<String, Object> secondAddressDxAddress3 = secondAddressDxAddress.get(2);

        Object secondAddressDxNumber1 = secondAddressDxAddress1.get("dxNumber");
        assertThat(secondAddressDxNumber1)
                .isNotNull()
                .isEqualTo("DX 123456777");

        Object secondAddressDxExchange1 = secondAddressDxAddress1.get("dxExchange");
        assertThat(secondAddressDxExchange1)
                .isNotNull()
                .isEqualTo("dxExchange");

        Object secondAddressDxNumber2 = secondAddressDxAddress2.get("dxNumber");
        assertThat(secondAddressDxNumber2)
                .isNotNull()
                .isEqualTo("DX 123456788");

        Object secondAddressDxExchange2 = secondAddressDxAddress2.get("dxExchange");
        assertThat(secondAddressDxExchange2)
                .isNotNull()
                .isEqualTo("dxExchange");

        Object secondAddressDxNumber3 = secondAddressDxAddress3.get("dxNumber");
        assertThat(secondAddressDxNumber3)
                .isNotNull()
                .isEqualTo("DX 1234567890");

        Object secondAddressDxExchange3 = secondAddressDxAddress3.get("dxExchange");
        assertThat(secondAddressDxExchange3)
                .isNotNull()
                .isEqualTo("dxExchange");
    }

    private static List<Map<String, Object>> sortByValue(final List<Map<String, Object>> maps,
                                                         final String key) {
        return maps
                .stream()
                .sorted(Comparator.comparing(map -> (String) map.get(key)))
                .collect(Collectors.toList());
    }
}
