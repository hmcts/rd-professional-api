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
     * This method generates UUID which is unique.
     * @return UUID
     */
    static UUID generateUniqueUuid() {
        return UUID.randomUUID();
    }

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
