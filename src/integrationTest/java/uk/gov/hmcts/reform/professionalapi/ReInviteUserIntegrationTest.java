/*
package uk.gov.hmcts.reform.professionalapi;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static uk.gov.hmcts.reform.professionalapi.helper.OrganisationFixtures.someMinimalOrganisationRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.professionalapi.controller.request.NewUserCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.util.AuthorizationEnabledIntegrationTest;

@Ignore
@RunWith(SpringIntegrationSerenityRunner.class)
public class ReInviteUserIntegrationTest extends AuthorizationEnabledIntegrationTest {

    private NewUserCreationRequest userCreationRequest;
    List<String> userRoles;
    String organisationIdentifier = null;
    @Value(("${resendInviteEnabled}"))
    protected boolean resendInviteEnabled;

    @Before
    public void setUp() {
        if (resendInviteEnabled) {
            userRoles = new ArrayList<>();
            userRoles.add("pui-user-manager");
            userCreationRequest = inviteUserCreationRequest("some@somedomain.com", userRoles);
            organisationIdentifier = createAndActivateOrganisation();
        }
    }

    // AC1: resend invite to a given user
    @Test
    public void should_return_201_when_user_reinvited() {
        if (resendInviteEnabled) {
            //userProfileCreateUserWireMock(HttpStatus.CREATED);

            Map<String, Object> newUserResponse =
                    professionalReferenceDataClient.addUserToOrganisation(organisationIdentifier, userCreationRequest, hmctsAdmin);
            String userIdentifierResponse = (String) newUserResponse.get("userIdentifier");
            assertEquals(newUserResponse.get("userIdentifier"), userIdentifierResponse);

            NewUserCreationRequest reinviteRequest = reInviteUserCreationRequest(userCreationRequest.getEmail(), userRoles);
            Map<String, Object> reInviteUserResponse =
                    professionalReferenceDataClient.addUserToOrganisation(organisationIdentifier, reinviteRequest, hmctsAdmin);
            assertNotNull(reInviteUserResponse.get("userIdentifier"));
        }
    }

    // AC3: resend invite to a given user who does not exist
    @Test
    public void should_return_404_when_user_doesnt_exists() throws Exception {

        if (resendInviteEnabled) {
            NewUserCreationRequest reinviteRequest = reInviteUserCreationRequest(userCreationRequest.getEmail(), userRoles);
            Map<String, Object> reInviteUserResponse =
                    professionalReferenceDataClient.addUserToOrganisation(organisationIdentifier, reinviteRequest, hmctsAdmin);
            assertThat(reInviteUserResponse.get("http_status")).isEqualTo("404");
            assertThat((String) reInviteUserResponse.get("response_body")).contains("User does not exist");
        }
    }

    // AC4: resend invite to a given user who is not in the 'Pending' state
    @Test
    public void should_return_400_when_user_reinvited_is_not_pending() {

        if (resendInviteEnabled) {
            //userProfileCreateUserWireMock(HttpStatus.CREATED);

            Map<String, Object> newUserResponse =
                    professionalReferenceDataClient.addUserToOrganisation(organisationIdentifier, userCreationRequest, hmctsAdmin);
            String userIdentifierResponse = (String) newUserResponse.get("userIdentifier");
            assertEquals(newUserResponse.get("userIdentifier"), userIdentifierResponse);

            //reinviteUserMock(HttpStatus.BAD_REQUEST);
            NewUserCreationRequest reinviteRequest = reInviteUserCreationRequest(userCreationRequest.getEmail(), userRoles);
            Map<String, Object> reInviteUserResponse =
                    professionalReferenceDataClient.addUserToOrganisation(organisationIdentifier, reinviteRequest, hmctsAdmin);
            assertThat(reInviteUserResponse.get("http_status")).isEqualTo("400");
            assertThat((String) reInviteUserResponse.get("response_body")).contains("User is not in PENDING state");
        }

    }

    // AC8: resend invite to a given user who was last invited less than 1 hour before
    @Test
    public void should_return_429_when_user_reinvited_within_one_hour() {

        if (resendInviteEnabled) {
            //userProfileCreateUserWireMock(HttpStatus.CREATED);

            Map<String, Object> newUserResponse =
                    professionalReferenceDataClient.addUserToOrganisation(organisationIdentifier, userCreationRequest, hmctsAdmin);
            String userIdentifierResponse = (String) newUserResponse.get("userIdentifier");
            assertEquals(newUserResponse.get("userIdentifier"), userIdentifierResponse);

            //reinviteUserMock(HttpStatus.TOO_MANY_REQUESTS);
            NewUserCreationRequest reinviteRequest = reInviteUserCreationRequest(userCreationRequest.getEmail(), userRoles);
            Map<String, Object> reInviteUserResponse =
                    professionalReferenceDataClient.addUserToOrganisation(organisationIdentifier, reinviteRequest, hmctsAdmin);
            assertThat(reInviteUserResponse.get("http_status")).isEqualTo("429");
            assertThat((String) reInviteUserResponse.get("response_body")).contains(String.format("The request was last made less than %s minutes ago. Please try after some time", resendInterval));
        }

    }

    // AC9: invited more than an hour ago but has recently activated their account
    @Test
    public void should_return_409_when_reinvited_user_gets_active_in_sidam_but_pending_in_up() {

        if (resendInviteEnabled) {
            //userProfileCreateUserWireMock(HttpStatus.CREATED);

            Map<String, Object> newUserResponse =
                    professionalReferenceDataClient.addUserToOrganisation(organisationIdentifier, userCreationRequest, hmctsAdmin);
            String userIdentifierResponse = (String) newUserResponse.get("userIdentifier");
            assertEquals(newUserResponse.get("userIdentifier"), userIdentifierResponse);

            //reinviteUserMock(HttpStatus.CONFLICT);
            NewUserCreationRequest reinviteRequest = reInviteUserCreationRequest(userCreationRequest.getEmail(), userRoles);
            Map<String, Object> reInviteUserResponse =
                    professionalReferenceDataClient.addUserToOrganisation(organisationIdentifier, reinviteRequest, hmctsAdmin);
            assertThat(reInviteUserResponse.get("http_status")).isEqualTo("409");
            assertThat((String) reInviteUserResponse.get("response_body")).contains(String.format("Resend invite failed as user is already active. Wait for %s minutes for the system to refresh.", syncInterval));
        }
    }

/*
    // should not allow re invite of user who is not in organisation
    @Test
    public void should_return_403_when_reinvited_user_not_present_in_organisation() {

        if (resendInviteEnabled) {
            userProfileCreateUserWireMock(HttpStatus.CREATED);

            OrganisationCreationRequest organisationCreationRequest1 = someMinimalOrganisationRequest().build();
            createAndActivateOrganisation(organisationCreationRequest1);

            OrganisationCreationRequest organisationCreationRequest2 = someMinimalOrganisationRequest().build();
            String org2 = createAndActivateOrganisation(organisationCreationRequest2);

            NewUserCreationRequest reinviteRequest = reInviteUserCreationRequest(organisationCreationRequest1.getSuperUser().getEmail(), userRoles);
            Map<String, Object> reInviteUserResponse =
                    professionalReferenceDataClient.addUserToOrganisation(org2, reinviteRequest, hmctsAdmin);
            assertThat(reInviteUserResponse.get("http_status")).isEqualTo("403");
            assertThat((String) reInviteUserResponse.get("response_body")).contains("User does not belong to same organisation");
        }
    }
}*/

