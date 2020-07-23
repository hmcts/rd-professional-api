package uk.gov.hmcts.reform.professionalapi.controller.request.validator.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.professionalapi.controller.request.InvalidRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.validator.OrganisationIdentifierValidator;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus;

@Component
@Slf4j
public class OrganisationStatusValidatorImpl implements OrganisationIdentifierValidator {

    @Value("${loggingComponentName}")
    private String loggingComponentName;

    @Override
    public void validate(Organisation existingOrganisation, OrganisationStatus inputStatus,
                         String inputOrganisationIdentifier) {
        //Into Organisation status validator

        validateOrganisationStatus(inputStatus, existingOrganisation.getStatus(), inputOrganisationIdentifier);

        //Validation completed for Update Organisation
    }

    private void validateOrganisationStatus(OrganisationStatus inputRequestOrganisationStatus,
                                            OrganisationStatus existingStatus, String inputOrganisationIdentifier) {
        if (existingStatus == OrganisationStatus.DELETED) {
            String errorMessage = "{}:: Cannot amend status since existing organisation status is DELETED for "
                    .concat("organisationIdentifier: ") + inputOrganisationIdentifier;
            log.error(errorMessage, loggingComponentName);
            throw new InvalidRequest(errorMessage);
        } else if (inputRequestOrganisationStatus == OrganisationStatus.PENDING
                && existingStatus == OrganisationStatus.ACTIVE) {
            String errorMessage = "{}:: Cannot amend status to PENDING since existing organisation status is ACTIVE for "
                    .concat("organisationIdentifier: ") + inputOrganisationIdentifier;
            log.error(errorMessage, loggingComponentName);
            throw new InvalidRequest(errorMessage);
        }
    }
}
