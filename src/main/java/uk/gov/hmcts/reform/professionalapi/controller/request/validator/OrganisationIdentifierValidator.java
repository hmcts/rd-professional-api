package uk.gov.hmcts.reform.professionalapi.controller.request.validator;

import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus;

public interface OrganisationIdentifierValidator {

    void validate(Organisation existingOrganisation, OrganisationStatus inputStatus, String inputOrganisationIdentifier);

}
