package uk.gov.hmcts.reform.professionalapi.generator;

import static org.junit.Assert.assertFalse;

import java.lang.reflect.Constructor;
import org.junit.Test;
import uk.gov.hmcts.reform.professionalapi.generator.ProfessionalApiGeneratorConstants;

public class ProfessionalApiGeneratorConstantsTest {

    @Test
    public void privateConstructorTest() throws Exception {
        Constructor<ProfessionalApiGeneratorConstants> constructor = ProfessionalApiGeneratorConstants.class.getDeclaredConstructor();
        assertFalse(constructor.isAccessible());
        constructor.setAccessible(true);
        constructor.newInstance((Object[]) null);
    }

}
