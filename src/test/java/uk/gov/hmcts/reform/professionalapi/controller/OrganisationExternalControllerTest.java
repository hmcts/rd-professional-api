package uk.gov.hmcts.reform.professionalapi.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.professionalapi.controller.external.OrganisationExternalController;
import uk.gov.hmcts.reform.professionalapi.controller.request.NewUserCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequestValidator;
import uk.gov.hmcts.reform.professionalapi.controller.request.UpdateOrganisationRequestValidator;
import uk.gov.hmcts.reform.professionalapi.controller.request.UserCreationRequestValidator;
import uk.gov.hmcts.reform.professionalapi.controller.response.OrganisationEntityResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.OrganisationResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.OrganisationsDetailResponse;

import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus;
import uk.gov.hmcts.reform.professionalapi.domain.PrdEnum;
import uk.gov.hmcts.reform.professionalapi.domain.PrdEnumId;

import uk.gov.hmcts.reform.professionalapi.service.OrganisationService;
import uk.gov.hmcts.reform.professionalapi.service.PaymentAccountService;
import uk.gov.hmcts.reform.professionalapi.service.ProfessionalUserService;
import uk.gov.hmcts.reform.professionalapi.service.UserAttributeService;
import uk.gov.hmcts.reform.professionalapi.service.impl.PrdEnumServiceImpl;


public class OrganisationExternalControllerTest {

    private OrganisationResponse organisationResponseMock;
    private OrganisationsDetailResponse organisationsDetailResponseMock;
    private OrganisationEntityResponse organisationEntityResponseMock;
    private OrganisationService organisationServiceMock;
    private ProfessionalUserService professionalUserServiceMock;
    private UserAttributeService userAttributeServiceMock;
    private PaymentAccountService paymentAccountServiceMock;
    private PrdEnumServiceImpl prdEnumServiceMock;
    private Organisation organisationMock;

    private OrganisationCreationRequest organisationCreationRequestMock;
    private NewUserCreationRequest newUserCreationRequestMock;

    private UpdateOrganisationRequestValidator updateOrganisationRequestValidatorMock;
    private OrganisationCreationRequestValidator organisationCreationRequestValidatorMock;

    private UserCreationRequestValidator userCreationRequestValidatorMock;

    private ResponseEntity responseEntity;

    @InjectMocks
    private OrganisationExternalController organisationExternalController;


    @Before
    public void setUp() throws Exception {
        organisationResponseMock = mock(OrganisationResponse.class);
        organisationServiceMock = mock(OrganisationService.class);
        professionalUserServiceMock = mock(ProfessionalUserService.class);
        userAttributeServiceMock = mock(UserAttributeService.class);
        paymentAccountServiceMock = mock(PaymentAccountService.class);
        prdEnumServiceMock = mock(PrdEnumServiceImpl.class);
        organisationCreationRequestMock = mock(OrganisationCreationRequest.class);
        organisationMock = mock(Organisation.class);
        organisationsDetailResponseMock = mock(OrganisationsDetailResponse.class);
        organisationEntityResponseMock = mock(OrganisationEntityResponse.class);
        organisationCreationRequestMock = mock(OrganisationCreationRequest.class);
        updateOrganisationRequestValidatorMock = mock(UpdateOrganisationRequestValidator.class);
        organisationCreationRequestValidatorMock = mock(OrganisationCreationRequestValidator.class);
        userCreationRequestValidatorMock = mock(UserCreationRequestValidator.class);
        newUserCreationRequestMock = mock(NewUserCreationRequest.class);
        updateOrganisationRequestValidatorMock = mock(UpdateOrganisationRequestValidator.class);
        organisationCreationRequestValidatorMock = mock(OrganisationCreationRequestValidator.class);
        responseEntity = mock(ResponseEntity.class);
        MockitoAnnotations.initMocks(this);

    }

    @Test
    public void testCreateOrganisation() {
        final HttpStatus expectedHttpStatus = HttpStatus.CREATED;

        when(organisationServiceMock.createOrganisationFrom(organisationCreationRequestMock)).thenReturn(organisationResponseMock);

        ResponseEntity<?> actual = organisationExternalController.createOrganisationUsingExternalController(organisationCreationRequestMock);

        verify(organisationCreationRequestValidatorMock,
                times(1))
                .validate(any(OrganisationCreationRequest.class));

        assertThat(actual).isNotNull();
        assertThat(actual.getStatusCode()).isEqualTo(expectedHttpStatus);

        verify(organisationServiceMock,
                times(1))
                .createOrganisationFrom(eq(organisationCreationRequestMock));
    }


    @Test
    public void testUpdatesOrganisation() {
        final HttpStatus expectedHttpStatus = HttpStatus.OK;

        doNothing().when(organisationCreationRequestValidatorMock).validate(any(OrganisationCreationRequest.class));
        doNothing().when(organisationCreationRequestValidatorMock).validateOrganisationIdentifier(any(String.class));
        doNothing().when(updateOrganisationRequestValidatorMock).validateStatus(any(Organisation.class), any(OrganisationStatus.class), any(String.class));

        when(organisationServiceMock.getOrganisationByOrgIdentifier(organisationMock.getOrganisationIdentifier())).thenReturn(organisationMock);

        ResponseEntity<?> actual = organisationExternalController.updatesOrganisationUsingExternalController(organisationCreationRequestMock, organisationMock.getOrganisationIdentifier());

        assertThat(actual).isNotNull();
        assertThat(actual.getStatusCode()).isEqualTo(expectedHttpStatus);
    }

    @Test
    public void testAddUserToOrganisation() {
        final HttpStatus expectedHttpStatus = HttpStatus.CREATED;

        PrdEnumId prdEnumId = new PrdEnumId(5, "ADMIN_ROLE");
        PrdEnum prdEnumMock = new PrdEnum(prdEnumId, "organisation-admin", "enum-desc");
        List<PrdEnum> prdEnumList = new ArrayList<PrdEnum>(){};
        prdEnumList.add(prdEnumMock);

        List<String> roles = new ArrayList<String>();
        roles.add("organisation-admin");

        doNothing().when(organisationCreationRequestValidatorMock).validateOrganisationIdentifier(any(String.class));
        doNothing().when(updateOrganisationRequestValidatorMock).validateStatus(any(Organisation.class), any(OrganisationStatus.class), any(String.class));
        when(newUserCreationRequestMock.getRoles()).thenReturn(roles);
        when(prdEnumServiceMock.findAllPrdEnums()).thenReturn(prdEnumList);
        when(organisationServiceMock.getOrganisationByOrgIdentifier(organisationMock.getOrganisationIdentifier())).thenReturn(organisationMock);

        ResponseEntity<?> actual = organisationExternalController.addUserToOrganisationUsingExternalController(newUserCreationRequestMock, organisationMock.getOrganisationIdentifier());

        assertThat(actual).isNotNull();
        assertThat(actual.getStatusCode()).isEqualTo(expectedHttpStatus);
    }

}