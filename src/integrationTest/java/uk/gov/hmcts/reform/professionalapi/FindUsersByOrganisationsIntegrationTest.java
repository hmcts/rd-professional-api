package uk.gov.hmcts.reform.professionalapi;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.professionalapi.controller.advice.ErrorResponse;
import uk.gov.hmcts.reform.professionalapi.controller.constants.IdamStatus;
import uk.gov.hmcts.reform.professionalapi.controller.request.NewUserCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrgAttributeRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationOtherOrgsCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.UsersInOrganisationsByOrganisationIdentifiersRequest;
import uk.gov.hmcts.reform.professionalapi.controller.response.OrganisationInfoWithUsersResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.ProfessionalUsersResponseWithoutRoles;
import uk.gov.hmcts.reform.professionalapi.controller.response.UsersInOrganisationsByOrganisationIdentifiersResponse;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;
import uk.gov.hmcts.reform.professionalapi.repository.OrganisationRepository;
import uk.gov.hmcts.reform.professionalapi.util.AuthorizationEnabledIntegrationTest;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.professionalapi.controller.request.ContactInformationCreationRequest.aContactInformationCreationRequest;
import static uk.gov.hmcts.reform.professionalapi.controller.request.DxAddressCreationRequest.dxAddressCreationRequest;
import static uk.gov.hmcts.reform.professionalapi.controller.request.UserCreationRequest.aUserCreationRequest;

public class FindUsersByOrganisationsIntegrationTest extends AuthorizationEnabledIntegrationTest {

    @Autowired
    private OrganisationRepository organisationRepository;

    private String organisationIdentifier1;
    private String organisationIdentifier2;
    private String organisationIdentifier3;
    private Map<String, List<String>> usersInOrganisation = new HashMap<>();

    @BeforeEach
    public void setup() {
        professionalUserRepository.deleteAll();
        organisationRepository.deleteAll();
        String solicitorOrgType = "SOLICITOR-ORG";

        // create organisation 1 with 1 superuser, 2 active users and 1 deleted user
        OrganisationOtherOrgsCreationRequest newOrgRequest1 = createUniqueOrganisationRequest("TstSO1", "SRA123",
                "PBA1234561", "super-email1@gmail.com", solicitorOrgType);
        organisationIdentifier1 = createAndActivateOrganisationWithGivenRequest(newOrgRequest1);

        userProfileCreateUserWireMock(HttpStatus.CREATED);
        String userIdentifier1Org1 = addAUserToOrganisation("user1.org1@test.com", organisationIdentifier1);

        userProfileCreateUserWireMock(HttpStatus.CREATED);
        String userIdentifier2Org1 = addAUserToOrganisation("user2.org1@test.com", organisationIdentifier1);


        // create organisation 2 with 1 superuser and 2 active users
        OrganisationOtherOrgsCreationRequest newOrgRequest2 = createUniqueOrganisationRequest("TstSO2", "SRA234",
                "PBA2345678", "super-email2@gmail.com", solicitorOrgType);
        organisationIdentifier2 = createAndActivateOrganisationWithGivenRequest(newOrgRequest2);

        userProfileCreateUserWireMock(HttpStatus.CREATED);
        String userIdentifier1Org2 = addAUserToOrganisation("user1.org2@test.com", organisationIdentifier2);

        userProfileCreateUserWireMock(HttpStatus.CREATED);
        String userIdentifier2Org2 = addAUserToOrganisation("user2.org2@test.com", organisationIdentifier2);

        usersInOrganisation.put(organisationIdentifier2, Arrays.asList(userIdentifier1Org2, userIdentifier2Org2));

        // create organisation 3 with 1 superuser and 2 active users
        OrganisationOtherOrgsCreationRequest newOrgRequest3 = createUniqueOrganisationRequest("TstSO3", "SRA345",
                "PBA3456789", "super-email3@gmail.com", solicitorOrgType);
        organisationIdentifier3 = createAndActivateOrganisationWithGivenRequest(newOrgRequest3);

        userProfileCreateUserWireMock(HttpStatus.CREATED);
        String userIdentifier1Org3 = addAUserToOrganisation("user1.org3@test.com", organisationIdentifier3);

        userProfileCreateUserWireMock(HttpStatus.CREATED);
        String userIdentifier2Org3 = addAUserToOrganisation("user2.org3@test.com", organisationIdentifier3);

        usersInOrganisation.put(organisationIdentifier3, Arrays.asList(userIdentifier1Org3, userIdentifier2Org3));

        userProfileCreateUserWireMock(HttpStatus.CREATED);
        String userIdentifier3Org1 = addAUserToOrganisation("user3.org1@test.com", organisationIdentifier1);

        // add in a random order to check if the order is maintained
        ProfessionalUser user = professionalUserRepository.findByUserIdentifier(userIdentifier3Org1);
        user.setDeleted(LocalDateTime.now());
        user.setIdamStatus(IdamStatus.SUSPENDED);
        professionalUserRepository.save(user);

        usersInOrganisation.put(organisationIdentifier1, Arrays.asList(userIdentifier1Org1, userIdentifier2Org1,
                userIdentifier3Org1));
    }

    @Test
    void when_no_org_filter_is_provided_including_deleted_return_all_users_for_all_orgs_and_status_200() {
        // arrange
        UsersInOrganisationsByOrganisationIdentifiersRequest request =
                new UsersInOrganisationsByOrganisationIdentifiersRequest();
        boolean showDeleted = true;
        Integer pageSize = null;
        UUID searchAfterUser = null;
        UUID searchAfterOrganisation = null;

        String expectedStatus = "200 OK";
        boolean expectedHasMoreRecords = false;
        int expectedOrganisationsCount = 3;
        int expectedUsersCount = 10;

        // act
        Map<String, Object> response =
                professionalReferenceDataClient.retrieveUsersInOrganisationsByOrganisationIdentifiers(request,
                        showDeleted, pageSize, searchAfterUser, searchAfterOrganisation);

        // assert
        assertSuccessfulResponse(response, expectedOrganisationsCount, expectedUsersCount, expectedStatus,
                expectedHasMoreRecords);
    }

    @Test
    void when_no_org_filter_is_provided_excluding_deleted_return_all_non_deleted_users_for_all_orgs_and_status_200() {
        // arrange
        UsersInOrganisationsByOrganisationIdentifiersRequest request =
                new UsersInOrganisationsByOrganisationIdentifiersRequest();
        boolean showDeleted = false;
        Integer pageSize = null;
        UUID searchAfterUser = null;
        UUID searchAfterOrganisation = null;

        String expectedStatus = "200 OK";
        boolean expectedHasMoreRecords = false;
        int expectedOrganisationsCount = 3;
        int expectedUsersCount = 9;

        // act
        Map<String, Object> response =
                professionalReferenceDataClient.retrieveUsersInOrganisationsByOrganisationIdentifiers(request,
                        showDeleted, pageSize, searchAfterUser, searchAfterOrganisation);

        // assert
        assertSuccessfulResponse(response, expectedOrganisationsCount, expectedUsersCount, expectedStatus,
                expectedHasMoreRecords);
    }

    @Test
    void when_org_filter_is_provided_including_deleted_return_all_users_for_given_orgs_and_status_200() {
        // arrange
        UsersInOrganisationsByOrganisationIdentifiersRequest request =
                new UsersInOrganisationsByOrganisationIdentifiersRequest();
        request.setOrganisationIdentifiers(Arrays.asList(organisationIdentifier1, organisationIdentifier2));
        boolean showDeleted = true;
        Integer pageSize = null;
        UUID searchAfterUser = null;
        UUID searchAfterOrganisation = null;

        String expectedStatus = "200 OK";
        boolean expectedHasMoreRecords = false;
        int expectedOrganisationsCount = 2;
        int expectedUsersCount = 6;

        // act
        Map<String, Object> response =
                professionalReferenceDataClient.retrieveUsersInOrganisationsByOrganisationIdentifiers(request,
                        showDeleted, pageSize, searchAfterUser, searchAfterOrganisation);

        // assert
        assertSuccessfulResponse(response, expectedOrganisationsCount, expectedUsersCount, expectedStatus,
                expectedHasMoreRecords, usersInOrganisation.get(organisationIdentifier3));
    }

    @Test
    void when_org_filter_is_provided_excluding_deleted_return_all_users_for_given_orgs_and_status_200() {
        // arrange
        UsersInOrganisationsByOrganisationIdentifiersRequest request =
                new UsersInOrganisationsByOrganisationIdentifiersRequest();
        request.setOrganisationIdentifiers(Arrays.asList(organisationIdentifier1, organisationIdentifier2));
        boolean showDeleted = false;
        Integer pageSize = null;
        UUID searchAfterUser = null;
        UUID searchAfterOrganisation = null;

        String expectedStatus = "200 OK";
        boolean expectedHasMoreRecords = false;
        int expectedOrganisationsCount = 2;
        int expectedUsersCount = 6;

        // act
        Map<String, Object> response =
                professionalReferenceDataClient.retrieveUsersInOrganisationsByOrganisationIdentifiers(request,
                        showDeleted, pageSize, searchAfterUser, searchAfterOrganisation);

        // assert
        assertSuccessfulResponse(response, expectedOrganisationsCount, expectedUsersCount, expectedStatus,
                expectedHasMoreRecords, usersInOrganisation.get(organisationIdentifier3));
    }

    @Test
    void when_paged_no_org_filter_is_provided_including_deleted_return_paged_users_for_all_orgs_and_status_200() {
        // arrange
        UsersInOrganisationsByOrganisationIdentifiersRequest request =
                new UsersInOrganisationsByOrganisationIdentifiersRequest();
        boolean showDeleted = true;
        Integer pageSize = 3;
        UUID searchAfterUser = null;
        UUID searchAfterOrganisation = null;

        String expectedStatus = "200 OK";
        boolean expectedHasMoreRecords = true;
        int expectedOrganisationsCount = 1;
        int expectedUsersCount = 3;

        // todo: how do we predict the order of orgs and users to better assert?
        // act
        Map<String, Object> response =
                professionalReferenceDataClient.retrieveUsersInOrganisationsByOrganisationIdentifiers(request,
                        showDeleted, pageSize, searchAfterUser, searchAfterOrganisation);

        // assert
        assertSuccessfulResponse(response, expectedOrganisationsCount, expectedUsersCount, expectedStatus,
                expectedHasMoreRecords);
    }

    @Test
    void when_paged_no_org_filter_is_provided_exclude_deleted_return_paged_all_non_deleted_users_for_all_orgs_and_status_200() {
        // arrange
        UsersInOrganisationsByOrganisationIdentifiersRequest request =
                new UsersInOrganisationsByOrganisationIdentifiersRequest();
        boolean showDeleted = false;
        Integer pageSize = 4;
        UUID searchAfterUser = null;
        UUID searchAfterOrganisation = null;

        String expectedStatus = "200 OK";
        boolean expectedHasMoreRecords = true;
        int expectedOrganisationsCount = 2; // no org has 4 active users
        int expectedUsersCount = 4;

        // act
        Map<String, Object> response =
                professionalReferenceDataClient.retrieveUsersInOrganisationsByOrganisationIdentifiers(request,
                        showDeleted, pageSize, searchAfterUser, searchAfterOrganisation);

        // assert
        assertSuccessfulResponse(response, expectedOrganisationsCount, expectedUsersCount, expectedStatus,
                expectedHasMoreRecords);
    }

    @Test
    void when_paged_org_filter_is_provided_including_deleted_return_all_users_for_given_orgs_and_status_200() {
        // arrange
        UsersInOrganisationsByOrganisationIdentifiersRequest request =
                new UsersInOrganisationsByOrganisationIdentifiersRequest();
        request.setOrganisationIdentifiers(Arrays.asList(organisationIdentifier1, organisationIdentifier2));
        boolean showDeleted = true;
        Integer pageSize = 4;
        UUID searchAfterUser = null;
        UUID searchAfterOrganisation = null;

        String expectedStatus = "200 OK";
        boolean expectedHasMoreRecords = true;
        int expectedOrganisationsCount = 2; // can we predict the order of orgs or stick to one org in the filter?
        int expectedUsersCount = 4;

        // act
        Map<String, Object> response =
                professionalReferenceDataClient.retrieveUsersInOrganisationsByOrganisationIdentifiers(request,
                        showDeleted, pageSize, searchAfterUser, searchAfterOrganisation);

        // assert
        assertSuccessfulResponse(response, expectedOrganisationsCount, expectedUsersCount, expectedStatus,
                expectedHasMoreRecords);
    }

    @Test
    void when_paged_org_filter_is_provided_excluding_deleted_return_all_users_for_given_orgs_and_status_200() {
        // arrange
        UsersInOrganisationsByOrganisationIdentifiersRequest request =
                new UsersInOrganisationsByOrganisationIdentifiersRequest();
        request.setOrganisationIdentifiers(Arrays.asList(organisationIdentifier1, organisationIdentifier2));
        boolean showDeleted = false;
        Integer pageSize = 4;
        UUID searchAfterUser = null;
        UUID searchAfterOrganisation = null;

        String expectedStatus = "200 OK";
        boolean expectedHasMoreRecords = true;
        int expectedOrganisationsCount = 2; // TODO: can we predict the order of orgs or stick to one org in the filter?
        int expectedUsersCount = 4;

        // act
        Map<String, Object> response =
                professionalReferenceDataClient.retrieveUsersInOrganisationsByOrganisationIdentifiers(request,
                        showDeleted, pageSize, searchAfterUser, searchAfterOrganisation);

        // assert
        assertSuccessfulResponse(response, expectedOrganisationsCount, expectedUsersCount, expectedStatus,
                expectedHasMoreRecords);
    }

    @Test
    void when_validation_fails_return_bad_request() {
        // arrange
        UsersInOrganisationsByOrganisationIdentifiersRequest request =
                new UsersInOrganisationsByOrganisationIdentifiersRequest();
        boolean showDeleted = false;
        Integer pageSize = 0;
        UUID searchAfterUser = null;
        UUID searchAfterOrganisation = null;

        String expectedStatus = "400";

        // act
        Map<String, Object> response =
                professionalReferenceDataClient.retrieveUsersInOrganisationsByOrganisationIdentifiers(request,
                        showDeleted, pageSize, searchAfterUser, searchAfterOrganisation);

        // assert
        assertThat(response.get("http_status")).isEqualTo(expectedStatus);

        String actualResponseBody = (String) response.get("response_body");
        ErrorResponse typedResponse = convertJsonToResponse(actualResponseBody, ErrorResponse.class);
        assertThat(typedResponse.getErrorDescription()).isEqualTo("Invalid pageSize");
    }

    @SuppressWarnings("unchecked")
    private void assertSuccessfulResponse(Map<String, Object> response, int expectedOrganisationsCount,
                                          int expectedUsersCount, String expectedStatus,
                                          boolean expectedHasMoreRecords, List<String>... unexpectedUserIdentifiers) {

        String actualStatus = (String) response.get("http_status");
        assertThat(actualStatus).isEqualTo(expectedStatus);

        String json = convertMapToJson(response);
        UsersInOrganisationsByOrganisationIdentifiersResponse typedResponse = convertJsonToResponse(json,
                UsersInOrganisationsByOrganisationIdentifiersResponse.class);

        assertThat(typedResponse.getOrganisationInfo().size()).isEqualTo(expectedOrganisationsCount);

        List<ProfessionalUsersResponseWithoutRoles> allUsers = new ArrayList<>();
        for (OrganisationInfoWithUsersResponse organisationInfo : typedResponse.getOrganisationInfo()) {
            allUsers.addAll(organisationInfo.getUsers());
        }


        for (List<String> unexpectedUserIdsInOrg : unexpectedUserIdentifiers) {
            for (String unexpectedUserIdentifier : unexpectedUserIdsInOrg) {
                assertThat(allUsers.stream().anyMatch(user -> user.getUserIdentifier().equals(unexpectedUserIdentifier))).isFalse();
            }
        }


        assertThat(allUsers.size()).isEqualTo(expectedUsersCount);
        assertThat(typedResponse.isMoreAvailable()).isEqualTo(expectedHasMoreRecords);
        assertThat(typedResponse.getLastOrgInPage()).isNotNull();
        assertThat(typedResponse.getLastUserInPage()).isNotNull();
    }

    public String convertMapToJson(Map<String, Object> map) {
        ObjectMapper objectMapper = new ObjectMapper();
        String json = "";
        try {
            json = objectMapper.writeValueAsString(map);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return json;
    }

    public <T> T convertJsonToResponse(String json, Class<T> type) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false);
        objectMapper.configure(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE, false);
        T response = null;
        try {
            response = objectMapper.readValue(json, type);
        } catch (Exception e) {
            // Log the error message and rethrow the exception or handle it appropriately
            System.err.println("Error processing JSON: " + e.getMessage());
        }
        return response;
    }

    private OrganisationOtherOrgsCreationRequest createUniqueOrganisationRequest(String companyNumber, String sraId,
                                                                                 String paymentAccount,
                                                                                 String superUserEmail,
                                                                                 String orgType) {
        Set<String> paymentAccounts = new HashSet<>();
        paymentAccounts.add(paymentAccount);

        List<OrgAttributeRequest> orgAttributeRequests = new ArrayList<>();

        OrgAttributeRequest orgAttributeRequest = new OrgAttributeRequest();

        orgAttributeRequest.setKey("testKey");
        orgAttributeRequest.setValue("testValue");

        orgAttributeRequests.add(orgAttributeRequest);

        return
                new OrganisationOtherOrgsCreationRequest("some-org-name",
                        "PENDING",
                        "test",
                        sraId,
                        "false",
                        companyNumber,
                        "company-url",
                        aUserCreationRequest()
                                .firstName("some-fname")
                                .lastName("some-lname")
                                .email(superUserEmail)
                                .build(),
                        paymentAccounts,
                        Collections
                                .singletonList(aContactInformationCreationRequest()
                                        .addressLine1("addressLine1")
                                        .addressLine2("addressLine2")
                                        .addressLine3("addressLine3")
                                        .country("country")
                                        .county("county")
                                        .townCity("town-city")
                                        .uprn("uprn")
                                        .postCode("some-post-code")
                                        .dxAddress(Collections
                                                .singletonList(dxAddressCreationRequest()
                                                        .dxNumber("DX 1234567890")
                                                        .dxExchange("dxExchange").build()))
                                        .build()),
                        orgType,
                        orgAttributeRequests);
    }

    private String addAUserToOrganisation(String email, String orgId) {
        List<String> userRoles = Collections.singletonList("pui-user-manager");
        NewUserCreationRequest userCreationRequest = inviteUserCreationRequest(email, userRoles);

        Map<String, Object> addUserResponse =
                professionalReferenceDataClient.addUserToOrganisation(orgId, userCreationRequest, hmctsAdmin);
        String id = (String) addUserResponse.get(USER_IDENTIFIER);
        setUserToActive(id,orgId);
        return id;
    }

    private void setUserToActive(String userIdentifier, String organisationIdentifier) {
        ProfessionalUser user = professionalUserRepository.findByUserIdentifier(userIdentifier);
        user.setIdamStatus(IdamStatus.ACTIVE);
        professionalUserRepository.saveAndFlush(user);


//        UserProfileUpdatedData userProfileUpdatedData = new UserProfileUpdatedData();
//        RoleName roleName1 = new RoleName(puiCaseManager);
//        RoleName roleName2 = new RoleName(puiOrgManager);
//        Set<RoleName> roles = new HashSet<>();
//        roles.add(roleName1);
//        roles.add(roleName2);
//
//        userProfileUpdatedData.setRolesAdd(roles);
//        userProfileUpdatedData.setIdamStatus(IdamStatus.ACTIVE.toString());
//        updateUserProfileRolesMock(HttpStatus.OK);
//
//        Map<String, Object> response = professionalReferenceDataClient
//                .modifyUserRolesOfOrganisation(userProfileUpdatedData, organisationIdentifier, userIdentifier,
//                        hmctsAdmin);
//
//        assertThat(response.get("http_status")).isNotNull();
//        assertThat(response.get("http_status")).isEqualTo("200 OK");
    }
}
