package uk.gov.hmcts.reform.professionalapi.generator;

import static org.assertj.core.api.Java6Assertions.assertThat;

import java.util.UUID;
import org.junit.Test;

public class ProfessionalApiGeneratorTest {

    @Test
    public void shouldReturnValidUuid() {

        UUID userIdentifier = ProfessionalApiGenerator.generateUniqueUuid();
        assertThat(userIdentifier).isNotNull();
        assertThat(userIdentifier.toString().length()).isEqualTo(ProfessionalApiGenerator.LENGTH_OF_UUID);
    }
}
