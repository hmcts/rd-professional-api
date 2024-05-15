package uk.gov.hmcts.reform.professionalapi.service;

import uk.gov.hmcts.reform.professionalapi.domain.PrdEnum;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;
import uk.gov.hmcts.reform.professionalapi.domain.UserAttribute;

import java.util.List;

public interface UserAttributeService {
    void addUserAttributesToUser(ProfessionalUser newUser, List<String> userRoles, List<PrdEnum> prdEnums);

    List<UserAttribute> addUserAttributesToSuperUser(ProfessionalUser user,
                                                                      List<UserAttribute> attributes);
}
