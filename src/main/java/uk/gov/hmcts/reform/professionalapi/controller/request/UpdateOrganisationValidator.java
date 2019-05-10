package uk.gov.hmcts.reform.professionalapi.controller.request;

import java.util.UUID;

public interface UpdateOrganisationValidator {

	void validate(OrganisationCreationRequest organisationCreationRequest, String inputOrganisationIdentifier);

}
