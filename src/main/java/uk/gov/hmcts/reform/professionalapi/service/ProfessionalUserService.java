package uk.gov.hmcts.reform.professionalapi.service;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.professionalapi.controller.response.NewUserResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.UsersInOrganisationsByOrganisationIdentifiersResponse;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.PrdEnum;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;
import uk.gov.hmcts.reform.professionalapi.domain.UserAccessType;
import uk.gov.hmcts.reform.professionalapi.domain.UserProfileUpdatedData;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;


public interface ProfessionalUserService {

    NewUserResponse addNewUserToAnOrganisation(ProfessionalUser newUser, List<String> roles, List<PrdEnum> prdEnums);

    ProfessionalUser findProfessionalUserById(UUID userIdentifier);

    ProfessionalUser findProfessionalUserByUserIdentifier(UUID userIdentifier);

    ResponseEntity<Object> findProfessionalUsersByOrganisation(Organisation existingOrganisation, String userIdentifier,
                                                               String showDeleted, boolean rolesRequired,
                                                               String status);

    ResponseEntity<Object> findProfessionalUsersByOrganisationWithPageable(Organisation existingOrganisation,
                                                                           String showDeleted, boolean rolesRequired,
                                                                           String status, Pageable pageable);

    ProfessionalUser persistUser(ProfessionalUser professionalUser);

    ResponseEntity<Object> modifyRolesForUser(UserProfileUpdatedData userProfileUpdatedData, UUID userId,
                                              Optional<String> origin);

    void saveAllUserAccessTypes(ProfessionalUser professionalUser, Set<UserAccessType> userAccessTypes);

    ResponseEntity<NewUserResponse> findUserStatusByEmailAddress(String email);

    void checkUserStatusIsActiveByUserId(UUID userId);

    ProfessionalUser findProfessionalUserByEmailAddress(String email);

    ResponseEntity<Object> modifyUserConfiguredAccessAndRoles(UserProfileUpdatedData userProfileUpdatedData,
                                                              UUID userId, Optional<String> origin);

    ResponseEntity<Object> fetchUsersForRefresh(String since, UUID userId, Integer pageSize, UUID searchAfter);

    UsersInOrganisationsByOrganisationIdentifiersResponse retrieveUsersByOrganisationIdentifiersWithPageable(
            List<String> organisationIdentifiers, Integer pageSize, UUID searchAfterUser,
            UUID searchAfterOrganisation);
}

