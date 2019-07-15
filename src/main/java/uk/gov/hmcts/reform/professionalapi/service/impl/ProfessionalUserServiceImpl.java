package uk.gov.hmcts.reform.professionalapi.service.impl;

import java.util.List;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;

import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.reform.professionalapi.controller.request.NewUserCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.response.NewUserResponse;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;
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
    public NewUserResponse addNewUserToAnOrganisation(NewUserCreationRequest newUserCreationRequest, String organisationIdentifier) {
        Organisation theOrganisation = organisationRepository.findByOrganisationIdentifier(organisationIdentifier);

        ProfessionalUser newUser = new ProfessionalUser(
                newUserCreationRequest.getFirstName(),
                newUserCreationRequest.getLastName(),
                newUserCreationRequest.getEmail(),
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
     */
    public ProfessionalUser findProfessionalUserByEmailAddress(String email) {
        return professionalUserRepository.findByEmailAddress(email);
    }

    @Override
    public List<ProfessionalUser> findProfessionalUsersByOrganisation(Organisation organisation, boolean showDeleted) {
        log.info("Into  ProfessionalService for get all users for organisation");
        List<ProfessionalUser> professionalUsers;

        if (showDeleted) {
            log.info("Getting all users regardless of deleted status");
            professionalUsers = professionalUserRepository.findByOrganisation(organisation);
        } else {
            log.info("Excluding DELETED users for search");
            List<ProfessionalUser> listOfUsers = professionalUserRepository.findByOrganisation(organisation);
            professionalUsers = listOfUsers.stream().filter(users -> users.getDeleted() == null).collect(Collectors.toList());
        }

        if (CollectionUtils.isEmpty(professionalUsers)) {
            throw new EmptyResultDataAccessException(1);
        }
        return professionalUsers;
    }

}
