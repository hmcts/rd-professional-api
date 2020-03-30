package uk.gov.hmcts.reform.professionalapi;

import static org.apache.commons.lang.RandomStringUtils.randomAlphabetic;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Map;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.professionalapi.controller.request.NewUserCreationRequest;
import uk.gov.hmcts.reform.professionalapi.idam.IdamOpenIdClient;

@RunWith(SpringIntegrationSerenityRunner.class)
@ActiveProfiles("functional")
public class ReInviteUserTest extends AuthorizationFunctionalTest {

    String orgIdentifierResponse = null;
    private IdamOpenIdClient idamOpenIdClient = null;
    public static final String RANDOM_EMAIL = "RANDOM_EMAIL";

    @Value(("${resendInviteEnabled}"))
    protected boolean resendInviteEnabled;

    @Value("${resendInterval}")
    protected String resendInterval;

    @Before
    public void createAndUpdateOrganisation() {
        if (resendInviteEnabled) {
            orgIdentifierResponse = createAndUpdateOrganisationToActive(hmctsAdmin);
        }
    }

    //AC3: resend invite to a given user who does not exist
    @Test
    public void should_return_404_when_reinvited_user_not_exists() throws Exception {
        if (resendInviteEnabled) {
            NewUserCreationRequest newUserCreationRequest = professionalApiClient.createReInviteUserRequest(RANDOM_EMAIL);
            newUserCreationRequest.setJurisdictions(new ArrayList<>());
            Map<String, Object> newUserResponse = professionalApiClient.addNewUserToAnOrganisation(orgIdentifierResponse, hmctsAdmin, newUserCreationRequest, HttpStatus.BAD_REQUEST);
            assertThat((String) newUserResponse.get("errorDescription")).contains("User does not exist");
        }
    }

    //AC4: resend invite to a given user who is not in the 'Pending' state
    @Test
    public void should_return_400_when_reinvited_user_is_active() throws Exception {

        if (resendInviteEnabled) {
            // create active user in UP
            String email = idamOpenIdClient.createUser("pui-user-manager");
            NewUserCreationRequest newUserCreationRequest = professionalApiClient.createNewUserRequest(email);
            Map<String, Object> newUserResponse = professionalApiClient.addNewUserToAnOrganisation(orgIdentifierResponse, hmctsAdmin, newUserCreationRequest, HttpStatus.CREATED);
            assertThat(newUserResponse).isNotNull();

            //re inviting active user should return 400
            NewUserCreationRequest reInviteUserCreationRequest = professionalApiClient.createReInviteUserRequest(email);
            Map<String, Object> reinviteUserResponse = professionalApiClient.addNewUserToAnOrganisation(orgIdentifierResponse, hmctsAdmin, reInviteUserCreationRequest, HttpStatus.BAD_REQUEST);
            assertThat((String) reinviteUserResponse.get("errorDescription")).contains("User is not in PENDING state");
        }
    }

    //AC5: resend invite to a given user who was last invited less than one hour before
    @Test
    public void should_return_429_when_user_reinvited_within_one_hour() throws Exception {

        if (resendInviteEnabled) {
            // create pending user in UP
            NewUserCreationRequest newUserCreationRequest = professionalApiClient.createNewUserRequest();
            Map<String, Object> newUserResponse = professionalApiClient.addNewUserToAnOrganisation(orgIdentifierResponse, hmctsAdmin, newUserCreationRequest, HttpStatus.CREATED);
            assertThat(newUserResponse).isNotNull();

            //re inviting active user should return 400
            NewUserCreationRequest reInviteUserCreationRequest = professionalApiClient.createReInviteUserRequest(newUserCreationRequest.getEmail());
            Map<String, Object> reinviteUserResponse = professionalApiClient.addNewUserToAnOrganisation(orgIdentifierResponse, hmctsAdmin, reInviteUserCreationRequest, HttpStatus.TOO_MANY_REQUESTS);
            assertThat((String) reinviteUserResponse.get("errorDescription")).contains(String.format("The request was last made less than %s minutes ago. Please try after some time"), resendInterval);
        }
    }

    //AC7:  professional(external) user resend invite user who is not in the 'Pending' state
    @Test
    public void should_return_400_when_user_reinvited_by_extrenal_user_is_active() throws Exception {

        if (resendInviteEnabled) {
            // create active PUM sidam user and invite
            String pumEmail = idamOpenIdClient.createUser("pui-user-manager");
            NewUserCreationRequest newUserCreationRequest = professionalApiClient.createNewUserRequest(pumEmail);
            professionalApiClient.addNewUserToAnOrganisation(orgIdentifierResponse, hmctsAdmin, newUserCreationRequest, HttpStatus.CREATED);


            // create active caseworker sidam user and invite
            String email = idamOpenIdClient.createUser("caseworker");
            newUserCreationRequest = professionalApiClient.createNewUserRequest(email);
            professionalApiClient.addNewUserToAnOrganisation(orgIdentifierResponse, hmctsAdmin, newUserCreationRequest, HttpStatus.CREATED);

            // get PUM bearer token and reinvite
            String pumBearerToken = idamOpenIdClient.getOpenIdToken(pumEmail);
            Map<String, Object> reinviteUserResponse = professionalApiClient.addNewUserToAnOrganisationExternal(newUserCreationRequest, professionalApiClient.getMultipleAuthHeaders(pumBearerToken), HttpStatus.BAD_REQUEST);
            assertThat((String) reinviteUserResponse.get("errorDescription")).contains("User is not in PENDING state");
        }
    }

    //AC8: professional(external) resend invite to a given user who was last invited less than one hour before
    @Test
    public void should_return_400_when_user_reinvited_by_extrenal_user_is_invited_within_one_hour() throws Exception {

        if (resendInviteEnabled) {
            // create active PUM sidam user and invite
            String pumEmail = idamOpenIdClient.createUser("pui-user-manager");
            NewUserCreationRequest newUserCreationRequest = professionalApiClient.createNewUserRequest(pumEmail);
            professionalApiClient.addNewUserToAnOrganisation(orgIdentifierResponse, hmctsAdmin, newUserCreationRequest, HttpStatus.CREATED);


            // create active caseworker sidam user and invite
            String email = randomAlphabetic(10) + "@hotmail.com".toLowerCase();
            newUserCreationRequest = professionalApiClient.createNewUserRequest(email);
            professionalApiClient.addNewUserToAnOrganisation(orgIdentifierResponse, hmctsAdmin, newUserCreationRequest, HttpStatus.CREATED);

            // get PUM bearer token and reinvite
            String pumBearerToken = idamOpenIdClient.getOpenIdToken(pumEmail);
            Map<String, Object> reinviteUserResponse = professionalApiClient.addNewUserToAnOrganisationExternal(newUserCreationRequest, professionalApiClient.getMultipleAuthHeaders(pumBearerToken), HttpStatus.BAD_REQUEST);
            assertThat((String) reinviteUserResponse.get("errorDescription")).contains(String.format("The request was last made less than %s minutes. Please try after some time"), resendInterval);
        }
    }

    // This is to test if EXUI sends resendInvite = false, then normal invite user should work
    @Test
    public void add_new_user_to_organisation_with_resend_Invite_false() {

        NewUserCreationRequest newUserCreationRequest = professionalApiClient.createNewUserRequest();
        newUserCreationRequest.setResendInvite(false);
        Map<String, Object> newUserResponse = professionalApiClient.addNewUserToAnOrganisation(orgIdentifierResponse, hmctsAdmin,  newUserCreationRequest, HttpStatus.CREATED);
        assertThat(newUserResponse).isNotNull();
        assertThat(newUserResponse.get("userIdentifier")).isNotNull();
    }
}
