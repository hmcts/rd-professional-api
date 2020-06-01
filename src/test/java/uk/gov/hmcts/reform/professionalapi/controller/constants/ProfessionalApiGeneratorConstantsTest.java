package uk.gov.hmcts.reform.professionalapi.controller.constants;

import static org.junit.Assert.assertFalse;

import java.lang.reflect.Constructor;
import org.junit.Test;

public class ProfessionalApiGeneratorConstantsTest {

    @Test
    public void privateConstructorTest() throws Exception {
        Constructor<ProfessionalApiConstants> constructor = ProfessionalApiConstants.class.getDeclaredConstructor();
        assertFalse(constructor.isAccessible());
        constructor.setAccessible(true);
        constructor.newInstance((Object[]) null);
    }
}
