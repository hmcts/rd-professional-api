package uk.gov.hmcts.reform.professionalapi.controller.request;

import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.professionalapi.controller.request.InvalidRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.UserCreationRequestValidator;
import uk.gov.hmcts.reform.professionalapi.domain.PrdEnum;
import uk.gov.hmcts.reform.professionalapi.domain.PrdEnumId;

public class UserCreationRequestValidatorTest {

    static UserCreationRequestValidator userCreationRequestValidatorMock;

    final String userManagerRole = "pui-user-manager";
    final String caseManagerRole = "pui-case-manager";

    @Before
    public void setUp() {
        userCreationRequestValidatorMock = mock(UserCreationRequestValidator.class);
    }

    @Test
    public void test_validateRoles() {
        List<String> roles = new ArrayList<>();
        roles.add(userManagerRole);
        roles.add(caseManagerRole);

        List<PrdEnum> prdEnumList = new ArrayList<>();
        prdEnumList.add(new PrdEnum(new PrdEnumId(2, "SIDAM_ROLE"), userManagerRole, "SIDAM_ROLE"));
        prdEnumList.add(new PrdEnum(new PrdEnumId(3, "SIDAM_ROLE"), caseManagerRole, "SIDAM_ROLE"));

        List<String> validatedRoles = userCreationRequestValidatorMock.validateRoles(roles, prdEnumList);

        assertFalse(validatedRoles.isEmpty());
    }

    @Test(expected = InvalidRequest.class)
    public void test_validateRolesThrows40WhenVerifiedRolesIsEmpty() {
        List<String> roles = new ArrayList<>();
        List<PrdEnum> prdEnumList = new ArrayList<>();
        prdEnumList.add(new PrdEnum(new PrdEnumId(2, "SIDAM_ROLE"), userManagerRole, "SIDAM_ROLE"));
        prdEnumList.add(new PrdEnum(new PrdEnumId(3, "SIDAM_ROLE"), caseManagerRole, "SIDAM_ROLE"));

        userCreationRequestValidatorMock.validateRoles(roles, prdEnumList);
    }

    @Test
    public void privateConstructorTest() throws Exception {
        Constructor<UserCreationRequestValidator> constructor = UserCreationRequestValidator.class.getDeclaredConstructor();
        assertFalse(constructor.isAccessible());
        constructor.setAccessible(true);
        constructor.newInstance((Object[]) null);
    }
}
