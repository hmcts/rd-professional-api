package uk.gov.hmcts.reform.professionalapi.service;

import java.util.List;
import uk.gov.hmcts.reform.professionalapi.controller.request.PbaEditRequest;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.PbaResponse;

public interface PaymentAccountService {

    Organisation findPaymentAccountsByEmail(String email);

    void deleteUserAccountMaps(Organisation organisation);

    void deletePaymentAccountsFromOrganisation(Organisation organisation);

    void addPaymentAccountsToOrganisation(PbaEditRequest pbaEditRequest, Organisation organisation);

    PbaResponse addUserAndPaymentAccountsToUserAccountMap(Organisation organisation);

    void validatePaymentAccounts(List<String> paymentAccounts);
}
