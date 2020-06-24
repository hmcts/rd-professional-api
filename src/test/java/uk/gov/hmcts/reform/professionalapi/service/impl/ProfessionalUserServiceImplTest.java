package uk.gov.hmcts.reform.professionalapi.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.LENGTH_OF_ORGANISATION_IDENTIFIER;
import static uk.gov.hmcts.reform.professionalapi.generator.ProfessionalApiGenerator.generateUniqueAlphanumericId;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import feign.Request;
import feign.Response;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import lombok.SneakyThrows;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import uk.gov.hmcts.reform.professionalapi.controller.advice.ExternalApiException;
import uk.gov.hmcts.reform.professionalapi.controller.advice.ResourceNotFoundException;
import uk.gov.hmcts.reform.professionalapi.controller.constants.IdamStatus;
import uk.gov.hmcts.reform.professionalapi.controller.feign.UserProfileFeignClient;
import uk.gov.hmcts.reform.professionalapi.controller.request.validator.UserProfileUpdateRequestValidator;
import uk.gov.hmcts.reform.professionalapi.controller.request.validator.impl.UserProfileUpdateRequestValidatorImpl;
import uk.gov.hmcts.reform.professionalapi.controller.response.GetUserProfileResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.NewUserResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.ProfessionalUsersEntityResponse;
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
import uk.gov.hmcts.reform.professionalapi.domain.UserProfile;
import uk.gov.hmcts.reform.professionalapi.domain.UserProfileUpdatedData;
import uk.gov.hmcts.reform.professionalapi.repository.OrganisationRepository;
import uk.gov.hmcts.reform.professionalapi.repository.PrdEnumRepository;
import uk.gov.hmcts.reform.professionalapi.repository.ProfessionalUserRepository;
import uk.gov.hmcts.reform.professionalapi.repository.UserAttributeRepository;

public class ProfessionalUserServiceImplTest {

    private final ProfessionalUserRepository professionalUserRepository = Mockito.mock(ProfessionalUserRepository.class);
    private final OrganisationRepository organisationRepository = mock(OrganisationRepository.class);
    private final UserAttributeRepository userAttributeRepository = mock(UserAttributeRepository.class);
    private final PrdEnumRepository prdEnumRepository = mock(PrdEnumRepository.class);
    private final UserProfileFeignClient userProfileFeignClient = mock(UserProfileFeignClient.class);
    private final UserAttributeServiceImpl userAttributeService = mock(UserAttributeServiceImpl.class);
    private final FeignException feignExceptionMock = mock(FeignException.class);

    private final Organisation organisation = new Organisation("some-org-name", null, "PENDING", null, null, null);
    private final UserProfile userProfile = new UserProfile(UUID.randomUUID().toString(), "test@email.com", "fName", "lName", IdamStatus.PENDING);
    private final GetUserProfileResponse getUserProfileResponseMock = new GetUserProfileResponse(userProfile, false);

    private final ProfessionalUserServiceImpl professionalUserService = new ProfessionalUserServiceImpl(
            organisationRepository, professionalUserRepository, userAttributeRepository,
            prdEnumRepository, userAttributeService, userProfileFeignClient);

    private final ProfessionalUser professionalUser = new ProfessionalUser("some-fname", "some-lname", "some-email", organisation);

    private final SuperUser superUser = new SuperUser("some-fname", "some-lname", "some-super-email", organisation);

    private List<PrdEnum> prdEnums = new ArrayList<>();
    private List<String> userRoles = new ArrayList<>();

    @Before
    public void setup() {
        userRoles.add("pui-user-manager");
        PrdEnumId prdEnumId = new PrdEnumId();
        PrdEnum anEnum = new PrdEnum(prdEnumId, "pui-user-manager", "SIDAM_ROLE");
        prdEnums.add(anEnum);

        organisation.setOrganisationIdentifier(generateUniqueAlphanumericId(LENGTH_OF_ORGANISATION_IDENTIFIER));
        superUser.setUserIdentifier(UUID.randomUUID().toString());
        professionalUser.setUserIdentifier(UUID.randomUUID().toString());
    }

    @Test
    public void retrieveUserByEmail() throws JsonProcessingException {
        List<SuperUser> users = new ArrayList<>();
        users.add(superUser);
        List<String> roles = new ArrayList<>();
        roles.add("pui-case-manager");

        organisation.setStatus(OrganisationStatus.ACTIVE);
        professionalUser.getOrganisation().setStatus(OrganisationStatus.ACTIVE);
        organisation.setUsers(users);

        List<Organisation> organisations = new ArrayList<>();
        organisations.add(organisation);

        when(professionalUserRepository.findByEmailAddress(any(String.class))).thenReturn(professionalUser);

        String email = "email@org.com";
        UserProfile profile = new UserProfile(UUID.randomUUID().toString(), email, "firstName", "lastName", IdamStatus.ACTIVE);
        GetUserProfileResponse userProfileResponse = new GetUserProfileResponse(profile, false);

        ObjectMapper mapper = new ObjectMapper();
        String body = mapper.writeValueAsString(userProfileResponse);
        getUserProfileResponseMock.setRoles(roles);

        when(userProfileFeignClient.getUserProfileById(anyString())).thenReturn(Response.builder().request(mock(Request.class)).body(body, Charset.defaultCharset()).status(200).build());

        ProfessionalUser user1 = professionalUserService.findProfessionalUserProfileByEmailAddress(email);
        assertEquals(professionalUser.getFirstName(), user1.getFirstName());
        assertEquals(professionalUser.getLastName(), user1.getLastName());
        assertEquals(professionalUser.getEmailAddress(), user1.getEmailAddress());

        verify(professionalUserRepository, times(1)).findByEmailAddress(email);
        verify(userProfileFeignClient, times(1)).getUserProfileById(anyString());
    }

    @Test(expected = EmptyResultDataAccessException.class)
    public void retrieveUserByEmail_EmptyData() throws JsonProcessingException {
        List<SuperUser> users = new ArrayList<>();
        users.add(superUser);
        List<String> roles = new ArrayList<>();
        roles.add("pui-case-manager");
        organisation.setStatus(OrganisationStatus.ACTIVE);
        professionalUser.getOrganisation().setStatus(OrganisationStatus.ACTIVE);
        organisation.setUsers(users);
        List<Organisation> organisations = new ArrayList<>();
        organisations.add(organisation);

        when(professionalUserRepository.findByEmailAddress(any(String.class))).thenReturn(null);

        String email = "email@org.com";
        UserProfile profile = new UserProfile(UUID.randomUUID().toString(), email, "firstName", "lastName", IdamStatus.ACTIVE);
        GetUserProfileResponse userProfileResponse = new GetUserProfileResponse(profile, false);

        ObjectMapper mapper = new ObjectMapper();
        String body = mapper.writeValueAsString(userProfileResponse);
        getUserProfileResponseMock.setRoles(roles);

        when(userProfileFeignClient.getUserProfileById(anyString())).thenReturn(Response.builder().request(mock(Request.class)).body(body, Charset.defaultCharset()).status(200).build());

        professionalUserService.findProfessionalUserProfileByEmailAddress(email);

        verify(professionalUserRepository, times(1)).findByEmailAddress(email);
    }


    @Test
    public void findUsersByOrganisation_with_deleted_users() throws Exception {
        List<ProfessionalUser> users = new ArrayList<>();
        users.add(professionalUser);

        ProfessionalUsersEntityResponse professionalUsersEntityResponse = new ProfessionalUsersEntityResponse();
        List<ProfessionalUsersResponse> userProfiles = new ArrayList<>();

        ProfessionalUser profile = new ProfessionalUser("firstName", "lastName", "email@org.com", organisation);

        ProfessionalUsersResponse userProfileResponse = new ProfessionalUsersResponse(profile);
        userProfileResponse.setUserIdentifier(professionalUser.getUserIdentifier());
        userProfiles.add(userProfileResponse);
        professionalUsersEntityResponse.setUserProfiles(userProfiles);
        ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        String body = mapper.writeValueAsString(professionalUsersEntityResponse);

        when(professionalUserRepository.findByOrganisation(organisation)).thenReturn(users);
        when(userProfileFeignClient.getUserProfiles(any(), any(), any())).thenReturn(Response.builder().request(mock(Request.class)).body(body, Charset.defaultCharset()).status(200).build());

        ResponseEntity responseEntity = professionalUserService.findProfessionalUsersByOrganisation(organisation, "false", true, "");

        assertThat(responseEntity).isNotNull();
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);

        verify(professionalUserRepository, Mockito.times(1)).findByOrganisation(organisation);
        verify(userProfileFeignClient, times(1)).getUserProfiles(any(), eq("false"), eq("true"));
    }

    @Test
    public void findUsersByOrganisation_with_status_active() throws Exception {
        ProfessionalUsersResponse professionalUsersResponse = new ProfessionalUsersResponse(new ProfessionalUser("fName", "lName", "some@email.com", organisation));
        ProfessionalUsersResponse professionalUsersResponse1 = new ProfessionalUsersResponse(new ProfessionalUser("fName1", "lName1", "some1@email.com", organisation));
        ProfessionalUsersResponse professionalUsersResponse2 = new ProfessionalUsersResponse(new ProfessionalUser("fName2", "lName2", "some2@email.com", organisation));

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

        ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        String body = mapper.writeValueAsString(professionalUsersEntityResponse);

        Response response = Response.builder().request(mock(Request.class)).body(body, Charset.defaultCharset()).status(200).build();

        List<ProfessionalUser> users = new ArrayList<>();
        users.add(professionalUser);

        when(professionalUserRepository.findByOrganisation(organisation)).thenReturn(users);
        when(userProfileFeignClient.getUserProfiles(any(), any(), any())).thenReturn(response);

        ResponseEntity responseEntity = professionalUserService.findProfessionalUsersByOrganisation(organisation, "false", true, "Active");

        Mockito.verify(professionalUserRepository, Mockito.times(1)).findByOrganisation(organisation);
        assertThat(responseEntity).isNotNull();

        verify(professionalUserRepository, Mockito.times(1)).findByOrganisation(organisation);
        verify(userProfileFeignClient, times(1)).getUserProfiles(any(), eq("false"), eq("true"));
    }

    //@Test
    // not yet implemented (tdd)
    public void modify_user_roles() throws Exception {

        Set<RoleName> rolesData = new HashSet<RoleName>();
        RoleName roleName1 = new RoleName("pui-case-manager");
        RoleName roleName2 = new RoleName("pui-case-organisation");
        rolesData.add(roleName1);
        rolesData.add(roleName2);


        /*List<RoleName> rolesData = new ArrayList<>();
        rolesData.add("pui-case-manager");
        rolesData.add("pui-organisation-manager");*/


        Set<RoleName> rolesToDeleteData = new HashSet<RoleName>();
        RoleName roleToDeleteName = new RoleName("pui-finance-manager");
        rolesToDeleteData.add(roleToDeleteName);

        UserProfileUpdatedData userProfileUpdatedData = new UserProfileUpdatedData();

        UserProfileUpdatedData userProfileUpdatedData1 =
                new UserProfileUpdatedData("test@test.com", "fname", "lname", IdamStatus.ACTIVE.name(), rolesData, rolesToDeleteData);
        UserProfileUpdateRequestValidator sut = new UserProfileUpdateRequestValidatorImpl();
        UserProfileUpdatedData actualModifyProfileData = sut.validateRequest(userProfileUpdatedData);
        assertThat(actualModifyProfileData).isNotNull();
        assertThat(actualModifyProfileData.getEmail()).isNull();
        assertThat(actualModifyProfileData.getIdamStatus()).isNull();

        /*ModifyUserRolesResponse modifyUserRolesResponse = new ModifyUserRolesResponse();
        modifyUserRolesResponse.setAddRolesResponse(createAddRoleResponse(HttpStatus.OK, "Success"));
        modifyUserRolesResponse.setDeleteRolesResponse(createDeleteRoleResponse(HttpStatus.OK, "Success"));

        ObjectMapper mapper = new ObjectMapper();

        String body = mapper.writeValueAsString(modifyUserRolesResponse);

        ObjectMapper mapper1 = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        String body1 = mapper.writeValueAsString(modifyUserRolesResponse);



        when(userProfileFeignClient.modifyUserRoles(any(), any(), any())).thenReturn(Response.builder().request(mock(Request.class)).body(body, Charset.defaultCharset()).status(200).build());
        String id = UUID.randomUUID().toString();
        ModifyUserRolesResponse response = professionalUserService.modifyRolesForUser(modifyUserProfileData, id);

        assertThat(response).isNotNull();
        assertThat(response.getAddRolesResponse()).isNotNull();
        assertThat(response.getAddRolesResponse().getIdamMessage()).isEqualTo("Success");
        assertThat(response.getDeleteRolesResponse()).isNotNull();
        assertThat(response.getDeleteRolesResponse().get(0).getIdamMessage()).isEqualTo("Success");*/
    }

    @Test
    public void modify_user_roles_bad_request() throws Exception {
        UserProfileUpdatedData userProfileUpdatedData = new UserProfileUpdatedData();

        Set<RoleName> roles = new HashSet<>();
        RoleName roleName1 = new RoleName("pui-case-manager");
        RoleName roleName2 = new RoleName("pui-case-organisation");
        roles.add(roleName1);
        roles.add(roleName2);
        userProfileUpdatedData.setRolesAdd(roles);

        ModifyUserRolesResponse modifyUserRolesResponse = new ModifyUserRolesResponse();
        modifyUserRolesResponse.setRoleAdditionResponse(createAddRoleResponse(HttpStatus.BAD_REQUEST, "Request Not Valid"));

        ObjectMapper mapper = new ObjectMapper();
        String body = mapper.writeValueAsString(modifyUserRolesResponse);

        when(userProfileFeignClient.modifyUserRoles(any(), any(), any())).thenReturn(Response.builder().request(mock(Request.class)).body(body, Charset.defaultCharset()).status(200).build());

        ModifyUserRolesResponse response = professionalUserService.modifyRolesForUser(userProfileUpdatedData, UUID.randomUUID().toString(), Optional.of(""));

        assertThat(response).isNotNull();
        assertThat(response.getRoleAdditionResponse()).isNotNull();
        assertThat(response.getRoleAdditionResponse().getIdamMessage()).isEqualTo("Request Not Valid");

        verify(userProfileFeignClient, times(1)).modifyUserRoles(any(), any(), any());
    }

    @Test(expected = ExternalApiException.class)
    public void modify_user_roles_server_error() throws Exception {
        ModifyUserRolesResponse modifyUserRolesResponse = new ModifyUserRolesResponse();
        modifyUserRolesResponse.setRoleAdditionResponse(createAddRoleResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error"));
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

        ModifyUserRolesResponse response = professionalUserService.modifyRolesForUser(userProfileUpdatedData, UUID.randomUUID().toString(), Optional.of(""));

        assertThat(response).isNotNull();
        assertThat(response.getRoleAdditionResponse()).isNotNull();
        assertThat(response.getRoleAdditionResponse().getIdamMessage()).isEqualTo("Internal Server Error");

        verify(userProfileFeignClient, times(1)).modifyUserRoles(any(), any(), any());
    }

    @Test
    public void addNewUserToAnOrganisation() {
        when(professionalUserRepository.save(any(ProfessionalUser.class))).thenReturn(professionalUser);

        NewUserResponse newUserResponse = professionalUserService.addNewUserToAnOrganisation(professionalUser, userRoles, prdEnums);
        assertThat(newUserResponse).isNotNull();

        verify(professionalUserRepository, times(1)).save(any(ProfessionalUser.class));
        verify(userAttributeService, times(1)).addUserAttributesToUser(any(ProfessionalUser.class), (Mockito.anyList()), (Mockito.anyList()));
    }

    @Test(expected = ExternalApiException.class)
    public void findUsersByOrganisationEmptyResultExceptionTest() throws Exception {
        List<String> ids = new ArrayList<>();
        ids.add(professionalUser.getUserIdentifier());
        List<ProfessionalUser> users = new ArrayList<>();
        users.add(professionalUser);

        FeignException exceptionMock = mock(FeignException.class);
        when(exceptionMock.status()).thenReturn(500);
        when(professionalUserRepository.findByOrganisation(organisation)).thenReturn(users);
        when(userProfileFeignClient.getUserProfiles(any(), any(), any())).thenThrow(exceptionMock);

        ResponseEntity responseEntity = professionalUserService.findProfessionalUsersByOrganisation(organisation, "false", true, "");

        verify(professionalUserRepository, times(1)).findByOrganisation(organisation);
        verify(userProfileFeignClient, times(1)).modifyUserRoles(any(), any(), any());
        assertThat(responseEntity).isNotNull();
    }

    @Test(expected = ExternalApiException.class)
    public void findUsersByOrganisationExternalExceptionWith300StatusTest() throws Exception {
        List<String> ids = new ArrayList<>();
        ids.add(professionalUser.getUserIdentifier());
        List<ProfessionalUser> users = new ArrayList<>();
        users.add(professionalUser);

        FeignException exceptionMock = mock(FeignException.class);
        when(exceptionMock.status()).thenReturn(300);
        when(professionalUserRepository.findByOrganisation(organisation)).thenReturn(users);
        when(userProfileFeignClient.getUserProfiles(any(), any(), any())).thenThrow(exceptionMock);

        ResponseEntity responseEntity = professionalUserService.findProfessionalUsersByOrganisation(organisation, "false", true, "");

        verify(professionalUserRepository, times(1)).findByOrganisation(organisation);
        verify(userProfileFeignClient, times(1)).modifyUserRoles(any(), any(), any());
        assertThat(responseEntity).isNotNull();
    }

    @Test
    public void shouldPersistUser() {
        when(professionalUserRepository.save(any(ProfessionalUser.class))).thenReturn(professionalUser);

        ProfessionalUser actualProfessionalUser = professionalUserService.persistUser(professionalUser);

        assertThat(actualProfessionalUser).isNotNull();
        verify(professionalUserRepository, times(1)).save(any(ProfessionalUser.class));
    }

    @Test
    public void shouldReturnProfessionalUserByEmail() {
        String email = "some@email.com";
        when(professionalUserRepository.findByEmailAddress(email)).thenReturn(professionalUser);

        ProfessionalUser user = professionalUserService.findProfessionalUserByEmailAddress(email);
        assertThat(user).isNotNull();

        verify(professionalUserRepository, times(1)).findByEmailAddress(email);
    }

    @Test
    public void shouldReturnProfessionalUserById() {
        UUID id = UUID.randomUUID();
        ProfessionalUser professionalUserMock = mock(ProfessionalUser.class);

        Optional<ProfessionalUser> professionalUserOptional = Optional.of(professionalUserMock);

        when(professionalUserRepository.findById(id)).thenReturn(professionalUserOptional);

        ProfessionalUser professionalUserResponse = professionalUserService.findProfessionalUserById(id);
        assertThat(professionalUserResponse).isNotNull();

        verify(professionalUserRepository, times(1)).findById(id);
    }

    @Test
    public void shouldReturnProfessionalUserByIdShouldReturnNullIfUserNotFound() {
        UUID id = UUID.randomUUID();
        Optional<ProfessionalUser> professionalUserOptional = Optional.empty();

        when(professionalUserRepository.findById(id)).thenReturn(professionalUserOptional);

        ProfessionalUser professionalUserResponse = professionalUserService.findProfessionalUserById(id);
        assertThat(professionalUserResponse).isNull();

        verify(professionalUserRepository, times(1)).findById(id);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldReturnUsersInResponseEntityWithPageable() throws JsonProcessingException {
        Pageable pageableMock = mock(Pageable.class);
        List<ProfessionalUser> professionalUserList = new ArrayList<>();
        Page<ProfessionalUser> professionalUserPage = (Page<ProfessionalUser>) mock(Page.class);

        ProfessionalUser professionalUser = new ProfessionalUser("fName", "lName", "some@email.com", organisation);
        ProfessionalUser professionalUser1 = new ProfessionalUser("fName", "lName", "some1@email.com", organisation);
        professionalUserList.add(professionalUser);
        professionalUserList.add(professionalUser1);

        when(professionalUserRepository.findByOrganisation(organisation, pageableMock)).thenReturn(professionalUserPage);
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
        ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        String body = mapper.writeValueAsString(professionalUsersEntityResponse);
        Response response = Response.builder().request(mock(Request.class)).body(body, Charset.defaultCharset()).status(200).build();
        when(userProfileFeignClient.getUserProfiles(any(), any(), any())).thenReturn(response);

        ResponseEntity responseEntity = professionalUserService.findProfessionalUsersByOrganisationWithPageable(organisation, "false", false, "Active", pageableMock);
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getHeaders().get("paginationInfo")).isNotEmpty();

        verify(professionalUserRepository, times(1)).findByOrganisation(organisation, pageableMock);
        verify(professionalUserPage, times(2)).getContent();
        verify(userProfileFeignClient, times(1)).getUserProfiles(any(), any(), any());
    }

    @Test(expected = ResourceNotFoundException.class)
    @SuppressWarnings("unchecked")
    public void shouldThrowResourceNotFoundExceptionWhenNoUsersReturnedWithPageable() {
        Pageable pageableMock = mock(Pageable.class);
        Page<ProfessionalUser> professionalUserPage = (Page<ProfessionalUser>) mock(Page.class);

        when(professionalUserRepository.findByOrganisation(organisation, pageableMock)).thenReturn(professionalUserPage);

        professionalUserService.findProfessionalUsersByOrganisationWithPageable(organisation, "false", false, "Active", pageableMock);

        verify(professionalUserRepository, times(1)).findByOrganisation(organisation, pageableMock);
    }

    @Test(expected = ResourceNotFoundException.class)
    @SuppressWarnings("unchecked")
    public void shouldThrowResourceNotFoundExceptionWhenNoUsersReturned() {
        Pageable pageableMock = mock(Pageable.class);
        Page<ProfessionalUser> professionalUserPage = (Page<ProfessionalUser>) mock(Page.class);

        when(professionalUserRepository.findByOrganisation(organisation, pageableMock)).thenReturn(professionalUserPage);

        professionalUserService.findProfessionalUsersByOrganisation(organisation, "false", false, "Active");

        verify(professionalUserRepository, times(1)).findByOrganisation(organisation, pageableMock);
    }

    @Test
    public void findUsersByOrganisation_without_roles() throws Exception {
        ProfessionalUsersResponse professionalUsersResponse = new ProfessionalUsersResponse(new ProfessionalUser("fName", "lName", "some@email.com", organisation));
        ProfessionalUsersResponse professionalUsersResponse1 = new ProfessionalUsersResponse(new ProfessionalUser("fName1", "lName1", "some1@email.com", organisation));
        ProfessionalUsersResponse professionalUsersResponse2 = new ProfessionalUsersResponse(new ProfessionalUser("fName2", "lName2", "some2@email.com", organisation));

        professionalUsersResponse.setIdamStatus(IdamStatus.ACTIVE.toString());
        professionalUsersResponse1.setIdamStatus(IdamStatus.ACTIVE.toString());
        professionalUsersResponse2.setIdamStatus(IdamStatus.PENDING.toString());

        List<ProfessionalUsersResponse> userProfiles = new ArrayList<>();
        userProfiles.add(professionalUsersResponse);
        userProfiles.add(professionalUsersResponse1);
        userProfiles.add(professionalUsersResponse2);

        ProfessionalUsersEntityResponse professionalUsersEntityResponse = new ProfessionalUsersEntityResponse();
        professionalUsersEntityResponse.setUserProfiles(userProfiles);

        ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        String body = mapper.writeValueAsString(professionalUsersEntityResponse);

        Response response = Response.builder().request(mock(Request.class)).body(body, Charset.defaultCharset()).status(200).build();

        List<ProfessionalUser> users = new ArrayList<>();
        users.add(professionalUser);

        when(professionalUserRepository.findByOrganisation(organisation)).thenReturn(users);
        when(userProfileFeignClient.getUserProfiles(any(), any(), any())).thenReturn(response);

        ResponseEntity responseEntity = professionalUserService.findProfessionalUsersByOrganisation(organisation, "false", true, "Active");

        assertThat(responseEntity).isNotNull();

        List<ProfessionalUsersResponse> usersResponse = ((ProfessionalUsersEntityResponse)responseEntity.getBody()).getUserProfiles();
        assertThat(usersResponse.get(0).getRoles()).isNull();
        assertThat(usersResponse.get(1).getRoles()).isNull();

        verify(professionalUserRepository, Mockito.times(1)).findByOrganisation(organisation);
        verify(userProfileFeignClient, times(1)).getUserProfiles(any(), eq("false"), eq("true"));
    }

    @Test
    public void findUserStatusByEmail() throws Exception {
        organisation.setStatus(OrganisationStatus.ACTIVE);
        professionalUser.getOrganisation().setStatus(OrganisationStatus.ACTIVE);

        when(professionalUserRepository.findByEmailAddress(professionalUser.getEmailAddress())).thenReturn(professionalUser);

        NewUserResponse newUserResponse = new NewUserResponse();
        newUserResponse.setUserIdentifier("a123dfgr46");
        newUserResponse.setIdamStatus("ACTIVE");
        ObjectMapper mapper = new ObjectMapper();
        String body = mapper.writeValueAsString(newUserResponse);

        when(userProfileFeignClient.getUserProfileByEmail(anyString())).thenReturn(Response.builder().request(mock(Request.class)).body(body, Charset.defaultCharset()).status(200).build());

        ResponseEntity<NewUserResponse> newResponse = professionalUserService.findUserStatusByEmailAddress(professionalUser.getEmailAddress());

        assertThat(newResponse).isNotNull();
        assertThat(newResponse.getBody()).isNotNull();
        assertThat(newResponse.getBody().getUserIdentifier()).isEqualTo("a123dfgr46");

        verify(professionalUserRepository, times(1)).findByEmailAddress(professionalUser.getEmailAddress());
        verify(userProfileFeignClient, times(1)).getUserProfileByEmail(anyString());
    }

    @Test
    public void findUserStatusByEmailForPending() throws Exception {
        organisation.setStatus(OrganisationStatus.ACTIVE);

        when(professionalUserRepository.findByEmailAddress(professionalUser.getEmailAddress())).thenReturn(professionalUser);
        NewUserResponse newUserResponse = new NewUserResponse();
        newUserResponse.setIdamStatus("PENDING");
        ObjectMapper mapper = new ObjectMapper();
        String body = mapper.writeValueAsString(newUserResponse);

        when(userProfileFeignClient.getUserProfileByEmail(anyString())).thenReturn(Response.builder().request(mock(Request.class)).body(body, Charset.defaultCharset()).status(200).build());

        ResponseEntity<NewUserResponse> newResponse = professionalUserService.findUserStatusByEmailAddress(professionalUser.getEmailAddress());

        assertThat(newResponse.getStatusCodeValue()).isEqualTo(404);
        verify(professionalUserRepository, times(1)).findByEmailAddress(professionalUser.getEmailAddress());
        verify(userProfileFeignClient, times(1)).getUserProfileByEmail(anyString());
    }

    @Test(expected = EmptyResultDataAccessException.class)
    public void findUserStatusByEmailForPendingOrgThrowsException() throws Exception {
        organisation.setStatus(OrganisationStatus.PENDING);

        when(professionalUserRepository.findByEmailAddress(professionalUser.getEmailAddress())).thenReturn(professionalUser);
        NewUserResponse newUserResponse = new NewUserResponse();
        newUserResponse.setIdamStatus("PENDING");
        ObjectMapper mapper = new ObjectMapper();
        String body = mapper.writeValueAsString(newUserResponse);

        when(userProfileFeignClient.getUserProfileByEmail(anyString())).thenReturn(Response.builder().request(mock(Request.class)).body(body, Charset.defaultCharset()).status(200).build());

        ResponseEntity<NewUserResponse> newResponse = professionalUserService.findUserStatusByEmailAddress(professionalUser.getEmailAddress());
        assertThat(newResponse).isNotNull();
        assertThat(newResponse.getStatusCodeValue()).isEqualTo(404);
        verify(professionalUserRepository, times(1)).findByEmailAddress(professionalUser.getEmailAddress());
        verify(userProfileFeignClient, times(0)).getUserProfileByEmail(anyString());
    }

    @Test(expected = ExternalApiException.class)
    public void findUserStatusByEmailForActiveThrowsExceptionWhenUpServiceDown() throws Exception {
        organisation.setStatus(OrganisationStatus.ACTIVE);

        when(professionalUserRepository.findByEmailAddress(professionalUser.getEmailAddress())).thenReturn(professionalUser);
        when(userProfileFeignClient.getUserProfileByEmail(anyString())).thenThrow(new ExternalApiException(HttpStatus.valueOf(500), "UP Email Service Down"));

        ResponseEntity<NewUserResponse> status = professionalUserService.findUserStatusByEmailAddress(professionalUser.getEmailAddress());

        assertThat(status).isNull();
        assertThat(status.getStatusCode().value()).isEqualTo(500);
        verify(professionalUserRepository, times(1)).findByEmailAddress(professionalUser.getEmailAddress());
        verify(userProfileFeignClient, times(1)).getUserProfileByEmail(anyString());
    }


    @SneakyThrows
    @Test(expected = Test.None.class)
    public void checkUserStatusIsActiveByUserId() {
        professionalUser.setIdamStatus(IdamStatus.ACTIVE);

        when(professionalUserRepository.findByUserIdentifier(any(String.class))).thenReturn(professionalUser);

        NewUserResponse newUserResponse = new NewUserResponse();
        newUserResponse.setIdamStatus("ACTIVE");
        ObjectMapper mapper = new ObjectMapper();
        String body = mapper.writeValueAsString(newUserResponse);

        when(userProfileFeignClient.getUserProfileByEmail(anyString())).thenReturn(Response.builder().request(mock(Request.class)).body(body, Charset.defaultCharset()).status(200).build());

        String userId = UUID.randomUUID().toString();
        professionalUserService.checkUserStatusIsActiveByUserId(userId);
    }

    @Test(expected = AccessDeniedException.class)
    public void checkUserStatusIsActiveByUserId_Throws403_WhenNoUserFoundWithGivenId() {
        professionalUserService.checkUserStatusIsActiveByUserId(UUID.randomUUID().toString());
    }

    @SneakyThrows
    @Test(expected = AccessDeniedException.class)
    public void checkUserStatusIsActiveByUserId_Throws403_WhenUserIsNotActive() {
        professionalUser.setIdamStatus(IdamStatus.PENDING);

        when(professionalUserRepository.findById(any(UUID.class))).thenReturn(Optional.of(professionalUser));

        NewUserResponse newUserResponse = new NewUserResponse();
        newUserResponse.setIdamStatus("PENDING");
        ObjectMapper mapper = new ObjectMapper();
        String body = mapper.writeValueAsString(newUserResponse);

        when(userProfileFeignClient.getUserProfileByEmail(anyString())).thenReturn(Response.builder().request(mock(Request.class)).body(body, Charset.defaultCharset()).status(200).build());

        String userId = UUID.randomUUID().toString();
        professionalUserService.checkUserStatusIsActiveByUserId(userId);
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
}