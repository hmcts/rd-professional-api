package uk.gov.hmcts.reform.professionalapi;

import static org.apache.commons.lang.RandomStringUtils.randomAlphabetic;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.HttpStatus;
import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.reform.professionalapi.controller.constants.IdamStatus;
import uk.gov.hmcts.reform.professionalapi.controller.request.NewUserCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.response.OrganisationEntityResponse;
import uk.gov.hmcts.reform.professionalapi.idam.IdamOpenIdClient;

@RunWith(SpringIntegrationSerenityRunner.class)
@Slf4j
public class MinimalOrganisationInfoRetrieveTest extends AuthorizationFunctionalTest {

    Map<String, OrganisationEntityResponse> activeOrgs;
    Map<String, OrganisationEntityResponse> pendingOrgs;

    OrganisationEntityResponse organisationEntityResponse1;
    OrganisationEntityResponse organisationEntityResponse2;
    OrganisationEntityResponse organisationEntityResponse3;
    OrganisationEntityResponse organisationEntityResponse4;
    String orgIdentifier1;
    String orgIdentifier2;
    String orgIdentifier3;
    String orgIdentifier4;
    List<String> validRoles;

    @Test
    //AC:1
    public void should_retrieve_organisations_info_with_200_with_correct_roles_and_status_active() {

        setUpTestData();
        List<OrganisationEntityResponse> responseList = professionalApiClient.retrieveAllActiveOrganisationsWithMinimalInfo(bearerToken, HttpStatus.OK, IdamStatus.ACTIVE.toString());
        assertThat(responseList).contains(activeOrgs.get(orgIdentifier1), activeOrgs.get(orgIdentifier2));
        assertThat(responseList).doesNotContain(activeOrgs.get(orgIdentifier3), activeOrgs.get(orgIdentifier4));

    }

    @Test
    //AC:2
    public void should_fail_to_retrieve_organisations_info_with_403_with_correct_roles_and_status_active_and_caller_user_is_pending() {

        // invite new user having valid roles but keep it pending
        inviteNewUser(getValidRoleList(),  false);
        professionalApiClient.retrieveAllActiveOrganisationsWithMinimalInfo(bearerToken, HttpStatus.FORBIDDEN, IdamStatus.ACTIVE.toString());
    }

    @Test
    //AC:3
    public void should_fail_to_retrieve_organisations_info_with_403_with_incorrect_roles_and_status_active() {

        // invite new user having invalid roles
        List<String> userRoles = new ArrayList<>();
        userRoles.add("caseworker");
        inviteNewUser(userRoles,  true);
        professionalApiClient.retrieveAllActiveOrganisationsWithMinimalInfo(bearerToken, HttpStatus.FORBIDDEN, IdamStatus.ACTIVE.toString());

    }

    @Test
    //AC:5
    public void should_fail_to_retrieve_organisations_info_with_404_with_correct_roles_and_status_pending() {

        // invite new user having valid roles and also make it active
        inviteNewUser(getValidRoleList(),  true);
        professionalApiClient.retrieveAllActiveOrganisationsWithMinimalInfo(bearerToken, HttpStatus.NOT_FOUND, IdamStatus.PENDING.toString());
    }

    @Test
    //AC:6
    public void should_fail_to_retrieve_organisations_info_with_404_with_correct_roles_and_status_not_passed() {

        // invite new user having valid roles and also make it active
        inviteNewUser(getValidRoleList(),  true);
        professionalApiClient.retrieveAllActiveOrganisationsWithMinimalInfo(bearerToken, HttpStatus.NOT_FOUND, null);

    }

    public void setUpTestData() {
        //create 2 active orgs for validating response has these 2 orgs present
        createActiveOrganisation1();
        createActiveOrganisation2();

        activeOrgs = new HashMap<>();
        activeOrgs.put(orgIdentifier1, organisationEntityResponse1);
        activeOrgs.put(orgIdentifier2, organisationEntityResponse2);

        //create 2 pending orgs for validating response doesnt have 2 orgs present
        createPendingOrganisation1();
        createPendingOrganisation2();

        pendingOrgs = new HashMap<>();
        pendingOrgs.put(orgIdentifier3, organisationEntityResponse3);
        pendingOrgs.put(orgIdentifier4, organisationEntityResponse4);

        //invite user under org1 with valid roles and who is active
        inviteNewUser(getValidRoleList(),  true);

    }

    public void inviteNewUser(List<String> roles, boolean requiredActiveUser) {
        IdamOpenIdClient idamOpenIdClient = new IdamOpenIdClient(configProperties);
        String email = idamOpenIdClient.nextUserEmail();
        NewUserCreationRequest newUserCreationRequest = professionalApiClient.createNewUserRequest();
        newUserCreationRequest.setEmail(email);
        newUserCreationRequest.setRoles(roles);

        if (requiredActiveUser) {
            idamOpenIdClient.createUser(hmctsAdmin, email, newUserCreationRequest.getFirstName(), newUserCreationRequest.getLastName());
        }

        professionalApiClient.addNewUserToAnOrganisation(orgIdentifier1 == null ? createActiveOrganisation1() : orgIdentifier1, hmctsAdmin, newUserCreationRequest, HttpStatus.OK);

        bearerToken = professionalApiClient.getMultipleAuthHeaders(idamOpenIdClient.getOpenIdToken(email));
    }

    public List<String> getValidRoleList() {
        if (CollectionUtils.isEmpty(validRoles)) {
            validRoles = new ArrayList<>();
            List<String> userRoles = new ArrayList<>();
            userRoles.add("pui-user-manager");
            userRoles.add("pui-case-manager");
            userRoles.add("pui-organisation-manager");
            userRoles.add("pui-finance-manager");
            userRoles.add("pui-caa");
        }
        return validRoles;
    }

    public String createActiveOrganisation1() {
        String orgName1 = randomAlphabetic(7);
        OrganisationCreationRequest.anOrganisationCreationRequest().name(orgName1);
        orgIdentifier1 = createAndUpdateOrganisationToActive(hmctsAdmin);
        organisationEntityResponse1 = new OrganisationEntityResponse(orgIdentifier1, orgName1);
        return orgIdentifier1;
    }

    public String createActiveOrganisation2() {
        String orgName2 = randomAlphabetic(7);
        OrganisationCreationRequest.anOrganisationCreationRequest().name(orgName2);
        orgIdentifier2 = createAndUpdateOrganisationToActive(hmctsAdmin);
        organisationEntityResponse2 = new OrganisationEntityResponse(orgIdentifier2, orgName2);
        return orgIdentifier2;
    }

    public void createPendingOrganisation1() {
        String orgName3 = randomAlphabetic(7);
        orgIdentifier3 = (String)professionalApiClient.createOrganisation().get("organisationIdentifier");
        organisationEntityResponse3 = new OrganisationEntityResponse(orgIdentifier3, orgName3);
    }

    public void createPendingOrganisation2() {
        String orgName4 = randomAlphabetic(7);
        orgIdentifier4 = (String)professionalApiClient.createOrganisation().get("organisationIdentifier");
        organisationEntityResponse4 = new OrganisationEntityResponse(orgIdentifier4, orgName4);
    }


}
