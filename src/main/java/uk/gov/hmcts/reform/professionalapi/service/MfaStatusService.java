package uk.gov.hmcts.reform.professionalapi.service;

import uk.gov.hmcts.reform.professionalapi.controller.request.MfaUpdateRequest;
import uk.gov.hmcts.reform.professionalapi.controller.response.MfaStatusResponse;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;

public interface MfaStatusService {

    MfaStatusResponse findMfaStatusByUserId(String id);

    void updateOrgMfaStatus(MfaUpdateRequest mfaUpdateRequest, Organisation organisation);

}
