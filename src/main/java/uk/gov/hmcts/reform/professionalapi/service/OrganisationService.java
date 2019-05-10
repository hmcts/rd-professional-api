package uk.gov.hmcts.reform.professionalapi.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.professionalapi.controller.request.*;
import uk.gov.hmcts.reform.professionalapi.controller.response.OrganisationResponse;
import uk.gov.hmcts.reform.professionalapi.domain.*;
import uk.gov.hmcts.reform.professionalapi.exception.ResourceNotFoundException;
import uk.gov.hmcts.reform.professionalapi.persistence.*;

import java.util.List;
import java.util.UUID;


@Service
@Slf4j
public class OrganisationService {

    private final OrganisationRepository organisationRepository;
    private final ProfessionalUserRepository professionalUserRepository;
    private final PaymentAccountRepository paymentAccountRepository;
    private final DxAddressRepository dxAddressRepository;
    private final ContactInformationRepository contactInformationRepository;

    public OrganisationService(
            OrganisationRepository organisationRepository,
            ProfessionalUserRepository professionalUserRepository,
            PaymentAccountRepository paymentAccountRepository,
            DxAddressRepository dxAddressRepository,
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
                OrganisationStatus.PENDING,
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

                addDxAddressToContactInformation(contactInfo.getDxAddress(), contactInformation);

                contactInformationRepository.save(contactInformation);
                organisation.addContactInformation(contactInformation);
            });
        }
    }

    private void addDxAddressToContactInformation(List<DxAddressCreationRequest> dxAddressCreationRequest, ContactInformation contactInformation) {
        if (dxAddressCreationRequest != null) {
            dxAddressCreationRequest.forEach(dxAdd -> {
                if (dxAdd.getIsDxRequestValid()) {
                    DxAddress dxAddress = new DxAddress(dxAdd.getDxNumber(), dxAdd.getDxExchange(), contactInformation);
                    dxAddressRepository.save(dxAddress);
                    contactInformation.addDxAddress(dxAddress);
                }
            });
        }
    }

    @Transactional
    public OrganisationResponse updateOrganisation(
            OrganisationCreationRequest organisationCreationRequest, UUID organisationIdentifier) {

        Organisation organisation = organisationRepository.findByOrganisationIdentifier(organisationIdentifier);

        if(organisation == null){
            String errorMessage ="Organisation not found with organisationIdentifier: "+ organisationIdentifier;
            log.error(errorMessage);
            throw new ResourceNotFoundException(errorMessage);
        }
        else{
            log.info("Into update Organisation service");
            organisation.setName(organisationCreationRequest.getName());
            organisation.setStatus(organisationCreationRequest.getStatus());
            organisation.setSraId(organisationCreationRequest.getSraId());
            organisation.setCompanyNumber(organisationCreationRequest.getCompanyNumber());
            organisation.setSraRegulated(organisationCreationRequest.getSraRegulated());
            organisation.setCompanyUrl(organisationCreationRequest.getCompanyUrl());
            organisationRepository.save(organisation);
            log.info("Update Organisation service done...");
        }
        return new OrganisationResponse(organisation);
    }
}

