package uk.gov.hmcts.reform.professionalapi.domain.service;

import javax.xml.ws.http.HTTPException;

import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;

import uk.gov.hmcts.reform.professionalapi.domain.entities.ProfessionalUser;
import uk.gov.hmcts.reform.professionalapi.domain.service.persistence.ProfessionalUserRepository;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Tabby Cromarty
 */
@Service
@Slf4j
public class ProfessionalUserService {

    private final ProfessionalUserRepository professionalUserRepository;

    public ProfessionalUserService(ProfessionalUserRepository professionalUserRepository) {
        this.professionalUserRepository = professionalUserRepository;
    }

    public ProfessionalUser findProfessionalUserByEmailAddress(String email) {
        ProfessionalUser user = new ProfessionalUser();
        user.setEmailAddress(email);
        return professionalUserRepository.findOne(Example.of(user)).orElseThrow(() -> {
            return new HTTPException(404);
        });
    }
}
