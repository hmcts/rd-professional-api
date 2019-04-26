package uk.gov.hmcts.reform.professionalapi.infrastructure.controllers.request;

import static org.assertj.core.api.Java6Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;

import org.junit.Test;

public class DXAddressCreationRequestTest {

	private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

	@Test
	public void has_mandatory_fields_specified_not_null() {

		DXAddressCreationRequest dxAddressCreationRequest = new DXAddressCreationRequest(null, null);

		Set<ConstraintViolation<DXAddressCreationRequest>> violations = validator.validate(dxAddressCreationRequest);

		assertThat(violations.size()).isEqualTo(0);
	}

	@Test
	public void creates_dx_address_creation_request_correctly() {

		DXAddressCreationRequest dxAddressCreationRequest = new DXAddressCreationRequest("some-number",
				"some-exchange");

		assertThat(dxAddressCreationRequest.getDxNumber()).isEqualTo("some-number");
		assertThat(dxAddressCreationRequest.getDxExchange()).isEqualTo("some-exchange");
	}
}
