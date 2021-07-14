package uk.gov.hmcts.reform.professionalapi.controller;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Request;
import feign.Response;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import uk.gov.hmcts.reform.professionalapi.controller.advice.ErrorResponse;
import uk.gov.hmcts.reform.professionalapi.controller.advice.ResourceNotFoundException;
import uk.gov.hmcts.reform.professionalapi.controller.constants.IdamStatus;
import uk.gov.hmcts.reform.professionalapi.controller.feign.UserProfileFeignClient;
import uk.gov.hmcts.reform.professionalapi.controller.request.InvalidRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.NewUserCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.UserCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.UserProfileCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.validator.OrganisationCreationRequestValidator;
import uk.gov.hmcts.reform.professionalapi.controller.request.validator.UserProfileUpdateRequestValidator;
import uk.gov.hmcts.reform.professionalapi.controller.response.OrganisationsDetailResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.UserProfileCreationResponse;
import uk.gov.hmcts.reform.professionalapi.domain.ModifyUserRolesResponse;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus;
import uk.gov.hmcts.reform.professionalapi.domain.PaymentAccount;
import uk.gov.hmcts.reform.professionalapi.domain.PrdEnum;
import uk.gov.hmcts.reform.professionalapi.domain.PrdEnumId;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;
import uk.gov.hmcts.reform.professionalapi.domain.RoleAdditionResponse;
import uk.gov.hmcts.reform.professionalapi.domain.UserProfileUpdatedData;
import uk.gov.hmcts.reform.professionalapi.repository.PrdEnumRepository;
import uk.gov.hmcts.reform.professionalapi.service.OrganisationService;
import uk.gov.hmcts.reform.professionalapi.service.PaymentAccountService;
import uk.gov.hmcts.reform.professionalapi.service.ProfessionalUserService;
import uk.gov.hmcts.reform.professionalapi.service.impl.PrdEnumServiceImpl;

import javax.servlet.http.HttpServletRequest;

public class SuperControllerTest {

    @InjectMocks
    private SuperController superController = mock(SuperController.class, CALLS_REAL_METHODS);

    private OrganisationsDetailResponse organisationsDetailResponse;
    private OrganisationService organisationServiceMock;
    private ProfessionalUserService professionalUserServiceMock;
    private PaymentAccountService paymentAccountServiceMock;
    private PrdEnumServiceImpl prdEnumServiceMock;
    private OrganisationCreationRequest organisationCreationRequest;
    private OrganisationCreationRequestValidator organisationCreationRequestValidatorMock;
    private PrdEnumRepository prdEnumRepository;
    private Organisation organisation;
    private ProfessionalUser professionalUser;
    private UserProfileUpdateRequestValidator userProfileUpdateRequestValidator;
    private NewUserCreationRequest newUserCreationRequest;
    private UserProfileFeignClient userProfileFeignClient;
    private UserProfileUpdatedData userProfileUpdatedData;

    private final PrdEnumId prdEnumId1 = new PrdEnumId(10, "JURISD_ID");
    private final PrdEnumId prdEnumId2 = new PrdEnumId(13, "JURISD_ID");
    private final PrdEnumId prdEnumId3 = new PrdEnumId(3, "PRD_ROLE");

    private final PrdEnum anEnum1 = new PrdEnum(prdEnumId1, "PROBATE", "JURISD_ID");
    private final PrdEnum anEnum2 = new PrdEnum(prdEnumId2, "BULKSCAN", "JURISD_ID");
    private final PrdEnum anEnum3 = new PrdEnum(prdEnumId3, "pui-case-manager", "PRD_ROLE");

    private List<PrdEnum> prdEnumList;
    private List<String> jurisdEnumIds;

    @Before
    public void setUp() throws Exception {
        organisationCreationRequestValidatorMock = mock(OrganisationCreationRequestValidator.class);
        organisationServiceMock = mock(OrganisationService.class);
        professionalUserServiceMock = mock(ProfessionalUserService.class);
        paymentAccountServiceMock = mock(PaymentAccountService.class);
        prdEnumServiceMock = mock(PrdEnumServiceImpl.class);
        prdEnumRepository = mock(PrdEnumRepository.class);
        userProfileFeignClient = mock(UserProfileFeignClient.class);
        userProfileUpdateRequestValidator = mock(UserProfileUpdateRequestValidator.class);

        organisation = new Organisation("Org-Name", OrganisationStatus.PENDING, "sra-id",
                "companyN", false, "www.org.com");
        professionalUser = new ProfessionalUser("some-fname", "some-lname",
                "soMeone@somewhere.com", organisation);
        organisationsDetailResponse = new OrganisationsDetailResponse(singletonList(organisation),
                false);
        userProfileUpdatedData = new UserProfileUpdatedData("test@email.com", "firstName",
                "lastName", IdamStatus.ACTIVE.name(), null, null);

        prdEnumList = new ArrayList<>();
        prdEnumList.add(anEnum1);
        prdEnumList.add(anEnum2);
        prdEnumList.add(anEnum3);


        List<String> userRoles = new ArrayList<>();
        userRoles.add("pui-user-manager");
        newUserCreationRequest = new NewUserCreationRequest("some-name", "some-last-name",
                "some@email.com", userRoles, false);
        UserCreationRequest userCreationRequest = new UserCreationRequest("some-fname", "some-lname",
                "some@email.com");
        organisationCreationRequest = new OrganisationCreationRequest("test", "PENDING", "sra-id",
                "false", "number02", "company-url", userCreationRequest,
                null, null);

        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void test_createOrganisationFrom() {
        when(prdEnumServiceMock.getPrdEnumByEnumType(any())).thenReturn(jurisdEnumIds);
        when(prdEnumRepository.findAll()).thenReturn(prdEnumList);

        ResponseEntity<?> actual = superController.createOrganisationFrom(organisationCreationRequest);

        assertThat(actual).isNotNull();
        assertThat(actual.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        verify(organisationCreationRequestValidatorMock, times(1))
                .validate(any(OrganisationCreationRequest.class));
        verify(organisationServiceMock, times(1))
                .createOrganisationFrom(organisationCreationRequest);
    }

    @Test
    public void test_retrieveAllOrganisationOrById() {
        final HttpStatus expectedHttpStatus = HttpStatus.OK;

        when(organisationServiceMock.retrieveAllOrganisations()).thenReturn(organisationsDetailResponse);

        ResponseEntity<?> actual = superController.retrieveAllOrganisationOrById(null, null);

        assertThat(actual.getBody()).isEqualTo(organisationsDetailResponse);
        assertThat(actual.getStatusCode()).isEqualTo(expectedHttpStatus);

        verify(organisationServiceMock, times(1)).retrieveAllOrganisations();
    }


    @Test
    public void test_retrievePaymentAccountByEmail() {
        String email = "some-email@test.com";
        final HttpStatus expectedHttpStatus = HttpStatus.OK;
        final List<PaymentAccount> paymentAccounts = new ArrayList<>();
        paymentAccounts.add(new PaymentAccount());
        organisation.setPaymentAccounts(paymentAccounts);

        when(paymentAccountServiceMock.findPaymentAccountsByEmail(email)).thenReturn(organisation);

        ResponseEntity<?> actual = superController.retrievePaymentAccountByUserEmail(email);

        assertThat(actual).isNotNull();
        assertThat(actual.getStatusCode()).isEqualTo(expectedHttpStatus);

        verify(paymentAccountServiceMock, times(1)).findPaymentAccountsByEmail(email);
    }

    @Test
    public void test_InviteUserToOrganisation() throws JsonProcessingException {
        final HttpStatus expectedHttpStatus = HttpStatus.OK;
        String orgId = UUID.randomUUID().toString().substring(0, 7);
        newUserCreationRequest.setRoles(singletonList("pui-case-manager"));
        organisation.setStatus(OrganisationStatus.ACTIVE);

        when(organisationServiceMock.getOrganisationByOrgIdentifier(orgId)).thenReturn(organisation);
        when(professionalUserServiceMock.findProfessionalUserByEmailAddress("test@email.com"))
                .thenReturn(professionalUser);
        when(prdEnumServiceMock.getPrdEnumByEnumType(any())).thenReturn(jurisdEnumIds);
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

        ResponseEntity<?> actual = superController.inviteUserToOrganisation(newUserCreationRequest, orgId, userId);

        assertThat(actual).isNotNull();
        assertThat(actual.getStatusCode()).isEqualTo(expectedHttpStatus);

        verify(organisationServiceMock, times(1)).getOrganisationByOrgIdentifier(orgId);
        verify(professionalUserServiceMock, times(1))
                .findProfessionalUserByEmailAddress("some@email.com");
        verify(prdEnumServiceMock, times(1)).findAllPrdEnums();
    }

    @Test(expected = HttpClientErrorException.class)
    public void test_checkUserAlreadyExist() {
        String email = "test@email.com";
        when(professionalUserServiceMock.findProfessionalUserByEmailAddress(email)).thenReturn(professionalUser);

        superController.checkUserAlreadyExist(email);
        verify(professionalUserServiceMock, times(1)).findProfessionalUserByEmailAddress(email);
    }

    @Test(expected = Test.None.class)
    public void test_checkUserAlreadyExistThrowsHttpClientErrorException() {
        String email = "test@email.com";
        when(professionalUserServiceMock.findProfessionalUserByEmailAddress(email)).thenReturn(null);

        superController.checkUserAlreadyExist(email);

        verify(professionalUserServiceMock, times(1)).findProfessionalUserByEmailAddress(email);
    }

    @Test
    public void test_checkOrganisationIsActive() {
        when(organisationServiceMock.getOrganisationByOrgIdentifier(organisation.getOrganisationIdentifier()))
                .thenReturn(organisation);

        Organisation existingOrg = superController.checkOrganisationIsActive(organisation.getOrganisationIdentifier());

        assertThat(existingOrg)
                .isNotNull()
                .isEqualTo(organisation);

        verify(organisationServiceMock, times(1)).getOrganisationByOrgIdentifier(organisation
                .getOrganisationIdentifier());
    }

    @Test
    public void test_ReInviteUserToOrganisation() throws JsonProcessingException {

        ReflectionTestUtils.setField(superController, "resendInviteEnabled", true);
        final HttpStatus expectedHttpStatus = HttpStatus.OK;
        newUserCreationRequest.setRoles(singletonList("pui-case-manager"));
        newUserCreationRequest.setResendInvite(true);
        organisation.setStatus(OrganisationStatus.ACTIVE);
        String orgId = UUID.randomUUID().toString().substring(0, 7);
        when(organisationServiceMock.getOrganisationByOrgIdentifier(orgId)).thenReturn(organisation);
        when(professionalUserServiceMock.findProfessionalUserByEmailAddress(any())).thenReturn(professionalUser);

        UserProfileCreationResponse userProfileCreationResponse = new UserProfileCreationResponse();
        userProfileCreationResponse.setIdamId(UUID.randomUUID().toString());
        userProfileCreationResponse.setIdamRegistrationResponse(201);
        String userId = UUID.randomUUID().toString();

        ObjectMapper mapper = new ObjectMapper();
        String body = mapper.writeValueAsString(userProfileCreationResponse);

        when(userProfileFeignClient.createUserProfile(any(UserProfileCreationRequest.class)))
                .thenReturn(Response.builder().request(mock(Request.class)).body(body, Charset.defaultCharset())
                        .status(200).build());

        ResponseEntity<?> actual = superController.inviteUserToOrganisation(newUserCreationRequest,
                professionalUser.getOrganisation().getOrganisationIdentifier(), userId);

        assertThat(actual).isNotNull();
        assertThat(actual.getStatusCode()).isEqualTo(expectedHttpStatus);

        verify(organisationServiceMock, times(1))
                .getOrganisationByOrgIdentifier(professionalUser.getOrganisation().getOrganisationIdentifier());
        verify(professionalUserServiceMock, times(1))
                .findProfessionalUserByEmailAddress("some@email.com");
        verify(prdEnumServiceMock, times(0)).findAllPrdEnums();
    }


    @Test
    public void test_ReInviteUserToOrganisation_when_up_fails() throws JsonProcessingException {

        ReflectionTestUtils.setField(superController, "resendInviteEnabled", true);
        newUserCreationRequest.setRoles(singletonList("pui-case-manager"));
        newUserCreationRequest.setResendInvite(true);
        organisation.setStatus(OrganisationStatus.ACTIVE);
        String orgId = UUID.randomUUID().toString().substring(0, 7);
        when(organisationServiceMock.getOrganisationByOrgIdentifier(orgId)).thenReturn(organisation);
        when(professionalUserServiceMock.findProfessionalUserByEmailAddress(any())).thenReturn(professionalUser);

        ErrorResponse errorDetails = new ErrorResponse("errorMessage", "errorDescription",
                "23:13");
        String userId = UUID.randomUUID().toString();

        ObjectMapper mapper = new ObjectMapper();
        String body = mapper.writeValueAsString(errorDetails);

        when(userProfileFeignClient.createUserProfile(any(UserProfileCreationRequest.class))).thenReturn(Response
                .builder().request(mock(Request.class)).body(body, Charset.defaultCharset()).status(409).build());

        ResponseEntity<?> actual = superController.inviteUserToOrganisation(newUserCreationRequest, professionalUser
                .getOrganisation().getOrganisationIdentifier(), userId);

        assertThat(actual).isNotNull();
        assertThat(actual.getBody()).isExactlyInstanceOf(ErrorResponse.class);
        assertThat(actual.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);

        verify(organisationServiceMock, times(1))
                .getOrganisationByOrgIdentifier(professionalUser.getOrganisation().getOrganisationIdentifier());
        verify(professionalUserServiceMock, times(1))
                .findProfessionalUserByEmailAddress("some@email.com");
        verify(prdEnumServiceMock, times(0)).findAllPrdEnums();
    }

    @Test
    public void test_ReInviteUserToOrganisation_when_user_does_not_exists() {

        ReflectionTestUtils.setField(superController, "resendInviteEnabled", true);
        newUserCreationRequest.setRoles(singletonList("pui-case-manager"));
        newUserCreationRequest.setResendInvite(true);
        organisation.setStatus(OrganisationStatus.ACTIVE);
        String orgId = UUID.randomUUID().toString().substring(0, 7);
        when(organisationServiceMock.getOrganisationByOrgIdentifier(orgId)).thenReturn(organisation);
        when(professionalUserServiceMock.findProfessionalUserByEmailAddress(any())).thenReturn(null);

        final Throwable raisedException = catchThrowable(() -> superController
                .inviteUserToOrganisation(newUserCreationRequest, orgId, UUID.randomUUID().toString()));

        assertThat(raisedException).isExactlyInstanceOf(ResourceNotFoundException.class);

        verify(organisationServiceMock, times(1)).getOrganisationByOrgIdentifier(orgId);
        verify(professionalUserServiceMock, times(1))
                .findProfessionalUserByEmailAddress("some@email.com");
        verify(prdEnumServiceMock, times(0)).findAllPrdEnums();
    }

    @Test
    public void test_ReinviteDoesntHappenwhenToggleOff() throws JsonProcessingException {
        ReflectionTestUtils.setField(superController, "resendInviteEnabled", false);
        final HttpStatus expectedHttpStatus = HttpStatus.OK;
        newUserCreationRequest.setRoles(singletonList("pui-case-manager"));
        newUserCreationRequest.setResendInvite(true);
        organisation.setStatus(OrganisationStatus.ACTIVE);
        String orgId = UUID.randomUUID().toString().substring(0, 7);
        when(organisationServiceMock.getOrganisationByOrgIdentifier(orgId)).thenReturn(organisation);
        when(professionalUserServiceMock.findProfessionalUserByEmailAddress("test@email.com"))
                .thenReturn(professionalUser);
        when(prdEnumServiceMock.getPrdEnumByEnumType(any())).thenReturn(jurisdEnumIds);
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

        ResponseEntity<?> actual = superController.inviteUserToOrganisation(newUserCreationRequest, orgId, userId);

        assertThat(actual).isNotNull();
        assertThat(actual.getStatusCode()).isEqualTo(expectedHttpStatus);

        verify(organisationServiceMock, times(1)).getOrganisationByOrgIdentifier(orgId);
        verify(professionalUserServiceMock, times(1))
                .findProfessionalUserByEmailAddress("some@email.com");
        verify(prdEnumServiceMock, times(1)).findAllPrdEnums();
    }

    @Test
    public void test_ReInviteUserToOrganisation_when_user_does_not_exists_in_organisation() {

        ReflectionTestUtils.setField(superController, "resendInviteEnabled", true);
        newUserCreationRequest.setRoles(singletonList("pui-case-manager"));
        newUserCreationRequest.setResendInvite(true);
        organisation.setStatus(OrganisationStatus.ACTIVE);
        String orgId = UUID.randomUUID().toString().substring(0, 7);
        when(organisationServiceMock.getOrganisationByOrgIdentifier(orgId)).thenReturn(organisation);
        when(professionalUserServiceMock.findProfessionalUserByEmailAddress(any())).thenReturn(professionalUser);

        final Throwable raisedException = catchThrowable(() -> superController
                .inviteUserToOrganisation(newUserCreationRequest, orgId, UUID.randomUUID().toString()));

        assertThat(raisedException).isExactlyInstanceOf(AccessDeniedException.class);

        verify(organisationServiceMock, times(1)).getOrganisationByOrgIdentifier(orgId);
        verify(professionalUserServiceMock, times(1))
                .findProfessionalUserByEmailAddress("some@email.com");
        verify(prdEnumServiceMock, times(0)).findAllPrdEnums();
    }

    @Test
    public void testModifyRolesForExistingUserOfOrganisation() throws JsonProcessingException {
        ModifyUserRolesResponse modifyUserRolesResponse = new ModifyUserRolesResponse();
        RoleAdditionResponse roleAdditionResponse = new RoleAdditionResponse();
        roleAdditionResponse.setIdamMessage("some nessage");
        roleAdditionResponse.setIdamStatusCode("200");
        modifyUserRolesResponse.setRoleAdditionResponse(roleAdditionResponse);
        ObjectMapper mapper = new ObjectMapper();
        String body = mapper.writeValueAsString(modifyUserRolesResponse);
        ResponseEntity<Object> responseEntity = ResponseEntity.status(200).body(body);

        when(userProfileUpdateRequestValidator.validateRequest(userProfileUpdatedData))
                .thenReturn(userProfileUpdatedData);
        when(professionalUserServiceMock.modifyRolesForUser(any(), any(), any())).thenReturn(responseEntity);

        String userId = UUID.randomUUID().toString();
        ResponseEntity<Object> actualData = superController.modifyRolesForUserOfOrganisation(userProfileUpdatedData,
                userId, Optional.of("EXUI"));

        assertThat(actualData).isNotNull();
        assertThat(actualData.getStatusCode()).isEqualTo(HttpStatus.OK);

        verify(userProfileUpdateRequestValidator, times(1)).validateRequest(userProfileUpdatedData);
        verify(professionalUserServiceMock, times(1)).modifyRolesForUser(userProfileUpdatedData,
                userId, Optional.of("EXUI"));
    }

    @Test(expected = InvalidRequest.class)
    public void testGetUserEmailThrows400WhenEmailIsNull() {
        HttpServletRequest httpRequest = mock(HttpServletRequest.class);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(httpRequest));
        when(httpRequest.getHeader(anyString())).thenReturn(null);

        superController.getUserEmail(null);

        verify(httpRequest, times(1)).getHeader(null);
    }

    @Test
    public void testGetUserEmailUsesParamEmailWhenHeaderEmailNull() {
        HttpServletRequest httpRequest = mock(HttpServletRequest.class);
        String email = "adil@praveen.com";

        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(httpRequest));
        when(httpRequest.getHeader(anyString())).thenReturn(null);

        assertThat(email).isEqualTo(superController.getUserEmail(email));

        verify(httpRequest, times(1)).getHeader("UserEmail");
    }

    @Test
    public void testGetUserEmailFromHeaderWhenHeaderEmailNull() {
        HttpServletRequest httpRequest = mock(HttpServletRequest.class);
        String email = "adil@praveen.com";

        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(httpRequest));
        when(httpRequest.getHeader(anyString())).thenReturn("adil@praveen.com");

        assertThat(email).isEqualTo(superController.getUserEmailFromHeader());

        verify(httpRequest, times(2)).getHeader("UserEmail");
    }
}