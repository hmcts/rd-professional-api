package uk.gov.hmcts.reform.professionalapi.generator;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiGeneratorConstants.LENGTH_OF_ORGANISATION_IDENTIFIER;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiGeneratorConstants.LENGTH_OF_UUID;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiGeneratorConstants.ORGANISATION_IDENTIFIER_FORMAT_REGEX;

import java.util.UUID;

import org.junit.Test;

public class ProfessionalApiGeneratorTest {

    @Test
    public void test_shouldReturnValidUuid() {
        UUID userIdentifier = ProfessionalApiGenerator.generateUniqueUuid();

        assertThat(userIdentifier).isNotNull();
        assertThat(userIdentifier.toString()).hasSize(LENGTH_OF_UUID);
    }

    @Test
    public void test_generateUniqueAlphanumericId() {
        String uniqueAlphanumericString = ProfessionalApiGenerator.generateUniqueAlphanumericId(LENGTH_OF_ORGANISATION_IDENTIFIER);

        assertThat(uniqueAlphanumericString).isNotNull();
        assertThat(uniqueAlphanumericString).hasSize(LENGTH_OF_ORGANISATION_IDENTIFIER);
        assertThat(uniqueAlphanumericString).matches(ORGANISATION_IDENTIFIER_FORMAT_REGEX);
    }
}
