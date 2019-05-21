package uk.gov.hmcts.reform.professionalapi.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.service.ProfessionalUserService;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUserStatus;
import uk.gov.hmcts.reform.professionalapi.persistence.ProfessionalUserRepository;

import javax.xml.ws.http.HTTPException;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class ProfessionalUserServiceImpl implements ProfessionalUserService {

    private final ProfessionalUserRepository professionalUserRepository;

    public ProfessionalUserServiceImpl(ProfessionalUserRepository professionalUserRepository) {
        this.professionalUserRepository = professionalUserRepository;
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
        if (user == null) {
            throw new HTTPException(404);
        }
        return user;
    }

    @Override
    public List<ProfessionalUser> findProfessionalUsersByOrganisation(Organisation organisation, boolean showDeleted){
        log.info("Into  ProfessionalService for get all users for organisation");
        List<ProfessionalUser> professionalUsers = null;
        if(showDeleted){
            log.info("Getting all users having any status");
            return professionalUserRepository.findByOrganisation(organisation);
        }else {
            log.info("Excluding DELETED users for search");
            professionalUsers = professionalUserRepository.findByOrganisationAndStatusNot(organisation, ProfessionalUserStatus.DELETED);
            if(CollectionUtils.isEmpty(professionalUsers)){
                log.error("No professional users with other than DELETED status present for Organisation: " + organisation.getOrganisationIdentifier());
                throw new EmptyResultDataAccessException(1);
            }
        }
        return professionalUsers;
    }
}
