package uk.gov.hmcts.reform.professionalapi.controller.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;

@NoArgsConstructor
public class SuperUserResponse {

    @JsonProperty
    private String firstName;
    @JsonProperty
    private String lastName;
    @JsonProperty
    private String email;


    public SuperUserResponse(ProfessionalUser professionalUser) {

        this.firstName = professionalUser.getFirstName();
        this.lastName = professionalUser.getLastName();
        this.email = professionalUser.getEmailAddress();

    }
}
