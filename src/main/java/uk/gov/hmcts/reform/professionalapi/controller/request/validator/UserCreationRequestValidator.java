package uk.gov.hmcts.reform.professionalapi.controller.request.validator;

import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.reform.professionalapi.controller.request.InvalidRequest;

@Slf4j
public class UserCreationRequestValidator {

    private UserCreationRequestValidator() {
    }

    public static List<String> validateRoles(List<String> roles) {
        if (CollectionUtils.isEmpty(roles)) {
            throw new InvalidRequest("No role(s) provided");
        }
        return roles;
    }
}
