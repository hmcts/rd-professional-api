package uk.gov.hmcts.reform.professionalapi.service.impl;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;
import uk.gov.hmcts.reform.professionalapi.persistence.*;
import uk.gov.hmcts.reform.professionalapi.service.PaymentAccountService;

@Service
@Slf4j
@AllArgsConstructor
public class PaymentAccountServiceImpl implements PaymentAccountService {

    private OrganisationRepository organisationRepository;
    private ProfessionalUserRepository professionalUserRepository;

    public Organisation findPaymentAccountsByEmail(String email) {

        ProfessionalUser user = professionalUserRepository.findByEmailAddress(email);

        if (user != null) {
            throw new EmptyResultDataAccessException(1);
        }
        Organisation organisation = organisationRepository.findByUsers(user);
        return organisation;
    }
}
