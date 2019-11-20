package uk.gov.hmcts.reform.professionalapi;

import static org.apache.commons.lang.RandomStringUtils.randomAlphabetic;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.professionalapi.controller.request.UserCreationRequest.aUserCreationRequest;
import static uk.gov.hmcts.reform.professionalapi.utils.OrganisationFixtures.someMinimalOrganisationRequest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
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

    @Test
    public void ac1_super_user_can_have_fpla_or_iac_roles() {

        String email = randomAlphabetic(10) + "@usersearch.test".toLowerCase();
        UserCreationRequest superUser = aUserCreationRequest()
                .firstName(firstName)
                .lastName(lastName)
                .email(email)
                .jurisdictions(OrganisationFixtures.createJurisdictions())
                .build();

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
        log.info("USER SEARCH RESPONSE::::::::::::" + searchUserResponse);

        //List<HashMap> superUserResponse = (List<HashMap>) searchUserResponse.get("users");
        //Map<String, Object> superUserDetails = superUserResponse.get(0);
        //log.info("SUPER USER DETAILS::::::::::::" + superUserResponse);

        List<Map> users = getNestedValue(searchUserResponse, "users");
        log.info("USERS::::::::::::" + users);
        Map superUserDetails = users.get(0);
        log.info("SUPER USER DETAILS:::::::::;" + superUserDetails);
        List<String> superUserRoles = getNestedValue(superUserDetails, "roles");
        log.info("SUPER USER ROLES:::::::::::::" + superUserRoles);

        assertThat(superUserRoles).contains("caseworker-publiclaw", "caseworker-publiclaw-solicitor", "caseworker-ia-legalrep-solicitor");

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
}
