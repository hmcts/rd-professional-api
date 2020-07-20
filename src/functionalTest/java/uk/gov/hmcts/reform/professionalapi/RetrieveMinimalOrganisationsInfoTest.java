package uk.gov.hmcts.reform.professionalapi;

import static org.apache.commons.lang.RandomStringUtils.randomAlphabetic;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ErrorConstants.ACCESS_EXCEPTION;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ErrorConstants.EMPTY_RESULT_DATA_ACCESS;
import static uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus.ACTIVE;
import static uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus.PENDING;
import static uk.gov.hmcts.reform.professionalapi.helper.OrganisationFixtures.someMinimalOrganisationRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.HttpStatus;
import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.reform.professionalapi.controller.advice.ErrorResponse;
import uk.gov.hmcts.reform.professionalapi.controller.constants.IdamStatus;
import uk.gov.hmcts.reform.professionalapi.controller.request.NewUserCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.response.OrganisationMinimalInfoResponse;
import uk.gov.hmcts.reform.professionalapi.idam.IdamOpenIdClient;

@RunWith(SpringIntegrationSerenityRunner.class)
public class RetrieveMinimalOrganisationsInfoTest extends AuthorizationFunctionalTest {

    private static final String STATUS_PARAM_INVALID_MESSAGE = "Please check status param passed as this is invalid status.";

    private Map<String, OrganisationMinimalInfoResponse> activeOrgs;
    private Map<String, OrganisationMinimalInfoResponse> pendingOrgs;

    private OrganisationMinimalInfoResponse organisationEntityResponse1;
    private OrganisationMinimalInfoResponse organisationEntityResponse2;
    private OrganisationMinimalInfoResponse organisationEntityResponse3;
    private OrganisationMinimalInfoResponse organisationEntityResponse4;
    private String orgIdentifier1;
    private String orgIdentifier2;
    private String orgIdentifier3;
    private String orgIdentifier4;
    private List<String> validRoles;

    @Test
    @SuppressWarnings("unchecked")
    //AC:1
    public void should_retrieve_organisations_info_with_200_with_correct_roles_and_status_active() {

        setUpTestData();
        List<OrganisationMinimalInfoResponse> responseList = (List<OrganisationMinimalInfoResponse>)professionalApiClient.retrieveAllActiveOrganisationsWithMinimalInfo(
                bearerToken, HttpStatus.OK, IdamStatus.ACTIVE.toString());
        assertThat(responseList).usingFieldByFieldElementComparator().contains(activeOrgs.get(orgIdentifier1), activeOrgs.get(orgIdentifier2));
        assertThat(responseList).usingFieldByFieldElementComparator().doesNotContain(pendingOrgs.get(orgIdentifier3), pendingOrgs.get(orgIdentifier4));

    }

    @Test
    //AC:3
    public void should_fail_to_retrieve_organisations_info_with_403_with_incorrect_roles_and_status_active() {

        // invite new user having invalid roles
        List<String> userRoles = new ArrayList<>();
        userRoles.add("caseworker");
        inviteNewUser(userRoles);
        validateErrorResponse((ErrorResponse) professionalApiClient.retrieveAllActiveOrganisationsWithMinimalInfo(bearerToken, HttpStatus.FORBIDDEN, ACTIVE.toString()),
                ACCESS_EXCEPTION.getErrorMessage(), ACCESS_IS_DENIED_ERROR_MESSAGE);
    }

    @Test
    //AC:5
    public void should_fail_to_retrieve_organisations_info_with_404_with_correct_roles_and_status_pending() {

        // invite new user having valid roles and also make it active
        inviteNewUser(getValidRoleList());
        validateErrorResponse((ErrorResponse) professionalApiClient.retrieveAllActiveOrganisationsWithMinimalInfo(bearerToken, HttpStatus.NOT_FOUND, PENDING.toString()),
                EMPTY_RESULT_DATA_ACCESS.getErrorMessage(), STATUS_PARAM_INVALID_MESSAGE);
    }

    @Test
    //AC:6
    public void should_fail_to_retrieve_organisations_info_with_404_with_correct_roles_and_status_not_passed() {

        // invite new user having valid roles and also make it active
        inviteNewUser(getValidRoleList());
        validateErrorResponse((ErrorResponse) professionalApiClient.retrieveAllActiveOrganisationsWithMinimalInfo(bearerToken, HttpStatus.NOT_FOUND, null),
                EMPTY_RESULT_DATA_ACCESS.getErrorMessage(), STATUS_PARAM_INVALID_MESSAGE);
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
        inviteNewUser(getValidRoleList());

    }

    public void inviteNewUser(List<String> roles) {
        IdamOpenIdClient idamOpenIdClient = new IdamOpenIdClient(configProperties);
        String email = idamOpenIdClient.nextUserEmail();
        NewUserCreationRequest newUserCreationRequest = professionalApiClient.createNewUserRequest();
        newUserCreationRequest.setEmail(email);
        newUserCreationRequest.setRoles(roles);

        idamOpenIdClient.createUser(hmctsAdmin, email, newUserCreationRequest.getFirstName(), newUserCreationRequest.getLastName());
        bearerToken = professionalApiClient.getMultipleAuthHeaders(idamOpenIdClient.getOpenIdToken(email));

        professionalApiClient.addNewUserToAnOrganisation(orgIdentifier1 == null ? createActiveOrganisation1() : orgIdentifier1, hmctsAdmin, newUserCreationRequest, HttpStatus.CREATED);

    }

    public List<String> getValidRoleList() {
        if (CollectionUtils.isEmpty(validRoles)) {
            validRoles = new ArrayList<>();
            validRoles.add(puiUserManager);
            validRoles.add(puiCaseManager);
            validRoles.add(puiOrgManager);
            validRoles.add(puiFinanceManager);
            validRoles.add(puiCaa);
        }
        return validRoles;
    }

    public String createActiveOrganisation1() {
        String orgName1 = randomAlphabetic(7);
        orgIdentifier1 = createAndctivateOrganisationWithGivenRequest(createMinimalOrganisationRequest(orgName1), hmctsAdmin);
        organisationEntityResponse1 = new OrganisationMinimalInfoResponse(orgName1, orgIdentifier1);
        return orgIdentifier1;
    }

    public String createActiveOrganisation2() {
        String orgName2 = randomAlphabetic(7);
        orgIdentifier2 = createAndctivateOrganisationWithGivenRequest(createMinimalOrganisationRequest(orgName2), hmctsAdmin);
        organisationEntityResponse2 = new OrganisationMinimalInfoResponse(orgName2, orgIdentifier2);
        return orgIdentifier2;
    }

    public void createPendingOrganisation1() {
        String orgName3 = randomAlphabetic(7);
        orgIdentifier3 = createOrganisation(orgName3);
        organisationEntityResponse3 = new OrganisationMinimalInfoResponse(orgName3, orgIdentifier3);
    }

    public void createPendingOrganisation2() {
        String orgName4 = randomAlphabetic(7);
        orgIdentifier4 = createOrganisation(orgName4);
        organisationEntityResponse4 = new OrganisationMinimalInfoResponse(orgName4, orgIdentifier4);
    }

    public String createOrganisation(String orgName) {
        return (String)professionalApiClient.createOrganisation(createMinimalOrganisationRequest(orgName)).get("organisationIdentifier");
    }

    public OrganisationCreationRequest createMinimalOrganisationRequest(String organisationName) {
        return someMinimalOrganisationRequest().name(organisationName).status(ACTIVE.name()).sraId(randomAlphabetic(10)).build();
    }

    public void validateErrorResponse(ErrorResponse errorResponse, String expectedErrorMessage, String expectedErrorDescription) {
        assertThat(errorResponse.getErrorDescription()).isEqualTo(expectedErrorDescription);
        assertThat(errorResponse.getErrorMessage()).isEqualTo(expectedErrorMessage);
    }


}
