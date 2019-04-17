package uk.gov.hmcts.reform.professionalapi.domain.service;

import javax.xml.ws.http.HTTPException;

import org.springframework.stereotype.Service;

import uk.gov.hmcts.reform.professionalapi.domain.entities.ProfessionalUser;
import uk.gov.hmcts.reform.professionalapi.domain.service.persistence.ProfessionalUserRepository;

@Service
public class ProfessionalUserService {

    private final ProfessionalUserRepository professionalUserRepository;

    public ProfessionalUserService(ProfessionalUserRepository professionalUserRepository) {
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
}
