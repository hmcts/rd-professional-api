package uk.gov.hmcts.reform.professionalapi.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.LENGTH_OF_ORGANISATION_IDENTIFIER;
import static uk.gov.hmcts.reform.professionalapi.generator.ProfessionalApiGenerator.generateUniqueAlphanumericId;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import feign.Request;
import feign.Response;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.assertj.core.api.Assertions;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.dao.EmptyResultDataAccessException;

import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.professionalapi.controller.advice.ExternalApiException;
import uk.gov.hmcts.reform.professionalapi.controller.constants.IdamStatus;
import uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants;
import uk.gov.hmcts.reform.professionalapi.controller.feign.UserProfileFeignClient;
import uk.gov.hmcts.reform.professionalapi.controller.request.ContactInformationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.DxAddressCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.InvalidRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.UserCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.validator.PaymentAccountValidator;
import uk.gov.hmcts.reform.professionalapi.controller.response.DeleteOrganisationResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.GetUserProfileResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.NewUserResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.OrganisationEntityResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.OrganisationResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.OrganisationsDetailResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.ProfessionalUsersEntityResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.ProfessionalUsersResponse;
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
import uk.gov.hmcts.reform.professionalapi.domain.UserAccountMapId;
import uk.gov.hmcts.reform.professionalapi.domain.UserAttribute;
import uk.gov.hmcts.reform.professionalapi.domain.UserProfile;
import uk.gov.hmcts.reform.professionalapi.domain.OrganisationMfaStatus;
import uk.gov.hmcts.reform.professionalapi.repository.ContactInformationRepository;
import uk.gov.hmcts.reform.professionalapi.repository.DxAddressRepository;
import uk.gov.hmcts.reform.professionalapi.repository.OrganisationRepository;
import uk.gov.hmcts.reform.professionalapi.repository.PaymentAccountRepository;
import uk.gov.hmcts.reform.professionalapi.repository.PrdEnumRepository;
import uk.gov.hmcts.reform.professionalapi.repository.ProfessionalUserRepository;
import uk.gov.hmcts.reform.professionalapi.repository.UserAccountMapRepository;
import uk.gov.hmcts.reform.professionalapi.repository.OrganisationMfaStatusRepository;
import uk.gov.hmcts.reform.professionalapi.service.PrdEnumService;
import uk.gov.hmcts.reform.professionalapi.service.UserAccountMapService;
import uk.gov.hmcts.reform.professionalapi.service.UserAttributeService;

public class OrganisationServiceImplTest {

    private final OrganisationRepository organisationRepository = mock(OrganisationRepository.class);
    private final ProfessionalUserRepository professionalUserRepositoryMock = mock(ProfessionalUserRepository.class);
    private final PaymentAccountRepository paymentAccountRepositoryMock = mock(PaymentAccountRepository.class);
    private final UserAccountMapRepository userAccountMapRepositoryMock = mock(UserAccountMapRepository.class);
    private final ContactInformationRepository contactInformationRepositoryMock
            = mock(ContactInformationRepository.class);
    private final DxAddressRepository dxAddressRepositoryMock = mock(DxAddressRepository.class);
    private final PrdEnumRepository prdEnumRepositoryMock = mock(PrdEnumRepository.class);
    private final OrganisationRepository organisationRepositoryImplNullReturnedMock
            = mock(OrganisationRepository.class);
    private final UserAccountMapService userAccountMapServiceMock = mock(UserAccountMapService.class);
    private final UserAttributeService userAttributeServiceMock = mock(UserAttributeService.class);
    private final UserProfileFeignClient userProfileFeignClient = mock(UserProfileFeignClient.class);
    private final OrganisationMfaStatusRepository organisationMfaStatusRepositoryMock
            = mock(OrganisationMfaStatusRepository.class);

    private final Organisation organisation = new Organisation("some-org-name", null,
            "PENDING", null, null, null);
    private final ProfessionalUser professionalUser = new ProfessionalUser("some-fname",
            "some-lname", "some@hmcts.net", organisation);
    private final PaymentAccount paymentAccount = new PaymentAccount("PBA1234567");
    private final ContactInformation contactInformation = new ContactInformation();
    private final DxAddress dxAddress = new DxAddress("dx-number", "dx-exchange",
            contactInformation);
    private final UserAccountMap userAccountMap = new UserAccountMap();
    private final SuperUser superUser = new SuperUser("some-fname", "some-lname",
            "some-email-address", organisation);
    private final OrganisationMfaStatus organisationMfaStatus = new OrganisationMfaStatus();

    private final String organisationIdentifier = generateUniqueAlphanumericId(LENGTH_OF_ORGANISATION_IDENTIFIER);
    private final PrdEnumService prdEnumService = new PrdEnumServiceImpl(prdEnumRepositoryMock);
    private final PaymentAccountValidator paymentAccountValidator
            = new PaymentAccountValidator(paymentAccountRepositoryMock);

    private UserAttribute userAttribute = new UserAttribute();
    private UserCreationRequest superUserCreationRequest;
    private DxAddressCreationRequest dxAddressRequest;
    private ContactInformationCreationRequest contactInformationCreationRequest;
    private OrganisationCreationRequest organisationCreationRequest;

    private List<Organisation> organisations;
    private List<ContactInformationCreationRequest> contactInformationCreationRequests;
    private List<DxAddressCreationRequest> dxAddressRequests;
    private List<PaymentAccount> paymentAccounts;
    private List<UserAccountMap> userAccountMaps;
    private List<String> userRoles = new ArrayList<>();
    private List<PrdEnum> prdEnums = new ArrayList<>();
    private List<UserAttribute> userAttributes;
    private List<String> jurisdictionIds;
    Set<String> paymentAccountList;
    private DeleteOrganisationResponse deleteOrganisationResponse;

    @InjectMocks
    private OrganisationServiceImpl sut;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        sut.setOrganisationRepository(organisationRepository);
        sut.setProfessionalUserRepository(professionalUserRepositoryMock);
        sut.setPaymentAccountRepository(paymentAccountRepositoryMock);
        sut.setDxAddressRepository(dxAddressRepositoryMock);
        sut.setContactInformationRepository(contactInformationRepositoryMock);
        sut.setPrdEnumRepository(prdEnumRepositoryMock);
        sut.setUserAccountMapService(userAccountMapServiceMock);
        sut.setUserProfileFeignClient(userProfileFeignClient);
        sut.setPrdEnumService(prdEnumService);
        sut.setUserAttributeService(userAttributeServiceMock);
        sut.setPaymentAccountValidator(paymentAccountValidator);
        sut.setOrganisationMfaStatusRepository(organisationMfaStatusRepositoryMock);

        paymentAccountList = new HashSet<>();
        String pbaNumber = "PBA1234567";
        paymentAccountList.add(pbaNumber);

        contactInformationCreationRequests = new ArrayList<>();
        dxAddressRequests = new ArrayList<>();
        organisations = new ArrayList<>();
        paymentAccounts = new ArrayList<>();
        paymentAccounts.add(paymentAccount);
        userAttributes = new ArrayList<>();
        userAccountMaps = new ArrayList<>();
        jurisdictionIds = new ArrayList<>();
        jurisdictionIds.add("PROBATE");

        organisation.setId(UUID.randomUUID());
        organisation.setPaymentAccounts(paymentAccounts);
        organisation.setOrganisationIdentifier(generateUniqueAlphanumericId(LENGTH_OF_ORGANISATION_IDENTIFIER));

        superUserCreationRequest = new UserCreationRequest("some-fname", "some-lname",
                "some-email");

        dxAddressRequest = new DxAddressCreationRequest("DX 1234567890", "dxExchange");
        dxAddressRequests.add(dxAddressRequest);

        contactInformationCreationRequest = new ContactInformationCreationRequest("addressLine-1",
                "addressLine-2", "addressLine-3", "townCity", "county",
                "country", "postCode", dxAddressRequests);

        contactInformationCreationRequests.add(contactInformationCreationRequest);

        organisationCreationRequest = new OrganisationCreationRequest("some-org-name", "PENDING",
                "sra-id", "false", "number01", "company-url",
                superUserCreationRequest, paymentAccountList, contactInformationCreationRequests);
        deleteOrganisationResponse = new DeleteOrganisationResponse(204,"successfully deleted");

        when(dxAddressRepositoryMock.save(any(DxAddress.class))).thenReturn(dxAddress);
        when(contactInformationRepositoryMock.save(any(ContactInformation.class))).thenReturn(contactInformation);
        when(professionalUserRepositoryMock.save(any(ProfessionalUser.class))).thenReturn(professionalUser);
        when(paymentAccountRepositoryMock.save(any(PaymentAccount.class))).thenReturn(paymentAccount);
        when(paymentAccountRepositoryMock.findAll()).thenReturn(paymentAccounts);
        when(userAccountMapRepositoryMock.save(any(UserAccountMap.class))).thenReturn(userAccountMap);
        when(userAccountMapRepositoryMock.findAll()).thenReturn(userAccountMaps);
        when(organisationRepository.save(any(Organisation.class))).thenReturn(organisation);
        when(organisationRepository.findAll()).thenReturn(organisations);
        when(organisationRepository.findByOrganisationIdentifier(any())).thenReturn(organisation);
        when(organisationRepository.findByStatus(any())).thenReturn(organisations);
        when(organisationRepositoryImplNullReturnedMock.findByOrganisationIdentifier(any())).thenReturn(null);
        when(organisationMfaStatusRepositoryMock.save(any(OrganisationMfaStatus.class)))
                .thenReturn(organisationMfaStatus);
    }

    @Test
    public void test_SavesAnOrganisation() {
        prdEnums.add(new PrdEnum(new PrdEnumId(0, "SIDAM_ROLE"),
                "pui-user-manager", "SIDAM_ROLE"));
        prdEnums.add(new PrdEnum(new PrdEnumId(4, "ADMIN_ROLE"),
                "organisation-admin", "ADMIN_ROLE"));
        prdEnums.add(new PrdEnum(new PrdEnumId(10, "JURISD_ID"),
                "PROBATE", "PROBATE"));
        prdEnums.add(new PrdEnum(new PrdEnumId(10, "JURISD_ID"),
                "PROBATE", "PROBATE"));

        OrganisationResponse organisationResponse = sut.createOrganisationFrom(organisationCreationRequest);

        assertThat(organisationResponse).isNotNull();

        verify(organisationRepository, times(1)).save(any(Organisation.class));
        verify(professionalUserRepositoryMock, times(1)).save(any(ProfessionalUser.class));
        verify(paymentAccountRepositoryMock, times(1)).save(any(PaymentAccount.class));
        verify(contactInformationRepositoryMock, times(1)).save(any(ContactInformation.class));
        verify(dxAddressRepositoryMock, times(1)).saveAll(any());
        verify(userAccountMapServiceMock, times(1))
                .persistedUserAccountMap(any(ProfessionalUser.class), anyList());
        verify(organisationMfaStatusRepositoryMock, times(1)).save(any(OrganisationMfaStatus.class));
    }

    @Test
    public void test_SavesOrganisationWithInvalidRequest() {
        Organisation organisationMock = mock(Organisation.class);
        when(organisationRepository.save(any(Organisation.class))).thenThrow(ConstraintViolationException.class);

        Assertions.assertThatThrownBy(() -> sut.saveOrganisation(organisationMock))
                .isExactlyInstanceOf(ConstraintViolationException.class);

        verify(organisationRepository, times(2)).save(any(Organisation.class));

        assertThat(organisation.getOrganisationIdentifier()).isNotNull();
        verify(organisationMock, times(1)).setOrganisationIdentifier(any(String.class));
    }

    @Test
    public void test_addPbaAccountToOrganisation() {
        Organisation organisationMock = mock(Organisation.class);
        Set<String> paymentAccounts = new HashSet<>();
        String pbaNumber = "PBA1234567";
        paymentAccounts.add(pbaNumber);

        sut.addPbaAccountToOrganisation(paymentAccounts, organisationMock, false);

        verify(organisationMock, times(1)).addPaymentAccount(any(PaymentAccount.class));
    }

    @Test
    public void test_addSuperUserToOrganisation() {
        Organisation organisationMock = mock(Organisation.class);

        sut.addSuperUserToOrganisation(superUserCreationRequest, organisationMock);

        verify(organisationMock, times(1)).addProfessionalUser(any(SuperUser.class));
    }

    @Test
    public void test_addDefaultMfaStatusToOrganisation() {
        Organisation organisationMock = mock(Organisation.class);

        sut.addDefaultMfaStatusToOrganisation(organisationMock);

        verify(organisationMock,times(1)).addOrganisationMfaStatus(any(OrganisationMfaStatus.class));
    }

    @Test
    public void test_setNewContactInformationFromRequest() {
        ContactInformation contactInformationMock = mock(ContactInformation.class);
        Organisation organisationMock = mock(Organisation.class);

        sut.setNewContactInformationFromRequest(contactInformationMock, contactInformationCreationRequest,
                organisationMock);

        verify(contactInformationMock, times(1)).setAddressLine1(any(String.class));
        verify(contactInformationMock, times(1)).setAddressLine2(any(String.class));
        verify(contactInformationMock, times(1)).setAddressLine3(any(String.class));
        verify(contactInformationMock, times(1)).setTownCity(any(String.class));
        verify(contactInformationMock, times(1)).setCounty(any(String.class));
        verify(contactInformationMock, times(1)).setCountry(any(String.class));
        verify(contactInformationMock, times(1)).setPostCode(any(String.class));
        verify(contactInformationMock, times(1)).setOrganisation(any(Organisation.class));
    }

    @Test
    public void test_UpdatesAnOrganisationVerifySetMethodsAreCalled() {
        Organisation organisationMock = mock(Organisation.class);

        when(organisationRepository.findByOrganisationIdentifier(any(String.class))).thenReturn(organisationMock);

        OrganisationResponse organisationResponse = sut.updateOrganisation(organisationCreationRequest,
                organisationIdentifier);

        assertThat(organisationResponse).isNotNull();

        verify(organisationMock, times(1)).setName((organisationCreationRequest.getName()));
        verify(organisationMock, times(1))
                .setStatus((OrganisationStatus.valueOf(organisationCreationRequest.getStatus())));
        verify(organisationMock, times(1)).setSraId((organisationCreationRequest.getSraId()));
        verify(organisationMock, times(1))
                .setCompanyNumber(organisationCreationRequest.getCompanyNumber());
        verify(organisationMock, times(1))
                .setSraRegulated(Boolean.parseBoolean(organisationCreationRequest.getSraRegulated()));
        verify(organisationMock, times(1))
                .setCompanyUrl((organisationCreationRequest.getCompanyUrl()));
        verify(organisationRepository, times(1))
                .findByOrganisationIdentifier(any(String.class));
        verify(organisationRepository, times(1)).save(any(Organisation.class));
    }


    @Test(expected = EmptyResultDataAccessException.class)
    public void test_retrieve_an_organisations_by_status() {
        sut.findByOrganisationStatus(OrganisationStatus.ACTIVE);
    }


    @Test(expected = EmptyResultDataAccessException.class)
    public void test_throwsEmptyResultDataAccessException() {
        Organisation testOrganisation = new Organisation();
        testOrganisation.setId(UUID.randomUUID());
        String testOrganisationId = testOrganisation.getOrganisationIdentifier();

        when(organisationRepository.findByOrganisationIdentifier(any())).thenReturn(null);
        sut.retrieveOrganisation(testOrganisationId);
    }

    @Test(expected = EmptyResultDataAccessException.class)
    public void test_RetrieveAllOrganisationsThrowExceptionWhenOrganisationEmpty() throws JsonProcessingException {
        UserProfile profile = new UserProfile(UUID.randomUUID().toString(), "email@org.com",
                "firstName", "lastName", IdamStatus.ACTIVE);

        GetUserProfileResponse userProfileResponse = new GetUserProfileResponse(profile, false);

        ObjectMapper mapper = new ObjectMapper();
        String body = mapper.writeValueAsString(userProfileResponse);

        when(userProfileFeignClient.getUserProfileById(anyString())).thenReturn(Response.builder()
                .request(mock(Request.class)).body(body, Charset.defaultCharset()).status(200).build());

        OrganisationsDetailResponse organisationsDetailResponse = sut.retrieveAllOrganisations();

        assertThat(organisationsDetailResponse).isNull();

        verify(organisationRepository, times(1)).findByStatus(OrganisationStatus.PENDING);
        verify(organisationRepository, times(1)).findByStatus(OrganisationStatus.ACTIVE);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void test_RetrieveAnOrganisationsByOrgIdentifier() throws Exception {
        superUser.setUserIdentifier(UUID.randomUUID().toString());
        List<SuperUser> users = new ArrayList<>();
        users.add(superUser);

        organisation.setStatus(OrganisationStatus.ACTIVE);
        organisation.setUsers(users);
        professionalUser.setUserIdentifier(UUID.randomUUID().toString());

        when(organisationRepository.findByOrganisationIdentifier(organisationIdentifier)).thenReturn(organisation);

        UserProfile profile = new UserProfile(UUID.randomUUID().toString(), "email@org.com",
                "firstName", "lastName", IdamStatus.ACTIVE);
        GetUserProfileResponse userProfileResponse = new GetUserProfileResponse(profile, false);

        ObjectMapper mapper = new ObjectMapper();
        String body = mapper.writeValueAsString(userProfileResponse);

        when(userProfileFeignClient.getUserProfileById(anyString())).thenReturn(Response.builder()
                .request(Request.create(Request.HttpMethod.POST, "", new HashMap<>(), Request.Body.empty(),
                        null)).body(body, Charset.defaultCharset()).status(200).build());

        OrganisationEntityResponse organisationEntityResponse = sut.retrieveOrganisation(organisationIdentifier);

        assertThat(organisationEntityResponse).isNotNull();
        verify(organisationRepository, times(1))
                .findByOrganisationIdentifier(any(String.class));
    }

    @Test
    public void test_RetrieveAnOrganisationsByWhenStatusActive() throws Exception {
        superUser.setUserIdentifier(UUID.randomUUID().toString());
        List<SuperUser> users = new ArrayList<>();
        users.add(superUser);

        organisation.setStatus(OrganisationStatus.ACTIVE);
        organisation.setUsers(users);
        professionalUser.setUserIdentifier(UUID.randomUUID().toString());

        List<Organisation> organisations = new ArrayList<>();
        organisations.add(organisation);

        when(organisationRepository.findByStatus(OrganisationStatus.ACTIVE)).thenReturn(organisations);

        ProfessionalUsersEntityResponse professionalUsersEntityResponse = new ProfessionalUsersEntityResponse();
        List<ProfessionalUsersResponse> userProfiles = new ArrayList<>();
        ProfessionalUser profile = new ProfessionalUser("firstName", "lastName",
                "email@org.com", organisation);

        ProfessionalUsersResponse userProfileResponse = new ProfessionalUsersResponse(profile);
        userProfileResponse.setUserIdentifier(UUID.randomUUID().toString());
        userProfiles.add(userProfileResponse);
        professionalUsersEntityResponse.getUserProfiles().addAll(userProfiles);
        ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                false);
        String body = mapper.writeValueAsString(professionalUsersEntityResponse);

        when(userProfileFeignClient.getUserProfiles(any(), any(), any())).thenReturn(Response.builder()
                .request(mock(Request.class)).body(body, Charset.defaultCharset()).status(200).build());

        OrganisationsDetailResponse organisationDetailResponse
                = sut.findByOrganisationStatus(OrganisationStatus.ACTIVE);

        assertThat(organisationDetailResponse).isNotNull();
        verify(organisationRepository, times(1)).findByStatus(OrganisationStatus.ACTIVE);
    }

    @Test
    public void test_retrieveAnOrganisation() {
        Organisation organisationMock = mock(Organisation.class);
        when(organisationRepository.findByOrganisationIdentifier(organisationIdentifier)).thenReturn(organisationMock);
        when(organisationMock.getStatus()).thenReturn(OrganisationStatus.ACTIVE);

        OrganisationEntityResponse organisationEntityResponse = sut.retrieveOrganisation(organisationIdentifier);

        assertThat(organisationEntityResponse).isNotNull();

        verify(organisationRepository, times(1))
                .findByOrganisationIdentifier(organisationIdentifier);
        verify(organisationMock, times(2)).getStatus();
        verify(organisationMock, times(1)).setUsers(anyList());
    }

    @Test(expected = EmptyResultDataAccessException.class)
    public void test_retrieveAnOrganisationByUuidNotFound() {
        when(organisationRepository.findByOrganisationIdentifier(any(String.class))).thenReturn(null);

        sut.retrieveOrganisation(organisationIdentifier);

        verify(organisationRepository, times(1))
                .findByOrganisationIdentifier(any(String.class));
    }

    @Test(expected = EmptyResultDataAccessException.class)
    public void test_retrieveAnPendingOrganisationThrowExceptionWhenOrgEmpty() {
        sut.findByOrganisationStatus(OrganisationStatus.PENDING);
    }

    @Test
    public void test_retrieveAnPendingOrganisation() {
        List<Organisation> organisations = new ArrayList<>();
        organisations.add(organisation);

        when(organisationRepository.findByStatus(OrganisationStatus.PENDING)).thenReturn(organisations);

        OrganisationsDetailResponse organisationDetailResponse
                = sut.findByOrganisationStatus(OrganisationStatus.PENDING);

        assertThat(organisationDetailResponse).isNotNull();
        verify(organisationRepository, times(1)).findByStatus(OrganisationStatus.PENDING);
    }

    @Test
    public void test_retrieveAllOrganisations() {
        List<Organisation> organisations = new ArrayList<>();
        organisations.add(organisation);
        organisations.add(organisation);

        when(organisationRepository.findAll()).thenReturn(organisations);

        OrganisationsDetailResponse organisationDetailResponse = sut.retrieveAllOrganisations();

        assertThat(organisationDetailResponse).isNotNull();
        verify(organisationRepository, times(1)).findAll();
    }

    @Test
    public void test_retrieveAnOrganisationByOrgId() {
        when(organisationRepository.findByOrganisationIdentifier(organisationIdentifier)).thenReturn(organisation);

        Organisation organisation = sut.getOrganisationByOrgIdentifier(organisationIdentifier);

        assertThat(organisation).isNotNull();
        verify(organisationRepository, times(1))
                .findByOrganisationIdentifier(organisationIdentifier);
    }


    @Test(expected = EmptyResultDataAccessException.class)
    public void test_ThrowsExceptionWhenOrganisationEmpty() {
        when(organisationRepository.findAll()).thenReturn(new ArrayList<>());

        sut.retrieveAllOrganisations();
    }


    @Test(expected = InvalidRequest.class)
    public void test_throwInvalidRequestWhenInvalidPbaIsPassed() {
        Set<String> paymentAccountList = new HashSet<>();
        String pbaNumber = "GBA1234567";
        paymentAccountList.add(pbaNumber);

        organisationCreationRequest = new OrganisationCreationRequest("some-org-name", "PENDING",
                "sra-id", "false", "company-number", "company-url",
                superUserCreationRequest, paymentAccountList, contactInformationCreationRequests);

        sut.createOrganisationFrom(organisationCreationRequest);
    }

    @Test(expected = InvalidRequest.class)
    public void test_throwInvalidRequestWhenNullSuperUserEmailIsPassed() {
        Set<String> paymentAccountList = new HashSet<>();
        String pbaNumber = "PBA1234567";
        paymentAccountList.add(pbaNumber);

        superUserCreationRequest = new UserCreationRequest("some-fname", "some-lname",
                null);

        organisationCreationRequest = new OrganisationCreationRequest("some-org-name", "PENDING",
                "sra-id", "false", "company-number", "company-url",
                superUserCreationRequest, paymentAccountList, contactInformationCreationRequests);

        sut.createOrganisationFrom(organisationCreationRequest);
    }

    @Test
    public void test_AllAttributesAddedToSuperUser() {
        prdEnums.add(new PrdEnum(new PrdEnumId(0, "SIDAM_ROLE"),
                "pui-user-manager", "SIDAM_ROLE"));
        prdEnums.add(new PrdEnum(new PrdEnumId(1, "SIDAM_ROLE"),
                "pui-user-manager", "SIDAM_ROLE"));
        prdEnums.add(new PrdEnum(new PrdEnumId(2, "SIDAM_ROLE"),
                "pui-user-manager", "SIDAM_ROLE"));
        prdEnums.add(new PrdEnum(new PrdEnumId(3, "SIDAM_ROLE"),
                "pui-user-manager", "SIDAM_ROLE"));
        prdEnums.add(new PrdEnum(new PrdEnumId(4, "ADMIN_ROLE"),
                "organisation-admin", "ADMIN_ROLE"));
        prdEnums.add(new PrdEnum(new PrdEnumId(10, "JURISD_ID"),
                "PROBATE", "PROBATE"));

        userRoles.add("pui-user-manager");
        userRoles.add("pui-organisation-manager");
        userRoles.add("pui-finance-manager");
        userRoles.add("pui-case-manager");
        userRoles.add("organisation-admin");

        when(prdEnumRepositoryMock.findAll()).thenReturn(prdEnums);
        when(prdEnumService.findAllPrdEnums()).thenReturn(prdEnums);

        assertExpectedOrganisationResponse(sut.createOrganisationFrom(organisationCreationRequest));

        verify(userAttributeServiceMock, times(1))
                .addUserAttributesToSuperUser(eq(professionalUser), eq(userAttributes));
        verify(professionalUserRepositoryMock, times(1)).save(any(ProfessionalUser.class));
        verify(organisationRepository, times(1)).save(any(Organisation.class));
    }

    @Test
    public void test_FakeAttributesNotAdded() {
        prdEnums.add(new PrdEnum(new PrdEnumId(0, "FAKE"),
                "pui-fake-manager", "FAKE_ROLE"));
        prdEnums.add(new PrdEnum(new PrdEnumId(1, "FAKE_ROLE"),
                "pui-fake-manager", "FAKE_ROLE"));
        prdEnums.add(new PrdEnum(new PrdEnumId(2, "FAKE_FAKE"),
                "pui-fake-manager", "FAKE_ROLE"));
        prdEnums.add(new PrdEnum(new PrdEnumId(10, "JURISD_ID"),
                "pui-fake-manager", "FAKE_ROLE"));
        prdEnums.add(new PrdEnum(new PrdEnumId(10, "FAKE_JURISD"),
                "PROBATE", "FAKE_ROLE"));

        List<UserAttribute> attributes = new ArrayList<>();
        attributes.add(userAttribute);
        when(prdEnumRepositoryMock.findAll()).thenReturn(prdEnums);
        when(prdEnumService.findAllPrdEnums()).thenReturn(prdEnums);
        when(userAttributeServiceMock.addUserAttributesToSuperUser(professionalUser, userAttributes
                )).thenReturn(attributes);

        assertExpectedOrganisationResponse(sut.createOrganisationFrom(organisationCreationRequest));

        verify(userAttributeServiceMock, times(1))
                .addUserAttributesToSuperUser(eq(professionalUser), eq(userAttributes));
    }

    @Test
    public void test_AddContactInformationToOrganisation() {
        ContactInformationCreationRequest contactInformationCreationRequest
                = new ContactInformationCreationRequest("addressLine-1", "addressLine-2",
                "addressLine-3", "townCity", "county", "country",
                "postCode", dxAddressRequests);
        contactInformationCreationRequests.add(contactInformationCreationRequest);
        Organisation organisation = new Organisation("some-org-name", OrganisationStatus.ACTIVE,
                "PENDING", "Test", Boolean.TRUE, "Demo");

        sut.addContactInformationToOrganisation(contactInformationCreationRequests, this.organisation);

        assertEquals("addressLine-1", contactInformationCreationRequests.get(0).getAddressLine1());
        assertEquals("addressLine-2", contactInformationCreationRequests.get(0).getAddressLine2());
        assertEquals("addressLine-3", contactInformationCreationRequests.get(0).getAddressLine3());
        assertEquals("townCity", contactInformationCreationRequests.get(0).getTownCity());
        assertEquals("county", contactInformationCreationRequests.get(0).getCounty());
        assertEquals("country", contactInformationCreationRequests.get(0).getCountry());
        assertEquals("postCode", contactInformationCreationRequests.get(0).getPostCode());
    }

    private void assertExpectedOrganisationResponse(OrganisationResponse organisationResponse) {
        final int orgIdLength = 7;
        assertThat(organisationResponse).isNotNull();
        assertThat(organisationResponse.getOrganisationIdentifier()).isNotNull();
        assertThat(organisationResponse.getOrganisationIdentifier()).hasSize(orgIdLength);
    }

    @Test
    public void testDeletePendingOrganisation() {
        Organisation organisation = getDeleteOrganisation(OrganisationStatus.PENDING);
        deleteOrganisationResponse = sut.deleteOrganisation(organisation, "123456789");

        assertThat(deleteOrganisationResponse).isNotNull();
        assertThat(deleteOrganisationResponse.getStatusCode()).isEqualTo(ProfessionalApiConstants.STATUS_CODE_204);
        assertThat(deleteOrganisationResponse.getMessage()).isEqualTo(ProfessionalApiConstants.DELETION_SUCCESS_MSG);
        verify(organisationRepository, times(1)).deleteById(any());
    }

    @Test
    public void testDeleteActiveOrganisationWithOrgAdminPending() throws Exception {


        NewUserResponse newUserResponse = new NewUserResponse();
        newUserResponse.setIdamStatus("PENDING");
        ObjectMapper mapper = new ObjectMapper();


        DeleteOrganisationResponse deleteOrganisationResponse = new DeleteOrganisationResponse();
        deleteOrganisationResponse.setStatusCode(ProfessionalApiConstants.STATUS_CODE_204);
        deleteOrganisationResponse.setMessage(ProfessionalApiConstants.DELETION_SUCCESS_MSG);
        ObjectMapper mapperOne = new ObjectMapper();
        String deleteBody = mapperOne.writeValueAsString(newUserResponse);
        String body = mapper.writeValueAsString(newUserResponse);

        when(professionalUserRepositoryMock.findByUserCountByOrganisationId(any())).thenReturn(1);
        when(userProfileFeignClient.getUserProfileByEmail(anyString())).thenReturn(Response.builder()
                .request(mock(Request.class)).body(body, Charset.defaultCharset()).status(200).build());
        when(userProfileFeignClient.deleteUserProfile(any())).thenReturn(Response.builder()
                .request(mock(Request.class)).body(deleteBody, Charset.defaultCharset()).status(204).build());
        Organisation organisation = getDeleteOrganisation(OrganisationStatus.ACTIVE);

        deleteOrganisationResponse = sut.deleteOrganisation(organisation,"123456789");

        assertThat(deleteOrganisationResponse).isNotNull();
        assertThat(deleteOrganisationResponse.getStatusCode()).isEqualTo(ProfessionalApiConstants.STATUS_CODE_204);
        assertThat(deleteOrganisationResponse.getMessage()).isEqualTo(ProfessionalApiConstants.DELETION_SUCCESS_MSG);
        verify(organisationRepository, times(1)).deleteById(any());
        verify(professionalUserRepositoryMock, times(1)).findByUserCountByOrganisationId(any());
        verify(userProfileFeignClient, times(1)).getUserProfileByEmail(anyString());
        verify(userProfileFeignClient, times(1)).deleteUserProfile(any());
    }

    @Test
    public void testDeleteActiveOrganisationWithOrgAdminActiveGives400WithMessage() throws Exception {


        NewUserResponse newUserResponse = new NewUserResponse();
        newUserResponse.setIdamStatus("ACTIVE");
        ObjectMapper mapper = new ObjectMapper();


        DeleteOrganisationResponse deleteOrganisationResponse = new DeleteOrganisationResponse();
        deleteOrganisationResponse.setStatusCode(ProfessionalApiConstants.ERROR_CODE_400);
        deleteOrganisationResponse.setMessage(ProfessionalApiConstants.ERROR_MESSAGE_400_ADMIN_NOT_PENDING);
        ObjectMapper mapperOne = new ObjectMapper();
        String deleteBody = mapperOne.writeValueAsString(newUserResponse);
        String body = mapper.writeValueAsString(newUserResponse);

        when(professionalUserRepositoryMock.findByUserCountByOrganisationId(any())).thenReturn(1);
        when(userProfileFeignClient.getUserProfileByEmail(anyString())).thenReturn(Response.builder()
                .request(mock(Request.class)).body(body, Charset.defaultCharset()).status(200).build());
        when(userProfileFeignClient.deleteUserProfile(any())).thenReturn(Response.builder()
                .request(mock(Request.class)).body(deleteBody, Charset.defaultCharset()).status(400).build());
        Organisation organisation = getDeleteOrganisation(OrganisationStatus.ACTIVE);

        deleteOrganisationResponse = sut.deleteOrganisation(organisation, "123456789");

        assertThat(deleteOrganisationResponse).isNotNull();
        assertThat(deleteOrganisationResponse.getStatusCode()).isEqualTo(ProfessionalApiConstants.ERROR_CODE_400);
        assertThat(deleteOrganisationResponse.getMessage())
                .isEqualTo(ProfessionalApiConstants.ERROR_MESSAGE_400_ADMIN_NOT_PENDING);
        verify(organisationRepository, times(0)).deleteById(any());
        verify(professionalUserRepositoryMock, times(1)).findByUserCountByOrganisationId(any());
        verify(userProfileFeignClient, times(1)).getUserProfileByEmail(anyString());
        verify(userProfileFeignClient, times(0)).deleteUserProfile(any());
    }

    @Test
    public void testDeleteActiveOrganisationWithMultiUsersGives400WithMessage() throws Exception {

        Organisation organisation = getDeleteOrganisation(OrganisationStatus.ACTIVE);
        when(professionalUserRepositoryMock.findByUserCountByOrganisationId(any())).thenReturn(2);
        deleteOrganisationResponse = sut.deleteOrganisation(organisation, "123456789");

        assertThat(deleteOrganisationResponse).isNotNull();
        assertThat(deleteOrganisationResponse.getStatusCode()).isEqualTo(ProfessionalApiConstants.ERROR_CODE_400);
        assertThat(deleteOrganisationResponse.getMessage())
                .isEqualTo(ProfessionalApiConstants.ERROR_MESSAGE_400_ORG_MORE_THAN_ONE_USER);
        verify(organisationRepository, times(0)).deleteById(any());
        verify(professionalUserRepositoryMock, times(1)).findByUserCountByOrganisationId(any());
        verify(userProfileFeignClient, times(0)).getUserProfileByEmail(anyString());
        verify(userProfileFeignClient, times(0)).deleteUserProfile(any());
    }

    @Test
    public void testDeleteActiveOrganisationGives500WithMessageWhenUpDown() throws Exception {


        NewUserResponse newUserResponse = new NewUserResponse();
        newUserResponse.setIdamStatus("PENDING");
        ObjectMapper mapper = new ObjectMapper();
        String body = mapper.writeValueAsString(newUserResponse);

        ObjectMapper mapperOne = new ObjectMapper();
        String deleteBody = mapperOne.writeValueAsString(newUserResponse);

        when(professionalUserRepositoryMock.findByUserCountByOrganisationId(any())).thenReturn(1);
        when(userProfileFeignClient.getUserProfileByEmail(anyString())).thenReturn(Response.builder()
                .request(mock(Request.class)).body(body, Charset.defaultCharset()).status(200).build());
        when(userProfileFeignClient.deleteUserProfile(any())).thenReturn(Response.builder()
                .request(mock(Request.class)).body(deleteBody, Charset.defaultCharset()).status(500).build());
        Organisation organisation = getDeleteOrganisation(OrganisationStatus.ACTIVE);
        DeleteOrganisationResponse deleteOrganisationResponse = sut.deleteOrganisation(organisation,"123456789");

        assertThat(deleteOrganisationResponse).isNotNull();
        assertThat(deleteOrganisationResponse.getStatusCode()).isEqualTo(ProfessionalApiConstants.ERROR_CODE_500);
        assertThat(deleteOrganisationResponse.getMessage()).isEqualTo("Error while invoking UP");
        verify(organisationRepository, times(0)).deleteById(any());
        verify(professionalUserRepositoryMock, times(1)).findByUserCountByOrganisationId(any());
        verify(userProfileFeignClient, times(1)).getUserProfileByEmail(anyString());
        verify(userProfileFeignClient, times(1)).deleteUserProfile(any());
    }

    @Test
    public void testDeleteActiveOrganisationWithNoUserProfileinUpGives500WithMessage() throws Exception {

        NewUserResponse newUserResponse = new NewUserResponse();
        newUserResponse.setIdamStatus(" ");
        ObjectMapper mapper = new ObjectMapper();
        String body = mapper.writeValueAsString(newUserResponse);

        ObjectMapper mapperOne = new ObjectMapper();
        String deleteBody = mapperOne.writeValueAsString(newUserResponse);

        when(professionalUserRepositoryMock.findByUserCountByOrganisationId(any())).thenReturn(1);
        when(userProfileFeignClient.getUserProfileByEmail(anyString())).thenReturn(Response.builder()
                .request(mock(Request.class)).body(body, Charset.defaultCharset()).status(404).build());
        Organisation organisation = getDeleteOrganisation(OrganisationStatus.ACTIVE);
        DeleteOrganisationResponse deleteOrganisationResponse = sut.deleteOrganisation(organisation,"123456789");

        assertThat(deleteOrganisationResponse).isNotNull();
        assertThat(deleteOrganisationResponse.getStatusCode()).isEqualTo(ProfessionalApiConstants.ERROR_CODE_500);
        assertThat(deleteOrganisationResponse.getMessage())
                .isEqualTo(ProfessionalApiConstants.ERR_MESG_500_ADMIN_NOTFOUNDUP);
        verify(organisationRepository, times(0)).deleteById(any());
        verify(professionalUserRepositoryMock, times(1)).findByUserCountByOrganisationId(any());
        verify(userProfileFeignClient, times(1)).getUserProfileByEmail(anyString());
        verify(userProfileFeignClient, times(0)).deleteUserProfile(any());
    }

    @Test(expected = ExternalApiException.class)
    public void testDeleteActiveOrganisationThrowsExceptionWhenUpServiceDown() throws Exception {


        NewUserResponse newUserResponse = new NewUserResponse();
        newUserResponse.setIdamStatus("PENDING");
        ObjectMapper mapper = new ObjectMapper();
        String body = mapper.writeValueAsString(newUserResponse);

        ObjectMapper mapperOne = new ObjectMapper();
        String deleteBody = mapperOne.writeValueAsString(newUserResponse);

        when(professionalUserRepositoryMock.findByUserCountByOrganisationId(any())).thenReturn(1);
        when(userProfileFeignClient.getUserProfileByEmail(anyString())).thenReturn(Response.builder()
                .request(mock(Request.class)).body(body, Charset.defaultCharset()).status(200).build());
        when(userProfileFeignClient.deleteUserProfile(any()))
                .thenThrow(new ExternalApiException(HttpStatus.valueOf(500), "Error while invoking UP"));

        Organisation organisation = getDeleteOrganisation(OrganisationStatus.ACTIVE);
        deleteOrganisationResponse = sut.deleteOrganisation(organisation, "123456789");

        assertThat(deleteOrganisationResponse).isNotNull();
        assertThat(deleteOrganisationResponse.getStatusCode()).isEqualTo(ProfessionalApiConstants.ERROR_CODE_500);
        assertThat(deleteOrganisationResponse.getMessage()).isEqualTo("Error while invoking UP");
        verify(organisationRepository, times(0)).deleteById(any());
        verify(professionalUserRepositoryMock, times(1)).findByUserCountByOrganisationId(any());
        verify(userProfileFeignClient, times(1)).getUserProfileByEmail(anyString());
        verify(userProfileFeignClient, times(1)).deleteUserProfile(any());
    }

    private Organisation getDeleteOrganisation(OrganisationStatus status) {

        ProfessionalUser profile = new ProfessionalUser("firstName", "lastName", "email@org.com", organisation);
        superUser.setUserIdentifier(UUID.randomUUID().toString());
        List<SuperUser> users = new ArrayList<>();
        users.add(superUser);

        organisation.setStatus(status);
        organisation.setUsers(users);

        contactInformation.setAddressLine1("addressLine1");

        List<DxAddress> dxAddresses = new ArrayList<>();
        List<ContactInformation> contactInformations = new ArrayList<>();

        dxAddresses.add(dxAddress);
        contactInformation.setDxAddresses(dxAddresses);
        contactInformations.add(contactInformation);
        organisation.setContactInformations(contactInformations);

        PrdEnum prdEnum = new PrdEnum(new PrdEnumId(0, "SIDAM_ROLE"), "pui-user-manager", "SIDAM_ROLE");
        UserAttribute userAttribute = new UserAttribute(professionalUser,prdEnum);
        userAttributes.add(userAttribute);
        professionalUser.setUserAttributes(userAttributes);

        UserAccountMapId userAccountMapId = new UserAccountMapId(professionalUser, paymentAccount);
        UserAccountMap userAccountMap = new UserAccountMap(userAccountMapId);
        userAccountMaps.add(userAccountMap);
        organisation.addPaymentAccount(paymentAccount);
        return organisation;
    }

}