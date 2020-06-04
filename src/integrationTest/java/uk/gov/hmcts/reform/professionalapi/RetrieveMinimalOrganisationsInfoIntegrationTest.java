package uk.gov.hmcts.reform.professionalapi;

import static org.apache.commons.lang.RandomStringUtils.randomAlphabetic;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.professionalapi.helper.OrganisationFixtures.someMinimalOrganisationRequest;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.reform.professionalapi.controller.advice.ErrorResponse;
import uk.gov.hmcts.reform.professionalapi.controller.request.NewUserCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.response.OrganisationMinimalInfoResponse;
import uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus;
import uk.gov.hmcts.reform.professionalapi.util.AuthorizationEnabledIntegrationTest;

@SuppressWarnings("unchecked")
public class RetrieveMinimalOrganisationsInfoIntegrationTest extends AuthorizationEnabledIntegrationTest {

    Map<String, OrganisationMinimalInfoResponse> activeOrgs = new HashMap<>();
    Map<String, OrganisationMinimalInfoResponse> pendingOrgs = new HashMap<>();

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

    public void setUpTestData() {
        createActiveOrganisation1();
        createActiveOrganisation2();
        createPendingOrganisation1();
        createPendingOrganisation2();
        inviteUser(true);
    }

    public void inviteUser(boolean useExistingOrg) {

        if (!useExistingOrg) {
            createActiveOrganisation1();
        }
        userProfileCreateUserWireMock(HttpStatus.CREATED);
        NewUserCreationRequest newUserCreationRequest = inviteUserCreationRequest(randomAlphabetic(5) + "@somedomain.com", getValidRoleList());
        Map<String, Object> newUserResponse = professionalReferenceDataClient.addUserToOrganisation(orgIdentifier1, newUserCreationRequest, hmctsAdmin);
        userIdentifier = (String)newUserResponse.get("userIdentifier");
    }

    @Test
    //AC:1
    public void should_retrieve_organisations_info_with_200_with_correct_roles_and_status_active() throws JsonProcessingException {

        setUpTestData();
        List<OrganisationMinimalInfoResponse> responseList = (List<OrganisationMinimalInfoResponse>)professionalReferenceDataClient.retrieveOrganisationsWithMinimalInfo(
                userIdentifier, "pui-caa", OrganisationStatus.ACTIVE.toString(), OrganisationMinimalInfoResponse[].class);
        assertThat(responseList).usingFieldByFieldElementComparator().contains(activeOrgs.get(orgIdentifier1), activeOrgs.get(orgIdentifier2));
        assertThat(responseList).usingFieldByFieldElementComparator().doesNotContain(activeOrgs.get(orgIdentifier3), activeOrgs.get(orgIdentifier4));
    }

    @Test
    //AC:3
    public void should_fail_to_retrieve_organisations_info_with_403_with_incorrect_roles_and_status_active() throws JsonProcessingException {
        inviteUser(false);
        Map<String, Object> errorResponseMap = (Map<String, Object>) professionalReferenceDataClient.retrieveOrganisationsWithMinimalInfo(
                userIdentifier, "pui-caa-manager", OrganisationStatus.ACTIVE.toString(), ErrorResponse.class);
        validateErrorResponse(errorResponseMap, HttpStatus.FORBIDDEN, "9 : Access Denied", "Access is denied");
    }

    @Test
    //AC:5
    public void should_fail_to_retrieve_organisations_info_with_404_with_correct_roles_and_status_pending() throws JsonProcessingException {
        inviteUser(false);
        Map<String, Object> errorResponseMap = (Map<String, Object>) professionalReferenceDataClient.retrieveOrganisationsWithMinimalInfo(
                userIdentifier, "pui-caa", OrganisationStatus.PENDING.toString(), ErrorResponse.class);
        validateErrorResponse(errorResponseMap, HttpStatus.NOT_FOUND, "4 : Resource not found", "Please check status param passed as this is invalid status.");
    }

    @Test
    //AC:6
    public void should_fail_to_retrieve_organisations_info_with_404_with_correct_roles_and_status_not_passed() throws JsonProcessingException {
        inviteUser(false);
        Map<String, Object> errorResponseMap = (Map<String,Object>) professionalReferenceDataClient.retrieveOrganisationsWithMinimalInfo(
                userIdentifier, "pui-caa", null, ErrorResponse.class);
        validateErrorResponse(errorResponseMap, HttpStatus.NOT_FOUND, "4 : Resource not found", "Please check status param passed as this is invalid status.");
    }

    public void validateErrorResponse(Map<String, Object> errorResponseMap, HttpStatus expectedStatus, String expectedErrorMessage, String expectedErrorDescription) {
        assertThat(errorResponseMap.get("http_status")).isEqualTo(expectedStatus);
        ErrorResponse errorResponse = (ErrorResponse)errorResponseMap.get("response_body");
        assertThat(errorResponse.getErrorDescription()).isEqualTo(expectedErrorDescription);
        assertThat(errorResponse.getErrorMessage()).isEqualTo(expectedErrorMessage);
    }


    public String createActiveOrganisation1() {
        userProfileCreateUserWireMock(HttpStatus.CREATED);
        String orgName1 = randomAlphabetic(7);
        orgIdentifier1 = createAndActivateOrganisationWithGivenRequest(someMinimalOrganisationRequest().name(orgName1).sraId(randomAlphabetic(10)).build());
        organisationEntityResponse1 = new OrganisationMinimalInfoResponse(orgIdentifier1, orgName1);
        activeOrgs.put(orgIdentifier1, organisationEntityResponse1);
        return orgIdentifier1;
    }

    public String createActiveOrganisation2() {
        userProfileCreateUserWireMock(HttpStatus.CREATED);
        String orgName2 = randomAlphabetic(7);
        orgIdentifier2 = createAndActivateOrganisationWithGivenRequest(someMinimalOrganisationRequest().name(orgName2).sraId(randomAlphabetic(10)).build());
        organisationEntityResponse2 = new OrganisationMinimalInfoResponse(orgIdentifier2, orgName2);
        activeOrgs.put(orgIdentifier2, organisationEntityResponse2);
        return orgIdentifier2;
    }

    public void createPendingOrganisation1() {
        String orgName3 = randomAlphabetic(7);
        orgIdentifier3 = createOrganisationRequestWithRequest(someMinimalOrganisationRequest().name(orgName3).sraId(randomAlphabetic(10)).build());
        organisationEntityResponse3 = new OrganisationMinimalInfoResponse(orgIdentifier3, orgName3);
        pendingOrgs.put(orgIdentifier3, organisationEntityResponse3);
    }

    public void createPendingOrganisation2() {
        String orgName4 = randomAlphabetic(7);
        orgIdentifier4 = createOrganisationRequestWithRequest(someMinimalOrganisationRequest().name(orgName4).sraId(randomAlphabetic(10)).build());
        organisationEntityResponse4 = new OrganisationMinimalInfoResponse(orgIdentifier4, orgName4);
        pendingOrgs.put(orgIdentifier4, organisationEntityResponse4);
    }

    public List<String> getValidRoleList() {
        if (CollectionUtils.isEmpty(validRoles)) {
            validRoles = new ArrayList<>();
            validRoles.add("pui-user-manager");
            validRoles.add("pui-case-manager");
            validRoles.add("pui-organisation-manager");
            validRoles.add("pui-finance-manager");
            validRoles.add("pui-caa");
        }
        return validRoles;
    }


}
