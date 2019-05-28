package uk.gov.hmcts.reform.professionalapi.service.impl;

import java.util.List;
import java.util.UUID;
import javax.transaction.Transactional;
import javax.xml.ws.http.HTTPException;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import uk.gov.hmcts.reform.professionalapi.controller.request.NewUserCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.response.NewUserResponse;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUserStatus;
import uk.gov.hmcts.reform.professionalapi.persistence.OrganisationRepository;
import uk.gov.hmcts.reform.professionalapi.persistence.PrdEnumRepository;
import uk.gov.hmcts.reform.professionalapi.persistence.ProfessionalUserRepository;
import uk.gov.hmcts.reform.professionalapi.persistence.UserAttributeRepository;
import uk.gov.hmcts.reform.professionalapi.service.ProfessionalUserService;

@Service
@Slf4j
public class ProfessionalUserServiceImpl implements ProfessionalUserService {

    OrganisationRepository organisationRepository;
    ProfessionalUserRepository professionalUserRepository;
    UserAttributeRepository userAttributeRepository;
    PrdEnumRepository prdEnumRepository;

    UserAttributeServiceImpl userAttributeService;

    @Autowired
    public ProfessionalUserServiceImpl(
            OrganisationRepository organisationRepository,
            ProfessionalUserRepository professionalUserRepository,
            UserAttributeRepository userAttributeRepository,
            PrdEnumRepository prdEnumRepository,
            UserAttributeServiceImpl userAttributeService) {

        this.organisationRepository = organisationRepository;
        this.professionalUserRepository = professionalUserRepository;
        this.userAttributeRepository = userAttributeRepository;
        this.prdEnumRepository = prdEnumRepository;
        this.userAttributeService = userAttributeService;
    }

    @Transactional
    @Override
    public NewUserResponse addNewUserToAnOrganisation(NewUserCreationRequest newUserCreationRequest, UUID organisationIdentifier) {
        Organisation theOrganisation = organisationRepository.findByOrganisationIdentifier(organisationIdentifier);

        ProfessionalUser newUser = new ProfessionalUser(
                newUserCreationRequest.getFirstName(),
                newUserCreationRequest.getLastName(),
                newUserCreationRequest.getEmail(),
                ProfessionalUserStatus.PENDING,
                theOrganisation);

        ProfessionalUser persistedNewUser = professionalUserRepository.save(newUser);

        userAttributeService.addUserAttributesToUser(persistedNewUser, newUserCreationRequest.getRoles());

        theOrganisation.addProfessionalUser(persistedNewUser);

        return new NewUserResponse(persistedNewUser);
    }

    /**
     * Searches for a user with the given email address.
     *
     * @param email The email address to search for
     * @return The user with the matching email address
     * @throws HTTPException with the status set to 404 if the email address was not
     *                       found
     */
    public ProfessionalUser findProfessionalUserByEmailAddress(String email) {
        ProfessionalUser user = professionalUserRepository.findByEmailAddress(email);
        /*if (user == null) {
            throw new HTTPException(404);
        }*/
        return user;
    }

    @Override
    public List<ProfessionalUser> findProfessionalUsersByOrganisation(Organisation organisation, boolean showDeleted) {
        log.info("Into  ProfessionalService for get all users for organisation");
        List<ProfessionalUser> professionalUsers = null;
        if (showDeleted) {
            log.info("Getting all users having any status");
            professionalUsers = professionalUserRepository.findByOrganisation(organisation);
        } else {
            log.info("Excluding DELETED users for search");
            professionalUsers = professionalUserRepository.findByOrganisationAndStatusNot(organisation, ProfessionalUserStatus.DELETED);
        }
        return professionalUsers;
    }
}
