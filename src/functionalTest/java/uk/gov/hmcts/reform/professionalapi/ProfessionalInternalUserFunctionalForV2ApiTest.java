package uk.gov.hmcts.reform.professionalapi;

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
import uk.gov.hmcts.reform.professionalapi.controller.request.NewUserCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationByProfileIdsRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationOtherOrgsCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationSraUpdateRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.UsersInOrganisationsByOrganisationIdentifiersRequest;
import uk.gov.hmcts.reform.professionalapi.controller.response.UsersInOrganisationsByOrganisationIdentifiersResponse;
import uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus;
import uk.gov.hmcts.reform.professionalapi.util.DateUtils;
import uk.gov.hmcts.reform.professionalapi.util.FeatureToggleConditionExtension;
import uk.gov.hmcts.reform.professionalapi.util.OrganisationProfileIdConstants;
import uk.gov.hmcts.reform.professionalapi.util.ToggleEnable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang.RandomStringUtils.randomAlphabetic;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.HttpStatus.MULTI_STATUS;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;
import static uk.gov.hmcts.reform.professionalapi.client.ProfessionalApiClient.createOrganisationRequest;
import static uk.gov.hmcts.reform.professionalapi.client.ProfessionalApiClient.createOrganisationRequestForV2;
import static uk.gov.hmcts.reform.professionalapi.util.DateUtils.convertStringToLocalDate;
import static uk.gov.hmcts.reform.professionalapi.util.DateUtils.generateRandomDate;

@SerenityTest
@SpringBootTest
@WithTags({@WithTag("testType:Functional")})
@Slf4j
@SuppressWarnings("unchecked")
class ProfessionalInternalUserFunctionalForV2ApiTest extends AuthorizationFunctionalTest {

    String intActiveOrgId;
    String superUserEmail;
    String invitedUserEmail;
    String invitedUserId;
    String searchAfter;
    String sinceDateTime;
    String lastOrgIdInPage;
    String lastUserInPage;
    List<String> orgIdentifiersList = new ArrayList<>();
    OrganisationOtherOrgsCreationRequest organisationOtherOrgsCreationRequest;

    @Test
    @DisplayName("PRD Internal Test Scenarios For V2 API")
    @ToggleEnable(mapKey = "OrganisationInternalControllerV2.createOrganisation", withFeature = true)
    @ExtendWith(FeatureToggleConditionExtension.class)
    void testInternalUserScenario() {
        setUpTestData();
        createOrganisationScenario();
        findOrganisationScenarios();
        retrieveOrganisationPbaScenarios();
        findOrganisationWithSinceDateGroupAccessScenarios();
        retrieveOrganisationsByProfileIds();
        retrieveOrganisationsUsersBySearchAfterGA();
        deleteOtherOrganisationScenarios();
    }

    public void setUpTestData() {
        superUserEmail = generateRandomEmail();
        invitedUserEmail = generateRandomEmail();
        organisationOtherOrgsCreationRequest = createOrganisationRequestForV2();
        organisationOtherOrgsCreationRequest.getSuperUser().setEmail(superUserEmail);

        intActiveOrgId = createAndUpdateOrganisationToActiveForV2(hmctsAdmin, organisationOtherOrgsCreationRequest);
        orgIdentifiersList.add(intActiveOrgId);

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


    public void findOrganisationScenarios() {
        findOrganisationByIdByInternalUserShouldBeSuccess();
        findActiveAndPendingOrganisationsByInternalUserShouldBeSuccess();
        findActiveOrganisationsByInternalUserShouldBeSuccess();
        findPendingOrganisationsByInternalUserShouldBeSuccess();
        findPendingAndActiveOrganisationsByInternalUserShouldBeSuccess();
        findPendingAndReviewOrganisationsByInternalUserShouldBeSuccess();
    }

    public void retrieveOrganisationPbaScenarios() {
        findOrganisationPbaWithEmailByInternalUserShouldBeSuccess();
        findOrganisationPbaWithEmailThroughHeaderByInternalUserShouldBeSuccess();
        findOrganisationPbaWithoutEmailByInternalUserShouldBeBadRequest();
    }

    public void retrieveOrganisationsUsersBySearchAfterGA() {
        inviteMultipleUserScenarios();
        setUpTestData();
        createOrganisationScenario();
        inviteMultipleUserScenarios();

        findUsersInOrgWithPageSizeAndOrSearchAfter("1", null, null);
        findUsersInOrgWithPageSizeAndOrSearchAfter("2", lastOrgIdInPage, lastUserInPage);
        findUsersInOrgWithPageSizeAndOrSearchAfter("3", lastOrgIdInPage, lastUserInPage);
        findUsersInOrgShouldHaveBaqRequest("1", lastOrgIdInPage, null);
        findUsersInOrgShouldHaveBaqRequest("1", null, lastUserInPage);
        findUsersInOrgShouldHaveBaqRequest("1", null, "123");
        findUsersInOrgShouldHaveBaqRequest("1", "123", null);

        findUsersInOrganisationUnAuthorized("1");
        findUsersInOrgWithPageSizeAndOrSearchAfter(null, null, null);
    }

    public void inviteMultipleUserScenarios() {
        for (int i = 0; i < 2; i++) {
            inviteUserByAnInternalOrgUser(generateRandomEmail());
        }
    }

    public void inviteUserByAnInternalOrgUser(String email) {
        log.info("inviteUserByAnInternalOrgUser :: STARTED");
        NewUserCreationRequest newUserCreationRequest = professionalApiClient.createNewUserRequest(email);
        Map<String, Object> newUserResponse = professionalApiClient.addNewUserToAnOrganisation(intActiveOrgId,
                hmctsAdmin, newUserCreationRequest, HttpStatus.CREATED);
        assertThat(newUserResponse).isNotNull();
        assertThat(newUserResponse.get("userIdentifier")).isNotNull();
        invitedUserId = (String) newUserResponse.get("userIdentifier");
        log.info("inviteUserByAnInternalOrgUser :: END");
    }

    public void findUsersInOrgShouldHaveBaqRequest(String pageSize,
                                                   String searchAfterOrg, String searchAfterUser) {

        log.info("findUsersInOrgShouldHaveBaqRequest :: STARTED");

        UsersInOrganisationsByOrganisationIdentifiersRequest request =
                new UsersInOrganisationsByOrganisationIdentifiersRequest();

        request.setOrganisationIdentifiers(orgIdentifiersList);

        Map<String, Object> response = professionalApiClient
                .findUsersInOrgShouldHaveBadRequest(request, pageSize, searchAfterOrg, searchAfterUser);

        assertThat(response).isNotNull();

        if (searchAfterOrg != null && !searchAfterOrg.equals("123") && searchAfterUser == null) {
            assertThat(response.get("errorDescription"))
                    .isEqualTo("searchAfterUser cannot be null when searchAfterOrg is provided");
        } else if (searchAfterUser != null && !searchAfterUser.equals("123") && searchAfterOrg == null) {
            assertThat(response.get("errorDescription"))
                    .isEqualTo("searchAfterOrg cannot be null when searchAfterUser is provided");
        } else if (searchAfterUser != null && searchAfterUser.equals("123") && searchAfterOrg == null) {
            assertThat(response.get("errorDescription"))
                    .isEqualTo("Invalid UUID string: 123");
        } else if (searchAfterOrg != null && searchAfterUser == null && searchAfterOrg.equals("123")) {
            assertThat(response.get("errorDescription"))
                    .isEqualTo("Invalid UUID string: 123");
        }
        log.info("findUsersInOrgShouldHaveBaqRequest :: END");
    }

    public void findUsersInOrganisationUnAuthorized(String pageSize) {

        log.info("findUsersInOrganisationUnAuthorized :: STARTED");
        professionalApiClient
                .retrieveUsersInOrgByUnAuthorizedS2sToken(pageSize);
        log.info("findUsersInOrganisationUnAuthorized :: END");

    }

    public void findUsersInOrgWithPageSizeAndOrSearchAfter(String pageSize,
                                                           String searchAfterOrg, String searchAfterUser) {

        log.info("findUsersInOrgWithPageSizeAndOrSearchAfter :: STARTED");

        UsersInOrganisationsByOrganisationIdentifiersRequest request =
                new UsersInOrganisationsByOrganisationIdentifiersRequest();

        request.setOrganisationIdentifiers(orgIdentifiersList);

        UsersInOrganisationsByOrganisationIdentifiersResponse response =
                professionalApiClient.findUsersInOrganisationShouldBeSuccess(request,
                        pageSize, searchAfterOrg, searchAfterUser);

        assertThat(response).isNotNull();


        assertThat(response.getLastOrgInPage()).isNotNull();
        assertThat(response.getLastUserInPage()).isNotNull();
        lastOrgIdInPage = response.getLastOrgInPage().toString();
        lastUserInPage = response.getLastUserInPage().toString();

        assertThat(response.getOrganisationInfo().get(0).getUsers()).isNotEmpty();

        if (pageSize != null && pageSize.equals("2")) {
            assertThat(response.getOrganisationInfo().get(0).getUsers()).hasSize(Integer.parseInt(pageSize));
            assertThat(response.isMoreAvailable()).isTrue();
        } else if (pageSize != null && pageSize.equals("3")) {
            assertThat(response.getOrganisationInfo().get(0).getUsers()).hasSize(3);
            assertThat(response.isMoreAvailable()).isFalse();
        } else if (pageSize == null && searchAfterOrg == null && searchAfterUser == null) {
            assertThat(response.getOrganisationInfo()).isNotEmpty();
            assertThat(response.getOrganisationInfo()).hasSize(2);
            assertThat(response.getOrganisationInfo().get(0).getUsers()).isNotEmpty();
            assertThat(response.getOrganisationInfo().get(0).getUsers()).hasSize(3);
            assertThat(response.getOrganisationInfo().get(1).getUsers()).hasSize(3);
            assertThat(response.isMoreAvailable()).isFalse();
        }

        assertThat(response.getOrganisationInfo().get(0).getOrganisationIdentifier()).isNotNull();
        System.out.println("lastOrgIdInPage" + lastOrgIdInPage);
        System.out.println("lastUserInPage" + lastUserInPage);

        log.info("findUsersInOrgWithPageSizeAndOrSearchAfter :: END");
    }



    public void createOrganisationWithoutS2STokenShouldReturnAuthorised() {
        Response response =
                professionalApiClient.createOrganisationWithoutS2STokenV2(createOrganisationRequestForV2());
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


    public void findOrganisationByIdByInternalUserShouldBeSuccess() {
        log.info("findOrganisationByIdByInternalUserShouldBeSuccess :: STARTED");
        validateSingleOrgResponseForV2(professionalApiClient.retrieveOrganisationDetailsForV2(
                intActiveOrgId, hmctsAdmin, OK), "ACTIVE");
        log.info("findOrganisationByIdByInternalUserShouldBeSuccess :: END");
    }

    public void findActiveAndPendingOrganisationsByInternalUserShouldBeSuccess() {
        log.info("findActiveAndPendingOrganisationsByInternalUserShouldBeSuccess :: STARTED");
        professionalApiClient.createOrganisation();
        Map<String, Object> finalResponse = professionalApiClient.retrieveAllOrganisationsV2(hmctsAdmin);
        assertThat(finalResponse.get("organisations")).isNotNull();
        Assertions.assertThat(finalResponse.size()).isPositive();
        log.info("findActiveAndPendingOrganisationsByInternalUserShouldBeSuccess :: END");
    }

    public void findActiveOrganisationsByInternalUserShouldBeSuccess() {
        log.info("findActiveOrganisationsByInternalUserShouldBeSuccess :: STARTED");
        Map<String, Object> response = professionalApiClient
                .retrieveOrganisationDetailsByStatusV2(OrganisationStatus.PENDING.name(), hmctsAdmin);
        assertThat(response.size()).isPositive();
        log.info("findActiveOrganisationsByInternalUserShouldBeSuccess :: END");
    }

    public void findPendingOrganisationsByInternalUserShouldBeSuccess() {
        log.info("findPendingOrganisationsByInternalUserShouldBeSuccess :: STARTED");
        Map<String, Object> response = professionalApiClient
                .retrieveOrganisationDetailsByStatusV2(OrganisationStatus.ACTIVE.name(), hmctsAdmin);
        assertThat(response.get("organisations")).isNotNull();
        assertThat(response.size()).isPositive();
        log.info("findPendingOrganisationsByInternalUserShouldBeSuccess :: END");
    }

    public void findPendingAndActiveOrganisationsByInternalUserShouldBeSuccess() {
        log.info("findPendingAndActiveOrganisationsByInternalUserShouldBeSuccess :: STARTED");
        Map<String, Object> response = professionalApiClient
                .retrieveOrganisationDetailsByStatusV2("ACTIVE,PENDING", hmctsAdmin);
        assertThat(response.get("organisations")).isNotNull();
        assertThat(response.size()).isGreaterThanOrEqualTo(1);
        assertThat(response.get("organisations").toString()).contains("status=ACTIVE");
        assertThat(response.get("organisations").toString()).contains("status=PENDING");
        log.info("findPendingAndActiveOrganisationsByInternalUserShouldBeSuccess :: END");
    }

    public void findPendingAndReviewOrganisationsByInternalUserShouldBeSuccess() {
        log.info("findPendingAndReviewOrganisationsByInternalUserShouldBeSuccess :: STARTED");

        Map<String, Object> response = professionalApiClient.createOrganisationV2();
        String orgIdentifier = (String) response.get("organisationIdentifier");
        String statusMessage = "Company in review";

        professionalApiClient
                .updateOrganisationToReviewV2(orgIdentifier, statusMessage, hmctsAdmin);
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
        Map<String, Object> orgResponse = professionalApiClient.retrievePaymentAccountsByEmailV2(
                superUserEmail.toLowerCase(), hmctsAdmin);

        validatePbaResponse(orgResponse);
        log.info("findOrganisationPbaWithEmailByInternalUserShouldBeSuccess :: END");
    }

    public void findOrganisationPbaWithoutEmailByInternalUserShouldBeBadRequest() {
        log.info("findOrganisationPbaWithoutEmailByInternalUserShouldBeBadRequest :: STARTED");
        professionalApiClient.retrievePaymentAccountsWithoutEmailForInternalV2();

        log.info("findOrganisationPbaWithoutEmailByInternalUserShouldBeBadRequest :: END");
    }

    public void findOrganisationPbaWithEmailThroughHeaderByInternalUserShouldBeSuccess() {
        log.info("findOrganisationPbaWithEmailThroughHeaderByInternalUserShouldBeSuccess :: STARTED");
        Map<String, Object> orgResponse = professionalApiClient
                .retrievePaymentAccountsByEmailFromHeaderV2(superUserEmail.toLowerCase(), hmctsAdmin);

        validatePbaResponse(orgResponse);
        log.info("findOrganisationPbaWithEmailThroughHeaderByInternalUserShouldBeSuccess :: END");
    }



    @Test
    @ToggleEnable(mapKey = "OrganisationInternalControllerV2.createOrganisation", withFeature = true)
    @ExtendWith(FeatureToggleConditionExtension.class)
    void findOrganisationsWithPaginationShouldReturnSuccess() {
        log.info("findOrganisationsWithPaginationShouldReturnSuccess :: STARTED");
        professionalApiClient.createOrganisationV2();
        Map<String, Object> organisations = professionalApiClient
            .retrieveAllOrganisationsWithPaginationV2(hmctsAdmin, "1", "10");

        assertThat(organisations).isNotNull().hasSize(2);
        log.info("findOrganisationsWithPaginationShouldReturnSuccess :: END");
    }

    public void retrieveOrganisationsByProfileIds() {
        findOrganisationWithSolicitorProfileAndPageSizeShouldBeSuccess("2");
        findOrganisationWithSolicitorProfileAndPageSizeShouldBeSuccess("1");
        findOrganisationWithSolicitorProfilePageSizeAndOrSearchAfter(null, searchAfter);
        findOrganisationWithSolicitorProfilePageSizeAndOrSearchAfter(null, null);
        findByOrganisationsByInvalidS2SToken("1");
    }

    public void findOrganisationWithSolicitorProfileAndPageSizeShouldBeSuccess(String pageSize) {
        log.info("findOrganisationWithSolicitorProfileAndPageSizeShouldBeSuccess :: STARTED");

        OrganisationByProfileIdsRequest request = new OrganisationByProfileIdsRequest();
        request.getOrganisationProfileIds().add(OrganisationProfileIdConstants.SOLICITOR_PROFILE);
        Map<String, Object> response = professionalApiClient.retrieveOrganisationsByProfileIds(request,
                pageSize, null);

        verifyOrganisationsByProfileIdResponse(response, OrganisationProfileIdConstants.SOLICITOR_PROFILE, pageSize);
        log.info("findOrganisationWithSolicitorProfileAndPageSizeShouldBeSuccess :: END");
    }

    public void findOrganisationWithSolicitorProfilePageSizeAndOrSearchAfter(String pageSize, String searchAfter) {
        log.info("findOrganisationWithSolicitorProfilePageSizeAndOrSearchAfter :: STARTED");

        OrganisationByProfileIdsRequest request = new OrganisationByProfileIdsRequest();
        request.getOrganisationProfileIds().add("UNKNOWN");
        Map<String, Object> response = professionalApiClient.retrieveOrganisationsByProfileIds(request,
                pageSize, searchAfter);

        verifyOrganisationsByProfileIdResponse(response, OrganisationProfileIdConstants.SOLICITOR_PROFILE, pageSize);
        log.info("findOrganisationWithSolicitorProfilePageSizeAndOrSearchAfter :: END");
    }

    public void findByOrganisationsByInvalidS2SToken(String pageSize) {
        log.info("findByOrganisationsByInvalidS2SToken :: STARTED");
        professionalApiClient
                .retrieveOrganisationsByUnAuthorizedS2sToken(pageSize);
    }

    private void verifyOrganisationsByProfileIdResponse(Map<String, Object> response, String expectedProfileId,
                                                        String expectCount) {

        assertThat(response.get("moreAvailable")).isNotNull();
        assertThat(response.get("lastRecordInPage")).isNotNull();
        searchAfter = (String) response.get("lastRecordInPage");
        List<HashMap> orgInfo = (List<HashMap>) response.get("organisationInfo");
        assertThat(orgInfo).isNotEmpty();
        if (expectCount != null) {
            assertThat((orgInfo)).hasSize(Integer.parseInt(expectCount));
        }

        orgInfo.forEach(org -> {
            assertThat(org.get("organisationIdentifier")).isNotNull();
            assertThat(org.get("status")).isNotNull();
            assertThat(org.get("lastUpdated")).isNotNull();

            List<String> organisationProfileIdList = (List<String>) org.get("organisationProfileIds");
            assertThat(organisationProfileIdList).isNotEmpty();
            assertThat((organisationProfileIdList)).hasSize(1);
            assertThat(organisationProfileIdList.get(0).equals(expectedProfileId));
        });

    }

    public void deleteOtherOrganisationScenarios() {
        deletePendingOtherOrganisationShouldReturnSuccess();
        deleteActiveOtherOrganisationShouldReturnSuccess();
    }


    public void deletePendingOtherOrganisationShouldReturnSuccess() {
        log.info("modifyUserStatusToSuspendedShouldReturnSuccess :: STARTED");
        Map<String, Object> response = professionalApiClient.createOrganisationV2();
        String pendingOrgIdentifier = (String) response.get("organisationIdentifier");
        assertThat(pendingOrgIdentifier).isNotEmpty();
        professionalApiClient.deleteOrganisation(pendingOrgIdentifier, hmctsAdmin, NO_CONTENT);
        professionalApiClient.retrieveOrganisationDetailsForV2(pendingOrgIdentifier, hmctsAdmin, NOT_FOUND);
        log.info("modifyUserStatusToSuspendedShouldReturnSuccess :: END");
    }

    public void deleteActiveOtherOrganisationShouldReturnSuccess() {
        log.info("deleteActiveOtherOrganisationShouldReturnSuccess :: STARTED");
        String orgIdentifierResponse = createAndUpdateOrganisationToActiveForV2(hmctsAdmin,professionalApiClient
                .createOrganisationRequestForV2());
        professionalApiClient.deleteOrganisation(orgIdentifierResponse, hmctsAdmin, NO_CONTENT);
        professionalApiClient.retrieveOrganisationDetailsForV2(orgIdentifierResponse, hmctsAdmin, NOT_FOUND);
        log.info("deleteActiveOtherOrganisationShouldReturnSuccess :: END");
    }

    public void findOrganisationWithSinceDateGroupAccessScenarios() {
        sinceDateTime = generateRandomDate(null, "30");
        log.info("Since Date is set to : {} ", sinceDateTime);
        findOrganisationBySinceDateInternalV2ShouldBeSuccess(sinceDateTime, null, null);
        findOrganisationBySinceDateInternalV2ShouldBeSuccess(sinceDateTime, "1", "2");
    }

    public void findOrganisationBySinceDateInternalV2ShouldBeSuccess(String sinceDate, String page,
                                                                     String pageSize) {
        log.info("findOrganisationBySinceDateInternalV2ShouldBeSuccess :: STARTED");
        Map<String, Object> response = professionalApiClient.retrieveOrganisationDetailsBySinceDateV2(
                sinceDate, page, pageSize);
        assertThat(response).isNotNull();
        verifyOrganisationDetailsBySinceDateV2(response, pageSize, sinceDate);
        log.info("findOrganisationBySinceDateInternalV2ShouldBeSuccess :: END");
    }

    private void verifyOrganisationDetailsBySinceDateV2(Map<String, Object> response, String pageSize,
                                                        String sinceDate) {
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


    @Test
    void updateOrganisationSraIdShouldReturnSuccess() {
        log.info("updateOrganisationNameShouldReturnSuccess :: STARTED");
        //create organisation
        String orgId1 = createActiveOrganisation();
        String orgId2 = createActiveOrganisation();
        String sraId1 = randomAlphabetic(7);
        String sraId2 = randomAlphabetic(7);
        //create request to update organisation
        OrganisationSraUpdateRequest organisationSraUpdateRequest = createOrganisationSraUpdateRequest(
            sraId1,sraId2,orgId1,orgId2);
        //call endpoint to update SraId as 'updatedSraId'
        Response orgUpdatedSraResponse = professionalApiClient.updatesOrganisationSra(
            organisationSraUpdateRequest, hmctsAdmin, OK);
        assertNotNull(orgUpdatedSraResponse);
        assertThat(orgUpdatedSraResponse.body().as(Map.class).get("status")).isEqualTo("success");
        assertThat(orgUpdatedSraResponse.body().as(Map.class).get("message")).isEqualTo(
            "All SraIds updated successfully");
        //retrieve 1st saved organisation by id
        verifyRetrievedOrg(orgId2,sraId1);
        //retrieve 2st saved organisation by id
        verifyRetrievedOrg(orgId2,sraId2);
        //Delete organisation
        deleteCreatedTestOrganisations(orgId1,orgId2);
        log.info("updateOrganisationSraIdShouldReturnSuccess :: END");

    }


    @Test
    void updateOrganisationSraIdShouldReturnFailureIfNoOrgId() {
        log.info("updateOrganisationSraIdShouldReturnFailureIfNoOrgId :: STARTED");
        String sraId1 = randomAlphabetic(7);
        String sraId2 = randomAlphabetic(7);
        //create request to update organisation
        OrganisationSraUpdateRequest organisationSraUpdateRequest = createOrganisationSraUpdateRequest(
            sraId1,sraId2,null,null);
        //call endpoint to update empty SraId
        Response orgUpdatedSraIdResponse = professionalApiClient.updatesOrganisationSra(
            organisationSraUpdateRequest,hmctsAdmin, MULTI_STATUS);
        assertNotNull(orgUpdatedSraIdResponse);
        assertThat(orgUpdatedSraIdResponse.body().as(Map.class).get("status")).isEqualTo("failure");
        ArrayList sraIds = (ArrayList) orgUpdatedSraIdResponse.body().as(Map.class).get("sarIds");
        LinkedHashMap response1 = (LinkedHashMap) sraIds.get(0);
        LinkedHashMap response2 = (LinkedHashMap) sraIds.get(1);

        assertThat(response1.get("status")).isEqualTo("failure");
        assertThat(response2.get("status")).isEqualTo("failure");

        assertThat(response1.get("statusCode")).isEqualTo(400);
        assertThat(response2.get("statusCode")).isEqualTo(400);

        assertThat(response1.get("message")).isEqualTo("Organisation id is missing");
        assertThat(response2.get("message")).isEqualTo("Organisation id is missing");
        log.info("updateOrganisationSraIdShouldReturnFailureIfNoOrgId :: END");
    }


    @Test
    void updateOrganisationSraIdShouldReturnFailureIfNoSraId() {
        log.info("updateOrganisationSraIdShouldReturnFailureIfNoSraId :: STARTED");
        //create organisation
        String orgId1 = createActiveOrganisation();
        String orgId2 = createActiveOrganisation();
        //create request to update organisation
        OrganisationSraUpdateRequest organisationSraUpdateRequest = createOrganisationSraUpdateRequest(
            null,null,orgId1,orgId2);
        //call endpoint to update empty Sra
        Response orgUpdatedSraResponse = professionalApiClient.updatesOrganisationSra(
            organisationSraUpdateRequest,hmctsAdmin, MULTI_STATUS);
        assertNotNull(orgUpdatedSraResponse);
        assertThat(orgUpdatedSraResponse.body().as(Map.class).get("status")).isEqualTo("failure");
        ArrayList sraIds = (ArrayList) orgUpdatedSraResponse.body().as(Map.class).get("sarIds");
        LinkedHashMap response1 = (LinkedHashMap) sraIds.get(0);
        LinkedHashMap response2 = (LinkedHashMap) sraIds.get(1);

        assertThat(response1.get("organisationId")).isEqualTo(orgId1);
        assertThat(response2.get("organisationId")).isEqualTo(orgId2);

        assertThat(response1.get("status")).isEqualTo("failure");
        assertThat(response2.get("status")).isEqualTo("failure");

        assertThat(response1.get("statusCode")).isEqualTo(400);
        assertThat(response2.get("statusCode")).isEqualTo(400);

        assertThat(response1.get("message")).isEqualTo("Organisation SraId is missing");
        assertThat(response2.get("message")).isEqualTo("Organisation SraId is missing");

        //Delete organisation
        deleteCreatedTestOrganisations(orgId1,orgId2);
        log.info("updateOrganisationSraIdShouldReturnFailureIfNoSraId :: END");
    }

    @Test
    void updateOrganisationSraShouldReturnPartialSuccessIfNoSraId() {
        log.info("updateOrganisationSraShouldReturnPartialSuccessIfNoSraId :: STARTED");
        //create organisation
        String orgId1 = createActiveOrganisation();
        String orgId2 = createActiveOrganisation();
        String sraId1 = randomAlphabetic(7);
        //create request to update organisation
        OrganisationSraUpdateRequest organisationSraUpdateRequest = createOrganisationSraUpdateRequest(
            null,sraId1,orgId1,orgId2);
        //call endpoint to update empty Sra
        Response orgUpdatedSraResponse = professionalApiClient.updatesOrganisationSra(
            organisationSraUpdateRequest,hmctsAdmin, MULTI_STATUS);
        assertNotNull(orgUpdatedSraResponse);
        assertThat(orgUpdatedSraResponse.body().as(Map.class).get("status")).isEqualTo("partial_success");
        ArrayList sraIds = (ArrayList) orgUpdatedSraResponse.body().as(Map.class).get("sraIds");
        LinkedHashMap response1 = (LinkedHashMap) sraIds.get(0);
        LinkedHashMap response2 = (LinkedHashMap) sraIds.get(1);

        assertThat(response1.get("organisationId")).isEqualTo(orgId1);
        assertThat(response2.get("organisationId")).isEqualTo(orgId2);

        assertThat(response1.get("status")).isEqualTo("failure");
        assertThat(response2.get("status")).isEqualTo("success");

        assertThat(response1.get("statusCode")).isEqualTo(400);
        assertThat(response2.get("statusCode")).isEqualTo(200);

        assertThat(response1.get("message")).isEqualTo("Organisation sraId is missing");
        assertThat(response2.get("message")).isEqualTo("SraId updated successfully");

        //retrieve 2st saved organisation by id
        verifyRetrievedOrg(orgId2,sraId1);
        //Delete organisation
        deleteCreatedTestOrganisations(orgId1,orgId2);
        log.info("updateOrganisationSraShouldReturnPartialSuccessIfNoSraId :: END");
    }


    private String createActiveOrganisation() {
        Map<String, Object> response = professionalApiClient.createOrganisation();
        String organisationIdentifier = (String) response.get("organisationIdentifier");
        assertThat(organisationIdentifier).isNotEmpty();
        OrganisationCreationRequest organisationCreationRequest = createOrganisationRequest().status("ACTIVE").build();
        professionalApiClient.updateOrganisation(organisationCreationRequest, hmctsAdmin, organisationIdentifier,OK);
        return organisationIdentifier;
    }

    public void deleteCreatedTestOrganisations(String orgId1, String orgId2) {
        professionalApiClient.deleteOrganisation(orgId1, hmctsAdmin, NO_CONTENT);;
        professionalApiClient.deleteOrganisation(orgId2, hmctsAdmin, NO_CONTENT);
    }

    public OrganisationSraUpdateRequest createOrganisationSraUpdateRequest(String sraId1,String sraId2,String orgId1,
                                                                             String orgId2) {
        //create request to update organisation
        OrganisationSraUpdateRequest organisationSraUpdateRequest = new OrganisationSraUpdateRequest();
        List<OrganisationSraUpdateRequest.OrganisationSraUpdateData> organisationSraUpdateDataList
            = new ArrayList<>();
        OrganisationSraUpdateRequest.OrganisationSraUpdateData organisationSraUpdateData1 =
            new OrganisationSraUpdateRequest.OrganisationSraUpdateData(sraId1,orgId1);
        OrganisationSraUpdateRequest.OrganisationSraUpdateData organisationSraUpdateData2 =
            new OrganisationSraUpdateRequest.OrganisationSraUpdateData(sraId2,orgId2);
        organisationSraUpdateDataList.add(organisationSraUpdateData1);
        organisationSraUpdateDataList.add(organisationSraUpdateData2);
        organisationSraUpdateRequest.setOrganisationSraUpdateDataList(organisationSraUpdateDataList);
        return organisationSraUpdateRequest;
    }

    public void verifyRetrievedOrg(String orgId,String sraId) {

        var orgResponse = professionalApiClient.retrieveOrganisationDetails(orgId, hmctsAdmin, OK);
        assertThat(orgResponse).isNotNull();
        List organisationAttributes2 = (List)orgResponse.get("orgAttributes");
        assertThat(organisationAttributes2).isNotNull();
        LinkedHashMap<String, Object> attr2 = (LinkedHashMap)organisationAttributes2.get(0);
        assertThat(attr2).isNotNull();
        assertThat(attr2.get("key")).isEqualTo("regulators-0");
        assertThat(attr2.get("value").toString()).isEqualTo(
            "{\"regulatorType\":\"Solicitor Regulation Authority "
                + "(SRA)\",\"organisationRegistrationNumber\":\"" + sraId + "\"}");

        LocalDateTime updatedOrgAttributeDate =  LocalDateTime.parse(orgResponse.get("lastUpdated").toString());
        assertThat(updatedOrgAttributeDate.toLocalDate()).isEqualTo(LocalDate.now());
        final Object sraIdSaved = orgResponse.get("sraId");
        assertThat(sraIdSaved).isNotNull().isEqualTo(sraId);
        LocalDateTime updatedDate =  LocalDateTime.parse(orgResponse.get("lastUpdated").toString());
        assertThat(updatedDate.toLocalDate()).isEqualTo(LocalDate.now());
    }


}