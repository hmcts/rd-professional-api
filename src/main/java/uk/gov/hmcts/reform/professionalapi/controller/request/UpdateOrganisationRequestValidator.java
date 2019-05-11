package uk.gov.hmcts.reform.professionalapi.controller.request;

import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class UpdateOrganisationRequestValidator {

	private final List<UpdateOrganisationValidator> validators;

	public UpdateOrganisationRequestValidator(List<UpdateOrganisationValidator> validators) {
		this.validators = validators;
	}

	public void validate(OrganisationCreationRequest organisationCreationRequest, String organisationIdentifier) {
		validators.forEach(v -> v.validate(organisationCreationRequest,organisationIdentifier));
	}
}