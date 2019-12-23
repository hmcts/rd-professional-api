package uk.gov.hmcts.reform.professionalapi.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.professionalapi.controller.response.NewUserResponse;
import uk.gov.hmcts.reform.professionalapi.domain.*;


public interface ProfessionalUserService {

    NewUserResponse addNewUserToAnOrganisation(ProfessionalUser newUser, List<String> roles, List<PrdEnum> prdEnums);

    ProfessionalUser findProfessionalUserProfileByEmailAddress(String email);

    ProfessionalUser findProfessionalUserById(UUID userIdentifier);

    ResponseEntity findProfessionalUsersByOrganisation(Organisation existingOrganisation, String showDeleted, boolean rolesRequired, String status);

    ResponseEntity findProfessionalUsersByOrganisationWithPageable(Organisation existingOrganisation, String showDeleted, boolean rolesRequired, String status, Pageable pageable);

    ProfessionalUser persistUser(ProfessionalUser professionalUser);

    ModifyUserRolesResponse modifyRolesForUser(UserProfileUpdatedData userProfileUpdatedData, String userId, Optional<String> origin);

    ResponseEntity<NewUserResponse> findUserStatusByEmailAddress(String email);
}

