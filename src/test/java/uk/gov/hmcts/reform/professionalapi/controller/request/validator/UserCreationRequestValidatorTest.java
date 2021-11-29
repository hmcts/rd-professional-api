package uk.gov.hmcts.reform.professionalapi.controller.request.validator;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.professionalapi.controller.request.validator.UserCreationRequestValidator.validateRoles;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.professionalapi.controller.request.InvalidRequest;

@ExtendWith(MockitoExtension.class)
class UserCreationRequestValidatorTest {

    final String userManagerRole = "pui-user-manager";
    final String caseManagerRole = "pui-case-manager";

    @Test
    void test_validateRoles() {
        List<String> validatedRoles = validateRoles(asList(userManagerRole, caseManagerRole));
        assertFalse(validatedRoles.isEmpty());
    }

    @Test
    void test_validateRolesThrows400WhenVerifiedRolesIsEmpty() {
        List<String> emptyList = emptyList();

        assertThrows(InvalidRequest.class, () ->
                validateRoles(emptyList));
    }

    @Test
    void test_privateConstructor() throws Exception {
        Constructor<UserCreationRequestValidator> constructor
                = UserCreationRequestValidator.class.getDeclaredConstructor();
        assertTrue(Modifier.isPrivate(constructor.getModifiers()));
        constructor.setAccessible(true);
        constructor.newInstance((Object[]) null);
    }
}
