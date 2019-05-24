package uk.gov.hmcts.reform.professionalapi.controllers.request;

import org.junit.Test;
import uk.gov.hmcts.reform.professionalapi.controller.request.UserAttributeRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.UserCreationRequest;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Java6Assertions.assertThat;

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

}
