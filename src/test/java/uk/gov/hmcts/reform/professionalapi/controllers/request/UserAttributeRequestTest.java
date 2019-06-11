package uk.gov.hmcts.reform.professionalapi.controllers.request;

import static org.assertj.core.api.Java6Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import org.junit.Test;
import uk.gov.hmcts.reform.professionalapi.controller.request.UserAttributeRequest;

public class UserAttributeRequestTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    public void testHasMandatoryFieldsSpecified() {
        UserAttributeRequest userAttributeRequest =
                new UserAttributeRequest(null);

        Set<ConstraintViolation<UserAttributeRequest>> violations = validator.validate(userAttributeRequest);

        assertThat(violations.size()).isEqualTo(0);
    }

    @Test
    public void testCreatesNewUser() {
        List<String> userRoles = new ArrayList<>();
        userRoles.add("pui-user-manager");

        UserAttributeRequest userAttributeRequest = new UserAttributeRequest(userRoles);

        assertThat(userAttributeRequest.getUserRoles()).hasSize(1);
    }

}
