package uk.gov.hmcts.reform.professionalapi.controller.request;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus;


@Component
@Slf4j
public class OrganisationIdentifierValidatorImpl implements UpdateOrganisationValidator {

    @Override
    public void validate(Organisation existingOrganisation, OrganisationStatus inputStatus, String inputOrganisationIdentifier) {
        log.info("Into Organisation identifier validator...");

        checkOrganisationDoesNotExist(existingOrganisation, inputOrganisationIdentifier);

        log.info("Validation completed for identifier Organisation...");
    }

    private void checkOrganisationDoesNotExist(Organisation organisation, String inputOrganisationIdentifier) {
        if (null == organisation) {
            String errorMessage = "Organisation not found with organisationIdentifier: " + inputOrganisationIdentifier;
            log.error(errorMessage);
            throw new EmptyResultDataAccessException(errorMessage, 1);
        }
    }

}
