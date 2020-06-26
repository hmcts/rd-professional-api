package uk.gov.hmcts.reform.professionalapi.controller.constants;

import static org.junit.Assert.assertTrue;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

import org.junit.Test;

public class ProfessionalApiGeneratorConstantsTest {

    @Test
    public void privateConstructorTest() throws Exception {
        Constructor<ProfessionalApiConstants> constructor = ProfessionalApiConstants.class.getDeclaredConstructor();
        assertTrue(Modifier.isPrivate(constructor.getModifiers()));
        constructor.setAccessible(true);
        constructor.newInstance((Object[]) null);
    }
}
