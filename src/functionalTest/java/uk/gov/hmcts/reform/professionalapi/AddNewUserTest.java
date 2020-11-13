package uk.gov.hmcts.reform.professionalapi;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.professionalapi.client.ProfessionalApiClient.createOrganisationRequest;
import static uk.gov.hmcts.reform.professionalapi.client.ProfessionalApiClient.getNestedValue;
import static uk.gov.hmcts.reform.professionalapi.controller.request.NewUserCreationRequest.aNewUserCreationRequest;
import static uk.gov.hmcts.reform.professionalapi.controller.request.UserCreationRequest.aUserCreationRequest;
import static uk.gov.hmcts.reform.professionalapi.helper.OrganisationFixtures.someMinimalOrganisationRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


import lombok.extern.slf4j.Slf4j;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import net.thucydides.core.annotations.WithTag;
import net.thucydides.core.annotations.WithTags;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.professionalapi.controller.request.NewUserCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest;

@RunWith(SpringIntegrationSerenityRunner.class)
@WithTags({@WithTag("testType:Functional")})
@ActiveProfiles("functional")
@Slf4j
public class AddNewUserTest extends AuthorizationFunctionalTest {

    static String orgIdentifierResponse = null;

    @Before
    public void createAndUpdateOrganisation() {
        if (isEmpty(orgIdentifierResponse)) {
            orgIdentifierResponse = createAndUpdateOrganisationToActive(hmctsAdmin);
        }
    }

    @Test
    public void add_new_user_to_organisation() {

        NewUserCreationRequest newUserCreationRequest = professionalApiClient.createNewUserRequest();
        Map<String, Object> newUserResponse = professionalApiClient.addNewUserToAnOrganisation(orgIdentifierResponse,
                hmctsAdmin, newUserCreationRequest, HttpStatus.CREATED);
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

        String firstName = "some-fname";
        String lastName = "some-lname";
        String email = generateRandomEmail();

        bearerToken = professionalApiClient.getMultipleAuthHeadersExternal("pui-user-manager", firstName,
                        lastName, generateRandomEmail());

        OrganisationCreationRequest request = someMinimalOrganisationRequest()
                .superUser(aUserCreationRequest()
                        .firstName(firstName)
                        .lastName(lastName)
                        .email(email)
                        .build())
                .build();

        Map<String, Object> response = professionalApiClient.createOrganisation(request);
        String orgIdentifier = (String) response.get("organisationIdentifier");
        request.setStatus("ACTIVE");
        professionalApiClient.updateOrganisation(request, hmctsAdmin, orgIdentifier);

        NewUserCreationRequest newUserCreationRequest = aNewUserCreationRequest()
                .firstName("someName")
                .lastName("someLastName")
                .email(generateRandomEmail())
                .roles(userRoles)
                .build();

        Map<String, Object> newUserResponse = professionalApiClient
                .addNewUserToAnOrganisationExternal(newUserCreationRequest, bearerToken, HttpStatus.CREATED);
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
        String email = generateRandomEmail().toLowerCase();

        NewUserCreationRequest newUserCreationRequest = aNewUserCreationRequest()
                .firstName(firstName)
                .lastName(lastName)
                .email(email)
                .roles(userRoles)
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