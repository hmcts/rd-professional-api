package uk.gov.hmcts.reform.professionalapi.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.professionalapi.controller.request.NewUserCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.response.OrganisationResponse;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;
import uk.gov.hmcts.reform.professionalapi.persistence.OrganisationRepository;
import uk.gov.hmcts.reform.professionalapi.persistence.ProfessionalUserRepository;
import uk.gov.hmcts.reform.professionalapi.service.ProfessionalUserServiceI;

@Service
@Slf4j
public class ProfessionalUserServiceImpl implements ProfessionalUserServiceI {

    OrganisationRepository organisationRepository;
    ProfessionalUserRepository professionalUserRepository;

    @Autowired
    public ProfessionalUserServiceImpl(
            OrganisationRepository organisationRepository,
            ProfessionalUserRepository professionalUserRepository) {

        this.organisationRepository = organisationRepository;
        this.professionalUserRepository = professionalUserRepository;
    }

    @Override
    public OrganisationResponse addNewUserToAnOrganisation(NewUserCreationRequest newUserCreationRequest, UUID organisationIdentifier) {

        Organisation theOrganisation = organisationRepository.findByOrganisationIdentifier(organisationIdentifier);

        ProfessionalUser newUser = new ProfessionalUser(
                newUserCreationRequest.getFirstName(),
                newUserCreationRequest.getLastName(),
                newUserCreationRequest.getEmail(),
                newUserCreationRequest.getStatus(),
                theOrganisation);

        ProfessionalUser persistedNewUser = professionalUserRepository.save(newUser);

        addRolesToUser(newUserCreationRequest.getRoles());

        theOrganisation.addProfessionalUser(persistedNewUser);

        return null;
    }

    public List<String> addRolesToUser(List<String> roles) {




        return null;
    }



}
