package uk.gov.hmcts.reform.professionalapi.controller.request.validator;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.reform.professionalapi.controller.constants.IdamStatus;
import uk.gov.hmcts.reform.professionalapi.controller.request.InvalidRequest;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;
import uk.gov.hmcts.reform.professionalapi.domain.RoleName;
import uk.gov.hmcts.reform.professionalapi.domain.UserProfileUpdatedData;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.ORGANISATION_MISMATCH;

@Component
@Slf4j
public class ProfessionalUserReqValidator {

    public void validateRequest(String orgId, String showDeleted, String status) {
        if (StringUtils.isEmpty(orgId) && StringUtils.isEmpty(showDeleted)) {
            throw new InvalidRequest("No input values given for the request");
        }

        if (StringUtils.isNotEmpty(status)) {
            validateUserStatus(status);
        }
    }

    public void validateUserStatus(String status) {
        try {
            IdamStatus.valueOf(status.toUpperCase());
        } catch (Exception ex) {
            throw new InvalidRequest(String.format("The status provided is invalid ex=%s", ex));
        }
    }

    public void validateStatusIsActive(String status) {
        if (!IdamStatus.ACTIVE.toString().equalsIgnoreCase(status)) {
            throw new InvalidRequest("Required status param value equal to 'Active'");
        }
    }

    public boolean validateUuid(String inputString) {
        if (inputString != null && !inputString.isEmpty()) {
            throw new InvalidRequest(
                    String.format(
                            "The input parameter: \"%s\", should neither be null nor empty",
                            inputString
                    ));
        }
        return true;
    }

    public void validateOrganisationMatch(String organisationIdentifier, ProfessionalUser user) {
        if (user == null) {
            throw new InvalidRequest("Invalid UserIdentifier passed in the request");
        }
        if (user.getOrganisation() == null
                || !organisationIdentifier.trim().equals(user.getOrganisation().getOrganisationIdentifier())) {
            throw new AccessDeniedException(ORGANISATION_MISMATCH);
        }
    }

    public void validateModifyRolesRequest(UserProfileUpdatedData userProfileUpdatedData, String userId) {

        if (null == userProfileUpdatedData || StringUtils.isEmpty(userId)
                || invalidRoleName(userProfileUpdatedData.getRolesAdd())
                || invalidRoleName(userProfileUpdatedData.getRolesDelete())) {

            throw new InvalidRequest("The Request provided is invalid for modify the roles for user");
        }
    }

    private boolean invalidRoleName(Set<RoleName> roleNames) {

        List<RoleName> emptyRoles = new ArrayList<>();
        if (!CollectionUtils.isEmpty(roleNames)) {
            emptyRoles = roleNames.stream().filter(roleName -> StringUtils.isBlank(roleName.getName())).toList();

        }
        return !emptyRoles.isEmpty();
    }
}
