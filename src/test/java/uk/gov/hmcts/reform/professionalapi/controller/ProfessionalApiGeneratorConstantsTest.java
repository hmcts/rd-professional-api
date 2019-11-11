package uk.gov.hmcts.reform.professionalapi.controller;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Constructor;
import org.junit.Test;
import uk.gov.hmcts.reform.professionalapi.generator.ProfessionalApiGeneratorConstants;

public class ProfessionalApiGeneratorConstantsTest {

    @Test
    public void privateConstructorTest() throws Exception {
        Constructor<ProfessionalApiGeneratorConstants> constructor = ProfessionalApiGeneratorConstants.class.getDeclaredConstructor();
        assertEquals(constructor.isAccessible(), false);
        constructor.setAccessible(true);
        constructor.newInstance((Object[]) null);
    }

}
