package uk.gov.hmcts.reform.professionalapi;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.professionalapi.client.ProfessionalApiClient.createOrganisationRequest;
import static uk.gov.hmcts.reform.professionalapi.helper.OrganisationFixtures.someMinimalOrganisationRequest;

import java.util.Map;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import net.thucydides.core.annotations.WithTag;
import net.thucydides.core.annotations.WithTags;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.professionalapi.controller.request.NewUserCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.idam.IdamOpenIdClient;

@RunWith(SpringIntegrationSerenityRunner.class)
@WithTags({@WithTag("testType:Functional")})
@ActiveProfiles("functional")
public class ReInviteUserTest extends AuthorizationFunctionalTest {

    String orgIdentifierResponse;

    private OrganisationCreationRequest organisationCreationRequest;

    public static final String RANDOM_EMAIL = "RANDOM_EMAIL";

    @Value(("${resendInviteEnabled}"))
    protected boolean resendInviteEnabled;

    @Value("${resendInterval}")
    protected String resendInterval;

    @Before
    public void createAndUpdateOrganisation() {
        if (resendInviteEnabled) {
            organisationCreationRequest = someMinimalOrganisationRequest().build();
            orgIdentifierResponse = createAndUpdateOrganisationToActive(hmctsAdmin, organisationCreationRequest);
        }
    }

    //AC3: resend invite to a given user who does not exist
    @Test
    @Ignore("convert to integration test once RDCC-2050 is completed")
    public void should_return_404_when_reinvited_user_not_exists() {
        if (resendInviteEnabled) {
            NewUserCreationRequest newUserCreationRequest = professionalApiClient
                    .createReInviteUserRequest(RANDOM_EMAIL);
            Map<String, Object> newUserResponse = professionalApiClient
                    .addNewUserToAnOrganisation(orgIdentifierResponse, hmctsAdmin, newUserCreationRequest,
                            HttpStatus.NOT_FOUND);
            assertThat((String) newUserResponse.get("errorDescription")).contains("User does not exist");
        }
    }

    //re inviting super user
    @Test
    @Ignore("convert to integration test once RDCC-2050 is completed")
    public void should_reinvite_super_user_within_one_hour() {

        if (resendInviteEnabled) {

            //create and  activate org
            OrganisationCreationRequest organisationCreationRequest = createOrganisationRequest().build();

            orgIdentifierResponse = createAndUpdateOrganisationToActive(hmctsAdmin, organisationCreationRequest);

            // re invite super user
            NewUserCreationRequest newUserCreationRequest = professionalApiClient
                    .createReInviteUserRequest(organisationCreationRequest.getSuperUser().getEmail());

            Map<String, Object> reinviteUserResponse = professionalApiClient
                    .addNewUserToAnOrganisation(orgIdentifierResponse, hmctsAdmin, newUserCreationRequest,
                            HttpStatus.TOO_MANY_REQUESTS);

            assertThat((String) reinviteUserResponse.get("errorDescription"))
                    .contains(String.format("The request was last made less than %s minutes ago. Please try after "
                            + "some time", resendInterval));
        }
    }

    // should not re invite when user does not exists in organisation for internal user
    @Test
    @Ignore("convert to integration test once RDCC-2050 is completed")
    public void should_return_403_when_internal_user_reinvites_user_which_does_not_exists_in_same_organisation() {
        if (resendInviteEnabled) {

            OrganisationCreationRequest organisationCreationRequest1 = someMinimalOrganisationRequest().build();
            createAndUpdateOrganisationToActive(hmctsAdmin, organisationCreationRequest1);
            String organisationIdentifier = createAndUpdateOrganisationToActive(hmctsAdmin,
                    someMinimalOrganisationRequest().build());

            NewUserCreationRequest newUserCreationRequest = professionalApiClient
                    .createReInviteUserRequest(organisationCreationRequest1.getSuperUser().getEmail());
            Map<String, Object> newUserResponse = professionalApiClient
                    .addNewUserToAnOrganisation(organisationIdentifier, hmctsAdmin, newUserCreationRequest,
                            HttpStatus.FORBIDDEN);
            assertThat((String) newUserResponse.get("errorDescription")).contains("User does not belong to same "
                    + "organisation");
        }
    }

    // should not re invite when user does not exists in organisation for external user
    @Test
    @Ignore("convert to integration test once RDCC-2050 is completed")
    public void should_return_403_when_external_user_reinvites_user_which_does_not_exists_in_same_organisation() {
        if (resendInviteEnabled) {
            if (resendInviteEnabled) {
                // create active PUM sidam user and invite
                IdamOpenIdClient idamOpenIdClient = new IdamOpenIdClient(configProperties);
                Map<String,String> pumUserCreds = idamOpenIdClient.createUser(addRoles("pui-user-manager"));
                NewUserCreationRequest newUserCreationRequest = professionalApiClient
                        .createNewUserRequest(pumUserCreds.get(EMAIL));
                professionalApiClient.addNewUserToAnOrganisation(orgIdentifierResponse, hmctsAdmin,
                        newUserCreationRequest, HttpStatus.CREATED);

                // get PUM bearer token and reinvite with any other user present in another org
                organisationCreationRequest = someMinimalOrganisationRequest().build();
                orgIdentifierResponse = createAndUpdateOrganisationToActive(hmctsAdmin, organisationCreationRequest);
                String pumBearerToken = idamOpenIdClient
                        .getOpenIdToken(pumUserCreds.get(EMAIL), pumUserCreds.get(CREDS));
                newUserCreationRequest.setResendInvite(true);
                newUserCreationRequest.setEmail(organisationCreationRequest.getSuperUser().getEmail());
                Map<String, Object> reinviteUserResponse = professionalApiClient
                        .addNewUserToAnOrganisationExternal(newUserCreationRequest, professionalApiClient
                                .getMultipleAuthHeaders(pumBearerToken), HttpStatus.FORBIDDEN);
                assertThat((String) reinviteUserResponse.get("errorDescription"))
                        .contains("User does not belong to same organisation");
            }
        }
    }


}
