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
    private final ProfessionalUserRepository professionalUserRepository = mock(ProfessionalUserRepository.class);
    private final PaymentAccountRepository paymentAccountRepository = mock(PaymentAccountRepository.class);
    private final OrganisationRepository organisationRepository = mock(OrganisationRepository.class);
    private final ContactInformationRepository contactInformationRepository = mock(ContactInformationRepository.class);
    private final DxAddressRepository dxAddressRepository = mock(DxAddressRepository.class);
    private final DxAddressCreationRequest dxAddressCreationRequest = mock(DxAddressCreationRequest.class);
    private OrganisationServiceImpl organisationServiceImpl = mock(OrganisationServiceImpl.class);

    private final ProfessionalUser professionalUser = mock(ProfessionalUser.class);
    private final Organisation organisation = mock(Organisation.class);
    private final PaymentAccount paymentAccount = mock(PaymentAccount.class);
    private final ContactInformation contactInformation = mock(ContactInformation.class);
    private final DxAddress dxAddress = mock(DxAddress.class);
    private final OrganisationResponse organisationResponse = mock(OrganisationResponse.class);

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

        dxAddressRequest.setIsDxRequestValid(true);

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

        organisationServiceImpl = new OrganisationServiceImpl(
                organisationRepository,
                professionalUserRepository,
                paymentAccountRepository,
                dxAddressRepository,
                contactInformationRepository);



        organisationCreationRequest =
                new OrganisationCreationRequest(
                        "some-org-name", OrganisationStatus.PENDING, "sra-id",Boolean.FALSE,"company-number","company-url",
                        superUser,
                        pbaAccountCreationRequests, contactInformationCreationRequests);

        when(organisation.getId()).thenReturn(UUID.randomUUID());

        when(organisation.getOrganisationIdentifier()).thenReturn(UUID.randomUUID());

        when(professionalUserRepository.save(any(ProfessionalUser.class)))
                .thenReturn(professionalUser);

        when(organisationRepository.save(any(Organisation.class)))
                .thenReturn(organisation);

        when(paymentAccountRepository.save(any(PaymentAccount.class)))
                .thenReturn(paymentAccount);

        when(contactInformationRepository.save(any(ContactInformation.class)))
                .thenReturn(contactInformation);

        when(dxAddressRepository.save(any(DxAddress.class)))
                .thenReturn(dxAddress);

        when(dxAddressCreationRequest.getIsDxRequestValid())
                .thenReturn(true);

        when(organisationRepository.findAll())
                .thenReturn(organisations);

        when(organisationRepository.findByOrganisationIdentifier(any()))
                .thenReturn(organisation);
    }

    @Test
    public void saves_an_organisation() {

        OrganisationResponse organisationResponse =
                organisationServiceImpl.createOrganisationFrom(organisationCreationRequest);

        assertThat(organisationResponse).isNotNull();

        verify(
                organisationRepository,
                times(2)).save(any(Organisation.class));
        verify(
                professionalUserRepository,
                times(1)).save(any(ProfessionalUser.class));
        verify(
                paymentAccountRepository,
                times(1)).save(any(PaymentAccount.class));
        verify(
                contactInformationRepository,
                times(2)).save(any(ContactInformation.class));
        verify(
                dxAddressRepository,
                times(1)).save(any(DxAddress.class));
        verify(
                contactInformation,
                times(1)).addDxAddress(any(DxAddress.class));
        verify(
                organisation,
                times(1)).addContactInformation(any(ContactInformation.class));
        verify(
                organisation,
                times(1)).addPaymentAccount(any(PaymentAccount.class));
    }

    @Test
    public void retrieve_an_organisations() {

        OrganisationsDetailResponse organisationDetailResponse =
                organisationServiceImpl.retrieveOrganisations();

        assertThat(organisationDetailResponse).isNotNull();

        verify(
                organisationRepository,
                times(1)).findAll();
    }

    @Test
    public void updates_an_organisation() {
        OrganisationResponse organisationResponse =
            organisationServiceImpl.updateOrganisation(organisationCreationRequest, UUID.randomUUID());

        assertThat(organisationResponse).isNotNull();
        verify(
            organisationRepository,
                times(1)).findByOrganisationIdentifier(any());

        verify(
            organisationRepository,
                times(1)).save(any(Organisation.class));

        verify(
                organisation,
                times(1)).setName(any());

        verify(
                organisation,
                times(1)).setStatus(any());

        verify(
                organisation,
                times(1)).setSraId(any());

        verify(
                organisation,
                times(1)).setCompanyNumber(any());

        verify(
                organisation,
                times(1)).setSraRegulated(any());

        verify(
                organisation,
                times(1)).setCompanyUrl(any());
    }
}