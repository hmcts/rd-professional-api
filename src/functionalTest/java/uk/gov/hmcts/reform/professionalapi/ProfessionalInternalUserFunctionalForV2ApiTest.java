package uk.gov.hmcts.reform.professionalapi;

import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import net.serenitybdd.annotations.WithTag;
import net.serenitybdd.annotations.WithTags;
import net.serenitybdd.junit5.SerenityJUnit5Extension;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.professionalapi.controller.request.NewUserCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationByProfileIdsRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationOtherOrgsCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.UsersInOrganisationsByOrganisationIdentifiersRequest;
import uk.gov.hmcts.reform.professionalapi.controller.response.UsersInOrganisationsByOrganisationIdentifiersResponse;
import uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus;
import uk.gov.hmcts.reform.professionalapi.util.CustomSerenityJUnit5Extension;
import uk.gov.hmcts.reform.professionalapi.util.DateUtils;
import uk.gov.hmcts.reform.professionalapi.util.ToggleEnable;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;
import static uk.gov.hmcts.reform.professionalapi.client.ProfessionalApiClient.createOrganisationRequestForV2;
import static uk.gov.hmcts.reform.professionalapi.util.DateUtils.convertStringToLocalDate;
import static uk.gov.hmcts.reform.professionalapi.util.DateUtils.generateRandomDate;
import static uk.gov.hmcts.reform.professionalapi.util.OrganisationProfileIdConstants.SOLICITOR_PROFILE;

@ExtendWith({CustomSerenityJUnit5Extension.class, SerenityJUnit5Extension.class, SpringExtension.class})
@SpringBootTest
@WithTags({@WithTag("testType:Functional")})
@Slf4j
@SuppressWarnings("unchecked")
class ProfessionalInternalUserFunctionalForV2ApiTest extends AuthorizationFunctionalTest {

    private static final List<String> organisationIdentifiers = new ArrayList<>();
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

    @AfterAll
    public static void cleanUp() {
        organisationIdentifiers.forEach(id ->
                professionalApiClient.deleteOrganisationByInternalUser(id));

    }

    @Test
    @DisplayName("PRD Internal Test Scenarios For V2 API")
    @ToggleEnable(mapKey = "OrganisationInternalControllerV2.createOrganisation", withFeature = true)
    void testInternalUserScenario() {
        setUpTestData();
        createOrganisationScenario();
        findOrganisationScenarios();
        retrieveOrganisationPbaScenarios();
        // DTSRD-6485 - Disabling flaky test temporarily until fix found.
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
        organisationIdentifiers.add(intActiveOrgId);
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
        // DTSRD-6485 - Disabling flaky test temporarily until fix found.
        findActiveAndPendingOrganisationsByInternalUserShouldBeSuccess();
        findActiveOrganisationsByInternalUserShouldBeSuccess();
        findPendingOrganisationsByInternalUserShouldBeSuccess();
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

        Map<String, Object> organisation = professionalApiClient.createOrganisation();
        String organisationIdentifier = (String) organisation.get("organisationIdentifier");
        organisationIdentifiers.add(organisationIdentifier);

        sinceDateTime = generateRandomDate(null, "30");
        log.info("Since Date is set to : {} ", sinceDateTime);
        Map<String, Object> response = professionalApiClient
                .retrieveAllV2OrganisationsBySinceDateAndStatus(sinceDateTime, "ACTIVE,PENDING");
        assertThat(response.size()).isPositive();
        verifyOrganisationDetailsBySinceDateV2(response, null, sinceDateTime);
        log.info("findActiveAndPendingOrganisationsByInternalUserShouldBeSuccess :: END");
    }

    public void findActiveOrganisationsByInternalUserShouldBeSuccess() {
        log.info("findActiveOrganisationsByInternalUserShouldBeSuccess :: STARTED");
        sinceDateTime = generateRandomDate(null, "30");
        log.info("Since Date is set to : {} ", sinceDateTime);
        Map<String, Object> response = professionalApiClient
                .retrieveAllV2OrganisationsBySinceDateAndStatus(sinceDateTime, OrganisationStatus.ACTIVE.name());
        assertThat(response.size()).isPositive();
        verifyOrganisationDetailsBySinceDateV2(response, null, sinceDateTime);
        log.info("findActiveOrganisationsByInternalUserShouldBeSuccess :: END");
    }

    public void findPendingOrganisationsByInternalUserShouldBeSuccess() {
        log.info("findPendingOrganisationsByInternalUserShouldBeSuccess :: STARTED");
        Map<String, Object> organisation = professionalApiClient.createOrganisation();
        String organisationIdentifier = (String) organisation.get("organisationIdentifier");
        organisationIdentifiers.add(organisationIdentifier);

        sinceDateTime = generateRandomDate(null, "30");
        log.info("Since Date is set to : {} ", sinceDateTime);
        Map<String, Object> response = professionalApiClient
                .retrieveAllV2OrganisationsBySinceDateAndStatus(sinceDateTime, OrganisationStatus.PENDING.name());
        assertThat(response.get("organisations")).isNotNull();
        assertThat(response.size()).isPositive();
        verifyOrganisationDetailsBySinceDateV2(response, null, sinceDateTime);
        log.info("findPendingOrganisationsByInternalUserShouldBeSuccess :: END");
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
    void findOrganisationsWithPaginationShouldReturnSuccess() {
        log.info("findOrganisationsWithPaginationShouldReturnSuccess :: STARTED");
        Map<String, Object> organisation = professionalApiClient.createOrganisationV2();
        String organisationIdentifier = (String) organisation.get("organisationIdentifier");
        organisationIdentifiers.add(organisationIdentifier);

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
        request.getOrganisationProfileIds().add(SOLICITOR_PROFILE);
        Map<String, Object> response = professionalApiClient.retrieveOrganisationsByProfileIds(request,
                pageSize, null);

        verifyOrganisationsByProfileIdResponse(response, SOLICITOR_PROFILE, pageSize);
        log.info("findOrganisationWithSolicitorProfileAndPageSizeShouldBeSuccess :: END");
    }

    public void findOrganisationWithSolicitorProfilePageSizeAndOrSearchAfter(String pageSize, String searchAfter) {
        log.info("findOrganisationWithSolicitorProfilePageSizeAndOrSearchAfter :: STARTED");

        OrganisationByProfileIdsRequest request = new OrganisationByProfileIdsRequest();
        request.getOrganisationProfileIds().add("UNKNOWN");
        request.getOrganisationProfileIds().add(SOLICITOR_PROFILE);
        Map<String, Object> response = professionalApiClient.retrieveOrganisationsByProfileIds(request,
                pageSize, searchAfter);
        verifyOrganisationsByProfileIdResponse(response, SOLICITOR_PROFILE, pageSize);
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
            assertThat(organisationProfileIdList.contains(expectedProfileId));
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
        organisationIdentifiers.add(pendingOrgIdentifier);

        assertThat(pendingOrgIdentifier).isNotEmpty();
        professionalApiClient.deleteOrganisation(pendingOrgIdentifier, NO_CONTENT);
        professionalApiClient.retrieveOrganisationDetailsForV2(pendingOrgIdentifier, hmctsAdmin, NOT_FOUND);
        log.info("modifyUserStatusToSuspendedShouldReturnSuccess :: END");
    }

    public void deleteActiveOtherOrganisationShouldReturnSuccess() {
        log.info("deleteActiveOtherOrganisationShouldReturnSuccess :: STARTED");
        String orgIdentifierResponse =
                createAndUpdateOrganisationToActiveForV2(hmctsAdmin, createOrganisationRequestForV2());
        professionalApiClient.deleteOrganisation(orgIdentifierResponse, NO_CONTENT);
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
                    .hasSizeBetween(1, Integer.parseInt(pageSize));
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
            assertThat(responseLocalDateTime).isAfterOrEqualTo(sinceLocalDateTime);

            List<String> organisationProfileIds = (ArrayList<String>) org.get("organisationProfileIds");

            if (organisationProfileIds != null && !organisationProfileIds.isEmpty()) {
                assertThat(organisationProfileIds)
                        .isNotEmpty()
                        .hasSizeGreaterThan(0)
                        .contains(SOLICITOR_PROFILE);
            }
        });
    }

}
