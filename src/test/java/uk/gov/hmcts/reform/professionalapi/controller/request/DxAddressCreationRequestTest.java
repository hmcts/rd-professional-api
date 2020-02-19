package uk.gov.hmcts.reform.professionalapi.controller.request;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;

import org.junit.Test;

import uk.gov.hmcts.reform.professionalapi.controller.request.DxAddressCreationRequest;

public class DxAddressCreationRequestTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    public void has_mandatory_fields_specified_not_null() {

        DxAddressCreationRequest dxAddressCreationRequest = new DxAddressCreationRequest(null, null);

        Set<ConstraintViolation<DxAddressCreationRequest>> violations = validator.validate(dxAddressCreationRequest);

        assertThat(violations.size()).isEqualTo(2);
    }

    @Test
    public void creates_dx_address_creation_request_correctly() {

        DxAddressCreationRequest dxAddressCreationRequest = new DxAddressCreationRequest("some-number",
                "some-exchange");

        assertThat(dxAddressCreationRequest.getDxNumber()).isEqualTo("some-number");
        assertThat(dxAddressCreationRequest.getDxExchange()).isEqualTo("some-exchange");
    }

    @Test
    public void does_not_create_dx_address_creation_request_when_number_does_not_have_a_space() {
        DxAddressCreationRequest dxAddressCreationRequest = new DxAddressCreationRequest("DX12345678901", "some-exchange");

        Set<ConstraintViolation<DxAddressCreationRequest>> violations = validator
                .validate(dxAddressCreationRequest);

        assertThat(violations.size()).isEqualTo(1);

    }

    @Test
    public void does_not_create_dx_address_creation_request_when_number_does_not_start_with_dx_or_ni() {
        DxAddressCreationRequest dxAddressCreationRequest = new DxAddressCreationRequest("AB 1345678901", "some-exchange");

        Set<ConstraintViolation<DxAddressCreationRequest>> violations = validator
                .validate(dxAddressCreationRequest);

        assertThat(violations.size()).isEqualTo(1);
    }

    @Test
    public void does_not_create_dx_address_creation_request_when_number_contains_non_numerical_digits() {
        DxAddressCreationRequest dxAddressCreationRequest = new DxAddressCreationRequest("DX 123456789Z", "some-exchange");

        Set<ConstraintViolation<DxAddressCreationRequest>> violations = validator
                .validate(dxAddressCreationRequest);

        assertThat(violations.size()).isEqualTo(1);
    }

    @Test
    public void does_not_create_dx_address_creation_request_when_number_does_not_have_10_digits() {
        DxAddressCreationRequest dxAddressCreationRequest = new DxAddressCreationRequest("DX 123456789", "some-exchange");

        Set<ConstraintViolation<DxAddressCreationRequest>> violations = validator
                .validate(dxAddressCreationRequest);

        assertThat(violations.size()).isEqualTo(1);
    }

    @Test
    public void buildMethodtest() {
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
