package uk.gov.hmcts.reform.professionalapi.service;

import uk.gov.hmcts.reform.professionalapi.controller.request.PbaEditRequest;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.PbaResponse;

public interface PaymentAccountService {

    Organisation findPaymentAccountsByEmail(String email);

    PbaResponse editPbaAccounts(PbaEditRequest pbaEditRequest, Organisation organisation);
}
