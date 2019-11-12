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
import java.util.Map;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import uk.gov.hmcts.reform.professionalapi.controller.internal.OrganisationInternalController;
import uk.gov.hmcts.reform.professionalapi.controller.request.InvalidRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.Jurisdiction;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequestValidator;
import uk.gov.hmcts.reform.professionalapi.controller.request.UserCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.response.OrganisationEntityResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.OrganisationResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.OrganisationsDetailResponse;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus;
import uk.gov.hmcts.reform.professionalapi.domain.PaymentAccount;
import uk.gov.hmcts.reform.professionalapi.domain.PrdEnum;
import uk.gov.hmcts.reform.professionalapi.domain.PrdEnumId;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;
import uk.gov.hmcts.reform.professionalapi.domain.UserAttribute;
import uk.gov.hmcts.reform.professionalapi.persistence.PrdEnumRepository;
import uk.gov.hmcts.reform.professionalapi.service.OrganisationService;
import uk.gov.hmcts.reform.professionalapi.service.PaymentAccountService;
import uk.gov.hmcts.reform.professionalapi.service.impl.PrdEnumServiceImpl;
import uk.gov.hmcts.reform.professionalapi.util.RefDataUtil;

public class OrganisationInternalControllerTest {
    private OrganisationResponse organisationResponseMock;
    private OrganisationsDetailResponse organisationsDetailResponseMock;
    private OrganisationEntityResponse organisationEntityResponseMock;
    private OrganisationService organisationServiceMock;
    private PaymentAccountService paymentAccountServiceMock;
    private Organisation organisationMock;
    private OrganisationCreationRequest organisationCreationRequestMock;
    private OrganisationCreationRequestValidator organisationCreationRequestValidatorMock;
    private RefDataUtil refDataUtilMock;

    private PrdEnumRepository prdEnumRepository;
    private final PrdEnumId prdEnumId1 = new PrdEnumId(10, "JURISD_ID");
    private final PrdEnumId prdEnumId2 = new PrdEnumId(13, "JURISD_ID");
    private final PrdEnumId prdEnumId3 = new PrdEnumId(3, "PRD_ROLE");

    private final ProfessionalUser user = Mockito.mock(ProfessionalUser.class);
    private final List<UserAttribute> attributeList = new ArrayList<>();
    private final PrdEnum anEnum1 = new PrdEnum(prdEnumId1, "PROBATE", "JURISD_ID");
    private final PrdEnum anEnum2 = new PrdEnum(prdEnumId2, "BULKSCAN", "JURISD_ID");
    private final PrdEnum anEnum3 = new PrdEnum(prdEnumId3, "pui-case-manager", "PRD_ROLE");
    private UserCreationRequest userCreationRequestMock;
    private ResponseEntity responseEntity;
    private PrdEnumServiceImpl prdEnumServiceMock;
    private List<PrdEnum> prdEnumList;
    private List<String> jurisdEnumIds;
    private List<Jurisdiction> jurisdictions;
    private Map<String,String> jid1;
    private Map<String,String> jid2;

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
        refDataUtilMock = mock(RefDataUtil.class);
        prdEnumServiceMock = mock(PrdEnumServiceImpl.class);
        prdEnumRepository = mock(PrdEnumRepository.class);
        responseEntity = mock(ResponseEntity.class);
        prdEnumList = new ArrayList<PrdEnum>();
        prdEnumList.add(anEnum1);
        prdEnumList.add(anEnum2);
        prdEnumList.add(anEnum3);

        jurisdEnumIds = new ArrayList<String>();
        jurisdEnumIds.add("Probate");
        jurisdEnumIds.add("Bulk Scanning");
        jurisdictions = new ArrayList<Jurisdiction>();
        Jurisdiction jurisdiction1 = new Jurisdiction();
        jurisdiction1.setId("Probate");
        Jurisdiction jurisdiction2 = new Jurisdiction();
        jurisdiction2.setId("Bulk Scanning");
        jurisdictions.add(jurisdiction1);
        jurisdictions.add(jurisdiction2);
        userCreationRequestMock = mock(UserCreationRequest.class);

        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testCreateOrganisation() {
        final HttpStatus expectedHttpStatus = HttpStatus.CREATED;

        when(organisationCreationRequestMock.getSuperUser()).thenReturn(userCreationRequestMock);
        when(userCreationRequestMock.getJurisdictions()).thenReturn(jurisdictions);

        when(organisationServiceMock.createOrganisationFrom(organisationCreationRequestMock)).thenReturn(organisationResponseMock);

        when(prdEnumServiceMock.getPrdEnumByEnumType(any())).thenReturn(jurisdEnumIds);
        when(prdEnumRepository.findAll()).thenReturn(prdEnumList);

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

        ResponseEntity<?> actual = organisationInternalController.retrieveOrganisations(null, null, null, null);
        Mockito.verify(organisationServiceMock, Mockito.times(1))
                .retrieveOrganisations();
        assertThat(actual).isNotNull();
        assertThat(actual.getStatusCode()).isEqualTo(expectedHttpStatus);
    }

    @Test
    public void testRetrieveOrganisationByIdWithStatusNull() {
        final HttpStatus expectedHttpStatus = HttpStatus.OK;

        when(organisationServiceMock.retrieveOrganisation(any(String.class))).thenReturn(organisationEntityResponseMock);
        when(organisationMock.getOrganisationIdentifier()).thenReturn(UUID.randomUUID().toString());
        ResponseEntity<?> actual = organisationInternalController.retrieveOrganisations(organisationMock.getOrganisationIdentifier(), null, null, null);
        Mockito.verify(organisationServiceMock, Mockito.times(1))
                .retrieveOrganisation(any(String.class));
        assertThat(actual).isNotNull();
        assertThat(actual.getStatusCode()).isEqualTo(expectedHttpStatus);
    }

    @Test
    public void testRetrieveOrganisationByIdWithStatusNotNull() {
        final HttpStatus expectedHttpStatus = HttpStatus.OK;

        when(organisationServiceMock.retrieveOrganisation(any(String.class))).thenReturn(organisationEntityResponseMock);
        when(organisationMock.getOrganisationIdentifier()).thenReturn(UUID.randomUUID().toString());
        ResponseEntity<?> actual = organisationInternalController.retrieveOrganisations(organisationMock.getOrganisationIdentifier(), "PENDING",null, null);
        Mockito.verify(organisationServiceMock, Mockito.times(1))
                .retrieveOrganisation(any(String.class));
        assertThat(actual).isNotNull();
        assertThat(actual.getStatusCode()).isEqualTo(expectedHttpStatus);
    }

    @Test
    public void testRetrieveOrganisationByStatusWithIdNull() {
        final HttpStatus expectedHttpStatus = HttpStatus.OK;

        when(organisationServiceMock.findByOrganisationStatus(any(OrganisationStatus.class))).thenReturn(organisationsDetailResponseMock);

        ResponseEntity<?> actual = organisationInternalController.retrieveOrganisations(null, "PENDING", null, null);
        Mockito.verify(organisationServiceMock, Mockito.times(1))
                .findByOrganisationStatus(any(OrganisationStatus.class));
        assertThat(actual).isNotNull();
        assertThat(actual.getStatusCode()).isEqualTo(expectedHttpStatus);
    }

    @Test
    public void testRetrieveOrganisationByIdNullStatusNull() {
        final HttpStatus expectedHttpStatus = HttpStatus.OK;

        when(organisationServiceMock.retrieveOrganisations()).thenReturn(organisationsDetailResponseMock);

        ResponseEntity<?> actual = organisationInternalController.retrieveOrganisations(null, null, null, null);
        Mockito.verify(organisationServiceMock, Mockito.times(1))
                .retrieveOrganisations();
        assertThat(actual).isNotNull();
        assertThat(actual.getStatusCode()).isEqualTo(expectedHttpStatus);
    }

    @Test
    public void testRetrieveOrganisationByIdNullStatusNullButPagingEnabled() {
        final HttpStatus expectedHttpStatus = HttpStatus.OK;

        when(refDataUtilMock.getPagingEnabled()).thenReturn("true");
        when(refDataUtilMock.getOrgSortColumn()).thenReturn("name");
        when(refDataUtilMock.getDefaultPageNumber()).thenReturn("0");
        when(refDataUtilMock.getDefaultPageSize()).thenReturn("10");
        Pageable pageableMock = mock(Pageable.class);

        when(organisationServiceMock.retrieveOrganisationsWithPageable(pageableMock)).thenReturn(responseEntity);

        ResponseEntity<?> actual = organisationInternalController.retrieveOrganisations(null, null, null, null);
        Mockito.verify(organisationServiceMock, Mockito.times(1))
                .retrieveOrganisationsWithPageable(any(Pageable.class));
        assertThat(actual).isNotNull();
        assertThat(actual.getStatusCode()).isEqualTo(expectedHttpStatus);
    }

    @Test
    public void testRetrieveOrganisationByStatusAndPaging() {
        final HttpStatus expectedHttpStatus = HttpStatus.OK;

        when(refDataUtilMock.getPagingEnabled()).thenReturn("true");
        when(refDataUtilMock.getOrgSortColumn()).thenReturn("name");
        when(refDataUtilMock.getDefaultPageNumber()).thenReturn("0");
        when(refDataUtilMock.getDefaultPageSize()).thenReturn("10");
        Pageable pageableMock = mock(Pageable.class);
        OrganisationStatus organisationStatusMock = mock(OrganisationStatus.class);
        responseEntity = mock(ResponseEntity.class);

        when(organisationServiceMock.findByOrganisationStatusWithPageable(organisationStatusMock, pageableMock)).thenReturn(responseEntity);
        when(responseEntity.getStatusCode()).thenReturn(HttpStatus.OK);
        ResponseEntity<?> actual = organisationInternalController.retrieveOrganisations(null, "PENDING", 0, 2);

        Mockito.verify(organisationServiceMock, Mockito.times(1))
                .findByOrganisationStatusWithPageable(any(OrganisationStatus.class),any(Pageable.class));
        assertThat(actual).isNotNull();
        assertThat(actual.getStatusCode()).isEqualTo(expectedHttpStatus);
    }

    @Test
    public void testRetrieveOrganisationByIdNullStatusNullAndPaging() {
        final HttpStatus expectedHttpStatus = HttpStatus.OK;

        when(refDataUtilMock.getPagingEnabled()).thenReturn("true");
        when(refDataUtilMock.getOrgSortColumn()).thenReturn("name");
        when(refDataUtilMock.getDefaultPageNumber()).thenReturn("0");
        when(refDataUtilMock.getDefaultPageSize()).thenReturn("10");
        Pageable pageableMock = mock(Pageable.class);
        responseEntity = mock(ResponseEntity.class);

        when(organisationServiceMock.retrieveOrganisationsWithPageable(pageableMock)).thenReturn(responseEntity);
        when(responseEntity.getStatusCode()).thenReturn(HttpStatus.OK);
        ResponseEntity<?> actual = organisationInternalController.retrieveOrganisations(null, null, 0, 2);

        Mockito.verify(organisationServiceMock, Mockito.times(1))
                .retrieveOrganisationsWithPageable(any(Pageable.class));
        assertThat(actual).isNotNull();
        assertThat(actual.getStatusCode()).isEqualTo(expectedHttpStatus);
    }

    @Test
    public void testRetrieveOrganisationByIdNullStatusNullButPagingDisabled() {
        final HttpStatus expectedHttpStatus = HttpStatus.OK;

        when(refDataUtilMock.getPagingEnabled()).thenReturn("false");
        when(organisationServiceMock.retrieveOrganisations()).thenReturn(organisationsDetailResponseMock);
        responseEntity = mock(ResponseEntity.class);

        when(responseEntity.getStatusCode()).thenReturn(HttpStatus.OK);
        ResponseEntity<?> actual = organisationInternalController.retrieveOrganisations(null, null, 0, 2);

        Mockito.verify(organisationServiceMock, Mockito.times(1))
                .retrieveOrganisations();
        assertThat(actual).isNotNull();
        assertThat(actual.getStatusCode()).isEqualTo(expectedHttpStatus);
    }

    @Test(expected = InvalidRequest.class)
    public void testRetrieveOrganisationThrows400WhenStatusInvalid() {
        when(organisationServiceMock.findByOrganisationStatus(any(OrganisationStatus.class))).thenReturn(organisationsDetailResponseMock);

        organisationInternalController.retrieveOrganisations(null, "this is not a status", null, null);
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