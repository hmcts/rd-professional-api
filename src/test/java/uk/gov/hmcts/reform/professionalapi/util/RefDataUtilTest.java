package uk.gov.hmcts.reform.professionalapi.util;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import feign.Request;
import feign.Response;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

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
import uk.gov.hmcts.reform.professionalapi.controller.request.RetrieveUserProfilesRequest;
import uk.gov.hmcts.reform.professionalapi.controller.response.GetUserProfileResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.NewUserResponse;
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
    private JsonFeignResponseUtil jsonFeignResponseUtil;


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
        jsonFeignResponseUtil = mock(JsonFeignResponseUtil.class);
    }

    @Test
    public void test_shouldReturnPaymentAccountsFromUserAccountMap() {
        List<UserAccountMap> userAccountMaps = new ArrayList<>();
        userAccountMaps.add(userAccountMap);

        List<PaymentAccount> paymentAccounts = RefDataUtil.getPaymentAccountsFromUserAccountMap(userAccountMaps);
        assertThat(paymentAccounts).isNotNull();
        assertThat(paymentAccounts.size()).isPositive();
    }

    @Test
    public void test_shouldReturnPaymentAccountsFromUserAccountMa_WhenUserAccountMapIdPaymentAccountIsEmpty() {
        UserAccountMapId userAccountMapId = new UserAccountMapId(null, null);
        UserAccountMap userAccountMap = new UserAccountMap(userAccountMapId);

        List<UserAccountMap> userAccountMaps = new ArrayList<>();
        userAccountMaps.add(userAccountMap);

        List<PaymentAccount> paymentAccounts = RefDataUtil.getPaymentAccountsFromUserAccountMap(userAccountMaps);
        assertThat(paymentAccounts).isNotNull();
        assertThat(paymentAccounts.size()).isGreaterThan(0);
    }

    @Test
    public void test_shouldReturnPaymentAccountsFromUserAccountMapWhenListIsEmpty() {
        List<UserAccountMap> userAccountMaps = new ArrayList<>();

        List<PaymentAccount> paymentAccounts = RefDataUtil.getPaymentAccountsFromUserAccountMap(userAccountMaps);
        assertThat(paymentAccounts).isNotNull();
        assertThat(paymentAccounts.size()).isZero();
    }

    @Test
    public void test_shouldReturnPaymentAccountFromUserMap() {
        List<PaymentAccount> userMapPaymentAccount = new ArrayList<>();
        userMapPaymentAccount.add(paymentAccount);

        List<PaymentAccount> paymentAccountsEntity = new ArrayList<>();
        paymentAccountsEntity.add(paymentAccount);

        List<PaymentAccount> paymentAccounts = RefDataUtil.getPaymentAccountFromUserMap(userMapPaymentAccount, paymentAccountsEntity);

        assertThat(paymentAccounts.size()).isPositive();
    }

    @Test
    public void test_shouldReturnPaymentAccountFromOrganisationUser() {
        List<PaymentAccount> paymentAccountsEntity = new ArrayList<>();
        paymentAccountsEntity.add(paymentAccount);

        if (!paymentAccountsEntity.isEmpty()) {
            List<PaymentAccount> paymentAccounts = RefDataUtil.getPaymentAccount(paymentAccountsEntity);
            assertThat(paymentAccounts.size()).isPositive();
        }
    }

    @Test
    public void test_removeEmptyWhiteSpaces() {
        assertThat(RefDataUtil.removeEmptySpaces(" Test ")).isEqualTo("Test");
        assertThat(RefDataUtil.removeEmptySpaces(null)).isNull();
        assertThat(RefDataUtil.removeEmptySpaces(" Te  st ")).isEqualTo("Te st");
    }

    @Test
    public void test_removeAllWhiteSpaces() {
        assertThat(RefDataUtil.removeAllSpaces(" T e s t    1 ")).isEqualTo("Test1");
        assertThat(RefDataUtil.removeAllSpaces(null)).isNull();
    }

    @Test(expected = AccessDeniedException.class)
    public void test_shouldReturnTrueValidateOrgIdentifier() {
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
        assertThat(responseUser.getIdamStatus()).isNull();
        assertThat(responseUser.getUserIdentifier()).isNull();
        assertThat(responseUser.getRoles()).isNull();
        assertThat(responseUser.getIdamStatusCode()).isNull();
        assertThat(responseUser.getIdamMessage()).isNull();
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
        ResponseEntity<Object> realResponseEntity = new ResponseEntity<>(professionalUsersEntityResponse, header, HttpStatus.OK);

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
        ResponseEntity<Object> realResponseEntity = new ResponseEntity<>(professionalUsersEntityResponse, header, HttpStatus.BAD_REQUEST);

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
        ResponseEntity<Object> realResponseEntity = new ResponseEntity<>(professionalUsersEntityResponse, header, HttpStatus.OK);

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

        verify(pageMock, times(1)).getTotalElements();
        verify(pageMock, times(1)).getTotalPages();
        verify(pageableMock, times(1)).getPageNumber();
        verify(pageableMock, times(1)).getPageSize();
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

        verify(pageMock, times(1)).getTotalElements();
        verify(pageMock, times(1)).getTotalPages();
        verify(pageableMock, times(1)).getPageNumber();
        verify(pageableMock, times(1)).getPageSize();
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

        assertThat(response).isEqualTo("true");
    }

    @Test
    public void test_getShowDeletedValueFalse() {
        String showDeleted = "false";
        String response = RefDataUtil.getShowDeletedValue(showDeleted);

        assertThat(response).isEqualTo("false");
    }

    @Test
    public void test_privateConstructor() throws Exception {
        Constructor<RefDataUtil> constructor = RefDataUtil.class.getDeclaredConstructor();
        assertTrue(Modifier.isPrivate(constructor.getModifiers()));
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
    public void test_decodeResponseFromUp_WithResponseStatus300() {
        Map<String, Collection<String>> header = new HashMap<>();
        Collection<String> list = new ArrayList<>();
        header.put("content-encoding", list);
        String body = "{"
                + "  \"statusUpdateResponse\": {"
                + "  \"idamStatusCode\": \"200\","
                + "  \"idamMessage\": \"Success\""
                + "  } "
                + "}";

        Response response = Response.builder().status(300).reason("").headers(header).body(body, UTF_8).request(mock(Request.class)).build();
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
        activeOrganisationDtls.put("1", organisation);
        activeOrganisationDtls.put("2", organisation);
        activeOrganisationDtls.put("3", organisation);
        ResponseEntity<?> realResponseEntity = new ResponseEntity<>(professionalUsersEntityResponse, header, HttpStatus.OK);
        Map<String, Organisation> response = RefDataUtil.updateUserDetailsForActiveOrganisation(realResponseEntity, activeOrganisationDtls);

        Organisation organisationRes = response.get("1");
        assertEquals(organisation, organisationRes);

        SuperUser item = users.get(0);
        assertNull(item.getId());
        assertEquals("fName", item.getFirstName());
        assertEquals("lName", item.getLastName());
        assertEquals("some@email.com", item.getEmailAddress());
        assertNull(item.getOrganisation().getId());
        assertEquals("Org-Name", item.getOrganisation().getName());
    }

    @Test
    public void test_GetSingleUserIdFromUserProfile() throws Exception {
        Map<String, Collection<String>> header = new HashMap<>();
        Collection<String> list = new ArrayList<>();
        header.put("content-encoding", list);
        UserProfile profile = new UserProfile(UUID.randomUUID().toString(), "some@email.com", "firstName", "lastName", IdamStatus.ACTIVE);
        GetUserProfileResponse userProfileResponse = new GetUserProfileResponse(profile, false);

        ObjectMapper mapper = new ObjectMapper();
        String body = mapper.writeValueAsString(userProfileResponse);
        Response response = Response.builder().status(200).reason("OK").headers(header).body(body, UTF_8).request(mock(Request.class)).build();
        when(userProfileFeignClient.getUserProfileById(any())).thenReturn(response);

        ProfessionalUser result = RefDataUtil.getSingleUserIdFromUserProfile(new ProfessionalUser("firstName", "lastName", "some@email.com", new Organisation("name", OrganisationStatus.PENDING, "sraId", "companyNumber", Boolean.TRUE, "companyUrl")), userProfileFeignClient, Boolean.TRUE);
        assertThat(result).isNotNull();
        assertThat(result.getFirstName()).isEqualTo("firstName");
        assertThat(result.getLastName()).isEqualTo("lastName");
        assertThat(result.getEmailAddress()).isEqualTo("some@email.com");
        verify(userProfileFeignClient, times(1)).getUserProfileById(any());
    }

    @Test
    public void test_GetSingleUserIdFromUserProfile_WhenResponseIs300() throws Exception {
        Map<String, Collection<String>> header = new HashMap<>();
        Collection<String> list = new ArrayList<>();
        header.put("content-encoding", list);
        UserProfile profile = new UserProfile(UUID.randomUUID().toString(), "some@email.com", "firstName", "lastName", IdamStatus.ACTIVE);
        GetUserProfileResponse userProfileResponse = new GetUserProfileResponse(profile, false);

        ObjectMapper mapper = new ObjectMapper();
        String body = mapper.writeValueAsString(userProfileResponse);
        Response response = Response.builder().status(300).reason("").headers(header).body(body, UTF_8).request(mock(Request.class)).build();
        when(userProfileFeignClient.getUserProfileById(any())).thenReturn(response);

        ProfessionalUser result = RefDataUtil.getSingleUserIdFromUserProfile(new ProfessionalUser("firstName", "lastName", "some@email.com", new Organisation("name", OrganisationStatus.PENDING, "sraId", "companyNumber", Boolean.TRUE, "companyUrl")), userProfileFeignClient, Boolean.TRUE);
        assertThat(result).isNotNull();
        assertThat(result.getFirstName()).isEqualTo("firstName");
        assertThat(result.getLastName()).isEqualTo("lastName");
        assertThat(result.getEmailAddress()).isEqualTo("some@email.com");
        verify(userProfileFeignClient, times(1)).getUserProfileById(any());
    }

    @Test
    public void test_getMultipleUserProfilesFromUp() throws JsonProcessingException {
        Map<String, Organisation> activeOrganisationDetails = new ConcurrentHashMap<>();
        activeOrganisationDetails.put("someId", organisation);

        Map<String, Collection<String>> header = new HashMap<>();
        Collection<String> list = new ArrayList<>();
        header.put("content-encoding", list);
        UserProfile profile = new UserProfile(UUID.randomUUID().toString(), "some@email.com", "firstName", "lastName", IdamStatus.ACTIVE);
        GetUserProfileResponse userProfileResponse = new GetUserProfileResponse(profile, false);

        ObjectMapper mapper = new ObjectMapper();
        String body = mapper.writeValueAsString(userProfileResponse);

        Response realResponse = Response.builder().status(200).reason("OK").headers(header).body(body, UTF_8).request(mock(Request.class)).build();
        Response response = mock(Response.class);
        when(response.body()).thenReturn(realResponse.body());
        when(response.status()).thenReturn(realResponse.status());
        when(userProfileFeignClient.getUserProfiles(any(), any(), any())).thenReturn(response);

        List<Organisation> orgResponse = RefDataUtil.getMultipleUserProfilesFromUp(userProfileFeignClient, mock(RetrieveUserProfilesRequest.class), "true", activeOrganisationDetails);
        assertThat(orgResponse).isNotNull();
        assertThat(orgResponse.get(0).getOrganisationIdentifier()).isEqualTo(organisation.getOrganisationIdentifier());
        verify(userProfileFeignClient, times(1)).getUserProfiles(any(), any(), any());
        verify(response, times(1)).body();
        verify(response, times(3)).status();
        verify(response, times(1)).close();
    }

    @Test
    public void test_getMultipleUserProfilesFromUp_ResponseStatusIs300() throws JsonProcessingException {
        Map<String, Organisation> activeOrganisationDetails = new ConcurrentHashMap<>();
        activeOrganisationDetails.put("someId", organisation);

        Map<String, Collection<String>> header = new HashMap<>();
        Collection<String> list = new ArrayList<>();
        header.put("content-encoding", list);
        UserProfile profile = new UserProfile(UUID.randomUUID().toString(), "some@email.com", "firstName", "lastName", IdamStatus.ACTIVE);
        GetUserProfileResponse userProfileResponse = new GetUserProfileResponse(profile, false);

        ObjectMapper mapper = new ObjectMapper();
        String body = mapper.writeValueAsString(userProfileResponse);

        Response response = Response.builder().status(300).reason("").headers(header).body(body, UTF_8).request(mock(Request.class)).build();
        when(userProfileFeignClient.getUserProfiles(any(), any(), any())).thenReturn(response);

        List<Organisation> orgResponse = RefDataUtil.getMultipleUserProfilesFromUp(userProfileFeignClient, mock(RetrieveUserProfilesRequest.class), "true", activeOrganisationDetails);
        assertThat(orgResponse).isNotNull();
        verify(userProfileFeignClient, times(1)).getUserProfiles(any(), any(), any());
    }


    @Test(expected = ExternalApiException.class)
    public void test_GetSingleUserIdFromUserProfileForException() throws Exception {
        Map<String, Collection<String>> header = new HashMap<>();
        Collection<String> list = new ArrayList<>();
        header.put("content-encoding", list);
        String body = "{"
                + "  \"errorMessage\": \"400\","
                + "  \"errorDescription\": \"BAD REQUEST\","
                + "  \"timeStamp\": \"23:10\""
                + "}";

        Response realResponse = Response.builder().status(500).reason("INTERNAL SERVER EERROR").headers(header).body(body, UTF_8).request(mock(Request.class)).build();
        Response response = mock(Response.class);
        when(response.body()).thenReturn(realResponse.body());
        when(response.status()).thenReturn(realResponse.status());
        when(userProfileFeignClient.getUserProfileById(any())).thenReturn(response);

        ProfessionalUser result = RefDataUtil.getSingleUserIdFromUserProfile(new ProfessionalUser("firstName", "lastName", "emailAddress", new Organisation("name", OrganisationStatus.PENDING, "sraId", "companyNumber", Boolean.TRUE, "companyUrl")), userProfileFeignClient, Boolean.TRUE);
        assertThat(result).isNotNull();
        verify(userProfileFeignClient, times(1)).getUserProfileById(any());
        verify(response, times(1)).body();
        verify(response, times(2)).status();
        verify(response, times(1)).close();
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

    @Test
    public void test_findUserProfileStatusByEmail() {
        Map<String, Collection<String>> header = new HashMap<>();
        Collection<String> list = new ArrayList<>();
        header.put("content-encoding", list);
        String body = "{"
                + "  \"userIdentifier\": \"1cb88d5f-ef2c-4587-aca0-f77a7f6f3742\","
                + "  \"idamStatus\": \"ACTIVE\""
                + "}";

        Response response = Response.builder().status(200).reason("OK").headers(header).body(body, UTF_8).request(mock(Request.class)).build();
        when(userProfileFeignClient.getUserProfileByEmail("some_email@hotmail.com")).thenReturn(response);


        NewUserResponse newUserResponse = RefDataUtil.findUserProfileStatusByEmail("some_email@hotmail.com", userProfileFeignClient);

        assertThat(newUserResponse).isNotNull();
        assertThat(newUserResponse.getIdamStatus()).isEqualTo("ACTIVE");
        assertThat(newUserResponse.getUserIdentifier()).isEqualTo("1cb88d5f-ef2c-4587-aca0-f77a7f6f3742");
        verify(userProfileFeignClient, times(1)).getUserProfileByEmail(any());
    }

    @Test
    public void test_findUserProfileStatusByEmail_WithResponse400() {
        Map<String, Collection<String>> header = new HashMap<>();
        Collection<String> list = new ArrayList<>();
        header.put("content-encoding", list);
        String body = "{" + "}";

        Response response = Response.builder().status(400).reason("BAD REQUEST").headers(header).body(body, UTF_8).request(mock(Request.class)).build();
        when(userProfileFeignClient.getUserProfileByEmail("some_email@hotmail.com")).thenReturn(response);

        NewUserResponse newUserResponse = RefDataUtil.findUserProfileStatusByEmail("some_email@hotmail.com", userProfileFeignClient);

        assertThat(newUserResponse).isNotNull();
        assertThat(newUserResponse.getIdamStatus()).isNull();
        assertThat(newUserResponse.getUserIdentifier()).isNull();
        verify(userProfileFeignClient, times(1)).getUserProfileByEmail(any());
    }

    @Test(expected = ExternalApiException.class)
    public void test_findUserProfileStatusByEmail_Returns500_WhenExternalApiException() {
        FeignException feignException = mock(FeignException.class);
        when(feignException.status()).thenReturn(500);

        Map<String, Collection<String>> header = new HashMap<>();
        Collection<String> list = new ArrayList<>();
        header.put("content-encoding", list);
        String body = "{"
                + "  \"userIdentifier\": \"1cb88d5f-ef2c-4587-aca0-f77a7f6f3742\","
                + "  \"idamStatus\": \"ACTIVE\""
                + "}";

        when(userProfileFeignClient.getUserProfileByEmail("some_email@hotmail.com")).thenThrow(feignException);

        RefDataUtil.findUserProfileStatusByEmail("some_email@hotmail.com", userProfileFeignClient);
        verify(userProfileFeignClient, times(1)).getUserProfileByEmail(any());
    }

    @Test
    public void test_getUserIdFromUserProfile() throws JsonProcessingException {
        SuperUser superUser = new SuperUser("fName", "lName", "someone@email.com", organisation);
        List<SuperUser> users = Arrays.asList(superUser);

        Map<String, Collection<String>> header = new HashMap<>();
        Collection<String> list = new ArrayList<>();
        header.put("content-encoding", list);
        UserProfile profile = new UserProfile(UUID.randomUUID().toString(), "some@email.com", "firstName", "lastName", IdamStatus.ACTIVE);
        GetUserProfileResponse userProfileResponse = new GetUserProfileResponse(profile, false);

        ObjectMapper mapper = new ObjectMapper();
        String body = mapper.writeValueAsString(userProfileResponse);

        Response response = Response.builder().status(200).reason("OK").headers(header).body(body, UTF_8).request(mock(Request.class)).build();
        when(userProfileFeignClient.getUserProfileById(any())).thenReturn(response);

        List<SuperUser> userProfileDtls = RefDataUtil.getUserIdFromUserProfile(users, userProfileFeignClient, true);

        assertThat(userProfileDtls).isNotNull();
        assertThat(userProfileDtls.size()).isNotZero();
        verify(userProfileFeignClient, times(1)).getUserProfileById(any());

    }
}