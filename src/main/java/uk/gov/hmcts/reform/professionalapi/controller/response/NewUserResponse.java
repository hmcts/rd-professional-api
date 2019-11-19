package uk.gov.hmcts.reform.professionalapi.controller.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;

@Setter
@NoArgsConstructor
public class NewUserResponse {

    @JsonProperty
    private  String userIdentifier;

    public NewUserResponse(ProfessionalUser user) {

        this.userIdentifier = user.getUserIdentifier();
    }

    public String getUserIdentifier() {
        return userIdentifier;
    }
}
