package uk.gov.hmcts.reform.professionalapi.controller.request.validator;

import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.reform.professionalapi.controller.request.InvalidRequest;

@Component
@Slf4j
public class UserCreationRequestValidator {

    private static String loggingComponentName;

    private UserCreationRequestValidator() {
    }

    public static List<String> validateRoles(List<String> roles) {

        if (CollectionUtils.isEmpty(roles)) {
            log.error("{}:: No user role(s) provided", loggingComponentName);
            throw new InvalidRequest("No role(s) provided");
        }

        return roles;
    }

    @Value("${logging-component-name}")
    public void setLoggingComponentName(String loggingComponentName) {
        UserCreationRequestValidator.loggingComponentName = loggingComponentName;
    }
}
