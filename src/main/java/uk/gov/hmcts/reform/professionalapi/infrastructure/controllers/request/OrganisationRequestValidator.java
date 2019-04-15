package uk.gov.hmcts.reform.professionalapi.infrastructure.controllers.request;

public interface OrganisationRequestValidator {

    void validate(OrganisationCreationRequest organisationCreationRequest);
}
