package uk.gov.hmcts.reform.professionalapi.service;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import org.springframework.web.client.HttpClientErrorException;
import uk.gov.hmcts.reform.professionalapi.controller.request.ContactInformationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.DxAddressCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.PbaAccountCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.UserCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.response.OrganisationResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.OrganisationsDetailResponse;
import uk.gov.hmcts.reform.professionalapi.domain.ContactInformation;
import uk.gov.hmcts.reform.professionalapi.domain.DxAddress;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus;
import uk.gov.hmcts.reform.professionalapi.domain.PaymentAccount;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;
import uk.gov.hmcts.reform.professionalapi.persistence.ContactInformationRepository;
import uk.gov.hmcts.reform.professionalapi.persistence.DxAddressRepository;
import uk.gov.hmcts.reform.professionalapi.persistence.OrganisationRepository;
import uk.gov.hmcts.reform.professionalapi.persistence.PaymentAccountRepository;
import uk.gov.hmcts.reform.professionalapi.persistence.ProfessionalUserRepository;
import uk.gov.hmcts.reform.professionalapi.service.impl.OrganisationServiceImpl;


public class OrganisationServiceImplTest {
    private final ProfessionalUserRepository professionalUserRepositoryMock = mock(ProfessionalUserRepository.class);
    private final PaymentAccountRepository paymentAccountRepositoryMock = mock(PaymentAccountRepository.class);
    private final OrganisationRepository organisationRepositoryMock = mock(OrganisationRepository.class);
    private final OrganisationRepository organisationRepositoryMockNullReturned = mock(OrganisationRepository.class);
    private final ContactInformationRepository contactInformationRepositoryMock = mock(ContactInformationRepository.class);
    private final DxAddressRepository dxAddressRepositoryMock = mock(DxAddressRepository.class);
    private OrganisationServiceImpl organisationServiceImplMock = mock(OrganisationServiceImpl.class);
    private OrganisationService  organisationServiceMock = mock(OrganisationService.class);

    private final ProfessionalUser professionalUserMock = mock(ProfessionalUser.class);
    private final Organisation organisationMock = mock(Organisation.class);
    private final PaymentAccount paymentAccountMock = mock(PaymentAccount.class);
    private final ContactInformation contactInformationMock = mock(ContactInformation.class);
    private final DxAddress dxAddressMock = mock(DxAddress.class);
    private final OrganisationResponse organisationResponseMock = mock(OrganisationResponse.class);
    private final OrganisationsDetailResponse organisationDetailResponseMock = mock(OrganisationsDetailResponse.class);

    private UserCreationRequest superUser;
    private List<PbaAccountCreationRequest> pbaAccountCreationRequests;
    private PbaAccountCreationRequest pbaAccountCreationRequest;
    private List<ContactInformationCreationRequest> contactInformationCreationRequests;
    private List<DxAddressCreationRequest> dxAddressRequests;
    private DxAddressCreationRequest dxAddressRequest;
    private ContactInformationCreationRequest contactInformationCreationRequest;
    private OrganisationCreationRequest organisationCreationRequest;
    private List<Organisation> organisations;

    @Before
    public void setUp() {

        superUser = new UserCreationRequest(
                "some-fname",
                "some-lname",
                "some-email");

        pbaAccountCreationRequests = new ArrayList<>();

        contactInformationCreationRequests = new ArrayList<>();

        dxAddressRequests = new ArrayList<>();

        organisations = new ArrayList<Organisation>();

        pbaAccountCreationRequest = new PbaAccountCreationRequest("pbaNumber-1");

        pbaAccountCreationRequests.add(pbaAccountCreationRequest);

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
                contactInformationRepositoryMock);



        organisationCreationRequest =
                new OrganisationCreationRequest(
                        "some-org-name", OrganisationStatus.PENDING, "sra-id",Boolean.FALSE,"company-number","company-url",
                        superUser,
                        pbaAccountCreationRequests, contactInformationCreationRequests);

        when(organisationMock.getId()).thenReturn(UUID.randomUUID());

        when(organisationMock.getOrganisationIdentifier()).thenReturn(UUID.randomUUID());

        when(professionalUserRepositoryMock.save(any(ProfessionalUser.class)))
                .thenReturn(professionalUserMock);

        when(organisationRepositoryMock.save(any(Organisation.class)))
                .thenReturn(organisationMock);

        when(paymentAccountRepositoryMock.save(any(PaymentAccount.class)))
                .thenReturn(paymentAccountMock);

        when(contactInformationRepositoryMock.save(any(ContactInformation.class)))
                .thenReturn(contactInformationMock);

        when(dxAddressRepositoryMock.save(any(DxAddress.class)))
                .thenReturn(dxAddressMock);

        when(organisationRepositoryMock.findAll())
                .thenReturn(organisations);

        when(organisationRepositoryMock.findByOrganisationIdentifier(any()))
                .thenReturn(organisationMock);

        when(organisationRepositoryMockNullReturned.findByOrganisationIdentifier(any()))
                .thenReturn(null);

        when(organisationRepositoryMock.findByStatus(any()))
                .thenReturn(organisations);
    }

    @Test
    public void saves_an_organisation() {

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
    }

    @Test
    public void retrieve_an_organisations() {

        OrganisationsDetailResponse organisationDetailResponse =
                organisationServiceImplMock.retrieveOrganisations();

        assertThat(organisationDetailResponse).isNotNull();

        verify(
                organisationRepositoryMock,
                times(1)).findAll();
    }

    @Test
    public void updates_an_organisation() {
        OrganisationResponse organisationResponse =
                organisationServiceImplMock.updateOrganisation(organisationCreationRequest, UUID.randomUUID());

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

    @Test
    public void retrieve_an_organisations_by_status() {

        OrganisationsDetailResponse organisationDetailResponse =
                organisationServiceImplMock.findByOrganisationStatus(OrganisationStatus.ACTIVE);

        assertThat(organisationDetailResponse).isNotNull();

        verify(
                organisationRepositoryMock,
                times(1)).findByStatus(any());

    }

    @Test
    public void getOrganisationByOrganisationIdentifierTest() {

        organisationMock.setId(UUID.randomUUID());
        UUID expectOrganisationId = organisationMock.getId();

        when(organisationServiceImplMock.getOrganisationByOrganisationIdentifier(expectOrganisationId)).thenReturn(organisationMock);
        assertThat(expectOrganisationId).isEqualByComparingTo(organisationMock.getId());
        assertThat(organisationMock).isNotNull();

    }

    @Test(expected = HttpClientErrorException.class)
    public void throwsExceptionWhenOrganisationIsNull() throws Exception {

        Organisation testOrganisation = new Organisation();
        testOrganisation.setId(UUID.randomUUID());
        UUID testOrganisationId = testOrganisation.getId();

        OrganisationService realOrganisationService = new OrganisationServiceImpl(organisationRepositoryMockNullReturned, professionalUserRepositoryMock, paymentAccountRepositoryMock, dxAddressRepositoryMock, contactInformationRepositoryMock);
        realOrganisationService.retrieveOrganisation(testOrganisationId);
    }

}