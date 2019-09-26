package uk.gov.hmcts.reform.professionalapi.service;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.professionalapi.controller.response.NewUserResponse;
import uk.gov.hmcts.reform.professionalapi.domain.*;
import uk.gov.hmcts.reform.professionalapi.domain.ModifyUserRolesResponse;

public interface ProfessionalUserService {
    NewUserResponse addNewUserToAnOrganisation(ProfessionalUser newUser, List<String> roles, List<PrdEnum> prdEnums);

    ProfessionalUser findProfessionalUserProfileByEmailAddress(String email);

    ProfessionalUser findProfessionalUserById(UUID userIdentifier);

    ResponseEntity findProfessionalUsersByOrganisation(Organisation existingOrganisation, String showDeleted, boolean rolesRequired, String status);

    ProfessionalUser persistUser(ProfessionalUser professionalUser);

    ModifyUserRolesResponse modifyRolesForUser(ModifyUserProfileData modifyUserProfileData, String userId);
}

