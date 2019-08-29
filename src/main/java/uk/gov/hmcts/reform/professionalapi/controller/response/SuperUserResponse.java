package uk.gov.hmcts.reform.professionalapi.controller.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.hmcts.reform.professionalapi.domain.SuperUser;

public class SuperUserResponse {

    @JsonProperty
    private String firstName;
    @JsonProperty
    private String lastName;
    @JsonProperty
    private String email;


    public SuperUserResponse(SuperUser professionalUser) {

        getSuperUserResponse(professionalUser);

    }

    private void getSuperUserResponse(SuperUser professionalUser) {
        this.firstName = professionalUser.getFirstName();
        this.lastName = professionalUser.getLastName();
        this.email = professionalUser.getEmailAddress();
    }
}
