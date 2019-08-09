package uk.gov.hmcts.reform.professionalapi.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.professionalapi.generator.ProfessionalApiGenerator.LENGTH_OF_ORGANISATION_IDENTIFIER;
import static uk.gov.hmcts.reform.professionalapi.generator.ProfessionalApiGenerator.generateUniqueAlphanumericId;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Request;
import feign.Response;

import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;

import org.powermock.api.mockito.PowerMockito;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.professionalapi.controller.feign.UserProfileFeignClient;
import uk.gov.hmcts.reform.professionalapi.controller.request.NewUserCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.RetrieveUserProfilesRequest;
import uk.gov.hmcts.reform.professionalapi.controller.response.GetUserProfileResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.IdamStatus;
import uk.gov.hmcts.reform.professionalapi.controller.response.NewUserResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.UserProfile;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus;
import uk.gov.hmcts.reform.professionalapi.domain.PrdEnum;
import uk.gov.hmcts.reform.professionalapi.domain.PrdEnumId;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;
import uk.gov.hmcts.reform.professionalapi.persistence.OrganisationRepository;
import uk.gov.hmcts.reform.professionalapi.persistence.PrdEnumRepository;
import uk.gov.hmcts.reform.professionalapi.persistence.ProfessionalUserRepository;
import uk.gov.hmcts.reform.professionalapi.persistence.UserAttributeRepository;
import uk.gov.hmcts.reform.professionalapi.service.impl.ProfessionalUserServiceImpl;
import uk.gov.hmcts.reform.professionalapi.service.impl.UserAttributeServiceImpl;

public class ProfessionalUserServiceTest {

    private final ProfessionalUserRepository professionalUserRepository = Mockito.mock(ProfessionalUserRepository.class);
    private final Organisation organisation = Mockito.mock(Organisation.class);
    private final OrganisationRepository organisationRepository = mock(OrganisationRepository.class);
    private final UserAttributeRepository userAttributeRepository = mock(UserAttributeRepository.class);
    private final PrdEnumRepository prdEnumRepository = mock(PrdEnumRepository.class);
    private final UserProfileFeignClient userProfileFeignClient = mock(UserProfileFeignClient.class);
    private final OrganisationRepository organisationRepositoryMock = mock(OrganisationRepository.class);

    private final UserAttributeServiceImpl userAttributeService = mock(UserAttributeServiceImpl.class);

    private final ProfessionalUser professionalUserMock = mock(ProfessionalUser.class);

    private final RetrieveUserProfilesRequest retrieveUserProfilesRequest = mock(RetrieveUserProfilesRequest.class);
    private final Response responseMock = mock(Response.class);
    private final GetUserProfileResponse getUserProfileResponseMock = mock(GetUserProfileResponse.class);

    private final ProfessionalUser professionalUser = new ProfessionalUser("some-fname",
            "some-lname",
            "some-email",
            Mockito.mock(Organisation.class));

    private List<ProfessionalUser> usersNonEmptyList = new ArrayList<ProfessionalUser>();

    private final ProfessionalUserServiceImpl professionalUserService = new ProfessionalUserServiceImpl(
            organisationRepository, professionalUserRepository, userAttributeRepository,
            prdEnumRepository, userAttributeService, userProfileFeignClient);

    private NewUserCreationRequest newUserCreationRequest;

    private  List<PrdEnum> prdEnums = new ArrayList<>();
    private List<String> userRoles;

    @Before
    public void setup() {
        userRoles = new ArrayList<>();
        userRoles.add("pui-user-manager");
        PrdEnumId prdEnumId = mock(PrdEnumId.class);
        PrdEnum anEnum = new PrdEnum(prdEnumId, "pui-user-manager", "SIDAM_ROLE");
        prdEnums.add(anEnum);

        newUserCreationRequest = new NewUserCreationRequest("first",
                "last",
                "domain@hotmail.com",
                userRoles,
                new ArrayList<>());
    }

    @Test
    public void retrieveUserByEmail() throws JsonProcessingException {
        UUID id = UUID.randomUUID();
        professionalUser.setUserIdentifier(id);

        List<ProfessionalUser> users = new ArrayList<>();
        users.add(professionalUser);
        List<String> roles = new ArrayList<>();
        roles.add("pui-case-manager");
        PowerMockito.when(professionalUser.getOrganisation().getStatus()).thenReturn(OrganisationStatus.ACTIVE);
        PowerMockito.when(organisation.getStatus()).thenReturn(OrganisationStatus.ACTIVE);
        PowerMockito.when(organisation.getUsers()).thenReturn(users);
        List<Organisation> organisations = new ArrayList<>();
        organisations.add(organisation);
        PowerMockito.when(professionalUserRepository.findByEmailAddress(any(String.class)))
                .thenReturn(professionalUser);

        UserProfile profile = new UserProfile(UUID.randomUUID(), "email@org.com", "firstName", "lastName", IdamStatus.ACTIVE);

        GetUserProfileResponse userProfileResponse = new GetUserProfileResponse(profile, false);

        ObjectMapper mapper = new ObjectMapper();

        String body = mapper.writeValueAsString(userProfileResponse);

        PowerMockito.when(getUserProfileResponseMock.getRoles()).thenReturn(roles);

        PowerMockito.when(userProfileFeignClient.getUserProfileById(anyString())).thenReturn(Response.builder().request(mock(Request.class)).body(body, Charset.defaultCharset()).status(200).build());

        ProfessionalUser user1 = professionalUserService.findProfessionalUserProfileByEmailAddress("email@org.com");
        assertEquals(professionalUser.getFirstName(), user1.getFirstName());
        assertEquals(professionalUser.getLastName(), user1.getLastName());
        assertEquals(professionalUser.getEmailAddress(), user1.getEmailAddress());
    }

    public void retrieveUserByEmailNotFound() {
        Mockito.when(professionalUserRepository.findByEmailAddress(any(String.class)))
                .thenReturn(null);

        assertThat(professionalUserService.findProfessionalUserProfileByEmailAddress("some-email")).isNull();
    }


    @Test
    @Ignore
    public void findUsersByOrganisation_with_deleted_users() {
        usersNonEmptyList.add(professionalUser);
        Mockito.when(professionalUserRepository.findByOrganisation(organisation))
                .thenReturn(usersNonEmptyList);

        List<UUID> usersId = new ArrayList<>();
        usersId.add(UUID.randomUUID());

        RetrieveUserProfilesRequest retrieveUserProfilesRequest1 = new RetrieveUserProfilesRequest(usersId);

        Mockito.when(userProfileFeignClient.getUserProfiles(retrieveUserProfilesRequest1, "true")).thenReturn(any(Response.class));

        ResponseEntity responseEntity = professionalUserService.findProfessionalUsersByOrganisation(organisation, "true");
        Mockito.verify(
                professionalUserRepository,
                Mockito.times(1)).findByOrganisation(organisation);

        assertThat(responseEntity).isNotNull();
    }

    @Test
    @Ignore
    public void findUsersByOrganisation_with_non_deleted_users() {

        ProfessionalUser professionalUserDeleted = new ProfessionalUser("some-fname",
                "some-lname",
                "some-email",
                Mockito.mock(Organisation.class));
        professionalUserDeleted.setDeleted(LocalDateTime.now());

        usersNonEmptyList.add(professionalUserDeleted);
        usersNonEmptyList.add(professionalUser);
        Mockito.when(professionalUserRepository.findByOrganisation(organisation))
                .thenReturn(usersNonEmptyList);

        ResponseEntity responseEntity = professionalUserService.findProfessionalUsersByOrganisation(organisation, "false");
        Mockito.verify(
                professionalUserRepository,
                Mockito.times(1)).findByOrganisation(organisation);

        assertThat(responseEntity).isNotNull();
    }

    @Test
    public void addNewUserToAnOrganisation() {

        when(organisation.getOrganisationIdentifier()).thenReturn(generateUniqueAlphanumericId(LENGTH_OF_ORGANISATION_IDENTIFIER));
        when(professionalUserRepository.save(any(ProfessionalUser.class))).thenReturn(professionalUser);

        NewUserResponse newUserResponse = professionalUserService.addNewUserToAnOrganisation(professionalUser, userRoles, prdEnums);
        assertThat(newUserResponse).isNotNull();

        verify(professionalUserRepository, times(1)).save(any(ProfessionalUser.class));
        verify(userAttributeService, times(1)).addUserAttributesToUser(any(ProfessionalUser.class), (Mockito.anyList()), (Mockito.anyList()));
    }

    @Test(expected = EmptyResultDataAccessException.class)
    @Ignore
    public void findUsersByOrganisationEmptyResultExceptionTest() {
        List<ProfessionalUser> emptyList = new ArrayList<>();

        Mockito.when(professionalUserRepository.findByOrganisation(organisation))
                .thenReturn(emptyList);

        ResponseEntity responseEntity = professionalUserService.findProfessionalUsersByOrganisation(organisation, "false");
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
        when(professionalUserRepository.findByEmailAddress("some@email.com")).thenReturn(professionalUserMock);

        ProfessionalUser user = professionalUserService.findProfessionalUserByEmailAddress("some@email.com");
        assertThat(user).isNotNull();
    }
}