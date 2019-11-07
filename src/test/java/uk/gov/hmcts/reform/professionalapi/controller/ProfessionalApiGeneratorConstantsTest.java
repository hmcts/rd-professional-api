package uk.gov.hmcts.reform.professionalapi.controller;

import org.junit.Test;
import uk.gov.hmcts.reform.professionalapi.generator.ProfessionalApiGeneratorConstants;

import java.lang.reflect.Constructor;

import static org.junit.Assert.assertEquals;

public class ProfessionalApiGeneratorConstantsTest {

    @Test
    public void privateConstructorTest() throws Exception {
        Constructor<ProfessionalApiGeneratorConstants> constructor = ProfessionalApiGeneratorConstants.class.getDeclaredConstructor();
        assertEquals(constructor.isAccessible(), false);
        constructor.setAccessible(true);
        constructor.newInstance((Object[]) null);
    }

}
