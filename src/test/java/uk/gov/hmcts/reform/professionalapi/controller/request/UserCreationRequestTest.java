package uk.gov.hmcts.reform.professionalapi.controller.request;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.professionalapi.helper.OrganisationFixtures.createJurisdictions;

import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;

import org.junit.Test;


public class UserCreationRequestTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    public void test_hasMandatoryFieldsSpecifiedNotNull() {
        UserCreationRequest userCreationRequest = new UserCreationRequest(null, null, "some@email.com", createJurisdictions());

        Set<ConstraintViolation<UserCreationRequest>> violations = validator.validate(userCreationRequest);
        
        assertThat(violations.size()).isEqualTo(2);
    }

    @Test
    public void test_ThatUserCreationIsChangedByBuilder() {
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