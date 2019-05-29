package uk.gov.hmcts.reform.professionalapi.controller.request;

import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus;

public interface UpdateOrganisationValidator {

    void validate(Organisation existingOrganisation, OrganisationStatus inputStatus, String inputOrganisationIdentifier);

}
