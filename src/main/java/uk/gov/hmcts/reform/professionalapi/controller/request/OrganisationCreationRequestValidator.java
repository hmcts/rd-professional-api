package uk.gov.hmcts.reform.professionalapi.controller.request;

import static uk.gov.hmcts.reform.professionalapi.generator.ProfessionalApiGenerator.LENGTH_OF_ORGANISATION_IDENTIFIER;
import static uk.gov.hmcts.reform.professionalapi.generator.ProfessionalApiGenerator.ORGANISATION_IDENTIFIER_FORMAT_REGEX;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus;

@Component
@Slf4j
public class OrganisationCreationRequestValidator {

    private final List<RequestValidator> validators;

    public OrganisationCreationRequestValidator(List<RequestValidator> validators) {
        this.validators = validators;
    }

    public void validate(OrganisationCreationRequest organisationCreationRequest) {
        validators.forEach(v -> v.validate(organisationCreationRequest));
    }

    public static boolean contains(String status) {
        for (OrganisationStatus type : OrganisationStatus.values()) {
            if (type.name().equalsIgnoreCase(status)) {
                return true;
            }
        }
        return false;
    }

    public void validateOrganisationIdentifier(String inputOrganisationIdentifier) {

        if (null == inputOrganisationIdentifier || LENGTH_OF_ORGANISATION_IDENTIFIER != inputOrganisationIdentifier.length() || !inputOrganisationIdentifier.matches(ORGANISATION_IDENTIFIER_FORMAT_REGEX)) {
            String errorMessage = "Invalid organisationIdentifier provided organisationIdentifier: " + inputOrganisationIdentifier;
            log.error(errorMessage);
            throw new EmptyResultDataAccessException(1);
        }
    }

    public void isOrganisationActive(Organisation organisation) {

        if (organisation == null) {
            log.error("Organisation not found");
            throw new EmptyResultDataAccessException("Organisation not found", 1);
        } else if (!organisation.isOrganisationStatusActive()) {
            log.error("Organisation is not active. Cannot add new users");
            throw new EmptyResultDataAccessException("Organisation is not active. Cannot add new users", 1);
        }
    }
}
