package uk.gov.hmcts.reform.professionalapi.service;

import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.professionalapi.controller.request.MfaUpdateRequest;
import uk.gov.hmcts.reform.professionalapi.controller.response.MfaStatusResponse;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;

public interface MfaStatusService {

    ResponseEntity<MfaStatusResponse> findMfaStatusByUserId(String id);

    ResponseEntity<Object> updateOrgMfaStatus(MfaUpdateRequest mfaUpdateRequest, Organisation organisation);

}
