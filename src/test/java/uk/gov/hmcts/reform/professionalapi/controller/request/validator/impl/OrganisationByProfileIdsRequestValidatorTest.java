package uk.gov.hmcts.reform.professionalapi.controller.request.validator.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.professionalapi.controller.request.InvalidRequest;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class OrganisationByProfileIdsRequestValidatorTest {

    private final OrganisationByProfileIdsRequestValidator validator =
            new OrganisationByProfileIdsRequestValidator();

    @Test
    void shouldNotThrowExceptionWhenPageSizeAndSearchAfterAreValid() {
        assertDoesNotThrow(() -> validator.validate(10));
    }

    @Test
    void shouldNotThrowExceptionWhenPageSizeIsNull() {
        assertDoesNotThrow(() -> validator.validate(null));
    }

    @Test
    void shouldThrowExceptionWhenPageSizeIsLessThanOne() {
        assertThrows(InvalidRequest.class, () -> validator.validate(0));
    }
}
