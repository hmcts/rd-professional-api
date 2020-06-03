package uk.gov.hmcts.reform.professionalapi;

import static org.apache.commons.lang.RandomStringUtils.randomAlphabetic;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.professionalapi.helper.OrganisationFixtures.organisationRequestWithAllFieldsAreUpdated;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.reform.professionalapi.controller.advice.ErrorResponse;
import uk.gov.hmcts.reform.professionalapi.controller.constants.IdamStatus;
import uk.gov.hmcts.reform.professionalapi.controller.request.NewUserCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.response.OrganisationMinimalInfoResponse;
import uk.gov.hmcts.reform.professionalapi.util.AuthorizationEnabledIntegrationTest;

@SuppressWarnings("unchecked")
public class RetrieveMinimalOrganisationsInfoIntegrationTest extends AuthorizationEnabledIntegrationTest {

    Map<String, OrganisationMinimalInfoResponse> activeOrgs;
    Map<String, OrganisationMinimalInfoResponse> pendingOrgs;

    OrganisationMinimalInfoResponse organisationEntityResponse1;
    OrganisationMinimalInfoResponse organisationEntityResponse2;
    OrganisationMinimalInfoResponse organisationEntityResponse3;
    OrganisationMinimalInfoResponse organisationEntityResponse4;
    String orgIdentifier1;
    String orgIdentifier2;
    String orgIdentifier3;
    String orgIdentifier4;
    List<String> validRoles;
    String userIdentifier;

    @Before
    public void setUpTestData(boolean requiredAllOrgsToCreate) {

        if (requiredAllOrgsToCreate) {
            activeOrgs = new HashMap<>();
            createActiveOrganisation1();
            createActiveOrganisation2();
            pendingOrgs = new HashMap<>();
            createPendingOrganisation1();
            createPendingOrganisation2();
        }

        NewUserCreationRequest newUserCreationRequest = inviteUserCreationRequest("some@somedomain.com", getValidRoleList());
        Map<String, Object> newUserResponse = professionalReferenceDataClient.addUserToOrganisation(createActiveOrganisation1(), newUserCreationRequest, hmctsAdmin);
        userIdentifier = (String)newUserResponse.get("userIdentifier");
    }

    @Test
    //AC:1
    public void should_retrieve_organisations_info_with_200_with_correct_roles_and_status_active() {

        setUpTestData(true);
        List<OrganisationMinimalInfoResponse> responseList = (List<OrganisationMinimalInfoResponse>)professionalReferenceDataClient.retrieveOrganisationsWithMinimalInfo(
                userIdentifier, "pui-caa", IdamStatus.ACTIVE.toString());
        assertThat(responseList).contains(activeOrgs.get(orgIdentifier1), activeOrgs.get(orgIdentifier2));
        assertThat(responseList).contains(pendingOrgs.get(orgIdentifier2), activeOrgs.get(orgIdentifier3));
    }

    @Test
    //AC:3
    public void should_fail_to_retrieve_organisations_info_with_403_with_incorrect_roles_and_status_active() {
        setUpTestData(false);
        Map<String, Object> errorResponseMap = (Map<String, Object>) professionalReferenceDataClient.retrieveOrganisationsWithMinimalInfo(
                userIdentifier, "pui-caa-manager", IdamStatus.ACTIVE.toString());
        validateErrorResponse(errorResponseMap, HttpStatus.FORBIDDEN, "expectedErrorMessage", "expectedErrorDescription");
    }

    @Test
    //AC:5
    public void should_fail_to_retrieve_organisations_info_with_404_with_correct_roles_and_status_pending() {
        setUpTestData(false);
        Map<String, Object> errorResponseMap = (Map<String, Object>) professionalReferenceDataClient.retrieveOrganisationsWithMinimalInfo(
                userIdentifier, "pui-caa", IdamStatus.PENDING.toString());
        validateErrorResponse(errorResponseMap, HttpStatus.NOT_FOUND, "expectedErrorMessage", "expectedErrorDescription");
    }

    @Test
    //AC:6
    public void should_fail_to_retrieve_organisations_info_with_404_with_correct_roles_and_status_not_passed() {
        setUpTestData(false);
        Map<String, Object> errorResponseMap = (Map<String,Object>) professionalReferenceDataClient.retrieveOrganisationsWithMinimalInfo(
                userIdentifier, "pui-caa", null);
        validateErrorResponse(errorResponseMap, HttpStatus.NOT_FOUND, "expectedErrorMessage", "expectedErrorDescription");
    }

    public void validateErrorResponse(Map<String, Object> errorResponseMap, HttpStatus expectedStatus, String expectedErrorMessage, String expectedErrorDescription) {
        assertThat(errorResponseMap.get("http_status")).isEqualTo(expectedStatus);
        ErrorResponse errorResponse = (ErrorResponse)errorResponseMap.get("response_body");
        assertThat(errorResponse.getErrorDescription()).isEqualTo(expectedErrorDescription);
        assertThat(errorResponse.getErrorMessage()).isEqualTo(expectedErrorMessage);
    }


    public String createActiveOrganisation1() {
        String orgName1 = randomAlphabetic(7);
        orgIdentifier1 = createAndActivateOrganisationWithGivenRequest(organisationRequestWithAllFieldsAreUpdated().name(orgName1).build());
        organisationEntityResponse1 = new OrganisationMinimalInfoResponse(orgIdentifier1, orgName1);
        activeOrgs.put(orgIdentifier1, organisationEntityResponse1);
        return orgIdentifier1;
    }

    public String createActiveOrganisation2() {
        String orgName2 = randomAlphabetic(7);
        orgIdentifier2 = createAndActivateOrganisationWithGivenRequest(organisationRequestWithAllFieldsAreUpdated().name(orgName2).build());
        organisationEntityResponse2 = new OrganisationMinimalInfoResponse(orgIdentifier2, orgName2);
        activeOrgs.put(orgIdentifier2, organisationEntityResponse2);
        return orgIdentifier2;
    }

    public void createPendingOrganisation1() {
        String orgName3 = randomAlphabetic(7);
        orgIdentifier3 = createOrganisationRequestWithRequest(organisationRequestWithAllFieldsAreUpdated().name(orgName3).build());
        organisationEntityResponse3 = new OrganisationMinimalInfoResponse(orgIdentifier3, orgName3);
        pendingOrgs.put(orgIdentifier3, organisationEntityResponse3);
    }

    public void createPendingOrganisation2() {
        String orgName4 = randomAlphabetic(7);
        orgIdentifier4 = createOrganisationRequestWithRequest(organisationRequestWithAllFieldsAreUpdated().name(orgName4).build());
        organisationEntityResponse4 = new OrganisationMinimalInfoResponse(orgIdentifier4, orgName4);
        pendingOrgs.put(orgIdentifier4, organisationEntityResponse4);
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


}
