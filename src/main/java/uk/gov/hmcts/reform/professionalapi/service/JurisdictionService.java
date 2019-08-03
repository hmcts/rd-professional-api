package uk.gov.hmcts.reform.professionalapi.service;

import java.util.List;
import java.util.Map;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;

public interface JurisdictionService {

    void propagateJurisdictionIdsForSuperUserToCcd(ProfessionalUser user, String userId);

    void propagateJurisdictionIdsForNewUserToCcd(List<Map<String, String>> jurisdictions, String userId, String email);
}
