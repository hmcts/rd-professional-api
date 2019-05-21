package uk.gov.hmcts.reform.professionalapi.service;

import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;

import java.util.List;
import java.util.UUID;

public interface PaymentAccountService {

    public Organisation findPaymentAccountsByEmail(String email);

}
