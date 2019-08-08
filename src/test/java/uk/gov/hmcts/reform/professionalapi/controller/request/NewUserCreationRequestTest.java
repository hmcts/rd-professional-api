package uk.gov.hmcts.reform.professionalapi.controller.request;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;

import org.junit.Test;

public class NewUserCreationRequestTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    public void testHasMandatoryFieldsSpecified() {
        NewUserCreationRequest newUserCreationRequest =
                new NewUserCreationRequest(null, null, "someemail.com", null);

        Set<ConstraintViolation<NewUserCreationRequest>> violations = validator.validate(newUserCreationRequest);

        assertThat(violations.size()).isEqualTo(4);
    }

    @Test
    public void testCreatesNewUser() {
        List<String> userRoles = new ArrayList<>();
        userRoles.add("pui-user-manager");

        NewUserCreationRequest newUserCreationRequest =
                new NewUserCreationRequest("some-name", "some-last-name", "some@email.com",  userRoles);

        assertThat(newUserCreationRequest.getFirstName()).isEqualTo("some-name");
        assertThat(newUserCreationRequest.getLastName()).isEqualTo("some-last-name");
        assertThat(newUserCreationRequest.getEmail()).isEqualTo("some@email.com");
        assertThat(newUserCreationRequest.getRoles()).hasSize(1);
    }

    @Test
    public void testDoesNotCreateNewUserWhenEmailIsInvalid() {
        List<String> userRoles = new ArrayList<>();
        userRoles.add("pui-user-manager");

        NewUserCreationRequest newUserCreationRequest =
                new NewUserCreationRequest("some-name", "some-last-name", "someemail.com", userRoles);

        Set<ConstraintViolation<NewUserCreationRequest>> violations = validator.validate(newUserCreationRequest);

        assertThat(violations.size()).isEqualTo(1);
    }

    @Test
    public void newUserCreationBuilderTest() {
        String testFirstName = "Jane";
        String testLastName = "Doe";
        String testEmail = "jane.doe@email.com";
        List<String> testRoles = new ArrayList<>();
        testRoles.add("a role");

        NewUserCreationRequest testNewUserCreationRequest = NewUserCreationRequest.aNewUserCreationRequest()
                .firstName(testFirstName)
                .lastName(testLastName)
                .email(testEmail)
                .roles(testRoles)
                .build();

        assertThat(testNewUserCreationRequest.getFirstName()).isEqualTo(testFirstName);
        assertThat(testNewUserCreationRequest.getLastName()).isEqualTo(testLastName);
        assertThat(testNewUserCreationRequest.getEmail()).isEqualTo(testEmail);
        assertThat(testNewUserCreationRequest.getRoles()).isEqualTo(testRoles);
    }
}