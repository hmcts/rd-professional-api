package uk.gov.hmcts.reform.professionalapi.controller.request;

import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class OrganisationCreationRequestValidator {

    private final List<RequestValidator> validators;

    public OrganisationCreationRequestValidator(List<RequestValidator> validators) {
        this.validators = validators;
    }

    public void validate(OrganisationCreationRequest organisationCreationRequest) {
        validators.forEach(v -> v.validate(organisationCreationRequest));
    }

}
