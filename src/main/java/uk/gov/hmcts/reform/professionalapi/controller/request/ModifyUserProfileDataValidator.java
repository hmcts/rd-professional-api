package uk.gov.hmcts.reform.professionalapi.controller.request;

import uk.gov.hmcts.reform.professionalapi.domain.ModifyUserProfileData;

public interface ModifyUserProfileDataValidator {

    ModifyUserProfileData validateRequest(ModifyUserProfileData modifyUserProfileData);
}
