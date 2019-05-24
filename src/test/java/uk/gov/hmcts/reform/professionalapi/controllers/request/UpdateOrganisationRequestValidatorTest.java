package uk.gov.hmcts.reform.professionalapi.controllers.request;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Java6Assertions.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.Mock;
import uk.gov.hmcts.reform.professionalapi.controller.request.InvalidRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.UpdateOrganisationRequestValidator;
import uk.gov.hmcts.reform.professionalapi.controller.request.UpdateOrganisationValidator;

import java.util.UUID;

@RunWith(MockitoJUnitRunner.class)
public class UpdateOrganisationRequestValidatorTest {

    @Mock
    UpdateOrganisationValidator validator1;

    @Mock
    UpdateOrganisationValidator validator2;

    @Test
    public void validates_uuid() {

        UpdateOrganisationRequestValidator updateOrganisationRequestValidator = new UpdateOrganisationRequestValidator(asList(validator1, validator2));

        UUID responseUuid = updateOrganisationRequestValidator.validateAndReturnInputOrganisationIdentifier(UUID.randomUUID().toString());

        assertThat(responseUuid).isNotNull();
    }

    @Test(expected = InvalidRequest.class)
    public void throws_error_when_invalid_uuid() {

        UpdateOrganisationRequestValidator updateOrganisationRequestValidator = new UpdateOrganisationRequestValidator(asList(validator1, validator2));

        UUID responseUuid = updateOrganisationRequestValidator.validateAndReturnInputOrganisationIdentifier("invalid");

        assertThat(responseUuid).isNotNull();
    }
}
