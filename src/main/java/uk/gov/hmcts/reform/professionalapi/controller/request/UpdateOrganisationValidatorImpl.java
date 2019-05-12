package uk.gov.hmcts.reform.professionalapi.controller.request;

import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.professionalapi.domain.*;
import uk.gov.hmcts.reform.professionalapi.persistence.OrganisationRepository;


@Service
@Slf4j
public class UpdateOrganisationValidatorImpl implements UpdateOrganisationValidator {

    private String existingName;
    private OrganisationStatus existingStatus;
    private String existingSraId;
    private Boolean existingSraRegulated;
    private String existingCompanyUrl;
    private UUID existingOrganisationIdentifier;
    private OrganisationStatus inputRequestOrganisationStatus;

    @Autowired
    private OrganisationRepository organisationRepository;

    @Override
    public void validate(OrganisationCreationRequest organisationCreationRequest, String inputOrganisationIdentifier) {
        log.info("Into UpdateOrganisationValidator...");
        UUID validInputOrganisationIdentifier = validateAndReturnInputOrganisationIdentifier(inputOrganisationIdentifier);
        Organisation organisation = organisationRepository.findByOrganisationIdentifier(validInputOrganisationIdentifier);

        checkOrganisationDoesNotExist(organisation, validInputOrganisationIdentifier);

        existingStatus = organisation.getStatus();
        existingSraId = organisation.getSraId();
        existingSraRegulated = organisation.getSraRegulated();
        existingCompanyUrl = organisation.getCompanyUrl();
        existingOrganisationIdentifier = organisation.getOrganisationIdentifier();
        inputRequestOrganisationStatus = organisationCreationRequest.getStatus();

        validateOrganisationStatus(inputRequestOrganisationStatus, existingStatus, validInputOrganisationIdentifier);
        log.info("Validation completed for Update Organisation...");
    }

    private void checkOrganisationDoesNotExist(Organisation organisation, UUID inputOrganisationIdentifier) {
        if (organisation == null) {
            String errorMessage = "Organisation not found with organisationIdentifier: " + inputOrganisationIdentifier;
            log.error(errorMessage);
            throw new EmptyResultDataAccessException(errorMessage, 1);
        }
    }

    private void validateOrganisationStatus(OrganisationStatus inputRequestOrganisationStatus, OrganisationStatus existingStatus, UUID inputOrganisationIdentifier) {
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

    private UUID validateAndReturnInputOrganisationIdentifier(String inputOrganisationIdentifier) {
        UUID orgIdentifier = null;
        try {
            orgIdentifier = UUID.fromString(inputOrganisationIdentifier);
        } catch (IllegalArgumentException ex) {
            String errorMessage = "Invalid organisationIdentifier provided organisationIdentifier: " + inputOrganisationIdentifier;
            log.error(errorMessage);
            throw new InvalidRequest(errorMessage);
        }
        return orgIdentifier;
    }
}
