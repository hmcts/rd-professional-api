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
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationOtherOrgsCreationRequest;
import uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus;
import uk.gov.hmcts.reform.professionalapi.util.FeatureToggleConditionExtension;
import uk.gov.hmcts.reform.professionalapi.util.ToggleEnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;
import static uk.gov.hmcts.reform.professionalapi.client.ProfessionalApiClient.createOrganisationRequestForV2;

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
        deleteOtherOrganisationScenarios();

    }

    public void setUpTestData() {
        superUserEmail = generateRandomEmail();
        invitedUserEmail = generateRandomEmail();
        organisationOtherOrgsCreationRequest = createOrganisationRequestForV2();
        organisationOtherOrgsCreationRequest.getSuperUser().setEmail(superUserEmail);

        intActiveOrgId = createAndUpdateOrganisationToActiveForV2(hmctsAdmin, organisationOtherOrgsCreationRequest);

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

        assertThat(organisations).isNotNull().hasSize(1);
        log.info("findOrganisationsWithPaginationShouldReturnSuccess :: END");
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
}
