package uk.gov.hmcts.reform.professionalapi.controller.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;

public class NewUserResponse {

    @JsonProperty
    private final String userIdentifier;

    public NewUserResponse(ProfessionalUser user) {

        this.userIdentifier = user.getUserIdentifier();
    }

    public String getUserIdentifier() {
        return userIdentifier;
    }
}
