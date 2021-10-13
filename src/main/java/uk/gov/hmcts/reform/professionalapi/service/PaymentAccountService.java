package uk.gov.hmcts.reform.professionalapi.service;

import uk.gov.hmcts.reform.professionalapi.controller.request.PbaEditRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.PbaUpdateRequest;
import uk.gov.hmcts.reform.professionalapi.controller.response.UpdatePbaStatusResponse;
import uk.gov.hmcts.reform.professionalapi.controller.request.PbaRequest;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.PbaResponse;

import java.util.List;

public interface PaymentAccountService {

    Organisation findPaymentAccountsByEmail(String email);

    PbaResponse editPaymentAccountsByOrganisation(Organisation organisation, PbaRequest pbaEditRequest);

    void deletePaymentsOfOrganisation(PbaRequest deletePbaRequest, Organisation organisation);

    PbaResponse editPaymentAccountsByOrganisation(Organisation organisation, PbaEditRequest pbaEditRequest);

    UpdatePbaStatusResponse updatePaymentAccountsStatusForAnOrganisation(
            List<PbaUpdateRequest> pbaRequestList, String orgId);

}
