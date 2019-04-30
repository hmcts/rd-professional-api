package uk.gov.hmcts.reform.professionalapi.infrastructure.controllers.request;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Java6Assertions.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;

import org.junit.Test;

public class DXAddressCreationRequestTest {

	private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

	private final DXAddressCreationRequestValidator dxValidator = new DXAddressCreationRequestValidator();

	@Test
	public void has_mandatory_fields_specified_not_null() {

		DXAddressCreationRequest dxAddressCreationRequest = new DXAddressCreationRequest(null, null);

		Set<ConstraintViolation<DXAddressCreationRequest>> violations = validator.validate(dxAddressCreationRequest);

		assertThat(violations.size()).isEqualTo(2);
	}

	@Test
	public void creates_dx_address_creation_request_correctly() {

		DXAddressCreationRequest dxAddressCreationRequest = new DXAddressCreationRequest("some-number",
				"some-exchange");

		assertThat(dxAddressCreationRequest.getDxNumber()).isEqualTo("some-number");
		assertThat(dxAddressCreationRequest.getDxExchange()).isEqualTo("some-exchange");
	}


	@Test
	public void does_not_create_dx_address_creation_request_when_number_does_not_have_a_space() {

		List<DXAddressCreationRequest> dxAddresses = new ArrayList<>();
		DXAddressCreationRequest dxAddressCreationRequest = new DXAddressCreationRequest("DX12345678901", "some-exchange");
		dxAddresses.add(dxAddressCreationRequest);

		List<PbaAccountCreationRequest> pbaAccounts = new ArrayList<>();
		PbaAccountCreationRequest pbaAccountCreationRequests = new PbaAccountCreationRequest("some-pba-number");
		pbaAccounts.add(pbaAccountCreationRequests);

		UserCreationRequest superUser = new UserCreationRequest("some-name", "last-name", "some-email");

		List<ContactInformationCreationRequest> contactInformationList = new ArrayList<>();
		ContactInformationCreationRequest contactInformationCreationRequest = new ContactInformationCreationRequest(
				"some-address1", "some-address2", "some-address3", "some-town-city", "some-county", "some-country", "some-post-code", dxAddresses);
		contactInformationList.add(contactInformationCreationRequest);

		OrganisationCreationRequest organisationCreationRequest = new OrganisationCreationRequest(
				"org-name", "some-id", true, "some-number", "some-url", superUser, pbaAccounts, contactInformationList);

		dxValidator.validate(organisationCreationRequest);
		assertThat(dxAddressCreationRequest.getIsDXRequestValid()== false);

	}
	
	@Test
	public void does_not_create_dx_address_creation_request_when_number_does_not_start_with_DX_or_NI() {

		List<DXAddressCreationRequest> dxAddresses = new ArrayList<>();
		DXAddressCreationRequest dxAddressCreationRequest = new DXAddressCreationRequest("AB 1345678901", "some-exchange");
		dxAddresses.add(dxAddressCreationRequest);

		List<PbaAccountCreationRequest> pbaAccounts = new ArrayList<>();
		PbaAccountCreationRequest pbaAccountCreationRequests = new PbaAccountCreationRequest("some-pba-number");
		pbaAccounts.add(pbaAccountCreationRequests);

		UserCreationRequest superUser = new UserCreationRequest("some-name", "last-name", "some-email");

		List<ContactInformationCreationRequest> contactInformationList = new ArrayList<>();
		ContactInformationCreationRequest contactInformationCreationRequest = new ContactInformationCreationRequest(
				"some-address1", "some-address2", "some-address3", "some-town-city", "some-county", "some-country", "some-post-code", dxAddresses);
		contactInformationList.add(contactInformationCreationRequest);

		OrganisationCreationRequest organisationCreationRequest = new OrganisationCreationRequest(
				"org-name", "some-id", true, "some-number", "some-url", superUser, pbaAccounts, contactInformationList);

		dxValidator.validate(organisationCreationRequest);
		assertThat(dxAddressCreationRequest.getIsDXRequestValid()== false);
	}
	
	@Test
	public void does_not_create_dx_address_creation_request_when_number_contains_non_numerical_digits() {

		List<DXAddressCreationRequest> dxAddresses = new ArrayList<>();
		DXAddressCreationRequest dxAddressCreationRequest = new DXAddressCreationRequest("DX 123456789Z", "some-exchange");
		dxAddresses.add(dxAddressCreationRequest);

		List<PbaAccountCreationRequest> pbaAccounts = new ArrayList<>();
		PbaAccountCreationRequest pbaAccountCreationRequests = new PbaAccountCreationRequest("some-pba-number");
		pbaAccounts.add(pbaAccountCreationRequests);

		UserCreationRequest superUser = new UserCreationRequest("some-name", "last-name", "some-email");

		List<ContactInformationCreationRequest> contactInformationList = new ArrayList<>();
		ContactInformationCreationRequest contactInformationCreationRequest = new ContactInformationCreationRequest(
				"some-address1", "some-address2", "some-address3", "some-town-city", "some-county", "some-country", "some-post-code", dxAddresses);
		contactInformationList.add(contactInformationCreationRequest);

		OrganisationCreationRequest organisationCreationRequest = new OrganisationCreationRequest(
				"org-name", "some-id", true, "some-number", "some-url", superUser, pbaAccounts, contactInformationList);

		dxValidator.validate(organisationCreationRequest);
		assertThat(dxAddressCreationRequest.getIsDXRequestValid()== false);
	}

	@Test
	public void does_not_create_dx_address_creation_request_when_number_does_not_have_10_digits() {

		List<DXAddressCreationRequest> dxAddresses = new ArrayList<>();
		DXAddressCreationRequest dxAddressCreationRequest = new DXAddressCreationRequest("DX 123456789", "some-exchange");
		dxAddresses.add(dxAddressCreationRequest);

		List<PbaAccountCreationRequest> pbaAccounts = new ArrayList<>();
		PbaAccountCreationRequest pbaAccountCreationRequests = new PbaAccountCreationRequest("some-pba-number");
		pbaAccounts.add(pbaAccountCreationRequests);

		UserCreationRequest superUser = new UserCreationRequest("some-name", "last-name", "some-email");

		List<ContactInformationCreationRequest> contactInformationList = new ArrayList<>();
		ContactInformationCreationRequest contactInformationCreationRequest = new ContactInformationCreationRequest(
				"some-address1", "some-address2", "some-address3", "some-town-city", "some-county", "some-country", "some-post-code", dxAddresses);
		contactInformationList.add(contactInformationCreationRequest);

		OrganisationCreationRequest organisationCreationRequest = new OrganisationCreationRequest(
				"org-name", "some-id", true, "some-number", "some-url", superUser, pbaAccounts, contactInformationList);

		dxValidator.validate(organisationCreationRequest);
		assertThat(dxAddressCreationRequest.getIsDXRequestValid()== false);
	}
}
