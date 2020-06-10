package uk.gov.hmcts.reform.professionalapi.controller.constants;

import java.lang.reflect.Constructor;

import org.junit.Ignore;
import org.junit.Test;

public class ProfessionalApiGeneratorConstantsTest {

    @Test
    @Ignore
    public void privateConstructorTest() throws Exception {
        Constructor<ProfessionalApiGeneratorConstants> constructor = ProfessionalApiGeneratorConstants.class.getDeclaredConstructor();
        //assertFalse(constructor.isAccessible());
        constructor.setAccessible(true);
        constructor.newInstance((Object[]) null);
    }
}
