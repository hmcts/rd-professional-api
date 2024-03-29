package uk.gov.hmcts.reform.professionalapi.controller.request.validator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class UpdateOrganisationRequestValidatorTest {

    @Test
    void test_UpdateOrganisationRequestValidator() {

        List<OrganisationIdentifierValidator> updateOrganisationRequestValidatorList = new ArrayList<>();
        updateOrganisationRequestValidatorList.add(mock(OrganisationIdentifierValidator.class));

        UpdateOrganisationRequestValidator updateOrganisationRequestValidator
                = new UpdateOrganisationRequestValidator(updateOrganisationRequestValidatorList);

        assertThat(updateOrganisationRequestValidator).isNotNull();
    }

}
