package uk.gov.hmcts.reform.professionalapi.service;

import java.util.List;

import uk.gov.hmcts.reform.professionalapi.domain.PrdEnum;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;
import uk.gov.hmcts.reform.professionalapi.domain.UserAttribute;

public interface UserAttributeService {
    void addUserAttributesToUser(ProfessionalUser newUser, List<String> userRoles, List<PrdEnum> prdEnums);

    List<UserAttribute> addUserAttributesToSuperUserWithJurisdictions( ProfessionalUser user, List<UserAttribute> attributes, List<String> jurisdictionIds);
}
