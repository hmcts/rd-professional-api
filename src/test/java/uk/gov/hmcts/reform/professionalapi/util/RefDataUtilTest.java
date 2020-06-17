package uk.gov.hmcts.reform.professionalapi.util;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import feign.Request;
import feign.Response;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import uk.gov.hmcts.reform.professionalapi.controller.advice.ExternalApiException;
import uk.gov.hmcts.reform.professionalapi.controller.advice.ResourceNotFoundException;
import uk.gov.hmcts.reform.professionalapi.controller.constants.IdamStatus;
import uk.gov.hmcts.reform.professionalapi.controller.feign.UserProfileFeignClient;
import uk.gov.hmcts.reform.professionalapi.controller.response.GetUserProfileResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.ProfessionalUsersEntityResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.ProfessionalUsersResponse;
import uk.gov.hmcts.reform.professionalapi.domain.ModifyUserRolesResponse;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus;
import uk.gov.hmcts.reform.professionalapi.domain.PaymentAccount;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;
import uk.gov.hmcts.reform.professionalapi.domain.SuperUser;
import uk.gov.hmcts.reform.professionalapi.domain.UserAccountMap;
import uk.gov.hmcts.reform.professionalapi.domain.UserAccountMapId;
import uk.gov.hmcts.reform.professionalapi.domain.UserProfile;

public class RefDataUtilTest {

    private PaymentAccount paymentAccount;
    private Organisation organisation;
    private ProfessionalUser professionalUser;
    private UserAccountMapId userAccountMapId;
    private UserAccountMap userAccountMap;
    private UserProfile profile;
    private GetUserProfileResponse getUserProfileResponse;
    private UserProfileFeignClient userProfileFeignClient;


    @Before
    public void setUp() {
        paymentAccount = new PaymentAccount("PBA1234567");
        organisation = new Organisation("Org-Name", OrganisationStatus.PENDING, "sra-id", "companyN", false, "www.org.com");
        professionalUser = new ProfessionalUser("some-fname", "some-lname", "soMeone@somewhere.com", organisation);
        professionalUser.setRoles(asList("pui-user-manager", "pui-case-manager"));
        userAccountMapId = new UserAccountMapId(professionalUser, paymentAccount);
        userAccountMap = new UserAccountMap(userAccountMapId);
        profile = new UserProfile(UUID.randomUUID().toString(), "email@org.com", "firstName", "lastName", IdamStatus.ACTIVE);
        getUserProfileResponse = new GetUserProfileResponse(profile, true);
        getUserProfileResponse.setRoles(singletonList("pui-case-manager"));
        getUserProfileResponse.setIdamStatusCode("400");
        getUserProfileResponse.setIdamMessage("BAD REQUEST");
        paymentAccount.setId(UUID.randomUUID());
        userProfileFeignClient = mock(UserProfileFeignClient.class);
    }

    @Test
    public void shouldReturnPaymentAccountsFromUserAccountMap() {
        List<UserAccountMap> userAccountMaps = new ArrayList<>();
        userAccountMaps.add(userAccountMap);

        List<PaymentAccount> paymentAccounts = RefDataUtil.getPaymentAccountsFromUserAccountMap(userAccountMaps);
        assertThat(paymentAccounts.size()).isGreaterThan(0);
    }

    @Test
    public void shouldReturnPaymentAccountsFromUserAccountMapWhenListIsEmpty() {
        List<UserAccountMap> userAccountMaps = new ArrayList<>();

        List<PaymentAccount> paymentAccounts = RefDataUtil.getPaymentAccountsFromUserAccountMap(userAccountMaps);
        assertThat(paymentAccounts.size()).isEqualTo(0);
    }

    @Test
    public void shouldReturnPaymentAccountFromUserMap() {
        List<PaymentAccount> userMapPaymentAccount = new ArrayList<>();
        userMapPaymentAccount.add(paymentAccount);

        List<PaymentAccount> paymentAccountsEntity = new ArrayList<>();
        paymentAccountsEntity.add(paymentAccount);

        List<PaymentAccount> paymentAccounts = RefDataUtil.getPaymentAccountFromUserMap(userMapPaymentAccount, paymentAccountsEntity);

        assertThat(paymentAccounts.size()).isGreaterThan(0);
    }

    @Test
    public void shouldReturnPaymentAccountFromOrganisationUser() {
        List<PaymentAccount> paymentAccountsEntity = new ArrayList<>();
        paymentAccountsEntity.add(paymentAccount);

        if (!paymentAccountsEntity.isEmpty()) {
            List<PaymentAccount> paymentAccounts = RefDataUtil.getPaymentAccount(paymentAccountsEntity);
            assertThat(paymentAccounts.size()).isGreaterThan(0);
        }
    }

    @Test
    public void removeEmptyWhiteSpacesTest() {
        assertThat(RefDataUtil.removeEmptySpaces(" Test ")).isEqualTo("Test");
        assertThat(RefDataUtil.removeEmptySpaces(null)).isEqualTo(null);
        assertThat(RefDataUtil.removeEmptySpaces(" Te  st ")).isEqualTo("Te st");

    }

    @Test
    public void removeAllWhiteSpacesTest() {
        assertThat(RefDataUtil.removeAllSpaces(" T e s t    1 ")).isEqualTo("Test1");
        assertThat(RefDataUtil.removeAllSpaces(null)).isEqualTo(null);

    }

    @Test(expected = AccessDeniedException.class)
    public void shouldReturnTrueValidateOrgIdentifier() {
        String uuid = UUID.randomUUID().toString();
        RefDataUtil.validateOrgIdentifier(uuid, UUID.randomUUID().toString());
    }

    @Test
    public void test_mapUserInfoCorrectly_with_roles() {

        HttpHeaders header = new HttpHeaders();
        header.setContentType(APPLICATION_JSON);
        ResponseEntity<?> realResponseEntity = new ResponseEntity<>(getUserProfileResponse, header, HttpStatus.OK);

        ProfessionalUser responseUser = RefDataUtil.mapUserInfo(professionalUser, realResponseEntity, true);
        assertThat(responseUser).isNotNull();
        assertThat(responseUser.getEmailAddress()).isEqualTo(profile.getEmail());
        assertThat(responseUser.getFirstName()).isEqualTo(profile.getFirstName());
        assertThat(responseUser.getLastName()).isEqualTo(profile.getLastName());
        assertThat(responseUser.getIdamStatus()).isEqualTo(profile.getIdamStatus());
        assertThat(responseUser.getUserIdentifier()).isEqualTo(profile.getIdamId());
        assertThat(responseUser.getRoles()).contains(professionalUser.getRoles().get(0));
        assertThat(getUserProfileResponse.getIdamStatusCode()).isEqualTo(getUserProfileResponse.getIdamStatusCode());
        assertThat(getUserProfileResponse.getIdamMessage()).isEqualTo(getUserProfileResponse.getIdamMessage());
    }

    @Test
    public void test_mapUserInfo_without_roles() {

        HttpHeaders header = new HttpHeaders();
        header.setContentType(APPLICATION_JSON);
        ResponseEntity<?> realResponseEntity = new ResponseEntity<>(getUserProfileResponse, header, HttpStatus.OK);

        ProfessionalUser responseUser = RefDataUtil.mapUserInfo(new ProfessionalUser(), realResponseEntity, false);

        assertThat(responseUser).isNotNull();
        assertThat(responseUser.getEmailAddress()).isEqualTo(profile.getEmail());
        assertThat(responseUser.getFirstName()).isEqualTo(profile.getFirstName());
        assertThat(responseUser.getLastName()).isEqualTo(profile.getLastName());
        assertThat(responseUser.getIdamStatus()).isEqualTo(null);
        assertThat(responseUser.getUserIdentifier()).isEqualTo(null);
        assertThat(responseUser.getRoles()).isNull();
        assertThat(responseUser.getIdamStatusCode()).isEqualTo(null);
        assertThat(responseUser.getIdamMessage()).isEqualTo(null);
    }

    @Test
    public void test_filterUsersByStatus() {
        ProfessionalUsersResponse professionalUsersResponse = new ProfessionalUsersResponse(new ProfessionalUser("fName", "lName", "some@email.com", organisation));
        ProfessionalUsersResponse professionalUsersResponse1 = new ProfessionalUsersResponse(new ProfessionalUser("fName1", "lName1", "some1@email.com", organisation));
        ProfessionalUsersResponse professionalUsersResponse2 = new ProfessionalUsersResponse(new ProfessionalUser("fName2", "lName2", "some2@email.com", organisation));

        professionalUsersResponse.setIdamStatus(IdamStatus.ACTIVE.toString());
        professionalUsersResponse1.setIdamStatus(IdamStatus.ACTIVE.toString());
        professionalUsersResponse2.setIdamStatus(IdamStatus.PENDING.toString());

        List<ProfessionalUsersResponse> userProfiles = asList(professionalUsersResponse, professionalUsersResponse1, professionalUsersResponse2);

        ProfessionalUsersEntityResponse professionalUsersEntityResponse = new ProfessionalUsersEntityResponse();
        professionalUsersEntityResponse.setUserProfiles(userProfiles);

        HttpHeaders header = new HttpHeaders();
        header.setContentType(APPLICATION_JSON);
        ResponseEntity<?> realResponseEntity = new ResponseEntity<>(professionalUsersEntityResponse, header, HttpStatus.OK);

        ProfessionalUsersEntityResponse professionalUsersEntityResponse1 = RefDataUtil.filterUsersByStatus(realResponseEntity, "Active");
        assertThat(professionalUsersEntityResponse1).isNotNull();

        assertThat(professionalUsersEntityResponse1.getUserProfiles().size()).isEqualTo(2);
        assertThat(professionalUsersEntityResponse1.getUserProfiles().get(0)).isEqualTo(professionalUsersResponse);
        assertThat(professionalUsersEntityResponse1.getUserProfiles().get(1)).isEqualTo(professionalUsersResponse1);
    }

    @Test(expected = ExternalApiException.class)
    public void test_filterUsersByStatusWhenStatusCodeIsNot200() {
        ProfessionalUsersResponse professionalUsersResponse = new ProfessionalUsersResponse(new ProfessionalUser("fName", "lName", "some@email.com", organisation));
        ProfessionalUsersResponse professionalUsersResponse1 = new ProfessionalUsersResponse(new ProfessionalUser("fName1", "lName1", "some1@email.com", organisation));
        ProfessionalUsersResponse professionalUsersResponse2 = new ProfessionalUsersResponse(new ProfessionalUser("fName2", "lName2", "some2@email.com", organisation));

        professionalUsersResponse.setIdamStatus(IdamStatus.ACTIVE.toString());
        professionalUsersResponse1.setIdamStatus(IdamStatus.ACTIVE.toString());
        professionalUsersResponse2.setIdamStatus(IdamStatus.PENDING.toString());

        List<ProfessionalUsersResponse> userProfiles = asList(professionalUsersResponse, professionalUsersResponse1, professionalUsersResponse2);

        ProfessionalUsersEntityResponse professionalUsersEntityResponse = new ProfessionalUsersEntityResponse();
        professionalUsersEntityResponse.setUserProfiles(userProfiles);

        HttpHeaders header = new HttpHeaders();
        header.setContentType(APPLICATION_JSON);
        ResponseEntity<?> realResponseEntity = new ResponseEntity<>(professionalUsersEntityResponse, header, HttpStatus.BAD_REQUEST);

        ProfessionalUsersEntityResponse professionalUsersEntityResponse1 = RefDataUtil.filterUsersByStatus(realResponseEntity, "Active");
        assertThat(professionalUsersEntityResponse1).isNotNull();

        assertThat(professionalUsersEntityResponse1.getUserProfiles().size()).isEqualTo(3);
        assertThat(professionalUsersEntityResponse1.getUserProfiles().get(0)).isEqualTo(professionalUsersResponse);
        assertThat(professionalUsersEntityResponse1.getUserProfiles().get(1)).isEqualTo(professionalUsersResponse1);
        assertThat(professionalUsersEntityResponse1.getUserProfiles().get(2)).isEqualTo(professionalUsersResponse2);
    }

    @Test(expected = ResourceNotFoundException.class)
    public void test_filterUsersByStatusWhereNoUsersFoundThrowsResourceNotFoundException() {
        ProfessionalUsersEntityResponse professionalUsersEntityResponse = new ProfessionalUsersEntityResponse();
        List<ProfessionalUsersResponse> userProfiles = new ArrayList<>();
        professionalUsersEntityResponse.setUserProfiles(userProfiles);

        HttpHeaders header = new HttpHeaders();
        header.setContentType(APPLICATION_JSON);
        ResponseEntity<?> realResponseEntity = new ResponseEntity<>(professionalUsersEntityResponse, header, HttpStatus.OK);

        RefDataUtil.filterUsersByStatus(realResponseEntity, "Active");
    }

    @Test
    public void test_shouldGenerateResponseEntityWithHeaderFromPage() {
        HttpHeaders responseHeader = new HttpHeaders();
        responseHeader.add("fakeHeader", "fakeValue");

        ResponseEntity<?> realResponseEntity = new ResponseEntity<>(null, responseHeader, HttpStatus.OK);

        Page pageMock = mock(Page.class);
        when(pageMock.getTotalElements()).thenReturn(1L);
        when(pageMock.getTotalPages()).thenReturn(2);

        Pageable pageableMock = mock(Pageable.class);
        when(pageableMock.getPageNumber()).thenReturn(0);
        when(pageableMock.getPageSize()).thenReturn(2);

        HttpHeaders httpHeaders = RefDataUtil.generateResponseEntityWithPaginationHeader(pageableMock, pageMock, realResponseEntity);

        assertThat(httpHeaders.containsKey("fakeHeader")).isTrue();
        assertThat(httpHeaders.containsKey("paginationInfo")).isTrue();
    }

    @Test
    public void test_shouldGenerateResponseEntityWithHeaderFromPageWhenResponseEntityIsNull() {
        Page pageMock = mock(Page.class);
        when(pageMock.getTotalElements()).thenReturn(1L);
        when(pageMock.getTotalPages()).thenReturn(2);

        Pageable pageableMock = mock(Pageable.class);
        when(pageableMock.getPageNumber()).thenReturn(0);
        when(pageableMock.getPageSize()).thenReturn(2);

        HttpHeaders httpHeaders = RefDataUtil.generateResponseEntityWithPaginationHeader(pageableMock, pageMock, null);

        assertThat(httpHeaders.containsKey("paginationInfo")).isTrue();
    }

    @Test
    public void test_shouldCreatePageableObject() {
        Integer page = 0;
        Integer size = 5;
        Sort sort = mock(Sort.class);

        Pageable pageable = RefDataUtil.createPageableObject(page, size, sort);

        assertThat(pageable).isNotNull();
        assertThat(pageable.getPageSize()).isEqualTo(5);
    }

    @Test
    public void test_shouldCreatePageableObjectWithDefaultPageSize() {
        Integer page = 0;
        Sort sort = mock(Sort.class);

        Pageable pageable = RefDataUtil.createPageableObject(page, null, sort);

        assertThat(pageable).isNotNull();
        assertThat(pageable.getPageSize()).isEqualTo(10);
    }

    @Test
    public void test_getShowDeletedValueTrue() {
        String showDeleted = "True";
        String response = RefDataUtil.getShowDeletedValue(showDeleted);

        assertThat(response.equals("true")).isTrue();
    }

    @Test
    public void test_getShowDeletedValueFalse() {
        String showDeleted = "false";
        String response = RefDataUtil.getShowDeletedValue(showDeleted);

        assertThat(response.equals("false")).isTrue();
    }

    @Test
    public void test_getReturnRolesValueTrue() {
        String returnRoles = "True";
        String response = RefDataUtil.getReturnRolesValue(returnRoles);

        assertThat(response.equals("true")).isTrue();
    }

    @Test
    public void test_getReturnRolesValueFalse() {
        String returnRoles = "false";
        String response = RefDataUtil.getReturnRolesValue(returnRoles);

        assertThat(response.equals("false")).isTrue();
    }

    @Test
    public void privateConstructorTest() throws Exception {
        Constructor<RefDataUtil> constructor = RefDataUtil.class.getDeclaredConstructor();
        assertFalse(constructor.isAccessible());
        constructor.setAccessible(true);
        constructor.newInstance((Object[]) null);
    }

    @Test
    public void test_decodeResponseFromUp() {
        Map<String, Collection<String>> header = new HashMap<>();
        Collection<String> list = new ArrayList<>();
        header.put("content-encoding", list);
        String body = "{"
            + "  \"statusUpdateResponse\": {"
            + "  \"idamStatusCode\": \"200\","
            + "  \"idamMessage\": \"Success\""
            + "  } "
            + "}";

        Response response = Response.builder().status(200).reason("OK").headers(header).body(body, UTF_8).request(mock(Request.class)).build();
        ModifyUserRolesResponse modifyUserRolesResponse = RefDataUtil.decodeResponseFromUp(response);
        assertThat(modifyUserRolesResponse.getStatusUpdateResponse().getIdamStatusCode()).isEqualTo("200");
        assertThat(modifyUserRolesResponse.getStatusUpdateResponse().getIdamMessage()).isEqualTo("Success");
    }

    @Test
    public void test_decodeResponseFromUp_with_UP_failed() {
        Map<String, Collection<String>> header = new HashMap<>();
        Collection<String> list = new ArrayList<>();
        header.put("content-encoding", list);
        String body = "{"
            + "  \"errorMessage\": \"400\","
            + "  \"errorDescription\": \"BAD REQUEST\","
            + "  \"timeStamp\": \"23:10\""
            + "}";

        Response response = Response.builder().status(400).reason("BAD REQUEST").headers(header).body(body, UTF_8).request(mock(Request.class)).build();
        ModifyUserRolesResponse modifyUserRolesResponse = RefDataUtil.decodeResponseFromUp(response);
        assertThat(modifyUserRolesResponse.getErrorResponse().getErrorMessage()).isEqualTo("400");
        assertThat(modifyUserRolesResponse.getErrorResponse().getErrorDescription()).isEqualTo("BAD REQUEST");
        assertThat(modifyUserRolesResponse.getErrorResponse().getTimeStamp()).isEqualTo("23:10");
    }

    @Test
    public void test_updateUserDetailsForActiveOrganisation_entity_reponse_null() {
        HttpHeaders header = new HttpHeaders();
        header.setContentType(APPLICATION_JSON);

        Map<String, Organisation> activeOrganisationDtls = new HashMap<>();
        ResponseEntity<?> realResponseEntity = new ResponseEntity<>(null, header, HttpStatus.OK);
        Map<String, Organisation> response = RefDataUtil.updateUserDetailsForActiveOrganisation(realResponseEntity, activeOrganisationDtls);
        assertThat(response).isEmpty();

    }

    @Test
    public void test_updateUserDetailsForActiveOrganisation_entity_reponse_empty() {
        ProfessionalUsersResponse professionalUsersResponse = new ProfessionalUsersResponse(new ProfessionalUser("fName", "lName", "some@email.com", organisation));
        ProfessionalUsersResponse professionalUsersResponse1 = new ProfessionalUsersResponse(new ProfessionalUser("fName1", "lName1", "some1@email.com", organisation));
        ProfessionalUsersResponse professionalUsersResponse2 = new ProfessionalUsersResponse(new ProfessionalUser("fName2", "lName2", "some2@email.com", organisation));

        professionalUsersResponse.setIdamStatus(IdamStatus.ACTIVE.toString());
        professionalUsersResponse1.setIdamStatus(IdamStatus.ACTIVE.toString());
        professionalUsersResponse2.setIdamStatus(IdamStatus.PENDING.toString());

        List<ProfessionalUsersResponse> userProfiles = asList(professionalUsersResponse, professionalUsersResponse1, professionalUsersResponse2);
        ProfessionalUsersEntityResponse professionalUsersEntityResponse = new ProfessionalUsersEntityResponse();
        professionalUsersEntityResponse.setUserProfiles(userProfiles);
        HttpHeaders header = new HttpHeaders();
        header.setContentType(APPLICATION_JSON);

        Map<String, Organisation> activeOrganisationDtls = new HashMap<>();
        ResponseEntity<?> realResponseEntity = new ResponseEntity<>(professionalUsersEntityResponse, header, HttpStatus.OK);
        Map<String, Organisation> response = RefDataUtil.updateUserDetailsForActiveOrganisation(realResponseEntity, activeOrganisationDtls);
        assertThat(response).isEmpty();


    }


    @Test
    public void test_updateUserDetailsForActiveOrganisation() {
        ProfessionalUsersResponse professionalUsersResponse = new ProfessionalUsersResponse(new ProfessionalUser("fName", "lName", "some@email.com", organisation));
        ProfessionalUsersResponse professionalUsersResponse1 = new ProfessionalUsersResponse(new ProfessionalUser("fName", "lName", "some@email.com", organisation));
        ProfessionalUsersResponse professionalUsersResponse2 = new ProfessionalUsersResponse(new ProfessionalUser("fName", "lName", "some@email.com", organisation));
        professionalUsersResponse.setUserIdentifier("1");
        professionalUsersResponse1.setUserIdentifier("2");
        professionalUsersResponse2.setUserIdentifier("3");
        professionalUsersResponse.setIdamStatus(IdamStatus.ACTIVE.toString());
        professionalUsersResponse1.setIdamStatus(IdamStatus.ACTIVE.toString());
        professionalUsersResponse2.setIdamStatus(IdamStatus.PENDING.toString());

        List<ProfessionalUsersResponse> userProfiles = asList(professionalUsersResponse, professionalUsersResponse1, professionalUsersResponse2);
        ProfessionalUsersEntityResponse professionalUsersEntityResponse = new ProfessionalUsersEntityResponse();
        professionalUsersEntityResponse.setUserProfiles(userProfiles);

        HttpHeaders header = new HttpHeaders();
        header.setContentType(APPLICATION_JSON);
        List<SuperUser> users = new ArrayList<>();
        users.add(professionalUser.toSuperUser());
        organisation.setUsers(users);
        Map<String, Organisation> activeOrganisationDtls = new HashMap<>();
        activeOrganisationDtls.put("1",organisation);
        activeOrganisationDtls.put("2",organisation);
        activeOrganisationDtls.put("3",organisation);
        ResponseEntity<?> realResponseEntity = new ResponseEntity<>(professionalUsersEntityResponse, header, HttpStatus.OK);
        Map<String, Organisation> response = RefDataUtil.updateUserDetailsForActiveOrganisation(realResponseEntity, activeOrganisationDtls);

        Organisation organisationRes = (Organisation)response.get("1");
        assertEquals(organisation,organisationRes);

        SuperUser item = ((SuperUser)users.get(0));
        assertNull(item.getId());
        assertEquals("fName", item.getFirstName());
        assertEquals("lName", item.getLastName());
        assertEquals("some@email.com", item.getEmailAddress());
        assertNull(item.getOrganisation().getId());
        assertEquals("Org-Name", item.getOrganisation().getName());
    }

    @Test(expected = ExternalApiException.class)
    public void testGetSingleUserIdFromUserProfileForException() throws Exception {
        Map<String, Collection<String>> header = new HashMap<>();
        Collection<String> list = new ArrayList<>();
        header.put("content-encoding", list);
        String body = "{"
            + "  \"errorMessage\": \"400\","
            + "  \"errorDescription\": \"BAD REQUEST\","
            + "  \"timeStamp\": \"23:10\""
            + "}";

        Response response = Response.builder().status(400).reason("BAD REQUEST").headers(header).body(body, UTF_8).request(mock(Request.class)).build();
        when(userProfileFeignClient.getUserProfileById(any())).thenReturn(response);

        ProfessionalUser result = RefDataUtil.getSingleUserIdFromUserProfile(new ProfessionalUser("firstName", "lastName", "emailAddress", new Organisation("name", OrganisationStatus.PENDING, "sraId", "companyNumber", Boolean.TRUE, "companyUrl")), userProfileFeignClient, Boolean.TRUE);
        verify(userProfileFeignClient, times(1)).getUserProfileById(any());
    }

    @Test
    public void test_mapUserInfo_without_rolesTrue() {

        HttpHeaders header = new HttpHeaders();
        header.setContentType(APPLICATION_JSON);
        ResponseEntity<?> realResponseEntity = new ResponseEntity<>(getUserProfileResponse, header, HttpStatus.OK);
        ProfessionalUser responseUser = RefDataUtil.mapUserInfo(new ProfessionalUser(), realResponseEntity, true);
        assertNull(responseUser.getId());
        assertEquals("firstName", responseUser.getFirstName());
        assertEquals("lastName", responseUser.getLastName());
        assertEquals("email@org.com", responseUser.getEmailAddress());
        assertNull(responseUser.getOrganisation());

        assertNull(responseUser.getDeleted());
        assertNull(responseUser.getLastUpdated());
        assertNull(responseUser.getCreated());
        assertEquals("pui-case-manager", responseUser.getRoles().get(0));

        assertEquals(IdamStatus.ACTIVE, responseUser.getIdamStatus());
        assertEquals("400", responseUser.getIdamStatusCode());
        assertEquals("BAD REQUEST", responseUser.getIdamMessage());

    }
}