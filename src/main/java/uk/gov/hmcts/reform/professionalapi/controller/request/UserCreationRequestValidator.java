package uk.gov.hmcts.reform.professionalapi.controller.request;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

@Slf4j
public class UserCreationRequestValidator {

    private UserCreationRequestValidator() {
    }


    public static List<String> validateRoles(List<String> roles) {

        if (CollectionUtils.isEmpty(roles)) {
            log.error("No user role(s) provided");
            throw new InvalidRequest("No role(s) provided");
        }

        return roles;
    }
}
