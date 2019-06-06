package uk.gov.hmcts.reform.professionalapi.controller.request;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus;

@Component
@Slf4j
public class OrganisationStatusValidatorImpl implements UpdateOrganisationValidator {

    @Override
    public void validate(Organisation existingOrganisation, OrganisationStatus inputStatus, String inputOrganisationIdentifier) {
        log.info("Into Organisation status validator...");

        validateOrganisationStatus(inputStatus, existingOrganisation.getStatus(), inputOrganisationIdentifier);

        log.info("Validation completed for Update Organisation...");
    }

    private void validateOrganisationStatus(OrganisationStatus inputRequestOrganisationStatus, OrganisationStatus existingStatus, String inputOrganisationIdentifier) {
        if (existingStatus == OrganisationStatus.DELETED) {
            String errorMessage = "Cannot amend status since existing organisation status is DELETED for organisationIdentifier: " + inputOrganisationIdentifier;
            log.error(errorMessage);
            throw new InvalidRequest(errorMessage);
        } else if (inputRequestOrganisationStatus == OrganisationStatus.PENDING && existingStatus == OrganisationStatus.ACTIVE) {
            String errorMessage = "Cannot amend status to PENDING since existing organisation status is ACTIVE for organisationIdentifier: " + inputOrganisationIdentifier;
            log.error(errorMessage);
            throw new InvalidRequest(errorMessage);
        }
    }
}
