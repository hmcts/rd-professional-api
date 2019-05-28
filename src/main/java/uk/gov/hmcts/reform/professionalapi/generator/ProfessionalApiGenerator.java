package uk.gov.hmcts.reform.professionalapi.generator;

import java.util.UUID;

/**
 * This interface is responsible for generating unique UUID.
 */

public interface ProfessionalApiGenerator {

    int LENGTH_OF_UUID = 36;

    static UUID generateUniqueUuid() {
        return UUID.randomUUID();
    }
}
