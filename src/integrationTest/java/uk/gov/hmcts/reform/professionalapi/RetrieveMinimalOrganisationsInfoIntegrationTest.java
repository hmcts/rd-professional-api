package uk.gov.hmcts.reform.professionalapi;

import static org.apache.commons.lang.RandomStringUtils.randomAlphabetic;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ErrorConstants.ACCESS_EXCEPTION;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ErrorConstants.EMPTY_RESULT_DATA_ACCESS;
import static uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus.PENDING;
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
import uk.gov.hmcts.reform.professionalapi.util.AuthorizationEnabledIntegrationTest;

@SuppressWarnings("unchecked")
public class RetrieveMinimalOrganisationsInfoIntegrationTest extends AuthorizationEnabledIntegrationTest {

    private static final String STATUS_PARAM_INVALID_MESSAGE =
            "Please check status param passed as this is invalid status.";
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

    @Test
    //AC:1
    public void should_retrieve_organisations_info_with_200_with_correct_roles_and_status_active()
            throws JsonProcessingException {

        setUpTestData();
        List<OrganisationMinimalInfoResponse> responseList = (List<OrganisationMinimalInfoResponse>)
                professionalReferenceDataClient.retrieveOrganisationsWithMinimalInfo(
                userIdentifier, puiCaa, ACTIVE, OrganisationMinimalInfoResponse[].class);
        assertThat(responseList).usingFieldByFieldElementComparator().contains(activeOrgs.get(orgIdentifier1),
                activeOrgs.get(orgIdentifier2));
        assertThat(responseList).usingFieldByFieldElementComparator().doesNotContain(activeOrgs.get(orgIdentifier3),
                activeOrgs.get(orgIdentifier4));
    }

    @Test
    //AC:2
    public void shouldFailTo_retrieve_orgInfo_with403_withCorrectRoles_andStatusActive_andPendingCallerUser()
            throws JsonProcessingException {
        inviteUser(false);
        getUserProfileByEmailWireMock(HttpStatus.OK);
        Map<String, Object> errorResponseMap = (Map<String, Object>) professionalReferenceDataClient
                .retrieveOrganisationsWithMinimalInfo(userIdentifier, puiCaa, ACTIVE, ErrorResponse.class);
        validateErrorResponse(errorResponseMap, HttpStatus.FORBIDDEN, ACCESS_EXCEPTION.getErrorMessage(),
                STATUS_MUST_BE_ACTIVE_ERROR_MESSAGE);
    }

    @Test
    //AC:3
    public void shouldFailTo_retrieve_orgInfo_with403_withIncorrect_roles_and_status_active()
            throws JsonProcessingException {
        inviteUser(false);
        Map<String, Object> errorResponseMap = (Map<String, Object>) professionalReferenceDataClient
                .retrieveOrganisationsWithMinimalInfo(userIdentifier, "pui-invalid-role",
                        ACTIVE, ErrorResponse.class);
        validateErrorResponse(errorResponseMap, HttpStatus.FORBIDDEN, ACCESS_EXCEPTION.getErrorMessage(),
                ACCESS_IS_DENIED_ERROR_MESSAGE);
    }

    @Test
    //AC:5
    public void should_fail_to_retrieve_organisations_info_with_404_with_correct_roles_and_status_pending()
            throws JsonProcessingException {
        inviteUser(false);
        Map<String, Object> errorResponseMap = (Map<String, Object>) professionalReferenceDataClient
                .retrieveOrganisationsWithMinimalInfo(userIdentifier, puiCaa, PENDING.toString(), ErrorResponse.class);
        validateErrorResponse(errorResponseMap, HttpStatus.NOT_FOUND, EMPTY_RESULT_DATA_ACCESS.getErrorMessage(),
                STATUS_PARAM_INVALID_MESSAGE);
    }

    @Test
    //AC:6
    public void should_fail_to_retrieve_organisations_info_with_404_with_correct_roles_and_status_not_passed()
            throws JsonProcessingException {
        inviteUser(false);
        Map<String, Object> errorResponseMap = (Map<String,Object>) professionalReferenceDataClient
                .retrieveOrganisationsWithMinimalInfo(userIdentifier, puiCaa, null, ErrorResponse.class);
        validateErrorResponse(errorResponseMap, HttpStatus.NOT_FOUND, EMPTY_RESULT_DATA_ACCESS.getErrorMessage(),
                STATUS_PARAM_INVALID_MESSAGE);
    }

    public void validateErrorResponse(Map<String, Object> errorResponseMap, HttpStatus expectedStatus,
                                      String expectedErrorMessage, String expectedErrorDescription) {
        assertThat(errorResponseMap.get("http_status")).isEqualTo(expectedStatus);
        ErrorResponse errorResponse = (ErrorResponse)errorResponseMap.get("response_body");
        assertThat(errorResponse.getErrorDescription()).isEqualTo(expectedErrorDescription);
        assertThat(errorResponse.getErrorMessage()).isEqualTo(expectedErrorMessage);
    }

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
        NewUserCreationRequest newUserCreationRequest = inviteUserCreationRequest(
                randomAlphabetic(5) + "@somedomain.com", getValidRoleList());
        Map<String, Object> newUserResponse = professionalReferenceDataClient.addUserToOrganisation(orgIdentifier1,
                newUserCreationRequest, hmctsAdmin);
        userIdentifier = (String)newUserResponse.get("userIdentifier");
    }

    public String createActiveOrganisation1() {
        userProfileCreateUserWireMock(HttpStatus.CREATED);
        String orgName1 = randomAlphabetic(7);
        orgIdentifier1 = createAndActivateOrganisationWithGivenRequest(
                someMinimalOrganisationRequest().name(orgName1).sraId(randomAlphabetic(10)).build());
        organisationEntityResponse1 = new OrganisationMinimalInfoResponse(orgName1, orgIdentifier1);
        activeOrgs.put(orgIdentifier1, organisationEntityResponse1);
        return orgIdentifier1;
    }

    public String createActiveOrganisation2() {
        userProfileCreateUserWireMock(HttpStatus.CREATED);
        String orgName2 = randomAlphabetic(7);
        orgIdentifier2 = createAndActivateOrganisationWithGivenRequest(
                someMinimalOrganisationRequest().name(orgName2).sraId(randomAlphabetic(10)).build());
        organisationEntityResponse2 = new OrganisationMinimalInfoResponse(orgName2, orgIdentifier2);
        activeOrgs.put(orgIdentifier2, organisationEntityResponse2);
        return orgIdentifier2;
    }

    public void createPendingOrganisation1() {
        String orgName3 = randomAlphabetic(7);
        orgIdentifier3 = createOrganisationRequestWithRequest(
                someMinimalOrganisationRequest().name(orgName3).sraId(randomAlphabetic(10)).build());
        organisationEntityResponse3 = new OrganisationMinimalInfoResponse(orgName3, orgIdentifier3);
        pendingOrgs.put(orgIdentifier3, organisationEntityResponse3);
    }

    public void createPendingOrganisation2() {
        String orgName4 = randomAlphabetic(7);
        orgIdentifier4 = createOrganisationRequestWithRequest(
                someMinimalOrganisationRequest().name(orgName4).sraId(randomAlphabetic(10)).build());
        organisationEntityResponse4 = new OrganisationMinimalInfoResponse(orgName4, orgIdentifier4);
        pendingOrgs.put(orgIdentifier4, organisationEntityResponse4);
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


}
