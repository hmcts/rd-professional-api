package uk.gov.hmcts.reform.professionalapi.util;

import uk.gov.hmcts.reform.professionalapi.controller.request.NewUserCreationRequest;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;

public interface ProfessionalUserUtil {

    static ProfessionalUser createProfessionalUser(NewUserCreationRequest newUserCreationRequest, Organisation organisation) {
        return new ProfessionalUser(
                newUserCreationRequest.getFirstName(),
                newUserCreationRequest.getLastName(),
                newUserCreationRequest.getEmail().toLowerCase(),
                organisation);
    }
}
