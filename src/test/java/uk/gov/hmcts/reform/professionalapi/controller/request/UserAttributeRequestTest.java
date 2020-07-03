package uk.gov.hmcts.reform.professionalapi.controller.request;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import org.junit.Test;

public class UserAttributeRequestTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
    private String puiUserManager = "pui-user-manager";

    @Test
    public void has_mandatory_fields_specified_not_null() {
        UserAttributeRequest userAttributeRequest = new UserAttributeRequest(null);

        Set<ConstraintViolation<UserAttributeRequest>> violations = validator.validate(userAttributeRequest);

        assertThat(violations.size()).isEqualTo(0);
    }

    @Test
    public void creates_new_user_creation_request_correctly() {
        UserAttributeRequest userAttributeRequest = new UserAttributeRequest(singletonList(puiUserManager));

        assertThat(userAttributeRequest.getUserRoles()).hasSize(1);
        assertThat(userAttributeRequest.getUserRoles().get(0)).isEqualTo(puiUserManager);
    }

    @Test
    public void userAttributeBuilderTest() {
        UserAttributeRequest testUserAttributeRequest = UserAttributeRequest.aUserAttributeCreationRequest()
                .userRoles(singletonList(puiUserManager)).build();

        assertThat(testUserAttributeRequest.getUserRoles()).hasSize(1);
        assertThat(testUserAttributeRequest.getUserRoles().get(0)).isEqualTo(puiUserManager);
    }
}