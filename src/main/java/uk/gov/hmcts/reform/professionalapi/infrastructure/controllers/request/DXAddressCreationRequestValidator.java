package uk.gov.hmcts.reform.professionalapi.infrastructure.controllers.request;

import static java.util.Objects.requireNonNull;

import java.util.List;

public class DXAddressCreationRequestValidator implements OrganisationRequestValidator {

	@Override
	public void validate(OrganisationCreationRequest organisationCreationRequest) {

		List<ContactInformationCreationRequest> contactInformationCreationRequests = organisationCreationRequest
				.getContactInformation();

		contactInformationCreationRequests.forEach(contactInfo -> {
			contactInfo.getDxAddress().forEach(dxAdd -> {
				if ((dxAdd.getDxNumber().startsWith("DX ") || dxAdd.getDxNumber().startsWith("NI "))) {

					String dxNumberToken[] = dxAdd.getDxNumber().split(" ");
					String dxNumberDigits = dxNumberToken[1];

					if (dxNumberDigits.length() != 10) {

						throw new InvalidRequest("DX Address Number should contain 10 numerical digits");
					}

					try {
						Integer.valueOf(dxNumberDigits);
					} catch (NumberFormatException ne) {

						throw new InvalidRequest("DX Address Number is invalid format");
					}

				} else {
					throw new InvalidRequest(
							"DX Address Number should start with either 'DX' or 'NI' and be followed by a space");
				}
			});
		});
	}

}
