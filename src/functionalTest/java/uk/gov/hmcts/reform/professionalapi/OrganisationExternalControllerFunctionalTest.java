package uk.gov.hmcts.reform.professionalapi;
import io.restassured.specification.RequestSpecification;
import lombok.extern.slf4j.Slf4j;
import net.thucydides.core.annotations.WithTag;
import net.thucydides.core.annotations.WithTags;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.professionalapi.controller.request.ContactInformationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.NewUserCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.PbaRequest;
import uk.gov.hmcts.reform.professionalapi.controller.response.OrganisationMinimalInfoResponse;
import uk.gov.hmcts.reform.professionalapi.domain.UserProfileUpdatedData;
import uk.gov.hmcts.reform.professionalapi.util.FeatureToggleConditionExtension;
import uk.gov.hmcts.reform.professionalapi.util.ToggleEnable;
import uk.gov.hmcts.reform.professionalapi.util.serenity5.SerenityTest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
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
import static uk.gov.hmcts.reform.professionalapi.controller.request.UserCreationRequest.aUserCreationRequest;

@SerenityTest
@SpringBootTest
@WithTags({@WithTag("testType:Functional")})
@Slf4j
@SuppressWarnings("unchecked")
public class OrganisationExternalControllerFunctionalTest extends AuthorizationFunctionalTest {
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
    List<ContactInformationCreationRequest> createContactInformationCreationRequests;
    String firstName = "firstName";
    String lastName = "lastName";

    @Test
    @DisplayName("Add Contact informations to organisations  Test Scenarios")
    @ToggleEnable(mapKey = "OrganisationExternalController.addContactInformationsToOrganisation", withFeature = false)
    @ExtendWith(FeatureToggleConditionExtension.class)
    void testAddContactsInformationsToOrganisationScenarios() {
        setUpOrgTestData();
        setUpUserBearerTokens(List.of(puiUserManager, puiCaseManager, puiOrgManager, puiFinanceManager, caseworker));
        inviteUserScenarios();
        addContactInformationsToOrganisationScenario();

        suspendUserScenarios();
    }

    public void setUpOrgTestData() {
        if (isEmpty(extActiveOrgId)) {
            log.info("Setting up organization...");
            superUserEmail = generateRandomEmail();
            organisationCreationRequest = createOrganisationRequest()
                    .superUser(aUserCreationRequest()
                            .firstName(firstName)
                            .lastName(lastName)
                            .email(superUserEmail)
                            .build())
                    .paymentAccount(Set.of("PBA".concat(RandomStringUtils.randomAlphanumeric(7)),
                            "PBA".concat(RandomStringUtils.randomAlphanumeric(7)),
                            "PBA".concat(RandomStringUtils.randomAlphanumeric(7))))
                    .build();

            organisationCreationRequest.setStatus("ACTIVE");
            extActiveOrgId = createAndctivateOrganisationWithGivenRequest(organisationCreationRequest, hmctsAdmin);

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
        inviteUserByPuiOrgManagerShouldBeSuccess();
        //inviteUserByPuiUserManagerShouldBeSuccess();
        // below test receives 504 from SIDAM intermittently, needs investigation:
        // inviteUserBySuperUserShouldBeSuccess();
    }



    public void suspendUserScenarios() {
        suspendUserByPuiOrgManagerShouldBeSuccess();
        //suspendUserByPumShouldBeSuccess();
    }
    public void addContactInformationsToOrganisationScenario() {
        addContactInformationsToOrganisationShouldBeSuccess();
    }


    public void addContactInformationsToOrganisationShouldBeSuccess() {
        log.info("addContactInformationsToOrganisationShouldBeSuccess :: STARTED");

        createContactInformationCreationRequests = createContactInformationCreationRequests();
        Map<String, Object> result = professionalApiClient
                .addContactInformationsToOrganisation(createContactInformationCreationRequests,
                        pomBearerToken,extActiveOrgId);

        assertThat(result.get("contactInformationsResponse")).isNotNull();
        log.info("addContactInformationsToOrganisationShouldBeSuccess :: END");
    }



    public void inviteUserByPuiUserManagerShouldBeSuccess() {
        log.info("inviteUserByPuiUserManagerShouldBeSuccess :: STARTED");
        Map<String, Object> newUserResponse = professionalApiClient
                .addNewUserToAnOrganisationExternal(createUserRequest(asList(puiCaseManager)),
                        professionalApiClient.getMultipleAuthHeaders(pumBearerToken), CREATED);
        assertThat(newUserResponse.get("userIdentifier")).isNotNull();
        log.info("inviteUserByPuiUserManagerShouldBeSuccess :: END");
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

}
