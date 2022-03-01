package uk.gov.hmcts.reform.professionalapi.controller.request;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserCreationRequestTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void test_hasMandatoryFieldsSpecifiedNotNull() {
        UserCreationRequest userCreationRequest = new UserCreationRequest(null, null,
                "some@email.com");

        Set<ConstraintViolation<UserCreationRequest>> violations = validator.validate(userCreationRequest);
        
        assertThat(violations).hasSize(2);
    }

    @Test
    void test_ThatUserCreationIsChangedByBuilder() {
        String firstName2 = "testFn";
        String lastName2 = "testLn";
        String email2 = "test@test.com";

        UserCreationRequest testUserCreationRequest = UserCreationRequest.aUserCreationRequest()
                .firstName(firstName2)
                .lastName(lastName2)
                .email(email2)
                .build();

        assertThat(testUserCreationRequest.getFirstName()).isEqualTo(firstName2);
        assertThat(testUserCreationRequest.getLastName()).isEqualTo(lastName2);
        assertThat(testUserCreationRequest.getEmail()).isEqualTo(email2);
    }

    @Test
    void test_ThatUserCreationIsChangedBySetter() {
        String firstName2 = "testFn";
        String lastName2 = "testLn";
        String email2 = "test@test.com";

        UserCreationRequest testUserCreationRequest =
                new UserCreationRequest(firstName2, lastName2, "email");

        testUserCreationRequest.setEmail(email2);

        assertThat(testUserCreationRequest.getFirstName()).isEqualTo(firstName2);
        assertThat(testUserCreationRequest.getLastName()).isEqualTo(lastName2);
        assertThat(testUserCreationRequest.getEmail()).isEqualTo(email2);
    }
}