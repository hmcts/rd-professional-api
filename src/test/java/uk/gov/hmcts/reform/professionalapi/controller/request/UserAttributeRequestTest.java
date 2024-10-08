package uk.gov.hmcts.reform.professionalapi.controller.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class UserAttributeRequestTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
    private final String puiUserManager = "pui-user-manager";

    @Test
    void test_has_mandatory_fields_specified_not_null() {
        UserAttributeRequest userAttributeRequest = new UserAttributeRequest(null);

        Set<ConstraintViolation<UserAttributeRequest>> violations = validator.validate(userAttributeRequest);

        assertThat(violations).isEmpty();
    }

    @Test
    void test_creates_new_user_creation_request_correctly() {
        UserAttributeRequest userAttributeRequest = new UserAttributeRequest(singletonList(puiUserManager));

        assertThat(userAttributeRequest.getUserRoles()).hasSize(1);
        assertThat(userAttributeRequest.getUserRoles().get(0)).isEqualTo(puiUserManager);
    }

    @Test
    void test_userAttributeBuilder() {
        UserAttributeRequest testUserAttributeRequest = UserAttributeRequest.aUserAttributeCreationRequest()
                .userRoles(singletonList(puiUserManager)).build();

        assertThat(testUserAttributeRequest.getUserRoles()).hasSize(1);
        assertThat(testUserAttributeRequest.getUserRoles().get(0)).isEqualTo(puiUserManager);
    }
}