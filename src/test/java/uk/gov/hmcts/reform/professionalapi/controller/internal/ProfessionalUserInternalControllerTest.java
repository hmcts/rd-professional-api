package uk.gov.hmcts.reform.professionalapi.controller.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;
import uk.gov.hmcts.reform.professionalapi.controller.constants.IdamStatus;
import uk.gov.hmcts.reform.professionalapi.controller.request.validator.OrganisationCreationRequestValidator;
import uk.gov.hmcts.reform.professionalapi.controller.request.validator.OrganisationIdentifierValidator;
import uk.gov.hmcts.reform.professionalapi.controller.request.validator.UserProfileUpdateRequestValidator;
import uk.gov.hmcts.reform.professionalapi.controller.request.validator.impl.OrganisationIdentifierValidatorImpl;
import uk.gov.hmcts.reform.professionalapi.domain.ModifyUserRolesResponse;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;
import uk.gov.hmcts.reform.professionalapi.domain.RoleAdditionResponse;
import uk.gov.hmcts.reform.professionalapi.domain.SuperUser;
import uk.gov.hmcts.reform.professionalapi.domain.UserProfileUpdatedData;
import uk.gov.hmcts.reform.professionalapi.oidc.JwtGrantedAuthoritiesConverter;
import uk.gov.hmcts.reform.professionalapi.service.OrganisationService;
import uk.gov.hmcts.reform.professionalapi.service.ProfessionalUserService;

public class ProfessionalUserInternalControllerTest {

    private OrganisationService organisationServiceMock;
    private ProfessionalUserService professionalUserServiceMock;
    private Organisation organisation;
    private OrganisationIdentifierValidator organisationIdentifierValidatorMock;
    private OrganisationCreationRequestValidator organisationCreationRequestValidatorMock;
    private UserProfileUpdateRequestValidator userProfileUpdateRequestValidatorMock;
    private ResponseEntity<Object> responseEntityMock;
    private UserProfileUpdatedData userProfileUpdatedData;
    List<String> prdAdminRoles;
    List<String> systemUserRoles;

    @InjectMocks
    private ProfessionalUserInternalController professionalUserInternalController;
    @Mock
    JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverterMock;

    @Before
    @SuppressWarnings("unchecked")
    public void setUp() {
        organisation = new Organisation("Org-Name", OrganisationStatus.PENDING, "sra-id",
                "companyN", false, "www.org.com");
        userProfileUpdatedData = new UserProfileUpdatedData("test@email.com", "firstName",
                "lastName", IdamStatus.ACTIVE.name(), null, null);

        organisationServiceMock = mock(OrganisationService.class);
        professionalUserServiceMock = mock(ProfessionalUserService.class);
        organisationIdentifierValidatorMock = mock(OrganisationIdentifierValidatorImpl.class);
        organisationCreationRequestValidatorMock = mock(OrganisationCreationRequestValidator.class);
        userProfileUpdateRequestValidatorMock = mock(UserProfileUpdateRequestValidator.class);
        responseEntityMock = mock(ResponseEntity.class);
        organisation.setOrganisationIdentifier(UUID.randomUUID().toString());
        prdAdminRoles = new ArrayList<>();
        prdAdminRoles.add("prd-admin");
        systemUserRoles = new ArrayList<>();
        systemUserRoles.add("prd-aac-system");

        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testFindUsersByOrganisation_for_prd_admin() {
        testFindUsersByOrganisation(prdAdminRoles);
    }

    @Test
    public void testFindUsersByOrganisation_for_system_user() {
        testFindUsersByOrganisation(systemUserRoles);
    }

    public void testFindUsersByOrganisation(List<String> userRoles) {
        final HttpStatus expectedHttpStatus = HttpStatus.OK;
        ProfessionalUser professionalUser =
                new ProfessionalUser("fName", "lastName", "emailAddress", organisation);

        List<SuperUser> users = new ArrayList<>();
        users.add(professionalUser.toSuperUser());
        organisation.setUsers(users);
        organisation.setStatus(OrganisationStatus.ACTIVE);

        when(organisationServiceMock.getOrganisationByOrgIdentifier(organisation.getOrganisationIdentifier()))
                .thenReturn(organisation);
        when(professionalUserServiceMock
                .findProfessionalUsersByOrganisation(any(Organisation.class), any(String.class), any(Boolean.class),
                        any(String.class))).thenReturn(responseEntityMock);
        when(responseEntityMock.getStatusCode()).thenReturn(HttpStatus.OK);
        when(jwtGrantedAuthoritiesConverterMock.getUserInfo())
                .thenReturn(new UserInfo("","", "", "", "", userRoles));

        doNothing().when(organisationIdentifierValidatorMock)
                .validate(any(Organisation.class), any(OrganisationStatus.class), any(String.class));
        doNothing().when(organisationCreationRequestValidatorMock).validateOrganisationIdentifier(any(String.class));

        ResponseEntity<?> actual = professionalUserInternalController
                .findUsersByOrganisation(organisation
                        .getOrganisationIdentifier(), "true", true, null, null);
        assertThat(actual).isNotNull();
        assertThat(actual.getStatusCode().value()).isEqualTo(expectedHttpStatus.value());

        verify(organisationServiceMock, times(1))
                .getOrganisationByOrgIdentifier(organisation.getOrganisationIdentifier());
        verify(professionalUserServiceMock, times(1))
                .findProfessionalUsersByOrganisation(any(Organisation.class),
                        any(String.class), any(Boolean.class), any(String.class));
        verify(responseEntityMock, times(1)).getStatusCode();
        verify(jwtGrantedAuthoritiesConverterMock, times(1)).getUserInfo();
    }


    @Test
    public void testFindUsersByOrganisationwithoutRoles() {
        final HttpStatus expectedHttpStatus = HttpStatus.OK;
        ProfessionalUser professionalUser = new ProfessionalUser("fName", "lastName",
                "emailAddress", organisation);

        List<SuperUser> users = new ArrayList<>();
        users.add(professionalUser.toSuperUser());
        organisation.setUsers(users);
        organisation.setStatus(OrganisationStatus.ACTIVE);

        when(organisationServiceMock.getOrganisationByOrgIdentifier(organisation.getOrganisationIdentifier()))
                .thenReturn(organisation);
        when(professionalUserServiceMock.findProfessionalUsersByOrganisation(any(Organisation.class), any(String.class),
                any(Boolean.class), any(String.class))).thenReturn(responseEntityMock);
        when(responseEntityMock.getStatusCode()).thenReturn(HttpStatus.OK);
        when(jwtGrantedAuthoritiesConverterMock.getUserInfo())
                .thenReturn(new UserInfo("","", "", "", "", prdAdminRoles));

        doNothing().when(organisationIdentifierValidatorMock).validate(any(Organisation.class),
                any(OrganisationStatus.class), any(String.class));
        doNothing().when(organisationCreationRequestValidatorMock).validateOrganisationIdentifier(any(String.class));

        ResponseEntity<?> actualRolesFalse = professionalUserInternalController
                .findUsersByOrganisation(organisation.getOrganisationIdentifier(), "true", true,
                        null, null);
        assertThat(actualRolesFalse).isNotNull();
        assertThat(actualRolesFalse.getStatusCode().value()).isEqualTo(expectedHttpStatus.value());

        verify(organisationServiceMock, times(1))
                .getOrganisationByOrgIdentifier(organisation.getOrganisationIdentifier());
        verify(professionalUserServiceMock, times(1))
                .findProfessionalUsersByOrganisation(organisation, "true", true, "");
        verify(responseEntityMock, times(1)).getStatusCode();
        verify(jwtGrantedAuthoritiesConverterMock, times(1)).getUserInfo();
    }

    @Test
    public void testFindUsersByOrganisationwithoutRolesDefaultNull() {
        final HttpStatus expectedHttpStatus = HttpStatus.OK;
        ProfessionalUser professionalUser = new ProfessionalUser("fName", "lastName",
                "emailAddress", organisation);

        List<SuperUser> users = new ArrayList<>();
        users.add(professionalUser.toSuperUser());
        organisation.setUsers(users);
        organisation.setStatus(OrganisationStatus.ACTIVE);

        when(organisationServiceMock.getOrganisationByOrgIdentifier(organisation.getOrganisationIdentifier()))
                .thenReturn(organisation);
        when(professionalUserServiceMock.findProfessionalUsersByOrganisation(any(Organisation.class), any(String.class),
                any(Boolean.class), any(String.class))).thenReturn(responseEntityMock);
        when(responseEntityMock.getStatusCode()).thenReturn(HttpStatus.OK);
        when(jwtGrantedAuthoritiesConverterMock.getUserInfo())
                .thenReturn(new UserInfo("","", "", "", "", prdAdminRoles));

        doNothing().when(organisationIdentifierValidatorMock).validate(any(Organisation.class),
                any(OrganisationStatus.class), any(String.class));
        doNothing().when(organisationCreationRequestValidatorMock).validateOrganisationIdentifier(any(String.class));

        ResponseEntity<?> actualRolesFalse = professionalUserInternalController
                .findUsersByOrganisation(organisation.getOrganisationIdentifier(), "true", true,
                        null, null);
        assertThat(actualRolesFalse).isNotNull();
        assertThat(actualRolesFalse.getStatusCode().value()).isEqualTo(expectedHttpStatus.value());

        verify(organisationServiceMock, times(1))
                .getOrganisationByOrgIdentifier(organisation.getOrganisationIdentifier());
        verify(professionalUserServiceMock, times(1))
                .findProfessionalUsersByOrganisation(organisation, "true", true, "");
        verify(responseEntityMock, times(1)).getStatusCode();
        verify(jwtGrantedAuthoritiesConverterMock, times(1)).getUserInfo();
    }

    @Test
    public void test_FindUserByEmailWithPuiUserManager() {
        final String email = "testing@email.com";
        final HttpStatus expectedHttpStatus = HttpStatus.OK;
        ProfessionalUser professionalUser = new ProfessionalUser("fName", "lastName",
                "test@email.com", organisation);
        List<SuperUser> users = new ArrayList<>();
        users.add(professionalUser.toSuperUser());
        organisation.setUsers(users);
        organisation.setStatus(OrganisationStatus.ACTIVE);

        when(organisationServiceMock.getOrganisationByOrgIdentifier(organisation.getOrganisationIdentifier()))
                .thenReturn(organisation);
        when(professionalUserServiceMock.findProfessionalUserProfileByEmailAddress(email)).thenReturn(professionalUser);
        when(responseEntityMock.getStatusCode()).thenReturn(HttpStatus.OK);

        doNothing().when(organisationIdentifierValidatorMock).validate(any(Organisation.class),
                any(OrganisationStatus.class), any(String.class));
        doNothing().when(organisationCreationRequestValidatorMock).validateOrganisationIdentifier(any(String.class));

        ResponseEntity actual = professionalUserInternalController.findUserByEmail(email);
        assertThat(actual).isNotNull();
        assertThat(actual.getStatusCode().value()).isEqualTo(expectedHttpStatus.value());

        verify(professionalUserServiceMock, times(1))
                .findProfessionalUserProfileByEmailAddress(email);
    }

    @Test
    public void test_ModifyRolesForExistingUserOfOrganisation() throws JsonProcessingException {

        ModifyUserRolesResponse modifyUserRolesResponse = new ModifyUserRolesResponse();
        RoleAdditionResponse roleAdditionResponse = new RoleAdditionResponse();
        roleAdditionResponse.setIdamMessage("some nessage");
        roleAdditionResponse.setIdamStatusCode("200");
        modifyUserRolesResponse.setRoleAdditionResponse(roleAdditionResponse);
        ObjectMapper mapper = new ObjectMapper();
        String body = mapper.writeValueAsString(modifyUserRolesResponse);
        ResponseEntity<Object> responseEntity = ResponseEntity.status(200).body(body);
        when(professionalUserServiceMock.modifyRolesForUser(any(), any(), any())).thenReturn(responseEntity);

        when(userProfileUpdateRequestValidatorMock.validateRequest(userProfileUpdatedData))
                .thenReturn(userProfileUpdatedData);

        String userId = UUID.randomUUID().toString();
        ResponseEntity<Object> actualData = professionalUserInternalController
                .modifyRolesForExistingUserOfOrganisation(userProfileUpdatedData, "123456A", userId,
                        Optional.of("EXUI"));

        assertThat(actualData).isNotNull();
        assertThat(actualData.getStatusCode()).isEqualTo(HttpStatus.OK);

        verify(professionalUserServiceMock, times(1)).modifyRolesForUser(userProfileUpdatedData,
                userId, Optional.of("EXUI"));
        verify(userProfileUpdateRequestValidatorMock, times(1))
                .validateRequest(userProfileUpdatedData);
    }
}
