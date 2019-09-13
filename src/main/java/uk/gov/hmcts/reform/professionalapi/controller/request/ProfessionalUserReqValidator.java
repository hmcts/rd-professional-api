package uk.gov.hmcts.reform.professionalapi.controller.request;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Component;

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

    public void validateRequest(String orgId, String showDeleted, String email) {


        if (null == orgId && null == showDeleted && null == email) {
            log.error("No input values for the request");
            throw new EmptyResultDataAccessException(1);
        }

        OrganisationCreationRequestValidator.validateEmail(email);
    }
}
