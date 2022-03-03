package uk.gov.hmcts.reform.professionalapi.service.impl;

import lombok.extern.slf4j.Slf4j;
import java.util.Objects;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.professionalapi.controller.advice.ResourceNotFoundException;
import uk.gov.hmcts.reform.professionalapi.controller.request.InvalidRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.MfaUpdateRequest;
import uk.gov.hmcts.reform.professionalapi.controller.response.MfaStatusResponse;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;
import uk.gov.hmcts.reform.professionalapi.repository.OrganisationRepository;
import uk.gov.hmcts.reform.professionalapi.repository.ProfessionalUserRepository;
import uk.gov.hmcts.reform.professionalapi.service.MfaStatusService;

import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.EMPTY_USER_ID;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.NO_USER_FOUND;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.ORG_NOT_ACTIVE;

@Service
@Slf4j
public class MfaStatusServiceImpl implements MfaStatusService {

    @Autowired
    ProfessionalUserRepository professionalUserRepository;
    @Autowired
    OrganisationRepository organisationRepository;

    @Override
    public ResponseEntity<MfaStatusResponse> findMfaStatusByUserId(String id) {

        if (StringUtils.isEmpty(id)) {
            throw new InvalidRequest(EMPTY_USER_ID);
        }

        ProfessionalUser user = professionalUserRepository.findByUserIdentifier(id);
        if (Objects.isNull(user)) {
            throw new ResourceNotFoundException(NO_USER_FOUND);
        }

        var org = user.getOrganisation();
        var mfaStatusResponse = new MfaStatusResponse();

        if (org.isOrganisationStatusActive()) {
            mfaStatusResponse.setMfa(org.getOrganisationMfaStatus().getMfaStatus().toString());
        } else {
            throw new InvalidRequest(ORG_NOT_ACTIVE);
        }

        return ResponseEntity
                .status(200)
                .body(mfaStatusResponse);
    }

    @Override
    public ResponseEntity<Object> updateOrgMfaStatus(MfaUpdateRequest mfaUpdateRequest, Organisation organisation) {

        var newStatus = mfaUpdateRequest.getMfa();
        organisation.getOrganisationMfaStatus().setMfaStatus(newStatus);
        organisationRepository.save(organisation);

        return ResponseEntity
                .status(200)
                .build();
    }
}
