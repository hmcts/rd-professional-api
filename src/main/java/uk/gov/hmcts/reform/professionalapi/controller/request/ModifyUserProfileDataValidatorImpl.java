package uk.gov.hmcts.reform.professionalapi.controller.request;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.professionalapi.controller.advice.ErrorConstants;
import uk.gov.hmcts.reform.professionalapi.domain.ModifyUserProfileData;

@Component
public class ModifyUserProfileDataValidatorImpl implements ModifyUserProfileDataValidator {

    @Override
    public ModifyUserProfileData validateRequest(ModifyUserProfileData modifyUserProfileData) {

        ModifyUserProfileData validatedData = new ModifyUserProfileData();

        //if required fields are empty
        if (null == modifyUserProfileData.getRolesAdd() && null == modifyUserProfileData.getRolesDelete() && StringUtils.isEmpty(modifyUserProfileData.getIdamStatus())) {
            throw new InvalidRequest(ErrorConstants.INVALID_REQUEST.getErrorMessage());
        }

        //if all are present
        if (null != modifyUserProfileData.getRolesAdd() && null != modifyUserProfileData.getRolesDelete() && !StringUtils.isEmpty(modifyUserProfileData.getIdamStatus())
                && !StringUtils.isEmpty(modifyUserProfileData.getFirstName()) && !StringUtils.isEmpty(modifyUserProfileData.getLastName()) && !StringUtils.isEmpty(modifyUserProfileData.getEmail())) {

            validatedData.setRolesAdd(modifyUserProfileData.getRolesAdd());
            validatedData.setRolesDelete(modifyUserProfileData.getRolesDelete());
            return validatedData;
        }

        //if both roles and status are present
        if (null != modifyUserProfileData.getRolesAdd() && null != modifyUserProfileData.getRolesDelete() && !StringUtils.isEmpty(modifyUserProfileData.getIdamStatus())) {
            validatedData.setRolesAdd(modifyUserProfileData.getRolesAdd());
            validatedData.setRolesDelete(modifyUserProfileData.getRolesDelete());
            return validatedData;
        }

        //if one role and status are present
        if (null != modifyUserProfileData.getRolesDelete() || null != modifyUserProfileData.getRolesAdd() && StringUtils.isEmpty(modifyUserProfileData.getIdamStatus())) {
            validatedData.setRolesAdd(modifyUserProfileData.getRolesAdd());
            validatedData.setRolesDelete(modifyUserProfileData.getRolesDelete());
            return validatedData;
        }

        //if no roles and status is present
        if (null == modifyUserProfileData.getRolesDelete() && null == modifyUserProfileData.getRolesAdd() && !StringUtils.isEmpty(modifyUserProfileData.getIdamStatus())) {
            validatedData.setIdamStatus(modifyUserProfileData.getIdamStatus());
            return validatedData;
        }

        return modifyUserProfileData;
    }
}
