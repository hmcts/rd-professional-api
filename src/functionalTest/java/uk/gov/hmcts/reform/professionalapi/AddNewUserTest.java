package uk.gov.hmcts.reform.professionalapi;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.professionalapi.client.ProfessionalApiClient.createOrganisationRequest;

import java.util.Map;

import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.professionalapi.controller.request.NewUserCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest;

@RunWith(SpringIntegrationSerenityRunner.class)
@ActiveProfiles("functional")
public class AddNewUserTest extends AuthorizationFunctionalTest {


    private static String organisationIdentifier = null;

    @BeforeClass
    public static void  createOrganisation() {
        Map<String, Object> response = professionalApiClient.createOrganisation();
        organisationIdentifier = (String) response.get("organisationIdentifier");
        assertThat(organisationIdentifier).isNotEmpty();
        professionalApiClient.updateOrganisation(organisationIdentifier, hmctsAdmin);
    }
    @Test
    public void add_new_user_to_organisation() {

        NewUserCreationRequest newUserCreationRequest = professionalApiClient.createNewUserRequest();
        Map<String, Object> newUserResponse = professionalApiClient.addNewUserToAnOrganisation(organisationIdentifier, hmctsAdmin,newUserCreationRequest, HttpStatus.CREATED);
        assertThat(newUserResponse).isNotNull();
    }

    @Test
    public void should_throw_400_when_add_duplicate_new_user_to_organisation() {

        // create pending org
        OrganisationCreationRequest pendingOrganisationCreationRequest = createOrganisationRequest().build();
        professionalApiClient.createOrganisation(pendingOrganisationCreationRequest);
        String email = pendingOrganisationCreationRequest.getSuperUser().getEmail();

        // now invite same user/email used in above pending org should give BAD_REQUEST
        NewUserCreationRequest newUserCreationRequest = professionalApiClient.createNewUserRequest();
        newUserCreationRequest.setEmail(email);
        Map<String, Object> newUserResponse = professionalApiClient.addNewUserToAnOrganisation(organisationIdentifier, hmctsAdmin,newUserCreationRequest, HttpStatus.BAD_REQUEST);
    }
}
