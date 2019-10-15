package uk.gov.hmcts.reform.professionalapi.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.when;
import static uk.gov.hmcts.reform.professionalapi.generator.ProfessionalApiGenerator.LENGTH_OF_ORGANISATION_IDENTIFIER;
import static uk.gov.hmcts.reform.professionalapi.generator.ProfessionalApiGenerator.generateUniqueAlphanumericId;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import feign.Request;
import feign.Response;

import java.nio.charset.Charset;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.assertj.core.api.Assertions;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.Before;
import org.junit.Test;

import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.dao.EmptyResultDataAccessException;

import uk.gov.hmcts.reform.professionalapi.controller.feign.UserProfileFeignClient;
import uk.gov.hmcts.reform.professionalapi.controller.request.ContactInformationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.DxAddressCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.InvalidRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.Jurisdiction;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.RetrieveUserProfilesRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.UserCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.response.GetUserProfileResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.IdamStatus;
import uk.gov.hmcts.reform.professionalapi.controller.response.OrganisationEntityResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.OrganisationResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.OrganisationsDetailResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.ProfessionalUsersEntityResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.ProfessionalUsersResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.UserProfile;
import uk.gov.hmcts.reform.professionalapi.domain.ContactInformation;
import uk.gov.hmcts.reform.professionalapi.domain.DxAddress;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus;
import uk.gov.hmcts.reform.professionalapi.domain.PaymentAccount;
import uk.gov.hmcts.reform.professionalapi.domain.PrdEnum;
import uk.gov.hmcts.reform.professionalapi.domain.PrdEnumId;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;
import uk.gov.hmcts.reform.professionalapi.domain.SuperUser;
import uk.gov.hmcts.reform.professionalapi.domain.UserAccountMap;
import uk.gov.hmcts.reform.professionalapi.domain.UserAttribute;
import uk.gov.hmcts.reform.professionalapi.persistence.ContactInformationRepository;
import uk.gov.hmcts.reform.professionalapi.persistence.DxAddressRepository;
import uk.gov.hmcts.reform.professionalapi.persistence.OrganisationRepository;
import uk.gov.hmcts.reform.professionalapi.persistence.PaymentAccountRepository;
import uk.gov.hmcts.reform.professionalapi.persistence.PrdEnumRepository;
import uk.gov.hmcts.reform.professionalapi.persistence.ProfessionalUserRepository;
import uk.gov.hmcts.reform.professionalapi.persistence.UserAccountMapRepository;
import uk.gov.hmcts.reform.professionalapi.persistence.UserAttributeRepository;
import uk.gov.hmcts.reform.professionalapi.service.impl.OrganisationServiceImpl;

@RunWith(MockitoJUnitRunner.class)
//@ContextConfiguration (loader = Annotation)
public class OrganisationServiceImplTest {

    private final ProfessionalUserRepository professionalUserRepositoryMock = mock(ProfessionalUserRepository.class);
    private final PaymentAccountRepository paymentAccountRepositoryMock = mock(PaymentAccountRepository.class);
    private final UserAccountMapRepository userAccountMapRepositoryMock = mock(UserAccountMapRepository.class);
    private final OrganisationRepository organisationRepositoryMock = mock(OrganisationRepository.class);
    private final ContactInformationRepository contactInformationRepositoryMock = mock(ContactInformationRepository.class);
    private final DxAddressRepository dxAddressRepositoryMock = mock(DxAddressRepository.class);
    private OrganisationServiceImpl organisationServiceImplMock = mock(OrganisationServiceImpl.class);
    private final UserAttributeRepository userAttributeRepositoryMock = mock(UserAttributeRepository.class);
    private final PrdEnumRepository prdEnumRepositoryMock = mock(PrdEnumRepository.class);
    private final ProfessionalUser professionalUserMock = mock(ProfessionalUser.class);
    private final SuperUser superUserMock = mock(SuperUser.class);
    private final Organisation organisationMock = mock(Organisation.class);
    private final PaymentAccount paymentAccountMock = mock(PaymentAccount.class);
    private final ContactInformation contactInformationMock = mock(ContactInformation.class);
    private final DxAddress dxAddressMock = mock(DxAddress.class);
    private final UserAccountMap userAccountMapMock = mock(UserAccountMap.class);
    private final OrganisationRepository organisationRepositoryNullReturnedMock = mock(OrganisationRepository.class);
    private final String organisationIdentifier = generateUniqueAlphanumericId(LENGTH_OF_ORGANISATION_IDENTIFIER);
    private UserAttributeService userAttributeServiceMock = mock(UserAttributeService.class);


    private final PrdEnumId prdEnumId = mock(PrdEnumId.class);

    private final UserAttribute userAttributeMock = mock(UserAttribute.class);
    private List<String> userRoles = new ArrayList<>();
    private List<PrdEnum> prdEnums = new ArrayList<>();

    private UserCreationRequest superUser;
    private List<ContactInformationCreationRequest> contactInformationCreationRequests;
    private List<DxAddressCreationRequest> dxAddressRequests;
    private DxAddressCreationRequest dxAddressRequest;
    private ContactInformationCreationRequest contactInformationCreationRequest;
    private OrganisationCreationRequest organisationCreationRequest;
    private List<Organisation> organisations;
    private List<UserAccountMap> userAccountMaps;
    private List<PaymentAccount> paymentAccounts;

    private final UserProfileFeignClient userProfileFeignClient = mock(UserProfileFeignClient.class);

    @Mock
    private UserProfileFeignClient userProfileFeignClientMock;

    @Mock
    private PrdEnumService prdEnumServiceMock;

    //@InjectMocks
    private OrganisationService sut = new OrganisationServiceImpl(organisationRepositoryMock, professionalUserRepositoryMock, paymentAccountRepositoryMock,
            dxAddressRepositoryMock, contactInformationRepositoryMock, userAttributeRepositoryMock, prdEnumRepositoryMock);

    @Before
    public void setUp() {

        MockitoAnnotations.initMocks(this);

        Jurisdiction jurisdiction = new Jurisdiction();
        jurisdiction.setId("PROBATE");

        List<Jurisdiction> jurisdictionIds = new ArrayList<>();
        jurisdictionIds.add(jurisdiction);

        superUser = new UserCreationRequest(
                "some-fname",
                "some-lname",
                "some-email",
                jurisdictionIds
        );

        List<String> paymentAccountList = new ArrayList<>();

        String pbaNumber = "PBA1234567";

        paymentAccountList.add(pbaNumber);

        contactInformationCreationRequests = new ArrayList<>();

        dxAddressRequests = new ArrayList<>();

        organisations = new ArrayList<Organisation>();

        paymentAccounts = new ArrayList<PaymentAccount>();

        userAccountMaps = new ArrayList<UserAccountMap>();

        dxAddressRequest = new DxAddressCreationRequest("DX 1234567890", "dxExchange");

        contactInformationCreationRequest = new ContactInformationCreationRequest(
                "addressLine-1",
                "addressLine-2",
                "addressLine-3",
                "townCity",
                "county",
                "country",
                "postCode",
                dxAddressRequests);

        dxAddressRequests.add(dxAddressRequest);

        contactInformationCreationRequests.add(contactInformationCreationRequest);

        organisationServiceImplMock = new OrganisationServiceImpl(
                organisationRepositoryMock,
                professionalUserRepositoryMock,
                paymentAccountRepositoryMock,
                dxAddressRepositoryMock,
                contactInformationRepositoryMock,
                userAttributeRepositoryMock, prdEnumRepositoryMock
                );


        organisationCreationRequest =
                new OrganisationCreationRequest(
                        "some-org-name", "PENDING", "sra-id", "false", "number01", "company-url",
                        superUser,
                        paymentAccountList, contactInformationCreationRequests);



        when(organisationMock.getId()).thenReturn(UUID.randomUUID());

        when(organisationMock.getPaymentAccounts()).thenReturn(paymentAccounts);

        when(organisationMock.getOrganisationIdentifier()).thenReturn(generateUniqueAlphanumericId(LENGTH_OF_ORGANISATION_IDENTIFIER));

        when(professionalUserRepositoryMock.save(any(ProfessionalUser.class)))
                .thenReturn(professionalUserMock);

        when(organisationRepositoryMock.save(any(Organisation.class)))
                .thenReturn(organisationMock);

        when(paymentAccountRepositoryMock.save(any(PaymentAccount.class)))
                .thenReturn(paymentAccountMock);

        when(paymentAccountRepositoryMock.findAll())
                .thenReturn(paymentAccounts);

        paymentAccounts.add(paymentAccountMock);

        when(userAccountMapRepositoryMock.save(any(UserAccountMap.class)))
                .thenReturn(userAccountMapMock);

        when(contactInformationRepositoryMock.save(any(ContactInformation.class)))
                .thenReturn(contactInformationMock);

        when(dxAddressRepositoryMock.save(any(DxAddress.class)))
                .thenReturn(dxAddressMock);

        when(organisationRepositoryMock.findAll())
                .thenReturn(organisations);

        when(organisationRepositoryMock.findByOrganisationIdentifier(any()))
                .thenReturn(organisationMock);

        when(organisationRepositoryNullReturnedMock.findByOrganisationIdentifier(any()))
                .thenReturn(null);

        when(organisationRepositoryMock.findByStatus(any()))
                .thenReturn(organisations);

        when(userAccountMapRepositoryMock.findAll())
                .thenReturn(userAccountMaps);
    }

    @Test
    public void testSavesAnOrganisation() {
        prdEnums.add(new PrdEnum(new PrdEnumId(0, "SIDAM_ROLE"), "pui-user-manager", "SIDAM_ROLE"));
        prdEnums.add(new PrdEnum(new PrdEnumId(4, "ADMIN_ROLE"), "organisation-admin", "ADMIN_ROLE"));
        prdEnums.add(new PrdEnum(new PrdEnumId(10, "JURISD_ID"), "PROBATE", "PROBATE"));
        prdEnums.add(new PrdEnum(new PrdEnumId(10, "JURISD_ID"), "PROBATE", "PROBATE"));

        when(prdEnumServiceMock.findAllPrdEnums()).thenReturn(prdEnums);

        SuperUser superUserMock = mock(SuperUser.class);

        when(professionalUserMock.toSuperUser()).thenReturn(superUserMock);

        OrganisationResponse organisationResponse =
                organisationServiceImplMock.createOrganisationFrom(organisationCreationRequest);

        assertThat(organisationResponse).isNotNull();

        verify(
                organisationRepositoryMock,
                times(1)).save(any(Organisation.class));
        verify(
                professionalUserRepositoryMock,
                times(1)).save(any(ProfessionalUser.class));
        verify(
                paymentAccountRepositoryMock,
                times(1)).save(any());
        verify(
                contactInformationRepositoryMock,
                times(1)).save(any(ContactInformation.class));
        verify(
                dxAddressRepositoryMock,
                times(1)).saveAll(any());
        verify(
                organisationMock,
                times(1)).addProfessionalUser(superUserMock);
        verify(
                userAccountMapRepositoryMock,
                times(1)).saveAll(any());

    }

    @Test
    public void testSavesOrganisationWithInvalidRequest() {

        when(organisationRepositoryMock.save(any(Organisation.class)))
                .thenThrow(ConstraintViolationException.class);

        Assertions.assertThatThrownBy(() -> organisationServiceImplMock.createOrganisationFrom(organisationCreationRequest))
                .isExactlyInstanceOf(ConstraintViolationException.class);

        verify(
                organisationMock,
                times(0)).setOrganisationIdentifier(generateUniqueAlphanumericId(LENGTH_OF_ORGANISATION_IDENTIFIER));

        verify(
                organisationRepositoryMock,
                times(2)).save(any(Organisation.class));

        organisationMock.setOrganisationIdentifier("1XCDFG3");
        assertThat(organisationMock.getOrganisationIdentifier()).isNotNull();
    }

    @Test
    public void testUpdatesAnOrganisation() {
        OrganisationResponse organisationResponse =
                organisationServiceImplMock.updateOrganisation(organisationCreationRequest, organisationIdentifier);

        assertThat(organisationResponse).isNotNull();
        verify(
                organisationRepositoryMock,
                times(1)).findByOrganisationIdentifier(any());

        verify(
                organisationRepositoryMock,
                times(1)).save(any(Organisation.class));

        verify(
                organisationMock,
                times(1)).setName(any());

        verify(
                organisationMock,
                times(1)).setStatus(any());

        verify(
                organisationMock,
                times(1)).setSraId(any());

        verify(
                organisationMock,
                times(1)).setCompanyNumber(any());

        verify(
                organisationMock,
                times(1)).setSraRegulated(any());

        verify(
                organisationMock,
                times(1)).setCompanyUrl(any());
    }

    @Test(expected = EmptyResultDataAccessException.class)
    public void retrieve_an_organisations_by_status() throws Exception {

        OrganisationsDetailResponse organisationDetailResponse =
                organisationServiceImplMock.findByOrganisationStatus(OrganisationStatus.ACTIVE);

        assertThat(organisationDetailResponse).isEqualTo(404);

        verify(
                organisationRepositoryMock,
                times(1)).findByStatus(any());

    }


    @Test(expected = EmptyResultDataAccessException.class)
    public void throwsEmptyResultDataAccessException() throws Exception {

        Organisation testOrganisation = new Organisation();
        testOrganisation.setId(UUID.randomUUID());
        String testOrganisationId = testOrganisation.getOrganisationIdentifier();

        OrganisationService realOrganisationService = new OrganisationServiceImpl(organisationRepositoryNullReturnedMock,
                professionalUserRepositoryMock,
                paymentAccountRepositoryMock,
                dxAddressRepositoryMock, contactInformationRepositoryMock,
                userAttributeRepositoryMock,
                prdEnumRepositoryMock);

        realOrganisationService.retrieveOrganisation(testOrganisationId);
    }

    @Test(expected = EmptyResultDataAccessException.class)
    public void testRetrieveAllOrganisationsThrowExceptionWhenOrganisationEmpty()throws Exception {

        ProfessionalUser user = mock(ProfessionalUser.class);
        String id = UUID.randomUUID().toString();

        when(user.getUserIdentifier()).thenReturn(id);
        List<String> users = new ArrayList<>();
        users.add(id.toString());
        List<Organisation> pendOrganisations = new ArrayList<>();
        pendOrganisations.add(organisationMock);

        UserProfile profile = new UserProfile(UUID.randomUUID().toString(), "email@org.com", "firstName", "lastName", IdamStatus.ACTIVE);

        GetUserProfileResponse userProfileResponse = new GetUserProfileResponse(profile, false);

        ObjectMapper mapper = new ObjectMapper();

        String body = mapper.writeValueAsString(userProfileResponse);

        when(userProfileFeignClient.getUserProfileById(anyString())).thenReturn(Response.builder().request(mock(Request.class)).body(body, Charset.defaultCharset()).status(200).build());

        OrganisationsDetailResponse organisationsDetailResponse =
                organisationServiceImplMock.retrieveOrganisations();

        assertThat(organisationsDetailResponse).isNull();

        verify(
                organisationRepositoryMock,
                times(1)).findByStatus(OrganisationStatus.PENDING);

        verify(organisationRepositoryMock, times(1)).findByStatus(OrganisationStatus.ACTIVE);

    }

    @Test
    public void testRetrieveAnOrganisationsByOrgIdentifier() throws Exception {

        SuperUser user = mock(SuperUser.class);

        String id = UUID.randomUUID().toString();
        when(user.getUserIdentifier()).thenReturn(id);
        ProfessionalUser userProf = mock(ProfessionalUser.class);
        when(user.toProfessionalUser()).thenReturn(userProf);
        when(userProf.getUserIdentifier()).thenReturn(id);

        List<SuperUser> users = new ArrayList<>();
        users.add(user);
        ProfessionalUser professionalUser = mock(ProfessionalUser.class);
        when(organisationRepositoryMock.findByOrganisationIdentifier(organisationIdentifier)).thenReturn(organisationMock);
        when(organisationMock.getStatus()).thenReturn(OrganisationStatus.ACTIVE);
        when(organisationMock.getUsers()).thenReturn(users);
        when(professionalUser.getUserIdentifier()).thenReturn(id);
        when(user.toProfessionalUser()).thenReturn(professionalUser);

        UserProfile profile = new UserProfile(UUID.randomUUID().toString(), "email@org.com", "firstName", "lastName", IdamStatus.ACTIVE);

        GetUserProfileResponse userProfileResponse = new GetUserProfileResponse(profile, false);

        ObjectMapper mapper = new ObjectMapper();

        String body = mapper.writeValueAsString(userProfileResponse);

        when(userProfileFeignClient.getUserProfileById(anyString())).thenReturn(Response.builder().request(Request.create(Request.HttpMethod.POST, "", new HashMap<>(), Request.Body.empty()))
                .body(body, Charset.defaultCharset()).status(200).build());


        OrganisationEntityResponse organisationEntityResponse =
                organisationServiceImplMock.retrieveOrganisation(organisationIdentifier);

        assertThat(organisationEntityResponse).isNotNull();
        verify(
                organisationRepositoryMock,
                times(1)).findByOrganisationIdentifier(any());

        verify(
                organisationMock,
                times(1)).setUsers(any());
    }

    @Test
    public void testRetrieveAnOrganisationsByWhenStatusActive() throws Exception {

        SuperUser user = mock(SuperUser.class);

        String id = UUID.randomUUID().toString();
        List<String> ids = new ArrayList<>();
        when(user.getUserIdentifier()).thenReturn(id);
        ids.add(id);
        RetrieveUserProfilesRequest retrieveUserProfilesRequest = new RetrieveUserProfilesRequest(ids);
        List<SuperUser> users = new ArrayList<>();
        users.add(user);
        when(organisationMock.getStatus()).thenReturn(OrganisationStatus.ACTIVE);
        when(organisationMock.getUsers()).thenReturn(users);
        List<Organisation> organisations = new ArrayList<>();
        organisations.add(organisationMock);

        when(organisationRepositoryMock.findByStatus(OrganisationStatus.ACTIVE)).thenReturn(organisations);

        ProfessionalUsersEntityResponse professionalUsersEntityResponse = new ProfessionalUsersEntityResponse();
        List<ProfessionalUsersResponse> userProfiles = new ArrayList<>();

        ProfessionalUser profile = new ProfessionalUser("firstName", "lastName", "email@org.com", organisationMock);

        ProfessionalUsersResponse userProfileResponse = new ProfessionalUsersResponse(profile);
        userProfileResponse.setUserIdentifier(id);
        userProfiles.add(userProfileResponse);
        professionalUsersEntityResponse.getUserProfiles().addAll(userProfiles);
        ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        String body = mapper.writeValueAsString(professionalUsersEntityResponse);

        when(userProfileFeignClient.getUserProfiles(any(),any(),any())).thenReturn(Response.builder().request(mock(Request.class)).body(body, Charset.defaultCharset()).status(200).build());
        OrganisationsDetailResponse organisationDetailResponse =
                organisationServiceImplMock.findByOrganisationStatus(OrganisationStatus.ACTIVE);

        assertThat(organisationDetailResponse).isNotNull();

        verify(
                organisationRepositoryMock,
                times(1)).findByStatus(OrganisationStatus.ACTIVE);
        verify(
                organisationMock,
                times(8)).getUsers();
    }

    @Test(expected = EmptyResultDataAccessException.class)
    public void retrieveAnOrganisationByUuidNotFound() {

        Mockito.when(organisationRepositoryMock.findByOrganisationIdentifier(any(String.class)))
                .thenReturn(null);

        organisationServiceImplMock.retrieveOrganisation(organisationIdentifier);
    }

    @Test(expected = EmptyResultDataAccessException.class)
    public void retrieveAnPendingOrganisationThrowExceptionWhenOrgEmpty() {

        OrganisationsDetailResponse organisationDetailResponse =
                organisationServiceImplMock.findByOrganisationStatus(OrganisationStatus.PENDING);

        assertThat(organisationDetailResponse).isNull();

        verify(
                organisationRepositoryMock,
                times(1)).findByStatus(OrganisationStatus.PENDING);
    }


    @Test
    public void retrieveAnPendingOrganisation() {

        List<Organisation> organisations = new ArrayList<>();
        organisations.add(organisationMock);

        when(organisationRepositoryMock.findByStatus(OrganisationStatus.PENDING)).thenReturn(organisations);
        OrganisationsDetailResponse organisationDetailResponse =
                organisationServiceImplMock.retrieveOrganisations();

        assertThat(organisationDetailResponse).isNotNull();

        verify(
                organisationRepositoryMock,
                times(1)).findByStatus(OrganisationStatus.PENDING);
    }

    @Test
    public void retrieveAnOrganisationByOrgId() {

        Mockito.when(organisationRepositoryMock.findByOrganisationIdentifier(organisationIdentifier))
                .thenReturn(organisationMock);

        Organisation organisation = organisationServiceImplMock.getOrganisationByOrgIdentifier(organisationIdentifier);

        assertThat(organisation).isNotNull();
        verify(organisationRepositoryMock,times(1)).findByOrganisationIdentifier(organisationIdentifier);
    }



    @Test(expected = EmptyResultDataAccessException.class)
    public void testThrowsExceptionWhenOrganisationEmpty() {
        Mockito.when(organisationRepositoryMock.findAll())
                .thenReturn(new ArrayList<Organisation>());
        organisationServiceImplMock.retrieveOrganisations();
    }


    @Test(expected = InvalidRequest.class)
    public void throwInvalidRequestWhenInvalidPbaIsPassed() {

        List<String> paymentAccountList = new ArrayList<>();

        String pbaNumber = "GBA1234567";

        paymentAccountList.add(pbaNumber);

        organisationCreationRequest =
                new OrganisationCreationRequest(
                        "some-org-name", "PENDING", "sra-id", "false", "company-number", "company-url",
                        superUser,
                        paymentAccountList, contactInformationCreationRequests);

        OrganisationResponse organisationResponse =
                organisationServiceImplMock.createOrganisationFrom(organisationCreationRequest);
    }



    @Test
    public void testFakeAttributesNotAdded() {
        prdEnums.add(new PrdEnum(new PrdEnumId(0, "FAKE"), "pui-fake-manager", "FAKE_ROLE"));
        prdEnums.add(new PrdEnum(new PrdEnumId(1, "FAKE_ROLE"), "pui-fake-manager", "FAKE_ROLE"));
        prdEnums.add(new PrdEnum(new PrdEnumId(2, "FAKE_FAKE"), "pui-fake-manager", "FAKE_ROLE"));
        prdEnums.add(new PrdEnum(new PrdEnumId(10, "JURISD_ID"), "pui-fake-manager", "FAKE_ROLE"));
        prdEnums.add(new PrdEnum(new PrdEnumId(10, "FAKE_JURISD"), "PROBATE", "FAKE_ROLE"));


        List<UserAttribute> attributes = new ArrayList<>();
        attributes.add(userAttributeMock);
        when(prdEnumRepositoryMock.findAll()).thenReturn(prdEnums);
        when(prdEnumServiceMock.findAllPrdEnums()).thenReturn(prdEnums);
        when(userAttributeRepositoryMock.saveAll(any())).thenReturn(attributes);

        organisationServiceImplMock.createOrganisationFrom(organisationCreationRequest);

        verify(userAttributeRepositoryMock, times(0)).saveAll(any());
    }
}