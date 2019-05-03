package uk.gov.hmcts.reform.professionalapi.domain.service;

import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import uk.gov.hmcts.reform.professionalapi.domain.entities.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.entities.OrganisationStatus;
import uk.gov.hmcts.reform.professionalapi.domain.entities.PaymentAccount;
import uk.gov.hmcts.reform.professionalapi.domain.entities.ProfessionalUser;
import uk.gov.hmcts.reform.professionalapi.domain.entities.ProfessionalUserStatus;
import uk.gov.hmcts.reform.professionalapi.domain.service.persistence.ContactInformationRepository;
import uk.gov.hmcts.reform.professionalapi.domain.service.persistence.DxAddressRepository;
import uk.gov.hmcts.reform.professionalapi.domain.service.persistence.OrganisationRepository;
import uk.gov.hmcts.reform.professionalapi.domain.service.persistence.PaymentAccountRepository;
import uk.gov.hmcts.reform.professionalapi.domain.service.persistence.ProfessionalUserRepository;
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
                OrganisationStatus.PENDING.name(),
                null,
                null,
                null,
                null
        );

        Organisation organisation = organisationRepository.save(newOrganisation);

        addPbaAccountToOrganisation(organisationCreationRequest.getPbaAccounts(), organisation);

        addSuperUserToOrganisation(organisationCreationRequest.getSuperUser(), organisation);

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
}

