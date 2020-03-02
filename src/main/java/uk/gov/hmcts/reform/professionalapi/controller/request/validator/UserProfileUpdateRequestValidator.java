package uk.gov.hmcts.reform.professionalapi.controller.request.validator;

import uk.gov.hmcts.reform.professionalapi.domain.UserProfileUpdatedData;

public interface UserProfileUpdateRequestValidator {

    UserProfileUpdatedData validateRequest(UserProfileUpdatedData userProfileUpdatedData);
}
