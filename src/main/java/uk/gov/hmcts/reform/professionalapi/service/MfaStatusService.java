package uk.gov.hmcts.reform.professionalapi.service;

import uk.gov.hmcts.reform.professionalapi.controller.response.MfaStatusResponse;

public interface MfaStatusService {

    MfaStatusResponse findMfaStatusByUserId(String id);

}
