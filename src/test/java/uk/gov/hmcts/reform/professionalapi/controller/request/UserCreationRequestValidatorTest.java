package uk.gov.hmcts.reform.professionalapi.controller.request;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.junit.Assert.assertFalse;
import static uk.gov.hmcts.reform.professionalapi.controller.request.UserCreationRequestValidator.validateRoles;

import java.lang.reflect.Constructor;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.professionalapi.domain.PrdEnum;
import uk.gov.hmcts.reform.professionalapi.domain.PrdEnumId;

public class UserCreationRequestValidatorTest {

    private List<PrdEnum> prdEnumList;

    final String userManagerRole = "pui-user-manager";
    final String caseManagerRole = "pui-case-manager";
    final String sidamRole = "SIDAM_ROLE";

    @Before
    public void setUp() {
        prdEnumList = asList(
                new PrdEnum(new PrdEnumId(2, sidamRole), userManagerRole, sidamRole),
                new PrdEnum(new PrdEnumId(3, sidamRole), caseManagerRole, sidamRole));
    }

    @Test
    public void test_validateRoles() {
        List<String> validatedRoles = validateRoles(asList(userManagerRole, caseManagerRole), prdEnumList);
        assertFalse(validatedRoles.isEmpty());
    }

    @Test(expected = InvalidRequest.class)
    public void test_validateRolesThrows40WhenVerifiedRolesIsEmpty() {
        validateRoles(emptyList(), prdEnumList);
    }

    @Test
    public void privateConstructorTest() throws Exception {
        Constructor<UserCreationRequestValidator> constructor = UserCreationRequestValidator.class.getDeclaredConstructor();
        assertFalse(constructor.isAccessible());
        constructor.setAccessible(true);
        constructor.newInstance((Object[]) null);
    }
}
