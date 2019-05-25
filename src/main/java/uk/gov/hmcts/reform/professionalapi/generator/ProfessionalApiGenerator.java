package uk.gov.hmcts.reform.professionalapi.generator;

import java.util.UUID;

public interface ProfessionalApiGenerator {

    int LENGTH_OF_UUID = 36;

    static UUID generateUniqueUuid() {
        return UUID.randomUUID();
    }
}
