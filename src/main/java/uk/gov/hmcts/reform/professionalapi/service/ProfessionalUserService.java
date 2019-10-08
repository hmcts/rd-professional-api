package uk.gov.hmcts.reform.professionalapi.service;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.professionalapi.controller.response.NewUserResponse;
import uk.gov.hmcts.reform.professionalapi.domain.ModifyUserProfileData;
import uk.gov.hmcts.reform.professionalapi.domain.ModifyUserRolesResponse;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.PrdEnum;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;


public interface ProfessionalUserService {
    NewUserResponse addNewUserToAnOrganisation(ProfessionalUser newUser, List<String> roles, List<PrdEnum> prdEnums);

    ProfessionalUser findProfessionalUserProfileByEmailAddress(String email);

    ProfessionalUser findProfessionalUserById(UUID userIdentifier);

    ResponseEntity findProfessionalUsersByOrganisation(Organisation existingOrganisation, String showDeleted, boolean rolesRequired, String status);

    ResponseEntity findProfessionalUsersByOrganisationWithPageable(Organisation existingOrganisation, String showDeleted, boolean rolesRequired, String status, Pageable pageable);

    ProfessionalUser persistUser(ProfessionalUser professionalUser);

    ModifyUserRolesResponse modifyRolesForUser(ModifyUserProfileData modifyUserProfileData, String userId);
}

