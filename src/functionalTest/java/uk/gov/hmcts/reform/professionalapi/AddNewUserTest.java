package uk.gov.hmcts.reform.professionalapi;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.professionalapi.client.ProfessionalApiClient.createOrganisationRequest;
import static uk.gov.hmcts.reform.professionalapi.client.ProfessionalApiClient.getNestedValue;
import static uk.gov.hmcts.reform.professionalapi.controller.request.NewUserCreationRequest.aNewUserCreationRequest;
import static uk.gov.hmcts.reform.professionalapi.controller.request.UserCreationRequest.aUserCreationRequest;
import static uk.gov.hmcts.reform.professionalapi.helper.OrganisationFixtures.createJurisdictions;
import static uk.gov.hmcts.reform.professionalapi.helper.OrganisationFixtures.someMinimalOrganisationRequest;

import com.microsoft.applicationinsights.boot.dependencies.apachecommons.lang3.RandomStringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.professionalapi.controller.constants.IdamStatus;
import uk.gov.hmcts.reform.professionalapi.controller.request.NewUserCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.UserCreationRequest;
import uk.gov.hmcts.reform.professionalapi.domain.UserProfileUpdatedData;

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
        Map<String, Object> newUserResponse = professionalApiClient.addNewUserToAnOrganisation(orgIdentifierResponse,
                hmctsAdmin, newUserCreationRequest, HttpStatus.CREATED);
        assertThat(newUserResponse).isNotNull();
    }

    @Test
    public void add_new_user_to_organisation_with_no_jurisdiction_should_return_400() {

        NewUserCreationRequest newUserCreationRequest = professionalApiClient.createNewUserRequest();
        newUserCreationRequest.setJurisdictions(new ArrayList<>());
        Map<String, Object> newUserResponse = professionalApiClient.addNewUserToAnOrganisation(orgIdentifierResponse,
                hmctsAdmin, newUserCreationRequest, HttpStatus.BAD_REQUEST);
        assertThat(newUserResponse).isNotNull();
    }

    @Test
    public void add_new_user_to_organisation_with_unknown_roles_should_return_404() {

        List<String> roles = new ArrayList<>();
        roles.add("unknown");
        NewUserCreationRequest newUserCreationRequest = professionalApiClient.createNewUserRequest();
        newUserCreationRequest.setRoles(roles);

        Map<String, Object> newUserResponse = professionalApiClient.addNewUserToAnOrganisation(orgIdentifierResponse,
                hmctsAdmin, newUserCreationRequest, HttpStatus.NOT_FOUND);
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
        Map<String, Object> newUserResponse = professionalApiClient.addNewUserToAnOrganisation(organisationIdentifier,
                hmctsAdmin, newUserCreationRequest, HttpStatus.CONFLICT);
        assertThat((String) newUserResponse.get("errorDescription")).contains("409 User already exists");
    }

    @Test
    public void add_new_user_to_organisation_by_super_user() {
        List<String> userRoles = new ArrayList<>();
        userRoles.add("caseworker");
        NewUserCreationRequest newUserCreationRequest = aNewUserCreationRequest()
                .firstName("someName")
                .lastName("someLastName")
                .email(RandomStringUtils.randomAlphabetic(10) + "@hotmail.com".toLowerCase())
                .roles(userRoles)
                .jurisdictions(createJurisdictions())
                .build();

        Map<String, Object> newUserResponse = professionalApiClient
                .addNewUserToAnOrganisationExternal(newUserCreationRequest, generateSuperUserBearerToken(),
                        HttpStatus.CREATED);
        assertThat(newUserResponse).isNotNull();
    }

    //currently returning 500 response status code which is coming 500 from SIDAM - but comes 403 when testing in
    // Swagger
    @Test
    public void add_new_user_to_organisation_when_super_user_is_not_active_throws_403() {
        String firstName = "some-fname";
        String lastName = "some-lname";
        String email = RandomStringUtils.randomAlphabetic(10) + "@usersearch.test".toLowerCase();
        UserCreationRequest superUser = aUserCreationRequest()
                .firstName(firstName)
                .lastName(lastName)
                .email(email)
                .jurisdictions(createJurisdictions())
                .build();

        //create Super User in IDAM
        bearerToken = professionalApiClient.getMultipleAuthHeadersExternal(puiUserManager, firstName, lastName, email);

        OrganisationCreationRequest request = someMinimalOrganisationRequest()
                .superUser(superUser)
                .build();

        //Create organisation with Super User that is already Active in IDAM
        Map<String, Object> response = professionalApiClient.createOrganisation(request);
        String orgIdentifier = (String) response.get("organisationIdentifier");
        request.setStatus("ACTIVE");
        professionalApiClient.updateOrganisation(request, hmctsAdmin, orgIdentifier);

        //Retrieve User Identifier to update status
        Map<String, Object> searchUsersResponse = professionalApiClient.searchUsersByOrganisation(orgIdentifier,
                hmctsAdmin, "false", HttpStatus.OK, "true");
        assertThat(searchUsersResponse.get("users")).asList().isNotEmpty();
        List<HashMap> professionalUsersResponses = (List<HashMap>) searchUsersResponse.get("users");

        UserProfileUpdatedData data = new UserProfileUpdatedData();
        data.setFirstName("UpdatedFirstName");
        data.setLastName("UpdatedLastName");
        data.setIdamStatus(IdamStatus.SUSPENDED.name());

        String userId = (String) professionalUsersResponses.get(0).get("userIdentifier");
        //Updating user status from Active to Suspended
        professionalApiClient.modifyUserToExistingUserForPrdAdmin(HttpStatus.OK, data, orgIdentifier, userId);

        List<String> userRoles = new ArrayList<>();
        userRoles.add("caseworker");
        NewUserCreationRequest newUserCreationRequest = aNewUserCreationRequest()
                .firstName("someName")
                .lastName("someLastName")
                .email(RandomStringUtils.randomAlphabetic(10) + "@hotmail.com".toLowerCase())
                .roles(userRoles)
                .jurisdictions(createJurisdictions())
                .build();

        //adding new user with Suspended Super User Bearer Token
        Map<String, Object> newUserResponse = professionalApiClient
                .addNewUserToAnOrganisationExternal(newUserCreationRequest, bearerToken,
                        HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(newUserResponse).isNotNull();
    }

    @Test
    public void add_new_user_with_caa_roles_to_organisation_should_return_201() {
        List<String> userRoles = new ArrayList<>();
        userRoles.add(puiCaa);
        userRoles.add(caseworkerCaa);
        userRoles.add(puiUserManager);
        String firstName = "someName";
        String lastName = "someLastName";
        String email = randomAlphabetic(10) + "@somewhere.com".toLowerCase();

        NewUserCreationRequest newUserCreationRequest = aNewUserCreationRequest()
                .firstName(firstName)
                .lastName(lastName)
                .email(email)
                .roles(userRoles)
                .jurisdictions(createJurisdictions())
                .build();

        professionalApiClient.getMultipleAuthHeadersExternal(puiUserManager, firstName, lastName, email);

        Map<String, Object> newUserResponse = professionalApiClient
                .addNewUserToAnOrganisation(orgIdentifierResponse, hmctsAdmin, newUserCreationRequest,
                        HttpStatus.CREATED);
        assertThat(newUserResponse).isNotNull();

        Map<String, Object> searchUserResponse = professionalApiClient
                .searchUsersByOrganisation(orgIdentifierResponse, hmctsAdmin, "false", HttpStatus.OK,
                        "true");

        List<Map> users = getNestedValue(searchUserResponse, "users");
        Map newUserDetails = getActiveUser(users);
        List<String> superUserRoles = getNestedValue(newUserDetails, "roles");

        assertThat(superUserRoles).contains(puiCaa, caseworkerCaa);
    }
}