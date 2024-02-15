package uk.gov.hmcts.reform.professionalapi.controller.external;

import feign.Request;
import feign.Response;
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
import uk.gov.hmcts.reform.idam.client.models.UserInfo;
import uk.gov.hmcts.reform.professionalapi.controller.feign.UserProfileFeignClient;
import uk.gov.hmcts.reform.professionalapi.controller.request.NewUserCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationOtherOrgsCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.UserCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.UserProfileCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.validator.OrganisationCreationRequestValidator;
import uk.gov.hmcts.reform.professionalapi.controller.request.validator.PaymentAccountValidator;
import uk.gov.hmcts.reform.professionalapi.controller.request.validator.impl.OrganisationIdentifierValidatorImpl;
import uk.gov.hmcts.reform.professionalapi.controller.response.OrganisationEntityResponseV2;
import uk.gov.hmcts.reform.professionalapi.controller.response.OrganisationResponse;
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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.professionalapi.domain.LanguagePreference.EN;
import static uk.gov.hmcts.reform.professionalapi.domain.UserCategory.PROFESSIONAL;
import static uk.gov.hmcts.reform.professionalapi.domain.UserType.EXTERNAL;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
class OrganisationExternalControllerV2Test {

    @InjectMocks
    private OrganisationExternalControllerV2 organisationExternalController;

    private OrganisationResponse organisationResponse;
    private OrganisationEntityResponseV2 organisationEntityResponse;
    private OrganisationService organisationServiceMock;
    private ProfessionalUserService professionalUserServiceMock;
    private PaymentAccountService paymentAccountServiceMock;
    private PrdEnumServiceImpl prdEnumServiceMock;

    private OrganisationOtherOrgsCreationRequest organisationOtherOrgsCreationRequest;
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
        organisationEntityResponse = new OrganisationEntityResponseV2(organisation,
                false, true, true,true);


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
    void test_CreateOrganisationV2() {
        when(organisationServiceMock.createOrganisationFrom(organisationOtherOrgsCreationRequest))
                .thenReturn(organisationResponse);

        ResponseEntity<?> actual = organisationExternalController
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
        when(organisationServiceMock.retrieveOrganisationForV2Api(id, true))
                .thenReturn(organisationEntityResponse);

        ResponseEntity<?> actual = organisationExternalController
                .retrieveOrganisationUsingOrgIdentifier(id, "");

        assertThat(actual).isNotNull();
        assertThat(actual.getStatusCode()).isEqualTo(expectedHttpStatus);

        verify(organisationServiceMock, times(1))
                .retrieveOrganisationForV2Api(eq(id), any(boolean.class));
    }

}
