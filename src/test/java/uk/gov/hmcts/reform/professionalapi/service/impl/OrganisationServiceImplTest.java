package uk.gov.hmcts.reform.professionalapi.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Request;
import feign.Response;
import org.assertj.core.api.Assertions;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.professionalapi.controller.advice.ExternalApiException;
import uk.gov.hmcts.reform.professionalapi.controller.advice.ResourceNotFoundException;
import uk.gov.hmcts.reform.professionalapi.controller.constants.IdamStatus;
import uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants;
import uk.gov.hmcts.reform.professionalapi.controller.feign.UserProfileFeignClient;
import uk.gov.hmcts.reform.professionalapi.controller.request.ContactInformationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.DxAddressCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.InvalidRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.PbaRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.UserCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.validator.PaymentAccountValidator;
import uk.gov.hmcts.reform.professionalapi.controller.response.DeleteOrganisationResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.GetUserProfileResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.NewUserResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.OrganisationEntityResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.OrganisationResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.OrganisationsDetailResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.OrganisationsWithPbaStatusResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.ProfessionalUsersEntityResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.ProfessionalUsersResponse;
import uk.gov.hmcts.reform.professionalapi.domain.AddPbaResponse;
import uk.gov.hmcts.reform.professionalapi.domain.ContactInformation;
import uk.gov.hmcts.reform.professionalapi.domain.DxAddress;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.OrganisationMfaStatus;
import uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus;
import uk.gov.hmcts.reform.professionalapi.domain.PaymentAccount;
import uk.gov.hmcts.reform.professionalapi.domain.PbaStatus;
import uk.gov.hmcts.reform.professionalapi.domain.PrdEnum;
import uk.gov.hmcts.reform.professionalapi.domain.PrdEnumId;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;
import uk.gov.hmcts.reform.professionalapi.domain.SuperUser;
import uk.gov.hmcts.reform.professionalapi.domain.UserAccountMap;
import uk.gov.hmcts.reform.professionalapi.domain.UserAccountMapId;
import uk.gov.hmcts.reform.professionalapi.domain.UserAttribute;
import uk.gov.hmcts.reform.professionalapi.domain.UserProfile;
import uk.gov.hmcts.reform.professionalapi.repository.ContactInformationRepository;
import uk.gov.hmcts.reform.professionalapi.repository.DxAddressRepository;
import uk.gov.hmcts.reform.professionalapi.repository.OrganisationMfaStatusRepository;
import uk.gov.hmcts.reform.professionalapi.repository.OrganisationRepository;
import uk.gov.hmcts.reform.professionalapi.repository.PaymentAccountRepository;
import uk.gov.hmcts.reform.professionalapi.repository.PrdEnumRepository;
import uk.gov.hmcts.reform.professionalapi.repository.ProfessionalUserRepository;
import uk.gov.hmcts.reform.professionalapi.repository.UserAccountMapRepository;
import uk.gov.hmcts.reform.professionalapi.service.PrdEnumService;
import uk.gov.hmcts.reform.professionalapi.service.ProfessionalUserService;
import uk.gov.hmcts.reform.professionalapi.service.UserAccountMapService;
import uk.gov.hmcts.reform.professionalapi.service.UserAttributeService;

import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.ERROR_MSG_PARTIAL_SUCCESS;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.LENGTH_OF_ORGANISATION_IDENTIFIER;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.ORG_NAME;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.PBA_STATUS_MESSAGE_AUTO_ACCEPTED;
import static uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus.ACTIVE;
import static uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus.REVIEW;
import static uk.gov.hmcts.reform.professionalapi.domain.PbaStatus.ACCEPTED;
import static uk.gov.hmcts.reform.professionalapi.domain.PbaStatus.PENDING;
import static uk.gov.hmcts.reform.professionalapi.generator.ProfessionalApiGenerator.generateUniqueAlphanumericId;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
class OrganisationServiceImplTest {

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
            "some-lname", "test@test.com", organisation);
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

    private final UserAttribute userAttribute = new UserAttribute();
    private UserCreationRequest superUserCreationRequest;
    private DxAddressCreationRequest dxAddressRequest;
    private ContactInformationCreationRequest contactInformationCreationRequest;
    private OrganisationCreationRequest organisationCreationRequest;

    private List<Organisation> organisations;
    private List<ContactInformationCreationRequest> contactInformationCreationRequests;
    private List<DxAddressCreationRequest> dxAddressRequests;
    private List<PaymentAccount> paymentAccounts;
    private List<UserAccountMap> userAccountMaps;
    private final List<String> userRoles = new ArrayList<>();
    private final List<PrdEnum> prdEnums = new ArrayList<>();
    private List<UserAttribute> userAttributes;
    private List<String> jurisdictionIds;
    Set<String> paymentAccountList;
    private DeleteOrganisationResponse deleteOrganisationResponse;

    @InjectMocks
    private OrganisationServiceImpl sut;

    private final ProfessionalUserService professionalUserServiceMock
            = mock(ProfessionalUserService.class);

    @BeforeEach
    void setUp() {
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

        contactInformationCreationRequest = new ContactInformationCreationRequest("uprn","addressLine-1",
                "addressLine-2", "addressLine-3", "townCity", "county",
                "country", "postCode", dxAddressRequests);

        contactInformationCreationRequests.add(contactInformationCreationRequest);

        organisationCreationRequest = new OrganisationCreationRequest("some-org-name", "PENDING", "statusMessage",
                "sra-id", "false", "number01", "company-url",
                superUserCreationRequest, paymentAccountList, contactInformationCreationRequests);
        deleteOrganisationResponse = new DeleteOrganisationResponse(204, "successfully deleted");

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
    void test_SavesAnOrganisation() {
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
    void test_SavesOrganisationWithInvalidRequest() {
        Organisation organisationMock = mock(Organisation.class);
        when(organisationRepository.save(any(Organisation.class))).thenThrow(ConstraintViolationException.class);

        Assertions.assertThatThrownBy(() -> sut.saveOrganisation(organisationMock))
                .isExactlyInstanceOf(ConstraintViolationException.class);

        verify(organisationRepository, times(2)).save(any(Organisation.class));

        assertThat(organisation.getOrganisationIdentifier()).isNotNull();
        verify(organisationMock, times(1)).setOrganisationIdentifier(any(String.class));
    }

    @Test
    void test_addPbaAccountToOrganisation() {
        Organisation organisationMock = mock(Organisation.class);
        Set<String> paymentAccounts = new HashSet<>();
        String pbaNumber = "PBA1234567";
        paymentAccounts.add(pbaNumber);

        sut.addPbaAccountToOrganisation(paymentAccounts, organisationMock, false, true);

        sut.addPbaAccountToOrganisation(paymentAccounts, organisationMock, false, false);

        verify(organisationMock, times(2)).addPaymentAccount(any(PaymentAccount.class));
    }

    @Test
    void test_updatePaymentAccounts() {
        List<PaymentAccount> pbas = new ArrayList<>();
        PaymentAccount pba = mock(PaymentAccount.class);
        pbas.add(pba);

        sut.updatePaymentAccounts(pbas);

        verify(pba, times(1)).setPbaStatus(ACCEPTED);
        verify(pba, times(1)).setStatusMessage(PBA_STATUS_MESSAGE_AUTO_ACCEPTED);
    }

    @Test
    void test_deletePbaAccountFromOrganisation() {
        doNothing().when(paymentAccountRepositoryMock).delete(any(PaymentAccount.class));
        sut.deletePaymentsOfOrganisation(paymentAccountList, organisation);
        verify(paymentAccountRepositoryMock, times(1)).deleteByPbaNumberUpperCase(anySet());
    }

    @Test
    void test_addSuperUserToOrganisation() {
        Organisation organisationMock = mock(Organisation.class);

        sut.addSuperUserToOrganisation(superUserCreationRequest, organisationMock);

        verify(organisationMock, times(1)).addProfessionalUser(any(SuperUser.class));
    }

    @Test
    void test_addDefaultMfaStatusToOrganisation() {
        Organisation organisationMock = mock(Organisation.class);

        sut.addDefaultMfaStatusToOrganisation(organisationMock);

        verify(organisationMock, times(1)).setOrganisationMfaStatus(any(OrganisationMfaStatus.class));
    }

    @Test
    void test_setNewContactInformationFromRequest() {
        ContactInformation contactInformationMock = mock(ContactInformation.class);
        Organisation organisationMock = mock(Organisation.class);

        sut.setNewContactInformationFromRequest(contactInformationMock, contactInformationCreationRequest,
                organisationMock);

        verify(contactInformationMock, times(1)).setUprn(any(String.class));
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
    void test_UpdatesAnOrganisationVerifySetMethodsAreCalled() {
        Organisation organisationMock = mock(Organisation.class);
        organisationCreationRequest.setStatus("ACTIVE");

        when(organisationRepository.findByOrganisationIdentifier(any(String.class))).thenReturn(organisationMock);
        when(organisationRepository.save(any(Organisation.class))).thenReturn(organisationMock);
        when(organisationMock.getPaymentAccounts()).thenReturn(paymentAccounts);

        OrganisationResponse organisationResponse = sut.updateOrganisation(organisationCreationRequest,
                organisationIdentifier, false);

        assertThat(organisationResponse).isNotNull();

        verify(organisationMock, times(1)).setName((organisationCreationRequest.getName()));
        verify(organisationMock, times(1))
                .setStatus((OrganisationStatus.valueOf(organisationCreationRequest.getStatus())));
        verify(organisationMock, times(1)).setStatusMessage((organisationCreationRequest.getStatusMessage()));
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


    @Test
    void test_retrieve_an_organisations_by_status() {
        String status = ACTIVE.name();

        assertThrows(EmptyResultDataAccessException.class, () ->
                sut.findByOrganisationStatus(status, null));
    }


    @Test
    void test_throwsEmptyResultDataAccessException() {
        Organisation testOrganisation = new Organisation();
        testOrganisation.setId(UUID.randomUUID());
        String testOrganisationId = testOrganisation.getOrganisationIdentifier();

        when(organisationRepository.findByOrganisationIdentifier(any())).thenReturn(null);
        assertThrows(EmptyResultDataAccessException.class, () ->
                sut.retrieveOrganisation(testOrganisationId, false));
    }

    @Test
    void test_RetrieveAllOrganisationsThrowExceptionWhenOrganisationEmpty() throws JsonProcessingException {
        UserProfile profile = new UserProfile(UUID.randomUUID().toString(), "email@org.com",
                "firstName", "lastName", IdamStatus.ACTIVE);

        GetUserProfileResponse userProfileResponse = new GetUserProfileResponse(profile, false);

        ObjectMapper mapper = new ObjectMapper();
        String body = mapper.writeValueAsString(userProfileResponse);

        when(userProfileFeignClient.getUserProfileById(anyString())).thenReturn(Response.builder()
                .request(mock(Request.class)).body(body, Charset.defaultCharset()).status(200).build());


        assertThrows(EmptyResultDataAccessException.class, () ->
                sut.retrieveAllOrganisations(null));
    }

    @Test
    void test_RetrieveAnOrganisationsByOrgIdentifier() throws Exception {
        superUser.setUserIdentifier(UUID.randomUUID().toString());
        List<SuperUser> users = new ArrayList<>();
        users.add(superUser);

        organisation.setStatus(ACTIVE);
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

        OrganisationEntityResponse organisationEntityResponse = sut.retrieveOrganisation(organisationIdentifier, false);

        assertThat(organisationEntityResponse).isNotNull();
        verify(organisationRepository, times(1))
                .findByOrganisationIdentifier(any(String.class));
    }

    @Test
    void test_RetrieveAnOrganisationsByWhenStatusActive() throws Exception {
        superUser.setUserIdentifier(UUID.randomUUID().toString());
        List<SuperUser> users = new ArrayList<>();
        users.add(superUser);

        organisation.setStatus(ACTIVE);
        organisation.setUsers(users);
        professionalUser.setUserIdentifier(UUID.randomUUID().toString());

        List<Organisation> organisations = new ArrayList<>();
        organisations.add(organisation);

        when(organisationRepository.findByStatusIn(List.of(ACTIVE))).thenReturn(organisations);

        ProfessionalUsersEntityResponse professionalUsersEntityResponse = new ProfessionalUsersEntityResponse();
        List<ProfessionalUsersResponse> userProfiles = new ArrayList<>();
        ProfessionalUser profile = new ProfessionalUser("firstName", "lastName",
                "email@org.com", organisation);

        ProfessionalUsersResponse userProfileResponse = new ProfessionalUsersResponse(profile);
        userProfileResponse.setUserIdentifier(UUID.randomUUID().toString());
        userProfiles.add(userProfileResponse);
        professionalUsersEntityResponse.getUsers().addAll(userProfiles);
        ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                false);
        String body = mapper.writeValueAsString(professionalUsersEntityResponse);

        when(userProfileFeignClient.getUserProfiles(any(), any(), any())).thenReturn(Response.builder()
                .request(mock(Request.class)).body(body, Charset.defaultCharset()).status(200).build());

        OrganisationsDetailResponse organisationDetailResponse
                = sut.findByOrganisationStatus(ACTIVE.name(), null);

        assertThat(organisationDetailResponse).isNotNull();
        verify(organisationRepository, times(1)).findByStatusIn(List.of(ACTIVE));
    }

    @Test
    void test_retrieveAnOrganisation() {
        Organisation organisationMock = mock(Organisation.class);
        when(organisationRepository.findByOrganisationIdentifier(organisationIdentifier)).thenReturn(organisationMock);
        when(organisationMock.getStatus()).thenReturn(ACTIVE);

        OrganisationEntityResponse organisationEntityResponse = sut.retrieveOrganisation(organisationIdentifier, false);

        assertThat(organisationEntityResponse).isNotNull();

        verify(organisationRepository, times(1))
                .findByOrganisationIdentifier(organisationIdentifier);
        verify(organisationMock, times(2)).getStatus();
        verify(organisationMock, times(1)).setUsers(anyList());
    }

    @Test
    void test_retrieveAnOrganisationByUuidNotFound() {
        when(organisationRepository.findByOrganisationIdentifier(any(String.class))).thenReturn(null);

        assertThrows(EmptyResultDataAccessException.class, () ->
                sut.retrieveOrganisation(organisationIdentifier, false));

        verify(organisationRepository, times(1))
                .findByOrganisationIdentifier(any(String.class));
    }

    @Test
    void test_retrieveAnPendingOrganisationThrowExceptionWhenOrgEmpty() {
        String status = OrganisationStatus.PENDING.name();

        assertThrows(EmptyResultDataAccessException.class, () ->
                sut.findByOrganisationStatus(status, null));
    }

    @Test
    void test_retrieveAnPendingOrganisation() {
        List<Organisation> organisations = new ArrayList<>();
        organisations.add(organisation);

        when(organisationRepository.findByStatusIn(asList(OrganisationStatus.PENDING))).thenReturn(organisations);

        OrganisationsDetailResponse organisationDetailResponse
                = sut.findByOrganisationStatus(OrganisationStatus.PENDING.name(), null);

        assertThat(organisationDetailResponse).isNotNull();
        verify(organisationRepository, times(1))
                .findByStatusIn(asList(OrganisationStatus.PENDING));
    }

    @Test
    void test_RetrieveOrganisationThrows400WhenStatusInvalid() {
        assertThrows(InvalidRequest.class,() ->
                sut.findByOrganisationStatus("this is not a status", null));
    }

    @Test
    void test_retrieveAllOrganisations() {
        List<Organisation> organisations = new ArrayList<>();
        organisations.add(organisation);
        organisations.add(organisation);

        when(organisationRepository.findAll()).thenReturn(organisations);

        OrganisationsDetailResponse organisationDetailResponse = sut.retrieveAllOrganisations(null);

        assertThat(organisationDetailResponse).isNotNull();
        verify(organisationRepository, times(1)).findAll();
    }

    @Test
    @SuppressWarnings("unchecked")
    void test_retrieveAllOrganisationsWithPagination() {
        List<Organisation> organisations = new ArrayList<>();
        organisations.add(organisation);
        organisations.add(organisation);

        Pageable pageable = mock(Pageable.class);
        Page<Organisation> orgPage = (Page<Organisation>) mock(Page.class);

        when(organisationRepository.findByStatusIn(List.of(OrganisationStatus.ACTIVE, OrganisationStatus.PENDING),
            pageable)).thenReturn(orgPage);
        when(orgPage.getContent()).thenReturn(organisations);

        OrganisationsDetailResponse organisationDetailResponse = sut.retrieveAllOrganisations(pageable);

        assertThat(organisationDetailResponse).isNotNull();
        verify(organisationRepository, times(1)).findByStatusIn(
            List.of(OrganisationStatus.ACTIVE, OrganisationStatus.PENDING),
            pageable);
    }

    @Test
    @SuppressWarnings("unchecked")
    void test_RetrieveAnOrganisationsByStatusAndPagination() throws JsonProcessingException {
        superUser.setUserIdentifier(UUID.randomUUID().toString());
        List<SuperUser> users = new ArrayList<>();
        users.add(superUser);

        organisation.setStatus(ACTIVE);
        organisation.setUsers(users);
        professionalUser.setUserIdentifier(UUID.randomUUID().toString());
        List<Organisation> organisations = new ArrayList<>();
        organisations.add(organisation);

        ProfessionalUsersEntityResponse professionalUsersEntityResponse = new ProfessionalUsersEntityResponse();
        List<ProfessionalUsersResponse> userProfiles = new ArrayList<>();
        ProfessionalUser profile = new ProfessionalUser("firstName", "lastName",
            "email@org.com", organisation);

        ProfessionalUsersResponse userProfileResponse = new ProfessionalUsersResponse(profile);
        userProfileResponse.setUserIdentifier(UUID.randomUUID().toString());
        userProfiles.add(userProfileResponse);
        professionalUsersEntityResponse.getUsers().addAll(userProfiles);
        ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
            false);
        String body = mapper.writeValueAsString(professionalUsersEntityResponse);

        Pageable pageable = PageRequest.of(1,2, Sort.by(Sort.DEFAULT_DIRECTION, ORG_NAME));
        Page<Organisation> orgPage = (Page<Organisation>) mock(Page.class);

        when(organisationRepository.findByStatusIn(List.of(ACTIVE), pageable)).thenReturn(orgPage);
        when(organisationRepository.findByStatusIn(List.of(ACTIVE), pageable).getContent())
            .thenReturn(organisations);
        when(organisationRepository.findByStatusIn(Collections.emptyList(), pageable)).thenReturn(orgPage);
        when(organisationRepository.findByStatusIn(Collections.emptyList(), pageable).getContent())
            .thenReturn(organisations);


        when(userProfileFeignClient.getUserProfiles(any(), any(), any())).thenReturn(Response.builder()
            .request(mock(Request.class)).body(body, Charset.defaultCharset()).status(200).build());

        OrganisationsDetailResponse organisationDetailResponse
            = sut.findByOrganisationStatus(ACTIVE.name(), pageable);

        assertThat(organisationDetailResponse).isNotNull();
        verify(organisationRepository, times(2))
            .findByStatusIn(List.of(ACTIVE), pageable);
        verify(organisationRepository, times(1))
            .findByStatusIn(Collections.emptyList(), pageable);
    }

    @Test
    void test_retrieveAllOrganisations_withEmptyUsers() {
        Organisation organisationMock = mock(Organisation.class);

        List<Organisation> organisations = new ArrayList<>();
        organisations.add(organisation);
        organisations.add(organisationMock);

        when(organisationRepository.findAll()).thenReturn(organisations);

        OrganisationsDetailResponse organisationDetailResponse = sut.retrieveAllOrganisations(null);

        assertThat(organisationDetailResponse).isNotNull();
        verify(organisationRepository, times(1)).findAll();
    }


    @Test
    void test_retrieveAllOrganisations_withBlockedOrganisation() {
        Organisation organisationMock = mock(Organisation.class);

        List<Organisation> organisations = new ArrayList<>();
        organisations.add(organisation);
        organisations.add(organisationMock);

        when(organisationRepository.findAll()).thenReturn(organisations);
        when(organisationMock.getStatus()).thenReturn(REVIEW);

        OrganisationsDetailResponse organisationDetailResponse = sut.retrieveAllOrganisations(null);

        assertThat(organisationDetailResponse).isNotNull();
        verify(organisationRepository, times(1)).findAll();
    }

    @Test
    void test_retrieveAnOrganisationByOrgId() {
        when(organisationRepository.findByOrganisationIdentifier(organisationIdentifier)).thenReturn(organisation);

        Organisation organisation = sut.getOrganisationByOrgIdentifier(organisationIdentifier);

        assertThat(organisation).isNotNull();
        verify(organisationRepository, times(1))
                .findByOrganisationIdentifier(organisationIdentifier);
    }


    @Test
    void test_ThrowsExceptionWhenOrganisationEmpty() {
        when(organisationRepository.findAll()).thenReturn(new ArrayList<>());

        assertThrows(EmptyResultDataAccessException.class, () ->
                sut.retrieveAllOrganisations(null));
    }


    @Test
    void test_throwInvalidRequestWhenInvalidPbaIsPassed() {
        Set<String> paymentAccountList = new HashSet<>();
        String pbaNumber = "GBA1234567";
        paymentAccountList.add(pbaNumber);

        organisationCreationRequest = new OrganisationCreationRequest("some-org-name", "PENDING", null,
                "sra-id", "false", "company-number", "company-url",
                superUserCreationRequest, paymentAccountList, contactInformationCreationRequests);

        assertThrows(InvalidRequest.class, () ->
                sut.createOrganisationFrom(organisationCreationRequest));
    }

    @Test
    void test_throwInvalidRequestWhenNullSuperUserEmailIsPassed() {
        Set<String> paymentAccountList = new HashSet<>();
        String pbaNumber = "PBA1234567";
        paymentAccountList.add(pbaNumber);

        superUserCreationRequest = new UserCreationRequest("some-fname", "some-lname",
                null);

        organisationCreationRequest = new OrganisationCreationRequest("some-org-name", "PENDING", null,
                "sra-id", "false", "company-number", "company-url",
                superUserCreationRequest, paymentAccountList, contactInformationCreationRequests);

        assertThrows(InvalidRequest.class, () ->
                sut.createOrganisationFrom(organisationCreationRequest));
    }

    @Test
    void test_updateStatusAndMessage() {
        PaymentAccount paymentAccountMock = mock(PaymentAccount.class);
        PbaStatus pbaStatusMock = mock(PbaStatus.class);
        sut.updateStatusAndMessage(paymentAccountMock, pbaStatusMock, "statusMessage");

        verify(paymentAccountMock, times(1)).setPbaStatus(pbaStatusMock);
        verify(paymentAccountMock, times(1)).setStatusMessage("statusMessage");
    }

    @Test
    void test_AllAttributesAddedToSuperUser() {
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
                .addUserAttributesToSuperUser(professionalUser,userAttributes);
        verify(professionalUserRepositoryMock, times(1)).save(any(ProfessionalUser.class));
        verify(organisationRepository, times(1)).save(any(Organisation.class));
    }

    @Test
    void test_FakeAttributesNotAdded() {
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
                .addUserAttributesToSuperUser(professionalUser, userAttributes);
    }

    @Test
    void test_AddContactInformationToOrganisation() {
        ContactInformationCreationRequest contactInformationCreationRequest
                = new ContactInformationCreationRequest("uprn","addressLine-1", "addressLine-2",
                "addressLine-3", "townCity", "county", "country",
                "postCode", dxAddressRequests);
        contactInformationCreationRequests.add(contactInformationCreationRequest);

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
    void testDeletePendingOrganisation() {
        Organisation organisation = getDeleteOrganisation(OrganisationStatus.PENDING);
        deleteOrganisationResponse = sut.deleteOrganisation(organisation, "123456789");

        assertThat(deleteOrganisationResponse).isNotNull();
        assertThat(deleteOrganisationResponse.getStatusCode()).isEqualTo(ProfessionalApiConstants.STATUS_CODE_204);
        assertThat(deleteOrganisationResponse.getMessage()).isEqualTo(ProfessionalApiConstants.DELETION_SUCCESS_MSG);
        verify(organisationRepository, times(1)).deleteById(any());
    }

    @Test
    void testDeleteActiveOrganisationWithOrgAdminPending() throws Exception {


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
        Organisation organisation = getDeleteOrganisation(ACTIVE);

        deleteOrganisationResponse = sut.deleteOrganisation(organisation, "123456789");

        assertThat(deleteOrganisationResponse).isNotNull();
        assertThat(deleteOrganisationResponse.getStatusCode()).isEqualTo(ProfessionalApiConstants.STATUS_CODE_204);
        assertThat(deleteOrganisationResponse.getMessage()).isEqualTo(ProfessionalApiConstants.DELETION_SUCCESS_MSG);
        verify(organisationRepository, times(1)).deleteById(any());
        verify(professionalUserRepositoryMock, times(1)).findByUserCountByOrganisationId(any());
        verify(userProfileFeignClient, times(1)).getUserProfileByEmail(anyString());
        verify(userProfileFeignClient, times(1)).deleteUserProfile(any());
    }

    @Test
    void testDeleteActiveOrganisationWithOrgAdminActiveGives400WithMessage() throws Exception {


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
        Organisation organisation = getDeleteOrganisation(ACTIVE);

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
    void testDeleteActiveOrganisationWithMultiUsersGives400WithMessage() {

        Organisation organisation = getDeleteOrganisation(ACTIVE);
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
    void testDeleteActiveOrganisationGives500WithMessageWhenUpDown() throws Exception {


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
        Organisation organisation = getDeleteOrganisation(ACTIVE);
        DeleteOrganisationResponse deleteOrganisationResponse = sut.deleteOrganisation(organisation, "123456789");

        assertThat(deleteOrganisationResponse).isNotNull();
        assertThat(deleteOrganisationResponse.getStatusCode()).isEqualTo(ProfessionalApiConstants.ERROR_CODE_500);
        assertThat(deleteOrganisationResponse.getMessage()).isEqualTo("Error while invoking UP");
        verify(organisationRepository, times(0)).deleteById(any());
        verify(professionalUserRepositoryMock, times(1)).findByUserCountByOrganisationId(any());
        verify(userProfileFeignClient, times(1)).getUserProfileByEmail(anyString());
        verify(userProfileFeignClient, times(1)).deleteUserProfile(any());
    }

    @Test
    void testDeleteActiveOrganisationWithNoUserProfileinUpGives500WithMessage() throws Exception {

        NewUserResponse newUserResponse = new NewUserResponse();
        newUserResponse.setIdamStatus(" ");
        ObjectMapper mapper = new ObjectMapper();
        String body = mapper.writeValueAsString(newUserResponse);

        ObjectMapper mapperOne = new ObjectMapper();
        String deleteBody = mapperOne.writeValueAsString(newUserResponse);

        when(professionalUserRepositoryMock.findByUserCountByOrganisationId(any())).thenReturn(1);
        when(userProfileFeignClient.getUserProfileByEmail(anyString())).thenReturn(Response.builder()
                .request(mock(Request.class)).body(body, Charset.defaultCharset()).status(404).build());
        Organisation organisation = getDeleteOrganisation(ACTIVE);
        DeleteOrganisationResponse deleteOrganisationResponse = sut.deleteOrganisation(organisation, "123456789");

        assertThat(deleteOrganisationResponse).isNotNull();
        assertThat(deleteOrganisationResponse.getStatusCode()).isEqualTo(ProfessionalApiConstants.ERROR_CODE_500);
        assertThat(deleteOrganisationResponse.getMessage())
                .isEqualTo(ProfessionalApiConstants.ERR_MESG_500_ADMIN_NOTFOUNDUP);
        verify(organisationRepository, times(0)).deleteById(any());
        verify(professionalUserRepositoryMock, times(1)).findByUserCountByOrganisationId(any());
        verify(userProfileFeignClient, times(1)).getUserProfileByEmail(anyString());
        verify(userProfileFeignClient, times(0)).deleteUserProfile(any());
    }

    @Test
    void testDeleteActiveOrganisationThrowsExceptionWhenUpServiceDown() throws Exception {
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

        Organisation organisation = getDeleteOrganisation(ACTIVE);
        assertThrows(ExternalApiException.class, () ->
                deleteOrganisationResponse = sut.deleteOrganisation(organisation, "123456789"));

        verify(organisationRepository, times(0)).deleteById(any());
        verify(professionalUserRepositoryMock, times(1)).findByUserCountByOrganisationId(any());
        verify(userProfileFeignClient, times(1)).getUserProfileByEmail(anyString());
        verify(userProfileFeignClient, times(1)).deleteUserProfile(any());
    }

    private Organisation getDeleteOrganisation(OrganisationStatus status) {

        ProfessionalUser profile = new ProfessionalUser("firstName", "lastName",
                "email@org.com", organisation);
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
        UserAttribute userAttribute = new UserAttribute(professionalUser, prdEnum);
        userAttributes.add(userAttribute);
        professionalUser.setUserAttributes(userAttributes);

        UserAccountMapId userAccountMapId = new UserAccountMapId(professionalUser, paymentAccount);
        UserAccountMap userAccountMap = new UserAccountMap(userAccountMapId);
        userAccountMaps.add(userAccountMap);
        organisation.addPaymentAccount(paymentAccount);
        return organisation;
    }

    @Test
    @SuppressWarnings("unchecked")
    void test_getOrganisationsByPbaStatus() {

        List<Organisation> organisations = getOrgsWithPbaSetup();
        when(organisationRepository.findByPbaStatus(ACCEPTED)).thenReturn(organisations);

        ResponseEntity<Object> responseEntity = sut.getOrganisationsByPbaStatus(ACCEPTED.toString());

        assertNotNull(responseEntity);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertNotNull(responseEntity.getBody());

        var orgsWithPbas = (List<OrganisationsWithPbaStatusResponse>) responseEntity.getBody();
        assertThat(orgsWithPbas).hasSize(2);
        assertThat(orgsWithPbas.get(0).getOrganisationIdentifier()).isEqualTo("ABCDEFG1");
        assertThat(orgsWithPbas.get(0).getPbaNumbers()).hasSize(1);
        assertThat(orgsWithPbas.get(1).getOrganisationIdentifier()).isEqualTo("ABCDEFG2");
        assertThat(orgsWithPbas.get(0).getPbaNumbers()).hasSize(1);

        verify(organisationRepository, times(1)).findByPbaStatus(ACCEPTED);
    }

    @Test
    void test_getOrganisationsByPbaStatus_invalidPbaStatus_throws400() {
        assertThrows(InvalidRequest.class,() ->
                sut.getOrganisationsByPbaStatus("I N V A L I D"));
    }

    private List<Organisation> getOrgsWithPbaSetup() {
        PaymentAccount pba1 = new PaymentAccount();
        pba1.setPbaStatus(ACCEPTED);
        pba1.setPbaNumber("PBA123456");
        pba1.setCreated(LocalDateTime.now());
        pba1.setLastUpdated(LocalDateTime.now());

        PaymentAccount pba2 = new PaymentAccount();
        pba2.setPbaStatus(PENDING);
        pba2.setPbaNumber("PBA123457");
        pba1.setCreated(LocalDateTime.now());
        pba1.setLastUpdated(LocalDateTime.now());

        List<PaymentAccount> pbas = new ArrayList<>();
        pbas.add(pba1);
        pbas.add(pba2);

        String orgId1 = "ABCDEFG1";
        Organisation org1 = new Organisation();
        org1.setStatus(ACTIVE);
        org1.setOrganisationIdentifier(orgId1);
        org1.setPaymentAccounts(pbas);

        String orgId2 = "ABCDEFG2";
        Organisation org2 = new Organisation();
        org2.setStatus(ACTIVE);
        org2.setOrganisationIdentifier(orgId2);
        org2.setPaymentAccounts(pbas);

        List<Organisation> orgs = new ArrayList<>();
        orgs.add(org1);
        orgs.add(org2);

        return orgs;
    }


    @Test
    void test_addPaymentAccountsToOrganisation() {
        var pbas = new HashSet<String>();
        pbas.add("PBA0000001");
        PbaRequest pbaRequest = new PbaRequest();
        pbaRequest.setPaymentAccounts(pbas);

        organisation.setStatus(OrganisationStatus.ACTIVE);
        organisation.setUsers(asList(superUser));
        when(organisationRepository.findByOrganisationIdentifier(any())).thenReturn(organisation);

        String orgId = UUID.randomUUID().toString().substring(0, 7);
        String userId = UUID.randomUUID().toString();
        ResponseEntity<Object> responseEntity = sut.addPaymentAccountsToOrganisation(pbaRequest, orgId, userId);
        assertThat(responseEntity.getBody()).isNull();
        verify(professionalUserServiceMock, times(1)).checkUserStatusIsActiveByUserId(any());
    }

    @Test
    void test_addPaymentAccountsToOrganisation_pba_invalid() {
        var pbas = new HashSet<String>();
        pbas.add("PBA00000012");
        PbaRequest pbaRequest = new PbaRequest();
        pbaRequest.setPaymentAccounts(pbas);
        AddPbaResponse addPbaResponse = new AddPbaResponse();
        ResponseEntity<Object> responseEntity = ResponseEntity
                .status(200)
                .body(addPbaResponse);

        String orgId = UUID.randomUUID().toString().substring(0, 7);
        String userId = UUID.randomUUID().toString();
        organisation.setStatus(ACTIVE);
        when(organisationRepository.findByOrganisationIdentifier(any())).thenReturn(organisation);

        responseEntity = sut.addPaymentAccountsToOrganisation(pbaRequest, orgId, userId);
        assertThat(responseEntity.getBody()).isNotNull();
        verify(professionalUserServiceMock, times(1)).checkUserStatusIsActiveByUserId(any());
    }

    @Test
    void test_addPaymentAccountsToOrganisation_pbaDb_NoMatch() {
        var pbas = new HashSet<String>();
        pbas.add("PBA00000012");
        PbaRequest pbaRequest = new PbaRequest();
        pbaRequest.setPaymentAccounts(pbas);
        organisation.setStatus(ACTIVE);
        when(organisationRepository.findByOrganisationIdentifier(any())).thenReturn(organisation);

        PaymentAccount paymentAccount = new PaymentAccount();
        paymentAccount.setPbaNumber("PBA1234568");
        List<PaymentAccount> paymentAccounts = new ArrayList<>();
        paymentAccounts.add(paymentAccount);
        when(paymentAccountRepositoryMock.findByPbaNumber(anyString())).thenReturn(Optional.of(paymentAccount));

        ResponseEntity<Object> responseEntity = sut.addPaymentAccountsToOrganisation(pbaRequest,
                UUID.randomUUID().toString().substring(0, 7), UUID.randomUUID().toString());
        assertThat(responseEntity.getBody()).isNotNull();
        verify(professionalUserServiceMock, times(1)).checkUserStatusIsActiveByUserId(any());

    }

    @Test
    void test_addPaymentAccountsToOrganisationEmpty() {
        var pbas = new HashSet<String>();
        PbaRequest pbaRequest = new PbaRequest();
        pbaRequest.setPaymentAccounts(pbas);

        String orgId = UUID.randomUUID().toString().substring(0, 7);
        String userId = UUID.randomUUID().toString();
        when(organisationRepository.findByOrganisationIdentifier(any())).thenReturn(null);

        assertThrows(ResourceNotFoundException.class,() ->
                sut.addPaymentAccountsToOrganisation(pbaRequest, orgId, userId));
    }

    @Test
    void test_addPaymentAccountsToOrganisation_pba_valid_And_invalid() {
        var pbas = new HashSet<String>();
        pbas.add("PBA0000001");
        pbas.add("test");
        PbaRequest pbaRequest = new PbaRequest();
        pbaRequest.setPaymentAccounts(pbas);
        AddPbaResponse addPbaResponse = new AddPbaResponse();

        organisation.setStatus(ACTIVE);
        organisation.setUsers(asList(superUser));
        when(organisationRepository.findByOrganisationIdentifier(any())).thenReturn(organisation);

        String orgId = UUID.randomUUID().toString().substring(0, 7);
        String userId = UUID.randomUUID().toString();
        ResponseEntity<Object> responseEntity = sut.addPaymentAccountsToOrganisation(pbaRequest, orgId, userId);
        AddPbaResponse response = (AddPbaResponse) responseEntity.getBody();

        assertThat(response).isNotNull();
        assertThat(response.getReason()).isNotNull();
        assertThat(response.getMessage()).isEqualTo(ERROR_MSG_PARTIAL_SUCCESS);
        verify(professionalUserServiceMock, times(1)).checkUserStatusIsActiveByUserId(any());
    }

    @Test
    void should_add_contact_informations_to_organisation() {

        Organisation organisationMock = mock(Organisation.class);
        final String orgUUId = generateUniqueAlphanumericId(LENGTH_OF_ORGANISATION_IDENTIFIER);
        when(organisationRepository.findByOrganisationIdentifier(orgUUId)).thenReturn(organisationMock);

        dxAddressRequest = new DxAddressCreationRequest("DX 1234567890", "dxExchange");
        dxAddressRequests.add(dxAddressRequest);

        contactInformationCreationRequest = new ContactInformationCreationRequest("uprn", "addressLine-1",
                "addressLine-2", "addressLine-3", "townCity", "county",
                "country", "postCode", dxAddressRequests);

        contactInformationCreationRequests.add(contactInformationCreationRequest);


        sut.addContactInformationsToOrganisation(contactInformationCreationRequests, orgUUId);


        verify(organisationRepository, times(1))
                .findByOrganisationIdentifier(orgUUId);
        verify(contactInformationRepositoryMock, times(2)).save(any(ContactInformation.class));
    }


    void test_sortContactInfoByCreatedDateAsc() {
        var contactInformation = new ContactInformation();
        contactInformation.setCountry("TestCountry");
        contactInformation.setCreated(LocalDateTime.now());

        var contactInformation1 = new ContactInformation();
        contactInformation1.setCountry("TestAnotherCountry");
        contactInformation1.setCreated(LocalDateTime.now());
        organisation.setContactInformations(List.of(contactInformation1, contactInformation));
        organisation.setStatus(ACTIVE);
        when(organisationRepository.findByOrganisationIdentifier(any())).thenReturn(organisation);

        var organisationEntityResponse =
                sut.retrieveOrganisation(organisationIdentifier, false);

        assertEquals("TestCountry", organisationEntityResponse.getContactInformation().get(0).getCountry());
        assertEquals("TestAnotherCountry",
                organisationEntityResponse.getContactInformation().get(1).getCountry());
    }

    @Test
    void testDeleteMultipleAddressOfGivenOrganisation() {
        var addressIds = new HashSet<UUID>();
        addressIds.add(UUID.randomUUID());
        doNothing().when(contactInformationRepositoryMock).deleteByIdIn(anySet());
        sut.deleteMultipleAddressOfGivenOrganisation(addressIds);
        verify(contactInformationRepositoryMock, times(1)).deleteByIdIn(anySet());
    }

}