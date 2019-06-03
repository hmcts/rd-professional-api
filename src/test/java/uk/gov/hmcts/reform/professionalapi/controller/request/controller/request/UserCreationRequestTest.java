package uk.gov.hmcts.reform.professionalapi.controller.request.controller.request;

import static org.assertj.core.api.Java6Assertions.assertThat;

import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;

import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.professionalapi.controller.request.UserCreationRequest;

public class UserCreationRequestTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
    private UserCreationRequest userCreationRequest;

    private String firstName = "Joe";
    private String lastName = "Bloggs";
    private String email = "joe.bloggs@email.com";

    @Before
    public void setUp() {
        userCreationRequest = new UserCreationRequest(firstName, lastName, email);
    }

    @Test
    public void has_mandatory_fields_specified_not_null() {

        UserCreationRequest userCreationRequest =
                new UserCreationRequest(null, null, null);

        Set<ConstraintViolation<UserCreationRequest>> violations =
                validator.validate(userCreationRequest);

        assertThat(violations.size()).isEqualTo(3);
    }

    @Test
    public void testThatUserCreationIsChangedByBuilder() {
        String firstName2 = "Jane";
        String lastName2 = "Doe";
        String email2 = "jane.doe@email.com";

        UserCreationRequest testUserCreationRequest = UserCreationRequest.aUserCreationRequest()
                .firstName(firstName2)
                .lastName(lastName2)
                .email(email2)
                .build();

        assertThat(testUserCreationRequest.getFirstName()).isEqualTo(firstName2);
        assertThat(testUserCreationRequest.getLastName()).isEqualTo(lastName2);
        assertThat(testUserCreationRequest.getEmail()).isEqualTo(email2);
    }

}