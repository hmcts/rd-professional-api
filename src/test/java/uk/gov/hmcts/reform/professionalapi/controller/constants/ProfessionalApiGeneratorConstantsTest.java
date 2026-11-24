package uk.gov.hmcts.reform.professionalapi.controller.constants;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ProfessionalApiGeneratorConstantsTest {

    @Test
    void privateConstructorTest() throws Exception {
        Constructor<ProfessionalApiConstants> constructor = ProfessionalApiConstants.class.getDeclaredConstructor();
        assertTrue(Modifier.isPrivate(constructor.getModifiers()));
        constructor.setAccessible(true);
        constructor.newInstance((Object[]) null);
    }
}
