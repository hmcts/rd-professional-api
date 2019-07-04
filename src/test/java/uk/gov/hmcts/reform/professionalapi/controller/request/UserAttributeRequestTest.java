package uk.gov.hmcts.reform.professionalapi.controller.request;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import org.junit.Test;

public class UserAttributeRequestTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    public void has_mandatory_fields_specified_not_null() {
        UserAttributeRequest userAttributeRequest =
                new UserAttributeRequest(null);

        Set<ConstraintViolation<UserAttributeRequest>> violations = validator.validate(userAttributeRequest);

        assertThat(violations.size()).isEqualTo(0);
    }

    @Test
    public void creates_new_user_creation_request_correctly() {
        List<String> userRoles = new ArrayList<>();
        userRoles.add("pui-user-manager");

        UserAttributeRequest userAttributeRequest = new UserAttributeRequest(userRoles);

        assertThat(userAttributeRequest.getUserRoles()).hasSize(1);
    }

    @Test
    public void userAttributeBuilderTest() {
        List<String> testUserRoles = new ArrayList<>();
        testUserRoles.add("pui-user-manager");

        UserAttributeRequest testUserAttributeRequest = UserAttributeRequest.aUserAttributeCreationRequest()
                .userRoles(testUserRoles)
                .build();

        assertThat(testUserAttributeRequest.getUserRoles()).isEqualTo(testUserRoles);
    }
}