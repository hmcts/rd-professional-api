package uk.gov.hmcts.reform.professionalapi.controller.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.doNothing;

import java.util.*;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequestValidator;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationIdentifierValidator;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationIdentifierValidatorImpl;
import uk.gov.hmcts.reform.professionalapi.controller.request.UserProfileUpdateRequestValidator;
import uk.gov.hmcts.reform.professionalapi.domain.*;
import uk.gov.hmcts.reform.professionalapi.service.OrganisationService;
import uk.gov.hmcts.reform.professionalapi.service.ProfessionalUserService;

public class ProfessionalUserInternalControllerTest {

    private OrganisationService organisationServiceMock;
    private ProfessionalUserService professionalUserServiceMock;
    private Organisation organisationMock;
    private OrganisationIdentifierValidator organisationIdentifierValidatorMock;
    private OrganisationCreationRequestValidator organisationCreationRequestValidatorMock;
    private UserProfileUpdateRequestValidator userProfileUpdateRequestValidatorMock;
    private ResponseEntity<?> responseEntityMock;
    private UserProfileUpdatedData userProfileUpdatedDataMock;


    @InjectMocks
    private ProfessionalUserInternalController professionalUserInternalController;


    @Before
    public void setUp() {
        organisationMock = mock(Organisation.class);
        organisationServiceMock = mock(OrganisationService.class);
        professionalUserServiceMock = mock(ProfessionalUserService.class);
        organisationIdentifierValidatorMock = mock(OrganisationIdentifierValidatorImpl.class);
        organisationCreationRequestValidatorMock = mock(OrganisationCreationRequestValidator.class);
        responseEntityMock = mock(ResponseEntity.class);
        userProfileUpdatedDataMock = mock(UserProfileUpdatedData.class);
        userProfileUpdateRequestValidatorMock = mock(UserProfileUpdateRequestValidator.class);


        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testFindUsersByOrganisation() {
        final HttpStatus expectedHttpStatus = HttpStatus.OK;
        ProfessionalUser professionalUser = new ProfessionalUser("fName", "lastName", "emailAddress", organisationMock);

        List<SuperUser> users = new ArrayList<>();
        users.add(professionalUser.toSuperUser());
        organisationMock.setUsers(users);
        organisationMock.setStatus(OrganisationStatus.ACTIVE);

        when(organisationMock.getOrganisationIdentifier()).thenReturn(UUID.randomUUID().toString());
        when(organisationMock.getStatus()).thenReturn(OrganisationStatus.ACTIVE);
        when(organisationServiceMock.getOrganisationByOrgIdentifier(organisationMock.getOrganisationIdentifier())).thenReturn(organisationMock);
        when(professionalUserServiceMock.findProfessionalUserProfileByEmailAddress("emailAddress")).thenReturn(professionalUser);
        when(professionalUserServiceMock.findProfessionalUsersByOrganisation(any(Organisation.class), any(String.class), any(Boolean.class), any(String.class))).thenReturn(responseEntityMock);
        when(responseEntityMock.getStatusCode()).thenReturn(HttpStatus.OK);

        doNothing().when(organisationIdentifierValidatorMock).validate(any(Organisation.class), any(OrganisationStatus.class), any(String.class));
        doNothing().when(organisationCreationRequestValidatorMock).validateOrganisationIdentifier(any(String.class));

        ResponseEntity<?> actual = professionalUserInternalController.findUsersByOrganisation(organisationMock.getOrganisationIdentifier(), "true", null, null);
        assertThat(actual).isNotNull();
        assertThat(actual.getStatusCode().value()).isEqualTo(expectedHttpStatus.value());
    }

    @Test
    public void testFindUserByEmailWithPuiUserManager() {
        final HttpStatus expectedHttpStatus = HttpStatus.OK;
        ProfessionalUser professionalUser = new ProfessionalUser("fName", "lastName", "test@email.com", organisationMock);
        List<SuperUser> users = new ArrayList<>();
        users.add(professionalUser.toSuperUser());
        organisationMock.setUsers(users);
        organisationMock.setStatus(OrganisationStatus.ACTIVE);


        when(organisationMock.getOrganisationIdentifier()).thenReturn(UUID.randomUUID().toString());
        when(organisationMock.getStatus()).thenReturn(OrganisationStatus.ACTIVE);
        when(organisationServiceMock.getOrganisationByOrgIdentifier(organisationMock.getOrganisationIdentifier())).thenReturn(organisationMock);
        when(professionalUserServiceMock.findProfessionalUserProfileByEmailAddress("testing@email.com")).thenReturn(professionalUser);
        when(responseEntityMock.getStatusCode()).thenReturn(HttpStatus.OK);

        doNothing().when(organisationIdentifierValidatorMock).validate(any(Organisation.class), any(OrganisationStatus.class), any(String.class));
        doNothing().when(organisationCreationRequestValidatorMock).validateOrganisationIdentifier(any(String.class));

        ResponseEntity actual = professionalUserInternalController.findUserByEmail("testing@email.com");
        assertThat(actual).isNotNull();
        assertThat(actual.getStatusCode().value()).isEqualTo(expectedHttpStatus.value());
    }

    @Test
    public void testModifyRolesForExistingUserOfOrganisation() {

        when(userProfileUpdateRequestValidatorMock.validateRequest(userProfileUpdatedDataMock)).thenReturn(userProfileUpdatedDataMock);

        ResponseEntity<ModifyUserRolesResponse> actualData = professionalUserInternalController.modifyRolesForExistingUserOfOrganisation(userProfileUpdatedDataMock, "123456A", UUID.randomUUID().toString(), Optional.of("EXUI"));

        assertThat(actualData).isNotNull();
        assertThat(actualData.getStatusCode()).isEqualTo(HttpStatus.OK);

    }
}
