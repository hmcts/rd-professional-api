package uk.gov.hmcts.reform.professionalapi.service.impl;

import java.util.List;
import java.util.UUID;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;

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
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUserStatus;
import uk.gov.hmcts.reform.professionalapi.domain.UserAccountMap;
import uk.gov.hmcts.reform.professionalapi.domain.UserAccountMapId;
import uk.gov.hmcts.reform.professionalapi.persistence.ContactInformationRepository;
import uk.gov.hmcts.reform.professionalapi.persistence.DxAddressRepository;
import uk.gov.hmcts.reform.professionalapi.persistence.OrganisationRepository;
import uk.gov.hmcts.reform.professionalapi.persistence.PaymentAccountRepository;
import uk.gov.hmcts.reform.professionalapi.persistence.ProfessionalUserRepository;
import uk.gov.hmcts.reform.professionalapi.persistence.UserAccountMapRepository;
import uk.gov.hmcts.reform.professionalapi.service.OrganisationService;

@Service
@Slf4j
public class OrganisationServiceImpl implements OrganisationService {

    OrganisationRepository organisationRepository;
    ProfessionalUserRepository professionalUserRepository;
    PaymentAccountRepository paymentAccountRepository;
    DxAddressRepository dxAddressRepository;
    ContactInformationRepository contactInformationRepository;
    UserAccountMapRepository userAccountMapRepository;

    @Autowired
    public OrganisationServiceImpl(
            OrganisationRepository organisationRepository,
            ProfessionalUserRepository professionalUserRepository,
            PaymentAccountRepository paymentAccountRepository,
            DxAddressRepository dxAddressRepository,
            ContactInformationRepository contactInformationRepository,
            UserAccountMapRepository userAccountMapRepository) {

        this.organisationRepository = organisationRepository;
        this.professionalUserRepository = professionalUserRepository;
        this.paymentAccountRepository = paymentAccountRepository;
        this.contactInformationRepository = contactInformationRepository;
        this.dxAddressRepository = dxAddressRepository;
        this.userAccountMapRepository = userAccountMapRepository;
    }

    @Override
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

        persistedUserAccountMap(persistedSuperUser,organisation.getPaymentAccounts());

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
                DxAddress dxAddress = new DxAddress(dxAdd.getDxNumber(), dxAdd.getDxExchange(), contactInformation);
                dxAddress = dxAddressRepository.save(dxAddress);
                contactInformation.addDxAddress(dxAddress);
            });
        }
    }

    private void persistedUserAccountMap(ProfessionalUser persistedSuperUser, List<PaymentAccount> paymentAccounts) {

        if (paymentAccounts != null
                &&  paymentAccounts.size() > 0) {
            log.debug("PaymentAccount is not empty");
            paymentAccounts.forEach(paymentAccount -> {

                userAccountMapRepository.save(new UserAccountMap(new UserAccountMapId(persistedSuperUser, paymentAccount)));
            });
        }
    }

    @Override
    public OrganisationsDetailResponse retrieveOrganisations() {
        List<Organisation> organisations = organisationRepository.findAll();
        log.debug("Retrieving all organisations...");
        return new OrganisationsDetailResponse(organisations, true);
    }

    @Override
    public OrganisationResponse updateOrganisation(
            OrganisationCreationRequest organisationCreationRequest, UUID organisationIdentifier) {

        Organisation organisation = organisationRepository.findByOrganisationIdentifier(organisationIdentifier);

        log.info("Into update Organisation service");
        organisation.setName(organisationCreationRequest.getName());
        organisation.setStatus(organisationCreationRequest.getStatus());
        organisation.setSraId(organisationCreationRequest.getSraId());
        organisation.setCompanyNumber(organisationCreationRequest.getCompanyNumber());
        organisation.setSraRegulated(organisationCreationRequest.getSraRegulated());
        organisation.setCompanyUrl(organisationCreationRequest.getCompanyUrl());
        organisationRepository.save(organisation);
        log.info("Update Organisation service done...");

        return new OrganisationResponse(organisation);
    }

    @Override
    public Organisation getOrganisationByOrganisationIdentifier(UUID organisationIdentifier) {
        return organisationRepository.findByOrganisationIdentifier(organisationIdentifier);
    }

    @Override
    public OrganisationEntityResponse retrieveOrganisation(UUID id) {
        Organisation organisation = organisationRepository.findByOrganisationIdentifier(id);
        if (organisation == null) {
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND);
        } else {
            log.debug("Retrieving organisation with ID " + id.toString());
            return new OrganisationEntityResponse(organisation, true);
        }
    }

    @Override
    public OrganisationsDetailResponse findByOrganisationStatus(OrganisationStatus status) {

        return new OrganisationsDetailResponse(organisationRepository.findByStatus(status), true);
    }
}

