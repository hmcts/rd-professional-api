package uk.gov.hmcts.reform.professionalapi.service;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.professionalapi.controller.response.NewUserResponse;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.PrdEnum;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;
import uk.gov.hmcts.reform.professionalapi.domain.UserProfileUpdatedData;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


public interface ProfessionalUserService {

    NewUserResponse addNewUserToAnOrganisation(ProfessionalUser newUser, List<String> roles, List<PrdEnum> prdEnums);

    ProfessionalUser findProfessionalUserById(UUID userIdentifier);

    ProfessionalUser findProfessionalUserByUserIdentifier(String userIdentifier);

    ResponseEntity<Object> findProfessionalUsersByOrganisation(Organisation existingOrganisation, String userIdentifier,
                                                             String showDeleted, boolean rolesRequired, String status);

    ResponseEntity<Object> findProfessionalUsersByOrganisationWithPageable(Organisation existingOrganisation,
                                                                           String showDeleted, boolean rolesRequired,
                                                                           String status, Pageable pageable);

    ProfessionalUser persistUser(ProfessionalUser professionalUser);

    ResponseEntity<Object> modifyRolesForUser(UserProfileUpdatedData userProfileUpdatedData, String userId,
                                              Optional<String> origin);

    ResponseEntity<NewUserResponse> findUserStatusByEmailAddress(String email);

    void checkUserStatusIsActiveByUserId(String userId);

    ProfessionalUser findProfessionalUserByEmailAddress(String email);

    void modifyUserConfiguredAccess(UserProfileUpdatedData userProfileUpdatedData, String userId);

}

