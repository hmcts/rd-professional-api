package uk.gov.hmcts.reform.professionalapi.controller.external;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import feign.Request;
import feign.Response;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;
import uk.gov.hmcts.reform.professionalapi.controller.advice.ResourceNotFoundException;
import uk.gov.hmcts.reform.professionalapi.controller.constants.TestConstants;
import uk.gov.hmcts.reform.professionalapi.controller.feign.UserProfileFeignClient;
import uk.gov.hmcts.reform.professionalapi.controller.request.ContactInformationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.DeleteMultipleAddressRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.DxAddressCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.InvalidRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.NewUserCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationOtherOrgsCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.PbaRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.UserCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.UserProfileCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.validator.OrganisationCreationRequestValidator;
import uk.gov.hmcts.reform.professionalapi.controller.request.validator.PaymentAccountValidator;
import uk.gov.hmcts.reform.professionalapi.controller.request.validator.impl.OrganisationIdentifierValidatorImpl;
import uk.gov.hmcts.reform.professionalapi.controller.response.ContactInformationResponseWithDxAddress;
import uk.gov.hmcts.reform.professionalapi.controller.response.OrganisationEntityResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.OrganisationMinimalInfoResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.OrganisationResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.UserProfileCreationResponse;
import uk.gov.hmcts.reform.professionalapi.domain.AddPbaResponse;
import uk.gov.hmcts.reform.professionalapi.domain.ContactInformation;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus;
import uk.gov.hmcts.reform.professionalapi.domain.PaymentAccount;
import uk.gov.hmcts.reform.professionalapi.domain.PrdEnum;
import uk.gov.hmcts.reform.professionalapi.domain.PrdEnumId;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;
import uk.gov.hmcts.reform.professionalapi.domain.SuperUser;
import uk.gov.hmcts.reform.professionalapi.domain.UserAccessType;
import uk.gov.hmcts.reform.professionalapi.repository.IdamRepository;
import uk.gov.hmcts.reform.professionalapi.repository.PrdEnumRepository;
import uk.gov.hmcts.reform.professionalapi.service.OrganisationService;
import uk.gov.hmcts.reform.professionalapi.service.PaymentAccountService;
import uk.gov.hmcts.reform.professionalapi.service.ProfessionalUserService;
import uk.gov.hmcts.reform.professionalapi.service.impl.PrdEnumServiceImpl;
import uk.gov.hmcts.reform.professionalapi.util.RefDataUtil;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.LENGTH_OF_ORGANISATION_IDENTIFIER;
import static uk.gov.hmcts.reform.professionalapi.domain.LanguagePreference.EN;
import static uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus.ACTIVE;
import static uk.gov.hmcts.reform.professionalapi.domain.UserCategory.PROFESSIONAL;
import static uk.gov.hmcts.reform.professionalapi.domain.UserType.EXTERNAL;
import static uk.gov.hmcts.reform.professionalapi.generator.ProfessionalApiGenerator.generateUniqueAlphanumericId;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
class OrganisationExternalControllerTest {

    @InjectMocks
    private OrganisationExternalController organisationExternalController;

    private OrganisationOtherOrgsCreationRequest organisationOtherOrgsCreationRequest;

    @InjectMocks
    private OrganisationExternalControllerV2 organisationExternalControllerV2;

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
    private IdamRepository idamRepositoryMock;
    private Authentication authentication;
    private SecurityContext securityContext;
    private UserInfo userInfoMock;
    RefDataUtil refDataUtilMock;
    private PaymentAccountValidator paymentAccountValidator;

    HttpServletRequest httpRequest = mock(HttpServletRequest.class);
    private final PrdEnumId prdEnumId1 = new PrdEnumId(10, "JURISD_ID");
    private final PrdEnumId prdEnumId2 = new PrdEnumId(13, "JURISD_ID");
    private final PrdEnumId prdEnumId3 = new PrdEnumId(3, "PRD_ROLE");

    private final PrdEnum anEnum1 = new PrdEnum(prdEnumId1, "PROBATE", "JURISD_ID");
    private final PrdEnum anEnum2 = new PrdEnum(prdEnumId2, "BULKSCAN", "JURISD_ID");
    private final PrdEnum anEnum3 = new PrdEnum(prdEnumId3, "pui-case-manager", "PRD_ROLE");

    private static final String USER_JWT = "Bearer 8gf364fg367f67";

    private List<PrdEnum> prdEnumList;

    @BeforeEach
    void setUp() {
        organisationCreationRequestValidatorMock = mock(OrganisationCreationRequestValidator.class);
        organisationServiceMock = mock(OrganisationService.class);
        organisationIdentifierValidatorImplMock = mock(OrganisationIdentifierValidatorImpl.class);
        professionalUserServiceMock = mock(ProfessionalUserService.class);
        paymentAccountServiceMock = mock(PaymentAccountService.class);
        prdEnumServiceMock = mock(PrdEnumServiceImpl.class);
        prdEnumRepository = mock(PrdEnumRepository.class);
        userProfileFeignClient = mock(UserProfileFeignClient.class);
        idamRepositoryMock = mock(IdamRepository.class);
        authentication = Mockito.mock(Authentication.class);
        securityContext = mock(SecurityContext.class);
        userInfoMock = mock(UserInfo.class);
        paymentAccountValidator = mock(PaymentAccountValidator.class);

        organisation = new Organisation("Org-Name", OrganisationStatus.PENDING, "sra-id",
                "companyN", false, "www.org.com");

        PaymentAccount paymentAccount = new PaymentAccount();
        paymentAccount.setPbaNumber("PBA1234567");
        organisation.addPaymentAccount(paymentAccount);

        organisation1 = new Organisation("Org-Name2", OrganisationStatus.ACTIVE, "sra-id2",
                "companyN2", false, "www2.org.com");
        organisationResponse = new OrganisationResponse(organisation);
        professionalUser = new ProfessionalUser("some-fname", "some-lname",
                "soMeone@somewhere.com", organisation);
        SuperUser superUser = new SuperUser("some-fname", "some-lname",
                "some-email-address", organisation);
        organisationEntityResponse = new OrganisationEntityResponse(organisation, false, true, true);


        prdEnumList = new ArrayList<>();
        prdEnumList.add(anEnum1);
        prdEnumList.add(anEnum2);
        prdEnumList.add(anEnum3);

        refDataUtilMock = mock(RefDataUtil.class);

        List<String> userRoles = new ArrayList<>();
        userRoles.add("pui-user-manager");

        HashSet<UserAccessType> userAccessTypes = new HashSet<>();
        userAccessTypes.add(new UserAccessType("jurisdictionId", "organisationProfileId", "accessTypeId", false));

        newUserCreationRequest = new NewUserCreationRequest("some-name", "some-last-name",
                "some@email.com", userRoles, false, userAccessTypes);
        userCreationRequest = new UserCreationRequest("some-fname", "some-lname",
                "some@email.com");
        organisationCreationRequest = new OrganisationCreationRequest("test", "PENDING", null,
                "sra-id", "false", "number02", "company-url",
                userCreationRequest, null, null);
        organisationOtherOrgsCreationRequest = new OrganisationOtherOrgsCreationRequest("test", "PENDING", null,
                "sra-id", "false", "number02", "company-url",
                userCreationRequest, null, null,"Doctor",null);
        userProfileCreationRequest = new UserProfileCreationRequest("some@email.com",
                "some-name", "some-last-name", EN, PROFESSIONAL, EXTERNAL, userRoles,
                false);
        response = Response.builder().status(200).reason("OK").body(mock(Response.Body.class))
                .request(mock(Request.class)).build();

        MockitoAnnotations.openMocks(this);
    }

    @Test
    void test_CreateOrganisation() {
        when(organisationServiceMock.createOrganisationFrom(organisationCreationRequest))
                .thenReturn(organisationResponse);

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
    void test_CreateOrganisationV2() {
        when(organisationServiceMock.createOrganisationFrom(organisationOtherOrgsCreationRequest))
                .thenReturn(organisationResponse);

        ResponseEntity<?> actual = organisationExternalControllerV2
                .createOrganisationUsingExternalController(organisationOtherOrgsCreationRequest);

        verify(organisationCreationRequestValidatorMock, times(1))
                .validate(any(OrganisationOtherOrgsCreationRequest.class));
        verify(organisationServiceMock, times(1))
                .createOrganisationFrom(organisationOtherOrgsCreationRequest);

        assertThat(actual).isNotNull();
        assertThat(actual.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @Test
    void test_RetrieveOrganisationByIdentifier() {
        final HttpStatus expectedHttpStatus = HttpStatus.OK;

        String id = UUID.randomUUID().toString().substring(0, 7);
        when(organisationServiceMock.retrieveOrganisation(id, true)).thenReturn(organisationEntityResponse);

        ResponseEntity<?> actual = organisationExternalController.retrieveOrganisationUsingOrgIdentifier(id, "");

        assertThat(actual).isNotNull();
        assertThat(actual.getStatusCode()).isEqualTo(expectedHttpStatus);

        verify(organisationServiceMock, times(1)).retrieveOrganisation(eq(id), any(boolean.class));
    }

    @Test
    void test_RetrievePaymentAccountByUserEmail() {
        final HttpStatus expectedHttpStatus = HttpStatus.OK;
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(httpRequest));

        List<String> authorities = new ArrayList<>();
        authorities.add(TestConstants.PUI_USER_MANAGER);
        String email = "test@email.com";
        when(httpRequest.getHeader(anyString())).thenReturn(email);
        when(idamRepositoryMock.getUserInfo(anyString())).thenReturn(userInfoMock);
        when(userInfoMock.getRoles()).thenReturn(authorities);

        Jwt jwt =   Jwt.withTokenValue(USER_JWT)
                .claim("aClaim", "aClaim")
                .claim("aud", Lists.newArrayList("ccd_gateway"))
                .header("aHeader", "aHeader")
                .build();
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication().getPrincipal()).thenReturn(jwt);

        when(paymentAccountServiceMock.findPaymentAccountsByEmail(email)).thenReturn(organisation);
        ResponseEntity<?> actual = organisationExternalController.retrievePaymentAccountByEmail(
                UUID.randomUUID().toString().substring(0, 7));

        assertThat(actual).isNotNull();
        assertThat(actual.getStatusCode()).isEqualTo(expectedHttpStatus);
        verify(paymentAccountServiceMock, times(1)).findPaymentAccountsByEmail(email);
        verify(httpRequest, times(2)).getHeader(anyString());
        verify(idamRepositoryMock, times(1)).getUserInfo(anyString());
        verify(userInfoMock, times(1)).getRoles();
    }

    @Test
    void testRetrievePaymentAccountByUserEmailFromHeader() {
        final HttpStatus expectedHttpStatus = HttpStatus.OK;
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(httpRequest));
        List<String> authorities = new ArrayList<>();
        authorities.add(TestConstants.PUI_USER_MANAGER);
        String email = "test@email.com";
        when(httpRequest.getHeader(anyString())).thenReturn(email);
        when(idamRepositoryMock.getUserInfo(anyString())).thenReturn(userInfoMock);
        when(userInfoMock.getRoles()).thenReturn(authorities);
        when(paymentAccountServiceMock.findPaymentAccountsByEmail(email)).thenReturn(organisation);
        ResponseEntity<?> actual = organisationExternalController.retrievePaymentAccountByEmail(
                UUID.randomUUID().toString().substring(0, 7));

        assertThat(actual).isNotNull();
        assertThat(actual.getStatusCode()).isEqualTo(expectedHttpStatus);
        verify(paymentAccountServiceMock, times(1)).findPaymentAccountsByEmail(email);
        verify(httpRequest, times(2)).getHeader(anyString());
        verify(idamRepositoryMock, times(1)).getUserInfo(anyString());
        verify(userInfoMock, times(1)).getRoles();

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
    void test_retrieveOrganisationsByStatusWithMinimalInfo_should_return_200_with_response() {
        ReflectionTestUtils.setField(organisationExternalController, "allowedOrganisationStatus", ACTIVE.name());
        List<Organisation> organisations = new ArrayList<>();
        organisations.add(organisation1);
        OrganisationMinimalInfoResponse organisationMinimalInfoResponse = new OrganisationMinimalInfoResponse(
                organisation1, true);
        when(organisationServiceMock.getOrganisationByStatus(any())).thenReturn(organisations);

        ResponseEntity<List<OrganisationMinimalInfoResponse>> responseEntity =
                organisationExternalController.retrieveOrganisationsByStatusWithAddressDetailsOptional(
                        ACTIVE.name(), true);
        List<OrganisationMinimalInfoResponse> minimalInfoResponseList = responseEntity.getBody();
        assertThat(minimalInfoResponseList).usingRecursiveFieldByFieldElementComparator()
                .contains(organisationMinimalInfoResponse);
        assertThat(responseEntity.getStatusCode().value()).isEqualTo(200);
        verify(organisationServiceMock, times(1)).getOrganisationByStatus(any());

    }

    @Test
    void test_retrieveAllOrganisationsByStatus_should_return_404_when_no_active_orgs_found() {
        ReflectionTestUtils.setField(organisationExternalController, "allowedOrganisationStatus", ACTIVE.name());
        when(organisationServiceMock.getOrganisationByStatus(any())).thenReturn(new ArrayList<>());
        Throwable raisedException = catchThrowable(() -> organisationExternalController
                .retrieveOrganisationsByStatusWithAddressDetailsOptional(ACTIVE.name(), true));
        assertThat(raisedException).isExactlyInstanceOf(ResourceNotFoundException.class)
                .hasMessageStartingWith("No Organisations found");
        verify(organisationServiceMock, times(1)).getOrganisationByStatus(any());
    }

    @Test
    void testDeletePaymentAccounts() {
        PbaRequest deletePbaRequest = new PbaRequest();
        var accountsToDelete = new HashSet<String>();
        accountsToDelete.add("PBA1234567");
        deletePbaRequest.setPaymentAccounts(accountsToDelete);
        when(organisationServiceMock.getOrganisationByOrgIdentifier(anyString())).thenReturn(organisation);

        String orgId = UUID.randomUUID().toString().substring(0, 7);
        String userId = UUID.randomUUID().toString();
        organisationExternalController
                .deletePaymentAccountsOfOrganisation(deletePbaRequest, orgId, userId);

        verify(professionalUserServiceMock, times(1))
                .checkUserStatusIsActiveByUserId(anyString());
        verify(organisationIdentifierValidatorImplMock, times(1))
                .validateOrganisationIsActive(any(Organisation.class), any(HttpStatus.class));
        verify(paymentAccountServiceMock, times(1))
                .deletePaymentsOfOrganisation(any(PbaRequest.class), any(Organisation.class));

    }

    @Test
    void test_deletePaymentAccounts_NoPaymentAccountsPassed() {
        PbaRequest deletePbaRequest = new PbaRequest();
        var accountsToDelete = new HashSet<String>();
        deletePbaRequest.setPaymentAccounts(accountsToDelete);
        String orgId = UUID.randomUUID().toString().substring(0, 7);
        String userId = UUID.randomUUID().toString();
        assertThrows(InvalidRequest.class,() ->
                organisationExternalController
                .deletePaymentAccountsOfOrganisation(deletePbaRequest, orgId, userId));

    }

    @Test
    void test_addPaymentAccountsToOrganisation() throws JsonProcessingException {
        Set<String> pbas = new HashSet<>();
        pbas.add("PBA0000001");
        PbaRequest pbaRequest = new PbaRequest();
        pbaRequest.setPaymentAccounts(pbas);
        AddPbaResponse addPbaResponse = new AddPbaResponse();
        addPbaResponse.setMessage("");
        ResponseEntity<Object> responseEntity = ResponseEntity
                .status(200)
                .body(addPbaResponse);

        String orgId = UUID.randomUUID().toString().substring(0, 7);
        String userId = UUID.randomUUID().toString();
        when(organisationServiceMock.addPaymentAccountsToOrganisation(pbaRequest, orgId, userId))
                .thenReturn(responseEntity);

        ResponseEntity<?> actual = organisationExternalController
                .addPaymentAccountsToOrganisation(pbaRequest, orgId, userId);

        final HttpStatus expectedHttpStatus = HttpStatus.OK;
        assertThat(actual).isNotNull();
        assertThat(actual.getStatusCode()).isEqualTo(expectedHttpStatus);

        verify(organisationServiceMock, times(1))
                .addPaymentAccountsToOrganisation(pbaRequest, orgId, userId);

    }

    @Test
    void should_add_contact_informations_to_organisation() {

        List<ContactInformationCreationRequest> contactInformationCreationRequests = new ArrayList<>();
        List<DxAddressCreationRequest> dxAddressRequests = new ArrayList<>();

        DxAddressCreationRequest dxAddressRequest = new DxAddressCreationRequest("DX 1234567890", "dxExchange");
        dxAddressRequests.add(dxAddressRequest);

        ContactInformationCreationRequest contactInformationCreationRequest =
                new ContactInformationCreationRequest("uprn", "addressLine-1",
                "addressLine-2", "addressLine-3", "townCity", "county",
                "country", "postCode", dxAddressRequests);

        contactInformationCreationRequests.add(contactInformationCreationRequest);
        List<ContactInformationResponseWithDxAddress> result = new ArrayList<>();

        final String orgUUId = generateUniqueAlphanumericId(LENGTH_OF_ORGANISATION_IDENTIFIER);

        doNothing().when(organisationServiceMock).addContactInformationsToOrganisation(anyList(), anyString());



        Organisation organisationMock = mock(Organisation.class);
        when(organisationServiceMock.getOrganisationByOrgIdentifier(orgUUId)).thenReturn(organisationMock);

        ResponseEntity<?> actual = organisationExternalController
                .addContactInformationsToOrganisation(contactInformationCreationRequests, orgUUId);
        verify(organisationServiceMock, times(1)).getOrganisationByOrgIdentifier(anyString());


        assertThat(actual).isNotNull();
        assertThat(actual.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @Test
    void testDeleteMultipleAddressesOfOrganisation() {
        ContactInformation contactInformation01 = new ContactInformation();
        contactInformation01.setAddressLine1("addressLine1");
        UUID uuid = UUID.randomUUID();
        contactInformation01.setId(uuid);

        ContactInformation contactInformation02 = new ContactInformation();
        contactInformation02.setAddressLine1("addressLine2");
        contactInformation02.setId(UUID.randomUUID());

        var contacts = new ArrayList<>(List.of(contactInformation01, contactInformation02));
        organisation.setContactInformations(contacts);

        var addressId = new HashSet<UUID>();
        addressId.add(uuid);

        var deleteMultipleAddressRequest = new DeleteMultipleAddressRequest(uuid.toString());
        var requestArrayList = new ArrayList<>(List.of(deleteMultipleAddressRequest));
        when(organisationServiceMock.getOrganisationByOrgIdentifier(anyString())).thenReturn(organisation);

        String orgId = uuid.toString().substring(0, 7);
        organisationExternalController
                .deleteMultipleAddressesOfOrganisation(requestArrayList, orgId);

        verify(organisationServiceMock, times(1))
                .deleteMultipleAddressOfGivenOrganisation(addressId);

    }

    @Test
    void test_deleteMultipleAddressesOfOrganisation_EmptyAddressIdPassed() {
        var deleteMultipleAddressRequest = new DeleteMultipleAddressRequest("");
        var requestArrayList = new ArrayList<>(List.of(deleteMultipleAddressRequest));
        String orgId = UUID.randomUUID().toString().substring(0, 7);
        assertThrows(InvalidRequest.class,() ->
                organisationExternalController
                        .deleteMultipleAddressesOfOrganisation(requestArrayList, orgId));
    }
}
