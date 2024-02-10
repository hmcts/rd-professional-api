package uk.gov.hmcts.reform.professionalapi;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
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
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;
import uk.gov.hmcts.reform.professionalapi.domain.SuperUser;
import uk.gov.hmcts.reform.professionalapi.repository.OrganisationRepository;
import uk.gov.hmcts.reform.professionalapi.util.AuthorizationEnabledIntegrationTest;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.professionalapi.controller.request.ContactInformationCreationRequest.aContactInformationCreationRequest;
import static uk.gov.hmcts.reform.professionalapi.controller.request.DxAddressCreationRequest.dxAddressCreationRequest;
import static uk.gov.hmcts.reform.professionalapi.controller.request.UserCreationRequest.aUserCreationRequest;

@Execution(ExecutionMode.SAME_THREAD)
public class FindUsersByOrganisationsIntegrationTest extends AuthorizationEnabledIntegrationTest {

    @Autowired
    private OrganisationRepository organisationRepository;

    private String organisationIdentifier1;
    private Organisation organisation1;
    private String organisationIdentifier2;
    private Organisation organisation2;
    private String organisationIdentifier3;
    private Organisation organisation3;

    // key: organisationIdentifier, value: list of userIdentifiers
    private Map<String, List<String>> unorderedUsersInOrganisation = new LinkedHashMap<>();
    // key: organisationId, value: list of professionalUsers
    private LinkedHashMap<UUID, List<ProfessionalUser>> sortedUsersInOrganisation;

    private ProfessionalUser deletedUser;

    @BeforeEach
    public void setup() {
        professionalUserRepository.deleteAll();
        organisationRepository.deleteAll();
        createOrganisationsAndUsers();
        orderOrganisationsAndUsers();
    }

    private void createOrganisationsAndUsers() {
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

        unorderedUsersInOrganisation.put(organisationIdentifier2, Arrays.asList(userIdentifier1Org2, userIdentifier2Org2));

        // create organisation 3 with 1 superuser and 2 active users
        OrganisationOtherOrgsCreationRequest newOrgRequest3 = createUniqueOrganisationRequest("TstSO3", "SRA345",
                "PBA3456789", "super-email3@gmail.com", solicitorOrgType);
        organisationIdentifier3 = createAndActivateOrganisationWithGivenRequest(newOrgRequest3);

        userProfileCreateUserWireMock(HttpStatus.CREATED);
        String userIdentifier1Org3 = addAUserToOrganisation("user1.org3@test.com", organisationIdentifier3);

        userProfileCreateUserWireMock(HttpStatus.CREATED);
        String userIdentifier2Org3 = addAUserToOrganisation("user2.org3@test.com", organisationIdentifier3);

        unorderedUsersInOrganisation.put(organisationIdentifier3, Arrays.asList(userIdentifier1Org3, userIdentifier2Org3));

        userProfileCreateUserWireMock(HttpStatus.CREATED);
        String userIdentifier3Org1 = addAUserToOrganisation("user3.org1@test.com", organisationIdentifier1);

        // add in a 3rd professional user in a random order to check if the sorting is maintained by the API
        deletedUser = professionalUserRepository.findByUserIdentifier(userIdentifier3Org1);
        deletedUser.setDeleted(LocalDateTime.now());
        deletedUser.setIdamStatus(IdamStatus.SUSPENDED);
        professionalUserRepository.save(deletedUser);

        unorderedUsersInOrganisation.put(organisationIdentifier1, Arrays.asList(userIdentifier1Org1,
                userIdentifier2Org1));
    }

    private void orderOrganisationsAndUsers() {
        // a bit clunky but reliable way to sort the organisations and users
        // Java treats each GUID as a pair of signed 64-bit integers in big-endian format. 89ABCDEF01234567
        // see https://devblogs.microsoft.com/oldnewthing/20190913-00/?p=102859
        sortedUsersInOrganisation = new LinkedHashMap<>();
        organisation1 = organisationRepository.findByOrganisationIdentifier(organisationIdentifier1);
        organisation2 = organisationRepository.findByOrganisationIdentifier(organisationIdentifier2);
        organisation3 = organisationRepository.findByOrganisationIdentifier(organisationIdentifier3);

        List<Organisation> organisations = Arrays.asList(organisation1, organisation2, organisation3);
        organisations.sort(Comparator.comparing(org -> org.getId().toString()));

        for (Organisation organisation : organisations) {
            List<ProfessionalUser> users = professionalUserRepository.findByOrganisation(organisation);
            users.sort(Comparator.comparing(user -> user.getId().toString()));
            sortedUsersInOrganisation.put(organisation.getId(), users);
        }
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

        String json = convertMapToJson(response);
        UsersInOrganisationsByOrganisationIdentifiersResponse typedResponse = convertJsonToResponse(json,
                UsersInOrganisationsByOrganisationIdentifiersResponse.class);

        // assert that the order of organisations and users is maintained and the users exist in the response
        for (OrganisationInfoWithUsersResponse orgResponse : typedResponse.getOrganisationInfo()) {
            Organisation organisation = getMatchingOrganisationKeyByIdentifier(orgResponse.getOrganisationIdentifier());
            assertThat(organisation).isNotNull();
            orgResponse.getUsers().forEach(user -> assertThat(sortedUsersInOrganisation.get(organisation.getId()).stream()
                    .anyMatch(professionalUser -> professionalUser.getUserIdentifier().equals(user.getUserIdentifier()))).isTrue());
        }
    }

    private Organisation getMatchingOrganisationKeyByIdentifier(String organisationIdentifier) {
        for (Organisation organisation : Arrays.asList(organisation1, organisation2, organisation3)) {
            if (organisation.getOrganisationIdentifier().equals(organisationIdentifier)) {
                return organisation;
            }
        }
        return null;
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
        int expectedUsersCount = 7;

        // act
        Map<String, Object> response =
                professionalReferenceDataClient.retrieveUsersInOrganisationsByOrganisationIdentifiers(request,
                        showDeleted, pageSize, searchAfterUser, searchAfterOrganisation);

        // assert
        assertSuccessfulResponse(response, expectedOrganisationsCount, expectedUsersCount, expectedStatus,
                expectedHasMoreRecords, unorderedUsersInOrganisation.get(organisationIdentifier3));
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
                expectedHasMoreRecords, unorderedUsersInOrganisation.get(organisationIdentifier3));
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
        Integer pageSize = 5;
        UUID searchAfterUser = null;
        UUID searchAfterOrganisation = null;

        String expectedStatus = "200 OK";
        boolean expectedHasMoreRecords = true;
        int expectedOrganisationsCount = 2;
        int expectedUsersCount = 5;

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
        int expectedOrganisationsCount = 2;
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
    void when_paged_no_org_filter_is_provided_with_search_after_user_and_search_after_org_including_deleted_return_paged_all_users_for_all_orgs_and_status_200() {
        // arrange
        UsersInOrganisationsByOrganisationIdentifiersRequest request =
                new UsersInOrganisationsByOrganisationIdentifiersRequest();
        boolean showDeleted = true;
        Integer pageSize = 15;

        Map.Entry<UUID, List<ProfessionalUser>> firstEntry = sortedUsersInOrganisation.entrySet().iterator().next();
        // skip the first user in first org (not necessarily organisation 1)
        UUID searchAfterUser = firstEntry.getValue().get(0).getId();

        UUID searchAfterOrganisation = firstEntry.getKey();

        String expectedStatus = "200 OK";
        boolean expectedHasMoreRecords = false; // returning everything with one page
        int expectedOrganisationsCount = 3;
        int expectedUsersCount = 9; // all but one

        // act
        Map<String, Object> response =
                professionalReferenceDataClient.retrieveUsersInOrganisationsByOrganisationIdentifiers(request,
                        showDeleted, pageSize, searchAfterUser, searchAfterOrganisation);

        // assert
        assertSuccessfulResponse(response, expectedOrganisationsCount, expectedUsersCount, expectedStatus,
                expectedHasMoreRecords);
    }

    @Test
    void when_paged_no_org_filter_is_provided_with_search_after_user_and_search_after_org_excluding_deleted_return_paged_all_non_deleted_users_for_all_orgs_and_status_200() {
        // arrange
        // remove deleted user from list to make setup predictable
        sortedUsersInOrganisation.get(deletedUser.getOrganisation().getId()).remove(deletedUser);
        UsersInOrganisationsByOrganisationIdentifiersRequest request =
                new UsersInOrganisationsByOrganisationIdentifiersRequest();
        boolean showDeleted = false;
        Integer pageSize = 3;

        Map.Entry<UUID, List<ProfessionalUser>> firstEntry = sortedUsersInOrganisation.entrySet().iterator().next();
        // skip the first 2 users in first org (not necessarily organisation 1)
        UUID searchAfterUser = firstEntry.getValue().get(0).getId();

        UUID searchAfterOrganisation = firstEntry.getKey();

        String expectedStatus = "200 OK";
        boolean expectedHasMoreRecords = true;
        int expectedOrganisationsCount = 2;
        int expectedUsersCount = 3;

        // act
        Map<String, Object> response =
                professionalReferenceDataClient.retrieveUsersInOrganisationsByOrganisationIdentifiers(request,
                        showDeleted, pageSize, searchAfterUser, searchAfterOrganisation);

        // assert
        assertSuccessfulResponse(response, expectedOrganisationsCount, expectedUsersCount, expectedStatus,
                expectedHasMoreRecords);
    }

    @Test
    void when_paged_org_filter_is_provided_with_search_after_user_and_search_after_org_including_deleted_return_paged_all_users_for_given_orgs_and_status_200() {
        // arrange
        UsersInOrganisationsByOrganisationIdentifiersRequest request =
                new UsersInOrganisationsByOrganisationIdentifiersRequest();
        request.setOrganisationIdentifiers(Arrays.asList(organisationIdentifier1, organisationIdentifier2));
        boolean showDeleted = true;
        Integer pageSize = 3;

        // get org 1 and org 2 in order
        List<Organisation> orgsForTest = Arrays.asList(getMatchingOrganisationKeyByIdentifier(organisationIdentifier1),
                getMatchingOrganisationKeyByIdentifier(organisationIdentifier2));
        Collections.sort(orgsForTest, Comparator.comparing(org -> org.getId().toString()));

        List<ProfessionalUser> professionalUsers = sortedUsersInOrganisation.get(orgsForTest.get(0).getId());
        // skip the first user in first org (not necessarily organisation 1)
        UUID searchAfterUser = professionalUsers.get(0).getId();
        UUID searchAfterOrganisation = orgsForTest.get(0).getId();

        String expectedStatus = "200 OK";
        boolean expectedHasMoreRecords = true;
        int expectedOrganisationsCount = 2;
        int expectedUsersCount = 3;

        // act
        Map<String, Object> response =
                professionalReferenceDataClient.retrieveUsersInOrganisationsByOrganisationIdentifiers(request,
                        showDeleted, pageSize, searchAfterUser, searchAfterOrganisation);

        // assert
        assertSuccessfulResponse(response, expectedOrganisationsCount, expectedUsersCount, expectedStatus,
                expectedHasMoreRecords);
    }

    @Test
    void when_paged_org_filter_is_provided_with_search_after_user_and_search_after_org_excluding_deleted_return_paged_all_non_deleted_users_for_given_orgs_and_status_200() {
        // arrange

        // remove deleted user from list to make setup predictable
        sortedUsersInOrganisation.get(deletedUser.getOrganisation().getId()).remove(deletedUser);
        UsersInOrganisationsByOrganisationIdentifiersRequest request =
                new UsersInOrganisationsByOrganisationIdentifiersRequest();
        request.setOrganisationIdentifiers(Arrays.asList(organisationIdentifier1, organisationIdentifier2));
        boolean showDeleted = false;
        Integer pageSize = 5;

        // get org 1 and org 2 in order
        List<Organisation> orgsForTest = Arrays.asList(getMatchingOrganisationKeyByIdentifier(organisationIdentifier1),
                getMatchingOrganisationKeyByIdentifier(organisationIdentifier2));
        Collections.sort(orgsForTest, Comparator.comparing(org -> org.getId().toString()));

        List<ProfessionalUser> professionalUsers = sortedUsersInOrganisation.get(orgsForTest.get(0).getId());
        // skip the first user in first org (not necessarily organisation 1)
        UUID searchAfterUser = professionalUsers.get(0).getId();
        UUID searchAfterOrganisation = orgsForTest.get(0).getId();

        String expectedStatus = "200 OK";
        boolean expectedHasMoreRecords = false; // only 5 users left without deleted and skipping the deleted user
        int expectedOrganisationsCount = 2;
        int expectedUsersCount = 5;

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
