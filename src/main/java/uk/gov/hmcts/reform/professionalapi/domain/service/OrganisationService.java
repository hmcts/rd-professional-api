package uk.gov.hmcts.reform.professionalapi.domain.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.professionalapi.domain.entities.ContactInformation;
import uk.gov.hmcts.reform.professionalapi.domain.entities.DXAddress;
import uk.gov.hmcts.reform.professionalapi.domain.entities.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.entities.OrganisationStatus;
import uk.gov.hmcts.reform.professionalapi.domain.entities.PaymentAccount;
import uk.gov.hmcts.reform.professionalapi.domain.entities.ProfessionalUser;
import uk.gov.hmcts.reform.professionalapi.domain.entities.ProfessionalUserStatus;
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

@Service
@Slf4j
public class OrganisationService {

    private final OrganisationRepository organisationRepository;
    private final ProfessionalUserRepository professionalUserRepository;
    private final PaymentAccountRepository paymentAccountRepository;
    private final DXAddressRepository dxAddressRepository;
    private final ContactInformationRepository contactInformationRepository;
    
    public OrganisationService(
    		OrganisationRepository organisationRepository,
            ProfessionalUserRepository professionalUserRepository,
            PaymentAccountRepository paymentAccountRepository,
            DXAddressRepository dxAddressRepository,
            ContactInformationRepository contactInformationRepository) {

        this.organisationRepository = organisationRepository;
        this.professionalUserRepository = professionalUserRepository;
        this.paymentAccountRepository = paymentAccountRepository;
        this.contactInformationRepository = contactInformationRepository;
        this.dxAddressRepository = dxAddressRepository;
    }

    @Transactional
    public OrganisationResponse createOrganisationFrom(
            OrganisationCreationRequest organisationCreationRequest) {

        Organisation newOrganisation = new Organisation(
                organisationCreationRequest.getName(),
                OrganisationStatus.PENDING.name(),
                organisationCreationRequest.getSraId(),
                organisationCreationRequest.getCompanyNumber(),
                organisationCreationRequest.getSraRegulated(),
                organisationCreationRequest.getCompanyUrl()
        );

        Organisation organisation = organisationRepository.save(newOrganisation);

        addPbaAccountToOrganisation(organisationCreationRequest.getPbaAccounts(), organisation);

        addSuperUserToOrganisation(organisationCreationRequest.getSuperUser(), organisation);

        addContactInformationToOrganisation(organisationCreationRequest.getContactInformation(), organisation);

        organisationRepository.save(organisation);

        return new OrganisationResponse(organisation);
    }

    private void addPbaAccountToOrganisation(
            List<PbaAccountCreationRequest> pbaAccountCreationRequest,
            Organisation organisation) {

        if (pbaAccountCreationRequest != null) {
            pbaAccountCreationRequest.forEach(pbaAccount -> {
                PaymentAccount paymentAccount = new PaymentAccount(pbaAccount.getPbaNumber());
                paymentAccount.setOrganisation(organisation);
                PaymentAccount persistedPaymentAccount = paymentAccountRepository.save(paymentAccount);
                organisation.addPaymentAccount(persistedPaymentAccount);
            });
        }
    }

    private void addSuperUserToOrganisation(
            UserCreationRequest userCreationRequest,
            Organisation organisation) {

        ProfessionalUser newProfessionalUser = new ProfessionalUser(
                userCreationRequest.getFirstName(),
                userCreationRequest.getLastName(),
                userCreationRequest.getEmail(),
                ProfessionalUserStatus.PENDING.name(),
                organisation);

        ProfessionalUser persistedSuperUser = professionalUserRepository.save(newProfessionalUser);

        organisation.addProfessionalUser(persistedSuperUser);
    }
    
    private void addContactInformationToOrganisation(
    		List<ContactInformationCreationRequest> contactInformationCreationRequest,
            Organisation organisation) {
    	
    	if (contactInformationCreationRequest != null) {
    		contactInformationCreationRequest.forEach(contactInfo -> {
                ContactInformation newContactInformation = new ContactInformation(contactInfo.getAddressLine1(),
                		contactInfo.getAddressLine2(),
                		contactInfo.getAddressLine3(),
                		contactInfo.getTownCity(),
                		contactInfo.getCounty(),
                		contactInfo.getCountry(),
                		contactInfo.getPostCode(),
            			organisation);
                
                ContactInformation contactInformation = contactInformationRepository.save(newContactInformation);
                
                addDXAddressToContactInformation(contactInfo.getDxAddress(), contactInformation);
                
            	contactInformationRepository.save(contactInformation);
            	organisation.addContactInformation(contactInformation);
            });
        }
    }
    
    private void addDXAddressToContactInformation(
    		List<DXAddressCreationRequest> dxAddressCreationRequest,
            ContactInformation contactInformation) {
    	
    	 if (dxAddressCreationRequest != null) {
    		 dxAddressCreationRequest.forEach(dxAdd -> {
                 DXAddress dxAddress = new DXAddress(dxAdd.getDxNumber(), dxAdd.getDxExchange(), contactInformation);
                 dxAddressRepository.save(dxAddress);
                 contactInformation.addDXAddress(dxAddress);	
             });
         }
    }
}

