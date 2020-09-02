package uk.gov.hmcts.reform.professionalapi.controller.external;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Request;
import feign.Response;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;
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
import uk.gov.hmcts.reform.professionalapi.oidc.JwtGrantedAuthoritiesConverter;
import uk.gov.hmcts.reform.professionalapi.service.OrganisationService;
import uk.gov.hmcts.reform.professionalapi.service.ProfessionalUserService;

public class ProfessionalExternalUserControllerTest {

    private OrganisationService organisationServiceMock;
    private ProfessionalUserService professionalUserServiceMock;
    private Organisation organisation;
    private ProfessionalUserReqValidator profExtUsrReqValidator;
    private OrganisationIdentifierValidatorImpl organisationIdentifierValidatorImpl;
    private OrganisationCreationRequestValidator organisationCreationRequestValidator;
    private ResponseEntity<Object> responseEntity;
    private ProfessionalUser professionalUser;
    private UserProfileFeignClient userProfileFeignClient;
    private JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverterMock;
    private UserInfo userInfoMock;


    @InjectMocks
    private ProfessionalExternalUserController professionalExternalUserController;

    @Before
    @SuppressWarnings("unchecked")
    public void setUp() throws Exception {
        organisation = new Organisation("Org-Name", OrganisationStatus.PENDING, "sra-id",
                "companyN", false, "www.org.com");
        professionalUser = new ProfessionalUser("fName", "lName", "user@test.com",
                organisation);
        organisationServiceMock = mock(OrganisationService.class);
        professionalUserServiceMock = mock(ProfessionalUserService.class);
        organisationIdentifierValidatorImpl = mock(OrganisationIdentifierValidatorImpl.class);
        profExtUsrReqValidator = mock(ProfessionalUserReqValidator.class);
        organisationCreationRequestValidator = mock(OrganisationCreationRequestValidator.class);
        responseEntity = mock(ResponseEntity.class);
        userProfileFeignClient = mock(UserProfileFeignClient.class);
        jwtGrantedAuthoritiesConverterMock = mock(JwtGrantedAuthoritiesConverter.class);
        userInfoMock = mock(UserInfo.class);

        organisation.setOrganisationIdentifier(UUID.randomUUID().toString());

        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void test_FindUsersByOrganisation() {
        final HttpStatus expectedHttpStatus = HttpStatus.OK;
        ProfessionalUser professionalUser = new ProfessionalUser("fName", "lastName",
                "emailAddress", organisation);

        List<SuperUser> users = new ArrayList<>();
        users.add(professionalUser.toSuperUser());
        organisation.setUsers(users);
        organisation.setStatus(OrganisationStatus.ACTIVE);

        List<String> authorities = new ArrayList<>();
        authorities.add(TestConstants.PUI_USER_MANAGER);

        when(jwtGrantedAuthoritiesConverterMock.getUserInfo()).thenReturn(userInfoMock);
        when(userInfoMock.getRoles()).thenReturn(authorities);

        organisation.setStatus(OrganisationStatus.ACTIVE);

        when(organisationServiceMock.getOrganisationByOrgIdentifier(organisation.getOrganisationIdentifier()))
                .thenReturn(organisation);
        when(professionalUserServiceMock.findProfessionalUserProfileByEmailAddress("emailAddress"))
                .thenReturn(professionalUser);
        when(professionalUserServiceMock.findProfessionalUsersByOrganisation(any(Organisation.class),
                any(String.class), any(Boolean.class), any(String.class))).thenReturn(responseEntity);
        when(organisationIdentifierValidatorImpl.ifUserRoleExists(authorities,
                TestConstants.PUI_USER_MANAGER)).thenReturn(true);
        when(responseEntity.getStatusCode()).thenReturn(HttpStatus.OK);

        doNothing().when(profExtUsrReqValidator).validateRequest(any(String.class), any(String.class),
                any(String.class));
        doNothing().when(organisationIdentifierValidatorImpl).validate(any(Organisation.class),
                any(OrganisationStatus.class), any(String.class));
        doNothing().when(organisationCreationRequestValidator).validateOrganisationIdentifier(any(String.class));

        ResponseEntity<?> actual = professionalExternalUserController.findUsersByOrganisation(organisation
                .getOrganisationIdentifier(), "true", "", true, null, null,
                null);

        assertThat(actual).isNotNull();
        assertThat(actual.getStatusCode().value()).isEqualTo(expectedHttpStatus.value());

        verify(organisationServiceMock, times(1))
                .getOrganisationByOrgIdentifier(organisation.getOrganisationIdentifier());
        verify(professionalUserServiceMock, times(1))
                .findProfessionalUsersByOrganisation(organisation, "true", true, "");
    }

    @Test
    public void test_FindUsersByOrganisationWithPuiCaseManager() {
        final HttpStatus expectedHttpStatus = HttpStatus.OK;
        ProfessionalUser professionalUser = new ProfessionalUser("fName", "lastName",
                "emailAddress", organisation);

        List<SuperUser> users = new ArrayList<>();
        users.add(professionalUser.toSuperUser());
        organisation.setUsers(users);
        organisation.setStatus(OrganisationStatus.ACTIVE);

        List<String> authorities = new ArrayList<>();
        authorities.add(TestConstants.PUI_CASE_MANAGER);

        when(jwtGrantedAuthoritiesConverterMock.getUserInfo()).thenReturn(userInfoMock);
        when(userInfoMock.getRoles()).thenReturn(authorities);

        organisation.setStatus(OrganisationStatus.ACTIVE);

        when(organisationServiceMock.getOrganisationByOrgIdentifier(organisation.getOrganisationIdentifier()))
                .thenReturn(organisation);
        when(professionalUserServiceMock.findProfessionalUserProfileByEmailAddress("emailAddress"))
                .thenReturn(professionalUser);
        when(professionalUserServiceMock.findProfessionalUsersByOrganisation(any(Organisation.class),
                any(String.class), any(Boolean.class), any(String.class))).thenReturn(responseEntity);
        when(organisationIdentifierValidatorImpl.ifUserRoleExists(authorities, TestConstants.PUI_CASE_MANAGER))
                .thenReturn(true);
        when(responseEntity.getStatusCode()).thenReturn(HttpStatus.OK);

        doNothing().when(profExtUsrReqValidator).validateRequest(any(String.class), any(String.class),
                any(String.class));
        doNothing().when(organisationIdentifierValidatorImpl).validate(any(Organisation.class),
                any(OrganisationStatus.class), any(String.class));
        doNothing().when(organisationCreationRequestValidator).validateOrganisationIdentifier(any(String.class));
        doNothing().when(professionalUserServiceMock).checkUserStatusIsActiveByUserId(any(String.class));
        doNothing().when(professionalUserServiceMock).checkUserStatusIsActiveByUserId(any(String.class));

        ResponseEntity<?> actual = professionalExternalUserController
                .findUsersByOrganisation(organisation.getOrganisationIdentifier(), "true", "",
                        true, null, null, null);
        assertThat(actual).isNotNull();
        assertThat(actual.getStatusCode().value()).isEqualTo(expectedHttpStatus.value());

        verify(organisationServiceMock, times(1))
                .getOrganisationByOrgIdentifier(organisation.getOrganisationIdentifier());
        verify(professionalUserServiceMock, times(1))
                .findProfessionalUsersByOrganisation(organisation, "true", true,
                        "Active");
    }

    @Test
    public void testFindUsersByOrganisationWithoutRoles() throws Exception {
        final HttpStatus expectedHttpStatus = HttpStatus.OK;
        ProfessionalUser professionalUser = new ProfessionalUser("fName", "lastName",
                "emailAddress", organisation);

        List<SuperUser> users = new ArrayList<>();
        users.add(professionalUser.toSuperUser());
        organisation.setUsers(users);
        organisation.setStatus(OrganisationStatus.ACTIVE);

        List<String> authorities = new ArrayList<>();
        authorities.add(TestConstants.PUI_CASE_MANAGER);

        when(jwtGrantedAuthoritiesConverterMock.getUserInfo()).thenReturn(userInfoMock);
        when(userInfoMock.getRoles()).thenReturn(authorities);

        organisation.setStatus(OrganisationStatus.ACTIVE);

        List<ProfessionalUsersResponse> userProfiles = new ArrayList<>();
        userProfiles.add(new ProfessionalUsersResponse(professionalUser));
        ProfessionalUsersEntityResponse professionalUsersEntityResponse = new ProfessionalUsersEntityResponse();
        professionalUsersEntityResponse.setUserProfiles(userProfiles);

        when(organisationServiceMock.getOrganisationByOrgIdentifier(organisation.getOrganisationIdentifier()))
                .thenReturn(organisation);
        when(professionalUserServiceMock.findProfessionalUserProfileByEmailAddress("emailAddress"))
                .thenReturn(professionalUser);
        when(professionalUserServiceMock.findProfessionalUsersByOrganisation(any(Organisation.class),
                any(String.class), any(Boolean.class), any(String.class))).thenReturn(responseEntity);
        when(responseEntity.getStatusCode()).thenReturn(HttpStatus.OK);
        when(responseEntity.getBody()).thenReturn(professionalUsersEntityResponse);

        doNothing().when(profExtUsrReqValidator).validateRequest(any(String.class), any(String.class),
                any(String.class));
        doNothing().when(organisationIdentifierValidatorImpl).validate(any(Organisation.class),
                any(OrganisationStatus.class), any(String.class));
        doNothing().when(organisationCreationRequestValidator).validateOrganisationIdentifier(any(String.class));
        doNothing().when(professionalUserServiceMock).checkUserStatusIsActiveByUserId(any(String.class));

        ResponseEntity<?> actual = professionalExternalUserController
                .findUsersByOrganisation(organisation.getOrganisationIdentifier(), "true", "",
                        false, null, null, null);
        assertThat(actual).isNotNull();
        assertThat(actual.getStatusCode().value()).isEqualTo(expectedHttpStatus.value());

        List<ProfessionalUsersResponse> usersResponse = ((ProfessionalUsersEntityResponse) actual.getBody())
                .getUserProfiles();
        assertThat(usersResponse.get(0).getRoles()).isNull();

        verify(organisationServiceMock, times(1))
                .getOrganisationByOrgIdentifier(organisation.getOrganisationIdentifier());
        verify(professionalUserServiceMock, times(1))
                .findProfessionalUsersByOrganisation(organisation, "true", false, "Active");
        verify(professionalUserServiceMock, times(1))
                .findProfessionalUsersByOrganisation(any(Organisation.class), any(String.class), any(Boolean.class),
                        any(String.class));
        verify(responseEntity, times(1)).getStatusCode();
        verify(responseEntity, times(1)).getBody();
    }

    @Test
    public void test_FindUserByEmailWithPuiUserManager() {
        final HttpStatus expectedHttpStatus = HttpStatus.OK;

        ProfessionalUser professionalUser = new ProfessionalUser("fName", "lastName",
                "test@email.com", organisation);
        List<SuperUser> users = new ArrayList<>();
        users.add(professionalUser.toSuperUser());
        organisation.setUsers(users);
        organisation.setStatus(OrganisationStatus.ACTIVE);

        UserInfo userInfoMock = mock(UserInfo.class);
        List<String> authorities = new ArrayList<>();
        authorities.add(TestConstants.PUI_USER_MANAGER);
        when(userInfoMock.getRoles()).thenReturn(authorities);

        organisation.setStatus(OrganisationStatus.ACTIVE);

        when(organisationServiceMock.getOrganisationByOrgIdentifier(organisation.getOrganisationIdentifier()))
                .thenReturn(organisation);
        when(professionalUserServiceMock.findProfessionalUserProfileByEmailAddress("testing@email.com"))
                .thenReturn(professionalUser);
        when(organisationIdentifierValidatorImpl.ifUserRoleExists(authorities, TestConstants.PUI_USER_MANAGER))
                .thenReturn(true);
        when(responseEntity.getStatusCode()).thenReturn(HttpStatus.OK);

        doNothing().when(profExtUsrReqValidator).validateRequest(any(String.class), any(String.class),
                any(String.class));
        doNothing().when(organisationIdentifierValidatorImpl).validate(any(Organisation.class),
                any(OrganisationStatus.class), any(String.class));
        doNothing().when(organisationCreationRequestValidator).validateOrganisationIdentifier(any(String.class));

        Optional<ResponseEntity<Object>> actual = professionalExternalUserController
                .findUserByEmail(organisation.getOrganisationIdentifier(), "testing@email.com");
        assertThat(actual).isNotNull();
        assertThat(actual.get().getStatusCode().value()).isEqualTo(expectedHttpStatus.value());

        verify(professionalUserServiceMock, times(1))
                .findProfessionalUserProfileByEmailAddress("testing@email.com");
    }

    @Test
    public void test_FindUserStatusByEmail() throws JsonProcessingException {
        organisation.setStatus(OrganisationStatus.ACTIVE);
        professionalUser.getOrganisation().setStatus(OrganisationStatus.ACTIVE);

        NewUserResponse newUserResponse = new NewUserResponse();
        newUserResponse.setUserIdentifier("a123dfgr46");
        newUserResponse.setIdamStatus("ACTIVE");
        ObjectMapper mapper = new ObjectMapper();
        String body = mapper.writeValueAsString(newUserResponse);

        ResponseEntity<NewUserResponse> responseEntity1 = new ResponseEntity<NewUserResponse>(newUserResponse,
                HttpStatus.OK);

        when(userProfileFeignClient.getUserProfileByEmail(anyString())).thenReturn(Response.builder()
                .request(mock(Request.class)).body(body, Charset.defaultCharset()).status(200).build());
        when(professionalUserServiceMock.findUserStatusByEmailAddress(professionalUser.getEmailAddress()))
                .thenReturn(responseEntity1);

        ResponseEntity<NewUserResponse> newResponse = professionalExternalUserController
                .findUserStatusByEmail(professionalUser.getEmailAddress());

        assertThat(newResponse).isNotNull();
        assertThat(newResponse.getBody()).isNotNull();
        assertThat(newResponse.getBody().getUserIdentifier()).isEqualTo("a123dfgr46");

        verify(professionalUserServiceMock, times(1))
                .findUserStatusByEmailAddress(professionalUser.getEmailAddress());
    }

    @Test(expected = InvalidRequest.class)
    public void test_FindUserByEmailWithPuiUserManagerThrows400WithInvalidEmail() {
        Optional<ResponseEntity<Object>> actual = professionalExternalUserController
                .findUserByEmail(organisation.getOrganisationIdentifier(), "invalid-email");

        assertThat(actual).isNotNull();
        assertThat(actual.get().getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verify(organisationCreationRequestValidator, times(1)).validateEmail("invalid-email");
    }
}