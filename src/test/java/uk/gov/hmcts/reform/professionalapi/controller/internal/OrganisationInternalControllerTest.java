package uk.gov.hmcts.reform.professionalapi.controller.internal;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import uk.gov.hmcts.reform.professionalapi.controller.request.InvalidRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequestValidator;
import uk.gov.hmcts.reform.professionalapi.controller.request.PaymentAccountValidator;
import uk.gov.hmcts.reform.professionalapi.controller.request.PbaEditRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.UserCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.response.OrganisationEntityResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.OrganisationResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.OrganisationsDetailResponse;
import uk.gov.hmcts.reform.professionalapi.domain.Jurisdiction;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus;
import uk.gov.hmcts.reform.professionalapi.domain.PaymentAccount;
import uk.gov.hmcts.reform.professionalapi.domain.PrdEnum;
import uk.gov.hmcts.reform.professionalapi.domain.PrdEnumId;
import uk.gov.hmcts.reform.professionalapi.persistence.PrdEnumRepository;
import uk.gov.hmcts.reform.professionalapi.service.OrganisationService;
import uk.gov.hmcts.reform.professionalapi.service.PaymentAccountService;
import uk.gov.hmcts.reform.professionalapi.service.impl.PrdEnumServiceImpl;

public class OrganisationInternalControllerTest {
    private OrganisationResponse organisationResponse;
    private OrganisationsDetailResponse organisationsDetailResponse;
    private OrganisationEntityResponse organisationEntityResponse;
    private OrganisationService organisationServiceMock;
    private PaymentAccountService paymentAccountServiceMock;
    private Organisation organisation;
    private OrganisationCreationRequest organisationCreationRequest;
    private OrganisationCreationRequestValidator organisationCreationRequestValidatorMock;
    private PaymentAccountValidator paymentAccountValidatorMock;

    private PrdEnumRepository prdEnumRepository;
    private final PrdEnumId prdEnumId1 = new PrdEnumId(10, "JURISD_ID");
    private final PrdEnumId prdEnumId2 = new PrdEnumId(13, "JURISD_ID");
    private final PrdEnumId prdEnumId3 = new PrdEnumId(3, "PRD_ROLE");

    private final PrdEnum anEnum1 = new PrdEnum(prdEnumId1, "PROBATE", "JURISD_ID");
    private final PrdEnum anEnum2 = new PrdEnum(prdEnumId2, "BULKSCAN", "JURISD_ID");
    private final PrdEnum anEnum3 = new PrdEnum(prdEnumId3, "pui-case-manager", "PRD_ROLE");
    private UserCreationRequest userCreationRequest;
    private PrdEnumServiceImpl prdEnumServiceMock;
    private List<PrdEnum> prdEnumList;
    private List<String> jurisdEnumIds;
    private List<Jurisdiction> jurisdictions;

    @InjectMocks
    private OrganisationInternalController organisationInternalController;

    @Before
    public void setUp() throws Exception {
        organisation = new Organisation("Org-Name", OrganisationStatus.PENDING, "sra-id", "companyN", false, "www.org.com");
        organisationResponse = new OrganisationResponse(organisation);
        organisationsDetailResponse = new OrganisationsDetailResponse(singletonList(organisation), false);
        organisationEntityResponse = new OrganisationEntityResponse(organisation, false);

        organisationServiceMock = mock(OrganisationService.class);
        paymentAccountServiceMock = mock(PaymentAccountService.class);
        organisationCreationRequestValidatorMock = mock(OrganisationCreationRequestValidator.class);
        paymentAccountValidatorMock = mock(PaymentAccountValidator.class);
        prdEnumServiceMock = mock(PrdEnumServiceImpl.class);
        prdEnumRepository = mock(PrdEnumRepository.class);

        prdEnumList = new ArrayList<>();
        prdEnumList.add(anEnum1);
        prdEnumList.add(anEnum2);
        prdEnumList.add(anEnum3);

        jurisdEnumIds = new ArrayList<>();
        jurisdEnumIds.add("Probate");
        jurisdEnumIds.add("Bulk Scanning");
        jurisdictions = new ArrayList<>();
        Jurisdiction jurisdiction1 = new Jurisdiction();
        jurisdiction1.setId("Probate");
        Jurisdiction jurisdiction2 = new Jurisdiction();
        jurisdiction2.setId("Bulk Scanning");
        jurisdictions.add(jurisdiction1);
        jurisdictions.add(jurisdiction2);

        userCreationRequest = new UserCreationRequest("some-fname", "some-lname", "some@email.com", jurisdictions);
        organisationCreationRequest = new OrganisationCreationRequest("test", "PENDING", "sra-id", "false", "number02", "company-url", userCreationRequest, null, null);

        organisation.setOrganisationIdentifier("AK57L4T");

        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testCreateOrganisation() {
        final HttpStatus expectedHttpStatus = HttpStatus.CREATED;

        when(organisationServiceMock.createOrganisationFrom(organisationCreationRequest)).thenReturn(organisationResponse);
        when(prdEnumServiceMock.getPrdEnumByEnumType(any())).thenReturn(jurisdEnumIds);
        when(prdEnumRepository.findAll()).thenReturn(prdEnumList);

        ResponseEntity<?> actual = organisationInternalController.createOrganisation(organisationCreationRequest);

        verify(organisationCreationRequestValidatorMock, times(1)).validate(any(OrganisationCreationRequest.class));
        verify(organisationServiceMock, times(1)).createOrganisationFrom(any(OrganisationCreationRequest.class));

        assertThat(actual).isNotNull();
        assertThat(actual.getStatusCode()).isEqualTo(expectedHttpStatus);
    }

    @Test
    public void testRetrieveOrganisations() {
        final HttpStatus expectedHttpStatus = HttpStatus.OK;

        when(organisationServiceMock.retrieveAllOrganisations()).thenReturn(organisationsDetailResponse);

        ResponseEntity<?> actual = organisationInternalController.retrieveOrganisations(null, null);

        assertThat(actual).isNotNull();
        assertThat(actual.getStatusCode()).isEqualTo(expectedHttpStatus);
    }

    @Test
    public void testRetrieveOrganisationByIdWithStatusNull() {
        final HttpStatus expectedHttpStatus = HttpStatus.OK;

        when(organisationServiceMock.retrieveOrganisation(any(String.class))).thenReturn(organisationEntityResponse);

        ResponseEntity<?> actual = organisationInternalController.retrieveOrganisations(organisation.getOrganisationIdentifier(), null);

        assertThat(actual).isNotNull();
        assertThat(actual.getStatusCode()).isEqualTo(expectedHttpStatus);
    }

    @Test
    public void testRetrieveOrganisationByIdWithStatusNotNull() {
        final HttpStatus expectedHttpStatus = HttpStatus.OK;

        when(organisationServiceMock.retrieveOrganisation(any(String.class))).thenReturn(organisationEntityResponse);

        ResponseEntity<?> actual = organisationInternalController.retrieveOrganisations(organisation.getOrganisationIdentifier(), "PENDING");

        assertThat(actual).isNotNull();
        assertThat(actual.getStatusCode()).isEqualTo(expectedHttpStatus);
    }

    @Test
    public void testRetrieveOrganisationByStatusWithIdNull() {
        final HttpStatus expectedHttpStatus = HttpStatus.OK;

        when(organisationServiceMock.findByOrganisationStatus(any(OrganisationStatus.class))).thenReturn(organisationsDetailResponse);

        ResponseEntity<?> actual = organisationInternalController.retrieveOrganisations(null, "PENDING");

        assertThat(actual).isNotNull();
        assertThat(actual.getStatusCode()).isEqualTo(expectedHttpStatus);
    }

    @Test(expected = InvalidRequest.class)
    public void testRetrieveOrganisationThrows400WhenStatusInvalid() {
        when(organisationServiceMock.findByOrganisationStatus(any(OrganisationStatus.class))).thenReturn(organisationsDetailResponse);

        organisationInternalController.retrieveOrganisations(null, "this is not a status");
    }

    @Test
    public void testRetrievePaymentAccountByEmail() {
        final HttpStatus expectedHttpStatus = HttpStatus.OK;

        final List<PaymentAccount> paymentAccounts = new ArrayList<>();
        paymentAccounts.add(new PaymentAccount());

        organisation.setPaymentAccounts(paymentAccounts);

        when(paymentAccountServiceMock.findPaymentAccountsByEmail("some-email@test.com")).thenReturn(organisation);

        ResponseEntity<?> actual = organisationInternalController.retrievePaymentAccountBySuperUserEmail("some-email@test.com");

        assertThat(actual).isNotNull();
        assertThat(actual.getStatusCode()).isEqualTo(expectedHttpStatus);
    }

    @Test(expected = EmptyResultDataAccessException.class)
    public void testRetrievePaymentAccountByEmailThrows404WhenNoAccFound() {
        organisationInternalController.retrievePaymentAccountBySuperUserEmail("some-email@test.com");
    }

    @Test(expected = InvalidRequest.class)
    public void testRetrievePaymentAccountByEmailThrows400WhenEmailIsInvalid() {
        organisationInternalController.retrievePaymentAccountBySuperUserEmail("some-email");
    }

    @Test
    public void testEditPaymentAccountsByOrgId() {
        final HttpStatus expectedHttpStatus = HttpStatus.OK;

        Set<String> pbas = new HashSet<>();
        pbas.add("PBA0000001");
        PbaEditRequest pbaEditRequest = new PbaEditRequest(pbas);

        when(organisationServiceMock.getOrganisationByOrgIdentifier(organisation.getOrganisationIdentifier())).thenReturn(organisation);

        ResponseEntity response = organisationInternalController.editPaymentAccountsByOrgId(pbaEditRequest, organisation.getOrganisationIdentifier());

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(expectedHttpStatus);
    }
}