package uk.gov.hmcts.reform.professionalapi;

import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.FALSE;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.TRUE;

import io.restassured.specification.RequestSpecification;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import net.thucydides.core.annotations.WithTag;
import net.thucydides.core.annotations.WithTags;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;

@RunWith(SpringIntegrationSerenityRunner.class)
@WithTags({@WithTag("testType:Functional")})
@ActiveProfiles("functional")
@Slf4j
public class FindUsersByOrganisationTest extends AuthorizationFunctionalTest {

    @Ignore("convert to integration test once RDCC-2050 is completed")
    @Test
    public void find_users_by_active_organisation_with_returnRoles_invalid() {
        professionalApiClient.searchUsersByOrganisation(activeOrgId,
                puiCaseManager, "True", HttpStatus.FORBIDDEN, "");
    }

    @Ignore("convert to integration test once RDCC-2050 is completed")
    @Test
    public void find_users_for_non_active_organisation() {
        professionalApiClient.searchUsersByOrganisation(
                (String) professionalApiClient.createOrganisation().get("organisationIdentifier"),
                hmctsAdmin, "False", HttpStatus.NOT_FOUND, "");
    }

    @Test
    @Ignore("convert to integration test once RDCC-2050 is completed")
    public void ac7_find_all_active_users_for_an_organisation_with_invalid_bearer_token_should_return_401() {
        professionalApiClient.searchOrganisationUsersByStatusExternal(HttpStatus.UNAUTHORIZED,
                professionalApiClient.getMultipleAuthHeadersWithEmptyBearerToken(""), "");
    }

    @Test
    @Ignore("convert to integration test once RDCC-2050 is completed")
    public void rdcc1439_ac4_find_all_active_users_without_appropriate_role_for_an_organisation_should_return_403() {
        Map<String, Object> orgResponse = professionalApiClient.createOrganisation();
        String orgIdentifierResponse = (String) orgResponse.get("organisationIdentifier");
        professionalApiClient.updateOrganisation(orgIdentifierResponse, hmctsAdmin);

        List<String> userRoles = new ArrayList<>();
        userRoles.add("caseworker-caa");
        String userEmail = generateRandomEmail();
        String lastName = "someLastName";
        String firstName = "someName";

        RequestSpecification bearerTokenForCaseworkerCaa = professionalApiClient
                .getMultipleAuthHeadersExternal("caseworker-caa", firstName, lastName, userEmail);

        professionalApiClient.searchOrganisationUsersByReturnRolesParamExternal(HttpStatus.FORBIDDEN,
                bearerTokenForCaseworkerCaa, "");
    }

    @Test
    //RDCC-1531-AC2
    @Ignore("convert to integration test once RDCC-2050 is completed")
    public void find_users_by_active_org_with_system_user_role_should_return_404_when_users_are_not_active_under_org() {
        professionalApiClient.searchUsersByOrganisation(createAndUpdateOrganisationToActive(hmctsAdmin),
                systemUser, FALSE, HttpStatus.NOT_FOUND, TRUE);
    }

    @Test
    //RDCC-1531-AC3
    @Ignore("convert to integration test once RDCC-2050 is completed")
    public void find_users_by_active_organisation_with_non_permitted_role_should_return_403() {
        professionalApiClient.searchUsersByOrganisation(activeOrgId, puiCaseManager, FALSE, HttpStatus.FORBIDDEN, TRUE);
    }

    @Ignore("convert to integration test once RDCC-2050 is completed")
    @Test
    public void should_fail_to_retrieve_organisations_info_with_403_with_incorrect_roles_and_status_active() {
        // invite new user having invalid roles
       /* List<String> userRoles = new ArrayList<>();
        userRoles.add("caseworker");
        inviteNewUser(userRoles);
        validateErrorResponse((ErrorResponse) professionalApiClient
                        .retrieveAllActiveOrganisationsWithMinimalInfo(bearerToken,
                                HttpStatus.FORBIDDEN, ACTIVE.toString(), true),
                ACCESS_EXCEPTION.getErrorMessage(), ACCESS_IS_DENIED_ERROR_MESSAGE);*/
    }
}
