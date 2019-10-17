package uk.gov.hmcts.reform.professionalapi.controller.request;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import uk.gov.hmcts.reform.professionalapi.controller.response.IdamStatus;
import uk.gov.hmcts.reform.professionalapi.domain.ModifyUserProfileData;
import uk.gov.hmcts.reform.professionalapi.domain.RoleName;

@Component
@Slf4j
public class ProfessionalUserReqValidator {

    public static boolean isValidEmail(String email) {
        if (!StringUtils.isEmpty(email)) {
            Pattern p = Pattern.compile("(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])");
            Matcher m = p.matcher(email);
            return m.find();
        }

        return false;
    }

    public void validateRequest(String orgId, String showDeleted, String status) {
        if (StringUtils.isEmpty(orgId) && StringUtils.isEmpty(showDeleted)) {
            throw new InvalidRequest("No input values given for the request");
        }

        if (!StringUtils.isEmpty(status)) {
            validateUserStatus(status);
        }
    }

    public static void validateUserStatus(String status) {
        boolean valid = false;

        for (IdamStatus idamStatus : IdamStatus.values()) {
            if (status.equalsIgnoreCase(idamStatus.toString())) {
                valid = true;
            }
        }

        if (!valid) {
            throw new InvalidRequest("The status provided is invalid");
        }
    }

    public void validateStatusIsActive(String status) {
        if (!IdamStatus.ACTIVE.toString().equalsIgnoreCase(status)) {
            throw new InvalidRequest("Required status param value equal to 'Active'");
        }
    }

    public void validateModifyRolesRequest(ModifyUserProfileData modifyUserProfileData, String userId) {

        if (null == modifyUserProfileData || StringUtils.isEmpty(userId)
                || invalidRoleName(modifyUserProfileData.getRolesAdd())
                || invalidRoleName(modifyUserProfileData.getRolesDelete())) {

            throw new InvalidRequest("The Request provided is invalid for modify the roles for user");
        }
    }

    public boolean invalidRoleName(Set<RoleName> roleNames) {

        List<RoleName> emptyRoles = new ArrayList<>();
        if (!CollectionUtils.isEmpty(roleNames)) {
            emptyRoles = roleNames.stream().filter(roleName -> StringUtils.isBlank(roleName.getName())).collect(Collectors.toList());

        }
        return emptyRoles.size() > 0 ? true : false;
    }


}
