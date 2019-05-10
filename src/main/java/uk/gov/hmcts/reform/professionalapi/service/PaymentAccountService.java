package uk.gov.hmcts.reform.professionalapi.service;

import javax.xml.ws.http.HTTPException;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;
import uk.gov.hmcts.reform.professionalapi.persistence.*;

@Service
@Slf4j
@AllArgsConstructor
public class PaymentAccountService {

    private OrganisationRepository organisationRepository;
    private ProfessionalUserRepository professionalUserRepository;

    public Organisation findPaymentAccountsByEmail(String email) {

        ProfessionalUser user = professionalUserRepository.findByEmailAddress(email);
        if (user == null) {
            throw new HTTPException(404);
        } else  {
            Organisation organisation = organisationRepository.findByUsers(user);
            if (organisation == null) {
                throw new HTTPException(404);
            }
            return organisation;
        }
    }

}
