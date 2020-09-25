package uk.gov.hmcts.reform.professionalapi.controller.external;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.professionalapi.domain.LanguagePreference.EN;
import static uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus.ACTIVE;
import static uk.gov.hmcts.reform.professionalapi.domain.UserCategory.PROFESSIONAL;
import static uk.gov.hmcts.reform.professionalapi.domain.UserType.EXTERNAL;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Request;
import feign.Response;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;
import uk.gov.hmcts.reform.professionalapi.controller.advice.ResourceNotFoundException;
import uk.gov.hmcts.reform.professionalapi.controller.constants.TestConstants;
import uk.gov.hmcts.reform.professionalapi.controller.feign.UserProfileFeignClient;
import uk.gov.hmcts.reform.professionalapi.controller.request.NewUserCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.UserCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.UserProfileCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.validator.OrganisationCreationRequestValidator;
import uk.gov.hmcts.reform.professionalapi.controller.request.validator.impl.OrganisationIdentifierValidatorImpl;
import uk.gov.hmcts.reform.professionalapi.controller.response.OrganisationEntityResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.OrganisationMinimalInfoResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.OrganisationResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.UserProfileCreationResponse;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus;
import uk.gov.hmcts.reform.professionalapi.domain.PrdEnum;
import uk.gov.hmcts.reform.professionalapi.domain.PrdEnumId;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;
import uk.gov.hmcts.reform.professionalapi.domain.SuperUser;
import uk.gov.hmcts.reform.professionalapi.oidc.JwtGrantedAuthoritiesConverter;
import uk.gov.hmcts.reform.professionalapi.repository.PrdEnumRepository;
import uk.gov.hmcts.reform.professionalapi.service.OrganisationService;
import uk.gov.hmcts.reform.professionalapi.service.PaymentAccountService;
import uk.gov.hmcts.reform.professionalapi.service.ProfessionalUserService;
import uk.gov.hmcts.reform.professionalapi.service.impl.PrdEnumServiceImpl;
import uk.gov.hmcts.reform.professionalapi.util.RefDataUtil;



public class OrganisationExternalControllerTest {

    @InjectMocks
    private OrganisationExternalController organisationExternalController;

    private OrganisationResponse organisationResponse;
    private OrganisationEntityResponse organisationEntityResponse;
    private OrganisationService organisationServiceMock;
    private ProfessionalUserService professionalUserServiceMock;
    private PaymentAccountService paymentAccountServiceMock;
    private PrdEnumServiceImpl prdEnumServiceMock;
    private OrganisationCreationRequest organisationCreationRequest;
    private OrganisationCreationRequestValidator organisationCreationRequestValidatorMock;
    private PrdEnumRepository prdEnumRepository;
    private UserCreationRequest userCreationRequest;
    private Organisation organisation;
    private Organisation organisation1;
    private ProfessionalUser professionalUser;
    private OrganisationIdentifierValidatorImpl organisationIdentifierValidatorImplMock;
    private UserProfileCreationRequest userProfileCreationRequest;
    private NewUserCreationRequest newUserCreationRequest;
    private UserProfileFeignClient userProfileFeignClient;
    private Response response;
    private JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverterMock;
    private UserInfo userInfoMock;
    RefDataUtil refDataUtilMock;

    HttpServletRequest httpRequest = mock(HttpServletRequest.class);
    private final PrdEnumId prdEnumId1 = new PrdEnumId(10, "JURISD_ID");
    private final PrdEnumId prdEnumId2 = new PrdEnumId(13, "JURISD_ID");
    private final PrdEnumId prdEnumId3 = new PrdEnumId(3, "PRD_ROLE");

    private final PrdEnum anEnum1 = new PrdEnum(prdEnumId1, "PROBATE", "JURISD_ID");
    private final PrdEnum anEnum2 = new PrdEnum(prdEnumId2, "BULKSCAN", "JURISD_ID");
    private final PrdEnum anEnum3 = new PrdEnum(prdEnumId3, "pui-case-manager", "PRD_ROLE");

    private List<PrdEnum> prdEnumList;

    @Before
    public void setUp() throws Exception {
        organisationCreationRequestValidatorMock = mock(OrganisationCreationRequestValidator.class);
        organisationServiceMock = mock(OrganisationService.class);
        organisationIdentifierValidatorImplMock = mock(OrganisationIdentifierValidatorImpl.class);
        professionalUserServiceMock = mock(ProfessionalUserService.class);
        paymentAccountServiceMock = mock(PaymentAccountService.class);
        prdEnumServiceMock = mock(PrdEnumServiceImpl.class);
        prdEnumRepository = mock(PrdEnumRepository.class);
        userProfileFeignClient = mock(UserProfileFeignClient.class);
        jwtGrantedAuthoritiesConverterMock = mock(JwtGrantedAuthoritiesConverter.class);
        userInfoMock = mock(UserInfo.class);

        organisation = new Organisation("Org-Name", OrganisationStatus.PENDING, "sra-id",
                "companyN", false, "www.org.com");
        organisation1 = new Organisation("Org-Name2", OrganisationStatus.ACTIVE, "sra-id2",
                "companyN2", false, "www2.org.com");
        organisationResponse = new OrganisationResponse(organisation);
        professionalUser = new ProfessionalUser("some-fname", "some-lname",
                "soMeone@somewhere.com", organisation);
        SuperUser superUser = new SuperUser("some-fname", "some-lname",
                "some-email-address", organisation);
        organisationEntityResponse = new OrganisationEntityResponse(organisation, false);


        prdEnumList = new ArrayList<>();
        prdEnumList.add(anEnum1);
        prdEnumList.add(anEnum2);
        prdEnumList.add(anEnum3);

        refDataUtilMock = mock(RefDataUtil.class);

        List<String> userRoles = new ArrayList<>();
        userRoles.add("pui-user-manager");

        newUserCreationRequest = new NewUserCreationRequest("some-name", "some-last-name",
                "some@email.com", userRoles,false);
        userCreationRequest = new UserCreationRequest("some-fname", "some-lname",
                "some@email.com");
        organisationCreationRequest = new OrganisationCreationRequest("test", "PENDING",
                "sra-id", "false", "number02", "company-url",
                userCreationRequest, null, null);
        userProfileCreationRequest = new UserProfileCreationRequest("some@email.com",
                "some-name", "some-last-name", EN, PROFESSIONAL, EXTERNAL, userRoles,
                false);
        response = Response.builder().status(200).reason("OK").body(mock(Response.Body.class))
                .request(mock(Request.class)).build();

        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void test_CreateOrganisation() {
        when(organisationServiceMock.createOrganisationFrom(organisationCreationRequest))
                .thenReturn(organisationResponse);
        when(prdEnumRepository.findAll()).thenReturn(prdEnumList);

        ResponseEntity<?> actual = organisationExternalController
                .createOrganisationUsingExternalController(organisationCreationRequest);

        verify(organisationCreationRequestValidatorMock, times(1))
                .validate(any(OrganisationCreationRequest.class));
        verify(organisationServiceMock, times(1))
                .createOrganisationFrom(organisationCreationRequest);

        assertThat(actual).isNotNull();
        assertThat(actual.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @Test
    public void test_RetrieveOrganisationByIdentifier() {
        final HttpStatus expectedHttpStatus = HttpStatus.OK;

        String id = UUID.randomUUID().toString().substring(0, 7);
        when(organisationServiceMock.retrieveOrganisation(id)).thenReturn(organisationEntityResponse);

        ResponseEntity<?> actual = organisationExternalController.retrieveOrganisationUsingOrgIdentifier(id);

        assertThat(actual).isNotNull();
        assertThat(actual.getStatusCode()).isEqualTo(expectedHttpStatus);

        verify(organisationServiceMock, times(1)).retrieveOrganisation(eq(id));
    }

    @Test
    public void test_RetrievePaymentAccountByUserEmail() {
        final HttpStatus expectedHttpStatus = HttpStatus.OK;
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(httpRequest));

        List<String> authorities = new ArrayList<>();
        authorities.add(TestConstants.PUI_USER_MANAGER);
        String email = "test@email.com";
        when(httpRequest.getHeader(anyString())).thenReturn(email);
        when(jwtGrantedAuthoritiesConverterMock.getUserInfo()).thenReturn(userInfoMock);
        when(userInfoMock.getRoles()).thenReturn(authorities);
        when(paymentAccountServiceMock.findPaymentAccountsByEmail(email)).thenReturn(organisation);
        ResponseEntity<?> actual = organisationExternalController.retrievePaymentAccountByEmail(email,
                UUID.randomUUID().toString().substring(0, 7));

        assertThat(actual).isNotNull();
        assertThat(actual.getStatusCode()).isEqualTo(expectedHttpStatus);
        verify(paymentAccountServiceMock, times(1)).findPaymentAccountsByEmail(email);
        verify(httpRequest, times(2)).getHeader(anyString());
        verify(jwtGrantedAuthoritiesConverterMock, times(1)).getUserInfo();
        verify(userInfoMock, times(1)).getRoles();
    }

    @Test
    public void testRetrievePaymentAccountByUserEmailFromHeader() {
        final HttpStatus expectedHttpStatus = HttpStatus.OK;

        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(httpRequest));
        List<String> authorities = new ArrayList<>();
        authorities.add(TestConstants.PUI_USER_MANAGER);
        String email = "test@email.com";
        when(httpRequest.getHeader(anyString())).thenReturn(email);
        when(jwtGrantedAuthoritiesConverterMock.getUserInfo()).thenReturn(userInfoMock);
        when(userInfoMock.getRoles()).thenReturn(authorities);
        when(paymentAccountServiceMock.findPaymentAccountsByEmail(email)).thenReturn(organisation);
        ResponseEntity<?> actual = organisationExternalController.retrievePaymentAccountByEmail(" ",
                UUID.randomUUID().toString().substring(0, 7));

        assertThat(actual).isNotNull();
        assertThat(actual.getStatusCode()).isEqualTo(expectedHttpStatus);
        verify(paymentAccountServiceMock, times(1)).findPaymentAccountsByEmail(email);
        verify(httpRequest, times(2)).getHeader(anyString());
        verify(jwtGrantedAuthoritiesConverterMock, times(1)).getUserInfo();
        verify(userInfoMock, times(1)).getRoles();

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

        ResponseEntity<?> actual = organisationExternalController
                .addUserToOrganisationUsingExternalController(newUserCreationRequest, orgId, userId);

        assertThat(actual).isNotNull();
        assertThat(actual.getStatusCode()).isEqualTo(expectedHttpStatus);

        verify(organisationServiceMock, times(1))
                .getOrganisationByOrgIdentifier(orgId);
        verify(professionalUserServiceMock, times(1))
                .findProfessionalUserByEmailAddress("some@email.com");
    }

    @Test
    public void test_retrieveOrganisationsByStatusWithMinimalInfo_should_return_200_with_response() {
        ReflectionTestUtils.setField(organisationExternalController, "allowedOrganisationStatus", ACTIVE.name());
        List<Organisation> organisations = new ArrayList<>();
        organisations.add(organisation1);
        OrganisationMinimalInfoResponse organisationMinimalInfoResponse = new OrganisationMinimalInfoResponse(
                organisation1, true);
        when(organisationServiceMock.getOrganisationByStatus(any())).thenReturn(organisations);

        ResponseEntity<List<OrganisationMinimalInfoResponse>> responseEntity =
                organisationExternalController.retrieveOrganisationsByStatusWithAddressDetailsOptional(
                        UUID.randomUUID().toString(), ACTIVE.name(), true);
        List<OrganisationMinimalInfoResponse> minimalInfoResponseList = responseEntity.getBody();
        assertThat(minimalInfoResponseList).usingFieldByFieldElementComparator()
                .contains(organisationMinimalInfoResponse);
        assertThat(responseEntity.getStatusCodeValue()).isEqualTo(200);
        verify(organisationServiceMock, times(1)).getOrganisationByStatus(any());

    }

    @Test
    public void test_retrieveAllOrganisationsByStatus_should_return_404_when_no_active_orgs_found() {
        ReflectionTestUtils.setField(organisationExternalController, "allowedOrganisationStatus", ACTIVE.name());
        when(organisationServiceMock.getOrganisationByStatus(any())).thenReturn(new ArrayList<>());
        Throwable raisedException = catchThrowable(() -> organisationExternalController
                .retrieveOrganisationsByStatusWithAddressDetailsOptional(UUID.randomUUID().toString(),
                        ACTIVE.name(), true));
        assertThat(raisedException).isExactlyInstanceOf(ResourceNotFoundException.class)
                .hasMessageStartingWith("No Organisations found");
        verify(organisationServiceMock, times(1)).getOrganisationByStatus(any());
    }
}