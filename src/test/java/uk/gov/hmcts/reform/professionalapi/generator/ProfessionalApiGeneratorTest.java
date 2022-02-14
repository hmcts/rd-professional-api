package uk.gov.hmcts.reform.professionalapi.generator;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.LENGTH_OF_ORGANISATION_IDENTIFIER;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.LENGTH_OF_UUID;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.ORGANISATION_IDENTIFIER_FORMAT_REGEX;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProfessionalApiGeneratorTest {

    @Test
    void test_shouldReturnValidUuid() {
        UUID userIdentifier = ProfessionalApiGenerator.generateUniqueUuid();

        assertThat(userIdentifier).isNotNull();
        assertThat(userIdentifier.toString()).hasSize(LENGTH_OF_UUID);
    }

    @Test
    void test_generateUniqueAlphanumericId() {
        String uniqueAlphanumericString
                = ProfessionalApiGenerator.generateUniqueAlphanumericId(LENGTH_OF_ORGANISATION_IDENTIFIER);

        assertThat(uniqueAlphanumericString).isNotNull()
                                            .hasSize(LENGTH_OF_ORGANISATION_IDENTIFIER)
                                            .matches(ORGANISATION_IDENTIFIER_FORMAT_REGEX);
    }
}
