package uk.gov.hmcts.reform.professionalapi.controller;

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
import uk.gov.hmcts.reform.professionalapi.TestConstants;
import uk.gov.hmcts.reform.professionalapi.controller.internal.ProfessionalUserInternalController;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequestValidator;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationIdentifierValidatorImpl;
import uk.gov.hmcts.reform.professionalapi.controller.request.ProfessionalUserReqValidator;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;
import uk.gov.hmcts.reform.professionalapi.domain.SuperUser;
import uk.gov.hmcts.reform.professionalapi.service.OrganisationService;
import uk.gov.hmcts.reform.professionalapi.service.ProfessionalUserService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.doNothing;

public class ProfessionalUserInternalControllerTest {

    private OrganisationService organisationServiceMock;
    private ProfessionalUserService professionalUserServiceMock;
    private Organisation organisation;
    private ProfessionalUserReqValidator profExtUsrReqValidator;
    private OrganisationIdentifierValidatorImpl organisationIdentifierValidatorImpl;
    private OrganisationCreationRequestValidator organisationCreationRequestValidator;
    private ResponseEntity<?> responseEntity;

    @InjectMocks
    private ProfessionalUserInternalController professionalUserInternalController;


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

        when(organisation.getOrganisationIdentifier()).thenReturn(UUID.randomUUID().toString());
        when(organisation.getStatus()).thenReturn(OrganisationStatus.ACTIVE);
        when(organisationServiceMock.getOrganisationByOrgIdentifier(organisation.getOrganisationIdentifier())).thenReturn(organisation);
        when(professionalUserServiceMock.findProfessionalUserProfileByEmailAddress("emailAddress")).thenReturn(professionalUser);
        when(professionalUserServiceMock.findProfessionalUsersByOrganisation(any(Organisation.class), any(String.class), any(Boolean.class), any(String.class))).thenReturn(responseEntity);
        when(responseEntity.getStatusCode()).thenReturn(HttpStatus.OK);

        doNothing().when(organisationIdentifierValidatorImpl).validate(any(Organisation.class), any(OrganisationStatus.class), any(String.class));
        doNothing().when(organisationCreationRequestValidator).validateOrganisationIdentifier(any(String.class));

        ResponseEntity<?> actual = professionalUserInternalController.findUsersByOrganisation(organisation.getOrganisationIdentifier(), "true", null, null);
        assertThat(actual).isNotNull();
        assertThat(actual.getStatusCode().value()).isEqualTo(expectedHttpStatus.value());
    }
}
