package uk.gov.hmcts.reform.professionalapi.controller.request;

import java.util.List;
import java.util.UUID;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus;

@Component
@Slf4j
public class UpdateOrganisationRequestValidator {

    private final List<UpdateOrganisationValidator> validators;

    public UpdateOrganisationRequestValidator(List<UpdateOrganisationValidator> validators) {
        this.validators = validators;
    }

    public void validateStatus(Organisation existingOrganisation, OrganisationStatus inputStatus, UUID inputOrganisationIdentifier) {
        validators.forEach(v -> v.validate(existingOrganisation, inputStatus, inputOrganisationIdentifier));
    }
}