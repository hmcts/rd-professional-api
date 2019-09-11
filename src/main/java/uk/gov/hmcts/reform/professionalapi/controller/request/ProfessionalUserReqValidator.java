package uk.gov.hmcts.reform.professionalapi.controller.request;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import uk.gov.hmcts.reform.professionalapi.controller.response.IdamStatus;

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
            throw new InvalidRequest("Your role does not permit you to search for users that are not active. Required status param value equal to 'Active'");
        }
    }
}
