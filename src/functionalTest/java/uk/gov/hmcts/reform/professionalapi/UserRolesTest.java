package uk.gov.hmcts.reform.professionalapi;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.professionalapi.controller.request.NewUserCreationRequest.aNewUserCreationRequest;
import static uk.gov.hmcts.reform.professionalapi.controller.request.UserCreationRequest.aUserCreationRequest;
import static uk.gov.hmcts.reform.professionalapi.helper.OrganisationFixtures.someMinimalOrganisationRequest;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import net.thucydides.core.annotations.WithTag;
import net.thucydides.core.annotations.WithTags;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.professionalapi.controller.constants.IdamStatus;
import uk.gov.hmcts.reform.professionalapi.controller.request.NewUserCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.UserCreationRequest;

@RunWith(SpringIntegrationSerenityRunner.class)
@WithTags({@WithTag("testType:Functional")})
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

        String email = generateRandomEmail().toLowerCase();
        UserCreationRequest superUser = createSuperUser(email);

        professionalApiClient.getMultipleAuthHeadersExternalForSuperUser(superUserRoles(), firstName, lastName, email);

        OrganisationCreationRequest request = someMinimalOrganisationRequest()
                .superUser(superUser)
                .build();
        log.info("create organisation request");
        Map<String, Object> response = professionalApiClient.createOrganisation(request);
        orgIdentifier = (String) response.get("organisationIdentifier");
        request.setStatus("ACTIVE");
        professionalApiClient.updateOrganisation(request, hmctsAdmin, orgIdentifier);

        Map<String, Object> searchUserResponse = professionalApiClient.searchUsersByOrganisation(orgIdentifier,
                hmctsAdmin, "false", HttpStatus.OK, "true");
        validateRetrievedUsers(searchUserResponse, "any");

        List<Map> users = getNestedValue(searchUserResponse, "users");
        Map superUserDetails = users.get(0);
        List<String> superUserRoles = getNestedValue(superUserDetails, "roles");

        assertThat(superUserRoles).contains("caseworker");

    }

    @Test
    public void rdcc_1387_ac1_super_user_can_have_caa_roles() {
        String email = generateRandomEmail().toLowerCase();
        UserCreationRequest superUser = createSuperUser(email);

        professionalApiClient.getMultipleAuthHeadersExternalForSuperUser(superUserRoles(), firstName, lastName, email);

        OrganisationCreationRequest request = someMinimalOrganisationRequest()
                .superUser(superUser)
                .build();

        Map<String, Object> response = professionalApiClient.createOrganisation(request);
        orgIdentifier = (String) response.get("organisationIdentifier");
        request.setStatus("ACTIVE");
        log.info("orgIdentifier::" + orgIdentifier);
        professionalApiClient.updateOrganisation(request, hmctsAdmin, orgIdentifier);

        Map<String, Object> searchUserResponse = professionalApiClient
                .searchUsersByOrganisation(orgIdentifier, hmctsAdmin, "false", HttpStatus.OK,
                        "true");

        log.info("searchUserResponse::" + searchUserResponse);
        validateRetrievedUsers(searchUserResponse, "any");

        List<Map> users = getNestedValue(searchUserResponse, "users");
        Map superUserDetails = users.get(0);
        List<String> superUserRoles = getNestedValue(superUserDetails, "roles");
        log.info("superUserRoles::" + superUserRoles);
        assertThat(superUserRoles).doesNotContain(puiCaa, caseworkerCaa);
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
                .build();
        return superUser;
    }

    private NewUserCreationRequest createNewUser(String email,List<String> userRoles) {
        NewUserCreationRequest newUser = aNewUserCreationRequest()
                .firstName(firstName)
                .lastName(lastName)
                .email(email)
                .roles(userRoles)
                .build();
        return newUser;
    }
}
