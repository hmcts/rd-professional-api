package uk.gov.hmcts.reform.professionalapi.controller.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;

@Getter
@Setter
@NoArgsConstructor
public class NewUserResponse {

    @JsonProperty
    private  String userIdentifier;
    @JsonProperty
    private String  idamStatus;

    public NewUserResponse(ProfessionalUser user) {

        this.userIdentifier = user.getUserIdentifier().toString();
    }

    public NewUserResponse(UserProfileCreationResponse userProfileCreationResponse) {
        this.userIdentifier = userProfileCreationResponse.getIdamId();
    }

    public String getUserIdentifier() {
        return userIdentifier;
    }
}
