package uk.gov.hmcts.reform.professionalapi.generator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static uk.gov.hmcts.reform.professionalapi.generator.ProfessionalApiGeneratorConstants.LENGTH_OF_ORGANISATION_IDENTIFIER;
import static uk.gov.hmcts.reform.professionalapi.generator.ProfessionalApiGeneratorConstants.LENGTH_OF_UUID;
import static uk.gov.hmcts.reform.professionalapi.generator.ProfessionalApiGeneratorConstants.ORGANISATION_IDENTIFIER_FORMAT_REGEX;

import java.lang.reflect.Constructor;
import java.util.UUID;
import org.junit.Test;

public class ProfessionalApiGeneratorTest {

    @Test
    public void shouldReturnValidUuid() {

        UUID userIdentifier = ProfessionalApiGenerator.generateUniqueUuid();
        assertThat(userIdentifier).isNotNull();
        assertThat(userIdentifier.toString().length()).isEqualTo(LENGTH_OF_UUID);
    }

    @Test
    public void generateUniqueAlphanumericId() {
        String uniqueAlphanumericString = ProfessionalApiGenerator.generateUniqueAlphanumericId(LENGTH_OF_ORGANISATION_IDENTIFIER);

        assertThat(uniqueAlphanumericString).isNotNull();
        assertThat(uniqueAlphanumericString.length()).isEqualTo(LENGTH_OF_ORGANISATION_IDENTIFIER);
        assertThat(uniqueAlphanumericString.matches(ORGANISATION_IDENTIFIER_FORMAT_REGEX)).isTrue();
    }

    @Test
    public void professionalApiGeneratorConstantsPrivateConstructorTest() throws Exception {
        Constructor<ProfessionalApiGeneratorConstants> constructor = ProfessionalApiGeneratorConstants.class.getDeclaredConstructor();
        assertFalse(constructor.isAccessible());
        constructor.setAccessible(true);
        constructor.newInstance((Object[]) null);
    }
}
