package uk.gov.hmcts.reform.professionalapi.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.professionalapi.controller.external.OrganisationExternalController;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequestValidator;
import uk.gov.hmcts.reform.professionalapi.controller.request.UpdateOrganisationRequestValidator;
import uk.gov.hmcts.reform.professionalapi.controller.response.OrganisationResponse;
import uk.gov.hmcts.reform.professionalapi.service.OrganisationService;
import uk.gov.hmcts.reform.professionalapi.service.PaymentAccountService;
import uk.gov.hmcts.reform.professionalapi.service.ProfessionalUserService;
import uk.gov.hmcts.reform.professionalapi.service.UserAttributeService;
import uk.gov.hmcts.reform.professionalapi.service.impl.PrdEnumServiceImpl;


public class OrganisationControllerTest {
    private OrganisationResponse organisationResponseMock;
    private OrganisationService organisationServiceMock;
    private ProfessionalUserService professionalUserServiceMock;
    private UserAttributeService userAttributeServiceMock;
    private PaymentAccountService paymentAccountServiceMock;
    private PrdEnumServiceImpl prdEnumServiceMock;

    private OrganisationCreationRequest organisationCreationRequestMock;

    private UpdateOrganisationRequestValidator updateOrganisationRequestValidatorMock;
    private OrganisationCreationRequestValidator organisationCreationRequestValidatorMock;

    private ResponseEntity responseEntity;

    @InjectMocks
    private OrganisationExternalController organisationController;


    @Before
    public void setUp() throws Exception {
        organisationResponseMock = mock(OrganisationResponse.class);
        organisationServiceMock = mock(OrganisationService.class);
        professionalUserServiceMock = mock(ProfessionalUserService.class);
        userAttributeServiceMock = mock(UserAttributeService.class);
        paymentAccountServiceMock = mock(PaymentAccountService.class);
        prdEnumServiceMock = mock(PrdEnumServiceImpl.class);

        organisationCreationRequestMock = mock(OrganisationCreationRequest.class);

        updateOrganisationRequestValidatorMock = mock(UpdateOrganisationRequestValidator.class);
        organisationCreationRequestValidatorMock = mock(OrganisationCreationRequestValidator.class);

        responseEntity = mock(ResponseEntity.class);

        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testCreateOrganisation() {
        final HttpStatus expectedHttpStatus = HttpStatus.CREATED;

        when(organisationServiceMock.createOrganisationFrom(organisationCreationRequestMock)).thenReturn(organisationResponseMock);

        ResponseEntity<?> actual = organisationController.createOrganisationUsingExternalController(organisationCreationRequestMock);

        verify(organisationCreationRequestValidatorMock,
                times(1))
                .validate(any(OrganisationCreationRequest.class));

        assertThat(actual).isNotNull();
        assertThat(actual.getStatusCode()).isEqualTo(expectedHttpStatus);

        verify(organisationServiceMock,
                times(1))
                .createOrganisationFrom(eq(organisationCreationRequestMock));
    }
}