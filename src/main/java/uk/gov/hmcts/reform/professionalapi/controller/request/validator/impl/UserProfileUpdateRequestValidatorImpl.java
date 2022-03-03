package uk.gov.hmcts.reform.professionalapi.controller.request.validator.impl;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.professionalapi.controller.constants.ErrorConstants;
import uk.gov.hmcts.reform.professionalapi.controller.request.InvalidRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.validator.UserProfileUpdateRequestValidator;
import uk.gov.hmcts.reform.professionalapi.domain.UserProfileUpdatedData;

@Component
public class UserProfileUpdateRequestValidatorImpl implements UserProfileUpdateRequestValidator {

    @Override
    public UserProfileUpdatedData validateRequest(UserProfileUpdatedData userProfileUpdatedData) {

        var validatedData = new UserProfileUpdatedData();

        //if required fields are empty
        if (null == userProfileUpdatedData.getRolesAdd() && null == userProfileUpdatedData
                .getRolesDelete() && StringUtils.isEmpty(userProfileUpdatedData.getIdamStatus())) {
            throw new InvalidRequest(ErrorConstants.INVALID_REQUEST.getErrorMessage());
        }

        //if all are present
        if (null != userProfileUpdatedData.getRolesAdd() && null != userProfileUpdatedData
                .getRolesDelete() && !StringUtils.isEmpty(userProfileUpdatedData.getIdamStatus())
                && !StringUtils.isEmpty(userProfileUpdatedData
                .getFirstName()) && !StringUtils.isEmpty(userProfileUpdatedData.getLastName()) && !StringUtils
                .isEmpty(userProfileUpdatedData.getEmail())) {

            validatedData.setRolesAdd(userProfileUpdatedData.getRolesAdd());
            validatedData.setRolesDelete(userProfileUpdatedData.getRolesDelete());
            return validatedData;
        }

        //if both roles and status are present
        if (null != userProfileUpdatedData.getRolesAdd() && null != userProfileUpdatedData
                .getRolesDelete() && !StringUtils.isEmpty(userProfileUpdatedData.getIdamStatus())) {
            validatedData.setRolesAdd(userProfileUpdatedData.getRolesAdd());
            validatedData.setRolesDelete(userProfileUpdatedData.getRolesDelete());
            return validatedData;
        }

        //if one role and status are present
        if (null != userProfileUpdatedData.getRolesDelete() || null != userProfileUpdatedData
                .getRolesAdd() && StringUtils.isEmpty(userProfileUpdatedData.getIdamStatus())) {
            validatedData.setRolesAdd(userProfileUpdatedData.getRolesAdd());
            validatedData.setRolesDelete(userProfileUpdatedData.getRolesDelete());
            return validatedData;
        }

        //if no roles and status is present
        if (null == userProfileUpdatedData.getRolesDelete() && null == userProfileUpdatedData
                .getRolesAdd() && !StringUtils.isEmpty(userProfileUpdatedData.getIdamStatus())) {
            validatedData.setIdamStatus(userProfileUpdatedData.getIdamStatus());
            return validatedData;
        }

        return userProfileUpdatedData;
    }
}
