package uk.gov.hmcts.reform.professionalapi.controller.request.controller;

import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.professionalapi.controller.request.InvalidRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.UserCreationRequestValidator;

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

        List<String> validatedRoles = userCreationRequestValidatorMock.validateRoles(roles);

        assertFalse(validatedRoles.isEmpty());
    }

    @Test(expected = InvalidRequest.class)
    public void test_validateRolesThrows40WhenVerifiedRolesIsEmpty() {
        List<String> roles = new ArrayList<>();

        userCreationRequestValidatorMock.validateRoles(roles);
    }

    @Test
    public void privateConstructorTest() throws Exception {
        Constructor<UserCreationRequestValidator> constructor = UserCreationRequestValidator.class.getDeclaredConstructor();
        assertFalse(constructor.isAccessible());
        constructor.setAccessible(true);
        constructor.newInstance((Object[]) null);
    }
}
