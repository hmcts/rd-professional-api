package uk.gov.hmcts.reform.professionalapi.controller.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.UUID;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;

public class NewUserResponse {

    @JsonProperty
    private final UUID userIdentifier;

    public NewUserResponse(ProfessionalUser user) {

        this.userIdentifier = user.getUserIdentifier();
    }

    public UUID getUserIdentifier() {
        return userIdentifier;
    }
}
