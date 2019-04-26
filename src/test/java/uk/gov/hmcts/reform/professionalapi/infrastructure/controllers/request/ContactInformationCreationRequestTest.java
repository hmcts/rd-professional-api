package uk.gov.hmcts.reform.professionalapi.infrastructure.controllers.request;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;

import org.junit.Before;
import org.junit.Test;

public class ContactInformationCreationRequestTest {

	private List<DXAddressCreationRequest> dxAddresses = new ArrayList<>();

	private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

	@Test
	public void has_mandatory_fields_specified_not_null() {

		ContactInformationCreationRequest contactInformationCreationRequest = new ContactInformationCreationRequest(
				null, null, null, null, null, null, null, null);

		Set<ConstraintViolation<ContactInformationCreationRequest>> violations = validator
				.validate(contactInformationCreationRequest);

		assertThat(violations.size()).isEqualTo(1);
	}

	@Test
	public void creates_contact_information_creation_request_correctly() {
		DXAddressCreationRequest dxAddressCreationRequest = new DXAddressCreationRequest("some-address", "some-exchange");

		dxAddresses.add(dxAddressCreationRequest);

		ContactInformationCreationRequest contactInformationCreationRequest = new ContactInformationCreationRequest(
				"some-address1", "some-address2", "some-address3", "some-town-city", "some-county", "some-country",
				"some-post-code", dxAddresses);

		assertThat(contactInformationCreationRequest.getAddressLine1()).isEqualTo("some-address1");
		assertThat(contactInformationCreationRequest.getAddressLine2()).isEqualTo("some-address2");
		assertThat(contactInformationCreationRequest.getAddressLine3()).isEqualTo("some-address3");
		assertThat(contactInformationCreationRequest.getTownCity()).isEqualTo("some-town-city");
		assertThat(contactInformationCreationRequest.getCounty()).isEqualTo("some-county");
		assertThat(contactInformationCreationRequest.getCountry()).isEqualTo("some-country");
		assertThat(contactInformationCreationRequest.getPostCode()).isEqualTo("some-post-code");
		assertThat(contactInformationCreationRequest.getDxAddress()).isEqualTo(dxAddresses);
	}
}
