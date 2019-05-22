package uk.gov.hmcts.reform.professionalapi.controller.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;

import java.util.UUID;

public class NewUserResponse {

    @JsonProperty
    private final UUID userIdentifier;

    public NewUserResponse(ProfessionalUser user) {

        this.userIdentifier = user.getProfessionalUserIdentifier();
    }

}
