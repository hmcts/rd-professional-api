package uk.gov.hmcts.reform.professionalapi.service;

import java.util.UUID;

import uk.gov.hmcts.reform.professionalapi.controller.request.NewUserCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.response.NewUserResponse;

public interface ProfessionalUserServiceI {

    NewUserResponse addNewUserToAnOrganisation(NewUserCreationRequest newUserCreationRequest, UUID organisationIdentifier);

}
