package uk.gov.hmcts.reform.professionalapi.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import uk.gov.hmcts.reform.auth.checker.spring.serviceanduser.ServiceAndUserDetails;
import uk.gov.hmcts.reform.professionalapi.controller.external.ProfessionalExternalUserController;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequestValidator;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationIdentifierValidatorImpl;
import uk.gov.hmcts.reform.professionalapi.controller.request.ProfessionalUserReqValidator;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;
import uk.gov.hmcts.reform.professionalapi.domain.SuperUser;
import uk.gov.hmcts.reform.professionalapi.service.OrganisationService;
import uk.gov.hmcts.reform.professionalapi.service.ProfessionalUserService;

public class ProfessionalExternalUserControllerTest {

    private OrganisationService organisationServiceMock;
    private ProfessionalUserService professionalUserServiceMock;
    private Organisation organisation;
    private ProfessionalUserReqValidator profExtUsrReqValidator;
    private OrganisationIdentifierValidatorImpl organisationIdentifierValidatorImpl;
    private OrganisationCreationRequestValidator organisationCreationRequestValidator;
    private ResponseEntity<?> responseEntity;

    @InjectMocks
    private ProfessionalExternalUserController professionalExternalUserController;


    @Before
    public void setUp() throws Exception {
        organisation = mock(Organisation.class);
        organisationServiceMock = mock(OrganisationService.class);
        professionalUserServiceMock = mock(ProfessionalUserService.class);
        organisationIdentifierValidatorImpl = mock(OrganisationIdentifierValidatorImpl.class);
        profExtUsrReqValidator = mock(ProfessionalUserReqValidator.class);
        organisationCreationRequestValidator = mock(OrganisationCreationRequestValidator.class);
        responseEntity = mock(ResponseEntity.class);

        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testFindUsersByOrganisation() {
        final HttpStatus expectedHttpStatus = HttpStatus.OK;
        ProfessionalUser professionalUser = new ProfessionalUser("fName", "lastName", "emailAddress", organisation);

        List<SuperUser> users = new ArrayList<>();
        users.add(professionalUser.toSuperUser());
        organisation.setUsers(users);
        organisation.setStatus(OrganisationStatus.ACTIVE);

        Authentication authentication = mock(Authentication.class);
        GrantedAuthority grantedAuthority = mock(GrantedAuthority.class);
        Collection<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
        when(grantedAuthority.getAuthority()).thenReturn("pui-user-manager");
        authorities.add(grantedAuthority);

        ServiceAndUserDetails serviceAndUserDetails = mock(ServiceAndUserDetails.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(securityContext.getAuthentication().getPrincipal()).thenReturn(serviceAndUserDetails);
        when(serviceAndUserDetails.getAuthorities()).thenReturn(authorities);
        SecurityContextHolder.setContext(securityContext);

        when(organisation.getOrganisationIdentifier()).thenReturn(UUID.randomUUID().toString());
        when(organisation.getStatus()).thenReturn(OrganisationStatus.ACTIVE);
        when(organisationServiceMock.getOrganisationByOrgIdentifier(organisation.getOrganisationIdentifier())).thenReturn(organisation);
        when(professionalUserServiceMock.findProfessionalUserProfileByEmailAddress("emailAddress")).thenReturn(professionalUser);
        when(professionalUserServiceMock.findProfessionalUsersByOrganisation(any(Organisation.class), any(String.class), any(Boolean.class), any(String.class))).thenReturn(responseEntity);
        when(organisationIdentifierValidatorImpl.ifUserRoleExists(authorities, "pui-user-manager")).thenReturn(true);
        when(responseEntity.getStatusCode()).thenReturn(HttpStatus.OK);

        doNothing().when(profExtUsrReqValidator).validateRequest(any(String.class), any(String.class), any(String.class));
        doNothing().when(organisationIdentifierValidatorImpl).validate(any(Organisation.class), any(OrganisationStatus.class), any(String.class));
        doNothing().when(organisationCreationRequestValidator).validateOrganisationIdentifier(any(String.class));

        ResponseEntity<?> actual = professionalExternalUserController.findUsersByOrganisation(organisation.getOrganisationIdentifier(), "true", "", null, null);
        assertThat(actual).isNotNull();
        assertThat(actual.getStatusCode().value()).isEqualTo(expectedHttpStatus.value());
    }

    @Test
    public void testFindUsersByOrganisationWithPuiCaseManager() {
        final HttpStatus expectedHttpStatus = HttpStatus.OK;
        ProfessionalUser professionalUser = new ProfessionalUser("fName", "lastName", "emailAddress", organisation);

        List<SuperUser> users = new ArrayList<>();
        users.add(professionalUser.toSuperUser());
        organisation.setUsers(users);
        organisation.setStatus(OrganisationStatus.ACTIVE);

        Authentication authentication = mock(Authentication.class);
        GrantedAuthority grantedAuthority = mock(GrantedAuthority.class);
        Collection<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
        when(grantedAuthority.getAuthority()).thenReturn("pui-user-manager");
        authorities.add(grantedAuthority);

        ServiceAndUserDetails serviceAndUserDetails = mock(ServiceAndUserDetails.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(securityContext.getAuthentication().getPrincipal()).thenReturn(serviceAndUserDetails);
        when(serviceAndUserDetails.getAuthorities()).thenReturn(authorities);
        SecurityContextHolder.setContext(securityContext);

        when(organisation.getOrganisationIdentifier()).thenReturn(UUID.randomUUID().toString());
        when(organisation.getStatus()).thenReturn(OrganisationStatus.ACTIVE);
        when(organisationServiceMock.getOrganisationByOrgIdentifier(organisation.getOrganisationIdentifier())).thenReturn(organisation);
        when(professionalUserServiceMock.findProfessionalUserProfileByEmailAddress("emailAddress")).thenReturn(professionalUser);
        when(professionalUserServiceMock.findProfessionalUsersByOrganisation(any(Organisation.class), any(String.class), any(Boolean.class), any(String.class))).thenReturn(responseEntity);
        when(organisationIdentifierValidatorImpl.ifUserRoleExists(authorities, "pui-case-manager")).thenReturn(true);
        when(responseEntity.getStatusCode()).thenReturn(HttpStatus.OK);

        doNothing().when(profExtUsrReqValidator).validateRequest(any(String.class), any(String.class), any(String.class));
        doNothing().when(organisationIdentifierValidatorImpl).validate(any(Organisation.class), any(OrganisationStatus.class), any(String.class));
        doNothing().when(organisationCreationRequestValidator).validateOrganisationIdentifier(any(String.class));

        ResponseEntity<?> actual = professionalExternalUserController.findUsersByOrganisation(organisation.getOrganisationIdentifier(), "true", "Pending", null, null);
        assertThat(actual).isNotNull();
        assertThat(actual.getStatusCode().value()).isEqualTo(expectedHttpStatus.value());
    }
}
