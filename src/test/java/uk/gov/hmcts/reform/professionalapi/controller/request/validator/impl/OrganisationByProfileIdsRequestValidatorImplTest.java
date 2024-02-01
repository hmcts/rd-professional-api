package uk.gov.hmcts.reform.professionalapi.controller.request.validator.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.professionalapi.controller.request.InvalidRequest;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
public class OrganisationByProfileIdsRequestValidatorImplTest {

    private final OrganisationByProfileIdsRequestValidatorImpl validator = new OrganisationByProfileIdsRequestValidatorImpl();

    @Test
    void shouldNotThrowExceptionWhenPageSizeAndSearchAfterAreValid() {
        assertDoesNotThrow(() -> validator.Validate(10, UUID.randomUUID()));
    }

    @Test
    void shouldNotThrowExceptionWhenPageSizeIsNullAndSearchAfterIsValid() {
        assertDoesNotThrow(() -> validator.Validate(null, UUID.randomUUID()));
    }

    @Test
    void shouldNotThrowExceptionWhenSearchAfterIsNullAndPageSizeIsValid() {
        assertDoesNotThrow(() -> validator.Validate(10, null));
    }

    @Test
    void shouldNotThrowExceptionWhenBothPageSizeAndSearchAfterAreNull() {
        assertDoesNotThrow(() -> validator.Validate(null, null));
    }
    @Test
    void shouldThrowExceptionWhenPageSizeIsLessThanOne() {
        assertThrows(InvalidRequest.class, () -> validator.Validate(0, UUID.randomUUID()));
    }
}
