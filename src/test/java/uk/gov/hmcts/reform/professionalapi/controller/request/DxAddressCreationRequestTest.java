package uk.gov.hmcts.reform.professionalapi.controller.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
class DxAddressCreationRequestTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void test_has_mandatory_fields_specified_not_null() {

        DxAddressCreationRequest dxAddressCreationRequest = new DxAddressCreationRequest(null,
                null);

        Set<ConstraintViolation<DxAddressCreationRequest>> violations = validator.validate(dxAddressCreationRequest);

        assertThat(violations).hasSize(2);
    }

    @Test
    void test_creates_dx_address_creation_request_correctly() {

        DxAddressCreationRequest dxAddressCreationRequest = new DxAddressCreationRequest("some-number",
                "some-exchange");

        assertThat(dxAddressCreationRequest.getDxNumber()).isEqualTo("some-number");
        assertThat(dxAddressCreationRequest.getDxExchange()).isEqualTo("some-exchange");
    }

    @Test
    void test_does_not_create_dx_address_creation_request_when_number_does_not_have_a_space() {
        DxAddressCreationRequest dxAddressCreationRequest = new DxAddressCreationRequest("DX12345678901",
                "some-exchange");

        Set<ConstraintViolation<DxAddressCreationRequest>> violations = validator
                .validate(dxAddressCreationRequest);

        assertThat(violations).hasSize(1);

    }

    @Test
    void test_does_not_create_dx_address_creation_request_when_number_does_not_start_with_dx_or_ni() {
        DxAddressCreationRequest dxAddressCreationRequest = new DxAddressCreationRequest("AB 1345678901",
                "some-exchange");

        Set<ConstraintViolation<DxAddressCreationRequest>> violations = validator
                .validate(dxAddressCreationRequest);

        assertThat(violations).hasSize(1);
    }

    @Test
    void test_does_not_create_dx_address_creation_request_when_number_contains_non_numerical_digits() {
        DxAddressCreationRequest dxAddressCreationRequest = new DxAddressCreationRequest("DX 123456789Z",
                "some-exchange");

        Set<ConstraintViolation<DxAddressCreationRequest>> violations = validator
                .validate(dxAddressCreationRequest);

        assertThat(violations).hasSize(1);
    }

    @Test
    void test_does_not_create_dx_address_creation_request_when_number_does_not_have_10_digits() {
        DxAddressCreationRequest dxAddressCreationRequest = new DxAddressCreationRequest("DX 123456789",
                "some-exchange");

        Set<ConstraintViolation<DxAddressCreationRequest>> violations = validator
                .validate(dxAddressCreationRequest);

        assertThat(violations).hasSize(1);
    }

    @Test
    void test_buildMethod() {
        String dxExchange = "dxExchange";
        String dxNumber = "dxExchange";

        DxAddressCreationRequest dxAddressCreationRequest = DxAddressCreationRequest.dxAddressCreationRequest()
                .dxExchange(dxExchange)
                .dxNumber(dxNumber)
                .build();

        assertThat(dxAddressCreationRequest.getDxExchange()).isEqualTo(dxExchange);
        assertThat(dxAddressCreationRequest.getDxNumber()).isEqualTo(dxNumber);

    }
}
