package uk.gov.hmcts.reform.professionalapi.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import feign.Request;
import feign.Response;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import uk.gov.hmcts.reform.professionalapi.controller.advice.ErrorResponse;
import uk.gov.hmcts.reform.professionalapi.controller.advice.ExternalApiException;
import uk.gov.hmcts.reform.professionalapi.controller.advice.ResourceNotFoundException;
import uk.gov.hmcts.reform.professionalapi.controller.constants.IdamStatus;
import uk.gov.hmcts.reform.professionalapi.controller.feign.UserProfileFeignClient;
import uk.gov.hmcts.reform.professionalapi.controller.request.RetrieveUserProfilesRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.validator.UserProfileUpdateRequestValidator;
import uk.gov.hmcts.reform.professionalapi.controller.response.GetUserProfileResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.NewUserResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.ProfessionalUsersEntityResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.ProfessionalUsersEntityResponseWithoutRoles;
import uk.gov.hmcts.reform.professionalapi.controller.response.ProfessionalUsersResponse;
import uk.gov.hmcts.reform.professionalapi.domain.ModifyUserRolesResponse;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus;
import uk.gov.hmcts.reform.professionalapi.domain.PrdEnum;
import uk.gov.hmcts.reform.professionalapi.domain.PrdEnumId;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;
import uk.gov.hmcts.reform.professionalapi.domain.RoleAdditionResponse;
import uk.gov.hmcts.reform.professionalapi.domain.RoleDeletionResponse;
import uk.gov.hmcts.reform.professionalapi.domain.RoleName;
import uk.gov.hmcts.reform.professionalapi.domain.SuperUser;
import uk.gov.hmcts.reform.professionalapi.domain.UserAccessType;
import uk.gov.hmcts.reform.professionalapi.domain.UserConfiguredAccess;
import uk.gov.hmcts.reform.professionalapi.domain.UserProfile;
import uk.gov.hmcts.reform.professionalapi.domain.UserProfileUpdatedData;
import uk.gov.hmcts.reform.professionalapi.repository.OrganisationRepository;
import uk.gov.hmcts.reform.professionalapi.repository.PrdEnumRepository;
import uk.gov.hmcts.reform.professionalapi.repository.ProfessionalUserRepository;
import uk.gov.hmcts.reform.professionalapi.repository.UserAttributeRepository;
import uk.gov.hmcts.reform.professionalapi.repository.UserConfiguredAccessRepository;

import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.ThrowableAssert.catchThrowable;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.ERROR_MESSAGE_UP_FAILED;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.ISO_DATE_TIME_FORMATTER;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.LENGTH_OF_ORGANISATION_IDENTIFIER;
import static uk.gov.hmcts.reform.professionalapi.generator.ProfessionalApiGenerator.generateUniqueAlphanumericId;

@ExtendWith(MockitoExtension.class)
class ProfessionalUserServiceImplTest {

    private final ProfessionalUserRepository professionalUserRepository
            = Mockito.mock(ProfessionalUserRepository.class);
    private final OrganisationRepository organisationRepository = mock(OrganisationRepository.class);
    private final UserAttributeRepository userAttributeRepository = mock(UserAttributeRepository.class);
    private final PrdEnumRepository prdEnumRepository = mock(PrdEnumRepository.class);
    private final UserProfileFeignClient userProfileFeignClient = mock(UserProfileFeignClient.class);
    private final UserAttributeServiceImpl userAttributeService = mock(UserAttributeServiceImpl.class);
    private final FeignException feignExceptionMock = mock(FeignException.class);
    private final UserConfiguredAccessRepository userConfiguredAccessRepository
            = mock(UserConfiguredAccessRepository.class);
    private final UserProfileUpdateRequestValidator userProfileUpdateRequestValidator
            = mock(UserProfileUpdateRequestValidator.class);

    private final Organisation organisation = new Organisation("some-org-name", null, "PENDING",
            null, null, null);
    private final String userIdentifier = "1234567";
    private final UserProfile userProfile = new UserProfile(UUID.randomUUID().toString(), "test@email.com",
            "fName", "lName", IdamStatus.PENDING);
    private final GetUserProfileResponse getUserProfileResponseMock = new GetUserProfileResponse(userProfile,
            false);

    private final ProfessionalUserServiceImpl professionalUserService = new ProfessionalUserServiceImpl(
            organisationRepository, professionalUserRepository, userAttributeRepository,
            prdEnumRepository, userAttributeService, userProfileFeignClient, userConfiguredAccessRepository,
            userProfileUpdateRequestValidator);

    private final ProfessionalUser professionalUser = new ProfessionalUser("some-fname",
            "some-lname", "some-email", organisation);

    private final SuperUser superUser = new SuperUser("some-fname", "some-lname",
            "some-super-email", organisation);

    private List<PrdEnum> prdEnums = new ArrayList<>();
    private List<String> userRoles = new ArrayList<>();
    ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    Optional<String> emptyOptional = Optional.of("");

    @BeforeEach
    void setup() {
        userRoles.add("pui-user-manager");
        PrdEnumId prdEnumId = new PrdEnumId();
        PrdEnum anEnum = new PrdEnum(prdEnumId, "pui-user-manager", "SIDAM_ROLE");
        prdEnums.add(anEnum);

        organisation.setOrganisationIdentifier(generateUniqueAlphanumericId(LENGTH_OF_ORGANISATION_IDENTIFIER));
        organisation.setStatus(OrganisationStatus.ACTIVE);
        superUser.setUserIdentifier(UUID.randomUUID().toString());
        professionalUser.setUserIdentifier(UUID.randomUUID().toString());
    }

    @Test
    void test_findUsersByOrganisation_with_deleted_users() throws Exception {
        List<ProfessionalUser> users = new ArrayList<>();
        users.add(professionalUser);

        ProfessionalUsersEntityResponse professionalUsersEntityResponse = new ProfessionalUsersEntityResponse();
        List<ProfessionalUsersResponse> userProfiles = new ArrayList<>();

        ProfessionalUser profile = new ProfessionalUser("firstName", "lastName",
                "email@org.com", organisation);

        ProfessionalUsersResponse userProfileResponse = new ProfessionalUsersResponse(profile);
        userProfileResponse.setUserIdentifier(professionalUser.getUserIdentifier());
        userProfiles.add(userProfileResponse);
        professionalUsersEntityResponse.setUserProfiles(userProfiles);
        ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                false);
        String body = mapper.writeValueAsString(professionalUsersEntityResponse);

        when(professionalUserRepository.findByOrganisationAndUserIdentifier(organisation,
                userIdentifier)).thenReturn(users);
        when(userProfileFeignClient.getUserProfiles(any(), any(), any())).thenReturn(Response.builder()
                .request(mock(Request.class)).body(body, Charset.defaultCharset()).status(200).build());

        ResponseEntity responseEntity = professionalUserService.findProfessionalUsersByOrganisation(organisation,
                userIdentifier,"false", true, "");

        assertThat(responseEntity).isNotNull();
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);

        verify(professionalUserRepository, Mockito.times(1))
                .findByOrganisationAndUserIdentifier(organisation, userIdentifier);
        verify(userProfileFeignClient, times(1)).getUserProfiles(any(), eq("false"),
                eq("true"));
    }

    @Test
    void test_findUsersByOrganisation_with_userIdentifier() throws Exception {
        organisation.setOrgType("Solicitor");
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

        List<ProfessionalUsersResponse> userProfiles = new ArrayList<>();
        userProfiles.add(professionalUsersResponse);
        userProfiles.add(professionalUsersResponse1);
        userProfiles.add(professionalUsersResponse2);

        ProfessionalUsersEntityResponse professionalUsersEntityResponse = new ProfessionalUsersEntityResponse();
        professionalUsersEntityResponse.setUserProfiles(userProfiles);

        HttpHeaders header = new HttpHeaders();
        header.setContentType(APPLICATION_JSON);

        ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                false);
        String body = mapper.writeValueAsString(professionalUsersEntityResponse);

        Response realResponse = Response.builder().request(mock(Request.class)).body(body,
                Charset.defaultCharset()).status(200).build();
        Response response = mock(Response.class);
        when(response.body()).thenReturn(realResponse.body());
        when(response.status()).thenReturn(realResponse.status());

        List<ProfessionalUser> users = new ArrayList<>();
        users.add(professionalUser);

        when(professionalUserRepository.findByOrganisationAndUserIdentifier(organisation,userIdentifier)).thenReturn(
                users);
        when(userProfileFeignClient.getUserProfiles(any(), any(), any())).thenReturn(response);

        ResponseEntity responseEntity = professionalUserService.findProfessionalUsersByOrganisation(organisation,
                userIdentifier, "false", true, "Active");

        ProfessionalUsersEntityResponse professionalUsersEntityResponse1
                = ((ProfessionalUsersEntityResponse)responseEntity.getBody());

        verify(professionalUserRepository, times(1)).findByOrganisationAndUserIdentifier(
                organisation, userIdentifier);
        assertThat(responseEntity).isNotNull();
        assertThat(responseEntity.getBody()).isExactlyInstanceOf(ProfessionalUsersEntityResponse.class);
        assertThat(professionalUsersEntityResponse1.getOrganisationIdentifier())
                .isEqualTo(organisation.getOrganisationIdentifier());
        assertThat(professionalUsersEntityResponse1.getOrganisationStatus())
                .isEqualTo(organisation.getStatus().name());
        assertThat(professionalUsersEntityResponse1.getOrganisationProfileIds())
                .contains("SOLICITOR_PROFILE");
        assertThat(professionalUsersEntityResponse1.getUsers()).hasSize(2);
        professionalUsersEntityResponse1.getUsers().forEach(userProfile -> {
            assertThat(userProfile.getIdamStatus()).isEqualToIgnoringCase("active");
        });


        verify(professionalUserRepository, times(1)).findByOrganisationAndUserIdentifier(
                organisation, userIdentifier);
        verify(userProfileFeignClient, times(1)).getUserProfiles(any(), eq("false"),
                eq("true"));
        verify(response, times(1)).body();
        verify(response, times(2)).status();
        verify(response, times(1)).close();
    }

    @Test
    void test_findUsersByOrganisation_with_status_active() throws Exception {
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

        List<ProfessionalUsersResponse> userProfiles = new ArrayList<>();
        userProfiles.add(professionalUsersResponse);
        userProfiles.add(professionalUsersResponse1);
        userProfiles.add(professionalUsersResponse2);

        ProfessionalUsersEntityResponse professionalUsersEntityResponse = new ProfessionalUsersEntityResponse();
        professionalUsersEntityResponse.setUserProfiles(userProfiles);

        HttpHeaders header = new HttpHeaders();
        header.setContentType(APPLICATION_JSON);

        ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                false);
        String body = mapper.writeValueAsString(professionalUsersEntityResponse);

        Response realResponse = Response.builder().request(mock(Request.class)).body(body,
                Charset.defaultCharset()).status(200).build();
        Response response = mock(Response.class);
        when(response.body()).thenReturn(realResponse.body());
        when(response.status()).thenReturn(realResponse.status());

        List<ProfessionalUser> users = new ArrayList<>();
        users.add(professionalUser);

        when(professionalUserRepository.findByOrganisation(organisation)).thenReturn(users);
        when(userProfileFeignClient.getUserProfiles(any(), any(), any())).thenReturn(response);

        ResponseEntity responseEntity = professionalUserService.findProfessionalUsersByOrganisation(organisation,
                null, "false", true, "Active");

        ProfessionalUsersEntityResponse professionalUsersEntityResponse1
                = ((ProfessionalUsersEntityResponse)responseEntity.getBody());

        verify(professionalUserRepository, times(1)).findByOrganisation(organisation);
        assertThat(responseEntity).isNotNull();
        assertThat(responseEntity.getBody()).isExactlyInstanceOf(ProfessionalUsersEntityResponse.class);
        assertThat(professionalUsersEntityResponse1.getOrganisationIdentifier())
                .isEqualTo(organisation.getOrganisationIdentifier());
        assertThat(professionalUsersEntityResponse1.getUsers()).hasSize(2);
        professionalUsersEntityResponse1.getUsers().forEach(userProfile -> {
            assertThat(userProfile.getIdamStatus()).isEqualToIgnoringCase("active");
        });


        verify(professionalUserRepository, times(1)).findByOrganisation(organisation);
        verify(userProfileFeignClient, times(1)).getUserProfiles(any(), eq("false"),
                eq("true"));
        verify(response, times(1)).body();
        verify(response, times(2)).status();
        verify(response, times(1)).close();
    }

    @Test
    void test_findUsersByOrganisation_failure_in_Up() throws Exception {
        HttpHeaders header = new HttpHeaders();
        header.setContentType(APPLICATION_JSON);
        List<ProfessionalUser> users = new ArrayList<>();
        users.add(professionalUser);
        when(professionalUserRepository.findByOrganisation(organisation)).thenReturn(users);
        ErrorResponse errorResponse = new ErrorResponse("some error message",
                "some error description", "21:10");
        when(userProfileFeignClient.getUserProfiles(any(), any(), any())).thenReturn(Response.builder()
                .request(mock(Request.class)).body(mapper.writeValueAsString(errorResponse),
                        Charset.defaultCharset()).status(400).build());
        ResponseEntity responseEntity = professionalUserService.findProfessionalUsersByOrganisation(organisation,
                null, "false", true, "");
        verify(professionalUserRepository, times(1)).findByOrganisation(organisation);
        assertThat(responseEntity).isNotNull();
        assertThat(responseEntity.getBody()).isExactlyInstanceOf(ErrorResponse.class);
        verify(professionalUserRepository, times(1)).findByOrganisation(organisation);
        verify(userProfileFeignClient, times(1)).getUserProfiles(any(), eq("false"),
                eq("true"));
    }

    @Test
    void test_modify_user_roles_bad_request() throws Exception {
        UserProfileUpdatedData userProfileUpdatedData = new UserProfileUpdatedData();

        Set<RoleName> roles = new HashSet<>();
        RoleName roleName1 = new RoleName("pui-case-manager");
        RoleName roleName2 = new RoleName("pui-case-organisation");
        roles.add(roleName1);
        roles.add(roleName2);
        userProfileUpdatedData.setRolesAdd(roles);

        ModifyUserRolesResponse modifyUserRolesResponse = new ModifyUserRolesResponse();
        modifyUserRolesResponse.setRoleAdditionResponse(createAddRoleResponse(HttpStatus.BAD_REQUEST,
                "Request Not Valid"));

        ObjectMapper mapper = new ObjectMapper();
        String body = mapper.writeValueAsString(modifyUserRolesResponse);

        when(userProfileFeignClient.modifyUserRoles(any(), any(), any())).thenReturn(Response.builder()
                .request(mock(Request.class)).body(body, Charset.defaultCharset()).status(200).build());

        String uuid = UUID.randomUUID().toString();

        ResponseEntity<Object> response = professionalUserService.modifyRolesForUser(userProfileUpdatedData,
                uuid, Optional.of(""));

        ModifyUserRolesResponse modifyUserRolesResponseFromTest = (ModifyUserRolesResponse) response.getBody();
        assertThat(modifyUserRolesResponseFromTest).isNotNull();
        assertThat(modifyUserRolesResponseFromTest.getRoleAdditionResponse()).isNotNull();
        assertThat(modifyUserRolesResponseFromTest.getRoleAdditionResponse().getIdamMessage())
                .isEqualTo("Request Not Valid");

        verify(userProfileFeignClient, times(1)).modifyUserRoles(any(), any(), any());
    }

    @Test
    void test_modify_user_roles_server_error() throws Exception {
        ModifyUserRolesResponse modifyUserRolesResponse = new ModifyUserRolesResponse();
        modifyUserRolesResponse.setRoleAdditionResponse(createAddRoleResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal Server Error"));
        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValueAsString(modifyUserRolesResponse);

        when(feignExceptionMock.status()).thenReturn(500);
        when(userProfileFeignClient.modifyUserRoles(any(), any(), any())).thenThrow(feignExceptionMock);

        UserProfileUpdatedData userProfileUpdatedData = new UserProfileUpdatedData();
        Set<RoleName> roles = new HashSet<RoleName>();
        RoleName roleName1 = new RoleName("pui-case-manager");
        RoleName roleName2 = new RoleName("pui-case-organisation");
        roles.add(roleName1);
        roles.add(roleName2);
        userProfileUpdatedData.setRolesAdd(roles);

        String uuid = UUID.randomUUID().toString();

        assertThrows(ExternalApiException.class, () ->
                professionalUserService.modifyRolesForUser(userProfileUpdatedData, uuid,
                emptyOptional));

        verify(userProfileFeignClient, times(1)).modifyUserRoles(any(), any(), any());
        verify(feignExceptionMock, times(2)).status();

        assertThrows(ExternalApiException.class, () ->
                professionalUserService.modifyRolesForUser(userProfileUpdatedData, uuid,
                emptyOptional));

        verify(userProfileFeignClient, times(2)).modifyUserRoles(any(), any(), any());
        verify(feignExceptionMock, times(4)).status();
    }

    @Test
    void test_addNewUserToAnOrganisation() {
        when(professionalUserRepository.save(any(ProfessionalUser.class))).thenReturn(professionalUser);

        NewUserResponse newUserResponse = professionalUserService.addNewUserToAnOrganisation(professionalUser,
                userRoles, prdEnums);
        assertThat(newUserResponse).isNotNull();

        verify(professionalUserRepository, times(1)).save(any(ProfessionalUser.class));
        verify(userAttributeService, times(1))
                .addUserAttributesToUser(any(ProfessionalUser.class), (Mockito.anyList()), (Mockito.anyList()));
    }

    @Test
    void test_findUsersByOrganisationEmptyResultException() {
        List<String> ids = new ArrayList<>();
        ids.add(professionalUser.getUserIdentifier());
        List<ProfessionalUser> users = new ArrayList<>();
        users.add(professionalUser);

        FeignException exceptionMock = mock(FeignException.class);
        when(exceptionMock.status()).thenReturn(500);
        when(professionalUserRepository.findByOrganisationAndUserIdentifier(organisation, userIdentifier)).thenReturn(
                users);
        when(userProfileFeignClient.getUserProfiles(any(), any(), any())).thenThrow(exceptionMock);

        assertThrows(ExternalApiException.class, () ->
                professionalUserService.findProfessionalUsersByOrganisation(organisation,
                        userIdentifier, "false", true, ""));

        verify(professionalUserRepository, times(1)).findByOrganisationAndUserIdentifier(
                organisation, userIdentifier);
    }

    @Test
    void findUsersByOrganisationExternalExceptionWith300StatusTest() throws Exception {
        List<String> ids = new ArrayList<>();
        ids.add(professionalUser.getUserIdentifier());
        List<ProfessionalUser> users = new ArrayList<>();
        users.add(professionalUser);

        FeignException exceptionMock = mock(FeignException.class);
        when(exceptionMock.status()).thenReturn(300);
        when(professionalUserRepository.findByOrganisationAndUserIdentifier(organisation, userIdentifier)).thenReturn(
                users);
        when(userProfileFeignClient.getUserProfiles(any(), any(), any())).thenThrow(exceptionMock);

        assertThrows(ExternalApiException.class, () ->
                professionalUserService.findProfessionalUsersByOrganisation(organisation, userIdentifier,
                "false", true, ""));

        verify(professionalUserRepository, times(1)).findByOrganisationAndUserIdentifier(
                organisation, userIdentifier);
    }

    @Test
    void test_shouldPersistUser() {
        when(professionalUserRepository.save(any(ProfessionalUser.class))).thenReturn(professionalUser);

        ProfessionalUser actualProfessionalUser = professionalUserService.persistUser(professionalUser);

        assertThat(actualProfessionalUser).isNotNull();
        verify(professionalUserRepository, times(1)).save(any(ProfessionalUser.class));
    }

    @Test
    void test_shouldReturnProfessionalUserByEmail() {
        String email = "some@email.com";
        when(professionalUserRepository.findByEmailAddress(email)).thenReturn(professionalUser);

        ProfessionalUser user = professionalUserService.findProfessionalUserByEmailAddress(email);
        assertThat(user).isNotNull();

        verify(professionalUserRepository, times(1)).findByEmailAddress(email);
    }

    @Test
    void test_shouldReturnProfessionalUserById() {
        UUID id = UUID.randomUUID();
        ProfessionalUser professionalUserMock = mock(ProfessionalUser.class);

        Optional<ProfessionalUser> professionalUserOptional = Optional.of(professionalUserMock);

        when(professionalUserRepository.findById(id)).thenReturn(professionalUserOptional);

        ProfessionalUser professionalUserResponse = professionalUserService.findProfessionalUserById(id);
        assertThat(professionalUserResponse).isNotNull();

        verify(professionalUserRepository, times(1)).findById(id);
    }

    @Test
    void test_shouldReturnProfessionalUserByIdShouldReturnNullIfUserNotFound() {
        UUID id = UUID.randomUUID();
        Optional<ProfessionalUser> professionalUserOptional = Optional.empty();

        when(professionalUserRepository.findById(id)).thenReturn(professionalUserOptional);

        ProfessionalUser professionalUserResponse = professionalUserService.findProfessionalUserById(id);
        assertThat(professionalUserResponse).isNull();

        verify(professionalUserRepository, times(1)).findById(id);
    }

    @Test
    void test_shouldReturnProfessionalUserByUserIdentifier() {
        String id = UUID.randomUUID().toString();
        ProfessionalUser professionalUserMock = mock(ProfessionalUser.class);

        when(professionalUserRepository.findByUserIdentifier(id)).thenReturn(professionalUserMock);

        ProfessionalUser professionalUserResponse = professionalUserService.findProfessionalUserByUserIdentifier(id);
        assertThat(professionalUserResponse).isNotNull();

        verify(professionalUserRepository, times(1)).findByUserIdentifier(id);
    }

    @Test
    void test_shouldReturnProfessionalUserByUserIdentifierShouldReturnNullIfUserNotFound() {
        String id = UUID.randomUUID().toString();
        when(professionalUserRepository.findByUserIdentifier(id)).thenReturn(null);

        ProfessionalUser professionalUserResponse = professionalUserService.findProfessionalUserByUserIdentifier(id);
        assertThat(professionalUserResponse).isNull();

        verify(professionalUserRepository, times(1)).findByUserIdentifier(id);
    }

    @Test
    @SuppressWarnings("unchecked")
    void test_shouldReturnUsersInResponseEntityWithPageable() throws JsonProcessingException {
        organisation.setOrgType("Government Organisation-DWP");

        Pageable pageableMock = mock(Pageable.class);
        List<ProfessionalUser> professionalUserList = new ArrayList<>();
        Page<ProfessionalUser> professionalUserPage = (Page<ProfessionalUser>) mock(Page.class);

        ProfessionalUser professionalUser = new ProfessionalUser("fName", "lName",
                "some@email.com", organisation);
        ProfessionalUser professionalUser1 = new ProfessionalUser("fName", "lName",
                "some1@email.com", organisation);
        professionalUserList.add(professionalUser);
        professionalUserList.add(professionalUser1);

        when(professionalUserRepository.findByOrganisation(organisation, pageableMock))
                .thenReturn(professionalUserPage);
        when(professionalUserPage.getContent()).thenReturn(professionalUserList);

        ProfessionalUsersResponse professionalUsersResponse = new ProfessionalUsersResponse(professionalUser);
        ProfessionalUsersResponse professionalUsersResponse1 = new ProfessionalUsersResponse(professionalUser1);
        professionalUsersResponse.setIdamStatus(IdamStatus.ACTIVE.toString());
        professionalUsersResponse1.setIdamStatus(IdamStatus.ACTIVE.toString());
        List<ProfessionalUsersResponse> userProfiles = new ArrayList<>();
        userProfiles.add(professionalUsersResponse);
        userProfiles.add(professionalUsersResponse1);
        ProfessionalUsersEntityResponse professionalUsersEntityResponse = new ProfessionalUsersEntityResponse();
        professionalUsersEntityResponse.setUserProfiles(userProfiles);

        HttpHeaders header = new HttpHeaders();
        header.setContentType(APPLICATION_JSON);
        String body = mapper.writeValueAsString(professionalUsersEntityResponse);
        Response response = Response.builder().request(mock(Request.class)).body(body, Charset.defaultCharset())
                .status(200).build();
        when(userProfileFeignClient.getUserProfiles(any(), any(), any())).thenReturn(response);

        ResponseEntity responseEntity = professionalUserService
                .findProfessionalUsersByOrganisationWithPageable(organisation, "false", false,
                        "Active", pageableMock);
        ProfessionalUsersEntityResponseWithoutRoles professionalUsersEntityResponseWithoutRoles
                = ((ProfessionalUsersEntityResponseWithoutRoles)responseEntity.getBody());
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getHeaders().get("paginationInfo")).isNotEmpty();
        assertThat(professionalUsersEntityResponseWithoutRoles.getOrganisationStatus())
                .isEqualTo(organisation.getStatus().name());
        assertThat(professionalUsersEntityResponseWithoutRoles.getOrganisationProfileIds())
                .contains("OGD_DWP_PROFILE");

        verify(professionalUserRepository, times(1))
                .findByOrganisation(organisation, pageableMock);
        verify(professionalUserPage, times(1)).getContent();
        verify(userProfileFeignClient, times(1)).getUserProfiles(any(), any(), any());
    }

    @Test
    @SuppressWarnings("unchecked")
    void test_shouldReturnUsersInResponseEntityWithPageable_300() throws JsonProcessingException {
        Pageable pageableMock = mock(Pageable.class);
        List<ProfessionalUser> professionalUserList = new ArrayList<>();
        Page<ProfessionalUser> professionalUserPage = (Page<ProfessionalUser>) mock(Page.class);

        ProfessionalUser professionalUser = new ProfessionalUser("fName", "lName",
                "some@email.com", organisation);
        ProfessionalUser professionalUser1 = new ProfessionalUser("fName", "lName",
                "some1@email.com", organisation);
        professionalUserList.add(professionalUser);
        professionalUserList.add(professionalUser1);

        when(professionalUserRepository.findByOrganisation(organisation, pageableMock))
                .thenReturn(professionalUserPage);
        when(professionalUserPage.getContent()).thenReturn(professionalUserList);

        ProfessionalUsersResponse professionalUsersResponse = new ProfessionalUsersResponse(professionalUser);
        ProfessionalUsersResponse professionalUsersResponse1 = new ProfessionalUsersResponse(professionalUser1);
        professionalUsersResponse.setIdamStatus(IdamStatus.ACTIVE.toString());
        professionalUsersResponse1.setIdamStatus(IdamStatus.ACTIVE.toString());
        List<ProfessionalUsersResponse> userProfiles = new ArrayList<>();
        userProfiles.add(professionalUsersResponse);
        userProfiles.add(professionalUsersResponse1);
        ProfessionalUsersEntityResponse professionalUsersEntityResponse = new ProfessionalUsersEntityResponse();
        professionalUsersEntityResponse.setUserProfiles(userProfiles);

        HttpHeaders header = new HttpHeaders();
        header.setContentType(APPLICATION_JSON);
        String body = mapper.writeValueAsString(professionalUsersEntityResponse);
        Response response = Response.builder().request(mock(Request.class)).body(body, Charset.defaultCharset())
                .status(300).build();
        when(userProfileFeignClient.getUserProfiles(any(), any(), any())).thenReturn(response);

        assertThrows(ExternalApiException.class, () ->
                professionalUserService.findProfessionalUsersByOrganisationWithPageable(organisation, "false",
                        false, "Active", pageableMock));

        verify(professionalUserRepository, times(1))
                .findByOrganisation(organisation, pageableMock);
        verify(professionalUserPage, times(1)).getContent();
        verify(userProfileFeignClient, times(1)).getUserProfiles(any(), any(), any());
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldThrowResourceNotFoundExceptionWhenNoUsersReturnedWithPageable() {
        Pageable pageableMock = mock(Pageable.class);
        Page<ProfessionalUser> professionalUserPage = (Page<ProfessionalUser>) mock(Page.class);

        when(professionalUserRepository.findByOrganisation(organisation, pageableMock))
                .thenReturn(professionalUserPage);

        assertThrows(ResourceNotFoundException.class, () ->
                professionalUserService.findProfessionalUsersByOrganisationWithPageable(organisation, "false",
                false, "Active", pageableMock));

        verify(professionalUserRepository, times(1))
                .findByOrganisation(organisation, pageableMock);
    }

    @Test
    @SuppressWarnings("unchecked")
    void test_shouldThrowResourceNotFoundExceptionWhenNoUsersReturned() {
        Pageable pageableMock = mock(Pageable.class);
        Page<ProfessionalUser> professionalUserPage = (Page<ProfessionalUser>) mock(Page.class);

        when(professionalUserRepository.findByOrganisation(organisation, pageableMock))
                .thenReturn(professionalUserPage);

        assertThrows(ResourceNotFoundException.class, () ->
                professionalUserService.findProfessionalUsersByOrganisation(organisation, userIdentifier,
                        "false", false, "Active"));

        verify(professionalUserRepository, times(1))
                .findByOrganisationAndUserIdentifier(organisation, userIdentifier);
    }

    @Test
    void test_findUsersByOrganisation_without_roles() throws Exception {
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

        List<ProfessionalUsersResponse> userProfiles = new ArrayList<>();
        userProfiles.add(professionalUsersResponse);
        userProfiles.add(professionalUsersResponse1);
        userProfiles.add(professionalUsersResponse2);

        ProfessionalUsersEntityResponse professionalUsersEntityResponse = new ProfessionalUsersEntityResponse();
        professionalUsersEntityResponse.setUserProfiles(userProfiles);

        ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                false);
        String body = mapper.writeValueAsString(professionalUsersEntityResponse);

        Response response = Response.builder().request(mock(Request.class)).body(body, Charset.defaultCharset())
                .status(200).build();

        List<ProfessionalUser> users = new ArrayList<>();
        users.add(professionalUser);

        when(professionalUserRepository.findByOrganisationAndUserIdentifier(organisation, userIdentifier)).thenReturn(
                users);
        when(userProfileFeignClient.getUserProfiles(any(), any(), any())).thenReturn(response);

        ResponseEntity responseEntity = professionalUserService.findProfessionalUsersByOrganisation(organisation,
                userIdentifier, "false", true, "Active");

        assertThat(responseEntity).isNotNull();

        List<ProfessionalUsersResponse> usersResponse
                = ((ProfessionalUsersEntityResponse)responseEntity.getBody()).getUsers();
        assertThat(usersResponse.get(0).getRoles()).isNull();
        assertThat(usersResponse.get(1).getRoles()).isNull();

        verify(professionalUserRepository, Mockito.times(1))
                .findByOrganisationAndUserIdentifier(organisation, userIdentifier);
        verify(userProfileFeignClient, times(1)).getUserProfiles(any(), eq("false"),
                eq("true"));
    }

    @Test
    void test_findUserStatusByEmail() throws Exception {
        organisation.setStatus(OrganisationStatus.ACTIVE);
        professionalUser.getOrganisation().setStatus(OrganisationStatus.ACTIVE);

        when(professionalUserRepository.findByEmailAddress(professionalUser.getEmailAddress()))
                .thenReturn(professionalUser);

        NewUserResponse getUserResponse = new NewUserResponse();
        getUserResponse.setUserIdentifier("a123dfgr46");
        getUserResponse.setIdamStatus("ACTIVE");
        ObjectMapper mapper = new ObjectMapper();
        String body = mapper.writeValueAsString(getUserResponse);
        when(userProfileFeignClient.getUserProfileByEmail(anyString())).thenReturn(Response.builder()
                .request(mock(Request.class)).body(body, Charset.defaultCharset()).status(200).build());

        ResponseEntity<NewUserResponse> newResponse = professionalUserService
                .findUserStatusByEmailAddress(professionalUser.getEmailAddress());

        assertThat(newResponse).isNotNull();
        assertThat(newResponse.getBody()).isNotNull();
        assertThat(newResponse.getBody().getUserIdentifier()).isEqualTo("a123dfgr46");

        verify(professionalUserRepository, times(1))
                .findByEmailAddress(professionalUser.getEmailAddress());
        verify(userProfileFeignClient, times(1)).getUserProfileByEmail(anyString());
        assertThat(newResponse.getBody().getIdamStatus()).isNull();
    }

    @Test
    void test_findUserStatusByEmailForPending() throws Exception {
        organisation.setStatus(OrganisationStatus.ACTIVE);

        when(professionalUserRepository.findByEmailAddress(professionalUser.getEmailAddress()))
                .thenReturn(professionalUser);
        NewUserResponse newUserResponse = new NewUserResponse();
        newUserResponse.setIdamStatus("PENDING");
        ObjectMapper mapper = new ObjectMapper();
        String body = mapper.writeValueAsString(newUserResponse);

        when(userProfileFeignClient.getUserProfileByEmail(anyString())).thenReturn(Response.builder()
                .request(mock(Request.class)).body(body, Charset.defaultCharset()).status(200).build());

        ResponseEntity<NewUserResponse> newResponse = professionalUserService
                .findUserStatusByEmailAddress(professionalUser.getEmailAddress());

        assertThat(newResponse.getStatusCode().value()).isEqualTo(404);
        verify(professionalUserRepository, times(1))
                .findByEmailAddress(professionalUser.getEmailAddress());
        verify(userProfileFeignClient, times(1)).getUserProfileByEmail(anyString());
    }

    @Test
    void test_findUserStatusByEmailForPendingOrgThrowsException() throws Exception {
        organisation.setStatus(OrganisationStatus.PENDING);

        when(professionalUserRepository.findByEmailAddress(professionalUser.getEmailAddress()))
                .thenReturn(professionalUser);
        NewUserResponse newUserResponse = new NewUserResponse();
        newUserResponse.setIdamStatus("PENDING");
        ObjectMapper mapper = new ObjectMapper();
        String body = mapper.writeValueAsString(newUserResponse);

        when(userProfileFeignClient.getUserProfileByEmail(anyString())).thenReturn(Response.builder()
                .request(mock(Request.class)).body(body, Charset.defaultCharset()).status(200).build());

        String email = professionalUser.getEmailAddress();
        assertThrows(EmptyResultDataAccessException.class, () ->
                professionalUserService.findUserStatusByEmailAddress(email));

        verify(professionalUserRepository, times(1))
                .findByEmailAddress(professionalUser.getEmailAddress());
        verify(userProfileFeignClient, times(0)).getUserProfileByEmail(anyString());
    }

    @Test
    void test_findUserStatusByEmailForActiveThrowsExceptionWhenUpServiceDown() {
        organisation.setStatus(OrganisationStatus.ACTIVE);

        when(professionalUserRepository.findByEmailAddress(professionalUser
                .getEmailAddress())).thenReturn(professionalUser);
        when(userProfileFeignClient.getUserProfileByEmail(anyString()))
                .thenThrow(new ExternalApiException(HttpStatus.valueOf(500), "UP Email Service Down"));

        String email = professionalUser.getEmailAddress();

        assertThrows(ExternalApiException.class,() ->
                professionalUserService.findUserStatusByEmailAddress(email));

        verify(professionalUserRepository, times(1))
                .findByEmailAddress(professionalUser.getEmailAddress());
        verify(userProfileFeignClient, times(1)).getUserProfileByEmail(anyString());
    }

    @SneakyThrows
    @Test
    void test_UserStatusIsActiveByUserId() {
        professionalUser.setIdamStatus(IdamStatus.ACTIVE);

        when(professionalUserRepository.findByUserIdentifier(any(String.class))).thenReturn(professionalUser);

        NewUserResponse newUserResponse = new NewUserResponse();
        newUserResponse.setIdamStatus("ACTIVE");
        ObjectMapper mapper = new ObjectMapper();
        String body = mapper.writeValueAsString(newUserResponse);

        when(userProfileFeignClient.getUserProfileByEmail(anyString())).thenReturn(Response.builder()
                .request(mock(Request.class)).body(body, Charset.defaultCharset()).status(200).build());

        String userId = UUID.randomUUID().toString();

        assertDoesNotThrow(() ->
                professionalUserService.checkUserStatusIsActiveByUserId(userId));
        verify(professionalUserRepository, times(1)).findByUserIdentifier(any(String.class));
        verify(userProfileFeignClient, times(1)).getUserProfileByEmail(anyString());
    }

    @Test
    void test_checkUserStatusIsActiveByUserId_Throws403_WhenNoUserFoundWithGivenId() {
        when(professionalUserRepository.findByUserIdentifier(any(String.class))).thenReturn(null);

        String uuid = UUID.randomUUID().toString();

        assertThrows(AccessDeniedException.class, () ->
                professionalUserService.checkUserStatusIsActiveByUserId(uuid));

        verify(professionalUserRepository, times(1)).findByUserIdentifier(any(String.class));
    }

    @SneakyThrows
    @Test
    void test_checkUserStatusIsActiveByUserId_Throws403_WhenUserIsNotActive() {
        professionalUser.setIdamStatus(IdamStatus.PENDING);

        when(professionalUserRepository.findById(any(UUID.class))).thenReturn(Optional.of(professionalUser));

        NewUserResponse newUserResponse = new NewUserResponse();
        newUserResponse.setIdamStatus("PENDING");
        ObjectMapper mapper = new ObjectMapper();
        String body = mapper.writeValueAsString(newUserResponse);

        when(userProfileFeignClient.getUserProfileByEmail(anyString())).thenReturn(Response.builder()
                .request(mock(Request.class)).body(body, Charset.defaultCharset()).status(200).build());


        String userId = UUID.randomUUID().toString();

        assertThrows(AccessDeniedException.class, () ->
                professionalUserService.checkUserStatusIsActiveByUserId(userId));
    }

    private RoleAdditionResponse createAddRoleResponse(HttpStatus status, String message) {
        RoleAdditionResponse addRoleResponse = new RoleAdditionResponse();
        addRoleResponse.setIdamStatusCode(status.toString());
        addRoleResponse.setIdamMessage(message);
        return addRoleResponse;
    }

    private List<RoleDeletionResponse> createDeleteRoleResponse(HttpStatus status, String message) {
        RoleDeletionResponse deleteRoleResponse = new RoleDeletionResponse();
        deleteRoleResponse.setIdamStatusCode(status.toString());
        deleteRoleResponse.setIdamMessage(message);
        List<RoleDeletionResponse> deleteRoleResponses = new ArrayList<>();
        deleteRoleResponses.add(deleteRoleResponse);
        return deleteRoleResponses;
    }

    @Test
    void test_modify_user_roles_feign_error_with_no_status() throws Exception {

        when(feignExceptionMock.status()).thenReturn(-1);
        callModifyRolesForUser(HttpStatus.INTERNAL_SERVER_ERROR);
        verify(feignExceptionMock, times(1)).status();

    }

    @Test
    void test_modify_user_roles_feign_error_with_no_400_status() throws Exception {

        when(feignExceptionMock.status()).thenReturn(400);
        callModifyRolesForUser(HttpStatus.BAD_REQUEST);
        verify(feignExceptionMock, times(2)).status();

    }

    @Test
    void test_generateRetrieveUserProfilesRequest() {
        List<ProfessionalUser> professionalUsers = new ArrayList<>();
        ProfessionalUser professionalUser = mock(ProfessionalUser.class);
        professionalUsers.add(professionalUser);

        when(professionalUser.getUserIdentifier()).thenReturn(UUID.randomUUID().toString());

        RetrieveUserProfilesRequest response =
                professionalUserService.generateRetrieveUserProfilesRequest(professionalUsers);

        assertThat(response).isNotNull();
        verify(professionalUser, times(1)).getUserIdentifier();
    }

    @Test
    void testModifyRolesForExistingUserOfOrganisation() throws JsonProcessingException {
        String userId = UUID.randomUUID().toString();
        NewUserResponse newUserResponse = new NewUserResponse();
        newUserResponse.setUserIdentifier(userId);
        newUserResponse.setIdamStatus("ACTIVE");
        ObjectMapper mapper = new ObjectMapper();
        String body = mapper.writeValueAsString(newUserResponse);

        when(userProfileFeignClient.getUserProfileByEmail(any())).thenReturn(Response.builder()
                .request(mock(Request.class)).body(body, Charset.defaultCharset()).status(200).build());

        Response response = mock(Response.class);
        when(response.status()).thenReturn(200);
        when(userProfileFeignClient.modifyUserRoles(any(), any(), any())).thenReturn(response);


        UserProfileUpdatedData userProfileUpdatedData = new UserProfileUpdatedData();
        when(userProfileUpdateRequestValidator.validateRequest(userProfileUpdatedData))
                .thenReturn(userProfileUpdatedData);

        ResponseEntity<Object> actualData = professionalUserService.modifyRolesForUser(userProfileUpdatedData,
                userId, Optional.of("EXUI"));

        assertThat(actualData).isNotNull();
        assertThat(actualData.getStatusCode()).isEqualTo(HttpStatus.OK);

        verify(userProfileUpdateRequestValidator, times(1)).validateRequest(userProfileUpdatedData);
    }

    @Test
    void test_modifyRolesAndUserConfiguredAccessForAddedAccessType() throws JsonProcessingException {
        UUID uuid = UUID.randomUUID();
        String uuidStr = uuid.toString();

        NewUserResponse newUserResponse = new NewUserResponse();
        newUserResponse.setUserIdentifier(uuidStr);
        newUserResponse.setIdamStatus("ACTIVE");
        ObjectMapper mapper = new ObjectMapper();
        String body = mapper.writeValueAsString(newUserResponse);

        when(userProfileFeignClient.getUserProfileByEmail(any())).thenReturn(Response.builder()
                .request(mock(Request.class)).body(body, Charset.defaultCharset()).status(200).build());

        Response response = mock(Response.class);
        when(response.status()).thenReturn(200);
        when(userProfileFeignClient.modifyUserRoles(any(), any(), any())).thenReturn(response);

        UserProfileUpdatedData userProfileUpdatedData = new UserProfileUpdatedData();
        when(userProfileUpdateRequestValidator.validateRequest(userProfileUpdatedData))
                .thenReturn(userProfileUpdatedData);

        ProfessionalUser professionalUserMock = mock(ProfessionalUser.class);
        when(professionalUserRepository.findByUserIdentifier(uuidStr)).thenReturn(professionalUserMock);
        when(professionalUserMock.getId()).thenReturn(uuid);

        Set<UserAccessType> userAccessTypes = new HashSet<>();
        UserAccessType userAccessType1 = new UserAccessType();
        userAccessTypes.add(userAccessType1);
        userProfileUpdatedData.setUserAccessTypes(userAccessTypes);

        UserConfiguredAccess userConfiguredAccess1 = new UserConfiguredAccess();
        UserConfiguredAccess userConfiguredAccess2 = new UserConfiguredAccess();
        List<UserConfiguredAccess> optUca = List.of(userConfiguredAccess1, userConfiguredAccess2);

        when(userConfiguredAccessRepository.findByUserConfiguredAccessId_ProfessionalUser_Id(uuid))
                .thenReturn(optUca);

        String origin = "EXUI";
        Optional<String> originOpt = Optional.of(origin);
        professionalUserService.modifyUserConfiguredAccessAndRoles(userProfileUpdatedData,
                uuidStr, originOpt);


        verify(userConfiguredAccessRepository, times(1)).deleteAll(optUca);
        verify(userConfiguredAccessRepository, times(1)).saveAll(any());
    }

    @Test
    void test_failModifyRolesAndUserConfiguredAccessForRoleUpdateException() throws JsonProcessingException {
        UUID uuid = UUID.randomUUID();
        String uuidStr = uuid.toString();

        NewUserResponse newUserResponse = new NewUserResponse();
        newUserResponse.setUserIdentifier(uuidStr);
        newUserResponse.setIdamStatus("ACTIVE");
        ObjectMapper mapper = new ObjectMapper();
        String body = mapper.writeValueAsString(newUserResponse);

        when(userProfileFeignClient.getUserProfileByEmail(any())).thenReturn(Response.builder()
                .request(mock(Request.class)).body(body, Charset.defaultCharset()).status(200).build());

        when(userProfileFeignClient.modifyUserRoles(any(), any(), any())).thenThrow(feignExceptionMock);

        UserProfileUpdatedData userProfileUpdatedData = new UserProfileUpdatedData();
        when(userProfileUpdateRequestValidator.validateRequest(userProfileUpdatedData))
                .thenReturn(userProfileUpdatedData);

        ProfessionalUser professionalUserMock = mock(ProfessionalUser.class);
        when(professionalUserRepository.findByUserIdentifier(uuidStr)).thenReturn(professionalUserMock);
        when(professionalUserMock.getId()).thenReturn(uuid);

        Set<UserAccessType> userAccessTypes = new HashSet<>();
        UserAccessType userAccessType1 = new UserAccessType();
        userAccessTypes.add(userAccessType1);
        userProfileUpdatedData.setUserAccessTypes(userAccessTypes);

        UserConfiguredAccess userConfiguredAccess1 = new UserConfiguredAccess();
        UserConfiguredAccess userConfiguredAccess2 = new UserConfiguredAccess();
        List<UserConfiguredAccess> optUca = List.of(userConfiguredAccess1, userConfiguredAccess2);

        when(userConfiguredAccessRepository.findByUserConfiguredAccessId_ProfessionalUser_Id(uuid))
                .thenReturn(optUca);

        String origin = "EXUI";
        Optional<String> originOpt = Optional.of(origin);

        try {
            professionalUserService.modifyUserConfiguredAccessAndRoles(userProfileUpdatedData,
                    uuidStr, originOpt);
        } catch (ExternalApiException externalApiException) {
            assertThat(externalApiException.getHttpStatus().value()).isEqualTo(500);
        }

        verify(userConfiguredAccessRepository, times(1)).deleteAll(optUca);
        verify(userConfiguredAccessRepository, times(1)).saveAll(any());
    }

    @Test
    void test_modifyUserRolesAndUserConfiguredAccessForNullAccessType() throws JsonProcessingException {
        UUID uuid = UUID.randomUUID();
        String uuidStr = uuid.toString();

        ProfessionalUser professionalUserMock = mock(ProfessionalUser.class);
        when(professionalUserRepository.findByUserIdentifier(uuidStr)).thenReturn(professionalUserMock);
        when(professionalUserMock.getId()).thenReturn(uuid);

        NewUserResponse newUserResponse = new NewUserResponse();
        newUserResponse.setUserIdentifier(uuidStr);
        newUserResponse.setIdamStatus("ACTIVE");
        ObjectMapper mapper = new ObjectMapper();
        String body = mapper.writeValueAsString(newUserResponse);

        when(userProfileFeignClient.getUserProfileByEmail(any())).thenReturn(Response.builder()
                .request(mock(Request.class)).body(body, Charset.defaultCharset()).status(200).build());

        Response response = mock(Response.class);
        when(response.status()).thenReturn(200);
        when(userProfileFeignClient.modifyUserRoles(any(), any(), any())).thenReturn(response);

        UserConfiguredAccess userConfiguredAccess1 = new UserConfiguredAccess();
        UserConfiguredAccess userConfiguredAccess2 = new UserConfiguredAccess();
        List<UserConfiguredAccess> optUca = List.of(userConfiguredAccess1, userConfiguredAccess2);

        when(userConfiguredAccessRepository.findByUserConfiguredAccessId_ProfessionalUser_Id(uuid))
                .thenReturn(optUca);

        UserProfileUpdatedData userProfileUpdatedData = new UserProfileUpdatedData();
        String origin = "EXUI";
        Optional<String> originOpt = Optional.of(origin);
        professionalUserService.modifyUserConfiguredAccessAndRoles(userProfileUpdatedData,
                uuidStr, originOpt);


        verify(userConfiguredAccessRepository, times(1)).deleteAll(optUca);
        verify(userConfiguredAccessRepository, times(0)).saveAll(any());
    }

    @Test
    void test_modifyUserConfiguredAccessForDeleteFail() throws JsonProcessingException {
        UUID uuid = UUID.randomUUID();
        String uuidStr = uuid.toString();

        ProfessionalUser professionalUserMock = mock(ProfessionalUser.class);
        when(professionalUserRepository.findByUserIdentifier(uuidStr)).thenReturn(professionalUserMock);
        when(professionalUserMock.getId()).thenReturn(uuid);

        NewUserResponse getUserResponse = new NewUserResponse();
        getUserResponse.setUserIdentifier(uuidStr);
        getUserResponse.setIdamStatus("ACTIVE");
        ObjectMapper mapper = new ObjectMapper();
        String body = mapper.writeValueAsString(getUserResponse);
        when(userProfileFeignClient.getUserProfileByEmail(any())).thenReturn(Response.builder()
                .request(mock(Request.class)).body(body, Charset.defaultCharset()).status(200).build());

        Set<UserAccessType> userAccessTypes = new HashSet<>();
        UserAccessType userAccessType1 = new UserAccessType();
        userAccessTypes.add(userAccessType1);
        UserProfileUpdatedData userProfileUpdatedData = new UserProfileUpdatedData();
        userProfileUpdatedData.setUserAccessTypes(userAccessTypes);
        UserConfiguredAccess userConfiguredAccess1 = new UserConfiguredAccess();
        UserConfiguredAccess userConfiguredAccess2 = new UserConfiguredAccess();
        List<UserConfiguredAccess> optUca = List.of(userConfiguredAccess1, userConfiguredAccess2);

        when(userConfiguredAccessRepository.findByUserConfiguredAccessId_ProfessionalUser_Id(uuid))
                .thenReturn(optUca);

        doThrow(new IllegalArgumentException()).when(userConfiguredAccessRepository).deleteAll(optUca);

        try {
            professionalUserService.modifyUserConfiguredAccessAndRoles(userProfileUpdatedData,
                    uuidStr, Optional.of("EXUI"));
        } catch (ExternalApiException eae) {
            assertThat(eae.getHttpStatus().value()).isEqualTo(500);
            assertThat(eae.getMessage()).contains("001");
        }

        verify(userConfiguredAccessRepository, times(0)).saveAll(any());
    }

    @Test
    void test_modifyUserConfiguredAccessForSaveFail() throws JsonProcessingException {
        UUID uuid = UUID.randomUUID();
        String uuidStr = uuid.toString();

        ProfessionalUser professionalUserMock = mock(ProfessionalUser.class);
        when(professionalUserRepository.findByUserIdentifier(uuidStr)).thenReturn(professionalUserMock);
        when(professionalUserMock.getId()).thenReturn(uuid);

        NewUserResponse getUserResponse = new NewUserResponse();
        getUserResponse.setUserIdentifier(uuidStr);
        getUserResponse.setIdamStatus("ACTIVE");
        ObjectMapper mapper = new ObjectMapper();
        String body = mapper.writeValueAsString(getUserResponse);
        when(userProfileFeignClient.getUserProfileByEmail(any())).thenReturn(Response.builder()
                .request(mock(Request.class)).body(body, Charset.defaultCharset()).status(200).build());

        Set<UserAccessType> userAccessTypes = new HashSet<>();
        UserAccessType userAccessType1 = new UserAccessType();
        userAccessTypes.add(userAccessType1);
        UserProfileUpdatedData userProfileUpdatedData = new UserProfileUpdatedData();
        userProfileUpdatedData.setUserAccessTypes(userAccessTypes);
        UserConfiguredAccess userConfiguredAccess1 = new UserConfiguredAccess();
        UserConfiguredAccess userConfiguredAccess2 = new UserConfiguredAccess();
        List<UserConfiguredAccess> optUca = List.of(userConfiguredAccess1, userConfiguredAccess2);

        when(userConfiguredAccessRepository.findByUserConfiguredAccessId_ProfessionalUser_Id(uuid))
                .thenReturn(optUca);

        doThrow(new IllegalArgumentException()).when(userConfiguredAccessRepository).saveAll(any());

        try {
            professionalUserService.modifyUserConfiguredAccessAndRoles(userProfileUpdatedData,
                    uuidStr, Optional.of("EXUI"));
        } catch (ExternalApiException eae) {
            assertThat(eae.getHttpStatus().value()).isEqualTo(500);
            assertThat(eae.getMessage()).contains("002");
        }

        verify(userConfiguredAccessRepository, times(1)).deleteAll(optUca);
    }


    @Test
    void test_ErrorOnsaveAllUserAccessTypes() {
        Set<UserAccessType> userAccessTypes = new HashSet<>();
        UserAccessType userAccessType1 = new UserAccessType();
        userAccessTypes.add(userAccessType1);
        UserAccessType userAccessType2 = new UserAccessType();
        userAccessTypes.add(userAccessType2);

        doThrow(new IllegalArgumentException()).when(userConfiguredAccessRepository).saveAll(any());

        try {
            professionalUserService.saveAllUserAccessTypes(professionalUser,
                    userAccessTypes);
        } catch (ExternalApiException eae) {
            assertThat(eae.getHttpStatus().value()).isEqualTo(500);
            assertThat(eae.getMessage()).contains("002");
        }

        verify(userConfiguredAccessRepository, times(1)).saveAll(any());
    }

    @Test
    void test_saveAllUserAccessTypes() {
        Set<UserAccessType> userAccessTypes = new HashSet<>();
        UserAccessType userAccessType1 = new UserAccessType();
        userAccessTypes.add(userAccessType1);
        UserAccessType userAccessType2 = new UserAccessType();
        userAccessTypes.add(userAccessType2);

        professionalUserService.saveAllUserAccessTypes(professionalUser,
                userAccessTypes);

        verify(userConfiguredAccessRepository, times(1)).saveAll(any());
    }

    void callModifyRolesForUser(HttpStatus status) {
        when(userProfileFeignClient.modifyUserRoles(any(), any(), any())).thenThrow(feignExceptionMock);

        String uuid = UUID.randomUUID().toString();

        Throwable thrown
                = catchThrowable(() ->  professionalUserService
                .modifyRolesForUser(new UserProfileUpdatedData(), uuid, Optional.of("")));
        assertThat(thrown)
                .isInstanceOf(ExternalApiException.class)
                .hasMessageContaining(ERROR_MESSAGE_UP_FAILED);
        assertThat(((ExternalApiException) thrown).getHttpStatus()).isEqualTo(status);
        verify(userProfileFeignClient, times(1)).modifyUserRoles(any(), any(), any());
    }

    @Test
    @SuppressWarnings("unchecked")
    void test_fetchUsersForRefresh() {
        List<ProfessionalUser> professionalUserList = new ArrayList<>();

        ProfessionalUser professionalUser = new ProfessionalUser("fName", "lName",
                "some@email.com", organisation);
        professionalUser.setCreated(LocalDateTime.now());
        professionalUserList.add(professionalUser);

        when(professionalUserRepository.findByLastUpdatedGreaterThanEqual(any()))
                .thenReturn(professionalUserList);

        LocalDateTime currentDateTime = LocalDateTime.of(2023,12,6,13,36,25);
        String since = currentDateTime.format(ISO_DATE_TIME_FORMATTER);

        ResponseEntity<Object> responseEntity = professionalUserService.fetchUsersForRefresh(since, null, null, null);

        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);

        verify(professionalUserRepository, times(1))
                .findByLastUpdatedGreaterThanEqual(any());
    }

    @Test
    @SuppressWarnings("unchecked")
    void test_fetchUsersForRefreshPageable() {
        List<ProfessionalUser> professionalUserList = new ArrayList<>();
        Page<ProfessionalUser> professionalUserPage = mock(Page.class);

        ProfessionalUser professionalUser = new ProfessionalUser("fName", "lName",
                "some@email.com", organisation);
        professionalUser.setCreated(LocalDateTime.now());
        professionalUserList.add(professionalUser);

        when(professionalUserRepository.findByLastUpdatedGreaterThanEqual(any(), any()))
                .thenReturn(professionalUserPage);

        when(professionalUserPage.getContent()).thenReturn(professionalUserList);

        LocalDateTime currentDateTime = LocalDateTime.of(2023,12,6,13,36,25);
        String since = currentDateTime.format(ISO_DATE_TIME_FORMATTER);

        ResponseEntity<Object> responseEntity =
                professionalUserService.fetchUsersForRefresh(since, null, 10, null);

        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);

        verify(professionalUserRepository, times(1))
                .findByLastUpdatedGreaterThanEqual(any(), any());
    }

    @Test
    void test_fetchUsersForRefreshWithSearchAfter() {
        List<ProfessionalUser> professionalUserList = new ArrayList<>();

        ProfessionalUser professionalUser = new ProfessionalUser("fName", "lName",
                "some@email.com", organisation);
        professionalUser.setCreated(LocalDateTime.now());
        professionalUserList.add(professionalUser);

        when(professionalUserRepository.findByLastUpdatedGreaterThanEqualAndIdGreaterThan(any(), any()))
                .thenReturn(professionalUserList);

        LocalDateTime currentDateTime = LocalDateTime.of(2023,12,6,13,36,25);
        String since = currentDateTime.format(ISO_DATE_TIME_FORMATTER);

        ResponseEntity<Object> responseEntity = professionalUserService
                .fetchUsersForRefresh(since, null, null, UUID.randomUUID());

        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);

        verify(professionalUserRepository, times(1))
                .findByLastUpdatedGreaterThanEqualAndIdGreaterThan(any(), any());
    }

    @Test
    @SuppressWarnings("unchecked")
    void test_findSingleRefreshUser() {
        ProfessionalUser professionalUser = new ProfessionalUser("fName", "lName",
                "some@email.com", organisation);
        professionalUser.setId(UUID.randomUUID());
        professionalUser.setCreated(LocalDateTime.now());

        when(professionalUserRepository.findByUserIdentifier(any()))
                .thenReturn(professionalUser);

        ResponseEntity<Object> responseEntity = professionalUserService
                .fetchUsersForRefresh(null, userIdentifier, null, null);

        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);

        verify(professionalUserRepository, times(1))
                .findByUserIdentifier(any());
    }

}
