package uk.gov.hmcts.reform.professionalapi.domain.service;

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

import uk.gov.hmcts.reform.professionalapi.domain.entities.ContactInformation;
import uk.gov.hmcts.reform.professionalapi.domain.entities.DXAddress;
import uk.gov.hmcts.reform.professionalapi.domain.entities.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.entities.PaymentAccount;
import uk.gov.hmcts.reform.professionalapi.domain.entities.ProfessionalUser;
import uk.gov.hmcts.reform.professionalapi.domain.service.persistence.ContactInformationRepository;
import uk.gov.hmcts.reform.professionalapi.domain.service.persistence.DXAddressRepository;
import uk.gov.hmcts.reform.professionalapi.domain.service.persistence.OrganisationRepository;
import uk.gov.hmcts.reform.professionalapi.domain.service.persistence.PaymentAccountRepository;
import uk.gov.hmcts.reform.professionalapi.domain.service.persistence.ProfessionalUserRepository;
import uk.gov.hmcts.reform.professionalapi.infrastructure.controllers.request.ContactInformationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.infrastructure.controllers.request.DXAddressCreationRequest;
import uk.gov.hmcts.reform.professionalapi.infrastructure.controllers.request.OrganisationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.infrastructure.controllers.request.PbaAccountCreationRequest;
import uk.gov.hmcts.reform.professionalapi.infrastructure.controllers.request.UserCreationRequest;
import uk.gov.hmcts.reform.professionalapi.infrastructure.controllers.response.OrganisationResponse;

public class OrganisationServiceTest {

    private final ProfessionalUserRepository professionalUserRepository = mock(ProfessionalUserRepository.class);
    private final PaymentAccountRepository paymentAccountRepository = mock(PaymentAccountRepository.class);
    private final OrganisationRepository organisationRepository = mock(OrganisationRepository.class);
    private final ContactInformationRepository contactInformationRepository = mock(ContactInformationRepository.class);
    private final DXAddressRepository dxAddressRepository = mock(DXAddressRepository.class);

    private final ProfessionalUser professionalUser = mock(ProfessionalUser.class);
    private final Organisation organisation = mock(Organisation.class);
    private final PaymentAccount paymentAccount = mock(PaymentAccount.class);
    private final ContactInformation contactInformation = mock(ContactInformation.class);
    private final DXAddress dxAddress = mock(DXAddress.class);
    
    private UserCreationRequest superUser;
    private List<PbaAccountCreationRequest> pbaAccountCreationRequests;
    private PbaAccountCreationRequest pbaAccountCreationRequest;
    private List<ContactInformationCreationRequest> contactInformationCreationRequests;
    private List<DXAddressCreationRequest> dxAddressRequests;
    
    private DXAddressCreationRequest dxAddressRequest ;
    
    private ContactInformationCreationRequest contactInformationCreationRequest;
    
    private OrganisationCreationRequest organisationCreationRequest ;
    
    private OrganisationService organisationService ;

    @Before
    public void setUp() {
    	
    	superUser = new UserCreationRequest(
                "some-fname",
                "some-lname",
                "some-email");
    	
    	pbaAccountCreationRequests = new ArrayList<>();
    	
    	contactInformationCreationRequests = new ArrayList<>();
    	
        dxAddressRequests = new ArrayList<>();
        
       	pbaAccountCreationRequest = new PbaAccountCreationRequest("pbaNumber-1");

        pbaAccountCreationRequests.add(pbaAccountCreationRequest);
        
        dxAddressRequest= new DXAddressCreationRequest("DXNumber-1", "dxExchange");
        
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
        
        organisationService = new OrganisationService(
                organisationRepository,
                professionalUserRepository,
                paymentAccountRepository, 
                dxAddressRepository, 
                contactInformationRepository);
        
        organisationCreationRequest =
                new OrganisationCreationRequest(
                        "some-org-name","sra-id",Boolean.FALSE,"company-number","company-url",
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
        
        when(dxAddressRepository.save(any(DXAddress.class)))
        .thenReturn(dxAddress);

    }

    @Test
    public void saves_an_organisation() {

        

        OrganisationResponse organisationResponse =
                organisationService.createOrganisationFrom(organisationCreationRequest);

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
                times(1)).save(any(DXAddress.class));
        verify(
                contactInformation,
                times(1)).addDXAddress(any(DXAddress.class));
        verify(
                organisation,
                times(1)).addContactInformation(any(ContactInformation.class));
        verify(
                organisation,
                times(1)).addPaymentAccount(any(PaymentAccount.class));
           }
}