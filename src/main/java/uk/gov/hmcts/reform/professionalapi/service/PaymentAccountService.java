package uk.gov.hmcts.reform.professionalapi.service;

import uk.gov.hmcts.reform.professionalapi.controller.request.PbaRequest;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.PbaResponse;

public interface PaymentAccountService {

    Organisation findPaymentAccountsByEmail(String email);

    PbaResponse editPaymentAccountsByOrganisation(Organisation organisation, PbaRequest pbaEditRequest);

    void deletePaymentsOfOrganisation(PbaRequest deletePbaRequest, Organisation organisation);
}
