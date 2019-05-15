package uk.gov.hmcts.reform.professionalapi.controller.request;

import java.util.UUID;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus;

public interface UpdateOrganisationValidator {

    void validate(Organisation existingOrganisation, OrganisationStatus inputStatus, UUID inputOrganisationIdentifier);

}
