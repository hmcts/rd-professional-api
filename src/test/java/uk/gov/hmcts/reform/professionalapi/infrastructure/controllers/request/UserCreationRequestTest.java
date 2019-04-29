package uk.gov.hmcts.reform.professionalapi.infrastructure.controllers.request;

import static org.assertj.core.api.Java6Assertions.assertThat;

import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import org.junit.Test;

public class UserCreationRequestTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    public void has_mandatory_fields_specified_not_null() {

        UserCreationRequest userCreationRequest =
                new UserCreationRequest(null, null, null);

        Set<ConstraintViolation<UserCreationRequest>> violations =
                validator.validate(userCreationRequest);

        assertThat(violations.size()).isEqualTo(3);
    }
    
}