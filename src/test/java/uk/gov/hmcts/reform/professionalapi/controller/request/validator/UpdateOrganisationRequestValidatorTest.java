package uk.gov.hmcts.reform.professionalapi.controller.request.validator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import uk.gov.hmcts.reform.professionalapi.controller.request.validator.OrganisationIdentifierValidator;
import uk.gov.hmcts.reform.professionalapi.controller.request.validator.UpdateOrganisationRequestValidator;

public class UpdateOrganisationRequestValidatorTest {

    private OrganisationIdentifierValidator organisationIdentifierValidator = mock(OrganisationIdentifierValidator.class);

    @Test
    public void test_UpdateOrganisationRequestValidator() {
        List<OrganisationIdentifierValidator> updateOrganisationRequestValidatorList = new ArrayList<>();
        updateOrganisationRequestValidatorList.add(organisationIdentifierValidator);

        UpdateOrganisationRequestValidator updateOrganisationRequestValidator = new UpdateOrganisationRequestValidator(updateOrganisationRequestValidatorList);

        assertThat(updateOrganisationRequestValidator).isNotNull();
    }
}
