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
import uk.gov.hmcts.reform.professionalapi.controller.response.OrganisationUserResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.UsersInOrganisationsByOrganisationIdentifiersResponse;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;
import uk.gov.hmcts.reform.professionalapi.repository.OrganisationRepository;
import uk.gov.hmcts.reform.professionalapi.util.AuthorizationEnabledIntegrationTest;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.professionalapi.controller.request.ContactInformationCreationRequest.aContactInformationCreationRequest;
import static uk.gov.hmcts.reform.professionalapi.controller.request.DxAddressCreationRequest.dxAddressCreationRequest;
import static uk.gov.hmcts.reform.professionalapi.controller.request.UserCreationRequest.aUserCreationRequest;

@SuppressWarnings("unchecked")
class FindUsersByOrganisationsIntegrationTest extends AuthorizationEnabledIntegrationTest {

    @Autowired
    private OrganisationRepository organisationRepository;

    private String organisationIdentifier1;
    private Organisation organisation1;
    private String organisationIdentifier2;
    private Organisation organisation2;
    private String organisationIdentifier3;
    private Organisation organisation3;

    // key: organisationIdentifier, value: list of userIdentifiers
    private Map<String, List<String>> unorderedUsersInOrganisation;
    // key: organisationId, value: list of professionalUsers
    private LinkedHashMap<UUID, LinkedList<ProfessionalUser>> sortedUsersInOrganisation;
    private ProfessionalUser deletedUser;

    @BeforeEach
    public void setup() {
        professionalUserRepository.deleteAll();
        organisationRepository.deleteAll();
        createOrganisationsAndUsers();
        orderOrganisationsAndUsers();
    }

    private void createOrganisationsAndUsers() {
        unorderedUsersInOrganisation = new LinkedHashMap<>();
        String solicitorOrgType = "SOLICITOR-ORG";

        // create organisation 1 with 1 superuser, 2 active users and 1 deleted user
        OrganisationOtherOrgsCreationRequest newOrgRequest1 = createUniqueOrganisationRequest("TstSO1", "SRA123",
                "PBA1234561", "super-email1@gmail.com", solicitorOrgType);
        organisationIdentifier1 = createAndActivateOrganisationWithGivenRequest(newOrgRequest1);

        userProfileCreateUserWireMock(HttpStatus.CREATED);
        String userIdentifier1Org1 = addAUserToOrganisation("user1.org1@test.com", organisationIdentifier1);

        userProfileCreateUserWireMock(HttpStatus.CREATED);
        String userIdentifier2Org1 = addAUserToOrganisation("user2.org1@test.com", organisationIdentifier1);

        userProfileCreateUserWireMock(HttpStatus.CREATED);
        String userIdentifier3Org1 = addAUserToOrganisation("user3.org1@test.com", organisationIdentifier1);

        unorderedUsersInOrganisation.put(organisationIdentifier1, Arrays.asList(userIdentifier1Org1,
                userIdentifier2Org1, userIdentifier3Org1));

        deletedUser = professionalUserRepository.findByUserIdentifier(userIdentifier3Org1);
        deletedUser.setDeleted(LocalDateTime.now());
        deletedUser.setIdamStatus(IdamStatus.SUSPENDED);
        professionalUserRepository.save(deletedUser);

        // create organisation 2 with 1 superuser and 2 active users
        OrganisationOtherOrgsCreationRequest newOrgRequest2 = createUniqueOrganisationRequest("TstSO2", "SRA234",
                "PBA2345678", "super-email2@gmail.com", solicitorOrgType);
        organisationIdentifier2 = createAndActivateOrganisationWithGivenRequest(newOrgRequest2);

        userProfileCreateUserWireMock(HttpStatus.CREATED);
        String userIdentifier1Org2 = addAUserToOrganisation("user1.org2@test.com", organisationIdentifier2);

        userProfileCreateUserWireMock(HttpStatus.CREATED);
        String userIdentifier2Org2 = addAUserToOrganisation("user2.org2@test.com", organisationIdentifier2);

        unorderedUsersInOrganisation.put(organisationIdentifier2, Arrays.asList(userIdentifier1Org2,
                userIdentifier2Org2));

        // create organisation 3 with 1 superuser and 2 active users
        OrganisationOtherOrgsCreationRequest newOrgRequest3 = createUniqueOrganisationRequest("TstSO3", "SRA345",
                "PBA3456789", "super-email3@gmail.com", solicitorOrgType);
        organisationIdentifier3 = createAndActivateOrganisationWithGivenRequest(newOrgRequest3);

        userProfileCreateUserWireMock(HttpStatus.CREATED);
        String userIdentifier1Org3 = addAUserToOrganisation("user1.org3@test.com", organisationIdentifier3);

        userProfileCreateUserWireMock(HttpStatus.CREATED);
        String userIdentifier2Org3 = addAUserToOrganisation("user2.org3@test.com", organisationIdentifier3);

        unorderedUsersInOrganisation.put(organisationIdentifier3, Arrays.asList(userIdentifier1Org3,
                userIdentifier2Org3));
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
            LinkedList<ProfessionalUser> usersLinkedList = new LinkedList<>(users);
            sortedUsersInOrganisation.put(organisation.getId(), usersLinkedList);
        }
    }

    @Test
    void return_all_users_when_no_org_filter_and_no_search_after_is_provided() {
        // arrange
        UsersInOrganisationsByOrganisationIdentifiersRequest request =
                new UsersInOrganisationsByOrganisationIdentifiersRequest();
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
                        pageSize, searchAfterUser, searchAfterOrganisation);

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
            orgResponse.getUsers().forEach(user -> assertThat(
                    sortedUsersInOrganisation.get(organisation.getId()).stream()
                    .anyMatch(professionalUser -> professionalUser.getUserIdentifier()
                            .equals(user.getUserIdentifier()))).isTrue());
        }
    }

    @Test
    void return_all_users_for_given_orgs_when_org_filter_is_provided_and_no_search_after_provided() {
        // arrange
        UsersInOrganisationsByOrganisationIdentifiersRequest request =
                new UsersInOrganisationsByOrganisationIdentifiersRequest();
        request.setOrganisationIdentifiers(Arrays.asList(organisationIdentifier1, organisationIdentifier2));
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
                        pageSize, searchAfterUser, searchAfterOrganisation);

        // assert
        assertSuccessfulResponse(response, expectedOrganisationsCount, expectedUsersCount, expectedStatus,
                expectedHasMoreRecords, unorderedUsersInOrganisation.get(organisationIdentifier3));
    }

    @Test
    void return_paged_all_users_when_no_org_filter_and_no_search_after_is_provided() {
        // arrange
        UsersInOrganisationsByOrganisationIdentifiersRequest request =
                new UsersInOrganisationsByOrganisationIdentifiersRequest();
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
                        pageSize, searchAfterUser, searchAfterOrganisation);

        // assert
        assertSuccessfulResponse(response, expectedOrganisationsCount, expectedUsersCount, expectedStatus,
                expectedHasMoreRecords);
    }

    @Test
    void return_paged_all_users_for_given_orgs_when_org_filter_and_no_search_after_provided() {
        // arrange
        UsersInOrganisationsByOrganisationIdentifiersRequest request =
                new UsersInOrganisationsByOrganisationIdentifiersRequest();
        request.setOrganisationIdentifiers(Arrays.asList(organisationIdentifier1, organisationIdentifier2));

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
                        pageSize, searchAfterUser, searchAfterOrganisation);

        // assert
        assertSuccessfulResponse(response, expectedOrganisationsCount, expectedUsersCount, expectedStatus,
                expectedHasMoreRecords);
    }

    @Test
    void return_paged_all_users_for_given_orgs_when_org_filter_and_search_after_is_provided() {
        // arrange
        UsersInOrganisationsByOrganisationIdentifiersRequest request =
                new UsersInOrganisationsByOrganisationIdentifiersRequest();

        Integer pageSize = 15;

        Map.Entry<UUID, LinkedList<ProfessionalUser>> firstEntry =
                sortedUsersInOrganisation.entrySet().iterator().next();
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
                        pageSize, searchAfterUser, searchAfterOrganisation);

        // assert
        assertSuccessfulResponse(response, expectedOrganisationsCount, expectedUsersCount, expectedStatus,
                expectedHasMoreRecords);
    }

    @Test
    void return_small_paged_all_users_for_given_orgs_when_org_filter_and_search_after_provided() {
        // arrange
        // org 1 has a deleted user, so it must be used in the test
        List<Organisation> orgsForTest = Arrays.asList(getMatchingOrganisationKeyByIdentifier(organisationIdentifier1),
                getMatchingOrganisationKeyByIdentifier(organisationIdentifier2));
        // cannot assume org 1 is first
        orgsForTest.sort(Comparator.comparing(org -> org.getId().toString()));

        UsersInOrganisationsByOrganisationIdentifiersRequest request =
                new UsersInOrganisationsByOrganisationIdentifiersRequest();
        request.setOrganisationIdentifiers(Arrays.asList(organisationIdentifier1, organisationIdentifier2));

        Integer pageSize = 3;

        List<ProfessionalUser> professionalUsers = sortedUsersInOrganisation.get(orgsForTest.get(0).getId());
        // 7 users in both orgs. skip the first user in org 1. expect 2 from the first org and 1 from the second
        UUID searchAfterUser = professionalUsers.get(0).getId();
        UUID searchAfterOrganisation = orgsForTest.get(0).getId();

        boolean isOrg1First = orgsForTest.get(0).getId().equals(organisation1.getId());

        String expectedStatus = "200 OK";
        boolean expectedHasMoreRecords = true;
        int expectedOrganisationsCount = isOrg1First ? 1 : 2; // org 1 has 4 users, org 2 has 3 users
        int expectedUsersCount = 3;

        // act
        Map<String, Object> response =
                professionalReferenceDataClient.retrieveUsersInOrganisationsByOrganisationIdentifiers(request,
                        pageSize, searchAfterUser, searchAfterOrganisation);

        // assert
        assertSuccessfulResponse(response, expectedOrganisationsCount, expectedUsersCount, expectedStatus,
                expectedHasMoreRecords);
    }

    @Test
    void return_small_paged_users_for_given_orgs_when_org_filter_and_search_after_provided() {
        // arrange

        // remove deleted user from list to make setup predictable
        sortedUsersInOrganisation.get(deletedUser.getOrganisation().getId()).remove(deletedUser);
        UsersInOrganisationsByOrganisationIdentifiersRequest request =
                new UsersInOrganisationsByOrganisationIdentifiersRequest();
        request.setOrganisationIdentifiers(Arrays.asList(organisationIdentifier1, organisationIdentifier2));

        Integer pageSize = 5;

        // get org 1 and org 2 in order
        List<Organisation> orgsForTest = Arrays.asList(getMatchingOrganisationKeyByIdentifier(organisationIdentifier1),
                getMatchingOrganisationKeyByIdentifier(organisationIdentifier2));
        orgsForTest.sort(Comparator.comparing(org -> org.getId().toString()));

        List<ProfessionalUser> professionalUsers = sortedUsersInOrganisation.get(orgsForTest.get(0).getId());
        // skip the first user in first org (not necessarily organisation 1)
        UUID searchAfterUser = professionalUsers.get(1).getId();
        UUID searchAfterOrganisation = orgsForTest.get(0).getId();

        String expectedStatus = "200 OK";
        boolean expectedHasMoreRecords = false;
        int expectedOrganisationsCount = 2;
        int expectedUsersCount = 5;

        // act
        Map<String, Object> response =
                professionalReferenceDataClient.retrieveUsersInOrganisationsByOrganisationIdentifiers(request,
                        pageSize, searchAfterUser, searchAfterOrganisation);

        // assert
        assertSuccessfulResponse(response, expectedOrganisationsCount, expectedUsersCount, expectedStatus,
                expectedHasMoreRecords);
    }

    @Test
    void search_without_searchAfter_and_then_search_with_after_until_no_more_is_available() {
        // arrange

        // use 2 organisations with same number of users for simplicity
        List<Organisation> orgsForTest = Arrays.asList(getMatchingOrganisationKeyByIdentifier(organisationIdentifier2),
                getMatchingOrganisationKeyByIdentifier(organisationIdentifier3));
        orgsForTest.sort(Comparator.comparing(org -> org.getId().toString()));

        List<ProfessionalUser> professionUsersForTest =
                new ArrayList<>(sortedUsersInOrganisation.get(orgsForTest.get(0).getId()));
        professionUsersForTest.addAll(sortedUsersInOrganisation.get(orgsForTest.get(1).getId()));

        UsersInOrganisationsByOrganisationIdentifiersRequest request =
                new UsersInOrganisationsByOrganisationIdentifiersRequest();
        request.setOrganisationIdentifiers(Arrays.asList(organisationIdentifier2, organisationIdentifier3));

        // act & assert, until no more records are available
        Integer pageSize = 2;

        Map<String, Object> response =
                professionalReferenceDataClient.retrieveUsersInOrganisationsByOrganisationIdentifiers(request,
                        pageSize, null, null);

        String json = convertMapToJson(response);
        UsersInOrganisationsByOrganisationIdentifiersResponse typedResponse = convertJsonToResponse(json,
                UsersInOrganisationsByOrganisationIdentifiersResponse.class);

        // first request / page will only return 2 users from the first organisation
        assertTrue(typedResponse.isMoreAvailable());
        assertThat(typedResponse.getOrganisationInfo().get(0).getUsers()).hasSize(2);
        assertThat(typedResponse.getOrganisationInfo().get(0).getOrganisationIdentifier())
                .isEqualTo(orgsForTest.get(0).getOrganisationIdentifier());

        List<OrganisationUserResponse> usersInResponse = getAllUsersInOrganisationResponse(typedResponse);

        assertThat(usersInResponse.get(0).getUserIdentifier())
                .isEqualTo(professionUsersForTest.get(0).getUserIdentifier());
        assertThat(usersInResponse.get(1).getUserIdentifier())
                .isEqualTo(professionUsersForTest.get(1).getUserIdentifier());

        UUID lastUserInPage = typedResponse.getLastUserInPage();
        UUID lastOrgInPage = typedResponse.getLastOrgInPage();

        assertThat(lastUserInPage).isEqualTo(professionUsersForTest.get(1).getId());
        assertThat(lastOrgInPage).isEqualTo(orgsForTest.get(0).getId());

        // second request / page will return 1 user from first organisation and 1 organisation from the second
        response = professionalReferenceDataClient.retrieveUsersInOrganisationsByOrganisationIdentifiers(request,
                pageSize, lastUserInPage, lastOrgInPage);

        json = convertMapToJson(response);
        typedResponse = convertJsonToResponse(json,
                UsersInOrganisationsByOrganisationIdentifiersResponse.class);

        assertTrue(typedResponse.isMoreAvailable());
        assertThat(typedResponse.getOrganisationInfo()).hasSize(2);
        assertThat(typedResponse.getOrganisationInfo().get(0).getUsers()).hasSize(1);
        assertThat(typedResponse.getOrganisationInfo().get(1).getUsers()).hasSize(1);
        assertThat(typedResponse.getOrganisationInfo().get(0).getOrganisationIdentifier())
                .isEqualTo(orgsForTest.get(0).getOrganisationIdentifier());
        assertThat(typedResponse.getOrganisationInfo().get(1).getOrganisationIdentifier())
                .isEqualTo(orgsForTest.get(1).getOrganisationIdentifier());

        usersInResponse = getAllUsersInOrganisationResponse(typedResponse);

        assertThat(usersInResponse.get(0).getUserIdentifier())
                .isEqualTo(professionUsersForTest.get(2).getUserIdentifier());
        assertThat(usersInResponse.get(1).getUserIdentifier())
                .isEqualTo(professionUsersForTest.get(3).getUserIdentifier());

        lastUserInPage = typedResponse.getLastUserInPage();
        lastOrgInPage = typedResponse.getLastOrgInPage();

        assertThat(lastUserInPage).isEqualTo(professionUsersForTest.get(3).getId());
        assertThat(lastOrgInPage).isEqualTo(orgsForTest.get(1).getId());

        // third request / page will return 2 users from the second organisation
        response = professionalReferenceDataClient.retrieveUsersInOrganisationsByOrganisationIdentifiers(request,
                pageSize, lastUserInPage, lastOrgInPage);

        json = convertMapToJson(response);
        typedResponse = convertJsonToResponse(json,
                UsersInOrganisationsByOrganisationIdentifiersResponse.class);

        assertThat(typedResponse.isMoreAvailable()).isFalse();
        assertThat(typedResponse.getOrganisationInfo().get(0).getUsers()).hasSize(2);
        assertThat(typedResponse.getOrganisationInfo().get(0).getOrganisationIdentifier())
                .isEqualTo(orgsForTest.get(1).getOrganisationIdentifier());

        usersInResponse = getAllUsersInOrganisationResponse(typedResponse);

        assertThat(usersInResponse.get(0).getUserIdentifier())
                .isEqualTo(professionUsersForTest.get(4).getUserIdentifier());
        assertThat(usersInResponse.get(1).getUserIdentifier())
                .isEqualTo(professionUsersForTest.get(5).getUserIdentifier());

        lastUserInPage = typedResponse.getLastUserInPage();
        lastOrgInPage = typedResponse.getLastOrgInPage();

        assertThat(lastUserInPage).isEqualTo(professionUsersForTest.get(5).getId());
        assertThat(lastOrgInPage).isEqualTo(orgsForTest.get(1).getId());
    }

    @Test
    void search_single_organisation_until_no_more_available() {
        // there are 3 users in organisation 2
        Integer pageSize = 1;

        // first request has no search after and page size 1
        UsersInOrganisationsByOrganisationIdentifiersRequest request =
                new UsersInOrganisationsByOrganisationIdentifiersRequest();
        request.setOrganisationIdentifiers(Collections.singletonList(organisationIdentifier2));

        Map<String, Object> response =
                professionalReferenceDataClient.retrieveUsersInOrganisationsByOrganisationIdentifiers(request,
                        pageSize, null, null);

        String json = convertMapToJson(response);
        UsersInOrganisationsByOrganisationIdentifiersResponse typedResponse = convertJsonToResponse(json,
                UsersInOrganisationsByOrganisationIdentifiersResponse.class);

        // first request / page will only return 1 user from the first organisation
        Organisation organisationForTest = getMatchingOrganisationKeyByIdentifier(organisationIdentifier2);
        assertThat(typedResponse.getOrganisationInfo()).hasSize(1);
        assertThat(typedResponse.getOrganisationInfo().get(0).getOrganisationIdentifier())
                .isEqualTo(organisationForTest.getOrganisationIdentifier());

        assertThat(typedResponse.getOrganisationInfo().get(0).getUsers()).hasSize(1);
        assertTrue(typedResponse.isMoreAvailable());


        List<OrganisationUserResponse> usersInResponse = getAllUsersInOrganisationResponse(typedResponse);
        List<ProfessionalUser> professionalUsersForTest =
                new ArrayList<>(sortedUsersInOrganisation.get(organisationForTest.getId()));
        assertThat(usersInResponse.get(0).getUserIdentifier())
                .isEqualTo(professionalUsersForTest.get(0).getUserIdentifier());

        UUID lastUserInPage = typedResponse.getLastUserInPage();
        UUID lastOrgInPage = typedResponse.getLastOrgInPage();


        // second request / page will return 1 user from the first organisation
        response = professionalReferenceDataClient.retrieveUsersInOrganisationsByOrganisationIdentifiers(request,
                pageSize, lastUserInPage, lastOrgInPage);

        json = convertMapToJson(response);
        typedResponse = convertJsonToResponse(json,
                UsersInOrganisationsByOrganisationIdentifiersResponse.class);

        assertTrue(typedResponse.isMoreAvailable());
        assertThat(typedResponse.getOrganisationInfo()).hasSize(1);
        assertThat(typedResponse.getOrganisationInfo().get(0).getUsers()).hasSize(1);
        assertThat(typedResponse.getOrganisationInfo().get(0).getOrganisationIdentifier())
                .isEqualTo(organisationForTest.getOrganisationIdentifier());

        usersInResponse = getAllUsersInOrganisationResponse(typedResponse);
        assertThat(usersInResponse.get(0).getUserIdentifier())
                .isEqualTo(professionalUsersForTest.get(1).getUserIdentifier());

        lastUserInPage = typedResponse.getLastUserInPage();
        lastOrgInPage = typedResponse.getLastOrgInPage();

        // third request / page will return 1 user from the first organisation
        response = professionalReferenceDataClient.retrieveUsersInOrganisationsByOrganisationIdentifiers(request,
                pageSize, lastUserInPage, lastOrgInPage);

        json = convertMapToJson(response);
        typedResponse = convertJsonToResponse(json,
                UsersInOrganisationsByOrganisationIdentifiersResponse.class);

        assertFalse(typedResponse.isMoreAvailable());
        assertThat(typedResponse.getOrganisationInfo()).hasSize(1);
        assertThat(typedResponse.getOrganisationInfo().get(0).getUsers()).hasSize(1);
        assertThat(typedResponse.getOrganisationInfo().get(0).getOrganisationIdentifier())
                .isEqualTo(organisationForTest.getOrganisationIdentifier());

        usersInResponse = getAllUsersInOrganisationResponse(typedResponse);
        assertThat(usersInResponse.get(0).getUserIdentifier())
                .isEqualTo(professionalUsersForTest.get(2).getUserIdentifier());
    }

    @Test
    void search_single_organisation_with_page_size_greater_than_number_of_users() {
        // there are 3 users in organisation 2
        Integer pageSize = 2;

        // first request has no search after and page size 1
        UsersInOrganisationsByOrganisationIdentifiersRequest request =
                new UsersInOrganisationsByOrganisationIdentifiersRequest();
        request.setOrganisationIdentifiers(Collections.singletonList(organisationIdentifier2));

        Map<String, Object> response =
                professionalReferenceDataClient.retrieveUsersInOrganisationsByOrganisationIdentifiers(request,
                        pageSize, null, null);

        String json = convertMapToJson(response);
        UsersInOrganisationsByOrganisationIdentifiersResponse typedResponse = convertJsonToResponse(json,
                UsersInOrganisationsByOrganisationIdentifiersResponse.class);

        // first request / page will only return 1 user from the first organisation
        Organisation organisationForTest = getMatchingOrganisationKeyByIdentifier(organisationIdentifier2);
        assertThat(typedResponse.getOrganisationInfo()).hasSize(1);
        assertThat(typedResponse.getOrganisationInfo().get(0).getOrganisationIdentifier())
                .isEqualTo(organisationForTest.getOrganisationIdentifier());
        assertThat(typedResponse.getOrganisationInfo().get(0).getUsers()).hasSize(2);
        assertTrue(typedResponse.isMoreAvailable());

        List<ProfessionalUser> professionalUsersForTest =
                new ArrayList<>(sortedUsersInOrganisation.get(organisationForTest.getId()));
        List<OrganisationUserResponse> usersInResponse = getAllUsersInOrganisationResponse(typedResponse);
        assertThat(usersInResponse.get(0).getUserIdentifier())
                .isEqualTo(professionalUsersForTest.get(0).getUserIdentifier());
        assertThat(usersInResponse.get(1).getUserIdentifier())
                .isEqualTo(professionalUsersForTest.get(1).getUserIdentifier());

        UUID lastUserInPage = typedResponse.getLastUserInPage();
        UUID lastOrgInPage = typedResponse.getLastOrgInPage();

        // second request / page will return remaining 1 user from the first organisation
        response = professionalReferenceDataClient.retrieveUsersInOrganisationsByOrganisationIdentifiers(request,
                pageSize, lastUserInPage, lastOrgInPage);

        json = convertMapToJson(response);
        typedResponse = convertJsonToResponse(json,
                UsersInOrganisationsByOrganisationIdentifiersResponse.class);

        assertFalse(typedResponse.isMoreAvailable());
        assertThat(typedResponse.getOrganisationInfo()).hasSize(1);
        assertThat(typedResponse.getOrganisationInfo().get(0).getUsers()).hasSize(1);
        assertThat(typedResponse.getOrganisationInfo().get(0).getOrganisationIdentifier())
                .isEqualTo(organisationForTest.getOrganisationIdentifier());

        usersInResponse = getAllUsersInOrganisationResponse(typedResponse);
        assertThat(usersInResponse.get(0).getUserIdentifier())
                .isEqualTo(professionalUsersForTest.get(2).getUserIdentifier());
    }

    private List<OrganisationUserResponse> getAllUsersInOrganisationResponse(
            UsersInOrganisationsByOrganisationIdentifiersResponse response) {
        return response.getOrganisationInfo().stream()
                .flatMap(organisationInfo -> organisationInfo.getUsers().stream()).collect(Collectors.toList());
    }

    @Test
    void return_bad_request_with_invalid_params() {
        // arrange
        UsersInOrganisationsByOrganisationIdentifiersRequest request =
                new UsersInOrganisationsByOrganisationIdentifiersRequest();

        Integer pageSize = 0;
        UUID searchAfterUser = null;
        UUID searchAfterOrganisation = null;

        String expectedStatus = "400";

        // act
        Map<String, Object> response =
                professionalReferenceDataClient.retrieveUsersInOrganisationsByOrganisationIdentifiers(request,
                        pageSize, searchAfterUser, searchAfterOrganisation);

        // assert
        assertThat(response).containsEntry("http_status", "response_body");

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

        assertThat(typedResponse.getOrganisationInfo()).hasSize(expectedOrganisationsCount);

        List<OrganisationUserResponse> allUsers = new ArrayList<>();
        for (OrganisationInfoWithUsersResponse organisationInfo : typedResponse.getOrganisationInfo()) {
            allUsers.addAll(organisationInfo.getUsers());
        }


        for (List<String> unexpectedUserIdsInOrg : unexpectedUserIdentifiers) {
            for (String unexpectedUserIdentifier : unexpectedUserIdsInOrg) {
                assertThat(allUsers.stream()
                        .anyMatch(user -> user.getUserIdentifier().equals(unexpectedUserIdentifier))).isFalse();
            }
        }


        assertThat(allUsers).hasSize(expectedUsersCount);
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
    }

    private Organisation getMatchingOrganisationKeyByIdentifier(String organisationIdentifier) {
        for (Organisation organisation : Arrays.asList(organisation1, organisation2, organisation3)) {
            if (organisation.getOrganisationIdentifier().equals(organisationIdentifier)) {
                return organisation;
            }
        }
        return null;
    }
}
