package uk.gov.hmcts.reform.professionalapi.service;

import uk.gov.hmcts.reform.professionalapi.controller.request.NewUserCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.response.OrganisationResponse;

import java.util.List;
import java.util.UUID;

public interface ProfessionalUserServiceI {

    OrganisationResponse addNewUserToAnOrganisation(NewUserCreationRequest newUserCreationRequest, UUID organisationIdentifier);

    List<String> addRolesToUser(List<String> roles);

}
