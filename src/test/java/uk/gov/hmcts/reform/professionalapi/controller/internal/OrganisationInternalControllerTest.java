package uk.gov.hmcts.reform.professionalapi.controller.internal;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Request;
import feign.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import uk.gov.hmcts.reform.professionalapi.controller.advice.ResourceNotFoundException;
import uk.gov.hmcts.reform.professionalapi.controller.feign.UserProfileFeignClient;
import uk.gov.hmcts.reform.professionalapi.controller.request.InvalidRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.MfaUpdateRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.NewUserCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationOtherOrgsCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.PbaRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.UserCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.UserProfileCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.validator.OrganisationCreationRequestValidator;
import uk.gov.hmcts.reform.professionalapi.controller.request.validator.PaymentAccountValidator;
import uk.gov.hmcts.reform.professionalapi.controller.request.validator.UpdateOrganisationRequestValidator;
import uk.gov.hmcts.reform.professionalapi.controller.request.validator.impl.OrganisationIdentifierValidatorImpl;
import uk.gov.hmcts.reform.professionalapi.controller.request.validator.impl.OrganisationStatusValidatorImpl;
import uk.gov.hmcts.reform.professionalapi.controller.response.DeleteOrganisationResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.OrganisationEntityResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.OrganisationResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.OrganisationsDetailResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.UserProfileCreationResponse;
import uk.gov.hmcts.reform.professionalapi.domain.MFAStatus;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus;
import uk.gov.hmcts.reform.professionalapi.domain.PaymentAccount;
import uk.gov.hmcts.reform.professionalapi.domain.PbaStatus;
import uk.gov.hmcts.reform.professionalapi.domain.PrdEnum;
import uk.gov.hmcts.reform.professionalapi.domain.PrdEnumId;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;
import uk.gov.hmcts.reform.professionalapi.domain.SuperUser;
import uk.gov.hmcts.reform.professionalapi.repository.PrdEnumRepository;
import uk.gov.hmcts.reform.professionalapi.service.MfaStatusService;
import uk.gov.hmcts.reform.professionalapi.service.OrganisationService;
import uk.gov.hmcts.reform.professionalapi.service.PaymentAccountService;
import uk.gov.hmcts.reform.professionalapi.service.ProfessionalUserService;
import uk.gov.hmcts.reform.professionalapi.service.impl.PrdEnumServiceImpl;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.ORG_NAME;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.ORG_STATUS;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
class OrganisationInternalControllerTest {
    private OrganisationResponse organisationResponse;
    private OrganisationsDetailResponse organisationsDetailResponse;
    private OrganisationEntityResponse organisationEntityResponse;
    private OrganisationService organisationServiceMock;
    private PaymentAccountService paymentAccountServiceMock;
    private Organisation organisation;
    private OrganisationCreationRequest organisationCreationRequest;
    private OrganisationOtherOrgsCreationRequest organisationOtherOrgsCreationRequest;
    private OrganisationCreationRequestValidator organisationCreationRequestValidatorMock;
    private PaymentAccountValidator paymentAccountValidatorMock;
    private ProfessionalUserService professionalUserServiceMock;
    private MfaStatusService mfaStatusServiceMock;
    private OrganisationIdentifierValidatorImpl orgIdValidatorMock;
    private UpdateOrganisationRequestValidator updateOrganisationRequestValidatorMock;
    private OrganisationStatusValidatorImpl organisationStatusValidatorMock;

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

    private ProfessionalUser professionalUser;
    private NewUserCreationRequest newUserCreationRequest;
    private UserProfileFeignClient userProfileFeignClient;
    private DeleteOrganisationResponse deleteOrganisationResponse;
    HttpServletRequest httpRequest = mock(HttpServletRequest.class);

    @InjectMocks
    private OrganisationInternalController organisationInternalController;

    @InjectMocks
    private OrganisationInternalControllerV2 organisationInternalControllerV2;

    @BeforeEach
    void setUp() throws Exception {
        organisation = new Organisation("Org-Name", OrganisationStatus.PENDING, "sra-id",
                "companyN", false, "www.org.com");
        organisationResponse = new OrganisationResponse(organisation);
        organisationsDetailResponse =
                new OrganisationsDetailResponse(singletonList(organisation), false, false, true);
        organisationEntityResponse =
                new OrganisationEntityResponse(organisation, false, true, true);
        deleteOrganisationResponse = new DeleteOrganisationResponse(204,"successfully deleted");
        organisationResponse = new OrganisationResponse(organisation);
        organisationsDetailResponse =
                new OrganisationsDetailResponse(singletonList(organisation), false, false, true);
        organisationEntityResponse =
                new OrganisationEntityResponse(organisation, false, true, true);

        organisationServiceMock = mock(OrganisationService.class);
        professionalUserServiceMock = mock(ProfessionalUserService.class);
        paymentAccountServiceMock = mock(PaymentAccountService.class);
        organisationCreationRequestValidatorMock = mock(OrganisationCreationRequestValidator.class);
        paymentAccountValidatorMock = mock(PaymentAccountValidator.class);
        prdEnumServiceMock = mock(PrdEnumServiceImpl.class);
        prdEnumRepository = mock(PrdEnumRepository.class);
        userProfileFeignClient = mock(UserProfileFeignClient.class);
        mfaStatusServiceMock = mock(MfaStatusService.class);
        orgIdValidatorMock = mock(OrganisationIdentifierValidatorImpl.class);
        updateOrganisationRequestValidatorMock = mock(UpdateOrganisationRequestValidator.class);
        organisationStatusValidatorMock = mock(OrganisationStatusValidatorImpl.class);
        prdEnumList = new ArrayList<>();
        prdEnumList.add(anEnum1);
        prdEnumList.add(anEnum2);
        prdEnumList.add(anEnum3);

        userCreationRequest = new UserCreationRequest("some-fname", "some-lname",
                "some@email.com");
        organisationCreationRequest = new OrganisationCreationRequest("test", "PENDING", null,
                "sra-id", "false", "number02", "company-url",
                userCreationRequest, null, null);
        organisationOtherOrgsCreationRequest = new OrganisationOtherOrgsCreationRequest("test", "PENDING", null,
                "sra-id", "false", "number02", "company-url",
                userCreationRequest, null, null,"Doctor",null);

        organisation.setOrganisationIdentifier("AK57L4T");

        organisationResponse = new OrganisationResponse(organisation);
        professionalUser = new ProfessionalUser("some-fname", "some-lname",
                "soMeone@somewhere.com", organisation);
        organisationEntityResponse = new OrganisationEntityResponse(organisation, false, false, true);

        List<String> userRoles = new ArrayList<>();
        userRoles.add("pui-user-manager");
        newUserCreationRequest = new NewUserCreationRequest("some-name", "some-last-name",
                "some@email.com", userRoles, false);

        MockitoAnnotations.openMocks(this);
    }

    @Test
    void test_CreateOrganisation() {
        final HttpStatus expectedHttpStatus = HttpStatus.CREATED;

        when(organisationServiceMock.createOrganisationFrom(organisationCreationRequest))
                .thenReturn(organisationResponse);

        ResponseEntity<?> actual = organisationInternalController.createOrganisation(organisationCreationRequest);

        verify(organisationCreationRequestValidatorMock, times(1))
                .validate(any(OrganisationCreationRequest.class));
        verify(organisationServiceMock, times(1))
                .createOrganisationFrom(any(OrganisationCreationRequest.class));

        assertThat(actual).isNotNull();
        assertThat(actual.getStatusCode()).isEqualTo(expectedHttpStatus);
    }

    @Test
    void test_CreateOrganisationV2() {
        final HttpStatus expectedHttpStatus = HttpStatus.CREATED;

        when(organisationServiceMock.createOrganisationFrom(organisationOtherOrgsCreationRequest))
                .thenReturn(organisationResponse);

        ResponseEntity<?> actual = organisationInternalControllerV2
                .createOrganisation(organisationOtherOrgsCreationRequest);

        verify(organisationCreationRequestValidatorMock, times(1))
                .validate(any(OrganisationOtherOrgsCreationRequest.class));
        verify(organisationServiceMock, times(1))
                .createOrganisationFrom(any(OrganisationOtherOrgsCreationRequest.class));

        assertThat(actual).isNotNull();
        assertThat(actual.getStatusCode()).isEqualTo(expectedHttpStatus);
    }

    @Test
    void test_RetrieveOrganisations() {
        final HttpStatus expectedHttpStatus = HttpStatus.OK;

        when(organisationServiceMock.retrieveAllOrganisations(null)).thenReturn(organisationsDetailResponse);

        ResponseEntity<?> actual = organisationInternalController.retrieveOrganisations(null, null, null, null);

        assertThat(actual).isNotNull();
        assertThat(actual.getStatusCode()).isEqualTo(expectedHttpStatus);

        verify(organisationServiceMock, times(1)).retrieveAllOrganisations(null);
    }

    @Test
    void test_RetrieveOrganisationByIdWithStatusNull() {
        final HttpStatus expectedHttpStatus = HttpStatus.OK;

        when(organisationServiceMock.retrieveOrganisation(any(String.class), any(boolean.class)))
                .thenReturn(organisationEntityResponse);

        ResponseEntity<?> actual = organisationInternalController
                .retrieveOrganisations(organisation.getOrganisationIdentifier(), null, 1, null);

        assertThat(actual).isNotNull();
        assertThat(actual.getStatusCode()).isEqualTo(expectedHttpStatus);
        assertThat(actual.getHeaders().get("total_records")).hasSizeGreaterThanOrEqualTo(1);

        verify(organisationServiceMock, times(1))
                .retrieveOrganisation(organisation.getOrganisationIdentifier(), true);
    }

    @Test
    void test_RetrieveOrganisationByIdWithStatusNotNull() {
        final HttpStatus expectedHttpStatus = HttpStatus.OK;

        when(organisationServiceMock.retrieveOrganisation(any(String.class), any(boolean.class)))
                .thenReturn(organisationEntityResponse);

        ResponseEntity<?> actual = organisationInternalController.retrieveOrganisations(organisation
                .getOrganisationIdentifier(), "PENDING", null, null);

        assertThat(actual).isNotNull();
        assertThat(actual.getStatusCode()).isEqualTo(expectedHttpStatus);

        verify(organisationServiceMock, times(1))
                .retrieveOrganisation(organisation.getOrganisationIdentifier(), true);
    }

    @Test
    void test_RetrieveOrganisationByStatusWithIdNull() {
        final HttpStatus expectedHttpStatus = HttpStatus.OK;

        when(organisationServiceMock.findByOrganisationStatus(any(), any()))
                .thenReturn(organisationsDetailResponse);

        ResponseEntity<?> actual = organisationInternalController.retrieveOrganisations(null, "PENDING", null, null);

        assertThat(actual).isNotNull();
        assertThat(actual.getStatusCode()).isEqualTo(expectedHttpStatus);

        verify(organisationServiceMock, times(1))
                .findByOrganisationStatus(OrganisationStatus.PENDING.name(), null);
    }

    @Test
    void test_RetrieveOrganisationWithPageNull() {
        final HttpStatus expectedHttpStatus = HttpStatus.OK;
        Sort.Order order = new Sort.Order(Sort.DEFAULT_DIRECTION, ORG_NAME).ignoreCase();
        Pageable pageable = PageRequest.of(0, 1, Sort.by(order));

        when(organisationServiceMock.retrieveAllOrganisations(pageable))
            .thenReturn(organisationsDetailResponse);

        ResponseEntity<?> actual = organisationInternalController.retrieveOrganisations(null, null, null, 1);

        assertThat(actual).isNotNull();
        assertThat(actual.getStatusCode()).isEqualTo(expectedHttpStatus);
        assertThat(actual.getHeaders().get("total_records")).hasSizeGreaterThanOrEqualTo(organisationsDetailResponse
                .getOrganisations().size());

        verify(organisationServiceMock, times(1))
            .retrieveAllOrganisations(pageable);
    }

    @Test
    void test_RetrieveOrganisationWithSizeNull() {
        final HttpStatus expectedHttpStatus = HttpStatus.OK;
        Sort.Order order = new Sort.Order(Sort.DEFAULT_DIRECTION, ORG_NAME).ignoreCase();
        Pageable pageable = PageRequest.of(0, 20, Sort.by(order));

        when(organisationServiceMock.retrieveAllOrganisations(pageable))
            .thenReturn(organisationsDetailResponse);

        ResponseEntity<?> actual = organisationInternalController.retrieveOrganisations(null, null, 1, null);

        assertThat(actual).isNotNull();
        assertThat(actual.getStatusCode()).isEqualTo(expectedHttpStatus);
        assertThat(actual.getHeaders().get("total_records")).hasSizeGreaterThanOrEqualTo(organisationsDetailResponse
                .getOrganisations().size());

        verify(organisationServiceMock, times(1))
            .retrieveAllOrganisations(pageable);
    }

    @Test
    void test_RetrieveOrganisationByStatusWithPagination() {
        final HttpStatus expectedHttpStatus = HttpStatus.OK;
        Sort.Order order = new Sort.Order(Sort.DEFAULT_DIRECTION, ORG_STATUS).ignoreCase();
        Sort.Order name = new Sort.Order(Sort.DEFAULT_DIRECTION, ORG_NAME).ignoreCase();
        Pageable pageable = PageRequest.of(0, 20, Sort.by(order).and(Sort.by(name)));

        when(organisationServiceMock.findByOrganisationStatus(any(), any()))
            .thenReturn(organisationsDetailResponse);

        ResponseEntity<?> actual = organisationInternalController.retrieveOrganisations(null, "PENDING", 1, 20);

        assertThat(actual).isNotNull();
        assertThat(actual.getStatusCode()).isEqualTo(expectedHttpStatus);
        assertThat(actual.getHeaders().get("total_records")).hasSizeGreaterThanOrEqualTo(organisationsDetailResponse
                .getOrganisations().size());

        verify(organisationServiceMock, times(1))
            .findByOrganisationStatus(OrganisationStatus.PENDING.name(), pageable);
    }

    @Test
    void test_RetrievePaymentAccountByEmailFromHeader() {

        final HttpStatus expectedHttpStatus = HttpStatus.OK;
        final List<PaymentAccount> paymentAccounts = new ArrayList<>();
        paymentAccounts.add(new PaymentAccount());
        organisation.setPaymentAccounts(paymentAccounts);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(httpRequest));
        String email = "some-email@test.com";
        when(httpRequest.getHeader(anyString())).thenReturn(email);
        when(paymentAccountServiceMock.findPaymentAccountsByEmail(email)).thenReturn(organisation);

        ResponseEntity<?> actual = organisationInternalController.retrievePaymentAccountBySuperUserEmail();

        assertThat(actual).isNotNull();
        assertThat(actual.getStatusCode()).isEqualTo(expectedHttpStatus);

        verify(paymentAccountServiceMock, times(1)).findPaymentAccountsByEmail(email);
        verify(httpRequest, times(2)).getHeader(anyString());
    }

    @Test
    void test_RetrievePaymentAccountByEmailThrows404WhenNoAccFound() {
        String email = "test@email.com";
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(httpRequest));
        when(httpRequest.getHeader(anyString())).thenReturn(email);
        when(paymentAccountServiceMock.findPaymentAccountsByEmail(email)).thenReturn(organisation);
        assertThrows(ResourceNotFoundException.class, () ->
                organisationInternalController.retrievePaymentAccountBySuperUserEmail());
    }

    @Test
    void test_RetrievePaymentAccountByEmailThrows400WhenEmailIsInvalid() {
        String email = "some-email";
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(httpRequest));
        when(httpRequest.getHeader(anyString())).thenReturn(email);
        assertThrows(InvalidRequest.class, () ->
                organisationInternalController.retrievePaymentAccountBySuperUserEmail());
    }

    @Test
    void test_RetrievePaymentAccountByEmailThrows400WhenEmailIsNull() {
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(httpRequest));
        when(httpRequest.getHeader(anyString())).thenReturn(null);
        assertThrows(InvalidRequest.class, () ->
                organisationInternalController.retrievePaymentAccountBySuperUserEmail());

        verify(httpRequest, times(1)).getHeader("UserEmail");
    }

    @Test
    void test_EditPaymentAccountsByOrgId() {
        final HttpStatus expectedHttpStatus = HttpStatus.OK;

        Set<String> pbas = new HashSet<>();
        pbas.add("PBA0000001");
        PbaRequest pbaEditRequest = new PbaRequest();
        pbaEditRequest.setPaymentAccounts(pbas);

        when(organisationServiceMock.getOrganisationByOrgIdentifier(organisation.getOrganisationIdentifier()))
                .thenReturn(organisation);

        ResponseEntity<Object> response = organisationInternalController.editPaymentAccountsByOrgId(pbaEditRequest,
                organisation.getOrganisationIdentifier());

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(expectedHttpStatus);

        verify(paymentAccountServiceMock, times(1))
                .editPaymentAccountsByOrganisation(organisation, pbaEditRequest);
    }

    @Test
    void test_InviteUserToOrganisation() throws JsonProcessingException {
        final HttpStatus expectedHttpStatus = HttpStatus.OK;
        String orgId = UUID.randomUUID().toString().substring(0, 7);
        newUserCreationRequest.setRoles(singletonList("pui-case-manager"));
        organisation.setStatus(OrganisationStatus.ACTIVE);

        when(organisationServiceMock.getOrganisationByOrgIdentifier(orgId)).thenReturn(organisation);
        when(prdEnumServiceMock.findAllPrdEnums()).thenReturn(prdEnumList);

        UserProfileCreationResponse userProfileCreationResponse = new UserProfileCreationResponse();
        userProfileCreationResponse.setIdamId(UUID.randomUUID().toString());
        userProfileCreationResponse.setIdamRegistrationResponse(201);
        String userId = UUID.randomUUID().toString();

        ObjectMapper mapper = new ObjectMapper();
        String body = mapper.writeValueAsString(userProfileCreationResponse);

        when(userProfileFeignClient.createUserProfile(any(UserProfileCreationRequest.class)))
                .thenReturn(Response.builder().request(mock(Request.class)).body(body, Charset.defaultCharset())
                        .status(200).build());

        ResponseEntity<?> actual = organisationInternalController.addUserToOrganisation(newUserCreationRequest,
                orgId, userId);

        assertThat(actual).isNotNull();
        assertThat(actual.getStatusCode()).isEqualTo(expectedHttpStatus);

        verify(organisationServiceMock, times(1)).getOrganisationByOrgIdentifier(orgId);
        verify(professionalUserServiceMock, times(1))
                .findProfessionalUserByEmailAddress("some@email.com");
    }

    @Test
    void testDeleteOrganisation() {

        final HttpStatus expectedHttpStatus = HttpStatus.NO_CONTENT;
        String orgId = UUID.randomUUID().toString().substring(0, 7);
        organisation.setStatus(OrganisationStatus.PENDING);
        when(organisationServiceMock.getOrganisationByOrgIdentifier(orgId)).thenReturn(organisation);
        when(organisationServiceMock.deleteOrganisation(organisation, "123456789"))
                .thenReturn(deleteOrganisationResponse);
        ResponseEntity<?> actual = organisationInternalController.deleteOrganisation(orgId, "123456789");

        verify(organisationServiceMock, times(1)).getOrganisationByOrgIdentifier(orgId);
        verify(organisationServiceMock, times(1))
                .deleteOrganisation(organisation, "123456789");

        assertThat(actual).isNotNull();
        assertThat(actual.getStatusCode()).isEqualTo(expectedHttpStatus);
    }

    @Test
    void testDeleteOrganisationWithStatusReview() {

        final HttpStatus expectedHttpStatus = HttpStatus.NO_CONTENT;
        String orgId = UUID.randomUUID().toString().substring(0, 7);
        organisation.setStatus(OrganisationStatus.REVIEW);
        when(organisationServiceMock.getOrganisationByOrgIdentifier(orgId)).thenReturn(organisation);
        when(organisationServiceMock.deleteOrganisation(organisation, "123456789"))
                .thenReturn(deleteOrganisationResponse);
        ResponseEntity<?> actual = organisationInternalController.deleteOrganisation(orgId, "123456789");

        verify(organisationServiceMock, times(1)).getOrganisationByOrgIdentifier(orgId);
        verify(organisationServiceMock, times(1))
                .deleteOrganisation(organisation, "123456789");

        assertThat(actual).isNotNull();
        assertThat(actual.getStatusCode()).isEqualTo(expectedHttpStatus);
    }

    @Test
    void testDeleteOrganisationThrows404WhenNoOrgFound() {
        String orgId = UUID.randomUUID().toString().substring(0, 7);
        when(organisationServiceMock.getOrganisationByOrgIdentifier(orgId)).thenReturn(null);
        assertThrows(EmptyResultDataAccessException.class, () ->
                organisationInternalController.deleteOrganisation(orgId, "123456789"));
        verify(organisationServiceMock, times(1)).getOrganisationByOrgIdentifier(orgId);
    }

    @Test
    void testUpdateOrgMfa() {
        final HttpStatus expectedHttpStatus = HttpStatus.OK;
        ResponseEntity<Object> updateResponseEntity = ResponseEntity.status(HttpStatus.OK).build();

        MfaUpdateRequest mfaUpdateRequest = new MfaUpdateRequest(MFAStatus.NONE);
        organisation.setStatus(OrganisationStatus.ACTIVE);
        when(organisationServiceMock.getOrganisationByOrgIdentifier(organisation.getOrganisationIdentifier()))
                .thenReturn(organisation);
        when(mfaStatusServiceMock.updateOrgMfaStatus(mfaUpdateRequest, organisation)).thenReturn(updateResponseEntity);

        ResponseEntity<Object> response = organisationInternalController.updateOrgMfaStatus(mfaUpdateRequest,
                organisation.getOrganisationIdentifier());

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(expectedHttpStatus);

        verify(mfaStatusServiceMock, times(1))
                .updateOrgMfaStatus(mfaUpdateRequest, organisation);
        verify(organisationServiceMock, times(1))
                .getOrganisationByOrgIdentifier(organisation.getOrganisationIdentifier());
    }

    @Test
    void testUpdateOrgMfaThrows400WhenOrgNotActive() {
        MfaUpdateRequest mfaUpdateRequest = new MfaUpdateRequest(MFAStatus.NONE);
        String orgId = organisation.getOrganisationIdentifier();

        when(organisationServiceMock.getOrganisationByOrgIdentifier(orgId)).thenReturn(organisation);

        assertThrows(InvalidRequest.class, () ->
                organisationInternalController.updateOrgMfaStatus(mfaUpdateRequest, orgId));

        verify(orgIdValidatorMock, times(1)).validateOrganisationExistsWithGivenOrgId(orgId);
        verify(organisationServiceMock, times(1)).getOrganisationByOrgIdentifier(orgId);
    }

    @Test
    void testUpdateOrganisation() {
        organisationCreationRequest.setStatusMessage("Company in review");
        organisationCreationRequest.setStatus(OrganisationStatus.REVIEW.toString());
        SuperUser superUser = new SuperUser();
        organisation.setUsers(Collections.singletonList(superUser));
        String orgId = "AK57L4T";

        when(organisationServiceMock.getOrganisationByOrgIdentifier(organisation.getOrganisationIdentifier()))
                .thenReturn(organisation);
        when(professionalUserServiceMock.findProfessionalUserById(any())).thenReturn(professionalUser);

        ResponseEntity<Object> response = organisationInternalController
                .updatesOrganisation(organisationCreationRequest, orgId, null);

        assertNotNull(response);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        verify(organisationServiceMock, times(1)).getOrganisationByOrgIdentifier(orgId);
        verify(professionalUserServiceMock, times(1)).findProfessionalUserById(any());
    }

    @Test
    void testRetrieveOrgByPbaStatus() {
        final HttpStatus expectedHttpStatus = HttpStatus.OK;
        ResponseEntity<Object> responseEntity = ResponseEntity.status(HttpStatus.OK).build();
        PbaStatus pbaStatus = PbaStatus.ACCEPTED;
        when(organisationServiceMock.getOrganisationsByPbaStatus(pbaStatus.toString())).thenReturn(responseEntity);

        ResponseEntity<Object> response = organisationInternalController.retrieveOrgByPbaStatus(pbaStatus.toString());

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(expectedHttpStatus);

        verify(organisationServiceMock, times(1))
                .getOrganisationsByPbaStatus(pbaStatus.toString());
    }

}