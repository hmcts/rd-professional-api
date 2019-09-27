package uk.gov.hmcts.reform.professionalapi.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import uk.gov.hmcts.reform.professionalapi.controller.advice.ExternalApiException;
import uk.gov.hmcts.reform.professionalapi.controller.advice.ResourceNotFoundException;
import uk.gov.hmcts.reform.professionalapi.controller.response.GetUserProfileResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.IdamStatus;
import uk.gov.hmcts.reform.professionalapi.controller.response.ProfessionalUsersEntityResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.ProfessionalUsersResponse;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.PaymentAccount;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;
import uk.gov.hmcts.reform.professionalapi.domain.UserAccountMap;
import uk.gov.hmcts.reform.professionalapi.domain.UserAccountMapId;
import uk.gov.hmcts.reform.professionalapi.util.RefDataUtil;

public class RefDataUtilTest {

    @Test
    public void shouldReturnPaymentAccountsFromUserAccountMap() {

        PaymentAccount paymentAccount = mock(PaymentAccount.class);
        UserAccountMapId userAccountMapId = mock(UserAccountMapId.class);
        UserAccountMap userAccountMap = mock(UserAccountMap.class);

        List<UserAccountMap> userAccountMaps = new ArrayList<>();
        userAccountMaps.add(userAccountMap);

        when(userAccountMap.getUserAccountMapId()).thenReturn(userAccountMapId);
        when(userAccountMapId.getPaymentAccount()).thenReturn(paymentAccount);

        List<PaymentAccount> paymentAccounts = RefDataUtil.getPaymentAccountsFromUserAccountMap(userAccountMaps);
        assertThat(paymentAccounts.size()).isGreaterThan(0);
    }

    @Test
    public void shouldReturnPaymentAccountsFromUserAccountMapWhenListIsEmpty() {
        PaymentAccount paymentAccount = mock(PaymentAccount.class);
        UserAccountMapId userAccountMapId = mock(UserAccountMapId.class);
        UserAccountMap userAccountMap = mock(UserAccountMap.class);

        List<UserAccountMap> userAccountMaps = new ArrayList<>();

        when(userAccountMap.getUserAccountMapId()).thenReturn(userAccountMapId);
        when(userAccountMapId.getPaymentAccount()).thenReturn(paymentAccount);

        List<PaymentAccount> paymentAccounts = RefDataUtil.getPaymentAccountsFromUserAccountMap(userAccountMaps);
        assertThat(paymentAccounts.size()).isEqualTo(0);
    }

    @Test
    public void shouldReturnPaymentAccountFromUserMap() {

        final UUID paymentAccountUuid = UUID.randomUUID();

        PaymentAccount paymentAccountMock = mock(PaymentAccount.class);

        when(paymentAccountMock.getId()).thenReturn(paymentAccountUuid);

        List<PaymentAccount> userMapPaymentAccount = new ArrayList<>();

        userMapPaymentAccount.add(paymentAccountMock);

        List<PaymentAccount> paymentAccountsEntity = new ArrayList<>();

        paymentAccountsEntity.add(paymentAccountMock);

        List<PaymentAccount> paymentAccounts = RefDataUtil.getPaymentAccountFromUserMap(userMapPaymentAccount, paymentAccountsEntity);

        assertThat(paymentAccounts.size()).isGreaterThan(0);
    }

    @Test
    public void shouldReturnPaymentAccountFromOrganisationUser() {

        PaymentAccount paymentAccountMock = mock(PaymentAccount.class);

        List<PaymentAccount> paymentAccountsEntity = new ArrayList<>();

        paymentAccountsEntity.add(paymentAccountMock);

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
        RefDataUtil.validateOrgIdentifier(uuid,UUID.randomUUID().toString());
    }

    @Test
    public void mapUserInfoCorrectly() {
        ProfessionalUser professionalUser = new ProfessionalUser();
        professionalUser.setFirstName("abc");
        professionalUser.setLastName("bcd");
        professionalUser.setEmailAddress("a@b.co.uk");
        professionalUser.setIdamStatusCode("200");
        professionalUser.setIdamMessage("Success");
        ResponseEntity responseEntity = mock(ResponseEntity.class);

        ProfessionalUser mappedUser = RefDataUtil.mapUserInfo(professionalUser, responseEntity, true);
        assertThat(mappedUser).isNotNull();
    }

    @Test
    public void test_mapUserInfo() {
        String id = UUID.randomUUID().toString();
        ProfessionalUser userMock = new ProfessionalUser();
        ResponseEntity responseResponseEntityMock = mock(ResponseEntity.class);
        GetUserProfileResponse getUserProfileResponseMock = mock(GetUserProfileResponse.class);
        when(responseResponseEntityMock.getBody()).thenReturn(getUserProfileResponseMock);
        when(getUserProfileResponseMock.getFirstName()).thenReturn("fname");
        when(getUserProfileResponseMock.getLastName()).thenReturn("lname");
        when(getUserProfileResponseMock.getEmail()).thenReturn("some@hmcts.net");
        when(getUserProfileResponseMock.getIdamStatus()).thenReturn(IdamStatus.ACTIVE);
        when(getUserProfileResponseMock.getIdamId()).thenReturn(id);
        when(getUserProfileResponseMock.getRoles()).thenReturn(new ArrayList<String>());
        when(getUserProfileResponseMock.getIdamStatusCode()).thenReturn("code");
        when(getUserProfileResponseMock.getIdamMessage()).thenReturn("test error message");
        ProfessionalUser responseUser = RefDataUtil.mapUserInfo(userMock, responseResponseEntityMock, true);
        assertThat(responseUser).isNotNull();
        assertThat(responseUser.getEmailAddress()).isEqualTo("some@hmcts.net");
        assertThat(responseUser.getFirstName()).isEqualTo("fname");
        assertThat(responseUser.getLastName()).isEqualTo("lname");
        assertThat(responseUser.getIdamStatus()).isEqualTo(IdamStatus.ACTIVE);
        assertThat(responseUser.getUserIdentifier()).isEqualTo(id);
        assertThat(responseUser.getRoles()).isNotNull();
        assertThat(responseUser.getUserIdentifier()).isEqualTo(id);
        assertThat(responseUser.getIdamStatusCode()).isEqualTo("code");
        assertThat(responseUser.getIdamMessage()).isEqualTo("test error message");
        assertThat(getUserProfileResponseMock.getIdamStatusCode()).isEqualTo("code");
        assertThat(getUserProfileResponseMock.getIdamMessage()).isEqualTo("test error message");
    }

    @Test
    public void test_filterUsersByStatus() {
        Organisation organisationMock = mock(Organisation.class);

        ProfessionalUsersResponse professionalUsersResponse = new ProfessionalUsersResponse(new ProfessionalUser("fName","lName", "some@email.com", organisationMock));
        ProfessionalUsersResponse professionalUsersResponse1 = new ProfessionalUsersResponse(new ProfessionalUser("fName1","lName1", "some1@email.com", organisationMock));
        ProfessionalUsersResponse professionalUsersResponse2 = new ProfessionalUsersResponse(new ProfessionalUser("fName2","lName2", "some2@email.com", organisationMock));
        professionalUsersResponse.setIdamStatus(IdamStatus.ACTIVE.toString());
        professionalUsersResponse1.setIdamStatus(IdamStatus.ACTIVE.toString());
        professionalUsersResponse2.setIdamStatus(IdamStatus.PENDING.toString());
        List<ProfessionalUsersResponse> userProfiles = new ArrayList<>();
        userProfiles.add(professionalUsersResponse);
        userProfiles.add(professionalUsersResponse1);
        userProfiles.add(professionalUsersResponse2);
        ProfessionalUsersEntityResponse professionalUsersEntityResponse = new ProfessionalUsersEntityResponse();
        professionalUsersEntityResponse.setUserProfiles(userProfiles);

        HttpHeaders header = new HttpHeaders();
        header.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<?> realResponseEntity = new ResponseEntity<>(professionalUsersEntityResponse, header, HttpStatus.OK);

        ResponseEntity responseEntity = mock(ResponseEntity.class);
        when(responseEntity.getStatusCode()).thenReturn(HttpStatus.OK);
        when(responseEntity.getBody()).thenReturn(realResponseEntity.getBody());

        ProfessionalUsersEntityResponse professionalUsersEntityResponse1 = RefDataUtil.filterUsersByStatus(responseEntity, "Active");
        assertThat(professionalUsersEntityResponse1).isNotNull();

        assertThat(professionalUsersEntityResponse1.getUserProfiles().size()).isEqualTo(2);
    }

    @Test(expected = ExternalApiException.class)
    public void test_filterUsersByStatusWhenStatusCodeIsNot200() {
        Organisation organisationMock = mock(Organisation.class);

        HttpHeaders header = new HttpHeaders();
        header.setContentType(MediaType.APPLICATION_JSON);

        ProfessionalUsersResponse professionalUsersResponse = new ProfessionalUsersResponse(new ProfessionalUser("fName","lName", "some@email.com", organisationMock));
        ProfessionalUsersResponse professionalUsersResponse1 = new ProfessionalUsersResponse(new ProfessionalUser("fName1","lName1", "some1@email.com", organisationMock));
        ProfessionalUsersResponse professionalUsersResponse2 = new ProfessionalUsersResponse(new ProfessionalUser("fName2","lName2", "some2@email.com", organisationMock));

        professionalUsersResponse.setIdamStatus(IdamStatus.ACTIVE.toString());
        professionalUsersResponse1.setIdamStatus(IdamStatus.ACTIVE.toString());
        professionalUsersResponse2.setIdamStatus(IdamStatus.PENDING.toString());

        List<ProfessionalUsersResponse> userProfiles = new ArrayList<>();
        userProfiles.add(professionalUsersResponse);
        userProfiles.add(professionalUsersResponse1);
        userProfiles.add(professionalUsersResponse2);

        ProfessionalUsersEntityResponse professionalUsersEntityResponse = new ProfessionalUsersEntityResponse();
        professionalUsersEntityResponse.setUserProfiles(userProfiles);

        ResponseEntity<?> realResponseEntity = new ResponseEntity<>(professionalUsersEntityResponse, header, HttpStatus.OK);

        ResponseEntity responseEntity = mock(ResponseEntity.class);
        when(responseEntity.getStatusCode()).thenReturn(HttpStatus.BAD_REQUEST);
        when(responseEntity.getBody()).thenReturn(realResponseEntity.getBody());

        ProfessionalUsersEntityResponse professionalUsersEntityResponse1 = RefDataUtil.filterUsersByStatus(responseEntity, "Active");
        assertThat(professionalUsersEntityResponse1).isNotNull();

        assertThat(professionalUsersEntityResponse1.getUserProfiles().size()).isEqualTo(3);
    }

    @Test(expected = ResourceNotFoundException.class)
    public void test_filterUsersByStatusWhereNoUsersFoundThrowsResourceNotFoundException() {
        ProfessionalUsersEntityResponse professionalUsersEntityResponse = new ProfessionalUsersEntityResponse();
        List<ProfessionalUsersResponse> userProfiles = new ArrayList<>();
        professionalUsersEntityResponse.setUserProfiles(userProfiles);

        HttpHeaders header = new HttpHeaders();
        header.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<?> realResponseEntity = new ResponseEntity<>(professionalUsersEntityResponse, header, HttpStatus.OK);

        ResponseEntity responseEntity = mock(ResponseEntity.class);
        when(responseEntity.getStatusCode()).thenReturn(HttpStatus.OK);
        when(responseEntity.getBody()).thenReturn(realResponseEntity.getBody());

        RefDataUtil.filterUsersByStatus(responseEntity, "Active");
    }

    @Test
    public void test_shouldGenerateResponseEntityWithHeaderFromPage() {
        HttpHeaders responseHeader = new HttpHeaders();
        responseHeader.add("fakeHeader", "fakeValue");

        ResponseEntity responseEntityMock = mock(ResponseEntity.class);
        when(responseEntityMock.getHeaders()).thenReturn(responseHeader);

        Page pageMock = mock(Page.class);
        when(pageMock.getTotalElements()).thenReturn(1L);
        when(pageMock.getTotalPages()).thenReturn(2);

        Pageable pageableMock = mock(Pageable.class);
        when(pageableMock.getPageNumber()).thenReturn(0);
        when(pageableMock.getPageSize()).thenReturn(2);

        HttpHeaders httpHeaders = RefDataUtil.generateResponseEntityWithPaginationHeader(pageableMock, pageMock, responseEntityMock);

        assertThat(httpHeaders.containsKey("fakeHeader")).isTrue();
        assertThat(httpHeaders.containsKey("paginationInfo")).isTrue();

        //MultiValueMap<String, String> multiValueMap = new LinkedMultiValueMap<>();
        //HttpHeaders httpHeadersMock = mock(HttpHeaders.class);

        //verify(httpHeadersMock, times(1)).addAll(multiValueMap);
        //verify(httpHeadersMock, times(1)).add(any(String.class), any(String.class));
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

        HttpHeaders httpHeadersMock = mock(HttpHeaders.class);

        assertThat(httpHeaders.containsKey("paginationInfo")).isTrue();
        //verify(httpHeadersMock, times(1)).add(any(String.class), any(String.class));
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
        RefDataUtil.defaultPageSize = 10;

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
}
