package uk.gov.hmcts.reform.professionalapi.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import uk.gov.hmcts.reform.professionalapi.controller.internal.OrganisationInternalController;
import uk.gov.hmcts.reform.professionalapi.controller.request.InvalidRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequestValidator;
import uk.gov.hmcts.reform.professionalapi.controller.response.OrganisationEntityResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.OrganisationResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.OrganisationsDetailResponse;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus;
import uk.gov.hmcts.reform.professionalapi.domain.PaymentAccount;
import uk.gov.hmcts.reform.professionalapi.service.OrganisationService;
import uk.gov.hmcts.reform.professionalapi.service.PaymentAccountService;



public class OrganisationInternalControllerTest {
    private OrganisationResponse organisationResponseMock;
    private OrganisationsDetailResponse organisationsDetailResponseMock;
    private OrganisationEntityResponse organisationEntityResponseMock;
    private OrganisationService organisationServiceMock;
    private PaymentAccountService paymentAccountServiceMock;
    private Organisation organisationMock;
    private OrganisationCreationRequest organisationCreationRequestMock;
    private OrganisationCreationRequestValidator organisationCreationRequestValidatorMock;

    @InjectMocks
    private OrganisationInternalController organisationInternalController;

    @Before
    public void setUp() throws Exception {
        organisationResponseMock = mock(OrganisationResponse.class);
        organisationServiceMock = mock(OrganisationService.class);
        paymentAccountServiceMock = mock(PaymentAccountService.class);
        organisationMock = mock(Organisation.class);
        organisationsDetailResponseMock = mock(OrganisationsDetailResponse.class);
        organisationEntityResponseMock = mock(OrganisationEntityResponse.class);
        organisationCreationRequestMock = mock(OrganisationCreationRequest.class);
        organisationCreationRequestValidatorMock = mock(OrganisationCreationRequestValidator.class);

        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testCreateOrganisation() {
        final HttpStatus expectedHttpStatus = HttpStatus.CREATED;

        when(organisationServiceMock.createOrganisationFrom(organisationCreationRequestMock)).thenReturn(organisationResponseMock);

        ResponseEntity<?> actual = organisationInternalController.createOrganisation(organisationCreationRequestMock);

        verify(organisationCreationRequestValidatorMock,
                times(1))
                .validate(any(OrganisationCreationRequest.class));

        verify(organisationServiceMock,
                times(1))
                .createOrganisationFrom(eq(organisationCreationRequestMock));

        assertThat(actual).isNotNull();
        assertThat(actual.getStatusCode()).isEqualTo(expectedHttpStatus);
    }

    @Test
    public void testRetrieveOrganisations() {
        final HttpStatus expectedHttpStatus = HttpStatus.OK;

        when(organisationServiceMock.retrieveOrganisations()).thenReturn(organisationsDetailResponseMock);

        ResponseEntity<?> actual = organisationInternalController.retrieveOrganisations(null, null);

        assertThat(actual).isNotNull();
        assertThat(actual.getStatusCode()).isEqualTo(expectedHttpStatus);
    }

    @Test
    public void testRetrieveOrganisationByIdWithStatusNull() {
        final HttpStatus expectedHttpStatus = HttpStatus.OK;

        when(organisationServiceMock.retrieveOrganisation(any(String.class))).thenReturn(organisationEntityResponseMock);

        ResponseEntity<?> actual = organisationInternalController.retrieveOrganisations(organisationMock.getOrganisationIdentifier(), null);

        assertThat(actual).isNotNull();
        assertThat(actual.getStatusCode()).isEqualTo(expectedHttpStatus);
    }

    @Test
    public void testRetrieveOrganisationByIdWithStatusNotNull() {
        final HttpStatus expectedHttpStatus = HttpStatus.OK;

        when(organisationServiceMock.retrieveOrganisation(any(String.class))).thenReturn(organisationEntityResponseMock);

        ResponseEntity<?> actual = organisationInternalController.retrieveOrganisations(organisationMock.getOrganisationIdentifier(), "PENDING");

        assertThat(actual).isNotNull();
        assertThat(actual.getStatusCode()).isEqualTo(expectedHttpStatus);
    }

    @Test
    public void testRetrieveOrganisationByStatusWithIdNull() {
        final HttpStatus expectedHttpStatus = HttpStatus.OK;

        when(organisationServiceMock.findByOrganisationStatus(any(OrganisationStatus.class))).thenReturn(organisationsDetailResponseMock);

        ResponseEntity<?> actual = organisationInternalController.retrieveOrganisations(null, "PENDING");

        assertThat(actual).isNotNull();
        assertThat(actual.getStatusCode()).isEqualTo(expectedHttpStatus);
    }

    @Test(expected = InvalidRequest.class)
    public void testRetrieveOrganisationThrows400WhenStatusInvalid() {
        when(organisationServiceMock.findByOrganisationStatus(any(OrganisationStatus.class))).thenReturn(organisationsDetailResponseMock);

        organisationInternalController.retrieveOrganisations(null, "this is not a status");
    }

    @Test
    public void testRetrievePaymentAccountByEmail() {
        final HttpStatus expectedHttpStatus = HttpStatus.OK;

        final List<PaymentAccount> paymentAccounts = new ArrayList<>();
        paymentAccounts.add(new PaymentAccount());

        when(organisationMock.getPaymentAccounts()).thenReturn(paymentAccounts);

        when(paymentAccountServiceMock.findPaymentAccountsByEmail("some-email")).thenReturn(organisationMock);

        ResponseEntity<?> actual = organisationInternalController.retrievePaymentAccountBySuperUserEmail("some-email");

        assertThat(actual).isNotNull();
        assertThat(actual.getStatusCode()).isEqualTo(expectedHttpStatus);
    }

    @Test(expected = EmptyResultDataAccessException.class)
    public void testRetrievePaymentAccountByEmailThrows404WhenNoAccFound() {
        organisationInternalController.retrievePaymentAccountBySuperUserEmail("some-email");
    }
}