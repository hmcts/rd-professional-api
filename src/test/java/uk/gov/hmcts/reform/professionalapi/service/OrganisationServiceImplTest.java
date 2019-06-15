package uk.gov.hmcts.reform.professionalapi.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.when;
import static uk.gov.hmcts.reform.professionalapi.generator.ProfessionalApiGenerator.LENGTH_OF_ORGANISATION_IDENTIFIER;
import static uk.gov.hmcts.reform.professionalapi.generator.ProfessionalApiGenerator.generateUniqueAlphanumericId;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.assertj.core.api.Assertions;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.Before;
import org.junit.Test;

import org.mockito.Mockito;
import org.springframework.dao.EmptyResultDataAccessException;
import uk.gov.hmcts.reform.professionalapi.controller.request.ContactInformationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.DxAddressCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.PbaAccountCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.UserCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.response.OrganisationEntityResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.OrganisationResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.OrganisationsDetailResponse;
import uk.gov.hmcts.reform.professionalapi.domain.ContactInformation;
import uk.gov.hmcts.reform.professionalapi.domain.DxAddress;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus;
import uk.gov.hmcts.reform.professionalapi.domain.PaymentAccount;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;
import uk.gov.hmcts.reform.professionalapi.domain.UserAccountMap;
import uk.gov.hmcts.reform.professionalapi.domain.UserAccountMapId;
import uk.gov.hmcts.reform.professionalapi.persistence.ContactInformationRepository;
import uk.gov.hmcts.reform.professionalapi.persistence.DxAddressRepository;
import uk.gov.hmcts.reform.professionalapi.persistence.OrganisationRepository;
import uk.gov.hmcts.reform.professionalapi.persistence.PaymentAccountRepository;
import uk.gov.hmcts.reform.professionalapi.persistence.PrdEnumRepository;
import uk.gov.hmcts.reform.professionalapi.persistence.ProfessionalUserRepository;
import uk.gov.hmcts.reform.professionalapi.persistence.UserAccountMapRepository;
import uk.gov.hmcts.reform.professionalapi.persistence.UserAttributeRepository;
import uk.gov.hmcts.reform.professionalapi.service.impl.OrganisationServiceImpl;

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
    private final Organisation organisationMock = mock(Organisation.class);
    private final PaymentAccount paymentAccountMock = mock(PaymentAccount.class);
    private final ContactInformation contactInformationMock = mock(ContactInformation.class);
    private final DxAddress dxAddressMock = mock(DxAddress.class);
    private final UserAccountMap userAccountMapMock = mock(UserAccountMap.class);
    private final UserAccountMapId userAccountMapIdMock = mock(UserAccountMapId.class);
    private final OrganisationResponse organisationResponseMock = mock(OrganisationResponse.class);
    private final OrganisationsDetailResponse organisationDetailResponseMock = mock(OrganisationsDetailResponse.class);
    private final OrganisationEntityResponse organisationEntityResponseMock = mock(OrganisationEntityResponse.class);
    private final OrganisationRepository organisationRepositoryNullReturnedMock = mock(OrganisationRepository.class);
    private final String organisationIdentifier = generateUniqueAlphanumericId(LENGTH_OF_ORGANISATION_IDENTIFIER);

    private UserCreationRequest superUser;
    private List<PbaAccountCreationRequest> pbaAccountCreationRequests;
    private PbaAccountCreationRequest pbaAccountCreationRequest;
    private List<ContactInformationCreationRequest> contactInformationCreationRequests;
    private List<DxAddressCreationRequest> dxAddressRequests;
    private DxAddressCreationRequest dxAddressRequest;
    private ContactInformationCreationRequest contactInformationCreationRequest;
    private OrganisationCreationRequest organisationCreationRequest;
    private List<Organisation> organisations;
    private List<UserAccountMap> userAccountMaps;
    private List<PaymentAccount> paymentAccounts;


    @Before
    public void setUp() {

        superUser = new UserCreationRequest(
                "some-fname",
                "some-lname",
                "some-email"
        );

        List<String> paymentAccountList = new ArrayList<>();

        String pbaNumber = "pbaNumber-1";

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
                userAttributeRepositoryMock, prdEnumRepositoryMock,
                userAccountMapRepositoryMock);

        organisationCreationRequest =
                new OrganisationCreationRequest(
                        "some-org-name", OrganisationStatus.PENDING, "sra-id", Boolean.FALSE, "company-number", "company-url",
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

        OrganisationResponse organisationResponse =
                organisationServiceImplMock.createOrganisationFrom(organisationCreationRequest);

        assertThat(organisationResponse).isNotNull();

        verify(
                organisationRepositoryMock,
                times(2)).save(any(Organisation.class));
        verify(
                professionalUserRepositoryMock,
                times(1)).save(any(ProfessionalUser.class));
        verify(
                paymentAccountRepositoryMock,
                times(1)).save(any(PaymentAccount.class));
        verify(
                contactInformationRepositoryMock,
                times(2)).save(any(ContactInformation.class));
        verify(
                dxAddressRepositoryMock,
                times(1)).save(any(DxAddress.class));

        verify(
                contactInformationMock,
                times(1)).addDxAddress(any(DxAddress.class));
        verify(
                organisationMock,
                times(1)).addContactInformation(any(ContactInformation.class));
        verify(
                organisationMock,
                times(1)).addPaymentAccount(any(PaymentAccount.class));
        verify(
                organisationMock,
                times(1)).addProfessionalUser(any(ProfessionalUser.class));
        verify(
                userAccountMapRepositoryMock,
                times(1)).save(any(UserAccountMap.class));

    }

    @Test
    public void testSavesOrganisationWithConstraintViolationException() {

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
    public void testRetrieveOrganisations() {

        ArrayList<ProfessionalUser> users = new ArrayList<>();
        ArrayList<Organisation> organisations = new ArrayList<>();
        users.add(professionalUserMock);
        organisations.add(organisationMock);

        when(organisationMock.getUsers())
                .thenReturn(users);

        when(organisationRepositoryMock.findAll())
                .thenReturn(organisations);

        OrganisationsDetailResponse organisationDetailResponse =
                organisationServiceImplMock.retrieveOrganisations();

        assertThat(organisationDetailResponse).isNotNull();

        verify(
                organisationRepositoryMock,
                times(1)).findAll();

        verify(
                organisationMock,
                times(1)).setUsers(any());
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
                prdEnumRepositoryMock,
                userAccountMapRepositoryMock);
        realOrganisationService.retrieveOrganisation(testOrganisationId);
    }

    @Test
    public void testRetrieveAnOrganisationsByOrganisationIdentifier() {

        ArrayList<ProfessionalUser> users = new ArrayList<>();
        users.add(professionalUserMock);

        when(organisationMock.getUsers())
                .thenReturn(users);

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

    @Test(expected = EmptyResultDataAccessException.class)
    public void retrieveAnOrganisationByUuidNotFound() {

        Mockito.when(organisationRepositoryMock.findByOrganisationIdentifier(any(String.class)))
                .thenReturn(null);

        organisationServiceImplMock.retrieveOrganisation(organisationIdentifier);
    }

    @Test
    public void getOrganisationByOrganisationIdentifier() {

        Organisation organisation =
                organisationServiceImplMock.getOrganisationByOrganisationIdentifier(organisationIdentifier);

        assertThat(organisation).isNotNull();

        verify(
                organisationRepositoryMock,
                times(1)).findByOrganisationIdentifier(any());
    }

}