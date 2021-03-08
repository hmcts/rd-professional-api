package uk.gov.hmcts.reform.professionalapi.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.professionalapi.controller.advice.ResourceNotFoundException;
import uk.gov.hmcts.reform.professionalapi.controller.request.InvalidRequest;
import uk.gov.hmcts.reform.professionalapi.controller.response.MfaStatusResponse;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;
import uk.gov.hmcts.reform.professionalapi.repository.ProfessionalUserRepository;
import uk.gov.hmcts.reform.professionalapi.service.MfaStatusService;

import static java.util.Objects.isNull;

@Service
@Slf4j
public class MfaStatusServiceImpl implements MfaStatusService {

    @Autowired
    ProfessionalUserRepository professionalUserRepository;

    @Override
    public MfaStatusResponse findMfaStatusByUserId(String id) {

        if (StringUtils.isEmpty(id)) {
            throw new InvalidRequest("User Id cannot be empty");
        }

        ProfessionalUser user = professionalUserRepository.findByUserIdentifier(id);
        if (isNull(user)) {
            throw new ResourceNotFoundException("The requested user does not exist");
        }

        Organisation org = user.getOrganisation();

        if (org.isOrganisationStatusActive()) {
            MfaStatusResponse mfaStatusResponse = new MfaStatusResponse();
            mfaStatusResponse.setMfa(org.getOrganisationMfaStatus().getMfaStatus().toString());
            return mfaStatusResponse;
        } else {
            throw new ResourceNotFoundException("The requested user's organisation is not 'Active'");
        }
    }
}
