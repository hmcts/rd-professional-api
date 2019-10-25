package uk.gov.hmcts.reform.professionalapi.controller.request.controller.request;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import uk.gov.hmcts.reform.professionalapi.controller.request.UpdateOrganisationRequestValidator;
import uk.gov.hmcts.reform.professionalapi.controller.request.UpdateOrganisationValidator;

public class UpdateOrganisationRequestValidatorTest {

    @Test
    public void test_UpdateOrganisationRequestValidator() {

        List<UpdateOrganisationValidator> updateOrganisationRequestValidatorList = new ArrayList<>();
        updateOrganisationRequestValidatorList.add(mock(UpdateOrganisationValidator.class));

        UpdateOrganisationRequestValidator updateOrganisationRequestValidator = new UpdateOrganisationRequestValidator(updateOrganisationRequestValidatorList);

        assertThat(updateOrganisationRequestValidator).isNotNull();
    }

}
