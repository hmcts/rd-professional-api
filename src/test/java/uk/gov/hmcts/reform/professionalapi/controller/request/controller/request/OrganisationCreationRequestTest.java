package uk.gov.hmcts.reform.professionalapi.controller.request.controller.request;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import org.junit.Test;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest;

public class OrganisationCreationRequestTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    public void has_mandatory_fields_specified_not_null() {

        OrganisationCreationRequest organisationCreationRequest =
                new OrganisationCreationRequest(null, null, null, "false", null, null, null, null, null);

        Set<ConstraintViolation<OrganisationCreationRequest>> violations = validator.validate(organisationCreationRequest);

        assertThat(violations.size()).isEqualTo(3);
    }

    @Test
    public void testOrganisationCreationRequest() {

        OrganisationCreationRequest organisationCreationRequest =
                new OrganisationCreationRequest("test", "PENDING", "sra-id", "false", "number02", "company-url", null, null, null);

        organisationCreationRequest.setStatus("ACTIVE");

        assertThat(organisationCreationRequest.getName()).isEqualTo("test");
        assertThat(organisationCreationRequest.getStatus()).isEqualTo("ACTIVE");
        assertThat(organisationCreationRequest.getSraId()).isEqualTo("sra-id");
        assertThat(organisationCreationRequest.getSraRegulated()).isEqualTo("false");
    }
}
