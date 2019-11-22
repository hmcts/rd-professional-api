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
    private String email = randomAlphabetic(10) + "@usersearch.test".toLowerCase();
    private String email2 = randomAlphabetic(10) + "@usersearch2.test".toLowerCase();
    private List<String> fplaAndIacRoles = Arrays.asList("caseworker-publiclaw", "caseworker-publiclaw-solicitor", "caseworker-ia-legalrep-solicitor");
    private List<String> dummmRoles = Arrays.asList("dummy-role-one", "dummy-role-two");
    private List<String> puiUserManagerRoleOnly = Arrays.asList(puiUserManager);
    private List<String> userRoles = new ArrayList<>();

    @Test
    public void ac1_super_user_can_have_fpla_or_iac_roles() {

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

        assertThat(superUserRoles).contains("caseworker-publiclaw", "caseworker-publiclaw-solicitor", "caseworker-ia-legalrep-solicitor");

    }

    @Test
    public void ac2_internal_user_can_add_new_user_with_fpla_or_iac_roles() {

        Map<String, Object> response = professionalApiClient.createOrganisation();
        orgIdentifier = (String) response.get("organisationIdentifier");
        professionalApiClient.updateOrganisation(orgIdentifier, hmctsAdmin);

        userRoles.addAll(fplaAndIacRoles);

        NewUserCreationRequest userCreationRequest = createNewUser(email, userRoles);

        professionalApiClient.getMultipleAuthHeadersExternal(puiUserManager, firstName, lastName, email);
        professionalApiClient.addNewUserToAnOrganisation(orgIdentifier, hmctsAdmin, userCreationRequest, HttpStatus.CREATED);

        Map<String, Object> searchUserResponse = professionalApiClient.searchUsersByOrganisation(orgIdentifier, hmctsAdmin, "false", HttpStatus.OK);
        validateRetrievedUsers(searchUserResponse, "any");

        List<Map> users = getNestedValue(searchUserResponse, "users");
        log.info("USERS:::::::::::::" + users);
        Map newUserDetails = users.get(1);
        log.info("NEW USER::::::::::::" + newUserDetails);
        List<String> newUserRoles = getNestedValue(newUserDetails, "roles");
        log.info("NEW USER ROLES::::::::::" + newUserRoles);

        assertThat(newUserRoles).contains("caseworker-publiclaw", "caseworker-publiclaw-solicitor", "caseworker-ia-legalrep-solicitor");

    }

    @Test
    public void ac4_internal_user_cannot_add_user_with_non_fpla_or_iac_roles() {

        String orgIdentifier =  createAndUpdateOrganisationToActive(hmctsAdmin);

        userRoles.addAll(dummmRoles);
        NewUserCreationRequest userCreationRequest = createNewUser(email, userRoles);

        professionalApiClient.getMultipleAuthHeadersExternal(puiUserManager, firstName, lastName, email);
        professionalApiClient.addNewUserToAnOrganisation(orgIdentifier, hmctsAdmin, userCreationRequest, HttpStatus.BAD_REQUEST);

    }

    public RequestSpecification generateBearerTokenForPuiManager() {
        Map<String, Object> response = professionalApiClient.createOrganisation();
        String orgIdentifierResponse = (String) response.get("organisationIdentifier");
        professionalApiClient.updateOrganisation(orgIdentifierResponse, hmctsAdmin);

        String userEmail = randomAlphabetic(5).toLowerCase() + "@hotmail.com";
        String lastName = "someLastName";
        String firstName = "someName";

        bearerTokenForPuiUserManager = professionalApiClient.getMultipleAuthHeadersExternal(puiUserManager, firstName, lastName, userEmail);

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

        return bearerTokenForPuiUserManager;
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
