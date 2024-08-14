package uk.gov.hmcts.reform.professionalapi.controller.external;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;
import uk.gov.hmcts.reform.professionalapi.controller.constants.IdamStatus;
import uk.gov.hmcts.reform.professionalapi.controller.constants.TestConstants;
import uk.gov.hmcts.reform.professionalapi.controller.feign.UserProfileFeignClient;
import uk.gov.hmcts.reform.professionalapi.controller.request.InvalidRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.validator.OrganisationCreationRequestValidator;
import uk.gov.hmcts.reform.professionalapi.controller.request.validator.ProfessionalUserReqValidator;
import uk.gov.hmcts.reform.professionalapi.controller.request.validator.impl.OrganisationIdentifierValidatorImpl;
import uk.gov.hmcts.reform.professionalapi.controller.response.NewUserResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.ProfessionalUsersEntityResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.ProfessionalUsersResponse;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;
import uk.gov.hmcts.reform.professionalapi.domain.SuperUser;
import uk.gov.hmcts.reform.professionalapi.domain.UserProfileUpdatedData;
import uk.gov.hmcts.reform.professionalapi.repository.IdamRepository;
import uk.gov.hmcts.reform.professionalapi.service.OrganisationService;
import uk.gov.hmcts.reform.professionalapi.service.ProfessionalUserService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.professionalapi.util.RefDataUtil.randomUUID;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
class ProfessionalExternalUserControllerTest {

    private OrganisationService organisationServiceMock;
    private ProfessionalUserService professionalUserServiceMock;
    private Organisation organisation;
    private ProfessionalUserReqValidator profExtUsrReqValidator;
    private OrganisationIdentifierValidatorImpl organisationIdentifierValidatorImpl;
    private OrganisationCreationRequestValidator organisationCreationRequestValidator;
    private ResponseEntity<Object> responseEntity;
    private ProfessionalUser professionalUser;
    private Authentication authentication;
    private UserProfileFeignClient userProfileFeignClient;
    private IdamRepository idamRepositoryMock;
    private SecurityContext securityContext;
    private UserInfo userInfoMock;
    private final UUID userIdentifier = UUID.randomUUID();
    private static final String USER_JWT = "Bearer 8gf364fg367f67";


    @InjectMocks
    private ProfessionalExternalUserController professionalExternalUserController;
    HttpServletRequest httpRequest = mock(HttpServletRequest.class);

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() throws Exception {
        organisation = new Organisation("Org-Name", OrganisationStatus.PENDING, "sra-id",
                "companyN", false, "www.org.com");
        professionalUser = new ProfessionalUser("fName", "lName", "user@test.com",
                organisation);
        organisationServiceMock = mock(OrganisationService.class);
        professionalUserServiceMock = mock(ProfessionalUserService.class);
        organisationIdentifierValidatorImpl = mock(OrganisationIdentifierValidatorImpl.class);
        profExtUsrReqValidator = mock(ProfessionalUserReqValidator.class);
        organisationCreationRequestValidator = mock(OrganisationCreationRequestValidator.class);
        authentication = Mockito.mock(Authentication.class);
        responseEntity = mock(ResponseEntity.class);
        securityContext = mock(SecurityContext.class);
        userProfileFeignClient = mock(UserProfileFeignClient.class);
        idamRepositoryMock = mock(IdamRepository.class);
        userInfoMock = mock(UserInfo.class);

        organisation.setOrganisationIdentifier(randomUUID());

        MockitoAnnotations.openMocks(this);
    }

    @Test
    void test_FindUsersByOrganisation() {
        final HttpStatus expectedHttpStatus = HttpStatus.OK;
        ProfessionalUser professionalUser = new ProfessionalUser("fName", "lastName",
                "emailAddress", organisation);

        List<SuperUser> users = new ArrayList<>();
        users.add(professionalUser.toSuperUser());
        organisation.setUsers(users);
        organisation.setStatus(OrganisationStatus.ACTIVE);

        List<String> authorities = new ArrayList<>();
        authorities.add(TestConstants.PUI_USER_MANAGER);

        when(idamRepositoryMock.getUserInfo(anyString())).thenReturn(userInfoMock);
        when(userInfoMock.getRoles()).thenReturn(authorities);

        organisation.setStatus(OrganisationStatus.ACTIVE);

        when(organisationServiceMock.getOrganisationByOrgIdentifier(organisation.getOrganisationIdentifier()))
                .thenReturn(organisation);
        when(professionalUserServiceMock.findProfessionalUsersByOrganisation(any(Organisation.class), anyString(),
                any(String.class), any(Boolean.class), any(String.class))).thenReturn(responseEntity);
        when(organisationIdentifierValidatorImpl.ifUserRoleExists(authorities,
                TestConstants.PUI_USER_MANAGER)).thenReturn(true);
        when(responseEntity.getStatusCode()).thenReturn(HttpStatus.OK);
        when(professionalUserServiceMock.findProfessionalUserByUserIdentifier(any(UUID.class)))
                .thenReturn(professionalUser);

        doNothing().when(profExtUsrReqValidator).validateRequest(any(String.class), any(String.class),
                any(String.class));
        lenient().doNothing().when(organisationIdentifierValidatorImpl).validate(any(Organisation.class),
                any(OrganisationStatus.class), any(String.class));
        doNothing().when(organisationCreationRequestValidator).validateOrganisationIdentifier(any(String.class));

        ResponseEntity<?> actual = professionalExternalUserController.findUsersByOrganisation(organisation
                        .getOrganisationIdentifier(), "true", "", true, null,
                null, userIdentifier.toString(), null);

        assertThat(actual).isNotNull();
        assertThat(actual.getStatusCode().value()).isEqualTo(expectedHttpStatus.value());

        verify(organisationServiceMock, times(1))
                .getOrganisationByOrgIdentifier(organisation.getOrganisationIdentifier());
        verify(professionalUserServiceMock, times(1))
                .findProfessionalUsersByOrganisation(organisation, userIdentifier.toString(), "true", true, "");
    }

    @Test
    void test_FindUsersByOrganisationWithPuiCaseManager() {
        final HttpStatus expectedHttpStatus = HttpStatus.OK;
        ProfessionalUser professionalUser = new ProfessionalUser("fName", "lastName",
                "emailAddress", organisation);

        List<SuperUser> users = new ArrayList<>();
        users.add(professionalUser.toSuperUser());
        organisation.setUsers(users);
        organisation.setStatus(OrganisationStatus.ACTIVE);

        List<String> authorities = new ArrayList<>();
        authorities.add(TestConstants.PUI_CASE_MANAGER);

        when(idamRepositoryMock.getUserInfo(anyString())).thenReturn(userInfoMock);
        when(userInfoMock.getRoles()).thenReturn(authorities);

        organisation.setStatus(OrganisationStatus.ACTIVE);

        when(organisationServiceMock.getOrganisationByOrgIdentifier(organisation.getOrganisationIdentifier()))
                .thenReturn(organisation);
        when(professionalUserServiceMock.findProfessionalUsersByOrganisation(any(Organisation.class), anyString(),
                any(String.class), any(Boolean.class), any(String.class))).thenReturn(responseEntity);
        when(organisationIdentifierValidatorImpl.ifUserRoleExists(authorities, TestConstants.PUI_USER_MANAGER))
                .thenReturn(true);
        when(responseEntity.getStatusCode()).thenReturn(HttpStatus.OK);
        when(professionalUserServiceMock.findProfessionalUserByUserIdentifier(any(UUID.class)))
                .thenReturn(professionalUser);

        doNothing().when(profExtUsrReqValidator).validateRequest(any(String.class), any(String.class),
                any(String.class));
        lenient().doNothing().when(organisationIdentifierValidatorImpl).validate(any(Organisation.class),
                any(OrganisationStatus.class), any(String.class));

        ResponseEntity<?> actual = professionalExternalUserController
                .findUsersByOrganisation(organisation.getOrganisationIdentifier(), "true", "",
                        true, null, null, userIdentifier.toString(),null);
        assertThat(actual).isNotNull();
        assertThat(actual.getStatusCode().value()).isEqualTo(expectedHttpStatus.value());

        verify(organisationServiceMock, times(1))
                .getOrganisationByOrgIdentifier(organisation.getOrganisationIdentifier());
        verify(professionalUserServiceMock, times(1))
                .findProfessionalUsersByOrganisation(organisation, userIdentifier.toString(), "true", true,
                        "");
    }

    @Test
    void testFindUsersByOrganisationWithoutRoles() throws Exception {
        final HttpStatus expectedHttpStatus = HttpStatus.OK;
        ProfessionalUser professionalUser = new ProfessionalUser("fName", "lastName",
                "emailAddress", organisation);

        List<SuperUser> users = new ArrayList<>();
        users.add(professionalUser.toSuperUser());
        organisation.setUsers(users);
        organisation.setStatus(OrganisationStatus.ACTIVE);

        List<String> authorities = new ArrayList<>();
        authorities.add(TestConstants.PUI_CASE_MANAGER);

        when(idamRepositoryMock.getUserInfo(anyString())).thenReturn(userInfoMock);
        when(userInfoMock.getRoles()).thenReturn(authorities);

        organisation.setStatus(OrganisationStatus.ACTIVE);

        List<ProfessionalUsersResponse> userProfiles = new ArrayList<>();
        userProfiles.add(new ProfessionalUsersResponse(professionalUser));
        ProfessionalUsersEntityResponse professionalUsersEntityResponse = new ProfessionalUsersEntityResponse();
        professionalUsersEntityResponse.setUserProfiles(userProfiles);

        when(organisationServiceMock.getOrganisationByOrgIdentifier(organisation.getOrganisationIdentifier()))
                .thenReturn(organisation);
        lenient().when(professionalUserServiceMock.findProfessionalUsersByOrganisation(any(Organisation.class),
                anyString(), any(String.class), any(Boolean.class), any(String.class))).thenReturn(responseEntity);
        when(responseEntity.getStatusCode()).thenReturn(HttpStatus.OK);
        when(responseEntity.getBody()).thenReturn(professionalUsersEntityResponse);
        when(professionalUserServiceMock.findProfessionalUserByUserIdentifier(any(UUID.class)))
                .thenReturn(professionalUser);

        doNothing().when(profExtUsrReqValidator).validateRequest(any(String.class), any(String.class),
                any(String.class));
        lenient().doNothing().when(organisationIdentifierValidatorImpl).validate(any(Organisation.class),
                any(OrganisationStatus.class), any(String.class));
        doNothing().when(organisationCreationRequestValidator).validateOrganisationIdentifier(any(String.class));

        ResponseEntity<?> actual = professionalExternalUserController
                .findUsersByOrganisation(organisation.getOrganisationIdentifier(), "true", "",
                        false, null, null, userIdentifier.toString(), null);
        assertThat(actual).isNotNull();
        assertThat(actual.getStatusCode().value()).isEqualTo(expectedHttpStatus.value());

        List<ProfessionalUsersResponse> usersResponse = ((ProfessionalUsersEntityResponse) actual.getBody())
                .getUsers();
        assertThat(usersResponse.get(0).getRoles()).isNull();

        verify(organisationServiceMock, times(1))
                .getOrganisationByOrgIdentifier(organisation.getOrganisationIdentifier());
        verify(professionalUserServiceMock, times(1))
                .findProfessionalUsersByOrganisation(organisation, userIdentifier.toString(), "true",
                        false, "Active");
        verify(professionalUserServiceMock, times(1))
                .findProfessionalUsersByOrganisation(any(Organisation.class), anyString(), any(String.class),
                        any(Boolean.class), any(String.class));
        verify(responseEntity, times(1)).getStatusCode();
        verify(responseEntity, times(1)).getBody();
    }

    @Test
    void testFindUsersByOrganisationWithUserHaveDifferentOrganisation() throws Exception {
        final HttpStatus expectedHttpStatus = HttpStatus.OK;
        ProfessionalUser professionalUser = new ProfessionalUser("fName", "lastName",
                "emailAddress", organisation);

        List<SuperUser> users = new ArrayList<>();
        users.add(professionalUser.toSuperUser());
        organisation.setUsers(users);
        organisation.setStatus(OrganisationStatus.ACTIVE);

        List<String> authorities = new ArrayList<>();
        authorities.add(TestConstants.PUI_CASE_MANAGER);

        Jwt jwt =   Jwt.withTokenValue(USER_JWT)
                .claim("aClaim", "aClaim")
                .claim("aud", Lists.newArrayList("ccd_gateway"))
                .header("aHeader", "aHeader")
                .build();
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication().getPrincipal()).thenReturn(jwt);
        when(idamRepositoryMock.getUserInfo(anyString())).thenReturn(userInfoMock);
        when(userInfoMock.getRoles()).thenReturn(authorities);
        when(profExtUsrReqValidator.validateUuid(anyString())).thenThrow(new InvalidRequest(""));
        organisation.setStatus(OrganisationStatus.ACTIVE);

        doNothing().when(profExtUsrReqValidator).validateRequest(any(String.class), any(String.class),
                any(String.class));
        String orgId = organisation.getOrganisationIdentifier();
        assertThrows(InvalidRequest.class, () ->  professionalExternalUserController
                .findUsersByOrganisation(orgId, "true", "",
                        false, null, null, "123456", null));
    }



    @Test
    void test_FindUserStatusByEmail() throws JsonProcessingException {
        organisation.setStatus(OrganisationStatus.ACTIVE);
        professionalUser.getOrganisation().setStatus(OrganisationStatus.ACTIVE);

        NewUserResponse newUserResponse = new NewUserResponse();
        newUserResponse.setUserIdentifier("a123dfgr46");
        newUserResponse.setIdamStatus("ACTIVE");
        ObjectMapper mapper = new ObjectMapper();
        String body = mapper.writeValueAsString(newUserResponse);
        String email = "test@email.com";
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(httpRequest));
        when(httpRequest.getHeader(anyString())).thenReturn(email);
        ResponseEntity<NewUserResponse> responseEntity1 = new ResponseEntity<NewUserResponse>(newUserResponse,
                HttpStatus.OK);

        when(professionalUserServiceMock.findUserStatusByEmailAddress(email))
                .thenReturn(responseEntity1);

        ResponseEntity<NewUserResponse> newResponse = professionalExternalUserController
                .findUserStatusByEmail(email);

        assertThat(newResponse).isNotNull();
        assertThat(newResponse.getBody()).isNotNull();
        assertThat(newResponse.getBody().getUserIdentifier()).isEqualTo("a123dfgr46");

        verify(professionalUserServiceMock, times(1))
                .findUserStatusByEmailAddress(email);
    }

    @Test
    void test_FindUserStatusByEmailFromHeader() throws JsonProcessingException {
        organisation.setStatus(OrganisationStatus.ACTIVE);
        professionalUser.getOrganisation().setStatus(OrganisationStatus.ACTIVE);

        NewUserResponse newUserResponse = new NewUserResponse();
        newUserResponse.setUserIdentifier("a123dfgr46");
        newUserResponse.setIdamStatus("ACTIVE");
        String email = "test@email.com";
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(httpRequest));
        when(httpRequest.getHeader(anyString())).thenReturn(email);

        ResponseEntity<NewUserResponse> responseEntity1 = new ResponseEntity<NewUserResponse>(newUserResponse,
                HttpStatus.OK);

        when(professionalUserServiceMock.findUserStatusByEmailAddress(email))
                .thenReturn(responseEntity1);

        ResponseEntity<NewUserResponse> newResponse = professionalExternalUserController
                .findUserStatusByEmail(email);

        assertThat(newResponse).isNotNull();
        assertThat(newResponse.getBody()).isNotNull();
        assertThat(newResponse.getBody().getUserIdentifier()).isEqualTo("a123dfgr46");

        verify(professionalUserServiceMock, times(1))
                .findUserStatusByEmailAddress(email);
        verify(httpRequest, times(2)).getHeader(anyString());

    }

    @Test
    void test_FindUserStatusByEmailFromHeaderThrows400WhenEmailIsInvalid() {
        String email = "some-email";
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(httpRequest));
        when(httpRequest.getHeader(anyString())).thenReturn(email);

        assertThrows(InvalidRequest.class, () ->
                professionalExternalUserController.findUserStatusByEmail(email));
    }

    @Test
    void test_modifyRolesForExistingUserOfExternalOrganisation() {
        ResponseEntity<Object> newResponse = ResponseEntity.status(200).body("");
        UserProfileUpdatedData userProfileUpdatedData = new UserProfileUpdatedData("test@email.com", "firstName",
                "lastName", IdamStatus.ACTIVE.name(), null, null, null);
        String orgId = "org123";
        UUID userId = UUID.randomUUID();
        String userIdStr = userId.toString();
        String origin = "EXUI";
        Optional<String> originOpt = Optional.of(origin);
        when(professionalUserServiceMock.modifyUserConfiguredAccessAndRoles(userProfileUpdatedData,
                userId, originOpt)).thenReturn(newResponse);

        ResponseEntity<Object> response = professionalExternalUserController
                .modifyUserConfiguredAccessAndRolesForExistingUserOfExternalOrganisation(userProfileUpdatedData,
                        orgId, userIdStr, origin);

        assertThat(response).isNotNull();
        assertThat(response.getBody()).isNotNull();
    }

}
