package uk.gov.hmcts.reform.professionalapi.controller.request;


public interface UpdateOrganisationValidator {

    void validate(OrganisationCreationRequest organisationCreationRequest, String inputOrganisationIdentifier);

}
