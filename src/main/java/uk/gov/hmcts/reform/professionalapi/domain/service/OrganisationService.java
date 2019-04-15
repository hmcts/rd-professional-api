package uk.gov.hmcts.reform.professionalapi.domain.service;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.professionalapi.domain.entities.*;
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

    public OrganisationService(
            OrganisationRepository organisationRepository,
            ProfessionalUserRepository professionalUserRepository,
            PaymentAccountRepository paymentAccountRepository) {

        this.organisationRepository = organisationRepository;
        this.professionalUserRepository = professionalUserRepository;
        this.paymentAccountRepository = paymentAccountRepository;
    }

    @Transactional
    public OrganisationResponse createOrganisationFrom(
            OrganisationCreationRequest organisationCreationRequest) {

        Organisation newOrganisation = new Organisation(
                organisationCreationRequest.getName(),
                OrganisationStatus.PENDING.name()
        );

        Organisation organisation = organisationRepository.save(newOrganisation);

        addSuperUserToOrganisation(organisationCreationRequest.getSuperUser(), organisation);

        addPbaAccountToOrganisation(organisationCreationRequest.getPbaAccounts(), organisation);

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
                paymentAccountRepository.save(paymentAccount);
                organisation.addPaymentAccount(paymentAccount);
            });
        }
    }

    private void addSuperUserToOrganisation(
            UserCreationRequest userCreationRequest,
            Organisation organisation) {

        ProfessionalUser superUser = professionalUserRepository.save(new ProfessionalUser(
                userCreationRequest.getFirstName(),
                userCreationRequest.getLastName(),
                userCreationRequest.getEmail(),
                ProfessionalUserStatus.PENDING.name(),
                organisation));

        organisation.addProfessionalUser(superUser);
    }
}
