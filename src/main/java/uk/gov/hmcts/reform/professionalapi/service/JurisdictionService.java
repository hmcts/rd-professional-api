package uk.gov.hmcts.reform.professionalapi.service;

import java.util.List;

import uk.gov.hmcts.reform.professionalapi.domain.Jurisdiction;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;

public interface JurisdictionService {

    void propagateJurisdictionIdsForSuperUserToCcd(ProfessionalUser user, String userId);

    void propagateJurisdictionIdsForNewUserToCcd(List<Jurisdiction> jurisdictions, String userId, String email);
}
