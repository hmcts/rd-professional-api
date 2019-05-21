package uk.gov.hmcts.reform.professionalapi.service;

import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;

import java.util.List;

public interface UserAttributeService {
    void addUserAttributesToUser(ProfessionalUser newUser, List<String> userRoles);
}
