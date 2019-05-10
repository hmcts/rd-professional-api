package uk.gov.hmcts.reform.professionalapi.controllers.request;

import org.junit.Test;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.Set;

import static org.assertj.core.api.Java6Assertions.assertThat;

public class OrganisationCreationRequestTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    public void has_mandatory_fields_specified_not_null() {

        OrganisationCreationRequest organisationCreationRequest =
                new OrganisationCreationRequest(null, null ,null, Boolean.FALSE,null,null,null,null, null);

        Set<ConstraintViolation<OrganisationCreationRequest>> violations =
                validator.validate(organisationCreationRequest);

        assertThat(violations.size()).isEqualTo(2);
    }
}