package uk.gov.hmcts.reform.professionalapi.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.professionalapi.controller.external.ProfessionalExternalUserController;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequestValidator;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationIdentifierValidatorImpl;
import uk.gov.hmcts.reform.professionalapi.controller.request.ProfessionalUserReqValidator;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;
import uk.gov.hmcts.reform.professionalapi.service.OrganisationService;
import uk.gov.hmcts.reform.professionalapi.service.ProfessionalUserService;

public class ProfessionalExternalUserControllerTest {

    private OrganisationService organisationServiceMock;
    private ProfessionalUserService professionalUserServiceMock;
    private Organisation organisation;
    private ProfessionalUserReqValidator profExtUsrReqValidator;
    private OrganisationIdentifierValidatorImpl organisationIdentifierValidatorImpl;
    private OrganisationCreationRequestValidator organisationCreationRequestValidator;

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
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testFindUsersByOrganisation() {
        final HttpStatus expectedHttpStatus = HttpStatus.OK;

        ProfessionalUser superUser = new ProfessionalUser("fName", "lastName", "emailAddress", organisation);

        List<ProfessionalUser> users = new ArrayList<>();
        users.add(superUser);
        organisation.setUsers(users);
        organisation.setStatus(OrganisationStatus.ACTIVE);

//        when(professionalUserServiceMock.findProfessionalUsersByOrganisation(organisation, "false")).thenReturn(users);
        when(organisationServiceMock.getOrganisationByOrgIdentifier(organisation.getOrganisationIdentifier())).thenReturn(organisation);
        when(organisation.getStatus()).thenReturn(OrganisationStatus.ACTIVE);

        doNothing().when(profExtUsrReqValidator).validateRequest(any(String.class), any(String.class), any(String.class));
        doNothing().when(organisationIdentifierValidatorImpl).validate(any(Organisation.class), any(OrganisationStatus.class), any(String.class));
        doNothing().when(organisationCreationRequestValidator).validateOrganisationIdentifier(any(String.class));

        ResponseEntity<?> actual = professionalExternalUserController.findUsersByOrganisation(organisation.getOrganisationIdentifier(), "false", "");
        assertThat(actual).isNotNull();
        assertThat(actual.getStatusCode()).isEqualTo(expectedHttpStatus);
    }
}
