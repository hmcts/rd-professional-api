package uk.gov.hmcts.reform.professionalapi;

import static org.apache.commons.lang.RandomStringUtils.randomAlphabetic;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.professionalapi.client.ProfessionalApiClient.createJurisdictions;
import static uk.gov.hmcts.reform.professionalapi.client.ProfessionalApiClient.createOrganisationRequest;
import static uk.gov.hmcts.reform.professionalapi.client.ProfessionalApiClient.getNestedValue;
import static uk.gov.hmcts.reform.professionalapi.controller.request.NewUserCreationRequest.aNewUserCreationRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.professionalapi.controller.request.NewUserCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest;

@RunWith(SpringIntegrationSerenityRunner.class)
@ActiveProfiles("functional")
public class AddNewUserTest extends AuthorizationFunctionalTest {

    String orgIdentifierResponse = null;

    @Before
    public void createAndUpdateOrganisation() {
        orgIdentifierResponse = createAndUpdateOrganisationToActive(hmctsAdmin);
    }

    @Test
    public void add_new_user_to_organisation() {

        NewUserCreationRequest newUserCreationRequest = professionalApiClient.createNewUserRequest();
        Map<String, Object> newUserResponse = professionalApiClient.addNewUserToAnOrganisation(orgIdentifierResponse, hmctsAdmin,  newUserCreationRequest, HttpStatus.CREATED);
        assertThat(newUserResponse).isNotNull();
    }

    @Test
    public void add_new_user_to_organisation_with_no_jurisdiction_should_return_400() {

        NewUserCreationRequest newUserCreationRequest = professionalApiClient.createNewUserRequest();
        newUserCreationRequest.setJurisdictions(new ArrayList<>());
        Map<String, Object> newUserResponse = professionalApiClient.addNewUserToAnOrganisation(orgIdentifierResponse, hmctsAdmin, newUserCreationRequest, HttpStatus.BAD_REQUEST);
        assertThat(newUserResponse).isNotNull();
    }

    @Test
    public void add_new_user_to_organisation_with_unknown_roles_should_return_404() {

        List<String> roles = new ArrayList<>();
        roles.add("unknown");
        NewUserCreationRequest newUserCreationRequest = professionalApiClient.createNewUserRequest();
        newUserCreationRequest.setRoles(roles);

        Map<String, Object> newUserResponse = professionalApiClient.addNewUserToAnOrganisation(orgIdentifierResponse, hmctsAdmin,newUserCreationRequest, HttpStatus.NOT_FOUND);
        assertThat(newUserResponse).isNotNull();
    }

    @Test
    public void should_throw_409_when_add_duplicate_new_user_to_organisation() {

        // create pending org
        OrganisationCreationRequest pendingOrganisationCreationRequest = createOrganisationRequest().build();
        professionalApiClient.createOrganisation(pendingOrganisationCreationRequest);

        // create organisation to add normal user
        String organisationIdentifier = createAndUpdateOrganisationToActive(hmctsAdmin);

        // now invite same user/email used in above pending org should give CONFLICT
        NewUserCreationRequest newUserCreationRequest = professionalApiClient.createNewUserRequest();
        newUserCreationRequest.setEmail(pendingOrganisationCreationRequest.getSuperUser().getEmail());
        Map<String, Object> newUserResponse = professionalApiClient.addNewUserToAnOrganisation(organisationIdentifier, hmctsAdmin,newUserCreationRequest, HttpStatus.CONFLICT);
        assertThat((String)newUserResponse.get("errorDescription")).contains("409 User already exists");
    }

    @Test
    public void add_new_user_with_caa_roles_to_organisation_should_return_201() {
        List<String> userRoles = new ArrayList<>();
        userRoles.add("pui-caa");
        userRoles.add("caseworker-caa");

        NewUserCreationRequest newUserCreationRequest = aNewUserCreationRequest()
                .firstName("someName")
                .lastName("someLastName")
                .email(randomAlphabetic(10) + "@hotmail.com".toLowerCase())
                .roles(userRoles)
                .jurisdictions(createJurisdictions())
                .build();

        Map<String, Object> newUserResponse = professionalApiClient.addNewUserToAnOrganisation(orgIdentifierResponse, hmctsAdmin,  newUserCreationRequest, HttpStatus.CREATED);
        assertThat(newUserResponse).isNotNull();

        Map<String, Object> searchUserResponse = professionalApiClient.searchUsersByOrganisation(orgIdentifierResponse, hmctsAdmin, "false", HttpStatus.OK);

        List<Map> users = getNestedValue(searchUserResponse, "users");
        Map superUserDetails = users.get(1);
        List<String> superUserRoles = getNestedValue(superUserDetails, "roles");

        assertThat(superUserRoles).contains("pui-caa");
        assertThat(superUserRoles).contains("caseworker-caa");
    }
}
