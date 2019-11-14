package uk.gov.hmcts.reform.professionalapi.service;

import java.util.List;

import uk.gov.hmcts.reform.professionalapi.domain.PaymentAccount;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;
import uk.gov.hmcts.reform.professionalapi.domain.UserAccountMapId;

public interface UserAccountMapService {

    void deleteByUserAccountMapIdIn(List<UserAccountMapId> accountsToDelete);

    void persistedUserAccountMap(ProfessionalUser persistedSuperUser, List<PaymentAccount> paymentAccounts);


}