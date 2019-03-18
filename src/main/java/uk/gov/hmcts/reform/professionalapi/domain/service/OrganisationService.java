package uk.gov.hmcts.reform.professionalapi.domain.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.professionalapi.domain.entities.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.entities.OrganisationStatus;
import uk.gov.hmcts.reform.professionalapi.domain.entities.ProfessionalUser;
import uk.gov.hmcts.reform.professionalapi.domain.entities.ProfessionalUserStatus;
import uk.gov.hmcts.reform.professionalapi.domain.service.persistence.OrganisationRepository;
import uk.gov.hmcts.reform.professionalapi.domain.service.persistence.ProfessionalUserRepository;
import uk.gov.hmcts.reform.professionalapi.infrastructure.controllers.request.OrganisationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.infrastructure.controllers.response.OrganisationResponse;

@Service
public class OrganisationService {

    private final OrganisationRepository organisationRepository;
    private final ProfessionalUserRepository professionalUserRepository;

    public OrganisationService(
            OrganisationRepository organisationRepository,
            ProfessionalUserRepository professionalUserRepository) {

        this.organisationRepository = organisationRepository;
        this.professionalUserRepository = professionalUserRepository;
    }

    @Transactional
    public OrganisationResponse create(
            OrganisationCreationRequest organisationCreationRequest) {

        Organisation organisation = organisationRepository.save(new Organisation(
                organisationCreationRequest.getName(),
                OrganisationStatus.PENDING.name()
        ));

        ProfessionalUser superUser = professionalUserRepository.save(new ProfessionalUser(
                organisationCreationRequest.getSuperUser().getFirstName(),
                organisationCreationRequest.getSuperUser().getLastName(),
                organisationCreationRequest.getSuperUser().getEmail(),
                ProfessionalUserStatus.PENDING.name(),
                organisation));

        organisation.addProfessionalUser(superUser);

        organisationRepository.save(organisation);

        return new OrganisationResponse(organisation);
    }
}
