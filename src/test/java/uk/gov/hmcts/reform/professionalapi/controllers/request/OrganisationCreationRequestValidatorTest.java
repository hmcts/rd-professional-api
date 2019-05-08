package uk.gov.hmcts.reform.professionalapi.controllers.request;

import static java.util.Arrays.asList;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequestValidator;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationRequestValidator;

@RunWith(MockitoJUnitRunner.class)
public class OrganisationCreationRequestValidatorTest {

    @Mock
    OrganisationRequestValidator validator1;

    @Mock
    OrganisationRequestValidator validator2;

    @Mock
    OrganisationCreationRequest request;

    @Test
    public void calls_all_validators() {

        OrganisationCreationRequestValidator organisationCreationRequestValidator =
                new OrganisationCreationRequestValidator(asList(validator1, validator2));

        organisationCreationRequestValidator.validate(request);

        verify(validator1, times(1)).validate(request);
        verify(validator2, times(1)).validate(request);
    }
}