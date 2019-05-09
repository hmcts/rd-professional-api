package uk.gov.hmcts.reform.professionalapi.service;

import javax.xml.ws.http.HTTPException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;
import uk.gov.hmcts.reform.professionalapi.persistence.*;

@Service
@Slf4j
public class PaymentAccountService {
    private final OrganisationRepository organisationRepository;
    private final ProfessionalUserRepository professionalUserRepository;

    public PaymentAccountService(
            OrganisationRepository organisationRepository,
            ProfessionalUserRepository professionalUserRepository) {

        this.organisationRepository = organisationRepository;
        this.professionalUserRepository = professionalUserRepository;
    }

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
