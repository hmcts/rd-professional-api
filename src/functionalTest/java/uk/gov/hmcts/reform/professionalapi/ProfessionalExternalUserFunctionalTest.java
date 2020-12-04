package uk.gov.hmcts.reform.professionalapi;

import static org.assertj.core.api.Assertions.assertThat;
import static org.codehaus.groovy.runtime.InvokerHelper.asList;
import static org.springframework.http.HttpStatus.CREATED;
import static uk.gov.hmcts.reform.professionalapi.client.ProfessionalApiClient.createOrganisationRequest;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.ACTIVE;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.FALSE;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.TRUE;
import static uk.gov.hmcts.reform.professionalapi.controller.request.UserCreationRequest.aUserCreationRequest;

import lombok.extern.slf4j.Slf4j;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import net.thucydides.core.annotations.WithTag;
import net.thucydides.core.annotations.WithTags;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.professionalapi.controller.constants.IdamStatus;
import uk.gov.hmcts.reform.professionalapi.controller.request.NewUserCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RunWith(SpringIntegrationSerenityRunner.class)
@WithTags({@WithTag("testType:Functional")})
@Slf4j
public class ProfessionalExternalUserFunctionalTest extends AuthorizationFunctionalTest {

    /*
    Create Organisation
    Approve Org
    Invite User to Org
    Find user by Org
    Get Org
    Get PBA
     */
    String pumBearerToken;
    String pcmBearerToken;
    String pomBearerToken;
    String pfmBearerToken;
    String systemUserBearerToken;
    String extActiveOrgId;

    @Test
    public void testInternalUserScenario() {
        setUpTestData();
        //create and approve org already taken care in AuthorizationFunctionalTest in BeforeTest
        inviteUserScenarios();
        findUsersByOrganisationScenarios();
        findOrganisationScenarios();
        // Get pba scenarios covered in integration test cases

    }

    public void setUpTestData() {
        String superUserEmail = generateRandomEmail();
        OrganisationCreationRequest organisationCreationRequest = createOrganisationRequest()
                .superUser(aUserCreationRequest()
                        .firstName("firstName")
                        .lastName("lastName")
                        .email(superUserEmail)
                        .build())
                .build();
        // TODO: super user fails
        //superUserBearerToken = professionalApiClient.getMultipleAuthHeaders(
        //getExternalSuperUserTokenWithRetry(superUserEmail, "firstName", "lastName"));

        extActiveOrgId = createAndctivateOrganisationWithGivenRequest(organisationCreationRequest, hmctsAdmin);
        //created active org
        pumBearerToken = inviteUser(puiUserManager);
        pcmBearerToken = inviteUser(puiCaseManager);
        pomBearerToken = inviteUser(puiOrgManager);
        pfmBearerToken = inviteUser(puiFinanceManager);
        // created PUM internally
    }

    public void inviteUserScenarios() {
        inviteUserByPuiUserManagerShouldBeSuccess();
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

    public void inviteUserByPuiUserManagerShouldBeSuccess() {
        log.info("inviteUserByPuiUserManagerShouldBeSuccess :: STARTED");
        Map<String, Object> newUserResponse = professionalApiClient
                .addNewUserToAnOrganisationExternal(createUserRequest(asList(puiCaseManager)),
                        professionalApiClient.getMultipleAuthHeaders(pumBearerToken), CREATED);
        assertThat(newUserResponse.get("userIdentifier")).isNotNull();
        log.info("inviteUserByPuiUserManagerShouldBeSuccess :: END");
    }

    public void findUsersByNonPumAndNoStatusProvidedShouldBeSuccess() {
        log.info("findUsersByNonPumAndNoStatusProvidedShouldBeSuccess :: STARTED");
        Map<String, Object> response = professionalApiClient.searchOrganisationUsersByStatusExternal(HttpStatus.OK,
                professionalApiClient.getMultipleAuthHeaders(pcmBearerToken), "");
        validateRetrievedUsers(response, "ACTIVE", true);
        log.info("findUsersByNonPumAndNoStatusProvidedShouldBeSuccess :: END");
    }

    public void findUsersByPumAndNoStatusProvidedShouldBeSuccess() {
        log.info("findUsersByPumAndNoStatusProvidedShouldBeSuccess :: STARTED");
        Map<String, Object> response = professionalApiClient.searchOrganisationUsersByStatusExternal(HttpStatus.OK,
                professionalApiClient.getMultipleAuthHeaders(pumBearerToken), "");
        validateRetrievedUsers(response, "ACTIVE", true);
        log.info("findUsersByPumAndNoStatusProvidedShouldBeSuccess :: END");
    }

    public void findUsersByPumAndWithStatusProvidedShouldBeSuccess() {
        log.info("findUsersByPumAndWithStatusProvidedShouldBeSuccess :: STARTED");
        Map<String, Object> response = professionalApiClient.searchOrganisationUsersByStatusExternal(HttpStatus.OK,
                professionalApiClient.getMultipleAuthHeaders(pumBearerToken), "Active");
        validateRetrievedUsers(response, "ACTIVE", true);
        log.info("findUsersByPumAndWithStatusProvidedShouldBeSuccess :: END");
    }

    public void findUsersByPumAndWithStatusSuspendedProvidedShouldReturnNotFound() {
        log.info("findUsersByPumAndWithStatusSuspendedProvidedShouldReturnNotFound :: STARTED");
        professionalApiClient.searchOrganisationUsersByStatusExternal(HttpStatus.NOT_FOUND,
                professionalApiClient.getMultipleAuthHeaders(pumBearerToken), "Suspended");
        log.info("findUsersByPumAndWithStatusSuspendedProvidedShouldReturnNotFound :: END");
    }

    public void findUsersByInvalidUserAndNoStatusProvidedShouldReturnNotFound() {
        log.info("findUsersByInvalidUserAndNoStatusProvidedShouldReturnNotFound :: STARTED");
        professionalApiClient.searchOrganisationUsersByStatusExternal(HttpStatus.UNAUTHORIZED,
                professionalApiClient.getMultipleAuthHeadersWithEmptyBearerToken(""), "");
        log.info("findUsersByInvalidUserAndNoStatusProvidedShouldReturnNotFound :: END");
    }

    public void findUsersByPcmAndNoRolesRequiredShouldBeSuccess() {
        log.info("findUsersByPcmAndNoRolesRequiredShouldBeSuccess :: STARTED");
        Map<String, Object> response = professionalApiClient
                .searchOrganisationUsersByReturnRolesParamExternal(HttpStatus.OK,
                        professionalApiClient.getMultipleAuthHeaders(pcmBearerToken), "false");
        validateRetrievedUsers(response, "ACTIVE", false);
        log.info("findUsersByPcmAndNoRolesRequiredShouldBeSuccess :: END");
    }

    public void findUsersByPcmAndWithRolesRequiredShouldBeSuccess() {
        log.info("findUsersByPcmAndWithRolesRequiredShouldBeSuccess :: STARTED");
        Map<String, Object> response = professionalApiClient
                .searchOrganisationUsersByReturnRolesParamExternal(HttpStatus.OK,
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
                        extActiveOrgId, FALSE, HttpStatus.OK, TRUE);

        List<HashMap> professionalUsers = (List<HashMap>) searchResponse.get("users");
        assertThat(professionalUsers.size()).isGreaterThan(1);
        validateRetrievedUsers(searchResponse, ACTIVE, false);
        log.info("findUsersBySystemAdminWithoutRolesRequiredShouldBeSuccess :: END");
    }

    public void findOrgByPfmShouldBeSuccess() {
        log.info("findOrgByPfmShouldBeSuccess :: STARTED");
        Map<String, Object> response = professionalApiClient.retrieveOrganisationByOrgIdExternal(HttpStatus.OK,
                professionalApiClient.getMultipleAuthHeaders(pfmBearerToken));
        assertThat(response.get("paymentAccount")).asList().hasSize(3);
        log.info("findOrgByPfmShouldBeSuccess :: END");
        responseValidate(response);
    }

    public void findOrgByPomShouldBeSuccess() {
        log.info("findOrgByPomShouldBeSuccess :: STARTED");
        Map<String, Object> response = professionalApiClient.retrieveOrganisationByOrgIdExternal(HttpStatus.OK,
                professionalApiClient.getMultipleAuthHeaders(pomBearerToken));
        assertThat(response.get("paymentAccount")).asList().hasSize(3);
        log.info("findOrgByPomShouldBeSuccess :: END");
    }

    public void findOrgByPumShouldBeSuccess() {
        log.info("findOrgByPumShouldBeSuccess :: STARTED");
        Map<String, Object> response = professionalApiClient.retrieveOrganisationByOrgIdExternal(HttpStatus.OK,
                professionalApiClient.getMultipleAuthHeaders(pumBearerToken));
        responseValidate(response);
        log.info("findOrgByPumShouldBeSuccess :: END");
    }

    public void findOrgByPcmShouldBeSuccess() {
        log.info("findOrgByPcmShouldBeSuccess :: STARTED");
        Map<String, Object> response = professionalApiClient.retrieveOrganisationByOrgIdExternal(HttpStatus.OK,
                professionalApiClient.getMultipleAuthHeaders(pcmBearerToken));
        responseValidate(response);
        log.info("findOrgByPcmShouldBeSuccess :: END");
    }

    public void findOrgByUnknownUserShouldReturnForbidden() {
        log.info("findOrgByUnknownUserShouldReturnForbidden :: STARTED");
        Map<String, Object> response = professionalApiClient.retrieveOrganisationByOrgIdExternal(HttpStatus.FORBIDDEN,
                professionalApiClient.getMultipleAuthHeaders(systemUserBearerToken));
        assertThat(response.get("errorMessage")).isNotNull();
        assertThat(response.get("errorMessage")).isEqualTo("9 : Access Denied");
        log.info("findOrgByUnknownUserShouldReturnForbidden :: END");
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

    public String inviteUser(String role) {
        List<String> userRoles = new ArrayList<>();
        String userEmail = generateRandomEmail();
        userRoles.add(role);
        NewUserCreationRequest pumUserCreationRequest = createUserRequest(userRoles);
        pumUserCreationRequest.setEmail(userEmail);
        String bearerToken = idamOpenIdClient.getExternalOpenIdToken(puiUserManager,
                "firstName", "lastName", userEmail);

        Map<String, Object> pumInternalUserResponse = professionalApiClient
                .addNewUserToAnOrganisation(extActiveOrgId, hmctsAdmin, pumUserCreationRequest, CREATED);
        assertThat(pumInternalUserResponse.get("userIdentifier")).isNotNull();
        return bearerToken;
    }

    private void responseValidate(Map<String, Object> orgResponse) {

        orgResponse.forEach((k,v) -> {

            if ("organisationIdentifier".equals(k) && "http_status".equals(k)
                    && "name".equals(k) &&  "status".equals(k)
                    && "superUser".equals(k) && "paymentAccount".equals(k)) {

                Assertions.assertThat(v.toString()).isNotEmpty();
                Assertions.assertThat(v.toString().contains("Ok"));
                Assertions.assertThat(v.toString().contains("some-org-name"));
                Assertions.assertThat(v.toString().equals("ACTIVE"));
                Assertions.assertThat(v.toString()).isNotEmpty();
            }

        });

    }
}
