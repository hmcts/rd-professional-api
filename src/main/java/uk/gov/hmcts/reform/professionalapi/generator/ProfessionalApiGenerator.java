package uk.gov.hmcts.reform.professionalapi.generator;

import java.util.UUID;
import org.apache.commons.lang3.RandomStringUtils;


/**
 * This interface is responsible for generating unique IDs.
 */

public interface ProfessionalApiGenerator {

    int LENGTH_OF_UUID = 36;
    int LENGTH_OF_ORGANISATION_IDENTIFIER = 7;
    String ORGANISATION_IDENTIFIER_FORMAT_REGEX = "^(?=.*[A-Z])(?=.*[0-9])[A-Z0-9]{7}$";

    /**
     * Generates UUID which is unique.
     * @return UUID
     */
    static UUID generateUniqueUuid() {
        return UUID.randomUUID();
    }

    /**
     * Generates unique string which has following constraints.
     * 1)Unique for every organisation
     * 2)Is 7 characters long
     * 3)Consists of upper-case alphanumeric characters [0-9] and [A-Z]
     * 4)Is different from the Primary key of the Organisation
     * 5)Does not involve special characters such as !, @, # etc.
     * 5)Is difficult to guess - e.g. should not be the first 7 Natural Nos. 1234567
     * 6)Should not be sequential
     *
     * @param lengthOfString length of string needs be formed
     * @return String unique alphanumeric string
     */
    static String generateUniqueAlphanumericId(int lengthOfString) {
        String generatedString = null;
        while (true) {
            generatedString = RandomStringUtils.randomAlphanumeric(lengthOfString);
            if (generatedString.matches(ORGANISATION_IDENTIFIER_FORMAT_REGEX)) {
                break;
            }
        }
        return generatedString;
    }
}
