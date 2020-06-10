package uk.gov.hmcts.reform.professionalapi.controller.request.validator;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.junit.Assert.assertFalse;
import static uk.gov.hmcts.reform.professionalapi.controller.request.validator.UserCreationRequestValidator.validateRoles;

import java.lang.reflect.Constructor;
import java.util.List;
import org.junit.Ignore;
import org.junit.Test;
import uk.gov.hmcts.reform.professionalapi.controller.request.InvalidRequest;

public class UserCreationRequestValidatorTest {

    final String userManagerRole = "pui-user-manager";
    final String caseManagerRole = "pui-case-manager";


    @Test
    public void test_validateRoles() {
        List<String> validatedRoles = validateRoles(asList(userManagerRole, caseManagerRole));
        assertFalse(validatedRoles.isEmpty());
    }

    @Test(expected = InvalidRequest.class)
    public void test_validateRolesThrows400WhenVerifiedRolesIsEmpty() {
        validateRoles(emptyList());
    }

    @Test
    @Ignore
    public void privateConstructorTest() throws Exception {
        Constructor<UserCreationRequestValidator> constructor = UserCreationRequestValidator.class.getDeclaredConstructor();
        //assertFalse(constructor.isAccessible());
        constructor.setAccessible(true);
        constructor.newInstance((Object[]) null);
    }
}
