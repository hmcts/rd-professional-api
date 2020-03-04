package uk.gov.hmcts.reform.professionalapi.controller.advice;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import uk.gov.hmcts.reform.professionalapi.controller.constants.ErrorConstants;

public class ResourceNotFoundExceptionTest {

    private ResourceNotFoundException resourceNotFoundException;

    @Test
    public void getResourceNotFoundExceptionTest() {
        resourceNotFoundException = new ResourceNotFoundException(ErrorConstants.RESOURCE_NOT_FOUND.getErrorMessage());
        assertThat(resourceNotFoundException.getMessage()).isEqualTo(ErrorConstants.RESOURCE_NOT_FOUND.getErrorMessage());
    }
}
