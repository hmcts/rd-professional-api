package uk.gov.hmcts.reform.professionalapi.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;

import uk.gov.hmcts.reform.professionalapi.controller.external.OrganisationExternalController;
import uk.gov.hmcts.reform.professionalapi.controller.feign.UserProfileFeignClient;
import uk.gov.hmcts.reform.professionalapi.controller.request.NewUserCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequestValidator;
import uk.gov.hmcts.reform.professionalapi.controller.request.UpdateOrganisationRequestValidator;
import uk.gov.hmcts.reform.professionalapi.controller.request.UserCreationRequestValidator;
import uk.gov.hmcts.reform.professionalapi.controller.response.OrganisationEntityResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.OrganisationResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.OrganisationsDetailResponse;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
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

    private final UserProfileFeignClient userProfileFeignClient = mock(UserProfileFeignClient.class);


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
    public void testRetrieveOrganisationByIdentifier() {

        final HttpStatus expectedHttpStatus = HttpStatus.OK;

        String id = UUID.randomUUID().toString().substring(0,7);
        when(organisationServiceMock.retrieveOrganisation(id)).thenReturn(organisationEntityResponseMock);

        ResponseEntity<?> actual = organisationExternalController.retrieveOrganisationUsingOrgIdentifier(id, id);

        verify(organisationCreationRequestValidatorMock,
                times(1))
                .validateOrganisationIdentifier(id);

        assertThat(actual).isNotNull();
        assertThat(actual.getStatusCode()).isEqualTo(expectedHttpStatus);

        verify(organisationServiceMock,
                times(1))
                .retrieveOrganisation(eq(id));
    }


    @Test(expected = AccessDeniedException.class)
    public void testRetrieveOrganisationByIdentifierNotMatchesThrowException() {

        String id = UUID.randomUUID().toString().substring(0,7);
        when(organisationServiceMock.retrieveOrganisation(id)).thenReturn(organisationEntityResponseMock);

        ResponseEntity<?> actual = organisationExternalController.retrieveOrganisationUsingOrgIdentifier(id, UUID.randomUUID().toString());

        verify(organisationCreationRequestValidatorMock,
                times(1))
                .validateOrganisationIdentifier(id);

    }

}