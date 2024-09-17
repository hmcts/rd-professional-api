package uk.gov.hmcts.reform.professionalapi.service;

import uk.gov.hmcts.reform.professionalapi.domain.PaymentAccount;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;

import java.util.List;

public interface UserAccountMapService {

    void persistedUserAccountMap(ProfessionalUser persistedSuperUser, List<PaymentAccount> paymentAccounts);

    void updateUser(ProfessionalUser existingAdmin, ProfessionalUser newAdmin);
}