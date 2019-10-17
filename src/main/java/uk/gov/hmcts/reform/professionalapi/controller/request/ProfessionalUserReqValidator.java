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
        if (email != null) {
            Pattern p = Pattern.compile("\\\\A(?=[a-zA-Z0-9@.!#$%&'*+/=?^_`{|}~-]{6,254}\\\\z)(?=[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]\" + \"{1,64}@)[a-zA-Z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\\\.[a-zA-Z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:(?=[a-zA-Z0-9-]{1,63}\" + \"\\\\.)[a-zA-Z0-9](?:[a-zA-Z0-9-]*[a-zA-Z0-9])?\\\\.)+(?=[a-zA-Z0-9-]{1,63}\\\\z)[a-zA-Z0-9](?:[a-zA-Z0-9-]*[a-zA-Z0-9])?\\\\z\" + \"'?[- a-zA-Z]+$\";\n");
            Matcher m = p.matcher(email);
            return m.find();
        }
        return false;
    }

    public void validateRequest(String orgId, String showDeleted, String email, String status) {
        if (null == orgId  && null == email && null == showDeleted) {
            throw new InvalidRequest("No input values given for the request");
        }

        if (!StringUtils.isEmpty(status)) {
            validateUserStatus(status);
        }

        isValidEmail(email);
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
        return emptyRoles.isEmpty();
    }


}
