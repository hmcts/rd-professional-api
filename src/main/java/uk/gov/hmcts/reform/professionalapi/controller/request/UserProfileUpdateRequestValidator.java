package uk.gov.hmcts.reform.professionalapi.controller.request;

import uk.gov.hmcts.reform.professionalapi.domain.UserProfileUpdatedData;

public interface UserProfileUpdateRequestValidator {

    UserProfileUpdatedData validateRequest(UserProfileUpdatedData userProfileUpdatedData);
}
