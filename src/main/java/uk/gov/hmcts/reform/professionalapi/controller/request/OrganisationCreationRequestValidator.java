package uk.gov.hmcts.reform.professionalapi.controller.request;

import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class OrganisationCreationRequestValidator {

    private final List<OrganisationRequestValidator> validators;

    public OrganisationCreationRequestValidator(List<OrganisationRequestValidator> validators) {
        this.validators = validators;
    }

    public void validate(OrganisationCreationRequest organisationCreationRequest) {
        validators.forEach(v -> v.validate(organisationCreationRequest));
    }

}
