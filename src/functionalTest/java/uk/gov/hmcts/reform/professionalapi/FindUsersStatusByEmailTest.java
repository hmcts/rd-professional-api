package uk.gov.hmcts.reform.professionalapi;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.restassured.specification.RequestSpecification;
import lombok.extern.slf4j.Slf4j;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import net.thucydides.core.annotations.WithTag;
import net.thucydides.core.annotations.WithTags;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.professionalapi.controller.request.NewUserCreationRequest;

@RunWith(SpringIntegrationSerenityRunner.class)
@WithTags({@WithTag("testType:Functional")})
@ActiveProfiles("functional")
@Slf4j
public class FindUsersStatusByEmailTest extends AuthorizationFunctionalTest {

    static String orgId;

    @Before
    public void setUp() {
        if (isEmpty(orgId)) {
            orgId = createAndUpdateOrganisationToActive(hmctsAdmin);
        }
    }

    @Ignore("convert to integration test once RDCC-2050 is completed")
    @Test
    public void findUserStatusByEmailFromHeaderWithPuiCaseManagerRoleShouldReturn200() {
        puiCaseManagerBearerToken = generateBearerToken(puiCaseManagerBearerToken, "pui-user-manager");

        Map<String, Object> response = professionalApiClient.findUserStatusByEmail(HttpStatus.OK,
                professionalApiClient.getMultipleAuthHeaders(puiCaseManagerBearerToken), email);
        assertThat(response.get("userIdentifier")).isNotNull();

    }

    @Ignore("convert to integration test once RDCC-2050 is completed")
    @Test
    public void findUsrStatusByEmailFrmHeaderWithPuiCaseManagerRoleShouldReturn200WithUserStatusActive() {
        puiCaseManagerBearerToken = generateBearerToken(puiCaseManagerBearerToken, "pui-user-manager");

        Map<String, Object> response = professionalApiClient.findUserStatusByEmail(HttpStatus.OK,
                professionalApiClient.getMultipleAuthHeaders(puiCaseManagerBearerToken), email);
        assertThat(response.get("userIdentifier")).isNotNull();
    }

    @Ignore("convert to integration test once RDCC-2050 is completed")
    @Test
    public void findUsrStatusByEmailFrmHeaderWithNotActivePuiFinanceMgrRoleShouldRtnStatusForUsr() {
        // creating new user request
        List<String> userRoles = new ArrayList<>();
        userRoles.add("pui-finance-manager");
        NewUserCreationRequest userCreationRequest = createUserRequest(userRoles);
        // inviting user
        professionalApiClient.addNewUserToAnOrganisation(orgId, hmctsAdmin, userCreationRequest, HttpStatus.CREATED);
        // find the status of the user
        Map<String, Object> response = professionalApiClient.findUserStatusByEmail(HttpStatus.OK,
                generateBearerTokenForEmailHeader(puiFinanceManager), userCreationRequest.getEmail());
        assertThat(response.get("userIdentifier")).isNotNull();
    }

    @Ignore("convert to integration test once RDCC-2050 is completed")
    @Test
    public void findUsrStatusByEmailFrmHeaderWithNotActivePuiFinanceMgrRoleShouldRtnStatusPendingForUsr() {
        // creating new user request
        List<String> userRoles = new ArrayList<>();
        userRoles.add("pui-finance-manager");
        NewUserCreationRequest userCreationRequest = createUserRequest(userRoles);
        // inviting user
        professionalApiClient.addNewUserToAnOrganisation(orgId, hmctsAdmin, userCreationRequest, HttpStatus.CREATED);
        // find the status of the user
        Map<String, Object> response = professionalApiClient.findUserStatusByEmail(HttpStatus.NOT_FOUND,
                generateBearerTokenFor(puiFinanceManager), userCreationRequest.getEmail());
        assertThat(response.get("userIdentifier")).isNull();
    }

    @Ignore("convert to integration test once RDCC-2050 is completed")
    @Test
    public void ac4_find_usr_status_by_email_with_active_pui_organisation_manager_role_should_return_status_for_usr() {
        puiOrgManagerBearerToken = generateBearerToken(puiOrgManagerBearerToken, "pui-user-manager");

        Map<String, Object> response =
                professionalApiClient.findUserStatusByEmail(HttpStatus.OK,
                        professionalApiClient.getMultipleAuthHeaders(puiOrgManagerBearerToken), email);
        assertThat(response.get("userIdentifier")).isNotNull();
    }

    @Test
    @Ignore("convert to integration test once RDCC-2050 is completed")

    public void findUsrStatusByEmailFrmHeaderWithPuiOrganisationMgrRoleShouldRtnStatusForUsr() {

        // creating new user request
        List<String> userRoles = new ArrayList<>();
        userRoles.add("pui-user-manager");
        userRoles.add("pui-finance-manager");
        userRoles.add("pui-organisation-manager");
        NewUserCreationRequest userCreationRequest = createUserRequest(userRoles);

        RequestSpecification bearerToken = professionalApiClient.getMultipleAuthHeadersExternal(puiCaseManager,
                userCreationRequest.getFirstName(), userCreationRequest.getLastName(), userCreationRequest.getEmail());

        // inviting user
        professionalApiClient.addNewUserToAnOrganisation(orgId, hmctsAdmin, userCreationRequest, HttpStatus.CREATED);
        // find the status of the user

        Map<String, Object> response = professionalApiClient.findUserStatusByEmail(HttpStatus.OK,
                bearerToken, userCreationRequest.getEmail());
        assertThat(response.get("userIdentifier")).isNotNull();
    }


    @Test
    @Ignore("convert to integration test once RDCC-2050 is completed")
    public void rdcc_719_ac1_find_usr_sts_by_email_caseworker_publiclaw_courtadmin_role_shld_rtn_200__usr_sts_active() {

        // creating new user request
        List<String> userRoles = new ArrayList<>();
        userRoles.add("pui-organisation-manager");
        NewUserCreationRequest userCreationRequest = createUserRequest(userRoles);

        // creating user in idam with the same email used in the invite user
        // so that status automatically will update in the up
        professionalApiClient.getMultipleAuthHeadersExternal(puiOrgManager, userCreationRequest.getFirstName(),
                userCreationRequest.getLastName(), userCreationRequest.getEmail());

        // inviting user
        professionalApiClient.addNewUserToAnOrganisation(orgId, hmctsAdmin, userCreationRequest, HttpStatus.CREATED);

        String email = generateRandomEmail().toLowerCase();
        RequestSpecification bearerTokenForCourtAdmin = professionalApiClient
                .getMultipleAuthHeadersExternal("caseworker-publiclaw-courtadmin", "externalFname",
                        "externalLname", email);

        Map<String, Object> response = professionalApiClient.findUserStatusByEmail(HttpStatus.OK,
                bearerTokenForCourtAdmin, userCreationRequest.getEmail());
        assertThat(response.get("userIdentifier")).isNotNull();
    }
}