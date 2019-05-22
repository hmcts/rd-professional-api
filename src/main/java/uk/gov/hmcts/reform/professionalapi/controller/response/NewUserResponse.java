package uk.gov.hmcts.reform.professionalapi.controller.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;

public class NewUserResponse {

    @JsonProperty
    private final String email;

    public NewUserResponse(ProfessionalUser user) {

        this.email = user.getEmailAddress();
    }

}
