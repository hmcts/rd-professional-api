package uk.gov.hmcts.reform.professionalapi.controllers.request;

import static org.assertj.core.api.Java6Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;

import org.junit.Test;

import uk.gov.hmcts.reform.professionalapi.controller.request.NewUserCreationRequest;

public class NewUserCreationRequestTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    public void testHasMandatoryFieldsSpecified() {
        NewUserCreationRequest newUserCreationRequest =
                new NewUserCreationRequest(null, null, null, null, null);

        Set<ConstraintViolation<NewUserCreationRequest>> violations = validator.validate(newUserCreationRequest);

        assertThat(violations.size()).isEqualTo(5);
    }

    @Test
    public void testCreatesNewUser() {
        List<String> userRoles = new ArrayList<>();
        userRoles.add("pui-user-manager");

        NewUserCreationRequest newUserCreationRequest =
                new NewUserCreationRequest("some-name", "some-last-name", "some@email.com", "PENDING", userRoles);

        assertThat(newUserCreationRequest.getFirstName()).isEqualTo("some-name");
        assertThat(newUserCreationRequest.getLastName()).isEqualTo("some-last-name");
        assertThat(newUserCreationRequest.getEmail()).isEqualTo("some@email.com");
        assertThat(newUserCreationRequest.getStatus()).isEqualTo("PENDING");
        assertThat(newUserCreationRequest.getRoles()).hasSize(1);
    }

    @Test
    public void testDoesNotCreateNewUserWhenEmailIsInvalid() {
        List<String> userRoles = new ArrayList<>();
        userRoles.add("pui-user-manager");

        NewUserCreationRequest newUserCreationRequest =
                new NewUserCreationRequest("some-name", "some-last-name", "someemail.com", "PENDING", userRoles);

        Set<ConstraintViolation<NewUserCreationRequest>> violations = validator.validate(newUserCreationRequest);

        assertThat(violations.size()).isEqualTo(1);
    }
}
