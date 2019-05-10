package uk.gov.hmcts.reform.professionalapi.controller.request;

public interface RequestValidator {

    void validate(OrganisationCreationRequest organisationCreationRequest);
}
