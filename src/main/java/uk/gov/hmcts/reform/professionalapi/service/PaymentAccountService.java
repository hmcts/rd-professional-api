package uk.gov.hmcts.reform.professionalapi.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
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
            throw new EmptyResultDataAccessException(404);
        }
        Organisation organisation = organisationRepository.findByUsers(user);
        return organisation;
    }

}
