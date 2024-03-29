package uk.gov.hmcts.reform.professionalapi.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.microsoft.azure.storage.CloudStorageAccount;
import feign.FeignException;
import feign.Request;
import feign.Response;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
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
import uk.gov.hmcts.reform.professionalapi.controller.request.InvalidRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.RetrieveUserProfilesRequest;
import uk.gov.hmcts.reform.professionalapi.controller.response.DeleteOrganisationResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.GetUserProfileResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.NewUserResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.ProfessionalUsersEntityResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.ProfessionalUsersEntityResponseWithoutRoles;
import uk.gov.hmcts.reform.professionalapi.controller.response.ProfessionalUsersResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.ProfessionalUsersResponseWithoutRoles;
import uk.gov.hmcts.reform.professionalapi.dataload.config.BlobStorageCredentials;
import uk.gov.hmcts.reform.professionalapi.domain.ContactInformation;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus;
import uk.gov.hmcts.reform.professionalapi.domain.PaymentAccount;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;
import uk.gov.hmcts.reform.professionalapi.domain.SuperUser;
import uk.gov.hmcts.reform.professionalapi.domain.UserAccountMap;
import uk.gov.hmcts.reform.professionalapi.domain.UserAccountMapId;
import uk.gov.hmcts.reform.professionalapi.domain.UserProfile;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.ERROR_MESSAGE_UP_FAILED;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.ERROR_MSG_NO_ORGANISATION_FOUND;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.ERROR_MSG_NO_PBA_FOUND;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.ERROR_MSG_ORG_ADDRESS;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.ERROR_MSG_ORG_IDS_DOES_NOT_MATCH;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.ERROR_MSG_ORG_NOT_EXIST;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.PRD_AAC_SYSTEM;
import static uk.gov.hmcts.reform.professionalapi.util.RefDataUtil.isSystemRoleUser;
import static uk.gov.hmcts.reform.professionalapi.util.RefDataUtil.setOrgInfoInGetUserResponseAndSort;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
class RefDataUtilTest {

    @MockBean
    private BlobStorageCredentials blobStorageCredentials;

    @MockBean
    CloudStorageAccount cloudStorageAccount;

    private PaymentAccount paymentAccount;
    private Organisation organisation;
    private ProfessionalUser professionalUser;
    private UserAccountMapId userAccountMapId;
    private UserAccountMap userAccountMap;
    private UserProfile profile;
    private GetUserProfileResponse getUserProfileResponse;
    private UserProfileFeignClient userProfileFeignClient;
    private JsonFeignResponseUtil jsonFeignResponseUtil;

    @BeforeEach
    void setUp() {
        paymentAccount = new PaymentAccount("PBA1234567");
        organisation = new Organisation("Org-Name", OrganisationStatus.PENDING, "sra-id",
                "companyN", false, "www.org.com");
        professionalUser = new ProfessionalUser("some-fname", "some-lname",
                "soMeone@somewhere.com", organisation);
        professionalUser.setRoles(asList("pui-user-manager", "pui-case-manager"));
        professionalUser.setOrganisation(organisation);
        professionalUser.setUserIdentifier(UUID.randomUUID().toString());
        professionalUser.setLastUpdated(LocalDateTime.of(2023, 12, 31, 23, 59, 59, 987654321));
        userAccountMapId = new UserAccountMapId(professionalUser, paymentAccount);
        userAccountMap = new UserAccountMap(userAccountMapId);
        profile = new UserProfile(UUID.randomUUID().toString(), "email@org.com", "firstName",
                "lastName", IdamStatus.ACTIVE);
        getUserProfileResponse = new GetUserProfileResponse(profile, true);
        getUserProfileResponse.setRoles(singletonList("pui-case-manager"));
        getUserProfileResponse.setIdamStatusCode("400");
        getUserProfileResponse.setIdamMessage("BAD REQUEST");
        paymentAccount.setId(UUID.randomUUID());
        userProfileFeignClient = mock(UserProfileFeignClient.class);
        jsonFeignResponseUtil = mock(JsonFeignResponseUtil.class);
    }


    @Test
    void test_getOrganisationProfileIds() {
        organisation.setOrgType(null);
        List<String> organisationProfileIds = RefDataUtil.getOrganisationProfileIds(organisation);
        assertThat(organisationProfileIds).hasSize(1);
        assertThat(organisationProfileIds.get(0)).isEqualTo("SOLICITOR_PROFILE");

        organisation.setOrgType("Solicitor");
        organisationProfileIds = RefDataUtil.getOrganisationProfileIds(organisation);
        assertThat(organisationProfileIds).hasSize(1);
        assertThat(organisationProfileIds.get(0)).isEqualTo("SOLICITOR_PROFILE");

        organisation.setOrgType("Government Organisation-HMRC");
        organisationProfileIds = RefDataUtil.getOrganisationProfileIds(organisation);
        assertThat(organisationProfileIds).hasSize(1);
        assertThat(organisationProfileIds.get(0)).isEqualTo("OGD_HMRC_PROFILE");
    }


    @Test
    void test_shouldReturnPaymentAccountsFromUserAccountMap() {
        List<UserAccountMap> userAccountMaps = new ArrayList<>();
        userAccountMaps.add(userAccountMap);

        List<PaymentAccount> paymentAccounts = RefDataUtil.getPaymentAccountsFromUserAccountMap(userAccountMaps);
        assertThat(paymentAccounts).isNotNull()
                                   .isNotEmpty();
        assertNotNull(paymentAccounts.get(0).getId());
        assertNotNull(paymentAccounts.get(0).getPbaNumber());
    }

    @Test
    void test_shouldReturnPaymentAccountsFromUserAccountMa_WhenUserAccountMapIdPaymentAccountIsEmpty() {
        UserAccountMapId userAccountMapId = new UserAccountMapId(null, null);
        UserAccountMap userAccountMap = new UserAccountMap(userAccountMapId);

        List<UserAccountMap> userAccountMaps = new ArrayList<>();
        userAccountMaps.add(userAccountMap);

        List<PaymentAccount> paymentAccounts = RefDataUtil.getPaymentAccountsFromUserAccountMap(userAccountMaps);
        assertThat(paymentAccounts).isNotNull()
                                    .isNotEmpty();
    }

    @Test
    void test_shouldReturnPaymentAccountsFromUserAccountMapWhenListIsEmpty() {
        List<UserAccountMap> userAccountMaps = new ArrayList<>();

        List<PaymentAccount> paymentAccounts = RefDataUtil.getPaymentAccountsFromUserAccountMap(userAccountMaps);
        assertThat(paymentAccounts).isNotNull()
                                   .isEmpty();
    }

    @Test
    void test_shouldReturnPaymentAccountFromUserMap() {
        List<PaymentAccount> userMapPaymentAccount = new ArrayList<>();
        userMapPaymentAccount.add(paymentAccount);

        List<PaymentAccount> paymentAccountsEntity = new ArrayList<>();
        paymentAccountsEntity.add(paymentAccount);

        List<PaymentAccount> paymentAccounts = RefDataUtil.getPaymentAccountFromUserMap(userMapPaymentAccount,
                paymentAccountsEntity);

        assertThat(paymentAccounts).isNotEmpty();
    }

    @Test
    void test_shouldReturnPaymentAccountFromOrganisationUser() {
        List<PaymentAccount> paymentAccountsEntity = new ArrayList<>();
        paymentAccountsEntity.add(paymentAccount);

        if (!paymentAccountsEntity.isEmpty()) {
            List<PaymentAccount> paymentAccounts = RefDataUtil.getPaymentAccount(paymentAccountsEntity);
            assertThat(paymentAccounts).isNotEmpty();
        }
    }

    @Test
    void test_removeEmptyWhiteSpaces() {
        assertThat(RefDataUtil.removeEmptySpaces(" Test ")).isEqualTo("Test");
        assertThat(RefDataUtil.removeEmptySpaces(null)).isNull();
        assertThat(RefDataUtil.removeEmptySpaces(" Te  st ")).isEqualTo("Te st");
    }

    @Test
    void test_removeAllWhiteSpaces() {
        assertThat(RefDataUtil.removeAllSpaces(" T e s t    1 ")).isEqualTo("Test1");
        assertThat(RefDataUtil.removeAllSpaces(null)).isNull();
    }

    @Test
    void test_shouldReturnTrueValidateOrgIdentifier() {
        String uuid = UUID.randomUUID().toString();
        String uuid2 = UUID.randomUUID().toString();
        assertThrows(AccessDeniedException.class,() ->
                RefDataUtil.validateOrgIdentifier(uuid, uuid2));
    }

    @Test
    void test_mapUserInfoCorrectly_with_roles() {

        HttpHeaders header = new HttpHeaders();
        header.setContentType(APPLICATION_JSON);
        ResponseEntity<Object> realResponseEntity = new ResponseEntity<>(getUserProfileResponse, header, HttpStatus.OK);

        ProfessionalUser responseUser = RefDataUtil.mapUserInfo(professionalUser, realResponseEntity,
                true);
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
    void test_mapUserInfo_without_roles() {

        HttpHeaders header = new HttpHeaders();
        header.setContentType(APPLICATION_JSON);
        ResponseEntity<Object> realResponseEntity = new ResponseEntity<>(getUserProfileResponse, header, HttpStatus.OK);

        ProfessionalUser responseUser = RefDataUtil.mapUserInfo(new ProfessionalUser(), realResponseEntity,
                false);

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
    void test_filterUsersByStatus() {
        ProfessionalUsersResponse professionalUsersResponse
                = new ProfessionalUsersResponse(new ProfessionalUser("fName", "lName",
                "some@email.com", organisation));
        ProfessionalUsersResponse professionalUsersResponse1
                = new ProfessionalUsersResponse(new ProfessionalUser("fName1", "lName1",
                "some1@email.com", organisation));
        ProfessionalUsersResponse professionalUsersResponse2
                = new ProfessionalUsersResponse(new ProfessionalUser("fName2", "lName2",
                "some2@email.com", organisation));
        professionalUsersResponse.setIdamStatus(IdamStatus.ACTIVE.toString());
        professionalUsersResponse1.setIdamStatus(IdamStatus.ACTIVE.toString());
        professionalUsersResponse2.setIdamStatus(IdamStatus.PENDING.toString());

        List<ProfessionalUsersResponse> userProfiles = asList(professionalUsersResponse, professionalUsersResponse1,
                professionalUsersResponse2);

        ProfessionalUsersEntityResponse professionalUsersEntityResponse = new ProfessionalUsersEntityResponse();
        professionalUsersEntityResponse.setUserProfiles(userProfiles);

        HttpHeaders header = new HttpHeaders();
        header.setContentType(APPLICATION_JSON);
        ResponseEntity<Object> realResponseEntity = new ResponseEntity<>(professionalUsersEntityResponse, header,
                HttpStatus.OK);

        ProfessionalUsersEntityResponse professionalUsersEntityResponse1
                = (ProfessionalUsersEntityResponse) RefDataUtil.filterUsersByStatus(realResponseEntity,
                "Active");
        assertThat(professionalUsersEntityResponse1).isNotNull();

        assertThat(professionalUsersEntityResponse1.getUsers()).hasSize(2);
        assertThat(professionalUsersEntityResponse1.getUsers().get(0)).isEqualTo(professionalUsersResponse);
        assertThat(professionalUsersEntityResponse1.getUsers().get(1)).isEqualTo(professionalUsersResponse1);
    }

    @Test
    void test_filterUsersByStatusWithoutRoles() {
        ProfessionalUsersResponseWithoutRoles professionalUsersResponse
                = new ProfessionalUsersResponseWithoutRoles(new ProfessionalUser("fName", "lName",
                "some@email.com", organisation));
        ProfessionalUsersResponseWithoutRoles professionalUsersResponse1
                = new ProfessionalUsersResponseWithoutRoles(new ProfessionalUser("fName1", "lName1",
                "some1@email.com", organisation));
        ProfessionalUsersResponseWithoutRoles professionalUsersResponse2
                = new ProfessionalUsersResponseWithoutRoles(new ProfessionalUser("fName2", "lName2",
                "some2@email.com", organisation));

        professionalUsersResponse.setIdamStatus(IdamStatus.ACTIVE.toString());
        professionalUsersResponse1.setIdamStatus(IdamStatus.ACTIVE.toString());
        professionalUsersResponse2.setIdamStatus(IdamStatus.PENDING.toString());

        List<ProfessionalUsersResponseWithoutRoles> userProfiles = asList(professionalUsersResponse,
                professionalUsersResponse1, professionalUsersResponse2);

        ProfessionalUsersEntityResponseWithoutRoles professionalUsersEntityResponseWithoutRoles
                = new ProfessionalUsersEntityResponseWithoutRoles();
        professionalUsersEntityResponseWithoutRoles.setUserProfiles(userProfiles);

        HttpHeaders header = new HttpHeaders();
        header.setContentType(APPLICATION_JSON);
        ResponseEntity<Object> realResponseEntity = new ResponseEntity<>(professionalUsersEntityResponseWithoutRoles,
                header, HttpStatus.OK);

        ProfessionalUsersEntityResponseWithoutRoles professionalUsersEntityResponseWithoutRoles11
                = (ProfessionalUsersEntityResponseWithoutRoles) RefDataUtil.filterUsersByStatus(realResponseEntity,
                "Active");
        assertThat(professionalUsersEntityResponseWithoutRoles11).isNotNull();

        assertThat(professionalUsersEntityResponseWithoutRoles11.getUserProfiles()).hasSize(2);
        assertThat(professionalUsersEntityResponseWithoutRoles11.getUserProfiles().get(0))
                .isEqualTo(professionalUsersResponse);
        assertThat(professionalUsersEntityResponseWithoutRoles11.getUserProfiles().get(1))
                .isEqualTo(professionalUsersResponse1);
    }

    @Test
    void test_filterUsersByStatusWhenStatusCodeIsNot200() {
        ProfessionalUsersResponse professionalUsersResponse
                = new ProfessionalUsersResponse(new ProfessionalUser("fName", "lName",
                "some@email.com", organisation));
        ProfessionalUsersResponse professionalUsersResponse1
                = new ProfessionalUsersResponse(new ProfessionalUser("fName1", "lName1",
                "some1@email.com", organisation));
        ProfessionalUsersResponse professionalUsersResponse2
                = new ProfessionalUsersResponse(new ProfessionalUser("fName2", "lName2",
                "some2@email.com", organisation));

        professionalUsersResponse.setIdamStatus(IdamStatus.ACTIVE.toString());
        professionalUsersResponse1.setIdamStatus(IdamStatus.ACTIVE.toString());
        professionalUsersResponse2.setIdamStatus(IdamStatus.PENDING.toString());

        List<ProfessionalUsersResponse> userProfiles = asList(professionalUsersResponse, professionalUsersResponse1,
                professionalUsersResponse2);

        ProfessionalUsersEntityResponse professionalUsersEntityResponse = new ProfessionalUsersEntityResponse();
        professionalUsersEntityResponse.setUserProfiles(userProfiles);

        HttpHeaders header = new HttpHeaders();
        header.setContentType(APPLICATION_JSON);
        ResponseEntity<Object> realResponseEntity = new ResponseEntity<>(professionalUsersEntityResponse, header,
                HttpStatus.BAD_REQUEST);

        assertThrows(ExternalApiException.class, () ->
                RefDataUtil.filterUsersByStatus(realResponseEntity, "Active"));

    }

    @Test
    void test_filterUsersByStatusWhereNoUsersFoundThrowsResourceNotFoundException() {
        ProfessionalUsersEntityResponse professionalUsersEntityResponse = new ProfessionalUsersEntityResponse();
        List<ProfessionalUsersResponse> userProfiles = new ArrayList<>();
        professionalUsersEntityResponse.setUserProfiles(userProfiles);

        HttpHeaders header = new HttpHeaders();
        header.setContentType(APPLICATION_JSON);
        ResponseEntity<Object> realResponseEntity = new ResponseEntity<>(professionalUsersEntityResponse, header,
                HttpStatus.OK);

        assertThrows(ResourceNotFoundException.class,() ->
                RefDataUtil.filterUsersByStatus(realResponseEntity, "Active"));
    }

    @Test
    void test_filterUsersByStatusWhereNoUsersFoundWithoutRolesThrowsResourceNotFoundException() {
        ProfessionalUsersEntityResponseWithoutRoles professionalUsersEntityResponseWithoutRoles
                = new ProfessionalUsersEntityResponseWithoutRoles();
        List<ProfessionalUsersResponseWithoutRoles> userProfiles = new ArrayList<>();
        professionalUsersEntityResponseWithoutRoles.setUserProfiles(userProfiles);

        HttpHeaders header = new HttpHeaders();
        header.setContentType(APPLICATION_JSON);
        ResponseEntity<Object> realResponseEntity = new ResponseEntity<>(professionalUsersEntityResponseWithoutRoles,
                header, HttpStatus.OK);

        assertThrows(ResourceNotFoundException.class,() ->
                RefDataUtil.filterUsersByStatus(realResponseEntity, "Active"));
    }

    @Test
    void test_shouldGenerateResponseEntityWithHeaderFromPage() {
        HttpHeaders responseHeader = new HttpHeaders();
        responseHeader.add("fakeHeader", "fakeValue");

        ResponseEntity<Object> realResponseEntity = new ResponseEntity<>(null, responseHeader, HttpStatus.OK);

        Page pageMock = mock(Page.class);
        when(pageMock.getTotalElements()).thenReturn(1L);
        when(pageMock.getTotalPages()).thenReturn(2);

        Pageable pageableMock = mock(Pageable.class);
        when(pageableMock.getPageNumber()).thenReturn(0);
        when(pageableMock.getPageSize()).thenReturn(2);

        HttpHeaders httpHeaders = RefDataUtil.generateResponseEntityWithPaginationHeader(pageableMock, pageMock,
                realResponseEntity);

        assertThat(httpHeaders).containsKey("fakeHeader")
                               .containsKey("paginationInfo");

        verify(pageMock, times(1)).getTotalElements();
        verify(pageMock, times(1)).getTotalPages();
        verify(pageableMock, times(1)).getPageNumber();
        verify(pageableMock, times(1)).getPageSize();
    }

    @Test
    void test_shouldGenerateResponseEntityWithHeaderFromPageWhenResponseEntityIsNull() {
        Page pageMock = mock(Page.class);
        when(pageMock.getTotalElements()).thenReturn(1L);
        when(pageMock.getTotalPages()).thenReturn(2);

        Pageable pageableMock = mock(Pageable.class);
        when(pageableMock.getPageNumber()).thenReturn(0);
        when(pageableMock.getPageSize()).thenReturn(2);

        HttpHeaders httpHeaders = RefDataUtil.generateResponseEntityWithPaginationHeader(pageableMock, pageMock,
                null);

        assertThat(httpHeaders).containsKey("paginationInfo");

        verify(pageMock, times(1)).getTotalElements();
        verify(pageMock, times(1)).getTotalPages();
        verify(pageableMock, times(1)).getPageNumber();
        verify(pageableMock, times(1)).getPageSize();
    }

    @Test
    void test_shouldCreatePageableObject() {
        Integer page = 0;
        Integer size = 5;
        Sort sort = mock(Sort.class);

        Pageable pageable = RefDataUtil.createPageableObject(page, size, sort);

        assertThat(pageable).isNotNull();
        assertThat(pageable.getPageSize()).isEqualTo(5);
    }

    @Test
    void test_shouldCreatePageableObjectWithDefaultPageSize() {
        Integer page = 0;
        Sort sort = mock(Sort.class);

        Pageable pageable = RefDataUtil.createPageableObject(page, null, sort);

        assertThat(pageable).isNotNull();
        assertThat(pageable.getPageSize()).isEqualTo(10);
    }

    @Test
    void test_getShowDeletedValue() {
        assertThat(RefDataUtil.getShowDeletedValue("True")).isEqualTo("true");
        assertThat(RefDataUtil.getShowDeletedValue("true")).isEqualTo("true");
        assertThat(RefDataUtil.getShowDeletedValue("TRUE")).isEqualTo("true");
        assertThat(RefDataUtil.getShowDeletedValue("False")).isEqualTo("false");
        assertThat(RefDataUtil.getShowDeletedValue("false")).isEqualTo("false");
        assertThat(RefDataUtil.getShowDeletedValue("FALSE")).isEqualTo("false");
        assertThat(RefDataUtil.getShowDeletedValue("invalid")).isEqualTo("false");
        assertThat(RefDataUtil.getShowDeletedValue("")).isEqualTo("false");
        assertThat(RefDataUtil.getShowDeletedValue(" ")).isEqualTo("false");
    }

    @Test
    void test_getReturnRolesValue() {
        assertThat(RefDataUtil.getReturnRolesValue(Boolean.TRUE)).isEqualTo(Boolean.TRUE);
        assertThat(RefDataUtil.getReturnRolesValue(Boolean.FALSE)).isEqualTo(Boolean.FALSE);
        assertThat(RefDataUtil.getReturnRolesValue(null)).isEqualTo(Boolean.TRUE);
    }

    @Test
    void test_privateConstructor() throws Exception {
        Constructor<RefDataUtil> constructor = RefDataUtil.class.getDeclaredConstructor();
        assertTrue(Modifier.isPrivate(constructor.getModifiers()));
        constructor.setAccessible(true);
        constructor.newInstance((Object[]) null);
    }

    @Test
    void test_updateUserDetailsForActiveOrganisation_entity_reponse_null() {
        HttpHeaders header = new HttpHeaders();
        header.setContentType(APPLICATION_JSON);

        Map<String, Organisation> activeOrganisationDtls = new HashMap<>();
        ResponseEntity<Object> realResponseEntity = new ResponseEntity<>(null, header, HttpStatus.OK);
        Map<String, Organisation> response = RefDataUtil.updateUserDetailsForActiveOrganisation(realResponseEntity,
                activeOrganisationDtls);
        assertThat(response).isEmpty();
    }

    @Test
    void test_updateUserDetailsForActiveOrganisation_entity_reponse_empty() {
        ProfessionalUsersResponse professionalUsersResponse
                = new ProfessionalUsersResponse(new ProfessionalUser("fName", "lName",
                "some@email.com", organisation));
        ProfessionalUsersResponse professionalUsersResponse1
                = new ProfessionalUsersResponse(new ProfessionalUser("fName1", "lName1",
                "some1@email.com", organisation));
        ProfessionalUsersResponse professionalUsersResponse2
                = new ProfessionalUsersResponse(new ProfessionalUser("fName2", "lName2",
                "some2@email.com", organisation));

        professionalUsersResponse.setIdamStatus(IdamStatus.ACTIVE.toString());
        professionalUsersResponse1.setIdamStatus(IdamStatus.ACTIVE.toString());
        professionalUsersResponse2.setIdamStatus(IdamStatus.PENDING.toString());

        List<ProfessionalUsersResponse> userProfiles = asList(professionalUsersResponse, professionalUsersResponse1,
                professionalUsersResponse2);
        ProfessionalUsersEntityResponse professionalUsersEntityResponse = new ProfessionalUsersEntityResponse();
        professionalUsersEntityResponse.setUserProfiles(userProfiles);
        HttpHeaders header = new HttpHeaders();
        header.setContentType(APPLICATION_JSON);

        Map<String, Organisation> activeOrganisationDtls = new HashMap<>();
        ResponseEntity<Object> realResponseEntity = new ResponseEntity<>(professionalUsersEntityResponse, header,
                HttpStatus.OK);
        Map<String, Organisation> response = RefDataUtil.updateUserDetailsForActiveOrganisation(realResponseEntity,
                activeOrganisationDtls);
        assertThat(response).isEmpty();

    }

    @Test
    void test_updateUserDetailsForActiveOrganisation() {
        ProfessionalUsersResponse professionalUsersResponse
                = new ProfessionalUsersResponse(new ProfessionalUser("fName", "lName",
                "some@email.com", organisation));
        ProfessionalUsersResponse professionalUsersResponse1
                = new ProfessionalUsersResponse(new ProfessionalUser("fName", "lName",
                "some@email.com", organisation));
        ProfessionalUsersResponse professionalUsersResponse2
                = new ProfessionalUsersResponse(new ProfessionalUser("fName", "lName",
                "some@email.com", organisation));
        professionalUsersResponse.setUserIdentifier("1");
        professionalUsersResponse1.setUserIdentifier("2");
        professionalUsersResponse2.setUserIdentifier("3");
        professionalUsersResponse.setIdamStatus(IdamStatus.ACTIVE.toString());
        professionalUsersResponse1.setIdamStatus(IdamStatus.ACTIVE.toString());
        professionalUsersResponse2.setIdamStatus(IdamStatus.PENDING.toString());

        List<ProfessionalUsersResponse> userProfiles = asList(professionalUsersResponse, professionalUsersResponse1,
                professionalUsersResponse2);
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
        ResponseEntity<Object> realResponseEntity = new ResponseEntity<>(professionalUsersEntityResponse, header,
                HttpStatus.OK);
        Map<String, Organisation> response
                = RefDataUtil.updateUserDetailsForActiveOrganisation(realResponseEntity, activeOrganisationDtls);

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
    void test_GetSingleUserIdFromUserProfile() throws Exception {
        Map<String, Collection<String>> header = new HashMap<>();
        Collection<String> list = new ArrayList<>();
        header.put("content-encoding", list);
        UserProfile profile = new UserProfile(UUID.randomUUID().toString(), "some@email.com",
                "firstName", "lastName", IdamStatus.ACTIVE);
        GetUserProfileResponse userProfileResponse = new GetUserProfileResponse(profile, false);

        ObjectMapper mapper = new ObjectMapper();
        String body = mapper.writeValueAsString(userProfileResponse);
        Response response = Response.builder().status(200).reason("OK").headers(header).body(body, UTF_8)
                .request(mock(Request.class)).build();
        Response responseMock = mock(Response.class);
        when(responseMock.body()).thenReturn(response.body());
        when(responseMock.status()).thenReturn(response.status());

        when(userProfileFeignClient.getUserProfileById(any())).thenReturn(responseMock);

        ProfessionalUser result = RefDataUtil.getSingleUserIdFromUserProfile(new ProfessionalUser("firstName",
                "lastName", "some@email.com", new Organisation("name",
                OrganisationStatus.PENDING, "sraId", "companyNumber", Boolean.TRUE,
                "companyUrl")), userProfileFeignClient, Boolean.TRUE);
        assertThat(result).isNotNull();
        assertThat(result.getFirstName()).isEqualTo("firstName");
        assertThat(result.getLastName()).isEqualTo("lastName");
        assertThat(result.getEmailAddress()).isEqualTo("some@email.com");
        verify(userProfileFeignClient, times(1)).getUserProfileById(any());
        verify(responseMock, times(1)).body();
        verify(responseMock, times(3)).status();
        verify(responseMock, times(1)).close();
    }

    @Test
    void test_GetSingleUserIdFromUserProfile_WithFeignException() {
        FeignException feignExceptionMock = mock(FeignException.class);
        when(feignExceptionMock.status()).thenReturn(500);

        when(userProfileFeignClient.getUserProfileById(any())).thenThrow(feignExceptionMock);

        ProfessionalUser user = new ProfessionalUser("firstName",
                "lastName", "some@email.com", new Organisation("name",
                OrganisationStatus.PENDING, "sraId", "companyNumber", Boolean.TRUE,
                "companyUrl"));

        assertThrows(ExternalApiException.class, () ->
                RefDataUtil.getSingleUserIdFromUserProfile(user, userProfileFeignClient, Boolean.TRUE));

        verify(userProfileFeignClient, times(1)).getUserProfileById(any());
    }

    @Test
    void test_getMultipleUserProfilesFromUp_WithFeignException() {
        Map<String, Organisation> activeOrganisationDetails = new ConcurrentHashMap<>();
        activeOrganisationDetails.put("someId", organisation);

        FeignException feignExceptionMock = mock(FeignException.class);
        when(feignExceptionMock.status()).thenReturn(500);

        when(userProfileFeignClient.getUserProfiles(any(),any(),any())).thenThrow(feignExceptionMock);


        assertThrows(ExternalApiException.class, () ->
                RefDataUtil.getMultipleUserProfilesFromUp(userProfileFeignClient,
                        mock(RetrieveUserProfilesRequest.class),
                        "false",activeOrganisationDetails));

        verify(userProfileFeignClient, times(1)).getUserProfiles(any(),any(),any());
    }

    @Test
    void test_GetSingleUserIdFromUserProfile_WhenResponseIs300() throws Exception {
        Map<String, Collection<String>> header = new HashMap<>();
        Collection<String> list = new ArrayList<>();
        header.put("content-encoding", list);
        UserProfile profile = new UserProfile(UUID.randomUUID().toString(), "some@email.com",
                "firstName", "lastName", IdamStatus.ACTIVE);
        GetUserProfileResponse userProfileResponse = new GetUserProfileResponse(profile, false);

        ObjectMapper mapper = new ObjectMapper();
        String body = mapper.writeValueAsString(userProfileResponse);
        Response response = Response.builder().status(300).reason("").headers(header).body(body, UTF_8)
                .request(mock(Request.class)).build();
        when(userProfileFeignClient.getUserProfileById(any())).thenReturn(response);

        ProfessionalUser result = RefDataUtil.getSingleUserIdFromUserProfile(new ProfessionalUser("firstName",
                "lastName", "some@email.com", new Organisation("name",
                OrganisationStatus.PENDING, "sraId", "companyNumber", Boolean.TRUE,
                "companyUrl")), userProfileFeignClient, Boolean.TRUE);
        assertThat(result).isNotNull();
        assertThat(result.getFirstName()).isEqualTo("firstName");
        assertThat(result.getLastName()).isEqualTo("lastName");
        assertThat(result.getEmailAddress()).isEqualTo("some@email.com");
        verify(userProfileFeignClient, times(1)).getUserProfileById(any());
    }

    @Test
    void test_GetSingleUserIdFromUserProfile_WhenResponseIs300_body_is_null() {
        Map<String, Collection<String>> header = new HashMap<>();
        Collection<String> list = new ArrayList<>();
        header.put("content-encoding", list);
        UserProfile profile = new UserProfile(UUID.randomUUID().toString(), "some@email.com",
                "firstName", "lastName", IdamStatus.ACTIVE);
        GetUserProfileResponse userProfileResponse = new GetUserProfileResponse(profile, false);

        Response response = Response.builder().status(301).reason("").headers(header).body(null, UTF_8)
                .request(mock(Request.class)).build();
        when(userProfileFeignClient.getUserProfileById(any())).thenReturn(response);

        assertThat(catchThrowable(() -> RefDataUtil.getSingleUserIdFromUserProfile(new ProfessionalUser("firstName",
                "lastName", "some@email.com", new Organisation("name",
                OrganisationStatus.PENDING, "sraId", "companyNumber", Boolean.TRUE,
                "companyUrl")), userProfileFeignClient, Boolean.TRUE)))
                .isExactlyInstanceOf(ExternalApiException.class)
                .hasMessage(ERROR_MESSAGE_UP_FAILED);
        verify(userProfileFeignClient, times(1)).getUserProfileById(any());
    }


    @Test
    void test_getMultipleUserProfilesFromUp_coverage() throws JsonProcessingException {

        ProfessionalUsersResponse professionalUsersResponse
                = new ProfessionalUsersResponse(new ProfessionalUser("fName", "lName",
                "some@email.com", organisation));
        ProfessionalUsersResponse professionalUsersResponse1
                = new ProfessionalUsersResponse(new ProfessionalUser("fName", "lName",
                "some@email.com", organisation));
        ProfessionalUsersResponse professionalUsersResponse2
                = new ProfessionalUsersResponse(new ProfessionalUser("fName", "lName",
                "some@email.com", organisation));
        professionalUsersResponse.setUserIdentifier("1");
        professionalUsersResponse1.setUserIdentifier("2");
        professionalUsersResponse2.setUserIdentifier("3");
        professionalUsersResponse.setIdamStatus(IdamStatus.ACTIVE.toString());
        professionalUsersResponse1.setIdamStatus(IdamStatus.ACTIVE.toString());
        professionalUsersResponse2.setIdamStatus(IdamStatus.PENDING.toString());

        List<ProfessionalUsersResponse> userProfiles = asList(professionalUsersResponse, professionalUsersResponse1,
                professionalUsersResponse2);
        ProfessionalUsersEntityResponse professionalUsersEntityResponse = new ProfessionalUsersEntityResponse();
        professionalUsersEntityResponse.setUserProfiles(userProfiles);

        List<SuperUser> users = new ArrayList<>();
        users.add(professionalUser.toSuperUser());
        organisation.setUsers(users);
        Map<String, Organisation> activeOrganisationDtls = new HashMap<>();
        activeOrganisationDtls.put("1", organisation);
        activeOrganisationDtls.put("2", organisation);
        activeOrganisationDtls.put("3", organisation);



        Map<String, Collection<String>> header = new HashMap<>();
        Collection<String> list = new ArrayList<>();
        header.put("content-encoding", list);
        UserProfile profile = new UserProfile(UUID.randomUUID().toString(), "some@email.com",
                "firstName", "lastName", IdamStatus.ACTIVE);

        GetUserProfileResponse userProfileResponse = new GetUserProfileResponse(profile, false);

        ObjectMapper mapper = new ObjectMapper();
        String body = mapper.writeValueAsString(userProfileResponse);


        Response realResponse = Response.builder().status(200).reason("OK").headers(header).body(body, UTF_8)
                .request(mock(Request.class)).build();
        Response response = mock(Response.class);
        when(response.body()).thenReturn(realResponse.body());
        when(response.status()).thenReturn(realResponse.status());
        when(userProfileFeignClient.getUserProfiles(any(), any(), any())).thenReturn(response);

        List<Organisation> orgResponse = RefDataUtil.getMultipleUserProfilesFromUp(userProfileFeignClient,
                mock(RetrieveUserProfilesRequest.class), "true", activeOrganisationDtls);

        Organisation organisationRes = orgResponse.get(0);
        assertEquals(organisation, organisationRes);

        SuperUser item = users.get(0);
        assertNull(item.getId());
        assertEquals("some-fname", item.getFirstName());
        assertEquals("some-lname", item.getLastName());
        assertEquals("soMeone@somewhere.com", item.getEmailAddress());
        assertNull(item.getOrganisation().getId());
        assertEquals("Org-Name", item.getOrganisation().getName());

        assertThat(orgResponse).isNotNull()
                                .isNotEmpty();
        assertThat(orgResponse.get(0).getOrganisationIdentifier()).isEqualTo(organisation.getOrganisationIdentifier());
        assertThat(orgResponse.get(0).getName()).isEqualTo("Org-Name");
        assertThat(orgResponse.get(0).getSraId()).isEqualTo("sra-id");
        assertThat(orgResponse.get(0).getCompanyNumber()).isEqualTo("companyN");
        assertThat(orgResponse.get(0).getUsers().get(0).getFirstName()).isEqualTo("some-fname");
        assertThat(orgResponse.get(0).getUsers().get(0).getLastName()).isEqualTo("some-lname");
        assertEquals(HttpStatus.OK.value(),realResponse.status());
        verify(userProfileFeignClient, times(1)).getUserProfiles(any(), any(), any());

        verify(response, times(1)).body();
        verify(response, times(3)).status();
        verify(response, times(1)).close();
    }

    @Test
    void test_getMultipleUserProfilesFromUp() throws JsonProcessingException {
        SuperUser superUser = new SuperUser("fName", "lName", "someone@email.com",
                organisation);
        List<SuperUser> users = Arrays.asList(superUser);
        organisation.setUsers(users);
        Map<String, Organisation> activeOrganisationDetails = new ConcurrentHashMap<>();
        activeOrganisationDetails.put("someId", organisation);

        Map<String, Collection<String>> header = new HashMap<>();
        Collection<String> list = new ArrayList<>();
        header.put("content-encoding", list);
        UserProfile profile = new UserProfile(UUID.randomUUID().toString(), "some@email.com",
                "firstName", "lastName", IdamStatus.ACTIVE);

        GetUserProfileResponse userProfileResponse = new GetUserProfileResponse(profile, false);

        ObjectMapper mapper = new ObjectMapper();
        String body = mapper.writeValueAsString(userProfileResponse);

        Response realResponse = Response.builder().status(200).reason("OK").headers(header).body(body, UTF_8)
                .request(mock(Request.class)).build();
        Response response = mock(Response.class);
        when(response.body()).thenReturn(realResponse.body());
        when(response.status()).thenReturn(realResponse.status());
        when(userProfileFeignClient.getUserProfiles(any(), any(), any())).thenReturn(response);

        List<Organisation> orgResponse = RefDataUtil.getMultipleUserProfilesFromUp(userProfileFeignClient,
                mock(RetrieveUserProfilesRequest.class), "true", activeOrganisationDetails);

        assertThat(orgResponse).isNotNull();
        assertThat(orgResponse.get(0).getOrganisationIdentifier()).isEqualTo(organisation.getOrganisationIdentifier());
        assertThat(orgResponse.get(0).getStatus()).isEqualTo(organisation.getStatus());
        assertThat(orgResponse.get(0).getName()).isEqualTo("Org-Name");
        assertThat(orgResponse.get(0).getSraId()).isEqualTo("sra-id");
        assertThat(orgResponse.get(0).getCompanyNumber()).isEqualTo("companyN");
        assertThat(orgResponse.get(0).getUsers().get(0).getFirstName()).isEqualTo("fName");
        assertThat(orgResponse.get(0).getUsers().get(0).getLastName()).isEqualTo("lName");
        assertEquals(HttpStatus.OK.value(),realResponse.status());
        verify(userProfileFeignClient, times(1)).getUserProfiles(any(), any(), any());

        verify(response, times(1)).body();
        verify(response, times(3)).status();
        verify(response, times(1)).close();
    }



    @Test
    void test_getMultipleUserProfilesFromUp_200() throws JsonProcessingException {
        Map<String, Organisation> activeOrganisationDetails = new ConcurrentHashMap<>();
        activeOrganisationDetails.put("someId", organisation);


        Map<String, Collection<String>> header = new HashMap<>();
        Collection<String> list = new ArrayList<>();
        header.put("content-encoding", list);

        List<ProfessionalUsersResponse> professionalUsersResponses = new ArrayList<>();
        ProfessionalUsersResponse professionalUsersResponse = new ProfessionalUsersResponse(professionalUser);
        professionalUsersResponses.add(professionalUsersResponse);
        ProfessionalUsersEntityResponse professionalUsersEntityResponse = new ProfessionalUsersEntityResponse();
        professionalUsersEntityResponse.setUserProfiles(professionalUsersResponses);


        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        String body = mapper.writeValueAsString(professionalUsersEntityResponse);


        Response realResponse = Response.builder().status(200).reason("OK").headers(header).body(body, UTF_8)
                .request(mock(Request.class)).build();
        Response response = mock(Response.class);
        when(response.body()).thenReturn(realResponse.body());
        when(response.status()).thenReturn(realResponse.status());
        when(userProfileFeignClient.getUserProfiles(any(), any(), any())).thenReturn(response);

        List<Organisation> orgResponse = RefDataUtil.getMultipleUserProfilesFromUp(userProfileFeignClient,
                mock(RetrieveUserProfilesRequest.class), "true", activeOrganisationDetails);
        assertThat(orgResponse).isNotNull();
        assertThat(orgResponse.get(0).getOrganisationIdentifier()).isEqualTo(organisation.getOrganisationIdentifier());
        assertThat(orgResponse.get(0).getName()).isEqualTo("Org-Name");
        assertThat(orgResponse.get(0).getSraId()).isEqualTo("sra-id");
        assertThat(orgResponse.get(0).getCompanyNumber()).isEqualTo("companyN");
        assertThat(response.body()).isNotNull();
        assertEquals(HttpStatus.OK.value(),realResponse.status());
        verify(userProfileFeignClient, times(1)).getUserProfiles(any(), any(), any());
    }

    @Test
    void test_getMultipleUserProfilesFromUp_301() throws JsonProcessingException {
        Map<String, Organisation> activeOrganisationDetails = new ConcurrentHashMap<>();
        activeOrganisationDetails.put("someId", organisation);


        Map<String, Collection<String>> header = new HashMap<>();
        Collection<String> list = new ArrayList<>();
        header.put("content-encoding", list);

        List<ProfessionalUsersResponse> professionalUsersResponses = new ArrayList<>();
        ProfessionalUsersResponse professionalUsersResponse = new ProfessionalUsersResponse(professionalUser);
        professionalUsersResponses.add(professionalUsersResponse);
        ProfessionalUsersEntityResponse professionalUsersEntityResponse = new ProfessionalUsersEntityResponse();
        professionalUsersEntityResponse.setUserProfiles(professionalUsersResponses);


        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        String body = mapper.writeValueAsString(professionalUsersEntityResponse);


        Response realResponse = Response.builder().status(301).reason("").headers(header).body(body, UTF_8)
                .request(mock(Request.class)).build();
        Response response = mock(Response.class);
        when(response.body()).thenReturn(realResponse.body());
        when(response.status()).thenReturn(realResponse.status());
        when(userProfileFeignClient.getUserProfiles(any(), any(), any())).thenReturn(response);

        List<Organisation> orgResponse = RefDataUtil.getMultipleUserProfilesFromUp(userProfileFeignClient,
                mock(RetrieveUserProfilesRequest.class), "true", activeOrganisationDetails);
        assertThat(orgResponse).isNotNull()
                                .isEmpty();
        assertThat(response.body()).isNotNull();
        verify(userProfileFeignClient, times(1)).getUserProfiles(any(), any(), any());
    }

    @Test
    void test_getMultipleUserProfilesFromUp_ResponseStatusIs300() throws JsonProcessingException {
        Map<String, Organisation> activeOrganisationDetails = new ConcurrentHashMap<>();
        activeOrganisationDetails.put("someId", organisation);

        Map<String, Collection<String>> header = new HashMap<>();
        Collection<String> list = new ArrayList<>();
        header.put("content-encoding", list);

        List<ProfessionalUsersResponse> professionalUsersResponses = new ArrayList<>();
        ProfessionalUsersResponse professionalUsersResponse = new ProfessionalUsersResponse(professionalUser);
        professionalUsersResponses.add(professionalUsersResponse);
        ProfessionalUsersEntityResponse professionalUsersEntityResponse = new ProfessionalUsersEntityResponse();
        professionalUsersEntityResponse.setUserProfiles(professionalUsersResponses);

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        String body = mapper.writeValueAsString(professionalUsersEntityResponse);

        Response realResponse = Response.builder().status(300).reason("").headers(header).body(body, UTF_8)
                .request(mock(Request.class)).build();
        when(userProfileFeignClient.getUserProfiles(any(), any(), any())).thenReturn(realResponse);


        List<Organisation> orgResponse = RefDataUtil.getMultipleUserProfilesFromUp(userProfileFeignClient,
                mock(RetrieveUserProfilesRequest.class), "true", activeOrganisationDetails);
        assertThat(orgResponse).isNotNull()
                                .isEmpty();
        assertEquals(HttpStatus.MULTIPLE_CHOICES.value(),realResponse.status());
        verify(userProfileFeignClient, times(1)).getUserProfiles(any(), any(), any());
    }


    @Test
    void test_getMultipleUserProfilesFromUp_ResponseStatusIs400() throws JsonProcessingException {
        Map<String, Organisation> activeOrganisationDetails = new ConcurrentHashMap<>();
        activeOrganisationDetails.put("someId", organisation);

        Map<String, Collection<String>> header = new HashMap<>();
        Collection<String> list = new ArrayList<>();
        header.put("content-encoding", list);


        Response realResponse = Response.builder().status(400).reason("").headers(header).body(null, UTF_8)
                .request(mock(Request.class)).build();
        Response response = mock(Response.class);
        when(response.body()).thenReturn(realResponse.body());
        when(response.status()).thenReturn(realResponse.status());
        when(userProfileFeignClient.getUserProfiles(any(), any(), any())).thenReturn(response);

        List<Organisation> orgResponse = RefDataUtil.getMultipleUserProfilesFromUp(userProfileFeignClient,
                mock(RetrieveUserProfilesRequest.class), "true", activeOrganisationDetails);

        assertEquals(HttpStatus.BAD_REQUEST.value(),response.status());
        verify(userProfileFeignClient, times(1)).getUserProfiles(any(), any(), any());
        verify(response, times(1)).body();
        verify(response, times(4)).status();
        verify(response, times(1)).close();
    }


    @Test
    void test_GetSingleUserIdFromUserProfileForException() {
        Map<String, Collection<String>> header = new HashMap<>();
        Collection<String> list = new ArrayList<>();
        header.put("content-encoding", list);
        String body = "{"
                + "  \"errorMessage\": \"400\","
                + "  \"errorDescription\": \"BAD REQUEST\","
                + "  \"timeStamp\": \"23:10\""
                + "}";

        Response realResponse = Response.builder().status(500).reason("INTERNAL SERVER EERROR").headers(header)
                .body(body, UTF_8).request(mock(Request.class)).build();
        Response response = mock(Response.class);
        when(response.body()).thenReturn(realResponse.body());
        when(response.status()).thenReturn(realResponse.status());
        when(userProfileFeignClient.getUserProfileById(any())).thenReturn(response);

        ProfessionalUser user = new ProfessionalUser("firstName",
                "lastName", "emailAddress", new Organisation("name",
                OrganisationStatus.PENDING, "sraId", "companyNumber", Boolean.TRUE,
                "companyUrl"));

        assertThrows(ExternalApiException.class, () ->
                RefDataUtil.getSingleUserIdFromUserProfile(user, userProfileFeignClient, Boolean.TRUE));

        verify(userProfileFeignClient, times(1)).getUserProfileById(any());
        verify(response, times(1)).body();
        verify(response, times(3)).status();
        verify(response, times(1)).close();
    }

    @Test
    void test_mapUserInfo_without_rolesTrue() {
        HttpHeaders header = new HttpHeaders();
        header.setContentType(APPLICATION_JSON);
        ResponseEntity<Object> realResponseEntity = new ResponseEntity<>(getUserProfileResponse, header, HttpStatus.OK);
        ProfessionalUser responseUser = RefDataUtil.mapUserInfo(new ProfessionalUser(), realResponseEntity,
                true);

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
    void test_findUserProfileStatusByEmail() {
        Map<String, Collection<String>> header = new HashMap<>();
        Collection<String> list = new ArrayList<>();
        header.put("content-encoding", list);
        String body = "{"
                + "  \"userIdentifier\": \"1cb88d5f-ef2c-4587-aca0-f77a7f6f3742\","
                + "  \"idamStatus\": \"ACTIVE\""
                + "}";

        Response response = Response.builder().status(200).reason("OK").headers(header).body(body, UTF_8)
                .request(mock(Request.class)).build();
        when(userProfileFeignClient.getUserProfileByEmail("test@test.com")).thenReturn(response);


        NewUserResponse newUserResponse = RefDataUtil.findUserProfileStatusByEmail("test@test.com",
                userProfileFeignClient);

        assertThat(newUserResponse).isNotNull();
        assertThat(newUserResponse.getIdamStatus()).isEqualTo("ACTIVE");
        assertThat(newUserResponse.getUserIdentifier()).isEqualTo("1cb88d5f-ef2c-4587-aca0-f77a7f6f3742");
        verify(userProfileFeignClient, times(1)).getUserProfileByEmail(any());
    }

    @Test
    void test_findUserProfileStatusByEmail_WithResponse400() {
        Map<String, Collection<String>> header = new HashMap<>();
        Collection<String> list = new ArrayList<>();
        header.put("content-encoding", list);
        String body = "{" + "}";

        Response response = Response.builder().status(400).reason("BAD REQUEST").headers(header).body(body, UTF_8)
                .request(mock(Request.class)).build();
        when(userProfileFeignClient.getUserProfileByEmail("test@test.com")).thenReturn(response);

        NewUserResponse newUserResponse = RefDataUtil.findUserProfileStatusByEmail("test@test.com",
                userProfileFeignClient);

        assertThat(newUserResponse).isNotNull();
        assertThat(newUserResponse.getIdamStatus()).isNull();
        assertThat(newUserResponse.getUserIdentifier()).isNull();
        verify(userProfileFeignClient, times(1)).getUserProfileByEmail(any());
    }

    @Test
    void test_findUserProfileStatusByEmail_WithResponse300() {
        Map<String, Collection<String>> header = new HashMap<>();
        Collection<String> list = new ArrayList<>();
        header.put("content-encoding", list);
        String body = "{" + "}";

        Response response = Response.builder().status(300).reason("").headers(header).body(body, UTF_8)
                .request(mock(Request.class)).build();
        when(userProfileFeignClient.getUserProfileByEmail("test@test.com")).thenReturn(response);

        assertThat(response).isNotNull();
        assertThrows(RuntimeException.class, () ->
                RefDataUtil.findUserProfileStatusByEmail("test@test.com", userProfileFeignClient));
        verify(userProfileFeignClient, times(1)).getUserProfileByEmail(any());

    }

    @Test
    void test_findUserProfileStatusByEmail_Returns500_WhenExternalApiException() {
        FeignException feignException = mock(FeignException.class);
        when(feignException.status()).thenReturn(500);

        Map<String, Collection<String>> header = new HashMap<>();
        Collection<String> list = new ArrayList<>();
        header.put("content-encoding", list);
        when(userProfileFeignClient.getUserProfileByEmail("test@test.com")).thenThrow(feignException);

        assertThrows(ExternalApiException.class, () ->
                RefDataUtil.findUserProfileStatusByEmail("test@test.com", userProfileFeignClient));
        verify(userProfileFeignClient, times(1)).getUserProfileByEmail(any());
    }

    @Test
    void test_getUserIdFromUserProfile() throws JsonProcessingException {
        SuperUser superUser = new SuperUser("fName", "lName", "someone@email.com",
                organisation);
        List<SuperUser> users = Arrays.asList(superUser);

        Map<String, Collection<String>> header = new HashMap<>();
        Collection<String> list = new ArrayList<>();
        header.put("content-encoding", list);
        UserProfile profile = new UserProfile(UUID.randomUUID().toString(), "some@email.com",
                "firstName", "lastName", IdamStatus.ACTIVE);
        GetUserProfileResponse userProfileResponse = new GetUserProfileResponse(profile, false);

        ObjectMapper mapper = new ObjectMapper();
        String body = mapper.writeValueAsString(userProfileResponse);

        Response response = Response.builder().status(200).reason("OK").headers(header).body(body, UTF_8)
                .request(mock(Request.class)).build();
        when(userProfileFeignClient.getUserProfileById(any())).thenReturn(response);

        List<SuperUser> userProfileDtls = RefDataUtil.getUserIdFromUserProfile(users, userProfileFeignClient,
                true);

        assertThat(userProfileDtls).isNotNull();
        assertThat(userProfileDtls.size()).isNotZero();
        verify(userProfileFeignClient, times(1)).getUserProfileById(any());

    }

    @Test
    void test_setOrgIdInGetUserResponse_with_roles_response() {
        List<ProfessionalUsersResponse> professionalUsersResponses = new ArrayList<>();
        ProfessionalUsersResponse professionalUsersResponse = new ProfessionalUsersResponse(professionalUser);
        professionalUsersResponses.add(professionalUsersResponse);
        ProfessionalUsersEntityResponse professionalUsersEntityResponse = new ProfessionalUsersEntityResponse();
        professionalUsersEntityResponse.setUserProfiles(professionalUsersResponses);
        ResponseEntity<Object> responseEntity = ResponseEntity.status(200).body(professionalUsersEntityResponse);
        ResponseEntity<Object> responseEntityOutput = setOrgInfoInGetUserResponseAndSort(responseEntity, "ABCD123",
                OrganisationStatus.ACTIVE, List.of("organisationProfileId1,organisationProfileId2".split(",")));
        assertThat(responseEntityOutput.getBody()).isExactlyInstanceOf(ProfessionalUsersEntityResponse.class);
        ProfessionalUsersEntityResponse output = (ProfessionalUsersEntityResponse) responseEntityOutput.getBody();
        assertThat(output.getOrganisationIdentifier()).hasToString("ABCD123");
        assertThat(output.getOrganisationStatus()).isEqualTo(OrganisationStatus.ACTIVE.name());
        assertThat(output.getOrganisationProfileIds()).contains("organisationProfileId1");
        assertThat(output.getOrganisationProfileIds()).contains("organisationProfileId2");
    }

    @Test
    void test_setOrgIdInGetUserResponse_with_roles_response_sort_first_name() {
        List<ProfessionalUsersResponse> professionalUsersResponses = new ArrayList<>();
        ProfessionalUsersResponse professionalUsersResponse = new ProfessionalUsersResponse(professionalUser);
        ProfessionalUsersResponse professionalUsersResponse2 = new ProfessionalUsersResponse(
                new ProfessionalUser("first-name", "last-name", "soMeone@somewhere.com", organisation));
        professionalUsersResponses.add(professionalUsersResponse);
        professionalUsersResponses.add(professionalUsersResponse2);
        ProfessionalUsersEntityResponse professionalUsersEntityResponse = new ProfessionalUsersEntityResponse();
        professionalUsersEntityResponse.setUserProfiles(professionalUsersResponses);
        ResponseEntity<Object> responseEntity = ResponseEntity.status(200).body(professionalUsersEntityResponse);
        ResponseEntity<Object> responseEntityOutput = setOrgInfoInGetUserResponseAndSort(responseEntity, "ABCD123",
                OrganisationStatus.ACTIVE, List.of("organisationProfileId1,organisationProfileId2".split(",")));
        assertThat(responseEntityOutput.getBody()).isExactlyInstanceOf(ProfessionalUsersEntityResponse.class);
        ProfessionalUsersEntityResponse output = (ProfessionalUsersEntityResponse) responseEntityOutput.getBody();
        assertThat(output.getOrganisationIdentifier()).hasToString("ABCD123");
        assertEquals(2, output.getUsers().size());
        assertThat(output.getUsers().get(0).getFirstName()).hasToString("first-name");
    }

    @Test
    void test_setOrgIdInGetUserResponse_without_roles_response() {
        List<ProfessionalUsersResponseWithoutRoles> professionalUsersEntityResponsesWithoutRoles = new ArrayList<>();
        ProfessionalUsersResponseWithoutRoles professionalUsersResponseWithoutRoles
                = new ProfessionalUsersResponseWithoutRoles(professionalUser);
        professionalUsersEntityResponsesWithoutRoles.add(professionalUsersResponseWithoutRoles);
        ProfessionalUsersEntityResponseWithoutRoles professionalUsersEntityResponseWithoutRoles
                = new ProfessionalUsersEntityResponseWithoutRoles();
        professionalUsersEntityResponseWithoutRoles.setUserProfiles(professionalUsersEntityResponsesWithoutRoles);
        ResponseEntity<Object> responseEntity
                = ResponseEntity.status(200).body(professionalUsersEntityResponseWithoutRoles);
        ResponseEntity<Object> responseEntityOutput = setOrgInfoInGetUserResponseAndSort(responseEntity, "ABCD123",
                OrganisationStatus.ACTIVE, List.of("organisationProfileId1,organisationProfileId2".split(",")));
        assertThat(responseEntityOutput.getBody())
                .isExactlyInstanceOf(ProfessionalUsersEntityResponseWithoutRoles.class);
        ProfessionalUsersEntityResponseWithoutRoles output
                = (ProfessionalUsersEntityResponseWithoutRoles) responseEntityOutput.getBody();
        assertThat(output.getOrganisationIdentifier()).hasToString("ABCD123");
        assertThat(output.getOrganisationStatus()).isEqualTo(OrganisationStatus.ACTIVE.name());
        assertThat(output.getOrganisationProfileIds()).contains("organisationProfileId1");
        assertThat(output.getOrganisationProfileIds()).contains("organisationProfileId2");
        assertThat(output.getUserProfiles()).hasSize(1);
        assertThat(output.getUserProfiles().get(0).getEmail()).isEqualTo("soMeone@somewhere.com");
        assertThat(output.getUserProfiles().get(0).getLastName()).isEqualTo("some-lname");
        assertThat(output.getUserProfiles().get(0).getFirstName()).isEqualTo("some-fname");
        assertThat(output.getUserProfiles().get(0).getLastUpdated().toString())
                .isEqualTo("2023-12-31T23:59:59.987654321");
    }

    @Test
    void test_setOrgIdInGetUserResponse_without_roles_response_sort_first_name() {
        List<ProfessionalUsersResponseWithoutRoles> professionalUsersEntityResponsesWithoutRoles = new ArrayList<>();
        ProfessionalUsersResponseWithoutRoles puwrUser1 = new ProfessionalUsersResponseWithoutRoles(professionalUser);
        ProfessionalUsersResponseWithoutRoles puwrUser2 = new ProfessionalUsersResponseWithoutRoles(
                new ProfessionalUser("first-name", "last-name", "firstlast@somewhere.com", organisation));
        professionalUsersEntityResponsesWithoutRoles.add(puwrUser1);
        professionalUsersEntityResponsesWithoutRoles.add(puwrUser2);
        ProfessionalUsersEntityResponseWithoutRoles professionalUsersEntityResponseWithoutRoles
                = new ProfessionalUsersEntityResponseWithoutRoles();
        professionalUsersEntityResponseWithoutRoles.setUserProfiles(professionalUsersEntityResponsesWithoutRoles);
        ResponseEntity<Object> responseEntity
                = ResponseEntity.status(200).body(professionalUsersEntityResponseWithoutRoles);
        ResponseEntity<Object> responseEntityOutput = setOrgInfoInGetUserResponseAndSort(responseEntity, "ABCD123",
                OrganisationStatus.ACTIVE, List.of("organisationProfileId1,organisationProfileId2".split(",")));
        assertThat(responseEntityOutput.getBody())
                .isExactlyInstanceOf(ProfessionalUsersEntityResponseWithoutRoles.class);
        ProfessionalUsersEntityResponseWithoutRoles output
                = (ProfessionalUsersEntityResponseWithoutRoles) responseEntityOutput.getBody();
        assertThat(output.getOrganisationIdentifier()).hasToString("ABCD123");
        assertEquals(2, output.getUserProfiles().size());
        assertThat(output.getUserProfiles().get(0).getFirstName()).hasToString("first-name");
    }

    @Test
    void verifyUserHasSystemRole() {
        List<String> roles = new ArrayList<>();
        roles.add(PRD_AAC_SYSTEM);
        assertTrue(isSystemRoleUser(roles));
        roles.add("prd-admin");
        assertFalse(isSystemRoleUser(roles));
        roles = new ArrayList<>();
        roles.add("prd-admin");
        assertFalse(isSystemRoleUser(roles));
    }

    @Test
    void delete_UserProfile_204() throws JsonProcessingException {


        Map<String, Collection<String>> header = new HashMap<>();
        Collection<String> list = new ArrayList<>();
        header.put("content-encoding", list);
        String body = "{" + "}";

        Response response = Response.builder().status(204).reason("OK").headers(header).body(body, UTF_8)
                .request(mock(Request.class)).build();
        when(userProfileFeignClient.deleteUserProfile(any())).thenReturn(response);

        DeleteOrganisationResponse deleteOrganisationResponse = RefDataUtil
                .deleteUserProfilesFromUp(any(), userProfileFeignClient);

        assertThat(deleteOrganisationResponse).isNotNull();
        assertThat(deleteOrganisationResponse.getStatusCode()).isEqualTo(204);
        verify(userProfileFeignClient, times(1)).deleteUserProfile(any());

    }

    @Test
    void delete_UserProfile_500() throws JsonProcessingException {


        Map<String, Collection<String>> header = new HashMap<>();
        Collection<String> list = new ArrayList<>();
        header.put("content-encoding", list);
        String body = "{" + "}";
        Response response = Response.builder().status(500).reason("service failed").headers(header).body(body, UTF_8)
                .request(mock(Request.class)).build();
        when(userProfileFeignClient.deleteUserProfile(any())).thenReturn(response);

        DeleteOrganisationResponse deleteOrganisationResponse = RefDataUtil
                .deleteUserProfilesFromUp(any(), userProfileFeignClient);

        assertThat(deleteOrganisationResponse).isNotNull();
        assertThat(deleteOrganisationResponse.getStatusCode()).isEqualTo(500);
        verify(userProfileFeignClient, times(1)).deleteUserProfile(any());

    }

    @Test
    void testCheckOrganisationExists() {
        assertThat(catchThrowable(() -> RefDataUtil.checkOrganisationAndPbaExists(null)))
                .isExactlyInstanceOf(ResourceNotFoundException.class)
                .hasMessage(ERROR_MSG_NO_ORGANISATION_FOUND);
    }

    @Test
    void testCheckPbaExists() {
        assertThat(catchThrowable(() -> RefDataUtil.checkOrganisationAndPbaExists(organisation)))
                .isExactlyInstanceOf(ResourceNotFoundException.class)
                .hasMessage(ERROR_MSG_NO_PBA_FOUND);
    }

    @Test
    void testCheckOrganisationAndMoreThanTwoAddressExists_Organisation_null() {
        assertThat(catchThrowable(() -> RefDataUtil
                .checkOrganisationAndMoreThanRequiredAddressExists(null, Set.of("1"))))
                .isExactlyInstanceOf(ResourceNotFoundException.class)
                .hasMessage(ERROR_MSG_ORG_NOT_EXIST);
    }

    @Test
    void testCheckOrganisationAndMoreThanTwoAddressExists_contact_null() {
        assertThat(catchThrowable(() -> RefDataUtil
                .checkOrganisationAndMoreThanRequiredAddressExists(organisation, Set.of("1"))))
                .isExactlyInstanceOf(InvalidRequest.class)
                .hasMessage(ERROR_MSG_ORG_ADDRESS);
    }

    @Test
    void testCheckOrganisationAndMoreThanTwoAddressExists_equal_size() {
        ContactInformation contactInformation = new ContactInformation();
        contactInformation.setAddressLine1("addressLine1");
        contactInformation.setId(UUID.randomUUID());
        organisation.setContactInformations(Arrays.asList(contactInformation));

        assertThat(catchThrowable(() -> RefDataUtil
                .checkOrganisationAndMoreThanRequiredAddressExists(organisation, Set.of("1"))))
                .isExactlyInstanceOf(InvalidRequest.class)
                .hasMessage(ERROR_MSG_ORG_ADDRESS);
    }

    @Test
    void testMatchAddressIdsWithOrgContactInformationIds_equal_size() {
        ContactInformation contactInformation = new ContactInformation();
        contactInformation.setAddressLine1("addressLine1");
        contactInformation.setId(UUID.randomUUID());
        organisation.setContactInformations(Arrays.asList(contactInformation));

        assertThat(catchThrowable(() -> RefDataUtil
                .matchAddressIdsWithOrgContactInformationIds(organisation, Set.of("1"))))
                .isExactlyInstanceOf(ResourceNotFoundException.class)
                .hasMessage(ERROR_MSG_ORG_IDS_DOES_NOT_MATCH + " : " + "1");
    }

    @Test
    void testRemoveEmptySpaces() {
        String result = RefDataUtil.removeEmptySpaces("value");
        Assertions.assertEquals("value", result);
    }

    @Test
    void testRemoveAllSpaces() {
        String result = RefDataUtil.removeAllSpaces("value");
        Assertions.assertEquals("value", result);
    }


    @Test
    void testGetShowDeletedValue() {
        String result = RefDataUtil.getShowDeletedValue("showDeleted");
        Assertions.assertEquals("false", result);
    }

    @Test
    void testGetReturnRolesValue() {
        Boolean result = RefDataUtil.getReturnRolesValue(Boolean.TRUE);
        Assertions.assertEquals(Boolean.TRUE, result);
    }

    @Test
    void testUpdateUserDetailsForActiveOrganisation() throws Exception {

        HttpHeaders header = new HttpHeaders();
        header.setContentType(APPLICATION_JSON);
        ResponseEntity<Object> realResponseEntity = new ResponseEntity<>(null, header, HttpStatus.OK);
        Map<String, Organisation> result = RefDataUtil.updateUserDetailsForActiveOrganisation(realResponseEntity,
                Map.of("String", new Organisation("name", OrganisationStatus.ACTIVE, "sraId",
                        "companyNumber", Boolean.TRUE, "companyUrl")));
        assertThat(result).hasSameClassAs(Map.of("String",
                new Organisation("name", OrganisationStatus.ACTIVE,
                        "sraId", "companyNumber", Boolean.TRUE, "companyUrl")));
    }
}
