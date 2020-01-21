package uk.gov.hmcts.reform.professionalapi;

import static org.apache.commons.lang.RandomStringUtils.randomAlphabetic;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.professionalapi.controller.request.NewUserCreationRequest.aNewUserCreationRequest;
import static uk.gov.hmcts.reform.professionalapi.controller.request.UserCreationRequest.aUserCreationRequest;
import static uk.gov.hmcts.reform.professionalapi.utils.OrganisationFixtures.someMinimalOrganisationRequest;

import io.restassured.specification.RequestSpecification;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.professionalapi.controller.request.NewUserCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.UserCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.response.IdamStatus;
import uk.gov.hmcts.reform.professionalapi.utils.OrganisationFixtures;

@RunWith(SpringIntegrationSerenityRunner.class)
@ActiveProfiles("functional")
@Slf4j
public class UserRolesTest extends AuthorizationFunctionalTest {

    private String orgIdentifier;
    private String firstName = "some-fname";
    private String lastName = "some-lname";

    private List<String> dummyRoles = Arrays.asList("dummy-role-one", "dummy-role-two");
    private List<String> puiUserManagerRoleOnly = Arrays.asList("pui-user-manager");

    @Test
    public void rdcc_720_ac1_super_user_can_have_fpla_or_iac_roles() {

        String email = randomAlphabetic(10) + "@usersearch.test".toLowerCase();
        UserCreationRequest superUser = createSuperUser(email);

        professionalApiClient.getMultipleAuthHeadersExternal(puiUserManager, firstName, lastName, email);

        OrganisationCreationRequest request = someMinimalOrganisationRequest()
                .superUser(superUser)
                .build();

        Map<String, Object> response = professionalApiClient.createOrganisation(request);
        orgIdentifier = (String) response.get("organisationIdentifier");
        request.setStatus("ACTIVE");
        professionalApiClient.updateOrganisation(request, hmctsAdmin, orgIdentifier);

        Map<String, Object> searchUserResponse = professionalApiClient.searchUsersByOrganisation(orgIdentifier, hmctsAdmin, "false", HttpStatus.OK);
        validateRetrievedUsers(searchUserResponse, "any");

        List<Map> users = getNestedValue(searchUserResponse, "users");
        Map superUserDetails = users.get(0);
        List<String> superUserRoles = getNestedValue(superUserDetails, "roles");

        assertThat(superUserRoles).contains("caseworker");

    }

    @Test
    public void ac5_internal_user_cannot_add_user_with_non_fpla_or_iac_roles() {

        String orgIdentifier =  createAndUpdateOrganisationToActive(hmctsAdmin);

        String email = randomAlphabetic(10) + "@usersearch.test".toLowerCase();
        NewUserCreationRequest userCreationRequest = createNewUser(email, dummyRoles);

        professionalApiClient.getMultipleAuthHeadersExternal(puiUserManager, firstName, lastName, email);
        professionalApiClient.addNewUserToAnOrganisation(orgIdentifier, hmctsAdmin, userCreationRequest, HttpStatus.BAD_REQUEST);

    }

    //External endpoint for create new user is failing because PUM is unable to add new user with fpla and iac roles in AAT env
    //Awaiting https://tools.hmcts.net/jira/browse/SIDM-3475 and https://tools.hmcts.net/jira/browse/SIDM-3476
    public void ac6_external_user_cannot_add_user_with_non_fpla_or_iac_roles() {

    }


    public RequestSpecification generateBearerTokenForPuiManager() {
        Map<String, Object> response = professionalApiClient.createOrganisation();
        String orgIdentifierResponse = (String) response.get("organisationIdentifier");
        professionalApiClient.updateOrganisation(orgIdentifierResponse, hmctsAdmin);

        String userEmail = randomAlphabetic(5).toLowerCase() + "@hotmail.com";
        String lastName = "someLastName";
        String firstName = "someName";

        bearerTokenForPuiManager = professionalApiClient.getMultipleAuthHeadersExternal(puiUserManager, firstName, lastName, userEmail);

        List<String> userRoles1 = new ArrayList<>();
        userRoles1.add("pui-organisation-manager");
        NewUserCreationRequest userCreationRequest = aNewUserCreationRequest()
                .firstName(firstName)
                .lastName(lastName)
                .email(userEmail)
                .roles(userRoles1)
                .jurisdictions(OrganisationFixtures.createJurisdictions())
                .build();
        professionalApiClient.addNewUserToAnOrganisation(orgIdentifierResponse, hmctsAdmin, userCreationRequest, HttpStatus.OK);

        return bearerTokenForPuiManager;
    }

    void validateRetrievedUsers(Map<String, Object> searchResponse, String expectedStatus) {
        assertThat(searchResponse.get("users")).asList().isNotEmpty();

        List<HashMap> professionalUsersResponses = (List<HashMap>) searchResponse.get("users");

        professionalUsersResponses.stream().forEach(user -> {
            assertThat(user.get("idamStatus")).isNotNull();
            assertThat(user.get("userIdentifier")).isNotNull();
            assertThat(user.get("firstName")).isNotNull();
            assertThat(user.get("lastName")).isNotNull();
            assertThat(user.get("email")).isNotNull();
            if (!expectedStatus.equals("any")) {
                assertThat(user.get("idamStatus").equals(expectedStatus));
            }
            if (user.get("idamStatus").equals(IdamStatus.ACTIVE.toString())) {
                assertThat(user.get("roles")).isNotNull();
            }
        });
    }

    public static <T> T getNestedValue(Map map, String... keys) {
        Object value = map;

        for (String key : keys) {
            value = ((Map) value).get(key);
        }

        return (T) value;
    }

    private UserCreationRequest createSuperUser(String email) {
        UserCreationRequest superUser = aUserCreationRequest()
                .firstName(firstName)
                .lastName(lastName)
                .email(email)
                .jurisdictions(OrganisationFixtures.createJurisdictions())
                .build();
        return superUser;
    }

    private NewUserCreationRequest createNewUser(String email,List<String> userRoles) {
        NewUserCreationRequest newUser = aNewUserCreationRequest()
                .firstName(firstName)
                .lastName(lastName)
                .email(email)
                .roles(userRoles)
                .jurisdictions(OrganisationFixtures.createJurisdictions())
                .build();
        return newUser;
    }
}
